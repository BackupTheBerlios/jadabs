/*
 * $Id: BTHeader.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */

/**
 * JXME[dk]
 * ch.ethz.iks.jxme.bluetooth.impl
 * BTHeader.java
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 * @author Ren&eacute; M&uuml;ller, muellren[at]student.ethz.ch
 *
 * Jul 9, 2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * 
 * Adaptations by Ren&eacute; M&uul;ller 
 * for semester work summer 2004
 * "JXME-Bluetooth for a Mobile Phone (J2ME/CLDC)"  
 */
package ch.ethz.jadabs.jxme.bt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

/**
 * This class represents the header of a message. It offers method to read a
 * header from a InputStream and write the header informations to a
 * OutputStream.
 * 
 * <p>Message format <code>BT_HEADER</code>:</b>
 * <table border="1">
 * 	<tr><th>Offset</th> <th>Field</th> <th>Type</th> <th>Length</th> 
 *        <th>Description</th></tr>
 *    <tr><td>0</td> <td><code>PROLOG</code></td> <td><code>char</code></td> 
 *        <td>4 bytes</td> <td>synchronisation sequence <code>'++++'</code></td></tr>
 *    <tr><td>4</td> <td><code>RECEIVER</code></td> <td><code>hex-string</code></td> 
 *        <td>12 bytes</td> <td>hex-string of Bluetooth address of receiving device</td></tr>
 *    <tr><td>16</td> <td><code>SENDER</code></td> <td><code>hex-string</code></td> 
 *        <td>12 bytes</td> <td>hex-string of Bluetooth address of sending device</td></tr>
 *    <tr><td>28</td> <td><code>PAYLOAD_LENGTH</code></td> <td><code>32-bit int</code></td> 
 *        <td>4 bytes</td> <td>length of payload data in bytes</td></tr>
 *    <tr><td>32</td> <td><code>MULTICAST</code></td> <td><code>char</code></td> 
 *        <td>1 byte</td> <td>0x10: multicast message, 0x14: unicast message</td></tr>
 * </table> 
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 * @author Ren&eacute; M&uuml;ller, muellren[at]student.ethz.ch
 */
public class BTHeader
{
    /** Logger to be used */
    private static Logger LOG = Logger.getLogger("BTHeader");
    
    /** value for multicast field: multicast message */
    private static final byte MULTICAST_MESSAGE = 0x0A;
    
    /** value for multicast field: unicast message */
    private static final byte UNICAST_MESSAGE = 0x14;    
    
    /** The preamble is sent before the header starts */
    public static final String PROLOG = "++++";

    /** BT address of the receiving device */
    private String receiver;

    /** BT address of the sending device */
    private String sender;

    /** message length (payload length) */
    private int payloadLength = -1;

    /** true if the message is is a multicast message */
    private boolean isMulticast = false;

    /**
     * Creates a header object of the given parameter list.
     * 
     * @param sender
     *            sender's Bluetooth device address as String
     * @param receiver
     *            receiver's Bluetooth device address as String
     * @param payLoadLength
     *            length of the data written/read to/from the stream.
     * @param isMulticast
     *            set this flag to <code>true</code> to indicate that this
     *            message is a multicast message.
     */
    public BTHeader(String sender, String receiver, int payLoadLength, boolean isMulticast)
    {
        this.receiver = receiver;
        this.sender = sender;
        this.payloadLength = payLoadLength;
        this.isMulticast = isMulticast;
    }

    /**
     * Return the BT address from the receiver field. 
     * @return Bluetooth device address of the receiver as
     *         <code>java.lang.String</code>, this must not be the localhosts
     *         Bluetooth address.
     */
    public String getReceiver()
    {
        return receiver;
    }

    /**
     * Return the BT address from the sender field
     * @return Bluetooth device address of the sender as
     *         <code>java.lang.String</code>.
     */
    public String getSender()
    {
        return sender;
    }

    /**
     * This method returns the length of the payload contained in the following
     * package.
     * 
     * @return payload length in bytes
     */
    public int getPayloadLength()
    {
        return payloadLength;
    }

    /**
     * This method returns a <code>boolean</code> indicating if the current
     * message is a multicast or not.
     * 
     * @return <code>true</code> if the message is a multicast message, 
     *         <code>false</code> otherwise
     */
    public boolean isMulticast()
    {
        return isMulticast;
    }

    /**
     * Writes the header specified by this object to the given output stream
     * 
     * @param out
     *            <code>java.io.OutputStream</code> to write the header to.
     * @throws IOException
     *             if the write operation fails.
     */
    public void writeHeader(OutputStream out) throws IOException
    {        
        out.write(PROLOG.getBytes());  	// write '++++' prolog bytes
        out.write(receiver.getBytes());	// write receiver BT address
        out.write(sender.getBytes());		// write sender BT address

        // print lenth of payload - 4 bytes 
        // Format: little endian (network order)
        for (int i = 0; i < 4; i++)
        {
            out.write((payloadLength >>> i * 8) & 0xFF);
        }       
        out.write(isMulticast?10:20);		 // print multicast flag - 1 byte
    }

    /**
     * This method reads the header of a message without the leading
     * <code>PROLOG</code>, i.e. <code>PROLOG</code> must already be 
     * stripped away. 
     * 
     * @param in InputStream to read header from 
     * @return returns the BTHeader object read from given InputStream. Returns
     *         <code>null</code> if the header is invalid.
     * @throws IOException from the underlying InputStream 
     */
    public static BTHeader readHeaderWithoutProlog(InputStream in) throws IOException
    {
        byte[] receiver = new byte[12];
        byte[] sender = new byte[12];
        byte[] lengthBuffer = new byte[4];
        int length = 0;
        boolean isMulticast;

        readFully(in, receiver);			// read receiver field (12 bytes)
        if (!isBTAddressValid(receiver)) {
            return null;
        }
        
        readFully(in, sender);			// read sender field (12 bytes)
        if (!isBTAddressValid(sender)) {
            return null;
        }

        readFully(in, lengthBuffer);	// read payload length field (4 bytes)        
        for (int i = 0; i < 4; i++)
        {	// put length value together (network order, little end first)
            length |= (lengthBuffer[i] << i*8) & (0xFF << i*8);
        }

        int tmp = in.read();				// read receiver field (12 bytes)
        switch (tmp) {
        case MULTICAST_MESSAGE:
            isMulticast = true;
            break;
        case UNICAST_MESSAGE:
            isMulticast = false;
            break;
        default:
            return null;					// invalid multicast field       
        }

        // finally create header 
        return new BTHeader(new String(sender), 
                            new String(receiver), length, isMulticast);
    }

    /**
     * Read bytes from input stream until <code>PROLOG</code> is found, then 
     * a header object out of rest of the given <code>InputStream</code> is 
     * created. 
     * 
     * @param in
     *            <code>java.io.InputStream</code> reading the data from.
     * @return returns the BTHeader object read from given InputStream. Returns
     *         <code>null</code> if the header is invalid.
     * @throws IOException if a corresponding exception occurs in InputStream
     */
    public static BTHeader synchroniseAndReadHeader(InputStream in) throws IOException
    {
        boolean syncing = false;
        int good = 0;
        while (good != PROLOG.length()) {
            int data = in.read();
            if (!syncing) {
                if (data == PROLOG.charAt(0)) {
                    syncing = true;
                    good++;
                }
            } else if (syncing) {
                if (data == PROLOG.charAt(good)) {
                    good++;
                } else {
                    // invalid character in PROLOG 
                    // start all over again 
                    syncing = false;
                    good = 0;
                }
            }
        }
        
        // post condition: + + + + | <RECEIVER> | ...
        //                           ^
        //                           | next to read
        return readHeaderWithoutProlog(in);
    }
    
    /** 
     * Read exactly <code>buffer.length</code> byte from <code>in</code> and
     * store data in array <code>buffer</code>.
     * 
     * @param in InputStream to read <code>buffer.length</code> data bytes from 
     * @param buffer buffer to write data to
     * @throws IOException if a corresponding exception is thrown in the 
     *         underlying InputStream. 
     */
    public static void readFully(InputStream in, byte[] buffer)
    			throws IOException 
    {
        int bytesRead = 0;
        while (bytesRead < buffer.length) {
            int count = in.read(buffer, bytesRead, buffer.length-bytesRead);
            if (count == -1) {
                // end of stream is reached before enough bytes could 
                // be read-in
                throw new IOException("Input stream ended prematurely.");
            }
            bytesRead += count;
        }
    }
 
    /**
     * Checks if Bluetooth address is valid. It has to be a 
     * 12 character string in hex encoding.
     * 
     * @param address array containing hex-string ecoded BT address
     * @return <code>true</code> if address is a proper BT address,
     *         <code>false</code> otherwise. 
     */
    public static boolean isBTAddressValid(byte address[]) 
    {
        if (address.length != 12) {
            return false;	// address field does not even match length
        }
        for (int i=0; i<12; i++) {
            char ch = Character.toLowerCase((char)address[i]);            
            if ((ch<'0') || 
                ((ch>'9') && (ch<'a')) ||
                (ch>'f')) {
                // invalid address
                return false;
            }
        }
        return true;
    }
}