/*
 * $Id: TCPTransport.java,v 1.1 2005/02/18 14:15:29 printcap Exp $
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
package ch.ethz.jadabs.jxme.tcp.cldc;


import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.SocketConnection;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.Transport;

/**
 * Provides a TCP messaging service for JXTA peers using the
 * networking infrastructor provided by the CLDC platform.
 * Note: This TCP transport does not work for J2SE. Use the 
 * TCP transport provided in the "main" Jadabs tree instead.
 * 
 * @author Ren&eacute; M&uum;ller 
 * @author afrei
 */
public final class TCPTransport implements Transport
{

    /** logger used for this transport */
    private static final Logger LOG = Logger.getLogger("ch.ethz.jadabs.jxme.tcp.cldc.TCPTransport");

    private ServerSocketConnection ss = null;

    /** a runnable waits for new connection and dispatches sessions */
    private Acceptor acceptor = null;

    /** list of session workers */
    private Vector sessionWorkers = new Vector();
    
//    private Session session = null;

    /** address of this endpoint */
    private EndpointAddress myUri;

    /** listener for JXME messages */
    private Listener listener;

    /**
     * Constructor for TCP Transport. Server socket is not created yet and thus does not 
     * start listening for incoming connections.
     */
    public TCPTransport()
    {
        /* empty */
    }

    public void init(EndpointAddress myUri, Listener listener) throws IOException
    {
        this.myUri = myUri;
        this.listener = listener;

        // TCP - Connection
        // check if myURI is given, then open a server socket
        if (myUri != null)
        {
            ss = (ServerSocketConnection)Connector.open("socket://:"+myUri.getPort());

            // create acceptor
            acceptor = new Acceptor(ss, this);
            Thread acceptThread = new Thread(acceptor);
            acceptThread.start();
            LOG.info("Accepting connections on port " + ss.getLocalPort());
        }

    }
   
    /** 
     * Handle received message, i.e. deliver it to the registered listener  
     * @param msg received message to delivered to the JXME layer
     */
    void handleMessage(Message msg)
    {
        LOG.info("msg: " + msg);
        if (listener != null) {
            listener.handleMessage(msg, null);
        }
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

        SocketConnection socket = (SocketConnection)Connector.open("socket://"+seedUri.getHost()+":"+seedUri.getPort());
        Session.createSession(socket);
        LOG.info("Connected to: " + seedUri);
    }

    /**
     * Send a JXME message to the destination peer 
     * @param outgoing message to be sent
     * @param desPeer destination endpoint 
     * @throws IOException always when something goes wrong ;-)
     */
    public void send(Message outgoing, EndpointAddress destPeer) throws IOException
    {
        // check desitination address
        if (destPeer == null) {
            throw new IOException("destPeer must not be null");
        }
        
        LOG.info("sending Message : " + outgoing + " to: " + destPeer);
        // look up session, if we are not already connected to this peer 
        // we need to establish a connection first.
        Session session = Session.getSession(destPeer);
        
        if (session != null) {
            // if yes, send message over session
            try {
                session.send(outgoing);
            } catch (IOException e) {
	            // may be previous session from the same peer is still lingering.
	            // close and re-create a session.
	            session.close();
	            session = Session.getSession(destPeer);
	            session.send(outgoing);
	        }
        } else {
            // we are not 
            throw new IOException("no connected session");            
        } 
    }

    /**
     * Propagate Message to send over all available connections.
     * @param message message to send over all available connections 
     * @throws IOException if an individual connection cause an exception
     */
    public void propagate(Message message) throws IOException
    {
        Session.propagate(message);
    }

    /**
     * Get protocol identifier string of this transport
     * @return STRING "tcp"
     */
    public String getProtocol()
    {
        return "tcp";
    }

    /**
     * Return endpoint address of this TCP transport 
     * @return EndpointAddress that belongs to this TCP transport 
     */
    public EndpointAddress getEndpointAddress()
    {
        return myUri;
    }
    
    /** 
     * Stop TCP transport, i.e. shut down server socket,
     * acceptor thread, and open connections. 
     */
    public void stop()
    {
        acceptor.shutdown();
        Enumeration pte = sessionWorkers.elements();
        while (pte.hasMoreElements())
        {
            SessionWorker worker = (SessionWorker) pte.nextElement();
            worker.shutdown();
        }

        int localport = -1;
        try {
            localport = ss.getLocalPort();
        } catch(IOException e) {
            /* just ignore */
        }
        LOG.info("Shuting down peer on port " + localport);
        try {
            ss.close();
        } catch (IOException ex)
        {
            LOG.debug("Error closing ServerSocket", ex);
        }
    }
    
    /*
     * 
     * inner classes
     * 
     */
    
    /** Acceptor thread waits for incomming connections */
    private static final class Acceptor implements Runnable
    {
        /** the server socket to be used */
        private ServerSocketConnection ss = null;

        /** TCP transport this acceptor belongs to */
        private TCPTransport tcpListener;
        
        /** when set to true transport is aborted */
        private boolean aborted = false;

        /**
         * Constructor for Acceptor threads.
         * 
         * @param ss
         *            ServerSocketConnection to listen on.
         * @param tcpListener
         *            incoming message handler.
         */
        Acceptor(ServerSocketConnection ss, TCPTransport tcpListener)
        {
            this.ss = ss;
            this.tcpListener = tcpListener;
        }

        /** run body of acceptor thread */
        public void run()
        {
            String clientDescription = null;
            while (!aborted)
            {
                Session incomingSession = null;
                try
                {
                    SocketConnection client = (SocketConnection)ss.acceptAndOpen();
                    clientDescription = client.toString();
                    LOG.info("Accepted connection from " + clientDescription);

                    // dispatch new worker thread on this connection
                    incomingSession = Session.createSession(client);
                    SessionWorker sw = new SessionWorker(incomingSession, tcpListener);
                    Thread t = new Thread(sw);
                    tcpListener.sessionWorkers.addElement(sw);
                    t.start();
                } catch (IOException ex)
                {
                    LOG.warn("Error starting session", ex);
                    continue;
                }
            }
        }
        
        /**
         * Shutdown acceptor, i.e. close client session
         */
        void shutdown() 
        {
            aborted = true;
            try {
                ss.close();
            } catch(IOException e) {
                /* So what? We are closing anyway... */
            }            
        }
    }

    /** Worker thread that reads data from a client connection */
    private static final class SessionWorker implements Runnable
    {
        /** client session */
        private Session incomingSession = null;

        /** listener to deliver received JXME messages */ 
        private TCPTransport listener = null;
        
        /** flag set to true indicates that this session worker is shut down */
        private boolean aborted = false;

        /**
         * Create a new session worker
         * @param incomingSession associated session 
         * @param listener listener for JXME messages 
         */
        public SessionWorker(Session incomingSession, TCPTransport listener)
        {
            this.incomingSession = incomingSession;
            this.listener = listener;
        }

        /**
         * Shutdown session, i.e. close client session.
         */
        void shutdown()
        {
            aborted = true;
            try {
                incomingSession.close();
            } catch(IOException e) {
                /* So what? We are closing anyway... */
            }
        }
        
        /**
         * Body method of thread 
         */
        public void run()
        {
            try {
                //receive message from session
                for (Message msg = incomingSession.recv();	(msg != null) && !aborted; msg = incomingSession.recv())  {
                    // deliver message just received to listener  
                    listener.handleMessage(msg);
                }
            } catch (IOException ex)  {
                LOG.error("Exception on " + incomingSession+": "+ex.getMessage());
                LOG.debug("Closing session.");
                try  {
                    incomingSession.close();
                } catch (IOException e)  {
                    e.printStackTrace();
                    incomingSession = null;
                }
            }
        }
    }
}