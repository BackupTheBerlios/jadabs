/*
 * Created on Dec 9, 2004
 *
 * $Id: LocalWiringCore.java,v 1.3 2005/02/17 17:29:17 printcap Exp $
 */
package ch.ethz.jadabs.core.wiring;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
import javax.microedition.io.SocketConnection;

import org.apache.log4j.Logger;


/**
 * The LocalWritingCore establishes a connection 
 * accepts UDP messages on CORE_WAKEUP_PORT port
 * and provides means to connect to a bundle 
 * MIDlets over the specified port. 
 *  
 * @author Ren&eacute; M&uuml;ller
 */
public class LocalWiringCore
{
    /** logger to be used */
    private static Logger logger = Logger.getLogger("LocalWiringCore");
    
    /** 
     * this port is used by other services to wake up 
     * the core MIDlet that contains this LocalWritingCore 
     */  
    private static final int CORE_WAKEUP_PORT = 4444; 
    
    /** buffer space to be allocated for wakeup datagram */
    private static final int DATAGRAM_BUFFER_SIZE = 15;
    
    /** flag indicating thread(s) that wiring core is shut down */
    private boolean aborted = false;

    /** Vector containing ConnectionNotifee instances */
    private Vector connectionNotifees = new Vector();

    /**
     * Default constructor
     */
    public LocalWiringCore() 
    {
        this(null);
    }
    
    /**
     * constructor
     * @param notfiee object to be called when a new connection is established  
     *        (may be <code>null</code> in which nobody is notified).
     */
    public LocalWiringCore(ConnectionNotifee notifee)    
    {
        if (notifee != null) {
            connectionNotifees.addElement(notifee);
        }
        Thread wakeupListener = new Thread(new Runnable() {
            public void run() {
                while (!aborted) {
	                // Wait for someone to wake us up. 
	                // this basically receives the wakeup
	                // UDP packet, so when receiving it 
	                // we are already up. However we must 
	                // setup a server socket that accepts
	                // the packet otherwise our PushRegistry
	                // entry is removed by the system 
	                // (this at least happens on the Nokia6600)                
	                try {
	                    DatagramConnection dc = (DatagramConnection)Connector.open(
	                      "datagram://:"+CORE_WAKEUP_PORT);
	                    Datagram dgrm = dc.newDatagram(DATAGRAM_BUFFER_SIZE);
	                    dc.receive(dgrm);
	                    byte[] buffer = dgrm.getData();
	                    DataInputStream din = new DataInputStream(new ByteArrayInputStream(buffer));	                    
	                    String magicCookie = din.readUTF();
	                    int portNumber = din.readInt();
	                    if (logger.isDebugEnabled()) {
	                        logger.debug("CORE_WAKEUP_DGRM received: '"+
	                                new String(buffer)+"' Port:"+portNumber);
	                    }
	                    if (portNumber > 1) {
	                        logger.debug("connecting to port "+portNumber);
	                        connect(portNumber);
	                    } else {
	                        logger.error("Invalid port number specified in CORE_WAKEUP_DGRM");
	                    }
	                    dc.close();
	                } catch(IOException e) {
	                    logger.error("Error in datagram wakeup listener: "+e);
	                }
                }
            }
        });
        wakeupListener.start();
    }
        
    /** 
     * Connect to a bundle MIDlet at the specified 
     * port number
     * @param portnr portnumber a local-loopback TCP
     * 	connection is to established
     * @return a connection object if connection
     * 	could be established
     * @throws IOException if connection could not be
     * 	established
     */
    public LocalWiringConnection connect(int portnr) 
    				throws IOException 
    {        
        SocketConnection s = (SocketConnection)Connector.open(
                "socket://127.0.0.1:"+portnr);        
        LocalWiringConnection connection = new LocalWiringConnection(s);
        
        // notify ConnectionNotifees about this new connection
        Enumeration e = connectionNotifees.elements();
        while (e.hasMoreElements()) {
            ConnectionNotifee notify = (ConnectionNotifee)e.nextElement();
            notify.connectionEstablished(connection);
        }
        return connection;
    }
    
    /**
     * Register connection notifee that is called whenever a new
     * connection is established. 
     * @param notifee object to register
     */
    public void addConnectionNotifee(ConnectionNotifee notifee)  {
        connectionNotifees.addElement(notifee);
    }
    
    /**
     * Unregister connection notifee.  
     * @param notfiee object to unregister
     */
    public void removeConnectionNotfiee(ConnectionNotifee notifee) {
        connectionNotifees.removeElement(notifee);
    }
}
