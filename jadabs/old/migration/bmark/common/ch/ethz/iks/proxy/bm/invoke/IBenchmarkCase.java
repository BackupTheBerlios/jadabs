package ch.ethz.iks.proxy.bm.invoke;


/**
 * Benchmarks that measure the invocation time of a method may implement this interface.
 * 
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public interface IBenchmarkCase {

	static final int LOOP_COUNT = 1000000;

	void prepare(); // init before benchmark

	long measure(); // return duration of case
	
	String getCaseName(); // identifier
	
	// allow dynamic config to modify callee (replace by their dynamic proxies)
	public void setCallee(Object callee);
}
