/*
 * Created on Apr 27, 2004
 *
 */
package ch.ethz.iks.benchmark;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.ethz.iks.bunit.TestManager;

//import sun.misc.Perf;


/**
 * @author andfrei
 * 
 */
public class Benchmark
{

    public static final String TEST_STANDALONE 			= "standalone";
    public static final String TEST_INSIDE_WO_PROXY     = "inside_wo_proxy";
    public static final String TEST_INSIDE_W_PROXY 	    = "inside_w_proxy";
    public static final String TEST_EXTERN_WO_PROXY     = "extern_wo_proxy";
    public static final String TEST_EXTERN_W_PROXY 	    = "extern_w_proxy";
    public static final String TEST_EVENTSYSTEM_WO_PROXY = "es_wo_proxy";
    public static final String TEST_EVENTSYSTEM_W_PROXY  = "es_w_proxy";
    
    public static final int ONE_RUN 		= 1;	     // 1       
    public static final int SMALL_RUN 	    = 100000;    // 100'000
    public static final int BIG_RUN 		= 10000000;  // 10'000'000
    public static final int HUGE_RUN		= 100000000; // 100'000'000
    
    private static final SimpleDateFormat SDF
    	= new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
   
    private static PrintWriter log;
    
    public static String testname = "undefined";
    public static int testid = 1;
    public static boolean logging = true;
    public static String testtag = "undefined";
    
    public static boolean w_proxy = false;
    public static boolean prodtest = false;
    
    public static int runs = SMALL_RUN;

    
//    static Perf perf = Perf.getPerf();
    public static long starttime;
    public static long hpstime;
    
    // synchronize Benchmark
    public static Object syncObj = new Object();
    public static boolean finished = false;
    
//    Logger LOG = Logger.getLogger("bmsql");
    
    public Benchmark()
    {

    }
    
    public static void signalFinished()
    {
        finished = true;
        synchronized(syncObj)
        {
            syncObj.notify();
        }
    }
    
    public static void setStartTime()
    {
        starttime = System.currentTimeMillis();
        
//        hpstime = perf.highResCounter();       
    }
    
    public static void stopTimeAndLog()
    {
        long stoptime = System.currentTimeMillis();
        long diffm = stoptime - starttime;
        
//        long hpstop = perf.highResCounter();
//        long diff = hpstop - hpstime ;
        
        System.out.println("ms: " + diffm + ", hr: " );
        
        logTime(0, diffm);
    }
    
    public static void logTime(int threadtime, long diff)
    {
        // log in the sql form order, sechema.sql
        // testName, testId, testTag, runs, method, timeDiff
      
        // setup testtag
        if (w_proxy)
            testtag = "w_proxy";
        else
            testtag = "wo_proxy";
        
        // setup testname
        String bmtestname;
        if (prodtest)
            bmtestname = testname.concat("_prod");
        else
            bmtestname = testname.concat("_fw");
        
        if (logging)
        {
	        
	        //String methodname = method.getName();
	        //String methodname = method.toString();
	        
	        StringBuffer sb = new StringBuffer();
	        
	        // testName
	        sb.append("'"+bmtestname+"'");
	        
	        // testId
	        sb.append(",'"+testid+"'");
	        
	        // testtag
	        sb.append(",'"+testtag+"'");
	        
	        // runs
	        sb.append(",'"+ runs+"'");
	        
	        // not threadtime
	        sb.append(",'"+threadtime+"'");
	        
	        // no method
	        sb.append(",''");
	        
	        // timediff
	        sb.append(",'"+diff+"'");
	        
	        
//	        System.out.println(sb.toString());
	        //LOG.info(sb.toString());
	        log2File(sb.toString());
        }
    }
    
    public static void openLog()
    {
//        File logfile = new File("/tmp/dbinsert");
        String logfile = "/tmp/dbinsert";
        try {
          FileOutputStream   fos = new FileOutputStream(logfile, true);
          OutputStreamWriter osw = new OutputStreamWriter(fos);
          BufferedWriter     bw  = new BufferedWriter(osw);
          log = new PrintWriter(bw);
          log.flush();

        } catch (IOException e) {
          System.err.println("Failed to open logfile " + logfile
    			 + " due to: " + e.getMessage());
        }
    }
    
    public static void closeLog(){
        log.flush();
        log.close();
        log = null;
    }
    
    private static void log2File(String str)
    {
        if (logging)
        {
		        String insert = "INSERT INTO methodcalls (" +
		        	"time, testName, testId, testTag, runs, threadtime, method, timeDiff) "+
		        	"VALUES ('"+SDF.format(new Date())+"',"+str+");";
		        
		        log.println(insert);
        }

    }
    
    // timing function, time for how many loops
    static public void runLongFunc(int time)
    {
        int dum = 0;
        for (int i = 0; i < time;i++)
        {
            dum += Math.random();
        }
    }
    
    public void configure()
    {
        // setup the Benchmark-Service
        //int defaultruns = Benchmark.ONE_RUN;
        String strruns = TestManager.bc.getProperty("bm.runs");
        if ( strruns != null)
            runs = Integer.parseInt(strruns);
        
        String prop = TestManager.bc.getProperty("testname");
        if (prop != null)
            testname = prop;
        
        prop = TestManager.bc.getProperty("log");
        if (prop != null)
            logging = true;
        else
            logging = false;
        
        prop = TestManager.bc.getProperty("prodtest");
        if (prop != null)
            prodtest = true;
        
        prop = TestManager.bc.getProperty("w_proxy");
        if (prop != null)
            w_proxy = true;
        else
            w_proxy = false;
        
        // overwrite testtag if specified
        prop = TestManager.bc.getProperty("testtag");
        if (prop != null)
            testtag = prop;
        
        String testId = TestManager.bc.getProperty("testid");
        if (testId != null)
            testid = Integer.parseInt(testId);      
        
    }
}
