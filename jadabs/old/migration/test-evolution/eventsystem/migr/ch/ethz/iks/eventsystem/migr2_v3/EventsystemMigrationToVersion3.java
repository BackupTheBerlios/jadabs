
package ch.ethz.iks.eventsystem.migr2_v3;


import ch.ethz.iks.evolution.step.IEvolutionStep;
import ch.ethz.iks.evolution.step.IUpgradeStrategy;
import ch.ethz.iks.jadabs.IComponentContext;
import ch.ethz.iks.jadabs.IComponentResource;

/**
 * Migration customization component for an online upgrade to version 3
 * of the eventsystem component
 * =>  component: adapter2_3_escop/
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */

public class EventsystemMigrationToVersion3 implements IEvolutionStep {


	private EventsystemMigrationToVersion3() {
		super();
		this.customMigr = new EventsystemUpgradeHandler();
	}
	
	/** 
	 * Convention for instantiation of main classes of (service) components.
	 * The (default) constructor should be declared private to allow singleton components.
	 * @see ch.ethz.iks.cop.ComponentResource#initServiceComponent
	 * @return an instance of this class
	 */
	public static EventsystemMigrationToVersion3 createComponentMain() {
		return new EventsystemMigrationToVersion3(); // non-singleton
	}
	
	private EventsystemUpgradeHandler customMigr;

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jadabs.evolution.migr.IMigrationCop#getCustomStrategy()
	 */
	public IUpgradeStrategy getCustomStrategy() {
		return this.customMigr;
	}
	
	public Object [] getLibraryInterface( IComponentResource cop) {
		return this.customMigr.getLibraryInterface(cop);
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
