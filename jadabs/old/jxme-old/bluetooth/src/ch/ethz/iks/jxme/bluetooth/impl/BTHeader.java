/**
 * JXME[dk]
 * ch.ethz.iks.jxme.bluetooth.impl
 * BTHeader.java
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 *
 * Jul 9, 2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * */
package ch.ethz.iks.jxme.bluetooth.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** This class represents the header of a message. It offers method to read a header from a InputStream and write the header informations to a OutputStream. */
public class BTHeader {

	/** The preamble is sent before the header starts */
	public static final String PROLOG ="++++";
	
	private String _receiver = null;
	
	private String _sender = null;
	
	private int _length = -1;
	
	private boolean _isMulticast = false;
	
	/** Creates a header object of the given parameter list.
	 * @param sender sender's Bluetooth device address as String
	 * @param receiver receiver's Bluetooth device address as String
	 * @param payLoadLength length of the data written/read to/from the
	 * stream.
	 * @param isMulitcast set this flag to <code>true</code> to indicate
	 * that this message is a multicast message. 
	 *  */
	public BTHeader(String sender, String receiver, int payLoadLength, boolean isMulticast){
		_receiver = receiver;
		_sender = sender;
		_length = payLoadLength;
		_isMulticast = isMulticast;
	}
	
	
	/** 
	 * @return Bluetooth device address of the receiver as <code>java.lang.String</code>, this must not be the localhosts Bluetooth address.
	 * */
	public String getReceiver(){
		return _receiver;
	}
	
	/**
	 *  @return  Bluetooth device address of the sender as <code>java.lang.String</code>. 
	 * */
	public String getSender(){
		return _sender;
	}
	
	/** This method returns the length of the payload contained in the following package.
	 * @return payload length in bytes */
	public int getPayloadLength(){
		return _length;
	}
	
	/** This method returns a <code>boolean</code> indicating if the current message is a multicast or not.
	 * @return 
	 * */
	public boolean isMulticast(){
		return _isMulticast;
	}
	

	/** Writes the header specified by this object to the given output stream</code>
	 * @param out <code>java.io.OutputStream</code> to write the header.
	 * @throws IOException if the write operation fails.
	 *  */
	public void writeHeader(OutputStream out) throws IOException{
		for( int i = 0; i < PROLOG.length(); i++){
			out.write( PROLOG.charAt(i) );
		}
		
		out.write(_receiver.getBytes());
		
		out.write(_sender.getBytes());
		
		// print lenth of payload - 4 bytes
		for( int i = 0; i < 4; i++){
			out.write( (_length >>> i*8) & 0xFF );
		}
		// print multicast flag - 1 byte
		if( _isMulticast ){
			out.write(10);
		} else {
			out.write(20);
		}
	}
	
	/** This method reads the header of a message without the leading <code>PROLOG</code>
	 *  @return returns the BTHeader object read from given InputStream. Returns <code>null</code> if the header is invalid.
	 */
	public static BTHeader readHeaderWithoutProlog( InputStream in ) throws IOException{
		byte[] receiver = new byte[12];
		byte[] sender = new byte[12];
		int length = 0;
		boolean isMulticast;
		
		if( in.read( receiver ) != 12){
			return null;
		}
		
		if( in.read( sender ) != 12 ){
			return null;
		}
		
		for( int i = 0; i < 4; i++){
			int tmp = in.read();
			length = length | (  (tmp << i * 8) & (0xFF << i * 8) );
		}
		
		int tmp = in.read();
		if( tmp == 10 ){
			isMulticast = true;
		} else {
			if( tmp == 20 ){
				isMulticast = false;
			} else {
				return null;
			}
		}
		
		return new BTHeader(new String(sender), new String(receiver), length, isMulticast);	
	}
	
	/** Creates a header object out of the given <code>InputStream</code>.
	 * @param in <code>java.io.InputStream</code> reading the data from.
	 * @return returns the BTHeader object read from given InputStream. Returns <code>null</code> if the header is invalid.
	 * */
	public static BTHeader readHeader( InputStream in ) throws IOException{

		
		for( int i = 0; i < PROLOG.length(); i++){
			if( PROLOG.charAt( i ) != in.read() ){
				return null;
			}
		}
		return readHeaderWithoutProlog( in );
	}	
		
}
