/*
 * Created on Sep 18, 2003
 * 
 * $Id: OutPacketThread.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.iks.jxme.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

import org.apache.log4j.Logger;

import ch.ethz.iks.concurrent.LinkedQueue;

/**
 * @author andfrei
 *  
 */
public class OutPacketThread extends Thread
{

    private static Logger LOG = Logger.getLogger(OutPacketThread.class);

    boolean threadRuns = true;

    private LinkedQueue outdpQ;

    private MulticastSocket ms;

    // for benchmarking
    int counter=0;
    
    public OutPacketThread(MulticastSocket ms, LinkedQueue outdpQ)
    {
        this.ms = ms;
        this.outdpQ = outdpQ;
    }

    public void run()
    {

        while (threadRuns)
        {

            try
            {
                DatagramPacket dp = (DatagramPacket) outdpQ.take();

                if (LOG.isDebugEnabled())
                        LOG.debug("Data Package sent size: "
                                + dp.getData().length);
               

//                if (Benchmark.prodtest)
                    ms.send(dp);
//                else
//                {
//                    // only for benchmarking purpose
//                    if (++counter == Benchmark.runs)
//                    {
//                        System.out.println("sent all datagrams");
//                        Benchmark.stopTimeAndLog();
//                        Benchmark.closeLog();
//                        Benchmark.signalFinished();
//                        
//                    }
//                }

            } catch (InterruptedException ie)
            {
                LOG.warn("linkedQueue reported an interrupted Exception");
            }
			catch(IOException ioe){
				LOG.warn("could not receive DatagramPacket");
			}

        }

    }

    public void stopThread()
    {

        threadRuns = false;

        //		Thread.currentThread().interrupt();
        //		ms.disconnect();
        //		ms.close();
        //		ms = null;
    }
}