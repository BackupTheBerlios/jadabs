package ch.ethz.iks.proxy.bm;

import ch.ethz.iks.eventsystem.svc.EventServiceImpl;
import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.IComponentContext;
import ch.ethz.iks.proxy.bm.invoke.IBenchmarkCase;
import ch.ethz.iks.testcop.TestComponentMain;

/**
 * Benchmark configuration without a proxy (original setup).
 * Be sure to startup the container without any
 * command line arguments. The evolution component is not required.
 * This class is the main class of => component bmWithoutProxy
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class ConfigWithoutProxy extends ABenchmarkConfig {

	public static ConfigWithoutProxy createComponentMain() {
		return new ConfigWithoutProxy();
	}

	public void init(IComponentContext ctx) {
		this.bmName = "jadabs without proxy";
		super.initComponent();
	}
	
	public void prepareCase(IBenchmarkCase bmark) {
		if (null != ComponentRepository.Instance().getComponentResourceByClassname("ch.ethz.iks.evolution.mgr.ComponentEvolutionMain")) {
			System.out.println("[ERROR] do NOT include  evolution framework in pcoprep (evolution.jar) when benchmarking "+bmName);
			System.exit(-1);
		}
		
		bmark.setCallee(TestComponentMain.createComponentMain());
		EventServiceImpl es = EventServiceImpl.createComponentMain();
		bmark.setCallee(es);
		bmark.setCallee(es.createEvent("hello world"));
		bmark.prepare();
	}


}
