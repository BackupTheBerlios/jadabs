/*
 * Created on May 7, 2004
 *
 */
package ch.ethz.iks.bunit;

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import ch.ethz.iks.benchmark.Benchmark;


/**
 * @author andfrei
 * 
 */
public class TestManager implements BundleActivator, TestServices
{
    
    public static final String PROP_RANGE_FROM  = "rangefrom";
    public static final String PROP_RANGE_TO    = "rangeto";
    public static final String PROP_RANGE_INC   = "rangeinc";
    public static final String PROP_TC_RUNS     = "tcruns";
    
    
    public static BundleContext bc;
    
    private boolean running = true;
    
    private LinkedList testservices = new LinkedList();
    
    
    private Benchmark bm;
    private int rangefrom = 0;
    private int rangeto = 0;
    private int rangeinc = 1;
    private int tcruns = 1;
    
    /* 
     */
    public void start(BundleContext bc) throws Exception
    {
        System.out.println("started benchmarking...");
        
        TestManager.bc = bc;
        configure();
        
        bc.registerService(TestServices.class.getName(), this, null);
        
        // register Benchmark
        bm = new Benchmark();
        bm.configure();
        bc.registerService(Benchmark.class.getName(), bm, null);
        
        runTests();
    }

    /* 
     */
    public void stop(BundleContext context) throws Exception
    {
        System.out.println("stopped benchmarking.");
        
    }
    
    //---------------------------------------------------
    // Implements: TestServices
    //---------------------------------------------------
    
    public void registerTestService(TestService testsvc)
    {
        synchronized(testservices){
            testservices.add(testsvc);
            testservices.notify();
        }
    }
    
    //---------------------------------------------------
    // TestManager methods
    //---------------------------------------------------
    
    public void configure()
    {
        
        String prop = bc.getProperty(PROP_RANGE_FROM);
        if (prop != null)
            rangefrom = Integer.parseInt(prop);
        
        prop = bc.getProperty(PROP_RANGE_TO);
        if (prop != null)
            rangeto = Integer.parseInt(prop);
        
        prop = bc.getProperty(PROP_RANGE_INC);
        if (prop != null)
            rangeinc = Integer.parseInt(prop);
        
        prop = bc.getProperty(PROP_TC_RUNS);
        if (prop != null)
            tcruns = Integer.parseInt(prop);
        
    }
    
    public void setupDependency(TestCaseTuple tuple) throws TestException
    {
       if (tuple.deptestcase != null)
       {
           String strfilter = "("+TestService.PROP_SERVICE_NAME+"="+tuple.deptestservice+")";
           
           try {
               ServiceReference[] sref = bc.getServiceReferences(null, strfilter);
               if (sref.length == 1)
               {
                   TestService testsvc = (TestService)bc.getService(sref[0]);
                   testsvc.invokeSetupTeardown("setup"+tuple.deptestcase, tuple);
               }
                else
                   throw new TestException("could not find Service, or more than one: "+tuple.deptestservice);
               
           } catch (InvalidSyntaxException ise)
           {
               throw new TestException("invalid filter", ise);
           }
       }
    }
    
    public void teardownDependency(TestCaseTuple tuple) throws TestException
    {
        if (tuple.deptestcase != null)
        {
            String strfilter = "("+TestService.PROP_SERVICE_NAME+"="+tuple.deptestservice+")";
            
            try {
               ServiceReference[] sref = bc.getServiceReferences(null, strfilter);
               if (sref.length == 1)
               {
                   TestService testsvc = (TestService)bc.getService(sref[0]);
                   testsvc.invokeSetupTeardown("teardown"+tuple.deptestcase, tuple);
               }
                else
                   throw new TestException("could not find Service, or more than one: "+tuple.deptestservice);
               
            } catch (InvalidSyntaxException ise)
            {
                throw new TestException("invalid filter", ise);
            }
        }
    }
    
    public void callTestCase(TestService testsvc, TestCaseTuple tuple) throws TestException
    {
        testsvc.invoke(tuple.testcase, tuple);
    }
    
    public void runTests()
    {

        new Thread("TestManager"){
           
            public void run()
            {
                System.out.println("Start TestManager...");
                int count = 0;
                
                while (running)
                {
                    try {
                        TestService testsvc;
                        synchronized(testservices){
                            // wait for new testservice when empty
                            while (testservices.isEmpty())
                                testservices.wait();
                            
                            testsvc = (TestService)testservices.removeFirst();
                        }
                        
                        Enumeration encases = testsvc.getTestCases();
                        String logstr = "do tests from: "+testsvc.getClass();
//                      LOG.info(logstr);
                        System.out.println(logstr);
                        
                        for(; encases.hasMoreElements();)
                        {
                            TestCaseTuple tuple = (TestCaseTuple)encases.nextElement();
                            System.out.println("do testcase: " + tuple.testcase);
                            
                            // do frameworktest, productive test
                            if (Benchmark.prodtest)
                            {
                                
                                dotestrun(testsvc, tuple);
                            }
                            else
                            {
                                // do with and without proxy
                                for( int w = 0; w<2; w++)
                                {
                                    Benchmark.w_proxy = !Benchmark.w_proxy;
                                    dotestrun(testsvc, tuple);

                                }
                            }
                        }
                        
                    } catch(NoSuchElementException nse){
                        try
                        {
                            if (count++ < 3)
                                Thread.sleep(3000);
                            else
                                running = false;
                        } catch (InterruptedException e){  }
                    } catch (InterruptedException ie)
                    {
                        // TODO Auto-generated catch block
                        //ie.printStackTrace();
                        System.out.println("TestManager Trhead interrupted!");
                    }
                }
                
                String logstr = "Finished tests from TestManager."; 
    //            LOG.info(logstr);
                System.out.println(logstr);
            }
            
            void dotestrun(TestService testsvc, TestCaseTuple tuple)
            {
                try {
                    // do more testruns
                    for (int tr = 0; tr < tcruns; tr++){
                        Benchmark.finished = false;
                        int r = rangefrom;
                        if (tuple.ranged)
                        {
                            //  loop over a range
                            for (; r <= rangeto; ){
                                Benchmark.finished = false;
                                r *= rangeinc;
                                Benchmark.runs = r;
    //                                          tuple.threadtime = Integer.parseInt(
    //                                                  context.getProperty("bm.time"));
                                    tuple.crtrange = r;
                                    
                                    setupDependency(tuple);
                                    callTestCase(testsvc, tuple);
                                    teardownDependency(tuple);
                            }
                        }
                        else // do it only once
                        {
                            setupDependency(tuple);
                                callTestCase(testsvc, tuple);
                                teardownDependency(tuple);
                        }
                    }
                } catch(TestException te)
                {
                    te.printStackTrace();
                }
            
            }
            
        }.start();
        
    }
    
}
