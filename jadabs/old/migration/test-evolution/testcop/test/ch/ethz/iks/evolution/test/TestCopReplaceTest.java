package ch.ethz.iks.evolution.test;

import java.lang.reflect.Proxy;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.mgr.ComponentEvolutionMain;
import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.ComponentResource;
import ch.ethz.iks.jadabs.IComponentResource;
import ch.ethz.iks.proxy.IProxy;
import ch.ethz.iks.proxy.cop.ProxyComponentResourceFactory;
import ch.ethz.iks.testcop.ITestCop;
import ch.ethz.iks.testcop.ITestListener;

/**
 * Example upgrade of test component testcop.jar from version 1 to 2
 * Use as testcase to verify upgrade success. Supports dyanamic proxy upgrade only!
 * => MUST NOT import TestComponentMain class in case of dynamic proxies (!doIfcHiding() == true)
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class TestCopReplaceTest extends RuntimeMigrationTest implements ITestListener {

	private String targetCop = "testcop.jar";
	private static final String codebase =  "tcEvolutionTest.jar";
	private static Logger LOG = Logger.getLogger(TestCopReplaceTest.class);
	
	public static void main(String[]args) {
		TestCopReplaceTest  test = TestCopReplaceTest.createComponentMain();
		try {
			test.setUp();
			test.runTest();
			test.tearDown();
		} catch (Throwable t) {
			LOG.error("[FAILED]   ",t);
		}
	}
	
	/** 
	 * Convention for instantiation of main classes of (service) components.
	 * The (default) constructor should be declared private to allow singleton components.
	 * @see ch.ethz.iks.cop.ComponentResource#initServiceComponent
	 * @return an instance of this class
	 */
	public static TestCopReplaceTest createComponentMain() {
		return new TestCopReplaceTest(); // non-singleton
	}
	
	/* (non-Javadoc)
	 * @see ch.ethz.iks.upgrade.RuntimeMigrationTest#testOnlineUpgrade()
	 */
	public void testOnlineUpgrade() {
		IComponentResource es = ComponentRepository.Instance().getComponentResourceByCodebase(targetCop);
		try {
			this.evolveComponent( es ); //old Cop must have been started already
		} catch (Exception e) {
			LOG.error("caught "+e,e);
		}
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.upgrade.RuntimeMigrationTest#getCodeBase()
	 */
	public String getCodeBase() {
		return codebase;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.upgrade.RuntimeMigrationTest#useOldVersion(ch.ethz.iks.cop.ComponentResource)
	 */
	public Object useOldVersion(ComponentResource testCop) throws Exception {
		Object proxy = this.useVersion();
		LOG.info(  "[  OK  ]   useOldVersion: tcm should be a proxy object");
		
		IComponentResource cop = null;
		if (ComponentEvolutionMain.doHideIfc()) {
			cop = ProxyComponentResourceFactory.getResourceOfProxy(proxy.getClass().getName()).getOriginalComponent();
		} else {
			cop = ComponentRepository.Instance().getComponentResourceByCodebase(targetCop);
		}
		assertTrue("wrong version "+ cop.getVersion()+", should be 1", cop.getVersion() == 1);
		return proxy;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.upgrade.RuntimeMigrationTest#useNewVersion(ch.ethz.iks.cop.ComponentResource)
	 */
	public Object useNewVersion(ComponentResource testCop) throws Exception {
		Object proxy = this.useVersion(); 
		LOG.info(  "[  OK  ]   useNewVersion: tcm should be a proxy object");
		
		IComponentResource cop = null;
		if (ComponentEvolutionMain.doHideIfc()) {
			cop = ProxyComponentResourceFactory.getResourceOfProxy(proxy.getClass().getName()).getOriginalComponent();
		} else {
			cop = ComponentRepository.Instance().getComponentResourceByCodebase(targetCop);
		}
		assertTrue("wrong version "+ cop.getVersion()+", should be 2", cop.getVersion() == 2);
		return proxy;
	}
	
	
	private Object useVersion() {
		ComponentResource cop = (ComponentResource) ComponentRepository.Instance().getComponentResourceByCodebase(targetCop);
		ITestCop tc = (ITestCop) cop.getExtObject(); 
		
		if (ComponentEvolutionMain.doHideIfc()) {
			assertTrue("[FAILED]   tcm should be a proxy object", tc instanceof IProxy);
		} else {
			assertTrue("[FAILED]   tcm should be a proxy object", tc instanceof Proxy);
		}
		// modify state of old, check if state has been copied to new
		tc.nop();
		tc.getInternalObj();
		tc.subscr(this);
		return tc;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.upgrade.RuntimeMigrationTest#getPrefixOfNewVersion()
	 */
	public String getPrefixOfNewVersion() {
		return "V2_";
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#disposeComponent()
	 */
	public void disposeComponent() {
		
	}
	
	public void react(ITestCop ext) {
		if (ComponentEvolutionMain.doHideIfc()) {
			assertTrue(ext instanceof IProxy);
		} else {
			assertTrue(ext instanceof Proxy);
		}
	}

}
