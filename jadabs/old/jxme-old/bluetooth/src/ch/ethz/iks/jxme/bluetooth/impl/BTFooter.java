/**
 * midas
 * ch.ethz.iks.jxme.bluetooth.impl
 * BTFooter.java
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

/** This class is representing the footer of packages. A footer does not contain any data (up to now).
 * It is simply used to read a control sequence (epilog) checking if the given length of the message 
 * is correct. */
public class BTFooter {

	/** sequence marking the footer of a message. */
	public static final String EPILOG = "----";
	
	/** writes the <code>EPILOG</code> to the given <code>java.io.OutputStream</code> 
	 * @throws IOException */
	public void  writeFooter( OutputStream out ) throws IOException{
		for( int i = 0; i < EPILOG.length(); i++ ){
			out.write(EPILOG.charAt( i ));
		}
	}
	
	/** reads the <code>EPILOG</code> from the given <code>java.io.InputStream</code>
	 * @throws IOException
	 * */
	public static BTFooter readFooter(InputStream in) throws IOException{
		for( int i = 0; i < EPILOG.length(); i++ ){
			if( in.read() != EPILOG.charAt( i) ){
				return null;
			}
		}
		return new BTFooter();
	}
}
