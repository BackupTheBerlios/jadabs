package ch.ethz.iks.evolution.cop;

import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import javassist.NotFoundException;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.adapter.DefaultAdapter;
import ch.ethz.iks.evolution.adapter.cop.AdapterComponentResource;
import ch.ethz.iks.evolution.adapter.cop.AdapterComponentResourceFactory;
import ch.ethz.iks.evolution.mgr.ComponentEvolutionMain;
import ch.ethz.iks.evolution.mgr.EvolutionManager;
import ch.ethz.iks.evolution.mgr.IEvolutionManager;
import ch.ethz.iks.evolution.mgr.OnlineUpgradeFailedException;
import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.ComponentResource;
import ch.ethz.iks.jadabs.IComponent;
import ch.ethz.iks.jadabs.IComponentResource;
import ch.ethz.iks.proxy.DependencyReplacer;
import ch.ethz.iks.proxy.TransparentProxyFactory;
import ch.ethz.iks.proxy.WrappingException;
import ch.ethz.iks.proxy.cop.ProxyComponentResource;
import ch.ethz.iks.proxy.cop.ProxyComponentResourceFactory;
import ch.ethz.iks.proxy.cop.ProxyLoader;
import ch.ethz.iks.utils.Decode;


/**
 * Components managed by this class support
 * runtime evolution.
 * The component's binaries are located in the pcoprep folder as JarFiles.
 * Resources of this kind of component encapsulate the original functionality
 * (business logic), e.g. "log4j.jar"
 * 
 * An UpgradeableComponentResource object is associated with its
 * classloader, its proxy component and (optionally) an adapter component.
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */

public class UpgradeableComponentResource extends ComponentResource {

	private ProxyLoader proxyLoader;
	
	public static final String UPGRADEABLE = "Upgradeable"; // Attribute in manifest
	public static final String NON_UPGRADEABLE_COP = "NONE"; // value that marks whole component as not-upgradeable
	
	
	private static Logger LOG = Logger.getLogger(UpgradeableComponentResource.class);

	private boolean upgradeable;
	

	protected UpgradeableComponentResource(String urnid, String codebase, String classname) {
		super(urnid, codebase, classname);
		readUpgradeableAttribute();
	}


	protected UpgradeableComponentResource(String urnid, String codebase, String classname, int version) {
		super(urnid, codebase, classname, version);
		readUpgradeableAttribute();
	}

	
	
	/**
	 * In addition to its UpgradeableComponentLoader, it initializes the 
	 * associated proxy resource.
	 */
	public void initClassLoader() {
		
		String jarfile = getExtResLocation() + getCodeBase();
		Vector dependencies = this.getComponentDeps();
		
		try { 
			
			extClassLoader = new UpgradeableComponentLoader(this, jarfile, new DependencyReplacer(jarfile, this.getVersion())); // REPLACE original instead of extending it
			if (this.isRuntimeEvolutionSupported(getClass())) { 
					initProxy();
					
					// during upgrade only: copy cache from old version BEFORE initServiceComponent BUT AFTER initClassLoader
					EvolutionManager currentUpgrade = (EvolutionManager) EvolutionManager.getManager(null,this);
					if ( currentUpgrade != null && currentUpgrade.isUpgrading()) {
						LOG.info(jarfile+" currently upgrading, copy cache");
						currentUpgrade.switchClassLoader();
					}
			} else {
				LOG.info(jarfile+" is NOT upgradeable");
				//super.initClassLoader();
			}
		} catch (NotFoundException e) {
			LOG.error(this,e);
			// micha try to use javassist loader failed
			super.initClassLoader();
		}
	}	
	
	

	/**
	 * create associated proxy component and the folder it is stored in 
	 *
	 */
	private void initProxy() {
		//		create parent folder proxy in same folder as pcoprep folder, e.g. "bin/proxies"
		String pathOfProxyFolder = ProxyComponentResourceFactory.getProxyFolder();
		String proxy4copPath = pathOfProxyFolder + File.separatorChar; 
		// create proxy folder containing all proxy binaries for this cop
		String proxy4copName = ProxyComponentResourceFactory.getProxyCodebase(this); 
		IComponentResource proxyCop = ComponentRepository.Instance().getComponentResourceByCodebase(proxy4copName);
			
		if (! (proxyCop instanceof ProxyComponentResource) ) {
			// no proxy component exists, create one
			ComponentRepository.Instance().setResourceFactory(new ProxyComponentResourceFactory());
			ProxyComponentResource proxyRes = (ProxyComponentResource) ComponentRepository.Instance().createResource( proxy4copName, null); // no main class , is a library component
			proxyRes.setExtResLocation(pathOfProxyFolder + File.separatorChar);
			proxyRes.setOriginalComponent(this);
		
		    // TODO: move creation of proxy loader inside initClassLoader() method of the ProxyCR class 
			this.proxyLoader = new ProxyLoader(proxyRes, proxy4copName); // the classloader for proxies of this cop
			this.proxyLoader.setExtResLocation(proxy4copPath);
			proxyRes.setClassLoader(proxyLoader);
			
			
			LOG.info("initProxy: inserting proxy component "+proxyRes.getCodeBase()+" for component "+this.getCodeBase());
			ComponentRepository.Instance().insert(proxyRes);
			ComponentRepository.Instance().setResourceFactory(new UpgradeableComponentResourceFactory());
		}
	}


	/**
	 * @return a ClassLoader that loads the proxy classes of this components classes
	 */
	public ProxyLoader getProxyLoader() {
		return this.proxyLoader;
	}
	
	/**
	 * @return a String that denotes the path on the filesystem where the binaries of this component are located
	 */
	public String getClassPath() {
		return getExtResLocation() + getCodeBase();
	}
	
	
	/**
	 * Search for classes declared by this component
	 * 
	 * @param className
	 * @return true if the class with the given <code>className</code> belongs to this component
	 */
	public boolean contains(String className) {
		boolean found = false;
		
		if (this.extClassLoader instanceof UpgradeableComponentLoader) {
			found = ((UpgradeableComponentLoader)this.extClassLoader).knowsClass(className);
			
		} else {
			int length = 0;
			LOG.error(" Expected an EvolutionClassLoader but found a "+this.extClassLoader+" in component "+this.getCodeBase());
			return false; 
		}
		return found;
	}
	
	
	/**
	 * @param toCheck
	 * @return true if this component is specified to support evolution (in case toCheck is null)
	 * or the given class is allowed to be hidden in a proxy as sepcified in the components manifest (evolvable attribute)
	 */
	public boolean isRuntimeEvolutionSupported(Class toCheck) { // TODO remove param
		//LOG.info(this+" isRuntimeEvolutionSupported("+toCheck+") == "+this.upgradeable);
		return this.upgradeable;
	}
	
	private void readUpgradeableAttribute() {
		// init map from manifest (attribute UPGRADEABLE)
		String compoLocation = getExtResLocation() + getCodeBase();
		JarFile jarfile = null;
		Manifest manifest = null;
		try {
			jarfile = new JarFile(compoLocation);
			manifest = jarfile.getManifest();
			Attributes attr = manifest.getMainAttributes();
			if (attr == null || attr.isEmpty()) { 
				// Attribute has not been declared in Manifest
				this.upgradeable = true;
				//LOG.info(this+" isRuntimeEvolutionSupported = "+this.upgradeable);
				return;
			}
			else { //if (attr.containsValue(toCheck.getName())) {
				this.upgradeable =  (attr.getValue(new Attributes.Name(UPGRADEABLE)) == null);
				//LOG.info(this+" isRuntimeEvolutionSupported == "+this.upgradeable);
				return;
			}
		} catch (IOException e) {
			LOG.error(this,e);
		} catch (NullPointerException nil) {
			 // Jarfile not found
				LOG.info("location="+getExtResLocation());
				LOG.info("codebase="+getCodeBase());
				LOG.error(this,nil);
				this.upgradeable =  false; 
				//LOG.info(this+"isRuntimeEvolutionSupported == "+this.upgradeable);
				return;
		}
		this.upgradeable =  false;
		//LOG.info(this+" isRuntimeEvolutionSupported == "+this.upgradeable);
	}


	 


	/**
	 * Returns true is this component is a dependency of usingCop
	 * @param usingCop
	 * @return true if this component is (may be) in use by usingCop
	 */           
	public boolean hasUsages(ComponentResource usingCop) {
		return usingCop.getComponentDeps().contains(this.getCodeBase());
	}


	/**
	 * Replaces the String oldDep with the String newDep in the component
	 * dependencies of this component (in memory only). This may be used to
	 * declare a proxy component as a components dependency instead of the hidden one.
	 * @param oldDep
	 * @param newDep
	 */
	public void replaceComponentDependency(String oldDep, String newDep) {
		Vector dependencies = this.getComponentDeps(); // read in
		if (dependencies.remove(oldDep)) {
			// modify in memory only
			dependencies.add(0, newDep);
			//String list = "dependencies of "+this.getCodeBase()+": ";
			
			LOG.info(this.toString()+": Replaced "+oldDep+" in deps = "+ this.getComponentDeps().toString());
		} 
		
	}
	
	
	/**
	 * Overridden to hide the component main object from access of other components.
	 * A proxy object is returned instead.
	 */
	public IComponent getExtObject() { 
		try {
			//LOG.info("getExtObject(): hides main object of cop by returning a proxy of it");
			
			//need to hide ext obj by a proxy (IComponents are required to declare a default constructor): 
			if (ComponentEvolutionMain.doHideIfc()) {
				Object hidden = getOriginalMainObject(this);
				Object transparentProxy = DefaultAdapter.Instance().wrapOutgoingObject(hidden, hidden.getClass().getName()); // IComponents must provide a default constructor
			
				return (IComponent) transparentProxy; // proxy instance must also implement the IComponent inteface (as ExtObj)
			
			} else {
				Object mainObj = getOriginalMainObject(this);
				Object dynProxy = DefaultAdapter.Instance().createDynamicProxy(mainObj);
				LOG.info("getExtObject wrapped "+mainObj+" behind "+dynProxy);
				return (IComponent) dynProxy;
			}
		} catch (Exception e) {
			LOG.error(this,e);
			WrappingException f = new WrappingException(e);
			throw f;
		}
		
	}
	
	/**
	 * @return Class - the Class object of the proxy class of the main object
	 * This class is part of the proxy component of this component 
	 */
	public Class getExtClass() {
		// need to hide ext obj by a proxy
		return TransparentProxyFactory.getProxyClass(this.proxyLoader, super.getExtClass());
	}
	
	/**
	 * Provides access to the hidden main object. Access is granted to selected obejcts only, 
	 * e.g. the EvolutionManager of this component.
	 * @param accessor - the context in which this call is made
	 * @return the main object of this component
	 * @throws IllegalAccessException - if accossor is not granted
	 */
	public IComponent getOriginalMainObject(Object accessor) throws IllegalAccessException {
		// replaces getExtObject(): for internal usage only
		
		if (this.isService()) {
			IComponent extObj = null;
			if (accessor instanceof IEvolutionManager) {
				
				extObj = super.getExtObject();
				LOG.info(" original extObject "+extObj+" accessed by "+accessor);
			}
			else if (accessor == this) {
				extObj = super.getExtObject();
				LOG.info(" original extObject "+extObj+" accessed by "+this);
			}
			else if (accessor instanceof ComponentEvolutionMain) extObj = super.getExtObject(); //remove, just debugging
			else if (accessor instanceof DefaultAdapter) extObj = super.getExtObject(); // TODO: remove, just debugging
			
			else {
				LOG.error("getOriginalMainObject causes an IllegalAccessException: access to unwrapped object is denied to external objects of class "+accessor.getClass().getName());
				throw new IllegalAccessException("access to unwrapped object is denied to external objects of class "+accessor.getClass().getName());	
			}
			return extObj;
		} else {
			//	Library components do no declare a Main-Class in their Manifest
			LOG.error("No main object for library cop "+this);
			return null;
		}
	}
	
	/**
	 * TODO: read Classname of Adapter from Manifest
	 * @param className - the fully qualified class name of the class that request
	 * a custom adapter
	 * @return a custom adapter class for className, named $package+Adapter4_+$simpleName
	 * @throws ClassNotFoundException
	 * @throws OnlineUpgradeFailedException
	 */
	public Class getCustomAdapter(String className) throws ClassNotFoundException, OnlineUpgradeFailedException {
		AdapterComponentResource cop = AdapterComponentResourceFactory.getAdapterComponentFor(null, this); 
		if (cop == null) {
			LOG.error("No adapter component available for "+this.getCodeBase());
			return null; // no adapter cop found
		}
		String adapterName = Decode.getPackage(className)+".Adapter4_"+Decode.getSimpleName(className);
		
		// TODO: specify name of adapters in Manifest instead 
		return cop.getClassLoader().loadClass(adapterName, true);
	}


	/**
	 * finalizes the loader by closing the jar file
	 */
	public void finalizeLoader() {
		LOG.info(getCodeBase()+" version "+getVersion()+": loader = null");
		((UpgradeableComponentLoader)this.extClassLoader).close();
		this.extClassLoader = null;
	}
	
	public String toString() {
		return getCodeBase()+" @ "+getExtResLocation()+" v"+getVersion();
	}
	

}
