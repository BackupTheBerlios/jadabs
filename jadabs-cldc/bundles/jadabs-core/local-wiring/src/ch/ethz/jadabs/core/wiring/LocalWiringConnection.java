/*
 * Created on Dec 9, 2004
 *
 * $Id: LocalWiringConnection.java,v 1.2 2004/12/27 15:25:03 printcap Exp $
 */
package ch.ethz.jadabs.core.wiring;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.SocketConnection;

import org.apache.log4j.Logger;


/**
 * This class wraps a network connection on the local
 * loopback interface between the Jadabs-Core and 
 * the additional bundle components. 
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class LocalWiringConnection
{
    /** logger to use in this class */
    private Logger LOG = Logger.getLogger("LocalWiringConnection");
    
    /** socket this connection runs over */
    private SocketConnection connection;
    
    /** input stream to read from connection */
    private DataInputStream in;
    
    /** output stream to write to connection */
    private DataOutputStream out;
    

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
        this.in = new DataInputStream(connection.openInputStream());
        this.out = new DataOutputStream(connection.openOutputStream());
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
        out.writeInt(b.length);
        out.write(b, 0, b.length);             
        out.flush();
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
        length = in.readInt();
        byte[] received = new byte[length];
        
        // now read byte message 
        b = 0; 
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
                // FIXME: Workaround for class hierarchy incompatibility between J2ME/J2SE
                // we need this cast below to trick the Java compiler
                // When compiling with osgi:install the J2SE bootclasspath
                // used. Hence DataInputStream inherits from FilterInputStream
                // with in turn inherits from InputStream. However on CLCD
                // DataInputStream *directly* extends InputStream. There 
                // is no FilterInputStream. Since the close() is overwritten 
                // in FilterInputStream the compiler places an invoke virtual
                // in the byte code:
                //
                //  11: invokevirtual #31; //Method java/io/FilterInputStream.close:()V
                //
                // Therefore we get a problem with the preverifer with this class
                // By statically chaning the type the compiler does not place 
                // any FilterInputStream stuff into the byte code               
                ((InputStream)in).close();
            } catch (IOException e) { 
                // simply ignore 
            } finally {
                in = null;
            }
        }
        if (out != null) {
            try {
                // FIXME: Workaround for class hierarchy incompatibility between J2ME/J2SE
                // Same as for DataInputStream also applies for DataOutputStream
                ((OutputStream)out).close();
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
