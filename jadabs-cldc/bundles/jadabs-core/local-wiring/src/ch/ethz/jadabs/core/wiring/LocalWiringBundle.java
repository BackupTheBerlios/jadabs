/*
 * Created on Dec 9, 2004
 *
 *	$Id: LocalWiringBundle.java,v 1.3 2005/02/17 17:29:17 printcap Exp $
 */
package ch.ethz.jadabs.core.wiring;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import javax.microedition.io.ServerSocketConnection;
import javax.microedition.io.SocketConnection;

import org.apache.log4j.Logger;


/**
 * The LocalWritingBundle sends a CORE_WAKEUP
 * datagram message to the LocalWritingBundle 
 * on port CORE_WAKEUP_PORT which then establishes
 * a TCP-connection for communication to this
 * service bundle MIDlet.
 *  
 * @author Ren&eacute; M&uuml;ller
 */
public class LocalWiringBundle
{
    /** port to send datagram to to wake up port (also in LocalWiringCore) */
    private static final int CORE_WAKEUP_PORT = 4444;
    
    /** logger to be used */
    private static Logger logger = Logger.getLogger("LocalWiringCore");
    
    /** magic cookie used in wakeup packet */
    private static String MAGIC_WAKEUP_COOKIE = "Wake up Polly!";
    
    /** listening port for TCP-connection */
    private int portnumber;
    
    /** listening server socket */
    private ServerSocketConnection serverSocket;
    
    /** the wiring connection to the core */
    private LocalWiringConnection connection;
    
    /** state of the connection */
    private boolean connected = false;
    
    /** listener that gets notified when wakeup call arrives */
    private ConnectionNotifee notifee;
    
    
    /** 
     * Create a new LocalWiringBundle on the listening port 
     * @param port number of listening port 
     * @param notifee notifee a ConnectionNotifee listener that gets notified 
     *        when a wakeup connection is established. 
     */
    public LocalWiringBundle(int port, ConnectionNotifee notifee)
    {
        this.portnumber = port;
        this.notifee = notifee;
        (new Thread(new ConnectorThread())).start();
    }
        
    /**
     * Sends Datagram packet to JadabsCore in order to wake it up. 
     */
    public void wakeupCore() throws IOException
    {
        DatagramConnection dc = (DatagramConnection)
        		Connector.open("datagram://127.0.0.1:"+CORE_WAKEUP_PORT);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        
        // send magic wake up cookie
        dout.writeUTF(MAGIC_WAKEUP_COOKIE);
        
        // send port number for later reconnect
        dout.writeInt(portnumber);
        dout.close();
        bout.close();
        byte[] buffer = bout.toByteArray(); 
        Datagram dgrm = dc.newDatagram(buffer, buffer.length);
        dc.send(dgrm);
    }
    
    /**
     * Get the Wiring connection 
     * @return wiring connection or null of !isConnected()
     */
    public LocalWiringConnection getConnection() 
    {
        return connection;
    }
    
    /**
     * Get the state of the TCP connection
     * @return true if currently a TCP connection
     * is available
     */
    public boolean isConnected()
    {
        return connected;
    }
    
    /**
     * Close socket and connection
     */
    public void close() 
    {
        connected = false;
        if (connection != null) {
            connection.close();
            connection = null;
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch(IOException e) {
                // ignore
            } finally {
                // permit collection of object 
                serverSocket = null;
            }
        }
    }
    
    /** 
     * This inner class implements the thread that is 
     * waiting for the core to establish a connection. 
     */
    private class ConnectorThread implements Runnable {
        
        public void run() {
            // Wait for someone to wake us up. 
            // this basically receives the wakeup
            // UDP packet, so when receiving it 
            // we are already up. However we must 
            // setup a server socket that accepts
            // the packet otherwise our PushRegistry
            // entry is removed by the system 
            // (this at least happens on the Nokia6600)
            if (serverSocket != null) {
                try { 
                    serverSocket.close();
                } catch(IOException e) { 
                    // ignore
                } finally {
                    // give lousy GC on mobile phones a 
                    // chance to collect object 
                    serverSocket = null;
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("waitforWakeConnection() called.");
            }
            try {
                serverSocket = (ServerSocketConnection)Connector.open(
                        "socket://:"+portnumber);
                SocketConnection c = (SocketConnection)serverSocket.acceptAndOpen();
                if (logger.isDebugEnabled()) {
                    logger.debug("connection to "+c.getAddress()+" opened and accepted.");
                }
                connection = new LocalWiringConnection(c);
                connected = true;
                if (notifee != null) {
                    notifee.connectionEstablished(connection);
                }
            } catch(IOException e) {
                logger.error("error accepting new connections: "+e);
                return;
            } finally {
                if (logger.isDebugEnabled()) {
                    logger.debug("listener thread shut down.");
                }
            }                    
        }
    }
}
