package ch.ethz.iks.proxy.cop;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.mgr.ComponentEvolutionMain;
import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.IComponentResource;
import ch.ethz.iks.jadabs.IResourceFactory;
import ch.ethz.iks.proxy.IProxy;

/**
 * todo: add comment
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class ProxyComponentResourceFactory implements IResourceFactory {
	
	private static Logger LOG = Logger.getLogger(ProxyComponentResourceFactory.class);
	
	private static HashMap cop2proxy = new HashMap();
	
	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IResourceFactory#createResource(java.lang.String, java.lang.String, java.lang.String)
	 */
	public IComponentResource createResource(String urnid, String codebase, String classname) {
		return new ProxyComponentResource(urnid, codebase, classname);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IResourceFactory#createResource(java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public IComponentResource createResource(String urnid, String codebase, String classname, int version) {
		return new ProxyComponentResource(urnid, codebase, classname, version);
	}

	public static ProxyComponentResource getResourceOfProxy(String proxyClassName) {
		Collection values = (Collection) ComponentRepository.Instance().getComponentResources().values();
		//LOG.debug("getComponentResourceByContent: Number of components loaded: "+values.size());
		Iterator cops = values.iterator();
		while (cops.hasNext()) {
			Object copRes = cops.next();
			if (copRes instanceof ProxyComponentResource) {
				if (((ProxyComponentResource)copRes).contains(proxyClassName)) return (ProxyComponentResource)copRes;
			}
		}
		return null;
	}

	/**
	 * Returns the codebase of the proxy component associated with the given component cop.
	 * The codebase denotes a folder on the filesystem, identified by the naming convention
	 * "proxy4_"+cop.getCodeBase() without the ".jar " suffix.
	 * The absolute path is returned.
	 * The folder is created if it does not yet exist.
	 * @param cop the original cop
	 * @return the codebase of the proxy component of cop
	 */
	public static String getProxyCodebase (IComponentResource cop) {
		
		// TODO relies on id creation id = codebase
		if (cop == null ) { 
			LOG.error(" (!) Cannot create a proxy folder for component \'null\'");
			return null;
		}
		
		Object value = cop2proxy.get(cop);
				if (value != null) {
					return (String) value;
		}
		String proxy4copName = IProxy.proxyJarPrefix + cop.getCodeBase(); // e.g. "proxy4_copX.jar"
		int suffixStart = proxy4copName.lastIndexOf('.'); // ".jar" last index, e.g. "junit-2.8.1.jar"
		if (suffixStart > 1) proxy4copName = proxy4copName.substring(0,suffixStart);
			
		File proxy4copFolder = new File(getProxyFolder() + File.separator + proxy4copName); // folder proxy4_copX 
		String folderName = proxy4copFolder.getName();
		if (! proxy4copFolder.exists()) {
			proxy4copFolder.mkdir();
			LOG.info("created folder "+folderName);				
		}
		cop2proxy.put(cop,folderName);
		return folderName;
	}


	public static String getProxyFolder() {
		String pcoprepFolder = (String) ComponentEvolutionMain.PCOPREP;
		if (pcoprepFolder == null)
			pcoprepFolder = "bin" + File.separator + "pcoprep";
		File binFolder = new File(pcoprepFolder).getParentFile(); // e.g. bin/pcoprep
		File proxyFolder = new File(binFolder, IProxy.proxyFolder); // e.g. bin/proxy
		if (!proxyFolder.exists())
			proxyFolder.mkdir();
		String proxyPath = binFolder.getName() + File.separatorChar + proxyFolder.getName();
		return proxyPath;
	}
	
	/*public IComponentContext createContext(IComponentResource copRes) {
		return new ComponentContext(copRes);
	}*/

}
