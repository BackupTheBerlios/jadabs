package ch.ethz.iks.proxy.bm.invoke;

import ch.ethz.iks.testcop.ITestCop;



/**
 * Benchmarks the performance of calls to <code>ITestCop.getInternalObj()</code>.
 * 
 * The method is invoked on the main object of the testcop component 
 * (must be included in the dependencies of the benchmark configuration component)
 * This case may be used with any benchmark configuration. The main object is expected 
 * to be hidden by the corresponding proxy in case of configurations having a proxy. 
 * 
 * This class must NOT import the implementation class of the dependency component 
 * (e.g. TestComponentMain) if used with a dynamic proxy configuration!
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class BenchmarkReturnInternalObject implements IBenchmarkCase {

	
	public BenchmarkReturnInternalObject() {
		super(); 
	}
	
	private ITestCop tcm;


	public void prepare() { 
		
	}

	
	public long measure() {
		long before = System.currentTimeMillis();
		for(int i=0;i < LOOP_COUNT; i++) {
			this.tcm.getInternalObj();
		}
		long after = System.currentTimeMillis();
		return after - before;
	}

	public String getCaseName() {
		return "wrap returnvalue";
	}


	public void setCallee(Object callee) {
		if (callee instanceof ITestCop) {
			this.tcm = (ITestCop)callee;
		}
	}

}
