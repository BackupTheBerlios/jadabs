package ch.ethz.iks.testcop.migr2_v2;

import ch.ethz.iks.evolution.mgr.OnlineUpgradeFailedException;
import ch.ethz.iks.evolution.step.IEvolutionStep;
import ch.ethz.iks.evolution.step.IUpgradeStrategy;
import ch.ethz.iks.jadabs.IComponentContext;
import ch.ethz.iks.jadabs.IComponentResource;

/**
 * Custom implementation of behaviour of the testcop during upgrade to version 2.
 * =>  component: adapter2_2_testcop/
 * 
 * This sample upgrade supports DYNAMIC PROXIES only (no <code>-proxy ifc</code> command line argument)
 * Adapters for the client components (e.g. copWithDependency) must be implemented to support transparent proxy upgrade.
 * Note that dynamic proxis rely on a coding convention for components restricting access to interfaces of dependency components only.
 * An external class must NOT be imported by the client. External objects are be to retreived via the <code>ComponentResource.getExtObject</code>
 * method for service components only.
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class TestcopMigrationToVersion2 implements IEvolutionStep {

	/** 
	 * Convention for instantiation of main classes of (service) components.
	 * The (default) constructor should be declared private to allow singleton components.
	 * @see ch.ethz.iks.cop.ComponentResource#initServiceComponent
	 * @return an instance of this class
	 */
	public static TestcopMigrationToVersion2 createComponentMain() {
		return new TestcopMigrationToVersion2(); // non-singleton
	}
	
	private TestcopUpgradeHandler customizer = new TestcopUpgradeHandler();
	/* (non-Javadoc)
	 * @see ch.ethz.iks.migr.IMigrationCop#getCustomStrategy()
	 */
	public IUpgradeStrategy getCustomStrategy() throws OnlineUpgradeFailedException {
		return customizer;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.migr.IMigrationCop#getLibraryInterface(ch.ethz.iks.cop.IComponentResource)
	 */
	public Object[] getLibraryInterface(IComponentResource cop) {
		return customizer.getLibraryInterface(cop);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#initComponent()
	 */
	public void init(IComponentContext ctx) {
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#startComponent(java.lang.String[])
	 */
	public void startComponent(String[] args) {
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#stopComponent()
	 */
	public void stopComponent() {
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#disposeComponent()
	 */
	public void disposeComponent() {
		
	}

}
