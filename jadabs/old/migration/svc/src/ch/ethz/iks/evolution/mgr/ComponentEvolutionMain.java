package ch.ethz.iks.evolution.mgr;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.adapter.cop.AdapterComponentResource;
import ch.ethz.iks.evolution.cop.UpgradeableComponentLoader;
import ch.ethz.iks.evolution.cop.UpgradeableComponentResource;
import ch.ethz.iks.evolution.cop.UpgradeableComponentResourceFactory;
import ch.ethz.iks.evolution.state.DefaultMigration;
import ch.ethz.iks.jadabs.BootstrapConstants;
import ch.ethz.iks.jadabs.ComponentManager;
import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.ComponentResource;
import ch.ethz.iks.jadabs.ComponentResourceFactory;
import ch.ethz.iks.jadabs.IComponent;
import ch.ethz.iks.jadabs.IComponentContext;
import ch.ethz.iks.jadabs.IComponentResource;
import ch.ethz.iks.jadabs.LocalComponentLoader;
import ch.ethz.iks.jadabs.MultiClassLoader;
import ch.ethz.iks.proxy.TransparentProxyFactory;
import ch.ethz.iks.proxy.WrappingException;
import ch.ethz.iks.proxy.cop.ProxyComponentResource;



/**
 * The main class of the evolution component (<code>evolution.jar</code>). On startup, this class takes care about
 * adding evolution support to ordinary components. It suspends the <code>LocalComponentLoader</code> and 
 * evolves it to a <code>LocalCopScanner</code> that is able to initiate online upgrades. 
 * Furthermore, it evolves all loaded <code>ComponentResource</code>s and <CODE>ComponentInitializer<CODE> objects 
 * to their pendants that provide support for component evolution: <code>UpgradeableComponentResource</code> and <code>UpgradeableComponentLoader</code>.
 * After <code>startComponent(String [] args)</code> has been finished, all components that are not in use by another one
 * are able to evolve at runtime to a newer version. As long as the evolution component is active, newly loaded components automatically
 * are initialized with a <code>UpgradeableComponentResource</code> and <code>UpgradeableComponentLoader</code>.
 * 
 * Command line arguments supported: <code>[-proxy ifc] [-adapt reflect | name | hash]</code>
 * If the first argument is set, interface evolution is turned on (transparent proxy), else a dynamic proxy is used.
 * The value of the second argument determines the way a proxy forwards invocations to its <code>IAdapter</code>. It takes
 * effect for transparnt proxies only. 
 * @see  ch.ethz.iks.evolution.adapter.IAdapter
 *  
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class ComponentEvolutionMain implements IComponent { 

	private static Logger LOG = Logger.getLogger(ComponentEvolutionMain.class);
	
	public static final String CMD_LINE_ARGS = "ARGS";
	
	public static final String CMDARGS_ADAPT = "-adapt";
	public static final String VALUE_ADAPT_HASH = "hash";
	public static final String VALUE_ADAPT_NAME = "name";
	public static final String VALUE_ADAPT_REFLECT = "reflect";
	
	public static final String CMDARGS_PROXY = "-proxy";
	public static final String VALUE_PROXY = "ifc";
	
	//members holding the argument values
	private static boolean createProxiesForInterfaces = false; // default value of -proxy
	private static String adapterMethodSignatureKind = VALUE_ADAPT_REFLECT; // default value of -adapt
	
	public static String PCOPREP = "pcoprep";
	
	private ComponentEvolutionMain() {
		super();
	}
	
	public static ComponentEvolutionMain createComponentMain() {
		return new ComponentEvolutionMain(); // non-singleton
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#initComponent()
	 */
	public void init(IComponentContext ctx) {
		PCOPREP = (String) ctx.getProperty(BootstrapConstants.PCOPREP);
		try {
			
			evolveLocalLoader();
			
		} catch (IllegalArgumentException e) {
			LOG.error("adding evolution support failed",e);
			this.stopComponent();
		} catch (NoSuchFieldException e) {
			LOG.error("adding evolution support failed",e);
			this.stopComponent();
		} catch (IllegalAccessException e) {
			LOG.error("adding evolution support failed",e);
			this.stopComponent();
		}

		ComponentRepository.Instance().setResourceFactory(new UpgradeableComponentResourceFactory());
	}


	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#startComponent(java.lang.String[])
	 */
	public void startComponent(String[] args) {
			  try {	
				if (args == null || args.length == 0) {
					args = readArgs(); // read from manifest
				}
				processArgs(args);		
				evolveComponentResources();
				
				//ComponentManager.Instance().startLocalComponentLoader();
				
				LOG.info(" Evolution of Component Resources has finished. Ready for online upgrades...");
			  } catch (Exception e) {
			  	LOG.error("adding evolution support failed",e);
				stopComponent();
			  }
		
	}

	/**
	 * reding args from manifest
	 * 
	 * @return
	 */
	private String[] readArgs() {
		LOG.info("No commandline arguments found, trying to read them from manifest (ARGS attribute)");
		String myCodebase = ComponentRepository.Instance().getComponentResourceByClassname(ComponentEvolutionMain.class.getName()).getCodeBase();
		String pcoprep = PCOPREP;
		JarFile myJar;
		
		try {
			myJar = new JarFile(pcoprep + File.separator + myCodebase); 
		
			Attributes attrs =  myJar.getManifest().getMainAttributes();
			Object attr = attrs.get(new Attributes.Name(CMD_LINE_ARGS));
			if (attr != null) {		
				String printArgs = "";
				StringTokenizer argSplitter = new StringTokenizer((String)attr, " ");
				String[] args = new String[argSplitter.countTokens()];
				int i = 0;
				while(argSplitter.hasMoreTokens()) {
					args[i++] = argSplitter.nextToken();
					printArgs += " "+args[i-1];
				}
				
				return args;
			}
		} catch (IOException e) {
			LOG.error("failed to read in cmd line args, defaulting to -proxy ifc -adapt hash",e);
		}
		return null;
	}

	/**
	 * NOT YET IMPLEMENTED
	 * @param args
	 */
	private void processArgs(String args[]) {
		if (args == null) return;
		
		for (int i = 0; i < args.length; i++) {
			String arg = args[i].toLowerCase();
			//LOG.info(i+"th arg = "+args[i]);
			if (arg.equals(CMDARGS_PROXY) ) {
				createProxiesForInterfaces = (args.length > i) && args[i+1].equals(VALUE_PROXY);
				
				TransparentProxyFactory.setIfcHiding(createProxiesForInterfaces);
				
			} else if (arg.equals(CMDARGS_ADAPT) && (args.length > i) ) {
				if (args[i+1].equals(VALUE_ADAPT_NAME) || args[i+1].equals(VALUE_ADAPT_HASH) || args[i+1].equals(VALUE_ADAPT_REFLECT)) {
					adapterMethodSignatureKind = args[i+1];
					// todo: choose between three invoke() methods that may be generated in proxies
					// VALUE_ADAPT_HASH: (custom adapters required) invoke(Object callee, int methodHash, String eclaringClassName, Object[] args)
					// VALUE_ADAPT_NAME:                            invoke(Object callee, String methodName, String eclaringClassName, Object[] args)
					// VALUE_ADAPT_REFLECT: (very slow)             invoke(Object callee, java.lang.reflect.Method invokedMethod, Object[] args)
					//ProxyFactory.setAdapterMethodKind(adapterMethodSignatureKind);
					
				} 
					
			} 
		}
		LOG.info(" processed cmdline args: doHideIfc = "+doHideIfc()+" and getAdapterKind = "+getAdapterKind());
	}


	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#stopComponent()
	 */
	public void stopComponent() {
		ComponentRepository.Instance().setResourceFactory(new ComponentResourceFactory());
	}


	// just for testing
	public static void main(String[] args) {
		ComponentEvolutionMain darwin = new ComponentEvolutionMain();
		darwin.init(null);
		darwin.startComponent(args);
	}

	/**
	 * replaces existing ComponentResources in the repository by UpgradeableComponentResources where possible
	 *
	 */
	private void evolveComponentResources() {
		// TODO: insert safepoint to wait for Repository idle
		// Aspect bootstrapSafepointAspect = createSafePoint(this /* this maps to copversion and evolutionSpec.xml in jar of cop version */);
		// Proseystem.getAspectManager().insert(bootstrapSafepointAspect);
		// following stmts should be executed/initiated from inside advice of safepoint aspect
		
		Hashtable cops = (Hashtable) ComponentRepository.Instance().getComponentResources();
		Hashtable clonedCops = (Hashtable) cops.clone(); // copies all references to keys and values to avoid concurrency problems
		Iterator allCops = clonedCops.values().iterator();
		while (allCops.hasNext()) {
			ComponentResource cop = (ComponentResource) allCops.next();
			try {
				if (cop instanceof ProxyComponentResource) {
					continue;
				} else if (cop instanceof AdapterComponentResource) {
					continue;
				}
				
				UpgradeableComponentResource eCop = evolve(cop);
				
				if (eCop == null || !eCop.isRuntimeEvolutionSupported(null)) {
					// undo
					ComponentRepository.Instance().getComponentResources().remove(cop.getCodeBase());
					ComponentRepository.Instance().getComponentResources().put(cop.getCodeBase(), cop);
					LOG.info("cop is NOT upgradable "+cop.getCodeBase());
				}
			} catch (Exception e) {
				ComponentRepository.Instance().getComponentResources().remove(cop.getCodeBase());
				ComponentRepository.Instance().getComponentResources().put(cop.getCodeBase(), cop);
				LOG.error("Adding evolution support for "+cop.getCodeBase()+" FAILED: ",e);
			}
		}
	}
	
	
	/**
	 * Tries to evolve an existing ComponentResource object to a UpgradeableComponentResource object
	 * This evolution step succeeds only if the component is not a dependency of other components that have already been started.
	 * 
	 */
	public static UpgradeableComponentResource evolve(ComponentResource cop)
		throws IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException, WrappingException {
		
		if (cop instanceof ProxyComponentResource) {
			return null; // do not evolve proxies
		}
		// create evolvable components from now on
		ComponentRepository.Instance().setResourceFactory(new UpgradeableComponentResourceFactory());
		// create evolvable resource
		UpgradeableComponentResource eCop =
			(UpgradeableComponentResource) ComponentRepository.Instance().createResource(
				cop.getCodeBase(),
				cop.getClassName(),
				cop.getVersion());
		
		Iterator cops = ComponentRepository.Instance().getComponentResources().values().iterator();
		IComponentResource usingCop = null;
		// check if evolution may be added to the component that is already loaded
		while (cops.hasNext()) {
			ComponentResource copRes = (ComponentResource) cops.next();
			if (!copRes.isInitialized() || (copRes.isService() && !copRes.isStarted())) {} 
			else if (eCop.hasUsages(copRes)) {
				usingCop = copRes;
				break;
			}
		}
		
		//LOG.info("evolving: " + cop.getCodeBase());
		if (usingCop != null /*&& !safepoint.allowsUsage()*/) {
			throw new WrappingException(
				" Cannot evolve component "+ cop.getCodeBase() +" while it is in use by " + usingCop.getCodeBase());
		}
		
		// copy state from old resource
		DefaultMigration.shallowCopy(cop,eCop); 
		// init additional state of UpgradeableComponentResource 
		eCop.initClassLoader();
		
		evolve( cop.getClassLoader(), eCop.getClassLoader() ); // evolve the classloader of the component
		
		// replace references to cop by eCop on all objects (assumes just repository has one)
		Hashtable copRegistry = ComponentRepository.Instance().getComponentResources();
		if (copRegistry.containsKey(eCop.getCodeBase())) {
			copRegistry.remove(eCop.getCodeBase());
			copRegistry.put(eCop.getCodeBase(), eCop); // replaces cop with eCop
			
		} else
			throw new WrappingException(
				"Unknown component "
					+ cop.getCopID()
					+ " is not registered at repository with its key "
					+ cop.getCodeBase());
		return eCop;
	}


	/** 
	 * copies cache of classes already loaded from the first ClassLoader to second one
	 * @param oldLoader - evolution (copy) source loader
	 * @param newLoader - evolution (copy) target loader
	 */
	static void evolve(MultiClassLoader oldLoader, MultiClassLoader newLoader) throws IllegalArgumentException, IllegalAccessException {
		boolean isUpgrade = (oldLoader instanceof UpgradeableComponentLoader); // false on initial evolutionn from ComponentInitializer only (insertion of evolution cop)
		
		Field [] members = MultiClassLoader.class.getDeclaredFields();
		for (int mIndex = 0; mIndex < members.length; mIndex++) {
			Field field = members[mIndex];
			if (field.getType() == Hashtable.class) {
				
				// cache of classes already loaded until now
				field.setAccessible(true);
				Hashtable cache = (Hashtable) field.get(oldLoader);
				try {
					field.set(newLoader, cache);
					//LOG.info("old loader cache contains: "+new Vector(cache.keySet()).toString());
					if (isUpgrade) {
						LOG.info("upgrading: copy only unknown classes to new cache");
						// unload old version of class definitions of own component
						// if new version component overwrites it 
						Vector obsoleteClasses = new Vector();
						Iterator entries = cache.entrySet().iterator();
						while (entries.hasNext()) {
							Map.Entry entry = (Map.Entry) entries.next();
							String key = (String) entry.getKey();
							Class value = (Class) entry.getValue();
							
							if (((UpgradeableComponentLoader)newLoader).knowsClass(key) ) {
								if ( doHideIfc() || !value.isInterface() ) {
									// keep ifcs in cache in case of dynamic proxies (interfaces must remain stable!)
									obsoleteClasses.add(key);
								}
							}
						}
						LOG.info(" removing obsolete class definitions from cache "+obsoleteClasses.toString());
						Enumeration toRemove = obsoleteClasses.elements();
						while (toRemove.hasMoreElements()) {
							cache.remove(toRemove.nextElement());
						}
					}
				} catch ( NullPointerException n) { LOG.error("cache ="+cache,n);}
				field.setAccessible(false);
			}
		}
	}

	/**
	 * Replaces the existing Componentloader by one that initializes an online upgrade if
	 * a new version of a runnning component is inserted. 
	 * Assumes that just ComponentManager keeps a reference to the loader.
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private void evolveLocalLoader() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		ComponentManager.Instance().getComponentLoader().stopLoader();
		LocalCopScanner loCopNew = null; 
		Field [] members = ComponentManager.class.getDeclaredFields();
		for (int mIndex = 0; mIndex < members.length; mIndex++) {
			Field f = members[mIndex];
			if (f.getType() == LocalComponentLoader.class) {
					f.setAccessible(true);
					LocalComponentLoader loCopOld = (LocalComponentLoader)f.get(ComponentManager.Instance());
					LOG.info("Creating new coploader, old loader is "+loCopOld);
					//TODO: retreive timeinterval from old pcoprep via reflection
					try {
						loCopOld.stopLoader();
					} catch (NullPointerException n) {
						LOG.error("FAILED to stop LocalComponentLoader ",n);
					}
					loCopNew = new LocalCopScanner();
					loCopNew.startLoader(2000);
					DefaultMigration.shallowCopy(loCopOld, loCopNew);
					
					f.set(ComponentManager.Instance(), loCopNew);
					
					f.setAccessible(false);
					break;
			}
		}
		if (loCopNew == null) throw new NoSuchFieldException(" No Field of type "+LocalComponentLoader.class.getName()+" has been found in the singleton "+ComponentManager.class.getName());
	}

	
	
	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#disposeComponent()
	 */
	public void disposeComponent() {
	}

	/**
	 * @return true if command line argument value of  <code>-proxy<code> is <code>ifc</code>
	 */
	public static boolean doHideIfc() {
		return createProxiesForInterfaces;
	}
	
	public static String getAdapterKind() {
		return adapterMethodSignatureKind;
	}

}

