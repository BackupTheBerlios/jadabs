/**
 * midas
 * ch.ethz.iks.jxme.bluetooth.impl
 * BTPackage.java
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 *
 * Jul 10, 2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * */
package ch.ethz.iks.jxme.bluetooth.impl;


/** This class is a wrapper class wrapping a raw message (<code>byte[]</code>) and the corresponding header and footer. */
public class BTPackage {
	private BTHeader _header = null;
	private BTFooter _footer = null;
	private byte[] _message = null;
	private String _incomingEdge = null;
	
	
	/** Default constructor
	 * @param header header object of this message
	 * @param rawMessage message as array of bytes
	 * @param footer footer object of this message
	 *  */
	public BTPackage(BTHeader header, byte[] rawMessage, BTFooter footer, String incomingEdge){
		_header = header;
		_message = rawMessage;
		_footer = footer;
		_incomingEdge = incomingEdge;
	}
	
	/** Getter of incoming edge */
	public String getIncomingEdge(){
		return _incomingEdge;
	}
	
	/** Getter of the header */
	public BTHeader getHeader(){
		return _header;
	}
	
	
	/** Getter of the footer */
	public BTFooter getFooter(){
		return _footer;
	}
	
	
	/** getter of the message */
	public byte[] getMessage(){
		return _message;
	}
	
	
	/** transforms the row message to a string */
	public String messageToString(){
		StringBuffer buffer = new StringBuffer();
		for( int i = 0; i < _message.length; i++){
			if( _message[i] == 0x0 ){
				buffer.append(" ");
			} else {
				buffer.append(_message[i]);
			}
		}
		return buffer.toString();
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("Sender:\t");
		buffer.append( getHeader().getSender());
		buffer.append("\nReceiver:\t");
		buffer.append(getHeader().getReceiver());
		buffer.append("\nPayload length:\t");
		buffer.append(getHeader().getPayloadLength());
		buffer.append("\nMessage:\n");
		buffer.append(messageToString());
		
		return buffer.toString();
	}
	
}
