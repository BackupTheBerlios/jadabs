package ch.ethz.iks.jxme.impl;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.adapter.DefaultAdapter;
import ch.ethz.iks.jxme.IMessage;
import ch.ethz.iks.jxme.IMessageListener;
import ch.ethz.iks.proxy.TransparentProxyFactory;

/**
 * adapter for JxmeService class implementation as in jxmesvc component.
 * Matches incoming invocations through the transparent proxy of the JxmeService
 * to the current implementation of it. This adapter just supports the mapping
 * of kind <code>hash</code> as specified by the <code>-adapt hash</code> command line
 * argument. 
 * => component adapter2_jxmesvc/ for sample evolution of escop
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class Adapter4_PeerNetwork extends DefaultAdapter {

	
	public Adapter4_PeerNetwork() {
		super(); 
	}
	
	//	constants representing methods of the proxy and the adaptee class LinkedQueue
	private static final int METHOD_ADDMESSAGELISTENER_IMESSAGELISTENER = TransparentProxyFactory.getMethodHash(PeerNetwork.class, "addMessageListener", new Class[] {IMessageListener.class});
	private static final int METHOD_TOSTRING  = TransparentProxyFactory.getMethodHash(PeerNetwork.class, "toString", null);
	private static final int METHOD_PROCESSMESSAGE_IMESSAGE = TransparentProxyFactory.getMethodHash(PeerNetwork.class, "processMessage", new Class[] {IMessage.class});
	
	private static Logger LOG = Logger.getLogger(Adapter4_PeerNetwork.class);
	
	/**
	 * adapts the invocation on the proxy of a JxmeService object to the hidden implementation.
	 * Uses the <code>hash</code> kind signature to match from the proxy method to the method of
	 * the hidden object. 
	 * 
	 * TODO: finish implementation by adapting methods other than addMessageListener and toString
	 * 
	 * This implementation maps by just forwarding the invocations on the proxy to the associated hidden object
	 */
	protected Object adapt(Object callee, int methodCode, Object [] args) throws Throwable {
		if (methodCode == METHOD_TOSTRING) {
				
				return callee.toString();
				
		} else if (methodCode == METHOD_ADDMESSAGELISTENER_IMESSAGELISTENER) {
				//LOG.info("addMessageListener "+args[0]+" @ "+args[0].getClass().getClassLoader()+" ? isa IMessageListener ? " + (args[0] instanceof IMessageListener) );
				((PeerNetwork)callee).addMessageListener( (IMessageListener) args[0] );
				
				return null;
		} else if (methodCode == METHOD_ADDMESSAGELISTENER_IMESSAGELISTENER) {
			
			((PeerNetwork)callee).processMessage( (IMessage) args[0] );
				
			return null;
		} else {
			throw new NoSuchMethodException("unknown method hash: "+methodCode);
		}
	}

}
