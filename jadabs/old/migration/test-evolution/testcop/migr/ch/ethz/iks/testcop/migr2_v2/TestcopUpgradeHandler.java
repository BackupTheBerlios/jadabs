package ch.ethz.iks.testcop.migr2_v2;

import java.lang.reflect.Method;

import ch.ethz.iks.evolution.mgr.OnlineUpgradeFailedException;
import ch.ethz.iks.evolution.step.DefaultUpgradeStrategy;
import ch.ethz.iks.jadabs.IComponentResource;

/**
 * Upgrade customizer for testcop, v2
 * =>  component: adapter2_2_testcop/
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */ 
public class TestcopUpgradeHandler extends DefaultUpgradeStrategy {

	/* (non-Javadoc)
	 * @see ch.ethz.iks.migr.DefaultUpgradeStrategy#doLaunchNewVersion()
	 */
	public boolean doLaunchNewVersion() throws OnlineUpgradeFailedException {
		return true;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.migr.DefaultUpgradeStrategy#doMigrateAll()
	 */
	public boolean doMigrateAll() {
		return false;
	}

	
	public void onUpgradeStart() throws OnlineUpgradeFailedException {
		
	}

	

	
	public void updateOriginal(Object oldHidden, Object newHidden) {
		
	}

	
	public Object invoke(Object proxy, String methodName, String declaringClass, Object[] args) throws Throwable {
		return null;
	}

	
	public Object invoke(Object proxy, int methodCode, String declaringClass, Object[] args) throws Throwable {
		return null;
	}

	
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		
		return null;
	}

	public Object [] getLibraryInterface( IComponentResource cop) {
		
		throw new RuntimeException("TestCop ias Service, not a Library component");
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.migr.DefaultUpgradeStrategy#createNew(java.lang.Object)
	 */
	protected Object createNew(Object oldObj) {
		// TODO implement TestCopUpgradeHandler.createNew
		throw new RuntimeException("NOT YET IMPLEMENTED: +TestCopUpgradeHandler.createNew");
		//return null;
	}

	

}
