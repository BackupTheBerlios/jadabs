package ch.ethz.iks.proxy.bm;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import ch.ethz.iks.eventsystem.IEvent;
import ch.ethz.iks.eventsystem.IEventService;
import ch.ethz.iks.eventsystem.svc.EventServiceImpl;
import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.IComponentContext;
import ch.ethz.iks.proxy.bm.invoke.IBenchmarkCase;
import ch.ethz.iks.testcop.ITestCop;
import ch.ethz.iks.testcop.ITestListener;
import ch.ethz.iks.testcop.TestComponentMain;


/**
 * Benchmarking configuration for the java dynamic proxy API
 * This configuration does not add evolution support to the components.
 * Be sure to startup the container with the <code>-adapt reflect</code>
 * command line arguments. 
 * This class is the main class of => component bmDynamicProxy
 * 
 * Note: benchmarks components that comply to the coding conventions only!
 * (as testcop.jar), but NOT escop.jar
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class DynamicProxyConfig extends ABenchmarkConfig {

	/** 
	 * Convention for instantiation of main classes of (service) components.
	 * The (default) constructor should be declared private to allow singleton components.
	 * @see ch.ethz.iks.cop.ComponentResource#initServiceComponent
	 * @return an instance of this class
	 */
	public static DynamicProxyConfig createComponentMain() {
		return new DynamicProxyConfig(); // non-singleton
	}
	// accessing original object!!!
	private IEventService es = EventServiceImpl.createComponentMain(); // needs to be typed as an interface
	private ITestCop tc = TestComponentMain.createComponentMain();
	
	public void prepareCase(IBenchmarkCase bmark) {
		if (null != ComponentRepository.Instance().getComponentResourceByClassname("ch.ethz.iks.evolution.mgr.ComponentEvolutionMain")) {
			System.out.println("[ERROR] do NOT include  evolution framework in pcoprep (evolution.jar) when benchmarking "+bmName);
			System.exit(-1);
		}
		
		InvocationHandler esHandler = new InvocationHandler() {
			private IEventService hidden = es;
			
			public Object invoke(Object callee, Method invoked, Object[] args) {
				String name = invoked.getName();
				if ("toString".equals( name) ) {
					return hidden.toString();
				} else if ("publish".equals( name ) ) {
					this.hidden.publish( (IEvent) args[0] );
					return null;
				} else if ("createEvent".equals( name ) ) {
					// does NOT wrap event on return!!!
					return this.hidden.createEvent( (String) args[0] );
				} else throw new RuntimeException("no such method "+name); // todo : add support for tcm
			}
		}; 
		IEventService p4es = (IEventService) Proxy.newProxyInstance(this.es.getClass().getClassLoader(),new Class[] {IEventService.class}, esHandler);
		//this.es = p4es; // invoke the dynamic proxy
		
		bmark.setCallee(p4es);
		
		
		
		final IEvent evt = this.es.createEvent("hello world"); // invoke on hidden es !!!
		InvocationHandler evtHandler = new InvocationHandler() {
					private IEvent hidden = evt;
			
					public Object invoke(Object callee, Method invoked, Object[] args) {
						String name = invoked.getName();
						if ("toString".equals( name) ) {
							return hidden.toString();
						}  else if ("toXMLString".equals( name ) ) {
							// does NOT wrap event on return!!!
							return this.hidden.toXMLString();
						} else throw new RuntimeException("no such method "+name); // todo : add support for tcm
					}
				}; 
		IEvent p4evt = (IEvent) Proxy.newProxyInstance(evt.getClass().getClassLoader(),new Class[] {IEvent.class}, evtHandler);
		
		bmark.setCallee(p4evt);
		
		
		
		InvocationHandler tcHandler = new InvocationHandler() {
					private ITestCop hidden = tc;
			
					public Object invoke(Object callee, Method invoked, Object[] args) {
						String name = invoked.getName();
						if ("nop".equals( name) ) {
							hidden.nop();
							return null;
						} else if ("getInternalObj".equals( name ) ) {
							return this.hidden.getInternalObj();
						} else if ("subscr".equals( name )) {
							hidden.subscr( (ITestListener)args[0] );
							return null;
						} else if ("toString".equals( name ) ) {
							return hidden.toString();
						} else throw new RuntimeException("no such method "+name); // todo : add support for tcm
					}
				}; 
				ITestCop p4tc = (ITestCop) Proxy.newProxyInstance(this.tc.getClass().getClassLoader(),new Class[] {ITestCop.class}, tcHandler);
				//this.tc = p4tc; // invoke the dynamic proxy
		
		bmark.setCallee(p4tc);
		
		
		
		bmark.prepare();
    }
	
	public void init(IComponentContext ctx) {
		this.bmName = "java dynamic proxy API";
		super.initComponent();
	}

}
