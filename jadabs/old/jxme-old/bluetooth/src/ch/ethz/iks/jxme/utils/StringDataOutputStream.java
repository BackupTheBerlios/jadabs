/**
 * JXMEbt
 * ch.ethz.iks.utils
 * StringDataOutputStream.java
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 *
 * 20.06.2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * */
package ch.ethz.iks.jxme.utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/** This class extends <code>java.io.DataOutputStream</code> by the methods
 *   <code>writeString(String string)</code> and <code>String readString()</code> */
public class StringDataOutputStream extends DataOutputStream {

	public StringDataOutputStream(OutputStream out) {
		super(out);
	}
	
	public void writeString(String string) throws IOException{
		for( int index=0; index < string.length(); index++ ){
			super.write( string.charAt( index ) );
		}
		return;
	}

}
