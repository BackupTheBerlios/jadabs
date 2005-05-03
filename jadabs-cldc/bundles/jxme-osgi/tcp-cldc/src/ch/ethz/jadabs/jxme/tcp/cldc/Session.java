/*
 * $Id: Session.java,v 1.3 2005/05/03 11:45:09 afrei Exp $
 * 
 * Copyright (c) 2001 Sun Microsystems, Inc. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 * include the following acknowledgment: "This product includes software
 * developed by the Sun Microsystems, Inc. for Project JXTA." Alternately, this
 * acknowledgment may appear in the software itself, if and wherever such
 * third-party acknowledgments normally appear.
 * 
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 * not be used to endorse or promote products derived from this software without
 * prior written permission. For written permission, please contact Project JXTA
 * at http://www.jxta.org.
 * 
 * 5. Products derived from this software may not be called "JXTA", nor may
 * "JXTA" appear in their name, without prior written permission of Sun.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SUN
 * MICROSYSTEMS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many individuals on
 * behalf of Project JXTA. For more information on Project JXTA, please see
 * <http://www.jxta.org/>.
 * 
 * This license is based on the BSD license adopted by the Apache Foundation.
 ******************************************************************************/

package ch.ethz.jadabs.jxme.tcp.cldc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.Message;


/**
 * Abstracts a client connection session, i.e. a TCP connection to a client peer. 
 * Note: This TCP transport does not work for J2SE. Use the 
 * TCP transport provided in the "main" Jadabs tree instead.
 *  
 * @author Ren&eacute; M&uuml;ller
 * @author afrei
 */
public final class Session
{
    /** logger to be used in the session */
    private static final Logger log = Logger.getLogger("ch.ethz.jadabs.jxme.tcp.cldc.Session");

    /** session timeout milliseconds */
    private static int timeout = 30000;

    /**
     * An upper bound on the number of concurrent sessions to maintain at any
     * given time
     */
    private static int maxSessions = 15;

    /** maintains a pool of sessions */
    static Vector sessionPool = new Vector();

    /** TCP socket connection this session runs over */
    private SocketConnection socket = null;

    /** input stream of socket connection */
    private DataInputStream dis = null;

    /** output stream of socket connection */
    private DataOutputStream dos = null;

    /** time stamp of last actions (traffic) on this session */
    private int lastUsed = 0;

    /**
     * Initialize session, i.e. obtain in/out streams and
     * add session to session pool.
     * @throws IOException if data streams cannot be obtained 
     * from socket connection
     */
    private void init() throws IOException
    {
        dis = new DataInputStream(socket.openInputStream());
        dos = new DataOutputStream(socket.openOutputStream());
        lastUsed = (int) (System.currentTimeMillis() / 1000L);
        sessionPool.addElement(this);
        log.info("Started session on " + socket.toString());
    }

    /**
     * Create new session (private) from an enpoint address. Clients
     * must use createSession(). 
     * @param uri Endpoint address the peer host and connection port is 
     *        taken from
     * @throws IOException if a connection cannot be established.
     */
    private Session(EndpointAddress uri) throws IOException
    {
        log.debug("openning a socket to " + uri.getHost() + ':' + uri.getPort());
        socket = (SocketConnection)Connector.open("socket://"+uri.getHost()+":"+uri.getPort());
        init();
    }

    /**
     * Create new session (private) from an already existing socket connection.
     * Clients must use createSession(). 
     * @param socket socket connection this sesssion is wrapped around
     * @throws IOException if the in/output streams cannot be obtained 
     * from the connection
     */
    private Session(SocketConnection socket) throws IOException
    {
        this.socket = socket;
        init();
    }

    /**
     * Close this session, i.e. remove session from session pool and 
     * close the connection underneath
     * @throws IOException 
     */
    public synchronized void close() throws IOException
    {
        log.debug("Closing session on " + socket.toString());
        sessionPool.removeElement(this);
        socket.close();
        // close input stream
        if (dis != null) {
            dis.close();
        }
        // close output stream
        if (dos != null) {
            dos.close();
        }
        dis = null;
        dos = null;
    }

    /**
     * Remove, i.e. close all session that have not been in used
     * since now-timeout, impose max sessions by removing LRU sessions 
     * @param now  time in milliseconds;
     * @throws IOException when error occures during session closing
     */
    private static void reap(int now) throws IOException
    {
        Enumeration si = sessionPool.elements();
        while (si.hasMoreElements()) {
            Session s = (Session) si.nextElement();
            // reap sessions that have timed-out
            if (now - s.lastUsed > timeout)
            {
                log.debug("Session timeout to " + s);
                s.close();
                continue;
            }
        }

        log.debug("# of Sessions: " + sessionPool.size());
        // impose maxSessions
        if (sessionPool.size() >= maxSessions) {
            closeSessionLRU(now);
        }
    }

    /**
     * Create a new session wrapped around a socket connection and 
     * insert it into the session pool.
     * @param sock socket connection 
     * @return the new ssion object 
     * @throws IOException during the creating of the session
     */
    public static synchronized Session createSession(SocketConnection sock) throws IOException
    {
        return new Session(sock);
    }

    /**
     * Get session to specified endpoint, if session is not in pool, create a new one
     * while maintaining the max sessions constraint of the pool. 
     * @param uri endpoint address of peer to connect
     * @return new session or session from the pool to specified peer
     * @throws IOException 
     */
    public static synchronized Session getSession(EndpointAddress uri) throws IOException
    {
        int now = (int) (System.currentTimeMillis() / 1000L);
        reap(now);
        
        Session session = null;
        Enumeration si = sessionPool.elements();
        
        // look for existing session in pool
        while (si.hasMoreElements()) {
            Session s = (Session) si.nextElement();
            if (s.socket.getAddress().equals(uri.getHost()) 
                    && s.socket.getPort() == uri.getPort())  {
                session = s;
                break;
            }
        }

        if (session == null) {
            // session to specified peer not in session pool 
            // create a new session 
            session = new Session(uri);
        }
        session.lastUsed = now;
        return session;
    }

    /**
     * Close and remove session from session pool that is least recently used
     * @param now timestamp in milliseconds 
     * @throws IOException 
     */
    private static void closeSessionLRU(int now) throws IOException
    {
        Session lruSession = null;
        int lru = now;
        Enumeration si = sessionPool.elements();
        while (si.hasMoreElements())  {
            Session s = (Session) si.nextElement();
            if (s.lastUsed < lru) {
                lru = s.lastUsed;
                lruSession = s;
            }
        }
        if (lruSession != null) {
            // there is a least recently used session 
            // so close it 
            lruSession.close();
        }
    }

    /**
     * Send a message over this session 
     * @param outgoing JXTA message to be sent 
     * @throws IOException
     */
    public synchronized void send(Message outgoing) throws IOException
    {
        outgoing.write(dos);
        dos.flush();

        // for session LRU expiry calculations
        lastUsed = (int) (System.currentTimeMillis() / 1000L);
    }
    
    /**
     * Propagate (broadcast) message over all connections in pool 
     * @param outgoing message to propagate
     * @throws IOException if an individual connection cause an exception
     */
    public static void propagate(Message outgoing) throws IOException
    {        
        Enumeration si = sessionPool.elements();
        boolean exceptionOccurred = false;
        String lastExceptionSession = null;
        
        // look for existing session in pool
        while (si.hasMoreElements()) {
            Session s = (Session) si.nextElement();
            try {
                s.send(outgoing);
            } catch(IOException e) {
                exceptionOccurred = true;
                lastExceptionSession = s.toString();
            }
        }
        if (exceptionOccurred) {
            throw new IOException("exception on at least one connection: "+lastExceptionSession);
        }
    }

    /**
     * Receive JXTA message over this session 
     * @return received JXTA message or <code>null</code> in case of
     *         error 
     * @throws IOException in case of an error
     */
    public synchronized Message recv() throws IOException
    {
        Message msg = null;
        msg = Message.read(dis);
        // for session LRU expiry calculations
        lastUsed = (int) (System.currentTimeMillis() / 1000L);
        return msg;
    }

    /**
     * Equality relation 
     * @param obj object to compare <code>this</code> with 
     * @return true if <code>obj</code> is a <code>Session</code>
     *    instance or a subclass of it and is connected to a
     *    host with the same inet address, false otherwise. 
     */
    public boolean equals(Object obj)
    {
        if (obj == null) { return false; }
        if (!(obj instanceof Session)) { return false; }

        Session s = (Session)obj;
        try {
            return equals(s.socket.getAddress(), s.socket.getPort());
        } catch(IOException e) {
            /* well, are there better solutions? */
            return false;
        }
    }

    /**
     * Two sessions are equal iff they are connected to the same address on the
     * same port. This will result in separate sessions to multihomed machines.
     * @param addr address string as obtained by <code>SocketConnection.getAddress</code>
     */
    private boolean equals(String addr, int port)
    {
        try {
            return socket.getPort() == port && socket.getAddress().equals(addr);
        } catch(IOException e) {
            /* well, are there better solutions? */
            return false;
        }
    }

    /**
     * Get string representation of this sessoin
     * @return string representation of this session
     */
    public String toString()
    {
        return socket.toString();
    }
}