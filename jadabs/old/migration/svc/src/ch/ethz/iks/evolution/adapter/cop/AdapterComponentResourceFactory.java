package ch.ethz.iks.evolution.adapter.cop;

import java.io.File;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.cop.UpgradeableComponentResourceFactory;
import ch.ethz.iks.evolution.mgr.ComponentEvolutionMain;
import ch.ethz.iks.evolution.mgr.OnlineUpgradeFailedException;
import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.IComponentResource;
import ch.ethz.iks.jadabs.IResourceFactory;

/**
 * Creates adapter components to support runtime component evolution.
 * 
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class AdapterComponentResourceFactory implements IResourceFactory {

	private final static String migrPrefix = "adapter2_";
	private final static String migrFolder = "adapters";
	private static Logger LOG = Logger.getLogger(AdapterComponentResourceFactory.class);
	private static Hashtable cop2adapter = new Hashtable();

	public AdapterComponentResourceFactory() {
		super();
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IResourceFactory#createResource(java.lang.String, java.lang.String, java.lang.String)
	 */
	public IComponentResource createResource(String urnid, String codebase, String classname) {
		return new AdapterComponentResource(urnid, codebase, classname, -1);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IResourceFactory#createResource(java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public IComponentResource createResource(String urnid, String codebase, String classname, int version) {
		return new AdapterComponentResource(urnid, codebase, classname, version);
	}


	/**
	 * Creates or gets the migr component for a component version (fromCop) or an online upgrade
	 * version fromCop to version toCop. The adapter component is registered at the component repository.
	 * In case <code>fromCop == null</code>, a customizer component for adapters to the current component is expected that
	 * is located at <code>getAdapterCodebase() + File.separator + migrPrefix + toCop.getCodeBase()</code> (without the .jar suffix), e.g. toCop codebase is <code>escop.jar</code> results in an adapter component at <code>bin/adapters/adapter2_escop</code>.
	 * Otherwise, a customizer component for a runtime evolution from <code>fromCop</code> to <code>toCop</code> is expected at
	 * <code>getAdapterCodebase() + File.separator + migrPrefix + "v" + toCop.getVersion() + toCop.getCodeBase()</code> (without the .jar suffix), , e.g. toCop codebase is <code>escop.jar</code> and versio is 3 results in an adapter component at <code>bin/adapters/adapter2_v3_escop</code>.
	 * 
	 * TODO: Specify naming in the (upgradeable) component's manifest instead of relying to a naming convention. 
	 * 
	 * @param fromCop - old component version or null
	 * @param toCop - new or current component version
	 * @return
	 * @throws OnlineUpgradeFailedException
	 */
	public static AdapterComponentResource getAdapterComponentFor(IComponentResource fromCop, IComponentResource toCop) throws OnlineUpgradeFailedException {
		
		if (toCop == null) LOG.error("component is \'null\'");
		if (toCop == null) throw new NullPointerException("component is \'null\'");
		if (toCop.getCodeBase() == null)  throw new NullPointerException(" codebase is \'null\'");
		
		
		String name = migrPrefix;
		String cbWithoutSuffix = toCop.getCodeBase();
		cbWithoutSuffix = cbWithoutSuffix.substring(0,cbWithoutSuffix.lastIndexOf('.'));
		String codebase = null;
		if (fromCop != null) {
			// upgrade, not just customization
			name += "v" + toCop.getVersion() +"_";
			
			name += cbWithoutSuffix;
			codebase = name;
			name = AdapterComponentResourceFactory.getAdaptersFolder() + File.separator + name;
			cop2adapter.clear(); // important: have to clear cache BEFORE new version cop is launched (gets back old version adapter cop otherwise!) 
		} else {
			name = getAdapterCodebase(toCop);
			Object adapterCop = cop2adapter.get(name);
			if (adapterCop instanceof AdapterComponentResource) {
				//LOG.info("cached adapter cop "+adapterCop+" for "+toCop+" version "+toCop.getVersion());
				return (AdapterComponentResource)adapterCop;
			}
			codebase = migrPrefix+cbWithoutSuffix;
		}
		
		
		Object cop = ComponentRepository.Instance().getComponentResourceByCodebase(name);
		if (cop instanceof AdapterComponentResource) {
			return (AdapterComponentResource)cop;
		}
		//create new cop and insert it
		//String migrFolder = name;
		//System.out.println(" folder of adapter = "+migrFolder);
		
		ComponentRepository.Instance().setResourceFactory(new AdapterComponentResourceFactory());
		String mainClassName = null;
		/*
		try {
			 TODO: let the developer specify the main class name
			  
			String manifestPath = migrFolder + File.separator + "META-INF" + File.separator + "MANIFEST.MF";
			File manifest = new File(manifestPath);
			Manifest m = new Manifest(new FileInputStream(manifest));
			mainClassName = (String) m.getMainAttributes().get(Attributes.Name.MAIN_CLASS);  
		} catch (IOException e) {
			if (fromCop != null) {
				// online upgrade in progress
				OnlineUpgradeFailedException f = new OnlineUpgradeFailedException("No migr cop found at "+migrFolder+" to guide upgrade to version "+toCop.getVersion()+" of "+toCop.getCodeBase());
				LOG.error(toCop,f);
				throw f;
			} 
		}*/
		
		// TODO: hack to retreive main class of migration customizer component FIXME
		if (fromCop != null) {
			char firstChar = Character.toUpperCase(cbWithoutSuffix.charAt(0));
			mainClassName = firstChar + cbWithoutSuffix.substring(1,cbWithoutSuffix.length());
			mainClassName += "MigrationToVersion"+toCop.getVersion();
			mainClassName = "ch.ethz.iks."+cbWithoutSuffix+".migr2_v"+toCop.getVersion()+"."+mainClassName;
		}
		LOG.info("**************** creating adapter cop "+name+" main = "+mainClassName+" toCop = "+toCop+" version "+toCop.getVersion()+" from "+fromCop);
		AdapterComponentResource migrCop = (AdapterComponentResource) ComponentRepository.Instance().createResource(codebase, mainClassName); // main class , may be service component
		ComponentRepository.Instance().setResourceFactory(new UpgradeableComponentResourceFactory());

		migrCop.setExtResLocation(AdapterComponentResourceFactory.getAdaptersFolder() + File.separator);
		migrCop.setMigrationSource(fromCop);
		migrCop.setMigrationTarget(toCop);
		
		ComponentRepository.Instance().insert(migrCop);
		cop2adapter.put(name, migrCop);
		LOG.info("********* adapter cop inserted");
		return migrCop;
	}
	

	/**
	 * 
	 * 
	 * @return the path to the adapters folder where all adapter component binaries are located (extResLocation)
	 */
	public static String getAdaptersFolder() {
		String pcoprepFolder = (String) ComponentEvolutionMain.PCOPREP;
		if (pcoprepFolder == null)
			pcoprepFolder = "bin" + File.separator + "pcoprep";
		File binFolder = new File(pcoprepFolder).getParentFile(); // e.g. bin/pcoprep
		File migrFolder = new File(binFolder, AdapterComponentResourceFactory.migrFolder); // e.g. bin/adapters
		if (!migrFolder.exists())
			migrFolder.mkdir();
		String migrPath = binFolder.getName() + File.separatorChar + migrFolder.getName();
		return migrPath;
	}
	
	/**
	 * Given a component resource, gets (or creates if n/a) a folder to hold
	 * adapter binaries for given component.
	 * 
	 * @param cop
	 * @return
	 */
	public static String getAdapterCodebase (IComponentResource cop) {
		// TODO: relies on id creation id = codebase
			if (cop == null ) { 
				LOG.error(" (!) Cannot create an adapter folder for component \'null\'");
				return null;
			}

			String adapter2copName = migrPrefix + cop.getCodeBase(); // e.g. "adapter2_copX.jar"
			int suffixStart = adapter2copName.lastIndexOf('.'); // ".jar" last index, e.g. "junit-2.8.1.jar"
			if (suffixStart > 1) adapter2copName = adapter2copName.substring(0,suffixStart);
			File adapter2copFolder = new File(getAdaptersFolder() + File.separator + adapter2copName); // folder proxy4_copX 
			if (! adapter2copFolder.exists()) {
				adapter2copFolder.mkdir();
				LOG.info("created folder "+adapter2copFolder.getName());				
			}
			return adapter2copFolder.getName();
		}

	/*public IComponentContext createContext(IComponentResource copRes) {
		return new ComponentContext(copRes);
	}*/
	

}
