/**
 * midas
 * ch.ethz.iks.jxme.bluetooth.impl
 * BTReadThreadConnectionHandle
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 *
 * 22.07.2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * */
package ch.ethz.iks.jxme.bluetooth.impl;

import java.io.IOException;

import javax.bluetooth.RemoteDevice;
import javax.microedition.io.StreamConnection;

import ch.ethz.iks.jxme.bluetooth.IConnectionHandle;


/** This class is representing a connection as the interface <code>IConnectionHandle</code> 
 * specifies. This class offers all methods and fields of its super class 
 * (<code>BTConnectionHandle</code>) and an additional to store the readThread assigned to
 * this connection.
 * @see IConnectionHandle, BTConnectionHandles
 * */
public class BTReadThreadConnectionHandle extends BTConnectionHandle{

	/** thread reading from this connections input stream. */ 
	private ReadThread _readThread = null;
	
	/** Use this constructor to create a connection handle if there is <b>no</b> preexisting
	 * instance of an <code>IConnectionHandle</code>. This constructor tries to 
	 * create a <code>RemoteDevice</code> out of the existing connection. 
	 * @param conn <code>StreamConnection</code>(RFCOMM) to the remote device.
	 * @param readThread thread reading incoming data of this connection 
	 * @throws IOException if the remote device is not correctly reachable
	 * */
	public BTReadThreadConnectionHandle(StreamConnection conn, 
																		ReadThread readThread,
																		boolean isMaster) throws IOException{
		super(conn, isMaster);
		_readThread = readThread;
	}
	
	
	/** Use this constructor to create a connection handle if there is <b>no</b> preexisting 
	 * instance of an <code>IConnectionHandle</code>.
	 * @param conn <code>StreamConnection</code>(RFCOMM) to the remote device.
	 * @param remoteDevice object representing the remote device corresponding to this connection
	 * @param readThread thread reading incoming data of this connection 
	 * @throws IOException if the remote device is not correctly reachable
	 * */
	public BTReadThreadConnectionHandle(StreamConnection conn, 
																		RemoteDevice remoteDevice, 
																		ReadThread readThread,
																		boolean isMaster) throws IOException{
		super(conn, remoteDevice, isMaster);
		_readThread = readThread;
	}
	
	
	/** Use this constructor to create a <code>BTReadThreadConnectionHandle</code> 
	 * if there is already an instance of  an implementation of a <IConnectionHandles>. 
	 * @param conn already existing instance of an implementation of an 
	 * <code>IConnectionHandle</code>
	 * @param readThread <code>ReadThread</code> reading incoming data of this connection
	 * @throws <code>IOException</code> 
	 * */
	public BTReadThreadConnectionHandle(IConnectionHandle conn, 
																		ReadThread readThread) throws IOException{
		super(conn);
		_readThread = readThread;
	}
	
	
	/** This method closes the connection handled by this handle. Additionaly the corresponding 
	 * reading thread will be stopped. To close the connection the stop method of the super class 
	 * <code>BTConnectionHandle</code> is called. 
	 * */
	public void close() throws IOException {
		_readThread.stopThread();
		super.close();
	}

}
