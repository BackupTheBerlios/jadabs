/**
 * midas
 * ch.ethz.iks.jxme.bluetooth.impl
 * BTConnectionHandle.java
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
package ch.ethz.iks.jxme.bluetooth.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.StreamConnection;

import org.apache.log4j.Logger;

import ch.ethz.iks.jxme.bluetooth.IConnectionHandle;


/** This class implements the interface IConnectionHandle and is designed
 * to store the information according to a connection.  */ 
public class BTConnectionHandle implements IConnectionHandle {
	
	private static Logger LOG = Logger.getLogger(BTConnectionHandle.class.getName());

	private StreamConnection _connection = null;
	private RemoteDevice _remoteDevice = null;
	private static LocalDevice _localDevice = null;
	private InputStream _in = null;
	private OutputStream _out = null;
	private boolean _isMaster = false;

	
	/** This constructor creates a connection handle out of a 
	 * <code>javax.microedition.io.StreamConnection</code>. Since it tries to reach
	 * the remote device to get the user friendly name the remote device must be connected.
	 * @param connection javax.microedition.io.StreamConnection
	 * @throws IOException if the connection is closed or the LocalDevice is not accessible.
	 *  */
	public BTConnectionHandle(StreamConnection connection, boolean isMaster) throws IOException{
		if( _localDevice == null ){
			_localDevice = LocalDevice.getLocalDevice();
		}

		_isMaster = isMaster;
		
		if( connection == null ){
			LOG.fatal("conneciton is null");
			throw new NullPointerException("connection is not allowed to be null");
		}

		_remoteDevice = RemoteDevice.getRemoteDevice( connection );
		_connection = connection;
		if( LOG.isDebugEnabled() ){
			LOG.debug("creating ConnectionHandle\n");
			LOG.debug(_localDevice.getBluetoothAddress() + " -> " + _remoteDevice.getBluetoothAddress());
		}
	}
	

	/** This constructor is similar to the above one, expect the remote device's object is given and 
	 * hasn't to be extracted out of the conneciton.
	 * @param connection Serial Port Profile connection, L2CAP connection, 
	 * or OBEX over RFCOMM connection. 
	 * <b>Type of connection is not checked!!!</b>
	 * @param remoteDevice javax.bluetooth.RemoteDevice
	 * */
	public BTConnectionHandle(StreamConnection connection, RemoteDevice remoteDevice, boolean isMaster) throws IOException{
		if( _localDevice == null ){
			_localDevice = LocalDevice.getLocalDevice();
		}
		
		_isMaster = isMaster;
		
		if( connection == null ){
			LOG.fatal("conneciton is null");
			throw new NullPointerException("connection is not allowed to be null");
		}
		if( remoteDevice == null ){
			LOG.fatal("remote device is null");
			throw new NullPointerException("connection is not allowed to be null");
		}
		
		_remoteDevice = remoteDevice;
		_connection = connection;
		
		if( LOG.isDebugEnabled() ){
			LOG.debug("creating ConnectionHandle");
			LOG.debug(_localDevice.getBluetoothAddress() + " -> " + _remoteDevice.getBluetoothAddress());
		}
	}

	/** This constructor is designed to offer the possiblity to subclasses the create new objects also if
	 * there also preexisting instances of any implementation of <code>IConnectionHandle</code>.
	 * @param conn preexisting <code>IConnectionHandle</code>
	 *  */
	protected BTConnectionHandle(IConnectionHandle conn) throws IOException{
		_isMaster = conn.isMaster();
		_in = conn.openInputStream();
		_out = conn.openOutputStream();
		_connection = conn.getConnection();
		_localDevice = LocalDevice.getLocalDevice();
		_remoteDevice = conn.getRemoteDevice();
	}


	/** This method returns the javax.microedition.io.Connection stored 
	 *   in this handle.
	 * @return javax.microedition.io.Connection
	 *  @see ch.ethz.iks.jxme.bluetooth.ConnectionHandle#getConnection()
	 */
	public StreamConnection getConnection() {
		return _connection;
	}

	/** This method returns the Bluetooth address of the remote device as String
	 * The Bluetooth address will be 12 characters long. 
	 * Valid characters are 0-9 
	 * and A-F. This method will never return <code>null</code>.
	 * @return the Bluetooth address of the remote device as java.lang.String. 
	 * @see ch.ethz.iks.jxme.bluetooth.ConnectionHandle#getIdentifier()
	 */
	public String getIdentifier() {
		try {
			return _remoteDevice.getFriendlyName(true);
		} catch (IOException e) {
			LOG.fatal("Can not read friendly name of [" + _remoteDevice.getBluetoothAddress() + 
								"] this connection is identified by the device's b  BT_ADDR." , e);
		}
		return _remoteDevice.getBluetoothAddress();
	}

	/** Opens an java.io.OutputStream corresponding to this connection
	 * @return java.io.OutputStream
	 * @see ch.ethz.iks.jxme.bluetooth.ConnectionHandle#openOutputStream()
	 */
	public OutputStream openOutputStream() throws IOException {
		if( _out == null ){
			_out = _connection.openOutputStream();
		}
		return _out;
	}

	/** Opens an java.io.InputStream corresponding to this conneciton
	 * @return java.io.InputStream
	 * @see ch.ethz.iks.jxme.bluetooth.ConnectionHandle#openInputStream()
	 */
	public InputStream openInputStream() throws IOException {
		if( _in == null ){
			_in = _connection.openInputStream();
		}
		return _in;
	}

	/** This method returns the Bluetooth device address of the remote device as String. 
	 * @return remote address as String (12 chars: 0-9A-F)
	 * @see ch.ethz.iks.jxme.bluetooth.IConnectionHandle#getRemoteBTAddress()
	 */
	public String getRemoteBTAddress() {
		return _remoteDevice.getBluetoothAddress();
	}

	/** This method returns the local Bluetooth device address as String.
	 * @return local address as String (12 chars: 0-9A-F)
	 * @see ch.ethz.iks.jxme.bluetooth.IConnectionHandle#getLocalRemoteBTAddress()
	 */
	public String getLocalBTAddress() {
		return _localDevice.getBluetoothAddress();
	}

	/** this method closes the existing input and output streams and also the underlaying 
	 * <code>javax.microedition.io.StreamingConnection</code>. */
	public void close() throws IOException{
		if(_out != null){
			_out.flush();
			_out.close();
		}
		
		if(_in != null){
			_in.close();
		}
		
		if(_connection != null){
			_connection.close();
		}
	}

	/* This method returns the object representing the remote device of this connection.
	 *  @return returns the object representing the remote device
	 */
	public RemoteDevice getRemoteDevice() {
		return _remoteDevice;
	}
	
	public boolean isMaster(){
		return _isMaster;
	}
}
