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
 * Created on Jul 31, 2003
 * 
 * $Id: UDPTransport.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.jadabs.jxme.udp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.concurrent.LinkedQueue;
import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.Transport;

/**
 * UDPConnection implements the PeerConnection. By using UDP MulticastSocket
 * DatagramPackets are sent multicast to all listening nodes.
 * 
 * @author andfrei
 */
public class UDPTransport implements Transport
{

    //---------------------------------------------------
    // static fields
    //---------------------------------------------------
    private static Logger LOG = Logger.getLogger(UDPTransport.class.getName());

    private byte TTL = (byte) 1;

    //---------------------------------------------------
    // Instance Fields
    //---------------------------------------------------
    private EndpointAddress endptadr = null;

    private Listener listener = null;

    private InetAddress mcastgroup = null;

    private int mcastport;

    private MulticastSocket mcastsocket = null;

    LinkedQueue indpQ = new LinkedQueue();

    LinkedQueue outdpQ = new LinkedQueue();

    private PnetThread udpPnetThread;

    private InPacketThread inpacketThread;

    private OutPacketThread outpacketThread;

    //---------------------------------------------------
    // Constructor
    //---------------------------------------------------
    public UDPTransport()
    {

    }

    /*
     */
    public void init(EndpointAddress endptadr, Listener listener) throws IOException
    {
        this.endptadr = endptadr;
        this.listener = listener;

        // UDP - Connection
        // check if endptadr is given, then open the multicast socket
        if (endptadr != null)
        {
            // check if endptadr is a valid UDP connection

            // create multicast group
            mcastgroup = InetAddress.getByName(endptadr.getHost());
            mcastport = endptadr.getPort();

            mcastsocket = new MulticastSocket(mcastport);
            mcastsocket.joinGroup(mcastgroup);
            //mcastsocket.setLoopbackMode(true);

            // create now the threads
            udpPnetThread = new PnetThread();
            udpPnetThread.setName("udpPnetThread");
            udpPnetThread.start();

            inpacketThread = new InPacketThread(mcastsocket, indpQ);
            inpacketThread.setName("inpacketThread");
            inpacketThread.start();

            outpacketThread = new OutPacketThread(mcastsocket, outdpQ);
            outpacketThread.setName("outpacketThread");
            outpacketThread.start();
        }

    }

    /*
     */
    public void send(Message message, EndpointAddress destURI) throws IOException
    {
//        byte[] data = getBytes(message);
//
//        DatagramPacket datagram = new DatagramPacket(
//                data, data.length, 
//                InetAddress.getByName(destURI.getHost()),
//                destURI.getPort());
//
//        DatagramSocket s = new DatagramSocket(mcastport);
//        s.send(datagram);
        
        // we just propagate it!
        propagate(message);
    }

    /*
     */
    public void propagate(Message message) throws IOException
    {
        LOG.debug("propagating message ... ");
        
        Element[] elm = message.getElements();
        Element[] elmNew = new Element[elm.length + 1];
        for (int index = 0; index < elm.length; index++) {
            elmNew[index] = elm[index];
        }
        // add srcEA Tag, to know from which node the message has been sent
        elmNew[elm.length] = new Element(
                Message.SRCEA_TAG,
                UDPActivator.peernetwork.getPeer().getName().getBytes(),
                Message.JXTA_NAME_SPACE, null);
        
        byte[] data = getBytes(new Message(elmNew));

        DatagramPacket datagram = new DatagramPacket(data, data.length, mcastgroup, mcastport);
        mcastsocket.send(datagram);
    }

    private byte[] getBytes(Message message) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        message.write(dos);
        dos.close();

        return baos.toByteArray();
    }

    public EndpointAddress getEndpointAddress()
    {
        return endptadr;
    }
    
    /*
     */
    public void stop()
    {
        // stop all local Threads
        udpPnetThread.receiveOK = false;
        udpPnetThread.interrupt();
        udpPnetThread = null;

        inpacketThread.stopThread();
        inpacketThread.interrupt();
        inpacketThread = null;

        outpacketThread.stopThread();
        outpacketThread.interrupt();
        outpacketThread = null;

        // closes the connection and leave the multicast group.
        try
        {
            if (mcastsocket != null)
            {
                mcastsocket.leaveGroup(mcastgroup);
                mcastsocket.close();
                mcastsocket = null;
            }
        } catch (IOException e)
        {
        }
    }

    /**
     * Listens for new incoming Datagram Packets, which have been received by
     * the <code>InPacketThread</code> and put into the <code>indpQ</code>.
     *  
     */
    class PnetThread extends Thread
    {

        private boolean receiveOK = true;

        /**
         * UDPConnection runs as a Thread and receives DatagramPackets.
         */
        public void run()
        {
            String hostname = UDPActivator.peernetwork.getPeer().getName();
            
            while (receiveOK)
            {
                try 
                {
		            byte[] data = (byte[]) indpQ.take();
		            
		            ByteArrayInputStream bais = new ByteArrayInputStream(data);
		            DataInputStream dis = new DataInputStream(bais);
		            Message msg = Message.read(dis);
		            
		            
		            bais.close();
		            dis.close();
		            bais = null;
		            dis = null;

		            LOG.debug("got message: "+msg.toXMLString());
		            
		            Element elm = msg.getElement(Message.SRCEA_TAG);
		            String nameSpace = elm.getNameSpace();
		            if (nameSpace.equals(Message.JXTA_NAME_SPACE))
		            {
		                if (!new String(elm.getData()).equals(hostname))
		                {
		                    // remove first the srcEA from the message
		                    //msg.removeElement(Message.SRCEA_TAG);
		                    
		                    Element[] elms = msg.getElements();
		                    Element[] elmNew = new Element[elms.length - 1];
		                    int i = 0;
		                    for (int index = 0; index < elms.length; index++)
		                    {
		                        if (!elms[index].getName().equals(Message.SRCEA_TAG))
		                            elmNew[i++] = elms[index];
		                    }
		                    
		                    listener.handleMessage(new Message(elmNew), null);
		                }
		            }
		            
                } catch (IOException ioe)
                {
                    LOG.debug("couldn't parse datagram", ioe);
                } catch(InterruptedException ie)
                {
                    LOG.debug("PnetThread interrupted", ie);
                }
            }
        }

    } // end PnetThread
}