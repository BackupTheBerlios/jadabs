package ch.ethz.iks.evolution.mgr;

import ch.ethz.iks.evolution.step.ISafepoint;
import ch.ethz.iks.evolution.step.IUpgradeStrategy;
import ch.ethz.iks.jadabs.IComponentResource;



/**
 * A guide to component evolution from one version to another
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public interface  IEvolutionManager { 

	
	public void customizeUpgradeStrategy (IUpgradeStrategy customizer);
	
	
	public Object applyUpgradeStrategy ( Object thisObject, String invokedMethodName, Object [] parameters ); // invokes IUpgradeStrategy.strategy(old,new,method,params)
	
	
	public IComponentResource getCurrentVersion (); 
	
	public boolean isUpgrading (); 
	
	public boolean isOnlineUpgradeSupported ();
	
	 
	public void addSafepoint (ISafepoint sp);  // affected version (old,new) is specified inside Safepoint
	
	
	public void safepointHit (ISafepoint sp); // safepoint was reached: enable strategy during upgrade and (in case of ! IUpgradeStrategy.isLazy(): start StateTransfer)
	
	// Who recognizes end of upgrade (no old objs anymore?) -> after visitor finished (all threads, all referenceed objects, all fields)
	public void onUpgradeFinish ()  throws OnlineUpgradeFailedException; // disable strategy during upgrade, cleanup safepoints, old version
	
	public void startRuntimeUpgrade() throws OnlineUpgradeFailedException;
	
	
}
