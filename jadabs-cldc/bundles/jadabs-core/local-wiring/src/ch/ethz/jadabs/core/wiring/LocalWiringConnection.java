/*
 * Created on Dec 9, 2004
 *
 * $Id: LocalWiringConnection.java,v 1.1 2004/12/22 09:35:09 printcap Exp $
 */
package ch.ethz.jadabs.core.wiring;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.SocketConnection;


/**
 * This class wraps a network connection on the local
 * loopback interface between the Jadabs-Core and 
 * the additional bundle components. 
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class LocalWiringConnection
{
    /** socket this connection runs over */
    private SocketConnection connection;
    
    /** input stream to read from connection */
    private InputStream in;
    
    /** output stream to write to connection */
    private OutputStream out;
    

    /**
     * Create new new LocalWiringConnection
     * @param connection socket connection this 
     * 	wiring connection is wrapped around 
     * @throws IOException if datastreams cannot be
     * 	opened. 
     */
    public LocalWiringConnection(SocketConnection connection)
    						throws IOException 
    {
        /*@ precondition connection != null @*/
        this.connection = connection;
        this.in = connection.openInputStream();
        this.out = connection.openOutputStream();
    }
    
    
    /**
     * Send mesasge specified in byte array 
     * to peer.
     * @param b byte array containing message 
     * 	to be sent to peer. 
     * @throws IOException if the connection is not 
     * 	available or something unexpected happens.
     */
    public void sendBytes(byte[] b) throws IOException 
    {
        /*@ precondition  b != null && 
                        isConnected()   @*/
        int length = b.length;
        byte[] towrite = new byte[length+4];
        
        // write length in bigendian 
        towrite[0] = (byte)(0xff & (length >> 24));
        towrite[1] = (byte)(0xff & (length >> 16));
        towrite[2] = (byte)(0xff & (length >> 8));
        towrite[3] = (byte)(0xff & length);
        System.arraycopy(b, 0, towrite, 4, length);
        
        out.write(towrite);             
        /*@ postcondition b unchanged @*/
    }
    
    /**
     * Read byte-array message from peer. 
     * @return Message that was sent by peer packed 
     * as a byte array  
     * @throws IOException if the connection is not available
     * 	or something unexpected happens.
     */
    public byte[] receiveBytes() throws IOException
    {
        int b, read=0, length = 0;
        
        // read length of message in bytes
        b = in.read();
        while (b != -1) {
            length |= 0xff & b;
            length = length << 8;
            b = in.read();
        }
        if (b == -1) {
            // error end of stream is reached 
            throw new IOException("end of stream reached");
        }
        byte[] received = new byte[length];
        
        // now read byte message 
        while ((read<length) && (b != -1)) {
            b = in.read(received, read, length-read);
            read += b;
        }
        return received;
    }
    
    /**
     * Close the connection of the stream 
     */
    public void close()
    {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) { 
                // simply ignore 
            } finally {
                in = null;
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                // simply ignore
            } finally {
                out = null;
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (IOException e) {
                // simply ingore
            } finally {
                connection = null;
            }
        }
    }
}
