/**
 * midas
 * ch.ethz.iks.jxme.bluetooth
 * ConnectionHandleIf.java
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 *
 * 26.06.2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * */
package ch.ethz.iks.jxme.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.bluetooth.RemoteDevice;
import javax.microedition.io.StreamConnection;


/** This is the interface definition of an Object binding a connection to an identifier.  */
public interface IConnectionHandle {
	
	/** @return javax.microedition.ioStreamConnection of this ConnectionHandle */
	public StreamConnection getConnection();
	
	/** @return Identifier of this ConnectionHandle */
	public String getIdentifier();
	
	/** @return OutputStream corresponding to Connection managed by this ConnectionHandle */
	public OutputStream openOutputStream() throws IOException;
	
	/** @return InputStream correspondign to Connection managed by this ConnectionHandle */
	public InputStream openInputStream() throws IOException;
	
	/** @return returns the Bluetooth device address of the connected remote device as String */
	public String getRemoteBTAddress();
	
	/** @return returns the locat Bluetooth device address as String */
	public String getLocalBTAddress();
	
	/** This method closes the connection represented by this connection handle.*/
	public void close() throws IOException;
	
	/** This method returns the representation of the remote device.
	 * @return remote devices's representation
	 * */
	public RemoteDevice getRemoteDevice();
	
	/** This method returns a flag that indicates if the local device has the master 
	 * role in the connection represented in this connection handle.
	 * <br><b>Node:</b> This is just an estimation since the JSR-82 doesn't offer any method to 
	 * access this bit of information.
	 * @return <code>true</code> if the local device is master, <code>false</code> else */
	public boolean isMaster();
}
