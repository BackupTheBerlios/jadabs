package ch.ethz.iks.proxy.bm.invoke;

import ch.ethz.iks.eventsystem.IEventService;



/**
 * Benchmarks the performance of calls to <code>Object.toString()</code>.
 * The method is invoked on the main object of the escop component 
 * (must be included in the dependencies of the benchmark configuration component)
 * This case may be used with benchmark configurations that support forwarding of toString() only. 
 * This excludes both configurations based on the java dynamic proxy API
 * The main object is expected to be hidden by the corresponding proxy in case of configurations having a proxy. 
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class BenchmarktoStringInvocation implements IBenchmarkCase {

	
	public BenchmarktoStringInvocation() {
		super(); 
	}
	
	private IEventService obj;


	public void prepare() { 
		
	}


	public long measure() {
		long before = System.currentTimeMillis();
		for(int i=0;i < LOOP_COUNT; i++) {
			this.obj.toString();
		}
		long after = System.currentTimeMillis();
		return after - before;
	}

	public String getCaseName() {
		return "toString";
	}

	public void setCallee(Object callee) {
		if (callee instanceof IEventService) {
			this.obj = (IEventService)callee;
		}
	}

}
