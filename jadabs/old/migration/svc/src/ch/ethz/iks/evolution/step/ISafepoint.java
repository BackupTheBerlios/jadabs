package ch.ethz.iks.evolution.step;


/**
 * NOT YET IN USE 
 * Similar to a breakpoint, a Safepoint marks points in the execution flow of
 * a program. A safepoitn defines a point in the execution flow where it is
 * safe to switch from one to the next versiion of code. This heaviliy depends
 * on the semantics of both versions.
 * 
 * Safepoints may easily be implemented using (d)AOP aspects. One must take
 * care that all threads hit a safepoint to be sure none executes while switching
 * the implementation.
 * 
 * These points may be specified in a declarative manner in a textfile and added
 * to the aspect implementing this interface as crosscuts. Further method signatures have to be defined.
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public interface ISafepoint {
	
	//public void addExecutionPoint(Crosscut cc);
	

}
