package ch.ethz.iks.eventsystem.svc;

import java.lang.reflect.Method;

import org.apache.log4j.Logger;

import ch.ethz.iks.eventsystem.IEvent;
import ch.ethz.iks.eventsystem.IEventListener;
import ch.ethz.iks.eventsystem.IEventService;
import ch.ethz.iks.eventsystem.IFilter;
import ch.ethz.iks.evolution.adapter.DefaultAdapter;
import ch.ethz.iks.jadabs.IComponent;
import ch.ethz.iks.proxy.TransparentProxyFactory;

/**
 * Custom adapter for EventServiceImpl class  (v1 or v3 compatible). 
 * Matches incoming invocations through the STABLE transparent proxy of the EventServiceImpl
 * to the (hidden) current implementation of it as well as from the UNSTABLE transparent proxy (depending on the jxmesvc version) 
 * implementing the current version of the eventsystem's IMessageListener interface. This adapter supports the mapping
 * of all kinds <code>hash</code>, <code>reflect</code> <code>name</code> as specified by the <code>-adapt</code> command line
 * argument. 
 * => component adapter2_escop/ for sample evolution of escop
 * 
 * TODO: generate adapters automatically (at least the ones initially used => just method forwarding) 
 *
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class Adapter4_EventServiceImpl extends DefaultAdapter {

	public Adapter4_EventServiceImpl() {
            super();
            //LOG.info("[ ??? ]  Is Hidden IFC? Loaded IFilter from "+IFilter.class.getClassLoader());
    }
    
    private static final Class adaptee = EventServiceImpl.class;

    // constants representing the methods of the proxy and the adaptee object
	private static final int METHOD_PUBLISH_EVT = TransparentProxyFactory.getMethodHash(adaptee, "publish", new Class[] {IEvent.class});
	private static final int METHOD_TOSTRING = TransparentProxyFactory.getMethodHash(adaptee, "toString", null);
	private static final int METHOD_SUBSCRIBE_FILTER_LISTENER = TransparentProxyFactory.getMethodHash(adaptee, "subscribe", new Class [] {IFilter.class, IEventListener.class} );
	private static final int METHOD_EXPORTFILTERS = TransparentProxyFactory.getMethodHash(adaptee, "exportFilters", null );
	private static final int METHOD_GETPEERNAME = TransparentProxyFactory.getMethodHash(adaptee, "getPeerName", null );
	private static final int METHOD_CREATECOMPONENTMAIN = TransparentProxyFactory.getMethodHash(adaptee, IComponent.factoryMethod, null );
	private static final int METHOD_PROCESSEVENT_IEVENT = TransparentProxyFactory.getMethodHash(adaptee, "processEvent", new Class[] { IEvent.class });
	private static final int METHOD_CREATEEVENT_STRING = TransparentProxyFactory.getMethodHash(adaptee, "createEvent", new Class[] { String.class });
	
	
	private static Logger LOG = Logger.getLogger(Adapter4_EventServiceImpl.class);
	
    /**
	 * adapts the invocation on the proxy of a EventServiceImpl object to the hidden implementation.
	 * Uses the <code>hash</code> kind signature to match from the proxy method to the method of
	 * the hidden object. 
	 * 
	 * This implementation maps by just forwarding the invocations on the proxy to the associated hidden object
	 */
	protected Object adapt(Object callee, int methodCode, Object [] args) throws Throwable {
		
		if (methodCode == METHOD_PUBLISH_EVT) {
//			LOG.info("published, arg "+args[0]+" isa IEvent ? "+(args[0] instanceof IEvent));
			((IEventService) callee).publish( (IEvent) args[0]);
			
			return null;
			
		} else if (methodCode == METHOD_TOSTRING) {
			return callee.toString();
			
		} else if (methodCode == METHOD_EXPORTFILTERS) {
			return ((EventServiceImpl)callee).exportFilters();
	
		}  else if (methodCode == METHOD_CREATEEVENT_STRING) {
			return ((EventServiceImpl)callee).createEvent( (String)args[0] );
	
		} else if (methodCode == METHOD_PROCESSEVENT_IEVENT) {
			
			((EventServiceImpl)callee).processEvent( (IEvent) args[0] );
			return null;
	
		} else if (methodCode == METHOD_SUBSCRIBE_FILTER_LISTENER) {
			try {
				IEventListener l = (IEventListener) args[1];
				//LOG.info("isa IEventListener");
				IFilter f = (IFilter) args[0];
				((IEventService) callee).subscribe( f,l);
				return null;
			} catch (ClassCastException c) {
				if (args[1] == null ) {
					LOG.error("2nd arg is null, should be an IEventListener");
					return null;
				}
				Class iEvtList = null;
				Class[] ifcs = args[1].getClass().getInterfaces();
				for (int i = 0; i < ifcs.length; i++) {
					if (ifcs[i].getName().endsWith("IEventListener")) {
						iEvtList = args[i].getClass();
					}
				}
				LOG.error(" ClassCastException occured on subscribe: Listener "+((Object)args[1]).toString()+" implements IEventListener @ "+iEvtList.getClassLoader()+" instead of @ "+IEventListener.class.getClassLoader(),c);
				
			} finally { 
				return null;
			}
	    } else {
			throw new NoSuchMethodException("unknown method hash: "+methodCode);
		}
	}
	
	protected Object adaptStatic(int methodCode, String declaringClassName, Object [] args) throws Throwable {
		if (methodCode == METHOD_GETPEERNAME) {
			//LOG.info("adaptStatic: getPeerName() ");
			return EventServiceImpl.getPeerName();
			
		} if (methodCode == METHOD_CREATECOMPONENTMAIN) {
			//LOG.info("adaptStatic: createComponentMain() ");
			return EventServiceImpl.createComponentMain();
			
		} else {
			throw new NoSuchMethodException("unknown (static) method hash: "+methodCode);
		}	
	}
	
	
	
	protected Object adapt(Object callee, String methodName, Object[] args) throws Throwable {
		if ("publish".equals(methodName) ) {
//					LOG.info("published, arg "+args[0]+" isa IEvent ? "+(args[0] instanceof IEvent));
					((IEventService) callee).publish( (IEvent) args[0]);
			
					return null;
			
				} else if ("toString".equals(methodName) ) {
					return callee.toString();
			
				} else if ("exportFilters".equals(methodName) ) {
					return ((EventServiceImpl)callee).exportFilters();
	
				}  else if ("createEvent".equals(methodName) ) {
					return ((EventServiceImpl)callee).createEvent( (String)args[0] );
	
				} else if ("processEvent".equals(methodName) ) {
			
					((EventServiceImpl)callee).processEvent( (IEvent) args[0] );
					return null;
	
				} else if ("subscribe".equals(methodName) ) {
					
						IEventListener l = (IEventListener) args[1];
						IFilter f = (IFilter) args[0];
						
						((IEventService) callee).subscribe( f,l);
						return null;
					
				} else {
					throw new NoSuchMethodException("unknown method named: "+methodName);
				}
	}
	
	protected Object adaptStatic(Method m, Object[] args) throws Throwable {
			return adaptStatic(m.getName(), m.getDeclaringClass().getName(), args);
	}
	
	
	protected Object adaptStatic(String methodName, String declaringClass, Object[] args) throws Throwable {
		if ( "getPeerName".equals(methodName) ) {
					//LOG.info("adaptStatic: getPeerName() ");
					return EventServiceImpl.getPeerName();
			
				} if (IComponent.factoryMethod.equals(methodName) ) {
					//LOG.info("adaptStatic: createComponentMain() ");
					return EventServiceImpl.createComponentMain();
			
				} else {
					throw new NoSuchMethodException("unknown (static) method named: "+methodName);
				}	
	}
	
	
	protected Object adapt(Object callee, Method m, Object [] args[]) throws Throwable {
		return adapt(callee, m.getName(), args);
	}

}
