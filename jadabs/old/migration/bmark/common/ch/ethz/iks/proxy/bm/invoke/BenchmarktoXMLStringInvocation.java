package ch.ethz.iks.proxy.bm.invoke;

import ch.ethz.iks.eventsystem.IEvent;
import ch.ethz.iks.eventsystem.IEventService;




/**
 * Benchmarks the performance of calls to <code>IEvent.toXMLString()</code>.
 * The method is invoked on a event created by the main object of the escop component 
 * (must be included in the dependencies of the benchmark configuration component)
 * This case may be used with any benchmark configuration. The main object is expected 
 * to be hidden by the corresponding proxy in case of configurations having a proxy. 
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class BenchmarktoXMLStringInvocation implements IBenchmarkCase {

	
	public BenchmarktoXMLStringInvocation() {
		super(); 
	}
	
	private IEventService es;
	private IEvent evt;

	public void prepare() { 
		
	}


	public long measure() {
		long before = System.currentTimeMillis();
		for(int i=0;i < LOOP_COUNT; i++) {
			this.evt.toXMLString();
		}
		long after = System.currentTimeMillis();
		return after - before;
	}

	public String getCaseName() {
		return "toXMLString";
	}

	public void setCallee(Object callee) {
		if (callee instanceof IEvent) {
			this.evt = (IEvent)callee;
		}
	}

}
