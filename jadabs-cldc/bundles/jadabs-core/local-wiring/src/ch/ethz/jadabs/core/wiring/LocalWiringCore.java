/*
 * Created on Dec 9, 2004
 *
 * $Id: LocalWiringCore.java,v 1.2 2004/12/27 15:25:03 printcap Exp $
 */
package ch.ethz.jadabs.core.wiring;

import java.io.IOException;

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

    
    /** constructor */
    public LocalWiringCore() 
    {
        Thread wakeupListener = new Thread(new Runnable() {
            public void run() {
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
                    if (logger.isDebugEnabled()) {
                        logger.debug("CORE_WAKEUP_DGRM received: '"+
                                new String(buffer)+"'");
                    }
                    dc.close();
                } catch(IOException e) {
                    logger.error("Error in datagram wakeup listener: "+e);
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
        return new LocalWiringConnection(s);
    }
}
