/*
 * Created on Sep 18, 2003
 *
 * $Id: InPacketThread.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
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
public class InPacketThread extends Thread {

	private static Logger LOG = Logger.getLogger(InPacketThread.class);

	boolean threadRuns = true;
		
	private LinkedQueue indpQ;
	private MulticastSocket ms;
	
	public InPacketThread(MulticastSocket ms, LinkedQueue indpQ){
		this.ms = ms;
		this.indpQ = indpQ;
	}
		
	public void run(){
			
//		byte[] buffer = new byte[8192];
//		
//		DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
		
		while (threadRuns) {
			try {
				
//				DatagramPacket dp = new DatagramPacket(buffer, buffer.length);

				byte[] buffer = new byte[8192];
				DatagramPacket dp = new DatagramPacket(buffer, buffer.length);

				ms.receive(dp);

				byte[] data1 = dp.getData();
				
        if (LOG.isDebugEnabled())
            LOG.debug("received data");
//				System.out.println("received data");
                
				indpQ.put(data1);
		  		
		  		dp.setLength(buffer.length);
				
//		  		Thread.sleep(1);
		  		
			} catch(InterruptedException ie){
				LOG.warn("linkedQueue reported an interrupted Exception");
			} catch(IOException ioe){
				LOG.warn("could not receive DatagramPacket");
			}
			
		}
				
	}
		
	public void stopThread(){
		
		threadRuns = false;
		
//		Thread.currentThread().interrupt();
//		ms.disconnect();
//		ms.close();
//		ms = null;
	}
}
