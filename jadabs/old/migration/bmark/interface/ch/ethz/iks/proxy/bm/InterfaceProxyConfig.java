package ch.ethz.iks.proxy.bm;

import ch.ethz.iks.eventsystem.IEvent;
import ch.ethz.iks.eventsystem.IEventService;
import ch.ethz.iks.evolution.mgr.ComponentEvolutionMain;
import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.ComponentResource;
import ch.ethz.iks.jadabs.IComponentContext;
import ch.ethz.iks.proxy.bm.invoke.IBenchmarkCase;

/**
 * Benchmark configuration for use with the interface based proxy (java dynamic proxy plus on-the-fly wrapping of arguments and return values)
 * Be sure to startup the container with the <code>-adapt name</code>
 * command line arguments. Additionally, the evolution component must be loaded by the container.
 * This class is the main class of => component bmInterfaceProxy
 * 
 * Note: benchmarks components that comply to the coding conventions only!
 * (as testcop.jar), but NOT escop.jar
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class InterfaceProxyConfig extends ABenchmarkConfig {

	
	/** 
	 * Convention for instantiation of main classes of (service) components.
	 * The (default) constructor should be declared private to allow singleton components.
	 * @see ch.ethz.iks.cop.ComponentResource#initServiceComponent
	 * @return an instance of this class
	 */
	public static InterfaceProxyConfig createComponentMain() {
		return new InterfaceProxyConfig(); // non-singleton
	}

	public void init(IComponentContext ctx) {
		this.bmName = "jadabs with interface-based proxy";
		super.initComponent();
	}
	
	public void prepareCase(IBenchmarkCase bmark) {
		ComponentResource evolutionFramework = (ComponentResource) ComponentRepository.Instance().getComponentResourceByClassname(ComponentEvolutionMain.class.getName());
		if (null == evolutionFramework) {
			System.out.println("[ERROR] MUST include evolution framework in pcoprep (evolution.jar) when benchmarking "+bmName);
			System.exit(-1);
		}
		boolean isTransparentProxyActive =  ComponentEvolutionMain.doHideIfc();
		if ( isTransparentProxyActive) {
			System.out.println("[ERROR] must NOT provide the command line arguments \"-proxy ifc \" when benchmarking "+bmName);
			System.exit(-1);
		}
		
		ComponentResource copRes = (ComponentResource) ComponentRepository.Instance().getComponentResourceByClassname("ch.ethz.iks.eventsystem.svc.EventServiceImpl");
		IEventService es = (IEventService) copRes.getExtObject();
		bmark.setCallee(es);
		IEvent evt = es.createEvent("hello world");
		bmark.setCallee(evt);
		
		copRes = (ComponentResource) ComponentRepository.Instance().getComponentResourceByClassname("ch.ethz.iks.testcop.TestComponentMain");
		bmark.setCallee(copRes.getExtObject());

		bmark.prepare();
	}
	
}
