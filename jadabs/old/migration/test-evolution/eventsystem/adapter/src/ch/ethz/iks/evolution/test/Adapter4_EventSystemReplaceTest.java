package ch.ethz.iks.evolution.test;

import org.apache.log4j.Logger;

import ch.ethz.iks.eventsystem.IEvent;
import ch.ethz.iks.eventsystem.IEventListener;
import ch.ethz.iks.evolution.adapter.DefaultAdapter;
import ch.ethz.iks.proxy.TransparentProxyFactory;


/**
 * adapter for EventSystemReplaceTest class implementation as in esupgradetest component.
 * Matches incoming invocations through the STABLE transparent proxy of the EventSystemReplaceTest
 * to the current implementation of it as well as from the UNSTABLE transparent proxy (depending on the escop version) 
 * implementing the current version of the eventsystem's IEventListener interface.
 * This adapter just supports the mapping of kind <code>hash</code> as specified by the 
 * <code>-adapt hash</code> command line argument. 
 * => component adapter2_esupgradetest/ for testing the sample evolution of escop
 * (replaced Class-Path: proxy4_escop.jar)
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class Adapter4_EventSystemReplaceTest extends DefaultAdapter  {

	private static Logger LOG = Logger.getLogger(Adapter4_EventSystemReplaceTest.class);
	
	private static int METHOD_PROCESSEVENT_IEVENT = TransparentProxyFactory.getMethodHash(EventSystemReplaceTest.class, "processEvent", new Class[] {IEvent.class});
	private static int METHOD_TOSTRING = TransparentProxyFactory.getMethodHash(EventSystemReplaceTest.class, "toString",null);
	
	
	public Adapter4_EventSystemReplaceTest() {
		super();
	}
	
	/**
	 * adapts the invocation on the proxy of a EventSystemReplaceTest object to the hidden implementation.
	 * Uses the <code>hash</code> kind signature to match from the proxy method to the method of
	 * the hidden object. 
	 * 
	 * TODO: finish implementation by adapting methods other than processEvent and toString
	 * 
	 * This implementation maps by just forwarding the invocations on the proxy to the associated hidden object
	 */
	public final synchronized Object adapt(Object callee, int methodCode, Object[] args) throws Throwable {
		if ( methodCode == METHOD_PROCESSEVENT_IEVENT) {
			//LOG.info("processEvent: IEventListener @ "+IEventListener.class.getClassLoader());
			//LOG.info("processEvent: IEvent @ "        +IEvent.class.getClassLoader());
			//LOG.info("processEvent: args[0] = "+args[0]+" @ "+args[0].getClass().getClassLoader());
			// arg must be wrapped in a IProxy, IEventListener and IEvent must be laded from proxy4_escop
			((IEventListener)callee).processEvent( (IEvent) args[0]);
			return null;
		} else if (methodCode == METHOD_TOSTRING) {
			return callee.toString();
		} else {
			throw new NoSuchMethodException(" hash = "+methodCode);
		}
	}
	
	

}
