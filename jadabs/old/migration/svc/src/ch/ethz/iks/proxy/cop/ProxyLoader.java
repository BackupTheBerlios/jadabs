package ch.ethz.iks.proxy.cop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.mgr.ComponentEvolutionMain;
import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.ComponentResource;
import ch.ethz.iks.jadabs.MultiClassLoader;
import ch.ethz.iks.proxy.IClassLoaderWithClassPath;
import ch.ethz.iks.utils.Decode;


/**
 * A custom classloader that loads binaries from one specific folder.
 * This folder contains the class files of the compiled proxy classes
 * for the associated component (UpgradableComponentResource) it hides
 *  
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class ProxyLoader extends MultiClassLoader implements IClassLoaderWithClassPath {

	/**
	 * @param extRes - the component this ClassLoader works for
	 * @param codebase -  the name of the folder in the filesystem where the binaries of this component are
	 */
	public ProxyLoader(ComponentResource extRes, String codebase) {
		super();
		this.copRes = (ProxyComponentResource) extRes;	
		this.folderName = codebase;
		//LOG.info(" new proxyloader @ "+codebase);
	}
	
	private String folderName;
	private String path;
	private static Logger LOG = Logger.getLogger(ProxyLoader.class);
	private ProxyComponentResource copRes;

	public void setExtResLocation(String location) {
		this.path = location;
		//LOG.info("new ProxyLoader for cop "+ path +" = "+this);
	}
	
	/**
	 * @return a String that denotes the path on the filesystem where the binaries get loaded from
	 */
	public String getClassPath() {
		return this.path + this.folderName;
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.MultiClassLoader#loadClassBytes(java.lang.String)
	 */
	protected byte [] loadClassBytes(String className) {
		String simpleName = null;
		String pathToFile = null;
		try {
			// get package of class to load 
			String packagePrefix = Decode.getPackage(className);
			simpleName = Decode.getSimpleName(className);
			
			// get proxy folder of component and search for a matching .class file
			pathToFile = this.path + this.folderName + File.separator + packagePrefix.replace('.',File.separatorChar);
				File folder = new File(pathToFile);
				if ( ! folder.exists() ) return null;
 				File [] proxies = folder.listFiles();
				
				for (int fIndex = 0; fIndex < proxies.length; fIndex++) {
					File proxyClassFile = proxies[fIndex];
					String javaClass = proxyClassFile.getName();
					String suffix = null;
					int suffixStart = javaClass.indexOf('.');
					if (suffixStart > 1) {
						suffix = javaClass.substring(suffixStart, javaClass.length());
						javaClass = javaClass.substring(0,suffixStart);
					} 
					if (".class".equals(suffix) && javaClass.equals(simpleName)) {
						
						FileInputStream reader = new FileInputStream(proxyClassFile);
						int byteCount = reader.available();
						byte [] bytes = new byte[byteCount];
						reader.read(bytes);
						
						return bytes;
					}
				}	
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		//LOG.error("(Not an error in case of interfaces) loadClassBytes: No .class file was found representing the java class "+simpleName+" in path "+this.path+this.folderName);
		return null;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.MultiClassLoader#loadClassFromDependency(java.lang.String, boolean)
	 */
	/**
	 * Specifies the dependencies of the proxy cop implicit by loading from the cop this proxy cop hides and the evolution mgr cop 
	 * The hidden component and the evolution mgr component are the only dependencies of the proxy components
	 */
	protected Class loadClassFromDependency(String className, boolean resolveIt) throws ClassNotFoundException {
		try { 
			// look into the hidden dependencies of the original component (load version dependent ifc)
			// look into the hidden component of the proxy component (e.g. to load interfaces) if NOT -proxy ifc
			ComponentResource cop = (ComponentResource)this.copRes.getOriginalComponent(); 
			//LOG.info(getClassPath()+": looking in original "+cop+" for "+className);
			Class c = cop.loadClass(className, resolveIt);
			//LOG.info(this.toString()+" dependency: loaded "+className+" from "+c.getClassLoader());
			return c;
			
		} catch (Exception c) {
			// hack to find IProxy (any proxy implements this ifc): hardcode dependency to evolution.jar
			ComponentResource cop = (ComponentResource) ComponentRepository.Instance().getComponentResourceByClassname(ComponentEvolutionMain.class.getName());
			Class loaded = cop.getClassLoader().loadClass(className, resolveIt);
			
			return loaded;
		}
	}

	/**
	 * @param className
	 * @return true if the binary declaring a class with the given name <code>className</code> may be loaded using this ClassLoader 
	 */
	public boolean knownsClass(String className) {
		String pathToFile = this.getClassPath() + File.separator + Decode.getAsPath(className) + ".class";
		File classFile = new File(pathToFile);
		return classFile.exists();
		//return loadClassBytes(className) != null;
	}

	public String toString() {
		return this.getClass().getName()+" @ "+this.getClassPath();
	}

}








