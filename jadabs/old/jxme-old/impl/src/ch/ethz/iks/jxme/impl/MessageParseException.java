/**
 * midas
 * ch.ethz.iks.jxme.msg
 * MessageParseException.java
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 *
 * 24.06.2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * 
 * $Id: MessageParseException.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 * */
package ch.ethz.iks.jxme.impl;


public class MessageParseException extends Exception {

	public MessageParseException(){
		super();
	}

	public MessageParseException(String string) {
		super(string);
	}

}
