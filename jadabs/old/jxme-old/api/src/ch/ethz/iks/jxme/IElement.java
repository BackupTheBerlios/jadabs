/**
 * midas
 * ch.ethz.iks.jxme.msg
 * IElement.java
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
 * $Id: IElement.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 * */
package ch.ethz.iks.jxme;

import java.io.IOException;
import java.io.OutputStream;

/** * An implementation of this Interface must also have a static method to create an <code>Element</code>:<br>
 * <code>public static myElementImpl createElement(byte[] data, String[] nameSpaces)</code>
 */
public interface IElement {
	
	/** This method returns the elements payload as array of <code>bytes</code>. 
	 * @return data contained in this element */
	byte[] getData();
	
	/** This method returns the elements payload as an <code>int</code>. 
	 * @return data contained in this element */
	int getIntData();

	/** This method returns the element's mimetype as <code>java.lang.String</code>.
	 *  @return mimetype of the data */
	String getMimeType();

	/** returns the element's name as <code>java.lang.String</code>.
	 * @return name of the element */
	String getName();

	/** returns the element's namespace as <code>java.lang.String</code>.
	 * @return namespace of this element */
	String getNameSpace();

	/** returns the element as string */
	String toString();
	
	/** returns the element as XML string */
	String toXMLString();
	
	/** returns the number of bytes used to serialize this element. 
	 * @return size of this element */
	int getSize();
	
	/** writes the element itself to the given output stream
	 * @param out output stream to write
	 * @param nameSpaces Since the namespaces are defined in the message's header and referenced
	 * by the element each element must have the list of namespaces. The namespace is encoded as number
	 * representing the namespace's position in the array <code>nameSpaces</code>. To get more information
	 * about the JXME encoding have a look at the documentation of JXME or in the report of this diploma thesis. */
	void write(OutputStream out, String[] nameSpaces) throws IOException;
}
