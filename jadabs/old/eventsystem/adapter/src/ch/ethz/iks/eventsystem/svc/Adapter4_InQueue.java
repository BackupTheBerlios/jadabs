package ch.ethz.iks.eventsystem.svc;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.adapter.DefaultAdapter;
import ch.ethz.iks.jxme.IMessage;
import ch.ethz.iks.proxy.TransparentProxyFactory;

/**
 * Custom adapter for InQueue (v1 or v3 compatible). 
 * Matches incoming invocations through the STABLE transparent proxy of the InQueue
 * to the (hidden) current implementation of it as well as from the UNSTABLE transparent proxy (depending on the jxmesvc version) 
 * implementing the current version of the eventsystem's IMessageListener interface. This adapter just supports the mapping
 * of kind <code>hash</code> as specified by the <code>-adapt hash</code> command line
 * argument. 
 * => component adapter2_escop/ for sample evolution of escop
 * 
 * TODO: generate adapters automatically (at least the ones initially used => just method forwarding) 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class Adapter4_InQueue extends DefaultAdapter {

	public Adapter4_InQueue() {
            super();
    }
 
	private static final Class adaptee = InQueue.class;
    
	// constants representing the methods of the proxy and the adaptee object   
	private static final int METHOD_PROCESS_ENTRY = TransparentProxyFactory.getMethodHash(InQueue.class, "processEntry", new Class[] {Object.class});
	private static final int METHOD_TOSTRING = TransparentProxyFactory.getMethodHash(InQueue.class, "toString", null);
	private static final int METHOD_PROCESS_MESSAGE = TransparentProxyFactory.getMethodHash(InQueue.class, "processMessage", new Class[] {IMessage.class});

	
	private static Logger LOG = Logger.getLogger(Adapter4_InQueue.class);
	
	/**
	 * adapts the invocation on the proxy of a InQueue object to the hidden implementation.
	 * Uses the <code>hash</code> kind signature to match from the proxy method to the method of
	 * the hidden object. 
	 * 
	 * TODO: finish implementation by adapting methods other than processEntry, processMessage, toString
	 * 
	 * This implementation maps by just forwarding the invocations on the proxy to the associated hidden object
	 */
	protected Object adapt(Object callee, int methodCode, Object [] args) throws Throwable {
		
		if (methodCode == METHOD_PROCESS_ENTRY) {
			((InQueue) callee).processEntry( args[0]);
			return null;
			
		} else if (methodCode == METHOD_TOSTRING) {
			return callee.toString();
			
		} else if (methodCode == METHOD_PROCESS_MESSAGE) {
			
			((InQueue)callee).processMessage( (IMessage)args[0] );
			return null;
			
	    } else {
			throw new NoSuchMethodException("unknown method hash: "+methodCode);
		}
	}
	
	

}
