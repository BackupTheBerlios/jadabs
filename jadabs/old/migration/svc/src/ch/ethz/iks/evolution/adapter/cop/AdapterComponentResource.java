package ch.ethz.iks.evolution.adapter.cop;

import java.io.File;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.cop.UpgradeableComponentResource;
import ch.ethz.iks.evolution.mgr.ComponentEvolutionMain;
import ch.ethz.iks.evolution.mgr.OnlineUpgradeFailedException;
import ch.ethz.iks.evolution.step.IEvolutionStep;
import ch.ethz.iks.evolution.step.IUpgradeStrategy;
import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.ComponentResource;
import ch.ethz.iks.jadabs.IComponentResource;

/**
 * This kind of component is associated either with exactly one component version or 
 * one runtime evolution step (a pair of versions of the same component). 
 * It contains helper classes to either adapt the proxy class 
 * to the current implementation version of its associated component or
 * to handle incoming method invcations during an upgrade. 
 * Further, it may contain classes that perform state migration, define safepoints
 * or do additional stuff during an upgrade that is not forseen in the default implementation
 * of <code>DefaultUpgradeStrategy</code>.
 * 
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class AdapterComponentResource extends ComponentResource {

	public AdapterComponentResource(String urnid, String codebase, String classname) {
		super(urnid, codebase, classname);
	}

	public AdapterComponentResource(String urnid, String codebase, String classname, int version) {
		super(urnid, codebase, classname, version);
	}

	private static Logger LOG = Logger.getLogger(AdapterComponentResource.class);
	private Thread t;

	protected void initClassLoader() {
		this.extClassLoader = new AdapterClassLoader(this, AdapterComponentResourceFactory.getAdaptersFolder() + File.separator + getCodeBase());
	}
	
	public Vector getComponentDeps() { 
		return new Vector();
	}
	
	public void setExtResLocation(String loc) {
		super.setExtResLocation(loc);
		if (this.extClassLoader instanceof AdapterClassLoader) {
			((AdapterClassLoader)this.extClassLoader).setClassPath(loc + getCodeBase());
		}
	}

	/**
	 * @return - a custom IUpgradeStrategy to handle method invocations during upgrade
	 * from oldVersion to newVersion
	 * @throws OnlineUpgradeFailedException
	 */
	public IUpgradeStrategy getUpgradeStrategy() throws OnlineUpgradeFailedException {
		LOG.info(" getting upgrade strategy for eventsystem");
		if (!this.isService()) {
			OnlineUpgradeFailedException f = new OnlineUpgradeFailedException("migr cop is not a service");
			LOG.error(this,f);
			throw f;
		}
		IEvolutionStep migrMain =  (IEvolutionStep) this.getExtObject();
		if (migrMain == null) throw new RuntimeException("migrMain is null");
		return migrMain.getCustomStrategy();
	}

	public static final String evolutionDependency = ComponentRepository.Instance().getComponentResourceByClassname(ComponentEvolutionMain.class.getName()).getCodeBase();
	private IComponentResource oldCop;
	private IComponentResource newCop;


	public void setMigrationTarget(IComponentResource newCompoVersion) {
		this.newCop = newCompoVersion;
	}

	public void setMigrationSource(IComponentResource oldCompoVersion) {
		this.oldCop = oldCompoVersion;
	}
	/*
	public Iterator getOldRootSet() throws OnlineUpgradeFailedException {
		return this.getRootSet((UpgradeableComponentResource) this.oldCop);
	}

	public Iterator getNewRootSet() throws OnlineUpgradeFailedException {
		return this.getRootSet((UpgradeableComponentResource) this.newCop);
	}*/
	

	/*
	 * gets all objects that define the interface of a library component (all hidden objects)
	 * @param cop
	 * @return
	 * @throws OnlineUpgradeFailedException
	 
	private Iterator getRootSet(UpgradeableComponentResource cop) throws OnlineUpgradeFailedException {
		if (cop.isService()) {
			OnlineUpgradeFailedException f =
				new OnlineUpgradeFailedException(" defined for Library component resources only");
			LOG.error(this, f);
			throw f;
		}
		// temporary add dependency to given cop version to gain access to hidden objects
		this.addCopResDependency(cop);
		
		IEvolutionStep migrMain = (IEvolutionStep) this.getExtObject();
		Iterator libraryInterfaces = migrMain.getCustomStrategy().getRootObjects(cop);
		
		this.removeCopResDependency(cop);
		return libraryInterfaces;
	}


	protected void removeCopResDependency(IComponentResource extRes) {
		this.getComponentDeps().remove(extRes.getCopID());

	}*/
	
	public UpgradeableComponentResource getOriginalComponent() {
		if (oldCop != null) {
			if (ComponentRepository.Instance().getComponentResourceByClassname(oldCop.getClassName()) == oldCop) {
				LOG.info(this+" getOrig returning oldCop "+oldCop);
				return (UpgradeableComponentResource)oldCop;
			} else {
				// oldCop has been unloaded from repository
				oldCop = null;
			}
		}
		return (UpgradeableComponentResource)this.newCop;
	}
	
	public void stopComponent() {
		super.stopComponent();
		this.extClassLoader = null;
		LOG.info("adapter loader = null "+this);
	}
	
}
