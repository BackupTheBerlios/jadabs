package ch.ethz.iks.evolution.step;

import ch.ethz.iks.evolution.mgr.OnlineUpgradeFailedException;
import ch.ethz.iks.jadabs.IComponent;


/**
 * A main class of an adapter component associated with a runtime evolution step
 * has to implement this interface to allow accessing the upgrade behaviour.
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public interface IEvolutionStep extends IComponent {

	
	public IUpgradeStrategy getCustomStrategy() throws OnlineUpgradeFailedException;
	
	


}
