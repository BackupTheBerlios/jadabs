package ch.ethz.iks.evolution.test;
import java.io.File;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.adapter.DefaultAdapter;
import ch.ethz.iks.evolution.adapter.cop.AdapterComponentResourceFactory;
import ch.ethz.iks.evolution.cop.UpgradeableComponentResource;
import ch.ethz.iks.evolution.mgr.ComponentEvolutionMain;
import ch.ethz.iks.evolution.mgr.EvolutionManager;
import ch.ethz.iks.evolution.mgr.IEvolutionManager;
import ch.ethz.iks.jadabs.BootstrapConstants;
import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.ComponentResource;
import ch.ethz.iks.jadabs.IComponent;
import ch.ethz.iks.jadabs.IComponentContext;
import ch.ethz.iks.jadabs.IComponentResource;
import ch.ethz.iks.proxy.cop.ProxyComponentResourceFactory;

/**
 * Testcase for runtime component evolution
 * 
 * Extend this testcase to verify the success of an online upgrade of a concrete component, e.g. escop to version 3
 * This testcase is suitable only for SERVICE components that's main object is a SINGLETON (condition)
 * The component being upgraded as well as its dependencies must be upgradeable and hidden by a proxy component.
 * 
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public abstract class RuntimeMigrationTest extends TestCase implements IComponent  {


	private static Logger LOG = Logger.getLogger(RuntimeMigrationTest.class);
	
	private Logger proxyDump = Logger.getLogger("ProxyDump");
	private IComponentContext ctx;
	
	public void setUp() throws Exception {
		super.setUp();
	}
	
	public void tearDown() throws Exception {
		super.tearDown();
	}
	
	public abstract void testOnlineUpgrade(); 
	
	public abstract String getCodeBase();
	
	// access the proxy and perfom some operations (modifiy state)
	public abstract Object useOldVersion( ComponentResource testCop ) throws Exception;
	
	public abstract Object useNewVersion( ComponentResource testCop ) throws Exception;

	public abstract String getPrefixOfNewVersion();


	protected void evolveComponent(IComponentResource oldCop) throws Exception {
		// old Cop supports evolution?
		LOG.info("[ ToDo ]       evolve cop "+oldCop.getCodeBase()+" from version "+oldCop.getVersion());
		System.out.println("[ HELP ]       Enable logging to get verbose output for this testcase");
		assertNotNull("[FAILED]   1)", oldCop);
		assertTrue("[FAILED]   2)", oldCop instanceof UpgradeableComponentResource);
		assertTrue("[FAILED]   3)",  ((UpgradeableComponentResource)oldCop).isRuntimeEvolutionSupported(null) );
		// evolution cop installed?
		File pcoprep = new File( (String) ctx.getProperty(BootstrapConstants.PCOPREP));
		IComponentResource evolutionCop = ComponentRepository.Instance().getComponentResourceByCodebase("evolution.jar");
		assertNotNull("[FAILED]   4)", evolutionCop);
		
		ComponentResource testCop = (ComponentResource) ComponentRepository.Instance().getComponentResourceByCodebase(this.getCodeBase());
		
		
		if (ComponentEvolutionMain.doHideIfc() ) {
			// transparent proxy: dependency replaced by proxy cop?
			String proxyCopOfOldCopID = ProxyComponentResourceFactory.getProxyCodebase(oldCop); 
			assertTrue("[FAILED]    5) The dependency "+oldCop.getCodeBase()+" of testCop "+testCop.getCodeBase()+" should have been replaced by its proxy cop "+proxyCopOfOldCopID+" deps = "+testCop.getComponentDeps().toString(), testCop.getComponentDeps().contains( proxyCopOfOldCopID ));
			// access the proxy and perform some operations (modifiy state)
		}
		Object proxy = this.useOldVersion( testCop ); // abstract, returns a (transparent) IProxy or (dynamic) java.lang.reflect.Proxy
		
		
		// newer version available (inside ext) ?
		File extFolder = new File(pcoprep.getParentFile(), "ext");
		assertTrue("[FAILED]  15)", extFolder.exists());
		File newCopFile = new File(extFolder, getPrefixOfNewVersion() + oldCop.getCodeBase() );
		assertTrue("[FAILED]  16) File Not found "+newCopFile, newCopFile.exists());
		
		// verify version is newer than oldCop.getVersion() [could be the same -> no evolution happens]
		int newVersion = Integer.MIN_VALUE;
		JarFile newCopJar = new JarFile(newCopFile);
		Attributes atts = newCopJar.getManifest().getMainAttributes();
		String version = atts.getValue(Attributes.Name.IMPLEMENTATION_VERSION);
		newVersion = Integer.parseInt(version);
		assertTrue("[FAILED]  17) no newer version found than running one "+oldCop.getVersion(), oldCop.getVersion() < newVersion);
		// migr cop available ?
		File migrCopFile = new File(AdapterComponentResourceFactory.getAdaptersFolder() + File.separator + AdapterComponentResourceFactory.getAdapterCodebase(oldCop) );
		assertTrue("[FAILED]  18) File not found: "+migrCopFile,  migrCopFile.exists() );
		
		//INSERT new version (MOVE inside pcoprep folder)
		File destOfNewCop = new File(pcoprep, oldCop.getCodeBase() );
		long oldTimestamp = destOfNewCop.lastModified();
		
		DefaultAdapter.printProxies(proxyDump, "BEFORE evolution"); 
		// move manually
		System.out.println("[input]     Please copy the new version "+newCopFile+" to "+destOfNewCop);
		//newCopFile.renameTo(destOfNewCop); 
		while(destOfNewCop.lastModified() <= oldTimestamp) {
			Thread.sleep(6000);
			System.out.println("[input]     cp "+newCopFile.getAbsolutePath()+" "+destOfNewCop.getAbsolutePath());
		}
		assertTrue("[FAILED]  19)  File not found after moving: "+destOfNewCop, destOfNewCop.exists());
		assertTrue("[FAILED]  20)", destOfNewCop.getParentFile().equals(pcoprep) );
		assertTrue("[FAILED]  21)", destOfNewCop.getName().equals(oldCop.getCodeBase()) );
		// jadabs should pick up the new version now
		newCopFile = null;
		destOfNewCop= null;
		IComponentResource newCop = null;
		while (newCop == null || newCop.getVersion() == oldCop.getVersion()) {
			newCop = ComponentRepository.Instance().getComponentResourceByCodebase(oldCop.getCodeBase());
			LOG.info("[ idle ]      waiting until upgrade begins ...");
			Thread.sleep(50);
		}
		IEvolutionManager darwin = EvolutionManager.getManager((UpgradeableComponentResource)oldCop, (UpgradeableComponentResource)newCop);
		assertTrue("[FAILED]  22)", darwin.isOnlineUpgradeSupported());
		
		while( darwin.isUpgrading() ) {
			LOG.info("[ idle ]      waiting until upgrade has been finished ...");
			Thread.sleep(3000);
		}
		// this method returns new version AFTER evolution has been completed
		LOG.info("upgrade finished, testing new version...");
		newCop = ComponentRepository.Instance().getComponentResourceByCodebase(oldCop.getCodeBase());
		assertTrue("[FAILED]  23)", newCop != oldCop); 
		assertTrue("[FAILED]  24)", newCop.getVersion() == newVersion);
		
		// access the proxy after upgrade has completed
		Object newProxy = this.useNewVersion(testCop); // abstract
		assertTrue("[FAILED]  35): Proxy should remain the same for both versions: "+proxy+" != "+newProxy, proxy == newProxy); // main object isa singleton
		DefaultAdapter.printProxies(proxyDump,"AFTER  evolution");
		LOG.info("[ Done ]       evolving cop "+newCop.getCodeBase()+" completed successfully, now running version "+newCop.getVersion());
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#initComponent()
	 */
	public void init(IComponentContext ctx) {
		this.ctx = ctx;
		try {
			setUp();
		} catch (Exception e) {
			LOG.error(this,e);
			fail(e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#startComponent(java.lang.String[])
	 */
	public void startComponent(String[] args) {
		
			Thread tester = new Thread() {
				public void run() {
					this.setName("testing upgrade of "+getCodeBase());
					
					testOnlineUpgrade();
					
				}
			};
			tester.start();
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#stopComponent()
	 */
	public void stopComponent() {
		try {
			tearDown();
		} catch (Exception e) {
			LOG.error(this,e);
			fail(e.getMessage());
		}
	}
	
	

}
