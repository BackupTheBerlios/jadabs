package ch.ethz.iks.evolution.step;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.adapter.DefaultAdapter;
import ch.ethz.iks.evolution.adapter.cop.AdapterClassLoader;
import ch.ethz.iks.evolution.adapter.cop.AdapterComponentResource;
import ch.ethz.iks.evolution.cop.UpgradeableComponentResource;
import ch.ethz.iks.evolution.cop.UpgradeableComponentResourceFactory;
import ch.ethz.iks.evolution.mgr.ComponentEvolutionMain;
import ch.ethz.iks.evolution.mgr.IEvolutionManager;
import ch.ethz.iks.evolution.mgr.OnlineUpgradeFailedException;
import ch.ethz.iks.evolution.state.DefaultMigration;
import ch.ethz.iks.jadabs.IComponent;
import ch.ethz.iks.jadabs.IComponentResource;
import ch.ethz.iks.proxy.IProxy;
import ch.ethz.iks.proxy.TransparentProxyFactory;
import ch.ethz.iks.proxy.cop.ProxyLoader;

/**
 * An Upgrade strategy implements the behaviour of an online component evolution step.
 * State transfer, launch time of the new component version and
 * invocation handling during the upgrade may be customized.
 * This class implements a default behaviour for state transfer 
 * by a shallow copy of the main object only.
 * 
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public abstract class DefaultUpgradeStrategy implements IUpgradeStrategy {

	private static Logger  LOG =  Logger.getLogger(DefaultUpgradeStrategy.class);
	protected IEvolutionManager mgr;
	private IComponent oldCopMain;
	private IComponent newCopMain;
	private HashMap old2new = new HashMap(200);

	public DefaultUpgradeStrategy() {
		super();
	}

	public DefaultUpgradeStrategy(IEvolutionManager upgradeMgr, IComponent oldESimpl, IComponent newESimpl)
		throws OnlineUpgradeFailedException {
		this();
		initServiceMigr(upgradeMgr, oldESimpl, newESimpl);
	}

	public DefaultUpgradeStrategy(IEvolutionManager upgradeMgr, AdapterComponentResource migrCop)
		throws OnlineUpgradeFailedException {
		this();
		initLibraryMigr(upgradeMgr, migrCop);
	}

	public void initServiceMigr(
		IEvolutionManager upgradeMgr,
		IComponent oldCompoVersionMain,
		IComponent newCompoVersionMain)
		throws OnlineUpgradeFailedException {
		this.mgr = upgradeMgr;
		this.oldCopMain = oldCompoVersionMain;
		this.newCopMain = newCompoVersionMain;
		//old2new.put(oldCopMain, newCopMain);
	}

	public void initLibraryMigr(IEvolutionManager upgradeMgr, AdapterComponentResource migrCop) {
		this.mgr = upgradeMgr;
		/*Iterator oldLibraryRootObjects = migrCop.getOldRootSet();
		Iterator newLibraryRootObjects = migrCop.getNewRootSet();
		// TODO: map old 2 new interfaces, create additional indirection if needed (e.g. one old ifc is splitted or two old ifcs are merged in new version)
		
		old2new.put(oldLibraryRootObjects.next(), newLibraryRootObjects.next()); */
	}

	public Iterator getRootObjects(IComponentResource cop) {
		return DefaultAdapter.getRegisteredProxies();//this.old2new.entrySet().iterator();
		//todo: implement by retreiving all hidden objects from the given cop to enable libray state transfer
	}

	public boolean isUpgrading() {
		return this.mgr.isUpgrading();
	}

	public void setOriginal(Object original) {
		this.newCopMain = (IComponent) original; //hack, not in use
	}

	public void transferState(IComponent oldExtObj, IComponent newExtObj) throws OnlineUpgradeFailedException {
		// this is the customizer called back on class version incompatibilities
		this.oldCopMain = oldExtObj;
		if (oldExtObj instanceof IProxy || newExtObj instanceof IProxy) {
			throw new OnlineUpgradeFailedException("[FAILED]  need original extObjects");
		}
		this.old2new.put(oldExtObj, newExtObj);
		boolean doCopy = false;
		DefaultMigration.shallowCopy(oldExtObj, newExtObj, this, doCopy, false);
	}

	/**
	 * Implements custom state transfer for Objects that are incompatible
	 * This includes Objects declared in the old Component version as variables
	 * of the new version may not hold them because they are intended to hold 
	 * new version Objects only. This incompatibility is caused by the different class loaders
	 * used for the old and new component version.
	 * 
	 * <code>return oldObj;</code> means copying old value of a member to a new version object
	 * 
	 * This implementation uses a recursive deep copy algorithm on incompatible objects.
	 * Be careful about cycles in the object reference graph!
	 */
	public Object transferState(Object oldObj, Object newObj, boolean isCopyingAllowed) {
		if (oldObj == null) { 
			return newObj;
		} 
		// check for cycles in object graph 
		if (!(oldObj instanceof IProxy) && !(newObj instanceof IProxy)) {
			try {
				Object alreadyMigrated = this.old2new.get(oldObj);
				if (alreadyMigrated != null) {
					// newObj := alreadyMigrated: object has already been migrated (e.g. on a different member)
					return alreadyMigrated;
				}
			} catch (Throwable e) {}
		}
		if (oldObj instanceof Class) {
			UpgradeableComponentResource newCop = (UpgradeableComponentResource) UpgradeableComponentResourceFactory.getComponentResourceByContent(this.newCopMain.getClass().getName());
			Class newClass = null;
			try {
				newClass = newCop.loadClass(((Class)oldObj).getName(), false);
				//LOG.info("********** [LOAD]   new version of "+newClass+" v "+newCop.getVersion());
			} catch (ClassNotFoundException e) {
				// LOG.error("[ ERROR ]   failed to load new version class of "+oldObj);
			}
			return newClass;
		}
		
		
		else if (oldObj instanceof IProxy) {
			
			ClassLoader loader = oldObj.getClass().getClassLoader();
			if (loader instanceof AdapterClassLoader) {	
				// unstable proxy
				LOG.info("[ PROXY:unstable ]    obj to migr isa IProxy: unstable proxy "+((IProxy)oldObj).dump()+" : create new proxy? ");
				if ( TransparentProxyFactory.belongToSameCop(oldObj, this.newCopMain) ) {
					// proxy of evolving cop stored in deps: keep proxy (implemented ifc a dependency cop is still valid), re-create hidden (new version)
					LOG.info("[ PROXY:NO ]    obj to migr isa IProxy: unstable proxy "+((IProxy)oldObj).dump()+" of evolving cop stored in dependency");
					
	/* => */		DefaultMigration.shallowCopy(oldObj, newObj, this, false, false);
					return oldObj; // transfer proxy to new member
				} 
				else {
					// unstable proxy of client obj stored in evolving cop (implements an obsolete interface version => replace)
					LOG.info("[ PROXY:YES ]    obj to migr isa IProxy: unstable proxy "+((IProxy)oldObj).dump()+" of client obj stored in evolving cop...");	
					try {
						
						// !!! create a new proxy to hide existing object
						newObj = DefaultAdapter.updateUnstableProxy((IProxy) oldObj, this.mgr.getCurrentVersion());
						if (newObj == null) {
							return oldObj; //already migrated?
						}
						LOG.info("            ...implemented an obsolete interface version =>  replaced by "+((IProxy)newObj).dump());
					} 
					catch (Exception e) {
						throw new OnlineUpgradeFailedException(e);
					}
					///this.old2new.put(oldObj, newObj);
					
	/* => */		DefaultMigration.shallowCopy(oldObj, newObj, this, true, false);
					return newObj; // new proxy
				}
			} 
			else if (loader instanceof ProxyLoader) {
				// stable proxy, TODO: updateOriginal, assign NEW adapter: stable proxy remains in new version
				LOG.info("[ PROXY:keep ]    obj to migr isa IProxy: stable proxy "+((IProxy)oldObj).dump());
				
	/* => */	DefaultMigration.shallowCopy(oldObj, newObj, this, false, false);
				return oldObj; // transfer proxy to new member
			} 
			else {
				// LOG.error("[ERROR]   unknown proxy "+oldObj+" loaded by "+loader);
				return oldObj;
			}
		} else if (oldObj instanceof Proxy) {
			// LOG.error("TODO: implement state transfer of dynamic proxies...");
			return oldObj; // transfer proxy to new member
		} 
		
		
		else if (TransparentProxyFactory.belongToSameCop( oldObj, this.oldCopMain) ) {
			// obj of evolving cop: drilldown & copy
			if (newObj == null || newObj == oldObj) {
				UpgradeableComponentResource cop =null;
				try {
					// create an instance of the corresponding type (mapping function)
					Class oldClass = oldObj.getClass();
					cop= (UpgradeableComponentResource) UpgradeableComponentResourceFactory.getComponentResourceByContent(oldClass.getName());
					//TODO: to verify that new version cop is returned
					Class newClass = cop.getClassLoader().loadClass(oldClass.getName());
					newObj = newClass.newInstance();
				} catch (Exception e) {
					newObj = createNew(oldObj); // abstract
				}
				LOG.info("[ NEW ]   "+newObj+" @ "+cop+" for obsolete EVOLVING "+oldObj);
			} 
			if (!(oldObj instanceof IProxy) && !(newObj instanceof IProxy)) {
				this.old2new.put(oldObj, newObj);
			}
			// drill down (deep copy)
			LOG.info("[DRILLDOWN]   obj of evolving cop: drilling down into " + oldObj+" "+oldObj.getClass());
			
	/* => */	DefaultMigration.shallowCopy(oldObj, newObj, this, true, false);
	
			LOG.info("[GO UP ]   evolving obj "+oldObj);
			return newObj; // existing/created new object
		} 
		
		
		else {
			// external obj (should be a proxy!) or instance of a runtime class: 
			if (isCopyingAllowed && newObj == null) {
			
				try {
					Class oldClass = oldObj.getClass();
					if (null!=UpgradeableComponentResourceFactory.getComponentResourceByContent(oldClass.getName())) {
						// object of a different component: how to instantiate it?
						// LOG.error("[ERROR]   tried to create an object of a different component to be able to copy to it: Returing insted");
							return newObj;
					} 
					newObj = oldClass.newInstance(); 
				} catch (Exception e) {
					newObj = createNew(oldObj); // abstract
				}
				LOG.info("[ NEW ]   "+newObj+" for obsolete STABLE "+oldObj+" "+oldObj.getClass());
			} 
			
			if (!(oldObj instanceof IProxy) && !(newObj instanceof IProxy)) {
				this.old2new.put(oldObj, newObj);
			}
			if (newObj == null){ 
				return newObj; // keep 
			}
			// drill down BUT do NOT copy state of non-upgrading object
			LOG.info("[DRILLDOWN]   stable obj (copy="+isCopyingAllowed+"): drilling down into " + oldObj);
	/* => */	DefaultMigration.shallowCopy(oldObj, newObj, this, isCopyingAllowed, false); 
			LOG.info("[ GO UP ]   stable obj "+oldObj);
			return newObj;// keep new stable object reference, but maybe state has been transferred from oldObj
		}

	}
	
	/**
	 * Creates new instances of ft.jar Classes
	 * 
	 * @param oldObj
	 * @return
	 */
	protected Object createNew(Object oldObj) {
		Class oldClass = oldObj.getClass();
		if (null==UpgradeableComponentResourceFactory.getComponentResourceByContent(oldClass.getName())) {
			// component independent object (runtime library of java or on the classpath)
			return oldObj;
		}
		return null;
	}

	public abstract boolean doLaunchNewVersion() throws OnlineUpgradeFailedException;

	public boolean doMigrate(Field member) {
		return false; // closed world assumption
	}
	


	public void onUpgradeFinish() throws OnlineUpgradeFailedException {
		// loop through proxy registry
		
		Iterator existingProxies = DefaultAdapter.getRegisteredProxies();

		while (existingProxies.hasNext()) {
		  if (ComponentEvolutionMain.doHideIfc() ) {
		  	// transparent proxies
			IProxy existingProxy = (IProxy) existingProxies.next();
			LOG.info(">>>>>");
			LOG.info("[ PROXY:todo ]   migrating proxy "+existingProxy.dump());
			
			if (existingProxy.getClass().getClassLoader() instanceof AdapterClassLoader) {
				
				// unstable proxy (incoming)
				if (TransparentProxyFactory.needsProxy(existingProxy, this.oldCopMain.getClass().getName())) {
					LOG.info("[unstable] proxy: if of evolving cop & stored in a variable of a dependency => update");
					// proxy of an object of own cop in a dependency of evolving cop
					DefaultAdapter.evolveMapping(existingProxy, this.old2new);
				} else {
					try {
						// proxy of a client cop object that implements an obsolete version of an interface declared in upgrading cop
						LOG.info("[unstable] proxy: should be already proxy implementing new version interfaces, e.g. "+existingProxy.getClass().getInterfaces()[1].getClassLoader());
						//has to be done on graph traversal, returns new proxy to be assigned as member: DefaultAdapter.updateUnstableProxy(existingProxy);
						
					} catch (Exception e) {
						throw new OnlineUpgradeFailedException(e);
					}
				}

			} else {
				//STABLE proxy (outgoing)
				LOG.info("[stable]    proxy: if of evolving cop => update");
				DefaultAdapter.evolveMapping(existingProxy, this.old2new);
				// adjust adapter to new hidden obj (how to find it?) old2new?
			}
		  } // transparent proxy
		  else {
		  	// dynamic proxy
			Proxy existingProxy = (Proxy) existingProxies.next();
			LOG.info(" TODO: migrate dynamic proxy "+existingProxy.getClass().getName());
		  }
		}
	}

	public abstract void onUpgradeStart() throws OnlineUpgradeFailedException;

	public abstract void updateOriginal(Object oldHidden, Object newHidden);

	public abstract Object invoke(Object proxy, String methodName, String declaringClass, Object[] args)
		throws Throwable;

	public abstract Object invoke(Object proxy, int methodCode, String declaringClass, Object[] args) throws Throwable;

	public abstract Object invoke(Object proxy, Method method, Object[] args) throws Throwable;

}
