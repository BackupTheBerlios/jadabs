/*
 * Created on May 5, 2004
 *
 */
package ch.ethz.iks.eventsystem.test;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;

import ch.ethz.iks.benchmark.Benchmark;
import ch.ethz.iks.bunit.TestCaseTuple;
import ch.ethz.iks.bunit.TestService;
import ch.ethz.iks.bunit.TestServices;
import ch.ethz.iks.eventsystem.EventService;
import ch.ethz.iks.eventsystem.IFilter;
import ch.ethz.iks.eventsystem.impl.FilterImpl;
import ch.ethz.iks.eventsystem.impl.StringEvent;


/**
 * @author andfrei
 * 
 */
public class Activator extends  TestService implements BundleActivator
{
    public static BundleContext bc;
    
    private Benchmark bm;
    
    private static String servicename = "EventSystemTest";
    
    EventService eventservice;
    Filter filter;
    TestEventService testeventsvc;
    
    public String getServiceName()
    {
        return servicename;
    }
    
    /*
     */
    public void start(BundleContext bc) throws Exception
    {
     
        Activator.bc = bc;
        
        // init TestCases
        initTestCases();
        
        // register the TestService in general
        Hashtable props = new Hashtable();
        props.put(TestService.PROP_SERVICE_NAME, servicename);
        bc.registerService(TestService.class.getName(), this, props);
        
        // register this TestService for the TestManager
        ServiceReference srtests = bc.getServiceReference(
                TestServices.class.getName());
        TestServices testsvcs = (TestServices)bc.getService(srtests);
        
        testsvcs.registerTestService(this);
 
    }
    
    private void initTestCases()
    {
        TestCaseTuple testcase = new TestCaseTuple(
                "testPublish", servicename, "EventSystem");
        testcases.add(testcase);
    }

    /*
     */
    public void stop(BundleContext context) throws Exception
    {
        
    }

    //---------------------------------------------------
    // TestCases with setup, teardown
    //---------------------------------------------------
    
    Vector bundledeps = new Vector();
    
    public void setupEventSystem(TestCaseTuple tuple) throws BundleException
    {
        setup();
    }
    
    public void teardownEventSystem(TestCaseTuple tuple)
    {
       teardown();
    }
    
    public void testPublish(TestCaseTuple tuple)
    {
        System.out.println("starting TestEventService... ");
        
        // Import needed services
        ServiceReference sref = bc.getServiceReference(EventService.class.getName());
        if (sref != null)
            System.out.println("got eventservice: ");
        else
            System.out.println("evenservice is null!");
        
        eventservice = (EventService)bc.getService(sref);
        
        String peername = bc.getProperty("jxme.peername");
        
        // get BenchmarkService
        ServiceReference srbm = bc.getServiceReference(Benchmark.class.getName());
        bm = (Benchmark)bc.getService(srbm);
        
        
        // Create, Start, Exported Services
        testeventsvc = new TestEventService(eventservice, bm);

        // start
        if (peername.equals("peer1"))
        {
            StringEvent event = new StringEvent();
            event.setMasterPeerName("peer2");
            filter = new FilterImpl(event);
            eventservice.subscribe(filter, testeventsvc);
            
            // not a thread anymore
            testeventsvc.run();    
        }
        else
        {
            EchoEventListener echolistener = new EchoEventListener(eventservice);
            
            StringEvent event = new StringEvent();
//            event.setSlavePeerName("peer1");
            filter = new FilterImpl(event);
            eventservice.subscribe(filter, echolistener);
            
            // second event-type
//          TestEvent tevent = new TestEvent();
//          FilterImpl filter2 = new FilterImpl(tevent);
//          eventsvc.subscribe(filter2, new TestEventListener());
        }
    }
    
    
    //---------------------------------------------------
    // setup, teardown generics
    //---------------------------------------------------
    
    String[] requiredBundles = new String[] {
    		"JXME UDP",
			"EventSystem Svc"};
    
    private void setup() throws BundleException
    {
    	Bundle[] bundles = bc.getBundles();
    	
        for( int i = 0; i < requiredBundles.length; i++)
        {
            for (int bi = 0; bi < bundles.length; bi++)
            {
            	String bundlename = (String)bundles[bi].getHeaders().
					get(Constants.BUNDLE_NAME);
            	if (bundlename != null && bundlename.equals(requiredBundles[i]))
				{
					bundles[bi].start();
					bundledeps.add(bundles[bi]);
				}
            }
        }
        
    }
    
    private void teardown()
    {
        
        synchronized(Benchmark.syncObj)
        {
            try
            {
                while(!Benchmark.finished)
                    Benchmark.syncObj.wait();
                
            } catch (InterruptedException e)
            {
                System.out.println("interrupted Benchmark.syncObj");
            }
        }
        
        // stop service
        testeventsvc.stopThread();
        eventservice.unsubscribe(filter);
        
        // unregister all bundles
        for(Enumeration enb = bundledeps.elements(); enb.hasMoreElements();)
        {
            Bundle bundle = (Bundle)enb.nextElement();
            
            try
            {
                bundle.stop();
//                bundle.uninstall();
            } catch (BundleException e)
            {
                System.out.println("Could not stop bundle: "+ bundle.getBundleId());
            }
            
            
        }
        
    }
}
