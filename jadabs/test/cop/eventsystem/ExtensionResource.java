/*
 * Created on Mar 4, 2003
 *
 * $Id: ExtensionResource.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.iks.cop.eventsystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.iks.cop.ComponentInitializer;
import ch.ethz.iks.cop.IComponent;
import ch.ethz.iks.cop.IComponentRepository;
import ch.ethz.iks.eventsystem.InitializationException;
import ch.ethz.iks.eventsystem.impl.AEvent;
import ch.ethz.iks.jxme.Element;
import ch.ethz.iks.jxme.IMessage;
import ch.ethz.iks.jxme.Message;

/**
 * An ExtensionResource contains all the required code, and information to instantiate
 * an extension. 
 * ExtensionsResource acts as an intermediate, adapter, between the real extension the the 
 * the class which uses the extension by initializing, starting, stopping the extension
 * 
 * @author andfrei
 */
public class ExtensionResource extends AEvent {

	private static Logger LOG = Logger.getLogger(ExtensionResource.class);

	/** insert Extension immediate request, 
	 * PRECONDITION: Extension code has already to be in the download directory
	 */
	public final static String INSERT_LOCAL 			= "ext_insloc";
	public final static String INSERT_REMOTE		= "ext_insrem";
	/** withdraw an extension request */
	public final static String WITHDRAW				= "ext_withdraw";

	// Represents the state of the Extension
	static final int NOT_LOADED		= 0;	// Extension class specified but instance not created
	static final int LOADED				= 1;	// Extension classes are loaded and instance created
	static final int INITIALIZED			= 2;	// Extension is initialized but not started
	static final int STARTING				= 5;  // Extension is now in the starting phase
	static final int STARTED				= 3;	// Extension is started and running
	static final int STOPPED				= 4;	// Extension stoped
	
	static int ExtStatus = NOT_LOADED;
	
	// id represents a unique ID over all possible extension in the world
	private String extID;
	// code is a .jar file which has to be sent or can be downloaded by a 
	// classloader to instantiate the classes.
	private String codebase;
	// classname is the main class and represents an Aspect
	private String classname;
	
	private String extResLocation = "./bin/ext/";  // download is the default directory for extension resources
	private String extDestLoc		= "./tmp/";
	
	// Class which represents this extension
	private Class clas;
	// Instance of clas, the Extension Object
	private IComponent extObj;
	
	// one classloader for each extension
	private ComponentInitializer extClassLoader;

	private IComponentRepository extSvc;

	// an extension Resource may depend on other extension resources
	private Vector extResDepsID = new Vector();  // List(ExtensionID)

	public ExtensionResource(){
		
	}

	/**
	 * Create an ExtensionResource which contains the information about the resource (.jar file), its location, 
	 * the main class and an Extension ID.
	 * 
	 * @param id  Extension ID
	 * @param extResLocation where the extension resources are stored
	 * @param codebase extension file (.jar) 
	 * @param classname the main classname which is instantiated as extension, including the package path
	 */
	public ExtensionResource(IComponentRepository extSvc, String urnid, String extResLocation, String codebase, String classname){
		
		this.extID = urnid;
		if (extResLocation != null)
			this.extResLocation = extResLocation;
		this.codebase = codebase;
		this.classname = classname;
		
		this.extSvc = extSvc;
	}

	protected void initClassLoader(){
		
		String jarfile = extResLocation + codebase;
		
		System.out.println("jarfile to be loaded: " + jarfile);
		
		ComponentInitializer extLoader = new ComponentInitializer( this, jarfile);
		
		Class clas = null;
		try {
			clas = extLoader.loadClass(classname, true);
			if (LOG.isInfoEnabled())
				LOG.info("class loaded: "+clas.getName());
		} catch( ClassNotFoundException cnfe){
			LOG.error("Class could not been loaded", cnfe);
		}
		

		setClassLoader(extLoader);
		setExtClass(clas);
	}



	/**
	 * Return the world unique ID.
	 * @return int
	 */
	public String getExtID(){
		return extID;
	}

	/**
	 * Return the CodeBase of the extension, usually a .jar file.
	 * @return String
	 */
	public String getCodeBase(){
		return codebase;
	}

	/**
	 * Set the directory where the Extension Resource is stored.
	 * 
	 * @param extResLocation
	 */
	public void setExtResLocation(String extResLocation){
		this.extResLocation = extResLocation;
	}

	/**
	 * Return the Path where the resource in the filesystem is stored.
	 * 
	 * @return
	 */
	public String getExtResLocation(){
		return extResLocation;
	}

	/**
	 * Return the className of the extension, the name of the aspect.
	 * 
	 * @return String
	 */
	public String getClassName(){
		return classname;
	}

	/**
	 * Set Class, wich represents this extension.
	 * 
	 * @param clas
	 */
	public void setExtClass(Class clas){
		
		this.clas = clas;
	}

	/**
	 * Set the instance of the Extension Class, the instance is supposed to be a singleton.
	 * 
	 * @param ext
	 */
	public void setExtObject(IComponent ext){
		extObj = ext;
	}

	/**
	 * Returns the Instance of the Extension Class, which is a singleton.
	 * @return
	 */
	public IComponent getExtObject(){
		return extObj;
	}
	/**
	 * Return the Class which represents this class.
	 * 
	 * @see java.lang.Object#getClass()
	 */
	public Class getExtClass(){
		return clas;
	}

	/**
	 * Set the ExtensionResource ClassLoader, each ExtensionResource has its own ClassLoader,
	 * which allows the ExtensionResource to be unloaded.
	 * 
	 * @param extClassLoader
	 */
	public void setClassLoader(ComponentInitializer extClassLoader){
		
		this.extClassLoader = extClassLoader;
	}

	public ComponentInitializer getClassLoader(){
		return extClassLoader;
	}

	public Class loadClassFromDependency(String className, boolean resolvelt) 
		throws ClassNotFoundException{
		
		for(Enumeration en = extResDepsID.elements(); en.hasMoreElements(); ){
			
			String extID = (String)en.nextElement();
			
			ExtensionResource extRes = extSvc.getComponentResource(extID);
			
			Class result = null;
			try{
				result = extRes.loadClass(className, resolvelt);
			} catch (ClassNotFoundException e) { }
			
			if (result != null)
				return result;
		}
		
		throw new ClassNotFoundException();

	}

	public synchronized Class loadClass(String className,  boolean resolveIt) 
		throws ClassNotFoundException {
	
		return extClassLoader.loadClass(className, resolveIt);

	}

	/**
	 * Add an Extension Resource Dependency, befor instantiating this extension all dependencies
	 * should alread be in the system.
	 * 
	 * @param extRes
	 */
	public void addExtResDependency(ExtensionResource extRes){
		
		extResDepsID.add(extRes.getExtID());
	}

	/**
	 * Return the ExtIDs from the ExtensResourse Dependencies.
	 * @return
	 */
	public String[] getExtResDepsID(){

		String[] depids = new String[extResDepsID.size()];

		int i = 0;
		for(Enumeration en = extResDepsID.elements(); en.hasMoreElements(); ){
			
			depids[i] = (String)en.nextElement();
		}

		return depids;

//		return (String[])extResDepsID.toArray();
	}

	/**
	 * Return the ExtensionResource ClassLoader.
	 * 
	 * @return ExtensionClassLoader
	 */
//	public ExtensionClassLoader getClassLoader(){
//		return extClassLoader;
//	}

	/**
	 * Returns the Extension Status, has to be tested with the given constants, NOT_LOADED, 
	 * LOADED, INITIALIZED, STARTED, STOPPED.
	 * 
	 */
	public int getExtStatus(){
		return ExtStatus;
	}

	/**
	 * Inititialize the extension.
	 *
	 */
	public void initExt() throws InstantiationException, IllegalAccessException{
		
//		if (ExtStatus == NOT_LOADED){

			// load and create Instance
			extObj = (IComponent)clas.newInstance();

			// init extension
			extObj.initServiceComponent();
//		}
//		else if (ExtStatus == LOADED){
			
//			// init extension
//			extObj.initExt();
//		}

		ExtStatus = INITIALIZED;
	}

	/**
	 * Start the extension with args.
	 * 
	 * @param args
	 */
	public void startExt(String[] args) throws InstantiationException {
		
		if (ExtStatus == INITIALIZED){
			
			ExtStatus = STARTING;
			
			extObj.startComponent(args);
			
			ExtStatus = STARTED;
		}
		else
			throw new InstantiationException("Extension has not been instantiated yet!");
	}
	
	/**
	 * Stop the extension.
	 *
	 */
	public void stopExt(){
		
		extObj.stopComponent();
		
		ExtStatus = STOPPED;
	}

	/**
	 * Stop the ExtensionResource from working, and delete the classloader.
	 * This ensures that the ExtensionResource class files get garbage collected and
	 * therefore unloaded.
	 * 
	 */
	public void stop(){
		
		if (extObj != null){
			extObj.stopComponent();
			extObj = null;
		}
		
		extClassLoader = null;
	}

// ------ Event methods

   public void init(IMessage msg){
		
	   try {
		   super.init(msg);
			
		   extID = Message.getElementString(msg, "extid");
		   codebase = Message.getElementString(msg, "code");
		   classname = Message.getElementString(msg, "classname");
			
			try {
				FileOutputStream out = new FileOutputStream(new File(extDestLoc + codebase));
				out.write(msg.getElement("filedat").getData());
				out.close();
		  	} catch (IOException e) {
		  	}
		
	   } catch(InitializationException ie){
		   LOG.error("couldn't initialize EventMessage with Message", ie);
	   } catch(IOException ioe){
		   LOG.error("couldn't initialize EventMessage with Message", ioe);
	   }
   }
	
   /**
	* Create a Message Type from the content.
	* 
	* @return Message
	*/
   public IMessage toMessage(Class clas){

		IMessage msg = null;
		if (clas != null)
			 msg = super.toMessage(clas);
		else
			msg = super.toMessage(ExtensionResource.class);
		
//	   IMessage msg = super.toMessage(ExtensionResource.class);

//	   msg.setElement(new Element(AEvent.TYPE,  ExtensionResource.class.getName()));
		 
	   msg.setElement(new Element("extid", extID));
	   msg.setElement(new Element("code", codebase));
	   msg.setElement(new Element("classname",  classname));
		
		if (tag.equals(INSERT_REMOTE)){
			
			// read file into a byte array
			byte[] bytes;
			try {
				bytes = getBytesFromFile(new File(extResLocation + codebase));
				msg.setElement(new Element(bytes, "filedat"));
			} catch (IOException ioe) {
				LOG.error("could not put file into message", ioe);
			}
			
			
		}
	   return (msg);
   }
	
   public static byte[] getBytesFromFile(File file) throws IOException {
		   InputStream is = new FileInputStream(file);
    
		   // Get the size of the file
		   long length = file.length();
    
		   // You cannot create an array using a long type.
		   // It needs to be an int type.
		   // Before converting to an int type, check
		   // to ensure that file is not larger than Integer.MAX_VALUE.
		   if (length > Integer.MAX_VALUE) {
			   LOG.error("File is to larg (bytes): "+length);
		   }
			
			LOG.error("length: "+length+", maxInt: "+Integer.MAX_VALUE);
	
		   // Create the byte array to hold the data
		   byte[] bytes = new byte[(int)length];
    
		   // Read in the bytes
		   int offset = 0;
		   int numRead = 0;
		   while (offset < bytes.length
				  && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
			   offset += numRead;
		   }
    
		   // Ensure all the bytes have been read in
		   if (offset < bytes.length) {
			   throw new IOException("Could not completely read file "+file.getName());
		   }
    
		   // Close the input stream and return bytes
		   is.close();
		   return bytes;
	   }
	
   public Object clone() throws CloneNotSupportedException{
	   Object clone = super.clone();
		
	   return clone;
   }

	public String toString(){
		return extID;
	}

	public void finalize(){
		LOG.info("called finalize");
	}

}
