package ch.ethz.iks.proxy;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Enumeration;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.cop.UpgradeableComponentLoader;
import ch.ethz.iks.evolution.cop.UpgradeableComponentResource;
import ch.ethz.iks.evolution.mgr.ComponentEvolutionMain;
import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.IComponent;
import ch.ethz.iks.jadabs.IComponentContext;
import ch.ethz.iks.jadabs.IComponentResource;
import ch.ethz.iks.jadabs.Jadabs;
import ch.ethz.iks.proxy.cop.ProxyComponentResourceFactory;
import ch.ethz.iks.proxy.cop.ProxyLoader;
import ch.ethz.iks.utils.Decode;
import ch.ethz.iks.utils.DependencyInspector;

/**
 * JUnit Test case for the transparent proxy creation and component dependency replacement
 * <p>
 * This Testcase may be run as an ordinary Jadabs component to simplify testing. 
 * However, it produces <em>non predictable results if run inside a development environment</em> 
 * that uses its own Classloading mechanism, e.g the eclipse IDE
 * </p><p>
 * To be able to verify the dynamic class of the external reference (the dependency) is replaced by a proxy at loadtime,
 * the component to be tested (the one with dependencies) has to implement the <code>interface DependencyInspector</code> 
 * </p><p>
 * To run the test as jadabs component, just create a Jarfile containing this class that conforms to the
 * Jadabs specification (Manifest) and put it into the pcoprep Folder (<code>Jadabs.getProperty(Jadabs.PCOPREP)</code>).
 * Start the Jadabs container and the Testcase is executed automatically. Be sure to declare both the Component having dependencies (e.g. <code>copWithDependency.jar</code>)
 * and the Component being referenced as dependency from the latter (e.g. <code>testcop.jar</code>) as dependencies of this Component in the Jars Manifest in order to guarantee
 * that both Components get loaded BEFORE the Testcase is executed. Just start the jadabs container.
 * Jadabs Usage (each statement on a single line): <br>  
 * <li>cd $jadabs_home
 * <li>perl jadabs.pl transparent
 * <li>Components that must be in the pcoprep upon starting jadabs: baselib.jar testcop.jar
 * <li>After starting jadabs, insert evolution.jar into pcoprep
 * <li>Then insert copWithDependeny.jar into pcoprep
 * <li>Finally insert the component this class belongs to (proxyTest.jar) into pcoprep 
 * </p><p>
 * To run the test with JUnit, make sure to remove the Jarfile containing this class from the pcoprep folder (e.g. <code>rm bin/pcoprep/evolutionTestcase.jar</code>).
 * JUnit Usage (each statement on a single line): <br>  
 * <li>cd $jadabs_home
 * <li>java -classpath bin/lib/jadabs.jar:bin/lib/common.jar:libs/junit-3.8.1.jar:libs/log4j-1.2.8.jar:libs/javassist-2.6.jar:libs/tools.jar:bin/ext/proxyTest.jar ch.ethz.iks.proxy.TransparentProxyTest -pcoprep $pcoprep
 * where $jadabs_home denotes the home directory of jadabs, e.g. ~/workspace/jadabs  
 * and   $pcoprep denotes the persisten component repository folder, e.g. bin/pcoprep
 * </p>
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */

public class TransparentProxyTest extends TestCase implements IComponent {

	/**
	 * Just for manual testing, Usage see above.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		TransparentProxyTest test = new TransparentProxyTest(args);
		test.setUp();
		test.testByteCodeInstrumentation();
		test.tearDown();
	}

	private static Logger LOG = Logger.getLogger(TransparentProxyTest.class);
	private String[] cmdline;
	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		System.out.println("[  HELP  ]   Enable logging to get verbose output for this testcase");
		//ProseSystem.startup(); // enables insertion of aspects at runtime
		Thread jadabs = new Thread() {
			public void run() {
				String[] args = cmdline;/*new String[2];
				args[0] = "-pcoprep";
				args[1] = "bin/pcoprep";*/
				Jadabs.main(args);
			}
		};
		jadabs.setName("Jadabs container");
		jadabs.start();
		// starts the jadabs container
		// initializes the ComponentManager, the ComponentRepository
		// and starts the LocalComponentLoader listening to the directory specified in args
		// NOT SUPPORTED! Class has not yet been loaded by Jadabs: boolean isComponent = ComponentRepository.Instance().getComponentResourceByClassname(StartEvolutionTest.class.getName()) != null;
		//if (!isComponent) 
		log("[  idle  ]   Waiting until Jadabs is up ...");
		Thread.sleep(3000);
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		boolean isComponent = ComponentRepository.Instance().getComponentResourceByClassname(this.getClass().getName()) != null;
		if (!isComponent)Thread.sleep(3000); // wait until threads of components have been terminated
		else {
			//this.stopComponent();
			Enumeration cops = ComponentRepository.Instance().getComponentResources().elements();
			while (cops.hasMoreElements()) {
				IComponentResource cop = (IComponentResource) cops.nextElement();
				cop.stopComponent();
			}
		}
		super.tearDown();
		//ProseSystem.teardown();
		Jadabs.Instance().stop();
	}

	/**
	 * Constructor for TransparentProxyTest.
	 * @param arg0
	 */
	public TransparentProxyTest(String [] args) {
		super(args[0]);
		this.cmdline = args;
	}
	
	/**
	 * Default constructor (each jadabs component must provide one) 
	 */
	private TransparentProxyTest() {
		super();
	}
	
	/** 
	 * Convention for instantiation of main classes of (service) components.
	 * The (default) constructor should be declared private to allow singleton components.
	 * @see ch.ethz.iks.cop.ComponentResource#initServiceComponent
	 * @return an instance of this class
	 */
	public static TransparentProxyTest createComponentMain() {
		return new TransparentProxyTest(); // non-singleton
	}

	/** 
	 * Testcase for load time scanning of classes using javassist and transparent proxy class creation.
	 * 
	 * <em>IMPORTANT: DO NOT import the class having dependencies NOR the dependency class itself !</em>
	 * In the following piece of code, these correspond with the classes <code>ch.ethz.iks.testclient.ComponentUsingAnotherOne</code> and <code>ch.ethz.iks.testcop.TestComponentMain</code> 
	 * 
	 * For this Testcase the JProse VM is NOT required. You may use any JRE version 1.3.x
	 * 
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	public void testByteCodeInstrumentation() throws Exception {
		hasReplacedExternalReferences("ch.ethz.iks.testclient.ComponentUsingAnotherOne", "ch.ethz.iks.testcop.TestComponentMain");
		// TODO: Add other cases (be sure to provide a getter to the external object): 
		// hasReplacedExternalReferences("", "");
	}
	
	private boolean hasReplacedExternalReferences(String qualifiedNameOfClassWithExternalReferences, String qualifiedNameOfExternallyReferencedClass)  throws Exception {	
		// prepare
		String packagePrefixOfExternallyReferencedClass = Decode.getPackage(qualifiedNameOfExternallyReferencedClass) + "."; //"ch.ethz.iks.testcop.";
		String packagePrefixOfClassWithExternalReferences = Decode.getPackage(qualifiedNameOfClassWithExternalReferences) + "."; //"ch.ethz.iks.test_cop.";
		String nameOfExternallyReferencedClass = Decode.getSimpleName(qualifiedNameOfExternallyReferencedClass); //"TestComponentMain";
		String nameOfClassWithExternalReferences = Decode.getSimpleName(qualifiedNameOfClassWithExternalReferences); //"ComponentUsingAnotherOne";
		String pcoprep = (String) ComponentEvolutionMain.PCOPREP;
		if (pcoprep == null) pcoprep = "bin"+File.separator+"pcoprep";
		assertNotNull("Please specify the persistent component repository folder by the command line argument -pcoprep",pcoprep);
		File persistentComponentRepository = new File(pcoprep);
		
		// Checking test precondition: testcop jar must be in pcoprep
		IComponentResource cop = ComponentRepository.Instance().getComponentResourceByClassname(packagePrefixOfExternallyReferencedClass + nameOfExternallyReferencedClass);
		debug("[ ToDo ]   Testcase:   hasReplacedExternalReferences( "+qualifiedNameOfClassWithExternalReferences+" , "+qualifiedNameOfExternallyReferencedClass+" )");
		String message;
 	  
 	  	assertTrue("[FAILED]   "+cop+" is not evolvable", cop instanceof UpgradeableComponentResource);
		UpgradeableComponentResource externallyReferencedCop = (UpgradeableComponentResource) cop;
		String copJarName = externallyReferencedCop.getCodeBase();
		File copJarFile = new File(persistentComponentRepository, copJarName);
		message  = "The Jar File "
				+ copJarName
				+ " of the Component "
				+ nameOfExternallyReferencedClass
				+ " should be put into the folder "
				+ persistentComponentRepository.getAbsolutePath()
				+ " before running this Testcase"
		;
		assertTrue("[FAILED]   "+message, copJarFile.exists());
		log("[  OK  ]   "+message);
		
		// the directory that contains all proxy Classfiles and the ones of each component must be created by the program
		File proxyFolder =
			new File(persistentComponentRepository.getParentFile(), IProxy.proxyFolder);
		message  = "The Folder \'"
				+ IProxy.proxyFolder
				+ "\' that contains the proxy components should have been created in the folder \'"
				+ persistentComponentRepository.getParent()
				+ "\'"
		;
		assertTrue("[FAILED]   "+message, proxyFolder.exists() && proxyFolder.isDirectory());
		log("[  OK  ]   "+message);
		File proxyFolderOfExternallyReferencedCop =
			new File(proxyFolder, ProxyComponentResourceFactory.getProxyCodebase(externallyReferencedCop));
		message  = "The Folder "
				+ proxyFolderOfExternallyReferencedCop.getAbsolutePath()
				+ "  that contains the proxy binaries of the Component "
				+ copJarName
				+ " should have been created"
		;
		assertTrue("[FAILED]   "+message, proxyFolderOfExternallyReferencedCop.exists() && proxyFolderOfExternallyReferencedCop.isDirectory());
		log("[  OK  ]   "+message);
		
		// Checking test precondition: copWithDependency jar must be in pcoprep
		UpgradeableComponentResource copWithDependencies = (UpgradeableComponentResource)
			ComponentRepository.Instance().getComponentResourceByClassname(packagePrefixOfClassWithExternalReferences + nameOfClassWithExternalReferences);
		assertNotNull("[FAILED]   copWithDependencies is NULL ", copWithDependencies);
		String copWDJarName = copWithDependencies.getCodeBase();
		copJarFile = new File(persistentComponentRepository, copJarName);
		message  = "The Jar File "
				+ copJarName
				+ " of the Component "
				+ copJarName
				+ " should be put into the folder "
				+ persistentComponentRepository.getAbsolutePath()
				+ " before running this Testcase"
		;
		assertTrue("[FAILED]   "+message, copJarFile.exists());
		log("[  OK  ]   "+message);
		
		/* no condition anymore: Checking test precondition: the proxy class of externallyReferencedCopClass must implement a (static) factory method
		Class main = externallyReferencedCop.getClassLoader().loadClass(externallyReferencedCop.getClassName()); // main class of cop
		String proxyClassNameOfExtRefObj = Decode.getSimpleName(main.getName()); ///ProxyFactory.getNamingOfWrapper(main);
		main = externallyReferencedCop.getProxyLoader().loadClass(packagePrefixOfExternallyReferencedClass + proxyClassNameOfExtRefObj);// proxyclass hiding mainclass;
		Method factory = main.getMethod(ProxyFactory.factoryMethod, null);
		Class declaredReturnType = factory.getReturnType();
		 message  = "The factory method "
				 + factory.getName()
				 + " in proxy classes "
				 + " should be declared to return an object of class "
				 + packagePrefixOfExternallyReferencedClass 
				 + nameOfExternallyReferencedClass
		;
		
		assertEquals("[FAILED]   "+message, declaredReturnType.getName(), packagePrefixOfExternallyReferencedClass + nameOfExternallyReferencedClass);
		log("[  OK  ]   "+message);
		*/
		// Checking test precondition: the Main class of copWithDependency must implement the DependencyInspector interface
		String classname = copWithDependencies.getClassName();
		ClassLoader loader = copWithDependencies.getClassLoader();	
		message = "The ClassLoader of the component "+copWithDependencies.getCodeBase()+" should know the class named "+classname;
		assertTrue("[FAILED]   "+message,((UpgradeableComponentLoader)loader).knowsClass(classname));
		log("[  OK  ]   "+message);
		Class main = loader.loadClass(classname);
		Class [] ifcs = main.getInterfaces();
		int i;
		for (i = 0; i < ifcs.length; i++) {
			if (ifcs[i].getName().equals(DependencyInspector.class.getName())) break;
		}
		message  = "The class having dependencies ("
				+ packagePrefixOfClassWithExternalReferences 
				+ nameOfClassWithExternalReferences
				+ ") should implement the interface DependencyInspector to allow testing"
		;
		assertTrue("[FAILED]   "+message, i < ifcs.length);
		log("[  OK  ]   "+message);
		
		// Checking test precondition: Both components have to loaded before testing may start
		///assertTrue("[FAILED]   component "+packagePrefixOfClassWithExternalReferences + nameOfClassWithExternalReferences+" has not been started", externallyReferencedCop != null && externallyReferencedCop.isStarted());
		assertTrue("[FAILED]   component "+packagePrefixOfExternallyReferencedClass + nameOfExternallyReferencedClass+" has not been started", copWithDependencies != null && copWithDependencies.isStarted());
		log("[  OK  ]   both components have been started");
		// Dynamic compilation: the proxy class must be compiled to the proxy directory of the component
		/*
		String pathToProxyClassFile = Decode.getAsPath(packagePrefixOfExternallyReferencedClass + nameOfExternallyReferencedClass);///proxyClassNameOfExtRefObj);
		File proxyClassFile = new File(proxyFolderOfExternallyReferencedCop, pathToProxyClassFile + ".class");
		message = "The binary file declaring the proxy class "
				+ proxyClassNameOfExtRefObj
				+ " should exist in the proxy directory "
				+ proxyFolderOfExternallyReferencedCop.getAbsolutePath()
				+ " of the Component (may be a compile error)"
		; 
		assertTrue("[FAILED]   "+message, proxyClassFile.exists());
		log("[  OK  ]   "+message);
		*/
		// dependencies should have been replaced by proxy cops
		message = "dependency "+copJarName+" should have been replaced by proxy cop "+ProxyComponentResourceFactory.getProxyCodebase(externallyReferencedCop);
		assertFalse("[FAILED]   "+message, copWithDependencies.getComponentDeps().contains(copJarName));
		assertTrue("[FAILED]   "+message, copWithDependencies.getComponentDeps().contains(ProxyComponentResourceFactory.getProxyCodebase(externallyReferencedCop)));
		log("[  OK  ]   "+message);
		// The external reference must be replaced by an instance of the proxy class
		
		Object mainObj = main.getDeclaredMethod(IComponent.factoryMethod, null).invoke(null,null);
		log("main "+ mainObj.getClass().getName());
		Method getExternalReference = main.getMethod(DependencyInspector.externalReferenceGetterName, null); 
		log("invoking "+ getExternalReference.getName());
		Object extRefObj = getExternalReference.invoke(mainObj, null); 
		log("getExternalReference returned "+ extRefObj);
		//extRefObj.getClass().getMethod("createString",null).invoke(extRefObj,null);
		message = "IProxy interface: External reference should be a proxy instance";	
		assertTrue("[FAILED]   "+message, extRefObj instanceof IProxy);
		log("[  OK  ]   "+message);
		// The proxy class must have been loaded by its ProxyLoader
		Method getClassLoader = main.getMethod(DependencyInspector.classLoaderGetterName, null); 
		ClassLoader cl = (ClassLoader) getClassLoader.invoke(mainObj, null);
		
		message = "Proxy loading: External reference should have been loaded by its ProxyLoader";	
		assertTrue("[FAILED]   "+message, ProxyLoader.class.isInstance(cl));
		log("[  OK  ]   "+message);
		String path = ((ProxyLoader)cl).getClassPath();
		log("[  OK  ]   External Reference has been loaded from "+path);
		message ="[ Done ]   Testcase completed sucessfully:   hasReplacedExternalReferences( "+qualifiedNameOfClassWithExternalReferences+" , "+qualifiedNameOfExternallyReferencedClass+" )";
		debug(message);
		return true; // testcase succeded
	}
	
	

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#initComponent()
	 */
	public void init(IComponentContext ctx) {
		try {
			setUp();
		} catch (Exception e) {
			error(this,e);
			fail(e.getClass().getName()+" occured during test setup, aborting... "+e.toString());
		}
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#startComponent(java.lang.String[])
	 */
	public void startComponent(String[] args) {
		log("Started Testcase for evolution component");
		// Need to start the Testcase in an own Thread
		try {
				Thread testcase = new Thread(){
					public void run() {
						try {
						
							testByteCodeInstrumentation();
						} catch (Exception e) {
							
							error(e.getClass().getName()+" occured during testing, aborting... "+e.toString());
							fail(e.getClass().getName()+" occured during testing, aborting... "+e.toString());
						}
					}
				};
				testcase.setName("StartEvolutionTestcase");
				testcase.start();
		} catch (Exception e) {
			
			error(e.getClass().getName()+" occured during testing, aborting... "+e.toString());
			fail(e.getClass().getName()+" occured during testing, aborting... "+e.toString());
		}
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#stopComponent()
	 */
	public void stopComponent() {
		log("Finished Testcase for evolution component");
		try {
			tearDown();
		} catch (Exception e) {
			error(this,e);
			fail(e.getClass().getName()+" occured during test teardown, aborting... "+e.toString());
		}
	}
	
	
	/* 
	 * wrappers for logging to switch between log4j and std out (for debugging of testcase only)
	 */
	private void log (String msg) {
		System.out.println(msg);
		//LOG.info(msg);
	}
	private void debug (String msg) {
		System.out.println(msg);
		//LOG.debug(msg);
	}
	private void error (String msg) {
		System.out.println(msg);
		//LOG.error(msg);
	}
	private void error (Object _this , Exception thrown) {
		error(_this.getClass().getName()+" threw an "+thrown.getClass().getName()  );
		//LOG.error(_this,thrown);
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#disposeComponent()
	 */
	public void disposeComponent() {
		
	}
}
