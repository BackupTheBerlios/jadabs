package ch.ethz.iks.proxy.bm;

import ch.ethz.iks.eventsystem.svc.EventServiceImpl;
import ch.ethz.iks.evolution.mgr.ComponentEvolutionMain;
import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.ComponentResource;
import ch.ethz.iks.jadabs.IComponentContext;
import ch.ethz.iks.proxy.bm.invoke.IBenchmarkCase;
import ch.ethz.iks.testcop.TestComponentMain;

/**
 * Benchmark configuration for use with the transparent proxy
 * Be sure to startup the container with the <code>-proxy ifc -adapt hash</code>
 * command line arguments. Additionally, the evolution component must be loaded by the container.
 * This class is the main class of => component bmTransparentProxy
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class TransparentProxyConfig extends ABenchmarkConfig {

	
	/** 
	 * Convention for instantiation of main classes of (service) components.
	 * The (default) constructor should be declared private to allow singleton components.
	 * @see ch.ethz.iks.cop.ComponentResource#initServiceComponent
	 * @return an instance of this class
	 */
	public static TransparentProxyConfig createComponentMain() {
		return new TransparentProxyConfig(); // non-singleton
	}

	public void init(IComponentContext ctx) {
		this.bmName = "jadabs with transparent proxy";
		super.initComponent();
	}
	
	public void prepareCase(IBenchmarkCase bmark) {
		ComponentResource evolutionFramework = (ComponentResource) ComponentRepository.Instance().getComponentResourceByClassname(ComponentEvolutionMain.class.getName());
		if (null == evolutionFramework) {
			System.out.println("[ERROR] MUST include evolution framework in pcoprep (evolution.jar) when benchmarking "+bmName);
			System.exit(-1);
		}
		boolean isTransparentProxyActive =  ComponentEvolutionMain.doHideIfc();
		if (! isTransparentProxyActive) {
			System.out.println("[ERROR] MUST provide the command line arguments \"-proxy ifc \" when benchmarking "+bmName);
			System.exit(-1);
		}
		
		bmark.setCallee(TestComponentMain.createComponentMain());
		EventServiceImpl es = EventServiceImpl.createComponentMain();
		bmark.setCallee(es);
		bmark.setCallee(es.createEvent("hello world"));

		bmark.prepare();
	}
	
}
