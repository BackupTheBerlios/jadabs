/************************************************************************
 *
 * $Id: TCPTransport.java,v 1.2 2005/02/18 15:01:36 printcap Exp $
 *
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *       Sun Microsystems, Inc. for Project JXTA."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact Project JXTA at http://www.jxta.org.
 *
 * 5. Products derived from this software may not be called "JXTA",
 *    nor may "JXTA" appear in their name, without prior written
 *    permission of Sun.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see
 * <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache
 * Foundation.
 **********************************************************************/
package ch.ethz.jadabs.jxme.tcp;


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.Transport;

/**
 * Provides a TCP messaging service for JXTA peers. Message receiving is based
 * on establishing a relationship with a JXTA Network.
 */
public final class TCPTransport implements Transport
{

    private static final Logger LOG = Logger.getLogger(TCPTransport.class.getName());

    private ServerSocket ss = null;

    private Thread acceptThread = null;

    private Vector pollThreads = new Vector();

    private Session session = null;

    private EndpointAddress myUri;

    private Listener listener;

    /**
     * Constructor for TCP Transport. Creates server socket and starts listening
     * for incoming connections.
     * 
     * @param myUri
     *            URI of this peer
     * @param listener
     *            object for incoming message handling.
     */
    public TCPTransport()
    {
    }

    public void init(EndpointAddress myUri, Listener listener) throws IOException
    {
        this.myUri = myUri;
        this.listener = listener;

        // TCP - Connection
        // check if myURI is given, then open a server socket
        if (myUri != null)
        {
            ss = new ServerSocket(myUri.getPort());

            acceptThread = new Thread(new Acceptor(ss, this));
            acceptThread.setDaemon(true);
            acceptThread.start();
            LOG.info("Accepting connections on port " + ss.getLocalPort());
        }

    }

    private static final class Acceptor implements Runnable
    {

        private ServerSocket ss = null;

        private TCPTransport tcpListener;

        /**
         * Constructor for Acceptor threads.
         * 
         * @param ss
         *            ServerSocket to listen on.
         * @param tcpListener
         *            incoming message handler.
         */
        Acceptor(ServerSocket ss, TCPTransport tcpListener)
        {
            this.ss = ss;
            this.tcpListener = tcpListener;
        }

        public void run()
        {
            Thread.currentThread().setName("TCP Acceptor " + ss.toString());
            String clientDescription = null;
            while (!Thread.interrupted())
            {
                Session incomingSession = null;
                try
                {
                    Socket client = ss.accept();
                    clientDescription = client.toString();
                    LOG.info("Accepted connection from " + clientDescription);

                    incomingSession = Session.createSession(client);
                    Thread t = new Thread(new Poller(incomingSession, tcpListener), 
                            "TCP Session to " + clientDescription);
                    tcpListener.pollThreads.addElement(t);
                    t.setDaemon(true);
                    t.start();
                } catch (IOException ex)
                {
                    LOG.warn("Error starting session", ex);
                    continue;
                }
            }
        }
    }

    private static final class Poller implements Runnable
    {

        private Session incomingSession = null;

        private TCPTransport listener = null;

        public Poller(Session incomingSession, TCPTransport listener)
        {
            this.incomingSession = incomingSession;
            this.listener = listener;
        }

        public void run()
        {
            try
            {

                for (Message msg = incomingSession.recv(); 
                		msg != null && !Thread.interrupted(); 
                		msg = incomingSession.recv())
                {
                    if (listener != null)
                    {
                        listener.handleMessage(msg);
                    }
                }
            } catch (IOException ex)
            {
                LOG.debug("Exception on " + incomingSession, ex);
                try
                {
                    incomingSession.close();
                } catch (IOException e)
                {
                    e.printStackTrace();
                    incomingSession = null;
                }
            }
        }
    }

    void handleMessage(Message msg)
    {
        LOG.info("msg: " + msg);
        listener.handleMessage(msg, null);
    }

    /**
     * Connectes to seed peer and establishes a session.
     * 
     * @param seedUri
     *            URI of seed peer.
     */
    public void connect(EndpointAddress seedUri) throws IOException
    {
        LOG.info("seeduri: " + seedUri.getHost() + ":" + seedUri.getPort());

        Socket socket = new Socket(seedUri.getHost(), seedUri.getPort());

        OutputStream os = socket.getOutputStream();

        LOG.info("socket.getLocalPort() = " + socket.getLocalPort());
        LOG.info("socket.getPort() = " + socket.getPort());
        Session.createSession(socket);
        LOG.info("Connected with: " + seedUri);
    }

    public void send(Message outgoing, EndpointAddress destPeer) throws IOException
    {
        if (destPeer == null)
        {
            throw new IOException("destPeer must not be null");
        }
        
        LOG.info("sending Message : " + outgoing + " to: " + destPeer);
        Session session = Session.getSession(destPeer);
        
        if (session != null)
        {
	        try
	        {
	            session.send(outgoing);
	        } catch (SocketException se)
	        {
	            // may be previous session from the same peer is still lingering.
	            // close and re-create a session.
	            session.close();
	            session = Session.getSession(destPeer);
	            session.send(outgoing);
	        }
        }
        else
            throw new IOException("no connected session");
    }

    /**
     * Propagate Message to all available connections.
     */
    public void propagate(Message message) throws IOException
    {
        Session.propagate(message);
    }

    public void process(DatagramPacket incoming) throws IOException
    {

        byte[] data = incoming.getData();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        Message msg = Message.read(dis);
        handleMessage(msg);
    }

    public String getProtocol()
    {
        return "tcp";
    }

    public EndpointAddress getEndpointAddress()
    {
        return myUri;
    }
    
    public void stop()
    {
        acceptThread.interrupt();
        Enumeration pte = pollThreads.elements();
        while (pte.hasMoreElements())
        {
            Thread pt = (Thread) pte.nextElement();
            pt.interrupt();
        }

        LOG.info("Shuting down peer on port " + ss.getLocalPort());
        try
        {
            ss.close();
        } catch (IOException ex)
        {
            LOG.debug("Error closing ServerSocket", ex);
        }
    }
}