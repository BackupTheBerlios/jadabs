package ch.ethz.iks.proxy.bm.invoke;

import ch.ethz.iks.testcop.ITestCop;
import ch.ethz.iks.testcop.ITestListener;



/**
 * Benchmarks the performance of calls to <code>ITestCop.nop</code>.
 * The method is invoked on the main object of the testcop component 
 * (must be included in the dependencies of the benchmark configuration component)
 * This case may be used with any benchmark configuration. The main object is expected 
 * to be hidden by the corresponding proxy in case of configurations having a proxy. 
 * This class must NOT import the implementation class of the dependency component 
 * (e.g. TestComponentMain) if used with a dynamic proxy configuration!
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class BenchmarkExternalArgumentInvocation implements IBenchmarkCase, ITestListener {

	
	public BenchmarkExternalArgumentInvocation() {
		super(); 
	}
	
	private ITestCop tcm;

	
	public void prepare() { 
		
	}

	
	public long measure() {
		long before = System.currentTimeMillis();
		for(int i=0;i < LOOP_COUNT; i++) {
			this.tcm.subscr( (ITestListener) this);
		}
		long after = System.currentTimeMillis();
		return after - before; 
	}


	public String getCaseName() {
		return "wrap argument";
	}


	/* (non-Javadoc)
	 * @see ch.ethz.iks.testcop.ITestListener#react(ch.ethz.iks.testcop.ITestCop)
	 */
	public void react(ITestCop tc) {
		
	}


	
	public void setCallee(Object callee) {
		if (callee instanceof ITestCop) {
			this.tcm = (ITestCop)callee;
		}
	}

}
