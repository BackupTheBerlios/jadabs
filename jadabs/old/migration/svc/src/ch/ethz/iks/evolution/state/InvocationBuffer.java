package ch.ethz.iks.evolution.state;

import java.util.LinkedList;



/**
 * NOT YET IN USE - Planned to buffer pending service requeset during a runtime evolution step
 * A typed FIFO queue container for <code>MethodInvocation<code> objects
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class InvocationBuffer extends LinkedList {
	
	/**
	 * append element at end of buffer
	 * @param call
	 */
	public void put(MethodInvocation call) {
		this.add(call); // append at end of list
	}
	
	/**
	 * remove first element from buffer
	 * @return
	 */
	public MethodInvocation get() {
		return (MethodInvocation) this.removeFirst();
	}

}
