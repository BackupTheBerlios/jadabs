/**
 * midas
 * ch.ethz.iks.jxme.bt
 * ElementParseException.java
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 *
 * 23.06.2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * 
 * $Id: ElementParseException.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 * */
package ch.ethz.iks.jxme.impl;

/** This exception will be thrown if the received element's binary data are corrupted. */
public class ElementParseException extends Exception {

	/** Constructor to set exceptions message.
	 * @param string exeption's message
	 */
	public ElementParseException(String string) {
		super(string);
	}
	
	/** Default constructor
	 */
	public ElementParseException(){
		super();
	}
}
