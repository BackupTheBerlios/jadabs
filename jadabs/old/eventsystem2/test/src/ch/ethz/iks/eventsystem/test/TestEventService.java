/*
 * Created on Mar 23, 2004
 *
 */
package ch.ethz.iks.eventsystem.test;

import org.apache.log4j.Logger;

import ch.ethz.iks.benchmark.Benchmark;
import ch.ethz.iks.eventsystem.IEvent;
import ch.ethz.iks.eventsystem.IEventListener;
import ch.ethz.iks.eventsystem.EventService;
import ch.ethz.iks.eventsystem.IFilter;
import ch.ethz.iks.eventsystem.impl.StringEvent;



/**
 * @author andfrei
 * TestEventServiceion$
 */
public class TestEventService implements EventListener
{
    
    private static Logger LOG = Logger.getLogger(TestEventService.class.getName());
    
    EventService eventsvc;
    boolean running = true;
    Filter filter;
    
    Benchmark bm;
    int runcounter = 0;
    
    int threadsleep = 100;
    
    public TestEventService(EventService eventsvc, Benchmark bm){

        this.eventsvc = eventsvc;
        this.bm = bm;
    }

    //---------------------------------------
    // Implements: Runnable
    //---------------------------------------
    public void run()
    {
        // open Benchmark 
        Benchmark.openLog();
        
        int number = 0;
        // do give other threads time to startup
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {   }
        
        System.out.println("start benchmark: ");
//        while (running){


//            StringEvent event = new StringEvent("Stringname", "hello event");
//            event.setMasterPeerName("peer1");
//            event.addAttribute("tcounter",Integer.toString(tcounter));
//            long starttime = System.currentTimeMillis();
//            event.addAttribute("starttime",Long.toString(starttime));

            Benchmark.setStartTime();
            
		        for( int i = 0; i < Benchmark.runs; i++)
		        {
		            StringEvent event = new StringEvent("Stringname", "hello event");
		            long starttime = System.currentTimeMillis();
		            event.addAttribute("starttime",Long.toString(starttime));
                  
		            eventsvc.publish(event);
                    
                    if (Benchmark.prodtest)
                        try
                        {
                            Thread.sleep(threadsleep);
                        } catch (InterruptedException e1)
                        {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

		        }
		        
                // give some time for responses and signal finished if
                // not yet all have been returned or lost
                if (Benchmark.prodtest)
                {
                    try
                    {
                        Thread.sleep(3000);
                    } catch (InterruptedException e1)
                    {
                    }
                    Benchmark.signalFinished();
                }
                
//		        Benchmark.stopTimeAndLog();
            
		        System.out.println("finished run");

        
//        try {
//            Thread.sleep(2*1000);
//        } catch (InterruptedException e) {   }
        
//        bm.closeLog();
        System.out.println("finished benchmark");
    }

    public void stopThread()
    {
        running = false;
    }
    
    //---------------------------------------
    // Implements: IEventListener
    //---------------------------------------
    public void processEvent(Event event) {

        long stoptime = System.currentTimeMillis();
        
        long starttime = Long.parseLong(
                (String)event.getAttributeValue("starttime"));
        
        long diff = stoptime - starttime;
        
        Benchmark.logTime(threadsleep, diff);
        
        if (runcounter++ == Benchmark.runs){
            Benchmark.signalFinished();
            System.out.println("finished testrun");
        }
            
        
//        int threadcount = Integer.parseInt(
//                (String)event.getAttributeValue("tcounter"));
        
//        System.out.println("got event from peer2:"+event.toXMLString());
//        System.out.println("timediff: "+threadcount+" : "+diff);
        

        
        
        
    }
}
