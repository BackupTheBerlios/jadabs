package ch.ethz.iks.evolution.mgr;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.adapter.DefaultAdapter;
import ch.ethz.iks.evolution.adapter.cop.AdapterComponentResource;
import ch.ethz.iks.evolution.adapter.cop.AdapterComponentResourceFactory;
import ch.ethz.iks.evolution.cop.UpgradeableComponentResource;
import ch.ethz.iks.evolution.cop.UpgradeableComponentResourceFactory;
import ch.ethz.iks.evolution.step.ISafepoint;
import ch.ethz.iks.evolution.step.IUpgradeStrategy;
import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.ComponentResource;
import ch.ethz.iks.jadabs.IComponent;
import ch.ethz.iks.jadabs.IComponentResource;
import ch.ethz.iks.jadabs.MultiClassLoader;
import ch.ethz.iks.proxy.ExternalReferenceScanner;
import ch.ethz.iks.proxy.TransparentProxyFactory;
import ch.ethz.iks.proxy.cop.ProxyComponentResource;
import ch.ethz.iks.proxy.cop.ProxyComponentResourceFactory;

/**
 * Guides a runtime component evolution of a component to another version of it.
 * 
 * Cares about replacing
 * external references to the hidden objects (the proxy to original mapping) of the old component version by the new ones.
 * 
 * It is also in charge to: Start the evolution step as soon as all conditions specified are met (Safepoints),
 * read in associated Migration component (migr2_...), the specification on when and how to transfer the state from one version to the other, 
 * find all existing objects, start the new and stop the old version and handle incoming service requests during upgrade.
 * However, it may delegate these tasks to a custom <code>IUpgradeStrategy</code> provided by the adapter component associated with this evolution step
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class EvolutionManager implements IEvolutionManager {

	private EvolutionManager() {
		super();
	}
	
	private static Logger LOG = Logger.getLogger(EvolutionManager.class);
	private IUpgradeStrategy customUpdater; // defined inside migr2_ cop
	private Thread upgrade = null;
	private Vector waiters = new Vector();

	/**
	 * Creates or returns an existing EvolutionManager for the given evolution step from
	 * oldVersion to newVersion
	 * 
	 * @param oldVersion may also be null on subsequent invocations (if mgr exists already and newVersion != null)
	 * @param newVersion may also be null on subsequent invocations (if mgr exists already and oldVersion != null)
	 * @return
	 */
	public static IEvolutionManager getManager(
		UpgradeableComponentResource oldVersion,
		UpgradeableComponentResource newVersion) {
		
		EvolutionManager mgr = (EvolutionManager) upcomingUpgrades.get(oldVersion);
		if (mgr != null) {
			return mgr;
		} 
		if (oldVersion == null) {
			Iterator upgrades = upcomingUpgrades.values().iterator();
			while (upgrades.hasNext()) {
				EvolutionManager m = (EvolutionManager)  upgrades.next();
				if (m.newCompoVersion == newVersion) {
					return m;
				}
			}
			return null;
		}
		mgr = new EvolutionManager();
		
		mgr.oldCompoVersion = oldVersion;
		
		if (!mgr.isOnlineUpgradeSupported())
			throw new OnlineUpgradeFailedException(
				" old component version does not support upgrades "
					+ oldVersion.getCodeBase()
					+ ", version "
					+ oldVersion.getVersion());
		
		/*if (mgr.oldCompoVersion instanceof ProxyComponentResource) {
			mgr.oldCompoVersion = ((ProxyComponentResource) mgr.oldCompoVersion).getOriginalComponent();
		}*/
		
		mgr.newCompoVersion = newVersion;
		
		if (newVersion == null || (!newVersion.isService() && !(newVersion instanceof UpgradeableComponentResource)) ) {
			String msg = " new library component does not support upgrades "
			+ oldVersion.getCodeBase()
			+ ", version "
			+ oldVersion.getVersion();
			LOG.error(msg);
			throw new OnlineUpgradeFailedException(msg);
				
		}
		
		return mgr;
	}


	/**
	 * Allows to retreive the associated EvolutionManager given any Object
	 * An Object o is associated to an EvolutionManager if its runtime class
	 * belongs to a component that has already created a <code>EvolutionManager</code>.
	 * 
	 * @param o
	 * @return the EvolutionManager that evolves the component o belongs to or null if none exists.
	 */
	public static IEvolutionManager getManager(Object o) {
		if (o == null)
			return null;
		Class c = o.getClass();
		IComponentResource oldV = UpgradeableComponentResourceFactory.getComponentResourceByContent(c.getName());
		if (oldV == null)
			return null;
		EvolutionManager mgr = (EvolutionManager) upcomingUpgrades.get(oldV);
		return mgr;
	}



	public void transferState(IComponent oldObj, IComponent newObj) {
		
		customUpdater.transferState(oldObj, newObj);
	}

	

	/* (non-Javadoc)
	 * @see ch.ethz.iks.evolvable.IOnlineUpgradeManager#startRuntimeUpgrade()
	 */
	/**
	 * Just enables online upgrade by installing Safepoints that specify under what conditions an upgrade
	 * is allowed to be performed. The upgrade starts after all safepoint have been hit.
	 */
	public void startRuntimeUpgrade() throws OnlineUpgradeFailedException {
		Object o = upcomingUpgrades.put(this.oldCompoVersion, this);
		// install safepoint and wait until migration may be start (just ENABLES upgrade)
		installSafepoints();
		// get the component that specifes when and how to upgrade (migr2_...)
		AdapterComponentResource migrCop =
			(AdapterComponentResource) AdapterComponentResourceFactory.getAdapterComponentFor(oldCompoVersion, newCompoVersion);
		
		customUpdater = migrCop.getUpgradeStrategy();
		
		if (customUpdater.doLaunchNewVersion()) {
			launchNewCopVersion();
		}
		// FIXME: dummy safepoint, start upgrade right away
		this.upgrade = new Thread() {
			public void run() {
				this.setName(
					"online upgrade of "
						+ oldCompoVersion.getCodeBase()
						+ " from version "
						+ oldCompoVersion.getVersion()
						+ " to "
						+ newCompoVersion.getVersion()
				);
				
				try {
					// default onUpgradeStart
					IComponent newMain = null;
					IComponent oldMain = null;
					if (oldCompoVersion.isService()) {
						
						oldMain =
							((UpgradeableComponentResource) oldCompoVersion).getOriginalMainObject(EvolutionManager.this);
						LOG.info("oldMain = "+oldMain);
						if (newCompoVersion instanceof UpgradeableComponentResource) {
							newMain =
								((UpgradeableComponentResource) newCompoVersion).getOriginalMainObject(EvolutionManager.this);
						} else {
							newMain = ((ComponentResource) newCompoVersion).getExtObject();
						}
						LOG.info("newMain = "+newMain);
						customUpdater.initServiceMigr(EvolutionManager.this, oldMain, newMain);
						
						transferState(oldMain, newMain); 
						
						
						onUpgradeFinish();
						
					} else {
						// Library components do no declare a Main-Class in their Manifest, thus, CR.extObject is null
						AdapterComponentResource migrCop =
							(AdapterComponentResource) AdapterComponentResourceFactory.getAdapterComponentFor(
								oldCompoVersion,
								newCompoVersion);
						customUpdater.initLibraryMigr(EvolutionManager.this, migrCop);

					}

				} catch (Exception i) {
					OnlineUpgradeFailedException f = new OnlineUpgradeFailedException(i);
					LOG.error("caught "+i, i);
					throw f;
				}
			}
		};
		this.safepointHit(null); // starts evolution if no safepoints at all have been installed
	}

	/**
	 * Starts the new version of the component
	 * 
	 * @throws OnlineUpgradeFailedException
	 */
	private void launchNewCopVersion() throws OnlineUpgradeFailedException {
		try {
			//LOG.info("old deps = "+oldCompoVersion.getComponentDeps().toString());
			Enumeration oldDeps = oldCompoVersion.getComponentDeps().elements();
			Enumeration newDeps = newCompoVersion.getComponentDeps().elements();
			//hack: assumes dependencies do not change, ordering remains stable from old to new version
			while (oldDeps.hasMoreElements() && newDeps.hasMoreElements()) {
				newCompoVersion.replaceComponentDependency((String)newDeps.nextElement(), (String)oldDeps.nextElement());
			}
			
			//LOG.info("deps of new version cop: "+newCompoVersion.getComponentDeps().toString());
			UpgradeableComponentResourceFactory.clearCache();
			ExternalReferenceScanner.clearCaches();
			TransparentProxyFactory.clearCache();
			
			IComponentResource oldAdapterCop = AdapterComponentResourceFactory.getAdapterComponentFor(null, oldCompoVersion);
			// removing old adapter cop 
			LOG.info("removed old adapter cop "+oldAdapterCop);
			ComponentRepository.Instance().withdraw(oldAdapterCop);
			
			ProxyComponentResource proxyCop = (ProxyComponentResource) ComponentRepository.Instance().getComponentResourceByCodebase(ProxyComponentResourceFactory.getProxyCodebase(oldCompoVersion));
			proxyCop.setOriginalComponent(newCompoVersion); // ensure class loading is delegated to new version 
			LOG.info("From now on, loading and redirecting to new component version "+newCompoVersion.getVersion());
			
			ComponentRepository.Instance().initComponent(newCompoVersion); // do NOT call insert here (checks for cop duplicates)!
		
		} catch (InstantiationException e1) {
			LOG.error(this, e1);
			throw new OnlineUpgradeFailedException(e1);
		} catch (IllegalAccessException e1) {
			LOG.error(this, e1);
			throw new OnlineUpgradeFailedException(e1);
		}
		LOG.info(" Launching new cop version ...");
		newCompoVersion.startComponent(null); // TODO: pass command line args to new version cop
		
		//TODO: need to migrate BEFORE start to guarantee no unnneccecary new proxies are created?
		// Or just pass new hidden to old proxy after migration (as old2new gets known)
		// fill in old2new while traversing
		// TODO: if unstable proxies have to be re-created but hidden is stable, just create new proxy during traversal.
		// But have to assign to same member variable!
		
		
		while (!newCompoVersion.isStarted()) { // services: wait until extObj != null
			try {
				Thread.sleep(3000);
				LOG.info(" ... launching new cop version ...");
			} catch (InterruptedException e) {
				LOG.error(this, e);
				throw new OnlineUpgradeFailedException(e);
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jadabs.evolution.IOnlineUpgradeManager#customizeUpgradeStrategy()
	 */
	public void customizeUpgradeStrategy() {
		throw new RuntimeException("NOT YET IMPLEMENTED: ComponentEvolution.customizeUpgradeStrategy");
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jadabs.evolution.IOnlineUpgradeManager#applyUpgradeStrategy(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	 /**
	  * Specifies behaviour of component during upgrade.
	  * E.g. how to handle incoming method invocations
	  */
	public Object applyUpgradeStrategy(Object thisObject, String invokedMethodName, Object[] parameters) {
		try {
			return this.customUpdater.invoke(thisObject, invokedMethodName, null, parameters);
		} catch (Throwable t) {
			throw new OnlineUpgradeFailedException(t);
		}
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jadabs.evolution.IOnlineUpgradeManager#getCurrentVersion()
	 */
	public IComponentResource getCurrentVersion() {
		
		String cb = ProxyComponentResourceFactory.getProxyCodebase(oldCompoVersion);
		ProxyComponentResource proxyCop = (ProxyComponentResource) ComponentRepository.Instance().getComponentResourceByCodebase(cb);
		return proxyCop.getOriginalComponent();		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jadabs.evolution.IOnlineUpgradeManager#isUpgrading()
	 */
	public boolean isUpgrading() {
		return upcomingUpgrades.containsValue(this);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jadabs.evolution.IOnlineUpgradeManager#isOnlineUpgradeSupported()
	 */
	 /**
	  * @return true if the old component version may be upgraded
	  */
	public boolean isOnlineUpgradeSupported() {
		/*if (this.oldCompoVersion instanceof ProxyComponentResource)
			return true;*/
		
		return  this.oldCompoVersion.isRuntimeEvolutionSupported(null);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jadabs.evolution.IOnlineUpgradeManager#addSafepoint()
	 */
	public void addSafepoint() {
		throw new RuntimeException("NOT YET IMPLEMENTED: +ComponentEvolution.addSafepoint");
	}

	public void customizeUpgradeStrategy(IUpgradeStrategy customizer) {
		//this.customUpdater = customizer;
		//IProxy proxy = (IProxy) DefaultProxyHandler.Instance().setIH(((UpgradeableComponentResource) this.oldCompoVersion).getOriginalMainObject());
		//proxy.setInvocationHandler(customizer);
	}

	/**
	 * Finishes evolution step and cleans up
	 * Withdraws all safepoints
	 * Updates proxy to original mappings to new object versions
	 * Updates original component of proxy component (proxy4_ to new version)
	 * stops old component version and migr component
	 * Only partialliy implemented: Should also install new custom adapters (Adapter4_...)
	 * and assign new hidden object versions to these adapters.  
	 */
	public void onUpgradeFinish() throws OnlineUpgradeFailedException {
		
		customUpdater.onUpgradeFinish();
		
		if (!oldCompoVersion.isService()) {
			LOG.info(" onUpgradeFinish, get lib ifcs");
			Iterator libraryIfc = customUpdater.getRootObjects(oldCompoVersion);
			Object anIfc = null;
			while (libraryIfc.hasNext()) {
				//Map.Entry lib = (Map.Entry) libraryIfc.next();
				// OBSOLETE, is done in DefaultUpgradeStrategy.onUpgradeFinish
				//DefaultAdapter.Instance().updateOriginal(lib.getKey(), lib.getValue()); // also resets handler
				if (anIfc == null)
					anIfc = libraryIfc.next();//lib.getKey();
			}
			// update original cop of proxycop (todo: must be done before updateOriginal)
			ProxyComponentResource pCop = ProxyComponentResourceFactory.getResourceOfProxy(anIfc.getClass().getName());
			pCop.setOriginalComponent((UpgradeableComponentResource) newCompoVersion);
			
			// todo: withdraw migr cop (before updating originals) 

		} else {
			IComponent oldMain;
			IComponent newMain;
			try {
				oldMain = ((UpgradeableComponentResource) this.oldCompoVersion).getOriginalMainObject(this);
				if (newCompoVersion instanceof UpgradeableComponentResource) {
					// replace CR.getExtObject() by ECR.getOriginalMainObject if new is evolvable (ECR.getExtObject() would return a proxy instance)
					newMain = ((UpgradeableComponentResource) newCompoVersion).getOriginalMainObject(this);
				} else {
					newMain = ((ComponentResource) newCompoVersion).getExtObject();
				}
			} catch (IllegalAccessException e) {
				LOG.error(this, e);
				OnlineUpgradeFailedException f = new OnlineUpgradeFailedException(e);
				throw f;
			}

			LOG.info(" onUpgradeFinish, stopping old cop and resetting handler");
			if (!customUpdater.doLaunchNewVersion()) {
				launchNewCopVersion(); // start new version after migr (=now)
			}
			
			AdapterComponentResource migrCop =
									(AdapterComponentResource) AdapterComponentResourceFactory.getAdapterComponentFor(
										oldCompoVersion,
										newCompoVersion);
			LOG.info(" onUpgradeFinish, stopping  migr cop "+migrCop.getCodeBase());
			ComponentRepository.Instance().withdraw(migrCop);
			
			
			if (ComponentEvolutionMain.doHideIfc()) {
				//	update original cop of proxycop
				ProxyComponentResource pCop =
					ProxyComponentResourceFactory.getResourceOfProxy(oldMain.getClass().getName());
				pCop.setOriginalComponent((UpgradeableComponentResource) newCompoVersion);
			}
			DefaultAdapter.Instance().updateOriginal(oldMain, newMain); 
		}
		// stop oldVersion
		ComponentRepository.Instance().withdraw(oldCompoVersion);
		
		
		IComponentResource cop =
			ComponentRepository.Instance().getComponentResourceByCodebase(newCompoVersion.getCodeBase());
		if (cop == null)
			throw new OnlineUpgradeFailedException(
				" new component version not found in repository: " + newCompoVersion.getCodeBase());
		if (!cop.isStarted())
			throw new OnlineUpgradeFailedException(
				"Could not start new component version " + newCompoVersion.getCodeBase());
		LOG.info(cop.getCodeBase() + " upgrade completed: Running version " + cop.getVersion());
		
		System.gc();
		LOG.info("***** upgrade thread ends here");
		upcomingUpgrades.remove(this.oldCompoVersion); // is upgrading == false
		
	}

	/**
	 * Starts online upgrade
	 * 
	 * @throws OnlineUpgradeFailedException
	 */
	protected void doUpgrade() throws OnlineUpgradeFailedException {
		this.upgrade.start();
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jadabs.evolution.IOnlineUpgradeManager#addSafepoint(ch.ethz.iks.jadabs.evolution.ISafepoint)
	 */
	public void addSafepoint(ISafepoint sp) {
		// TODO NOT YET IMPLEMENTED: +ComponentEvolution.addSafepoint
		throw new RuntimeException("NOT YET IMPLEMENTED: +ComponentEvolution.addSafepoint");
	}

	private Vector safepoints;
	protected UpgradeableComponentResource oldCompoVersion = null;
	protected UpgradeableComponentResource newCompoVersion = null;
	protected static HashMap upcomingUpgrades = new HashMap();

	protected HashMap old2new = new HashMap(); // new is a wrapper if old is one

	protected void installSafepoints() {
		this.safepoints = new Vector();
		//TODO: insert all safe point aspects
		//this.safepoints.add();
		
	}

	/**
	 * Invoke this method every time a safepoint has been hit (from within advice code).
	 * After the last one is hit, the upgrade may begin. 
	 * @param sp
	 */
	public void safepointHit(ISafepoint sp) {
		boolean removed = this.safepoints.remove(sp);
		if (this.safepoints.size() == 0) {
			LOG.info("All safepoints have been hit, starting online upgrade...");
			this.doUpgrade();
		}
	}
	
	public void switchClassLoader() {
		MultiClassLoader oldLoader =  ((UpgradeableComponentResource)this.oldCompoVersion).getClassLoader();
		MultiClassLoader newLoader =  ((UpgradeableComponentResource)this.newCompoVersion).getClassLoader();
		try {
			
			ComponentEvolutionMain.evolve(oldLoader, newLoader);
			oldCompoVersion.finalizeLoader();
		} catch (Exception e) {
			LOG.error(this,e);
			throw new OnlineUpgradeFailedException(e);
		}
	}

}
