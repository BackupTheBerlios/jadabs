/*
 * Created on Jul 28, 2004
 *
 * $Id: BTConnection.java,v 1.2 2005/01/19 10:01:58 afrei Exp $
 */
package ch.ethz.jadabs.jxme.bt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.bluetooth.RemoteDevice;
import javax.microedition.io.StreamConnection;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.MalformedURIException;


/**
 * This class manages a single connection from a BT client to the 
 * server. It incorporates a reader thread.  
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class BTConnection implements Runnable 
{
    /** logger for instances of BTConnection */
    private static final Logger LOG = Logger.getLogger("ch.ethz.jadabs.jxme.bt.BTConnection");
    
    /** this BT connection */
    private StreamConnection conn;
    
    /** the remote device at the other side of the connection */
    private RemoteDevice remoteDevice;
    
    /** Bluetooth address of this host */
    private String localBTAddress;
    
    /** Bluetooth address of remote device */
    private String remoteBTAddress;  
    
    /** reference to transport that we call when a new message is received */
    private BTTransport btTransport;
    
    /** thread that reads data from the connection */
    private Thread readerThread;
    
    /** input stream from the connection this device reads from */
    private InputStream in;
    
    /** output stream from the connection this device writes to */
    private OutputStream out;
    
    /** true if we are aborting */
    private boolean aborting = false;
    
    /** address of the opposite end of the connection */
    private EndpointAddress remoteAddress;
    
    /**
     * Creates a new BTConnection and starts the reader thread
     * @param conn BT connection this class wraps around
     * @param remoteDevice device at the other end of the connection
     * @param btTransport reference to the transport 
     */
    public BTConnection(StreamConnection conn, RemoteDevice remoteDevice,
                        BTTransport btTransport) 
    {
        this.conn = conn;
        this.remoteDevice = remoteDevice;
        this.btTransport = btTransport;
        this.localBTAddress = btTransport.getEndpointAddress().getHost();
        this.remoteBTAddress = remoteDevice.getBluetoothAddress();
        try {
            this.in = conn.openInputStream();
            this.out = conn.openOutputStream();
        } catch(IOException e) {
            LOG.fatal("cannot open input/output streams from connection.");
            e.printStackTrace();            
            // continuing anyway
        }
        try {
            this.remoteAddress = new EndpointAddress("btspp", 
                                    remoteDevice.getBluetoothAddress());
        } catch (MalformedURIException e) {
            // this should never ever happen
        }        
    }

    /**
     * Get remote address of the other end of the connection. 
     * @return EndpointAddress of the opposite end of the connection
     */
    public EndpointAddress getRemoteAddress() 
    {
        return remoteAddress;
    }
    
    /**
     * Create worker thread (reader) and start it 
     */
    public void startWorker() 
    {
        // create reader thread and start it 
        readerThread = new Thread(this);
        readerThread.start();
    }

    /** 
     * This is the run-body of the reader thread 
     * @see java.lang.Runnable#run()
     */
    public void run()
    {
        try {
            LOG.debug("new connection to "+remoteBTAddress);
            
            // add BT Transport to EndpointService
//          BTActivator.endptsvc.addTransport(
//                  remoteAddress, 
//                  btTransport);
            
            while (!aborting) {
                // skip bytes in InputStream until PROLOG field in header 
                // is encountered
                BTHeader header = BTHeader.synchroniseAndReadHeader(in);
                if ((header == null) && !aborting) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("invalid header received from "+remoteAddress.getHost());
                        continue;
                    }
                }
                
                // read message itself
                byte messagedata[] = new byte[header.getPayloadLength()];
                int bytesRead = 0;
                while (bytesRead < messagedata.length) {
                    int count = in.read(messagedata, bytesRead, 
                                        messagedata.length-bytesRead);
                    if (count == -1) {
                        // end of stream is reached before enough bytes could 
                        // be read-in
                        throw new IOException("Input stream ended prematurely.");
                    }
                    bytesRead += count;
                }                             
                
                // read footer of message
                BTFooter footer = BTFooter.readFooter(in);
                if ((footer == null) && !aborting) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("invalid footer received from "+remoteAddress.getHost());
                        continue;
                    }
                }
                                                
                // deliver message
                btTransport.deliverMessage(header.getReceiver(), header.getSender(),
                         messagedata, header.isMulticast());                
            }
        } catch(IOException e) {
            if (!aborting) {
                
                // remove Endpoint from Peers URI list
//                BTActivator.endptsvc.removeTransport(remoteAddress);
                
                // an exception always occurs when we are aborting 
                // however in this case nothing is reported
                if (LOG.isDebugEnabled()) {
                    LOG.debug("error in connection from "+remoteAddress.getHost());
                    LOG.debug("  closing connection.");
                    close();
                }
            }
        }
    }
    
    /**
     * Close this connection.     
     */
    public void close() 
    {
        if (aborting) {
            // do nothing if we are already aborting
            return;
        }
        synchronized(this) {
            aborting = true;
        }
        
        // closing data streams and connection
        try {
           in.close();
           out.close();
           conn.close();
        } catch(IOException e) {
            // there is nothing we can do about that...
            // just go on.
        }
        
        // notify transport that connection is closed
        // thus causing its removal from the connection table
        btTransport.connectionClosed(this);
    }
    
    /**
     * Send bytes from specified byte array over the RFCOMM link 
     * to the remote device 
     * @param message reference to byte array to be sent
     * @param receiver 12-byte string with receiver's BT address
     * @param multicast <code>true</code> if this is a multicast message,
     *        <code>false</code> otherwise. 
     * @throws IOException in case that an a correspoding exception
     *         occurs in the underlying <code>OutputStream</code>. 
     */
    public synchronized void sendMessage(byte[] message, String receiver, 
                                         boolean multicast)	throws IOException 
    {      
        BTHeader header = new BTHeader(localBTAddress,
                remoteBTAddress, message.length, multicast);
        try {
            header.writeHeader(out);					// write header            
            out.write(message);							// write message                        
            BTFooter.getFooter().writeFooter(out);	// write footer
            out.flush();
        } catch(IOException e) {
            LOG.error("cannot send message to "+receiver+": "+e.getMessage());
            throw e;
        }        
        
        
    }
    
    /**
     * Retrun BT address of remote device
     * @return 12 character hex-string BT address
     */
    public String getRemoteBTAddress() 
    {
        return remoteBTAddress;
    }
}
