/**
 * midas
 * ch.ethz.iks.jxme.msg
 * IMessage.java
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 *
 * 06.06.2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * 
 * $Id: IMessage.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 * */
package ch.ethz.iks.jxme;

import java.io.IOException;
import java.io.OutputStream;

/** This interface defines the methods an message must offer.
 */
public interface IMessage {
	
	String getID();


	/** returns the element of position <code>index</code> in the list of the elements.
	 * @param index position of the element to return
	 * @return element at the given position (parameter <code>index</code>)
	 * */ 
	IElement getElement(int index);
	
	
	/** inserts a given element at the given position in the list of elements
	 * @param element element to be inserted
	 * @param index position to insert the given element in the list of elements
	 *  */
	void setElement(IElement element, int index);
	
	
	/** inserts the given element at any place in the list of elements.
	 * @param element element to be inserted in the list of elements */
	void setElement(IElement element);


	/** returns the number of elements in the current list of elements
	 * @return number of elements in the current message */
	int getElementCount();

	
	/** returns the element having the given string as name.
	 * @return element corresponding to the given name */
	IElement getElement(String name);


	/** returns the number of bytes the message takes to be written to an output stream.
	 * @return number of bytes needed to serialize the message */ 
	int getSize();
	
	
	/** This method writes the message and its elements to the given output stream.
	 * @param out output stream to write the message */
	void writeMessage(OutputStream out) throws IOException;
	
	
	/** returns the message as XML */
	String toXMLString();

}
