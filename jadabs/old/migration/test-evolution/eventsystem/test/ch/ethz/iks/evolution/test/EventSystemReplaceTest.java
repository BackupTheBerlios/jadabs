package ch.ethz.iks.evolution.test;



import org.apache.log4j.Logger;

import ch.ethz.iks.eventsystem.IEvent;
import ch.ethz.iks.eventsystem.IEventListener;
import ch.ethz.iks.eventsystem.IFilter;
import ch.ethz.iks.eventsystem.svc.EventServiceImpl;
import ch.ethz.iks.eventsystem.svc.FilterImpl;
import ch.ethz.iks.eventsystem.svc.StringEvent;
import ch.ethz.iks.jadabs.ComponentRepository;
import ch.ethz.iks.jadabs.ComponentResource;
import ch.ethz.iks.jadabs.IComponentResource;
import ch.ethz.iks.proxy.IProxy;
import ch.ethz.iks.proxy.cop.ProxyComponentResourceFactory;


/**
 * Example upgrade of eventsystem component escop.jar from version 1 to 3
 * Use as testcase to verify upgrade success. Supports dyanamic as well as transparent proxy upgrade
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class EventSystemReplaceTest extends RuntimeMigrationTest implements IEventListener {

	
	private static final String codebase =  "esEvolutionTest.jar";
	private static final String targetCop =  "eventsystem.jar";
	
	
	public static void main(String[]args) {
		EventSystemReplaceTest  test = EventSystemReplaceTest.createComponentMain();
		try {
			test.setUp();
			test.runTest();
			test.tearDown();
		} catch (Throwable t) {
			LOG.error("[FAILED]   ",t);
		}
	}
	
	public static EventSystemReplaceTest createComponentMain() {
		return new EventSystemReplaceTest();
	}
	
	public String getCodeBase() {
		return codebase;
	}
	
	public String getPrefixOfNewVersion() {
		return "V3_";
	}
 
	private static Logger LOG = Logger.getLogger(EventSystemReplaceTest.class);

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jadabs.evolution.ComponentEvolutionTest#testOnlineUpgrade()
	 */
	public void testOnlineUpgrade() {
		IComponentResource es = ComponentRepository.Instance().getComponentResourceByCodebase(targetCop);
		try {
			this.evolveComponent( es ); //old Cop must have been started already
		} catch (Exception e) {
			LOG.error("caught "+e,e);
		}
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jadabs.evolution.ComponentEvolutionTest#useOldVersion(ch.ethz.iks.evolvable.cop.UpgradeableComponentResource)
	 */
	public Object useOldVersion(ComponentResource testCop) throws Exception {
		IProxy proxy = this.useVersion(true);
		LOG.info(  "[  OK  ]   useOldVersion: eventsystem should be a proxy object");
		IComponentResource cop = ProxyComponentResourceFactory.getResourceOfProxy(proxy.getClass().getName()).getOriginalComponent();
		assertTrue("wrong version "+ cop.getVersion()+", should be 1", cop.getVersion() == 1);
		return (IProxy)proxy;
	}
	
	// just for transparent proxy
	private IProxy useVersion(boolean doSubscribe) throws Exception {
		LOG.info("[ INFO ]   EventServiceImpl class was loaded from "+EventServiceImpl.class.getClassLoader());
		Object main = EventServiceImpl.createComponentMain(); 
		LOG.info("[ INFO ]   createComponentMain returned an object loaded from "+main.getClass().getClassLoader());
		EventServiceImpl es = (EventServiceImpl) main;
		assertTrue("[FAILED]   eventsystem should be a proxy object", es instanceof IProxy);
		
		// modify state (e.g. Filters) of old, check if state has been copied to new
		LOG.info(" peerName = "+EventServiceImpl.getPeerName());
		
		StringEvent evt = new StringEvent("hello","world");
		assertTrue("evt is null ",evt != null);
		assertTrue("[FAILED]   event should be a proxy object", evt instanceof IProxy);
		assertTrue("[FAILED]   event should have initialized its hashvalue ",((IProxy)evt).hash() != 0);
		
		IFilter filter = new FilterImpl(evt);
		if (doSubscribe) {
			es.subscribe(filter, this);
		}
		String [] filters = (String[]) es.exportFilters();
		String allFilters = new String();
		if (filters != null) {
			for (int i = 0; i < filters.length; i++) {
				allFilters += filters[i] +"\n";
			}
		}
		LOG.info(" exported Filters: \n"+ allFilters );
		
		evt.setMasterPeerName("test");
		LOG.info(" toString not overridden in subscriber proxy. TODO: test publishing");
		//es.publish(evt); TODO: check if publish succeeds in new design
		
		return (IProxy) es;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jadabs.evolution.ComponentEvolutionTest#useNewVersion(ch.ethz.iks.cop.IComponentResource)
	 */
	public Object useNewVersion(ComponentResource testCop) throws Exception {
		IProxy proxy = this.useVersion(false); 
		LOG.info(  "[  OK  ]   useNewVersion: eventsystem should be a proxy object");
		IComponentResource cop = ProxyComponentResourceFactory.getResourceOfProxy(proxy.getClass().getName()).getOriginalComponent();
		assertTrue("wrong version "+ cop.getVersion()+", should be 3", cop.getVersion() == 3);
		return (IProxy)proxy;
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponent#disposeComponent()
	 */
	public void disposeComponent() {
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.eventsystem.IEventListener#processEvent(ch.ethz.iks.eventsystem.IEvent)
	 */
	public void processEvent(IEvent event) {
		LOG.info("received "+event.toXMLString());
	}
	
	public String toString() {
		return "[plain esTest for "+targetCop+"] "+super.toString();
	}
	
	protected void runTest() {
		//super.runTest();
		testOnlineUpgrade();
	}

}
