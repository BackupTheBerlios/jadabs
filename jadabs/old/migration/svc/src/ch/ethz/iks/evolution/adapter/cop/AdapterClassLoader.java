package ch.ethz.iks.evolution.adapter.cop;

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
 * Class loader for adapter components 
 * Loades class files from a folder, usually bin/adapters/adapter2_$codebase
 * Dependencies of this component is the evolution manager component and
 * the hidden (upgradeable) component, e.g.
 * <code>Class-Path: evolution.jar hidden.jar</code>
 * 
 * Note that the adapter component has direct access to the hidden component
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class AdapterClassLoader extends MultiClassLoader implements IClassLoaderWithClassPath {

	/**
	 * @param extRes
	 * @param jarName - incl. extResLocation (e.g. bin/migr/migr2_copXY.jar)
	 */
	public AdapterClassLoader(AdapterComponentResource extRes, String jarName) {
		super();
		this.copRes = extRes;
		this.path = jarName;
		
	}
	
	private static Logger LOG = Logger.getLogger(AdapterClassLoader.class);
	
	private String path;
	private AdapterComponentResource copRes;
	
	public String getClassPath() {
		return path;
	}
	
	public void setClassPath(String p) {
		path = p;
	}
	
	public String toString() {
		return this.path;
	}
	
	
	
	protected Class loadClassFromDependency(String className, boolean resolveIt)  throws ClassNotFoundException {
		try {
			ComponentResource origCop = this.copRes.getOriginalComponent();
			LOG.info("loading from deps: origCop= "+origCop);
			Class loaded = origCop.loadClass(className, resolveIt);
			//LOG.info(this.toString()+" dependency: loaded "+className+" from "+loaded.getClassLoader());
			return loaded;
		} catch (ClassNotFoundException c) {
			ComponentResource cop = (ComponentResource) ComponentRepository.Instance().getComponentResourceByClassname(ComponentEvolutionMain.class.getName());
			Class loaded = cop.getClassLoader().loadClass(className, resolveIt);
			return loaded;
		}
	}
	
	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.MultiClassLoader#loadClassBytes(java.lang.String)
	 */
	 // TODO: make a common superclass SingleFolderLoader of this Class and ProxyLoader to implement this method
	protected byte [] loadClassBytes(String className) {
		String simpleName = null;
		String pathToFile = null;
		try {
			// get package of class to load 
			String packagePrefix = Decode.getPackage(className);
			simpleName = Decode.getSimpleName(className);
			
			// get adapter folder of component and search for a matching .class file
			pathToFile = this.path + File.separator + packagePrefix.replace('.',File.separatorChar);
			
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
						//LOG.info(this.toString()+" loadClassBytes ("+className+") from "+pathToFile);
						return bytes;
					}
				}	
			}
			catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		//LOG.error("(Not an error in case of interfaces) loadClassBytes: No .class file was found representing the java class "+simpleName+" in path "+this.path);
		return null;
	}


}
