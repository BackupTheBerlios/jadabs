package ch.ethz.iks.proxy.bm.invoke;


import ch.ethz.iks.eventsystem.IEvent;
import ch.ethz.iks.eventsystem.IEventService;


/**
 * Benchmarks the performance of calls to <code>IEventService.publish(IEvent)</code>.
 *  The method is invoked on the main object of the escop component 
 * (must be included in the dependencies of the benchmark configuration component)
 * This case may be used with any benchmark configuration. The main object is expected 
 * to be hidden by the corresponding proxy in case of configurations having a proxy. 
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class BenchmarkPublishInvocation implements IBenchmarkCase {

	
	public BenchmarkPublishInvocation() {
		super(); 
	}
	
	private IEventService es;
	private IEvent evt;
	

	
	public void prepare() { 
		if (es == null) {
			System.exit(-1);
		}
		this.evt = es.createEvent("hello world");
		
		if (evt == null) {
			System.exit(-1);
		}
	}

	
	public long measure() {
		long before = System.currentTimeMillis();
		for(int i=0;i < LOOP_COUNT; i++) {
			this.es.publish(evt);
		}
		long after = System.currentTimeMillis();
		return after - before;
	}

	public String getCaseName() {
		return "es.publish(evt)";
	}
	
	public void setCallee(Object callee) {
		if (callee instanceof IEventService) {
		this.es = (IEventService)callee;
		}
	}


	

}
