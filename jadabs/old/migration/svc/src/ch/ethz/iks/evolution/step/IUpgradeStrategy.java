package ch.ethz.iks.evolution.step;


import java.lang.reflect.Field;
import java.util.Iterator;

import ch.ethz.iks.evolution.adapter.IAdapter;
import ch.ethz.iks.evolution.adapter.cop.AdapterComponentResource;
import ch.ethz.iks.evolution.mgr.IEvolutionManager;
import ch.ethz.iks.evolution.mgr.OnlineUpgradeFailedException;
import ch.ethz.iks.jadabs.IComponent;
import ch.ethz.iks.jadabs.IComponentResource;

/**
 * Provides specific information to the current runtime evolution step represented by
 * a concrete object of this type. An adapter component being associated with a runtime evolution step
 * may implement this interface to customize behaviour.
 * 
 * It is planned that information may be read in from a specification file
 * attachted to the adapter component of this runtime evolution step.
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public interface IUpgradeStrategy extends IAdapter {
	
	public void initServiceMigr(IEvolutionManager mgr, IComponent oldCompoVersionMain, IComponent newCompoVersionMain) throws OnlineUpgradeFailedException;
	
	public boolean isUpgrading();
	
	public void transferState(IComponent oldExtObj, IComponent newExtObj) throws OnlineUpgradeFailedException;
	
	public Object transferState(Object oldObj, Object newObj, boolean b);
		
	public boolean doLaunchNewVersion();
	
	public boolean doMigrate(Field member);
	
	public void onUpgradeStart() throws OnlineUpgradeFailedException;
	
	public void onUpgradeFinish() throws OnlineUpgradeFailedException;
	
	public void updateOriginal(Object oldHidden, Object newHidden);
	
	public Iterator getRootObjects(IComponentResource cop);

	/**
	 * @param evolution
	 * @param oldLibraryInterfaces
	 * @param newLibraryInterfaces
	 */
	public void initLibraryMigr(IEvolutionManager mgr, AdapterComponentResource migrCop);
	
	

}
