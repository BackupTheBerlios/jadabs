/*
 * Copyright (c) 2003-2004, Jadabs project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * - Neither the name of the Jadabs project nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * 
 * Created on Sep 18, 2003
 *
 * $Id: InPacketThread.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.jadabs.jxme.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.concurrent.LinkedQueue;

/**
 * @author andfrei
 *
 */
public class InPacketThread extends Thread {

	private static Logger LOG = Logger.getLogger(InPacketThread.class.getName());

	boolean threadRuns = true;
		
	private LinkedQueue indpQ;
	private MulticastSocket ms;
	
	public InPacketThread(MulticastSocket ms, LinkedQueue indpQ){
		this.ms = ms;
		this.indpQ = indpQ;
	}
		
	public void run(){
			
		while (threadRuns) 
		{
			try 
			{
				byte[] buffer = new byte[8192];
				DatagramPacket dp = new DatagramPacket(buffer, buffer.length);

				ms.receive(dp);

				byte[] data1 = dp.getData();
				
		        if (LOG.isDebugEnabled())
		            LOG.debug("received data");
                
				indpQ.put(data1);
		  		
		  		dp.setLength(buffer.length);
		  		
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
