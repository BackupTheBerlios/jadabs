package ch.ethz.iks.evolution.cop;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javassist.ClassPath;
import javassist.ClassPool;
import javassist.Loader;
import javassist.NotFoundException;
import javassist.Translator;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.mgr.ComponentEvolutionMain;
import ch.ethz.iks.jadabs.ComponentInitializer;
import ch.ethz.iks.proxy.IClassLoaderWithClassPath;


/**
 * Custom <code>ClassLoader</code> for components supporting evolution
 * Adds notification mechanisms similar to <code>javassist.Loader</code>
 * as well as class searching in the component's JarFile.
 * TODO: remove dependency to javassist library
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class UpgradeableComponentLoader extends ComponentInitializer implements IClassLoaderWithClassPath {

	private ClassPool pool;
	private Loader cl;
	private String path; 
	private ClassPath jarHandle;
	private int version;
	private Vector classesInCop = new Vector(12);
	private static Logger LOG = Logger.getLogger(UpgradeableComponentLoader.class);

	/**
	 * @param extRes
	 * @param jarName
	 * @param handler - the listener to notify on an invocation of <code>loadClassBytes()</code>
	 */
	public UpgradeableComponentLoader(UpgradeableComponentResource extRes, String jarName, Translator handler) throws NotFoundException {
		super(extRes, jarName);
		ClassPool dflt = ClassPool.getDefault();
		this.path = extRes.getExtResLocation() + extRes.getCodeBase(); 
		this.jarHandle = dflt.appendClassPath(this.path);
		
		pool = new ClassPool(dflt, handler);
		cl = new Loader(pool); 
		version = extRes.getVersion();
		try {
			this.classesInCop = getJarContents(extRes.getCodeBase());
            //LOG.info(jarName+" knows: "+classesInCop.toString());     
		} catch (IOException e) {
			LOG.error(this.path,e);
		}
		
	}
	
	/**
	 *  
	 * @param codebase
	 * @return - a list of the names of all files in the codebase of this component
	 * @throws IOException
	 */
	protected static Vector getJarContents( String codebase) throws IOException {
		// init a cache of class files in jar to speedup searching
		Vector content = new Vector();
		JarFile jar = new JarFile(ComponentEvolutionMain.PCOPREP + File.separator+ codebase);
		Enumeration files = jar.entries();
		while(files.hasMoreElements()) {
			ZipEntry classfile = (ZipEntry) files.nextElement();
			String cn = classfile.getName();
			int index = cn.lastIndexOf('.');
			if (index > 1) {
				cn = cn.substring(0,index);
			}
			content.add(cn.replace(File.separatorChar,'.'));
		}
		
		return content;
	}

	/**
	 * Adds support for bytecode manipulation at load time of classes
	 * Calls the registered <code>javassist.Translator</code> on loading the bytecode of the class having the given <code>className</code>
	 */
	protected byte [] loadClassBytes(String className) {
		byte [] result = null; 
		if ( !knowsClass(className) ) {
			// restrict access to classes not in this component, use loadClassFromDependency instead
			return null;
		}
		try {
			
			//Class c = cl.loadClass(className); // notifies Translators
			result = pool.write(className);
			
			return result;
		} catch (Throwable e) {
			LOG.error("FAILED to load "+className+" from "+path+" version "+version+": ",e); 
			
			return super.loadClassBytes(className);
		}
	}

	/**
	 * Search for class files on the components Jar.
	 * @param resourceName - the fully qualified name of the class
	 * @return true if the class belongs to this component
	 */
	public boolean knowsClass(String resourceName){
		boolean success = this.classesInCop.contains(resourceName);
		
		return success;
	}
	
	public String toString() {
		return this.getClass().getName()+" @ "+this.path+" : "+version;
	}

	
	void close() {
		
		this.pool.removeClassPath(jarHandle);
		
	}
	
	public String getClassPath() {
		return this.path;
	}
	
	public Class loadClassFromDependency(String className, boolean resolvelt) throws ClassNotFoundException {
		Class loaded = super.loadClassFromDependency(className, resolvelt);
		
		return loaded;
	}




}
