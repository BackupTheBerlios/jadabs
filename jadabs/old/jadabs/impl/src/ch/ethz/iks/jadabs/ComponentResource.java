/*
 * Created on Mar 4, 2003
 *
 * $Id: ComponentResource.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */

package ch.ethz.iks.jadabs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import ch.ethz.iks.logger.ILogger;
import ch.ethz.iks.logger.Logger;

/**
 * A ComponentResource contains all the required code, and information to instantiate
 * a component. 
 * ComponentResource acts as an intermediate, adapter, between the real component the the 
 * the class which uses the component by initializing, starting, stopping the component.
 * 
 * The component resource uses a jar to package the component physically. The jar
 * file must also contain a manifest file with information about the main-class and implementation-version.
 * To create a jar file with the corresponding manifest file use ant's jar, manifest tags (ex. in this projects build.xml).
 * 
 * @author andfrei
 */
public class ComponentResource implements IComponentResource {

	private static ILogger LOG = Logger.getLogger(ComponentResource.class);

	/** insert Extension immediate request, 
	 * PRECONDITION: Extension code has already to be in the download directory
	 */
	public final static String INSERT_LOCAL 		= "ext_insloc";
	public final static String INSERT_REMOTE		= "ext_insrem";
	/** withdraw an extension request */
	public final static String WITHDRAW				= "ext_withdraw";

	// Represents the state of the Extension
	private int NOT_LOADED		= 0;	// Extension class specified but instance not created
	private int INITIALIZED		= 2;	// Extension is initialized but not started
	private int STARTED			= 3;	// Extension is started and running
	private int STOPPED			= 4;	// Extension stopped, can be started again
	private int DISPOSED        = 5;    // Component ready for cleanup
    
	private int copStatus = NOT_LOADED;
	
	// id represents a unique ID over all possible extension in the world
	private String extID;
	// code is a .jar file which has to be sent or can be downloaded by a 
	// classloader to instantiate the classes.
	private String codebase;
	// classname is the main class and represents an Aspect
	private String classname;
	
	/** version of jar file, increasing number, no x.x.x notation */
	private int version;
	
	private String extResLocation  = "./pcoprep/";  // download is the default directory for extension resources
    
    private String extDestLoc      = "./tmp/";
	
	// Class which represents this extension
	private Class clas;
	// Instance of clas, the Extension Object
	private IComponent extObj;


	// one classloader for each component
	protected MultiClassLoader extClassLoader;


	// an extension Resource may depend on other extension resources
	private Vector copResDeps;  // late binding use getComponentDeps(), List(codebase)

	/**
	 * Create an ExtensionResource which contains the information about the resource (.jar file), its location, 
	 * the main class and an Extension ID.
	 * 
	 * @param id  Extension ID
	 * @param extResLocation where the extension resources are stored
	 * @param codebase extension file (.jar) 
	 * @param classname the main classname which is instantiated as extension, including the package path
	 */
	protected ComponentResource(String urnid, String codebase, String classname){
		
		this.extID = urnid;
        this.classname = classname;
        this.codebase = codebase;
		
		
//		this.extResLocation = new File((String)Jadabs.Instance().
//                getProperty(Jadabs.PCOPREP)).getAbsolutePath();
        this.extResLocation = new File(BootstrapConstants.PCOPREP_DEFAULT).getAbsolutePath();
	}

	/**
	 * Give an additional version number, the number is supposed to increase and not of the form x.x.x .
	 * 
	 * @param urnid
	 * @param codebase
	 * @param classname
	 * @param version
	 */
	protected ComponentResource(String urnid, String codebase, String classname, int version){
		
		this(urnid, codebase, classname);
		this.version = version;
	}

	protected void initClassLoader(){
		String jarfile = extResLocation + File.separatorChar + codebase;
		
		LOG.info("jarfile to be loaded: " + jarfile);
			
		extClassLoader = new ComponentInitializer( this, jarfile);
		
	}



	/**
	 * Return the world unique ID.
	 * @return int
	 */
	public String getCopID(){
		return extID;
	}

	/**
	 * Return the CodeBase of the extension, usually a .jar file.
	 * @return String
	 */
	public String getCodeBase(){
		return codebase;
	}

	public int getVersion(){
		return version;
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

	public MultiClassLoader getClassLoader() {
		return extClassLoader; //micha: return a MultiClassLoader instead of a ComponentInitializer to be more flexible (e.g. ProxyLoader extends MultiClassLoader)

	}

	protected Class loadClassFromDependency(String className, boolean resolvelt) 
		throws ClassNotFoundException{
		
		for(Enumeration en = getComponentDeps().elements(); en.hasMoreElements(); ){
			
			String cbase = (String)en.nextElement();
			
			IComponentResource copres = ComponentRepository.Instance().getComponentResourceByCodebase(cbase);
			
			Class result = null;
			try{
				result = ((ComponentResource)copres).loadClass(className, resolvelt);
			} catch (ClassNotFoundException e) { }
			
			if (result != null) {
				//LOG.info("component "+this.codebase+" loaded "+className+" from dependency "+copres.getCodeBase());
				return result;
			}
		}
		
		throw new ClassNotFoundException();

	}
	
	
	// TODO: make protected (there is a nonsense compile error)
	public Vector getComponentDeps() {
		// lazy binding: read the deps information the first time it is needed.
		if (copResDeps == null){
			copResDeps = new Vector();
			
			// read the class-path information
			JarFile jarfile;
            String file = "" ;
			try {
                file = extResLocation + File.separatorChar + codebase;
                //BUGFIX: problems with deleting the file under windows, use a URL
                String OS = System.getProperty("os.name").toLowerCase();
                if (OS.indexOf("windows") > -1)
                {
                    URL urlfile= new URL("file:/"+file); 
                    file = urlfile.getFile();
                }
                
				jarfile = new JarFile(file);
				
				Manifest manifest = jarfile.getManifest();
				if (manifest != null){
					Attributes atts = manifest.getMainAttributes();
	
					String classpath = atts.getValue(Attributes.Name.CLASS_PATH);
					LOG.info(codebase + " deps classpath: " +classpath);
					
					if (classpath != null) {
						StringTokenizer st = new StringTokenizer(classpath);
						while (st.hasMoreTokens()) {
							copResDeps.add(st.nextToken());
						}
					}
				}
				
                jarfile.close();
			} catch (IOException e) {
				e.printStackTrace();
                LOG.error("couldn't read file: " + file);
			}
		}
		
		return copResDeps;
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
	public void addCopResDependency(ComponentResource extRes){
		
		copResDeps.add(extRes.getCopID());
	}

	/**
	 * Return the ExtIDs from the ExtensResourse Dependencies.
	 * @return
	 */
//	public String[] getExtResDepsID(){
//
//		String[] depids = new String[copResDepsID.size()];
//
//		int i = 0;
//		for(Enumeration en = copResDepsID.elements(); en.hasMoreElements(); ){
//			
//			depids[i] = (String)en.nextElement();
//		}
//
//		return depids;
//
////		return (String[])extResDepsID.toArray();
//	}
	
	/**
	 * Before initializing the own component init first the dependency components.
	 *
	 */
	private void initComponentDeps() throws InstantiationException, IllegalAccessException {
		
		for(Enumeration en = getComponentDeps().elements(); en.hasMoreElements(); ){
			
			String codebase = (String)en.nextElement();
			
			if (!ComponentRepository.Instance().isComponentInitializedOrStarted(codebase)){
				IComponentResource copres = LocalComponentLoader.getComponentResource(codebase);
				
				ComponentRepository.Instance().initComponent(copres);
				
			}
		}
		
	}

	/**
	 * Before initializing the own component init first the dependency components.
	 *
	 */
	private void startComponentDeps(String[] args) {
		
		for(Enumeration en = getComponentDeps().elements(); en.hasMoreElements(); ){
			
			String codebase = (String)en.nextElement();
			
			IComponentResource copres = (IComponentResource)ComponentRepository.Instance().
				getComponentResourceByCodebase(codebase);
			
			if (!copres.isStarted())
				copres.startComponent(args);
			
		}
		
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
		return copStatus;
	}

	public boolean isService(){
		return classname != null;
	}

	/** 
	 * Checks if the component is initialized.
	 * 
	 * @return
	 */
	public boolean isInitialized(){
		return copStatus == INITIALIZED;
	}
	
	/**
	 * Checks if the ocmponent is already started.
	 * 
	 */
	public boolean isStarted(){
		
		return copStatus == STARTED;
	}

    /**
     * Checks if the ocmponent is stopped.
     * 
     */
    public boolean isStopped(){
        
        return copStatus == STOPPED;
    }
    
	/**
	 * Initialize Library Component, this loads each library with its own classloader.
	 * This allows to remove a library component without leaving anything back.
	 * 
	 */
	private void initLibraryComponent() {
		
		// initialize component classloader
		initClassLoader();
	}

	/**
	 * Inititialize the extension.
	 *
	 */
	private void initServiceComponent() throws InstantiationException, IllegalAccessException{

			// initialize component classloader
			initClassLoader();

			try {
				clas = extClassLoader.loadClass(classname, true);
				
				if (LOG.isInfoEnabled())
					LOG.info(this.getCodeBase()+" ("+this.getVersion()+") main class loaded: "+clas.getName());
					
				// load and create Instance
				// micha: allow singleton in contrast to: extObj = (IComponent)clas.newInstance();
				this.extObj = (IComponent) clas.getMethod(IComponent.factoryMethod, null).invoke(null,null);
					
				
			} catch( ClassNotFoundException cnfe){
				LOG.error(classname+" could not been loaded", cnfe);
			} catch (IllegalArgumentException e) {				
				LOG.error("The factory method of "+classname+" must be NOT take any arguments: \"public static "+clas.getName()+" "+IComponent.factoryMethod+" () \". Could not create an instance of it.",e); // no arguments
			} catch (SecurityException e) {						
				LOG.error("The factory method of "+classname+" must be public: \"public static "+clas.getName()+" "+IComponent.factoryMethod+" () \". Could not create an instance of it.",e);
			} catch (InvocationTargetException e) {				
				LOG.error("target is null, invocation exception"); // Target is null (static method)
			} catch (NoSuchMethodException e) {
				LOG.error(classname+" does not define the factory method \"public static "+classname+" "+IComponent.factoryMethod+" () \". Could not create an instance of it.",e);
			}
			
			try {
				 //		init extension
			 	//extObj.initComponent(); //andi: changed the initComponent to init(ComponentContext)
			 	IComponentContext context = new ComponentContext(this);
			 	extObj.init(context);
			 	
			} catch (Exception e) {
				LOG.error("Error occured on initialization of component main object "+extObj,e);
			}
			 // print classloader path
			 //DefaultBootStrap.traceClassLoader(extObj.getClass());
	}
	

	public void initComponent() throws InstantiationException, IllegalAccessException{
		
		if (!isInitialized() && !isStarted()){
			initComponentDeps();
			
			if (isService())
				initServiceComponent();
			else
				initLibraryComponent();
			
			LOG.info("Component initialized: " + codebase);
			
			copStatus = INITIALIZED;
		}
		
	}

	/**
	 * Start the extension with args.
	 * 
	 * @param args
	 */
	public void startComponent(String[] args) {
		
		if (isInitialized() && !isStarted()){
            copStatus = STARTED;
            
			if (isService()){
				startComponentDeps(args);
			
				extObj.startComponent(args);
			}
			LOG.debug("Component started: " + codebase);		
//			copStatus = STARTED;
		}
	}
	
	/**
	 * Stop the ExtensionResource from working, and delete the classloader.
	 * This ensures that the ExtensionResource class files get garbage collected and
	 * therefore unloaded.
	 * 
	 */
	public void stopComponent(){
		
		if (extObj != null){
			extObj.stopComponent();
//			extObj = null;
		}
		
//		extClassLoader = null;
		
		copStatus = STOPPED;
	}
    

    public void disposeComponent()
    {
        if (isService()){
            if (copStatus == STARTED)
                extObj.stopComponent();
            
            extObj.disposeComponent();
            extObj = null;
        }
        
        // check as long as migration still needs extClassLoader of type Multiclassloader
        if (extClassLoader instanceof ComponentInitializer)
            ((ComponentInitializer)extClassLoader).stop();
        else
            LOG.error("extClassLoader not instance of ComponentInitializer," +
                    "can't release classloader");
        
        extClassLoader = null;
        
        // set status;
        copStatus = DISPOSED;
        
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
	
	//micha: to be able to have two objects with same name in a collection (e.g. HashMap of repository)
	public boolean equals(Object o) {
		if (o instanceof IComponentResource) {
			IComponentResource cr = (IComponentResource)o;
			//TODO: enable as soon as IDFactory has been implemented: return this.extID.equals(cr.getCopID());
			return super.equals(o) && cr.getCodeBase().equals(this.codebase) && (cr.getVersion() == this.version);
		} else {
			return super.equals(o);
		}
	}

}
