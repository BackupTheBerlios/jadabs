/**
 * midas
 * ch.ethz.iks.jxme.bluetooth.impl
 * BTEndpointServer.java
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 *
 * 03.07.2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * */
package ch.ethz.iks.jxme.bluetooth.impl;

import java.io.IOException;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import org.apache.log4j.Logger;

import ch.ethz.iks.jxme.bluetooth.IConnectionPool;


/** BTEndpointServer is a server routine listening for incoming RFComm connections.
 * implements a singleton. To get the current instance of the <code>BTEndpointServer</code>
 * call the method <code>getBTEndpointServer()</code> */
public class BTEndpointServer implements Runnable {

	static Logger LOG = Logger.getLogger(BTEndpointServer.class.getName());
	
	private static BTEndpointServer _server = null;
	
	private String _serviceURL = "btspp://localhost:8800;name=JxmeBtServer";
	//private String _serviceURL = "btspp://localhost:" + new UUID("8800", true) + ";name=JxmeBtServer";
	private IConnectionPool _connections = null;
	private boolean _newConnection = false;
	StreamConnectionNotifier _service = null;
	
	/** Starts the <code>BTEndpointServer</code> so the instantiating class hasn't to
	 * care about starting the server. Since the server implements the singleton pattern
	 * the constructor is private. To get a instance of the server call get method
	 * <code>getBTEndpointServer</code>*/
	private BTEndpointServer( IConnectionPool connections ){
		_connections = connections;
		if(LOG.isInfoEnabled()){
			try {
				LOG.info("Local device " + 
									LocalDevice.getLocalDevice().getFriendlyName() + " [" +
									LocalDevice.getLocalDevice().getBluetoothAddress() + "]");
			} catch (BluetoothStateException e) {
				LOG.fatal("Can't get local device", e);
			}
		}
	}
	
	
	/** Since the class <code>BTEndpointServer</code> implements a singleton
	 * you need to call this method to get an instance.
	 * @param connections specifies the connection pool managing the connections.
	 *  */
	public static BTEndpointServer getBTEndpointServer( IConnectionPool connections ){
		if( _server == null){
			_server = new BTEndpointServer( connections );
		}
		return _server;
	}
	
	/** This method starts the thread listening for incoming connections. */
	public void connect() {
		new Thread(this).start();
	}
	
	
	/** This method will stop the server as soon as possible.
	 * @throws IOException
	 */
	public void stopServer() throws IOException{
		_service.close();
	}
	

	/** This thread is waiting for incoming connections. It will accept <b>any</b>
	 * incoming connection.
	 */
	public void run() {
		
		try {
			_service =
				(StreamConnectionNotifier) Connector.open(_serviceURL);
			if (LOG.isInfoEnabled()){
				LOG.info("start service: " + _serviceURL);
			}
		} catch (IOException e) {
			LOG.fatal("Can't open StreamConnectionNotifier: stop BTEndpointServer" + e.getLocalizedMessage() );
		}
		while( true ){
			try {
				StreamConnection conn = _service.acceptAndOpen();
				RemoteDevice remoteDevice = RemoteDevice.getRemoteDevice(conn);
				BTConnectionHandle btConnHandle = new BTConnectionHandle(conn, remoteDevice, false); 
				_connections.addConnection( btConnHandle );
				synchronized( this ){
					_newConnection = true;
					this.notifyAll();
				}
				if( LOG.isDebugEnabled() ){
					LOG.debug("connected: " + btConnHandle.getLocalBTAddress() + " -> " + btConnHandle.getRemoteBTAddress());
				}
			} catch (IOException e1) {
				LOG.error("Can't open connection: " + e1.getLocalizedMessage());
				e1.printStackTrace();
			}
		}
	}

}
