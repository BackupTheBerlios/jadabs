package ch.ethz.iks.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.cop.UpgradeableComponentResource;
import ch.ethz.iks.evolution.cop.UpgradeableComponentResourceFactory;
import ch.ethz.iks.evolution.mgr.ComponentEvolutionMain;
import ch.ethz.iks.evolution.mgr.EvolutionManager;
import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.IComponent;
import ch.ethz.iks.jadabs.IComponentContext;
import ch.ethz.iks.jadabs.IComponentResource;
import ch.ethz.iks.jadabs.Jadabs;
import ch.ethz.iks.proxy.cop.ProxyComponentResourceFactory;
import ch.ethz.iks.proxy.cop.ProxyLoader;



/**
 * JUnit Testcase for dynamic proxy generation
 * Verifies that the <code>TransparentProxyFactory</code> 
 * <li>produces correct java source files
 * <li>follows the naming convention of the proxy class
 * <li>adds the required methods to the proxy (e.g. a static factory for instances)
 * <li>may compile the sources generated
 * <li>is able to load the compiled proxy classes
 * 
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class ProxyCreatorTest extends TestCase implements IComponent {
	
	private static Logger LOG = Logger.getLogger(ProxyCreatorTest.class);


	public static void main(String args[]) throws Exception {
		// manual testing
		
		ProxyCreatorTest t = new ProxyCreatorTest();
		t.setUp();
		t.testWrapAround();
		t.tearDown();
	}
	
	/** 
	 * Convention for instantiation of main classes of (service) components.
	 * The (default) constructor should be declared private to allow singleton components.
	 * @see ch.ethz.iks.cop.ComponentResource#initServiceComponent
	 * @return an instance of this class
	 */
	public static ProxyCreatorTest createComponentMain() {
		return new ProxyCreatorTest(); // non-singleton
	}
	

	public void setUp() throws Exception {
		ProxyComponentResourceFactory.getProxyFolder(); // create the proxies folder
		Thread jadabs = new Thread() {
			public void run() {
				Jadabs.main(new String [] {"-pcoprep", "bin/pcoprep"});
			}
		};
		jadabs.start();
		ComponentRepository.Instance().setResourceFactory(new UpgradeableComponentResourceFactory());
		LOG.info("[  idle  ]   Waiting until Jadabs is up ...");
		Thread.sleep(3000);
	}
	
	public void tearDown() {
		Jadabs.Instance().stop();
	}


	public void testWrapAround() throws Exception {
		assertTrue("[FAILED]   Provide the command line arg \"-proxy ifc\" to perform this test",ComponentEvolutionMain.doHideIfc());
		// create proxies for all components in the pcoprep
		
		Iterator cops = ComponentRepository.Instance().getComponentResources().values().iterator();
		while (cops.hasNext()) {
			// TODO: create proxies for all evolvable components in the pcoprep for testing
			IComponentResource icop = (IComponentResource) cops.next();
			if (! (icop instanceof UpgradeableComponentResource)) continue;
			UpgradeableComponentResource cop = (UpgradeableComponentResource)icop;
			if (! cop.isRuntimeEvolutionSupported(null)) continue;
			ProxyLoader proxyLoader = cop.getProxyLoader();  
			String cb = ProxyComponentResourceFactory.getProxyCodebase(cop); // create the proxy class destination folder
			// TODO: create proxies for all public classes of the component
			IComponent original = cop.getOriginalMainObject( EvolutionManager.getManager(cop,cop)); // hack to bypass access restriction to original object
			Class copIfc = original.getClass();
			
			System.out.println("\n[ ToDo ]   creating a proxy for "+copIfc.getName()+" of cop "+cop.getCodeBase()+" in proxy cop "+cop.getProxyLoader().getClassPath());
			assertFalse("[FAILED]   a proxy was returned by getOriginalObject", original instanceof IProxy);
			Class proxy = TransparentProxyFactory.getProxyClass(proxyLoader, copIfc); 
			assertNotNull(proxy);
			
			// proxy must implement the IProxy interface
			assertTrue(IProxy.class.isAssignableFrom(proxy));
			
			// proxy must have the same super class
			Class superC = copIfc.getSuperclass();
			Class superProxy = proxyLoader.loadClass(superC.getName());
			assertEquals("[FAILED]   proxy class does not extend the same class "+superC, proxy.getSuperclass(), superProxy == null ? superC : superProxy);
			
			// proxy must implement all interfaces the original class does
			Class [] implIfcs = copIfc.getInterfaces();
			Class [] proxyIfcs = proxy.getInterfaces();
			assertTrue(proxyIfcs.length > 0); // IProxy at index 0
			if (implIfcs != null) {
				for (int i = 0; i < implIfcs.length; i++) {
					Class proxyIfc = proxyLoader.loadClass(implIfcs[i].getName());
					assertEquals("[FAILED]   proxy does not implement "+implIfcs[i], proxyIfcs[i+1], proxyIfc == null ? implIfcs[i] : proxyIfc);
				}
			}
			System.out.println("[  OK  ]  proxy class extends/implements the same classes/interfaces as the original class");
			
			// proxy must declare all public methods the original class does
			Method[] methods = copIfc.getMethods();
			for (int i = 0; i < methods.length; i++) {
				if (! Modifier.isNative(methods[i].getModifiers()) ) {
					
					Class [] params = methods[i].getParameterTypes();
					if (params != null) {
						for (int j = 0; j < params.length; j++) { 
							if (params[j].isPrimitive()) {;}
							/*else if (params[j].isArray()) {
								MAY LOAD ARRAY CLASSES ???
								String classname = Decode.formatTypeName(params[j]);
								int eoname = classname.indexOf(' ');
								classname = classname.substring(0,eoname);
								Class proxyParam = proxyLoader.loadClass(params[j].getName());
								if (proxyParam != null) {
									params[j] = proxyParam.;
								}
							}*/ else {
								Class proxyParam = proxyLoader.loadClass(params[j].getName());
								if (proxyParam != null) {
									params[j] = proxyParam;
								}
							}
						}
					}
					Method proxyM = proxy.getMethod(methods[i].getName(),params);
					assertNotNull("[FAILED]   proxy class does not declare "+methods[i],proxyM);
					// proxy methods must declare all exceptions thrown the original class does
					Class[] exceptions = methods[i].getExceptionTypes();
					Class[] proxyExc = proxyM.getExceptionTypes();
					if (exceptions != null) {
						for (int j = 0; j < exceptions.length; j++) {
							Class proxyEx = proxyLoader.loadClass(exceptions[j].getName());
							if (proxyEx != null) {
								exceptions[j] = proxyEx;
							}
							assertEquals("[FAILED]   proxy class does not declare to throw "+exceptions[j],exceptions[j], proxyExc[j]);
						}
					}
					System.out.println("OK "+(i+1)+" of "+methods.length+" proxy declares : "+methods[i].getName());
				} else {
					System.out.println("[ WARN ]   native methods are not hidden! "+methods[i]);
				}
			}
			// TODO: warn if a public field is declared
			if (!(copIfc.getFields() == null || copIfc.getFields().length == 0)) {
				System.out.println("[ WARN ]   public fields are not hidden!");
			}
			// TODO: test if superclass proxy extends the superclass of the original class
			System.out.println("[  OK  ]   proxy class declares the same public methods (signature)");
			Object proxyObj = cop.getExtObject(); // returns a proxy instance
			assertFalse("[FAILED]   No proxy, but an original object has been created", proxyObj == original);
			assertFalse("[FAILED]   Proxy class and original class are the same", proxyObj.getClass() == original.getClass());
			assertTrue("[FAILED]   The proxy object is not an instance of the Proxy class created", proxy.isInstance(proxyObj));
			
			System.out.println("[ Done ]   Proxy for "+copIfc.getName());	
		}
						
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jadabs.IComponent#init(ch.ethz.iks.jadabs.IComponentContext)
	 */
	public void init(IComponentContext context) {
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jadabs.IComponent#startComponent(java.lang.String[])
	 */
	public void startComponent(String[] args) {
		try {
			//setUp();
			ProxyComponentResourceFactory.getProxyFolder();
			
			testWrapAround();
			
			//tearDown();
		} catch (Exception e) {
			LOG.error(this,e);
		}
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jadabs.IComponent#stopComponent()
	 */
	public void stopComponent() {
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jadabs.IComponent#disposeComponent()
	 */
	public void disposeComponent() {
		
	}

}
