/*******************************************************************************
 * 
 * $Id: Session.java,v 1.2 2004/11/19 08:16:36 afrei Exp $
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

package ch.ethz.jadabs.jxme.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.Message;


public final class Session
{

    private static final Logger log = Logger.getLogger(Session.class.getName());

    /** session timeout */
    private static int timeout = 30000;

    /**
     * An upper bound on the number of concurrent sessions to maintain at any
     * given time
     */
    private static int maxSessions = 15;

    /** maintains a pool of sessions */
    static Vector sessionPool = new Vector();

    private Socket socket = null;

    private DataInputStream dis = null;

    private DataOutputStream dos = null;

    private int lastUsed = 0;

    /*
     * static { try { timeout = Properties.getIntProperty("Session.timeout");
     * maxSessions = Properties.getIntProperty("Session.maxSessions"); } catch
     * (ConfigException ex) { // exception is already logged, continue with
     * default } }
     */
    private void init() throws IOException
    {
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        lastUsed = (int) (System.currentTimeMillis() / 1000L);
        sessionPool.addElement(this);
        log.info("Started session on " + socket.toString());
    }

    private Session(EndpointAddress uri) throws IOException
    {
        log.debug("openning a socket to " + uri.getHost() + ':' + uri.getPort());
        socket = new Socket(uri.getHost(), uri.getPort());
        init();
    }

    private Session(Socket socket) throws IOException
    {
        this.socket = socket;
        init();
    }

    public synchronized void close() throws IOException
    {
        log.debug("Closing session on " + socket.toString());
        sessionPool.removeElement(this);
        socket.close();
        if (dis != null)
        {
            dis.close();
        }
        if (dos != null)
        {
            dos.close();
        }
        dis = null;
        dos = null;
    }

    private static void reap(int now) throws IOException
    {
        Enumeration si = sessionPool.elements();
        while (si.hasMoreElements())
        {
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
        if (sessionPool.size() >= maxSessions)
        {
            closeSessionLRU(now);
        }
    }

    public static synchronized Session createSession(Socket sock) throws IOException
    {
        return new Session(sock);
    }

    public static synchronized Session getSession(EndpointAddress uri) throws IOException
    {
        int now = (int) (System.currentTimeMillis() / 1000L);
        reap(now);

        Session session = null;
        Enumeration si = sessionPool.elements();
        while (si.hasMoreElements())
        {
            Session s = (Session) si.nextElement();
            log.debug(s.socket.getInetAddress() + ":" + s.socket.getPort() + "-" + uri.getHost() + ":" + uri.getPort());
            if (s.socket.getInetAddress().equals(InetAddress.getByName(uri.getHost()))
                    && s.socket.getPort() == uri.getPort())
            {
                session = s;
                // we should break here but we want to continue
                // the reaping of sessions that have timed-out
            }
        }

        if (session == null)
        {
            // TBD: May be, I need to send HELLO message again
            session = new Session(uri);
        }

        session.lastUsed = now;
        return session;
    }

    private static void closeSessionLRU(int now) throws IOException
    {
        Session lruSession = null;
        int lru = now;
        Enumeration si = sessionPool.elements();
        while (si.hasMoreElements())
        {
            Session s = (Session) si.nextElement();
            if (s.lastUsed < lru)
            {
                lru = s.lastUsed;
                lruSession = s;
            }
        }

        if (lruSession != null)
        {
            lruSession.close();
        }
    }

    public synchronized void send(Message outgoing) throws IOException
    {

        outgoing.write(dos);
        dos.flush();

        // for session LRU expiry calculations
        lastUsed = (int) (System.currentTimeMillis() / 1000L);
    }

    public synchronized Message recv() throws IOException
    {

        Message msg = null;
        msg = Message.read(dis);
        // for session LRU expiry calculations
        lastUsed = (int) (System.currentTimeMillis() / 1000L);

        return msg;
    }

    public boolean equals(Object obj)
    {
        if (obj == null) { return false; }

        if (!(obj instanceof Session)) { return false; }

        Session s = (Session) obj;
        return equals(s.socket.getInetAddress(), s.socket.getPort());
    }

    /**
     * Two sessions are equal iff they are connected to the same address on the
     * same port. This will result in separate sessions to multihomed machines.
     */
    private boolean equals(InetAddress addr, int port)
    {
        return socket.getPort() == port && socket.getInetAddress().equals(addr);
    }

    public String toString()
    {
        return socket.toString();
    }
}