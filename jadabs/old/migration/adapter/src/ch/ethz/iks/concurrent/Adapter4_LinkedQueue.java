package ch.ethz.iks.concurrent;

import ch.ethz.iks.evolution.adapter.DefaultAdapter;
import ch.ethz.iks.proxy.TransparentProxyFactory;

/**
 * adapter for LinkedQueue class implementation as in baselib component.
 * Matches incoming invocations through the transparent proxy of the LinkedQueue
 * to the current implementation of it. This adapter just supports the mapping
 * of kind <code>hash</code> as specified by the <code>-adapt hash</code> command line
 * argument. 
 * => component adapter2_baselib/ for sample evolution
 * 
 * TODO: implement mapping to other methods than put and take; 
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class Adapter4_LinkedQueue extends DefaultAdapter {

	
	public Adapter4_LinkedQueue() {
		super();	
	}
	
	
	// constants representing methods of the proxy and the adaptee class LinkedQueue
	private static final int METHOD_PUT_OBJECT = TransparentProxyFactory.getMethodHash(LinkedQueue.class, "put", new Class[] {Object.class});
	private static final int METHOD_TAKE  = TransparentProxyFactory.getMethodHash(LinkedQueue.class, "take", null);
	
	/**
	 * adapts the invocation on the proxy of a LinkedQueue object to the hidden implementation.
	 * Uses the <code>hash</code> kind signature to match from the proxy method to the method of
	 * the hidden object. 
	 * 
	 * This implementation maps by just forwarding the invocations on the proxy to the associated hidden object
	 */
	protected Object adapt(Object callee, int methodCode, Object [] args) throws Throwable {
		if (methodCode == METHOD_TAKE) {
				
				return ((LinkedQueue)callee).take();
				
		} else if (methodCode == METHOD_PUT_OBJECT) {
			
				((LinkedQueue)callee).put( args[0] );
				
				return null;
		} else {
			throw new NoSuchMethodException("unknown method hash: "+methodCode);
		}
	}

}
