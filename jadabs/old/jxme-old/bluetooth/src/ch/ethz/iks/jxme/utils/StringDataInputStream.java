/**
 * JXMEbt
 * ch.ethz.iks.utils
 * StringDataInputStream.java
 * 
 * @author Daniel Kaeppeli, danielka@student.ethz.ch
 *
 * 23.06.2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * */
package ch.ethz.iks.jxme.utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;


public class StringDataInputStream extends DataInputStream {
	
	public StringDataInputStream(InputStream in){
		super(in);
	}
	
	public String readString(int length) throws IOException {
		int currentByte;
		byte[] bytes = new byte[length];
		
		for (int index = 0; index < length; index++){
			currentByte = in.read();
			if (currentByte < 0){
				throw new IOException("invalid string size");
			}
			bytes[index] = (byte) currentByte;	
		}
		return new String(bytes);
	}
	
}
