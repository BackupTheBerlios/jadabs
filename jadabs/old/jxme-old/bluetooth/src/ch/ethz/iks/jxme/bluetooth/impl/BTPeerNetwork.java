/**
 * midas
 * ch.ethz.iks.jxme.bluetooth.impl
 * BTPeerNetwork.java
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 *
 * 30.06.2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * 
 * $Id: BTPeerNetwork.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 * */
package ch.ethz.iks.jxme.bluetooth.impl;

import java.io.IOException;

import org.apache.log4j.Logger;

import ch.ethz.iks.jxme.IMessage;
import ch.ethz.iks.jxme.IMessageListener;
import ch.ethz.iks.jxme.bluetooth.IConnectListener;
import ch.ethz.iks.jxme.bluetooth.IConnectionFilter;
import ch.ethz.iks.jxme.bluetooth.IConnectionPool;
import ch.ethz.iks.jxme.bluetooth.IDisconnectListener;
import ch.ethz.iks.jxme.impl.PeerNetwork;

/** This class implements the interface <code>ch.ethz.iks.jxme.msg.IPeerNetwork</code> and 
 * allows the send messages over a Bluetooth network. */
public class BTPeerNetwork extends PeerNetwork {

	private static Logger LOG =Logger.getLogger( BTPeerNetwork.class.getName() );
	
	public static final  int IS_RENDEZ_VOUS_PEER = BTEndpoint.IS_CLIENT;
	public static final int IS_PEER = BTEndpoint.IS_SERVER;
	public static final int IS_BOTH = BTEndpoint.IS_CLIENT_AND_SERVER;
	
	// constants defining the roles the local device might have
	public static final int NO_CONNECTION = IConnectionPool.NO_CONNECTION;
	public static final int MASTER = IConnectionPool.MASTER;
	public static final int SLAVE = IConnectionPool.SLAVE;
	public static final int MASTER_AND_SLAVE = IConnectionPool.MASTER_AND_SLAVE;
	
	private static IConnectionPool _bluetoothConnections = null;
	private BTEndpoint _endpoint = null;
	private boolean _isRendezVousPeer = false;


	/** Constructor starting a peer. Each peer can be a rendez-vous peer or not.
	 * To start a rendez-vous peer set the parameter 
	 * <code>isRendezVousPeer</code> to <code>true</code>. If a peer is a 
	 * rendez-vous peer it can be started as service periodically searching for
	 * new devices and it's services. To do so specify a timeout (in milliseconds)
	 * that is larger than zero. Specifying a timeout larger than zero you must not
	 * call the <code>connect()</code> method. Specifying a timeout smaller than
	 * zero the <code>connect()</code> method has to be invoked manually and 
	 * is blocking until the inquiry and the service discovery process is terminated. 
	 * @param isRendezVous set this parameter to <code>true</code> to start a 
	 * rendez-vous peer else <code>false</code>
	 * @param inqTimeout timeout in millisecond between two inquiry procedures. If
	 * parameter <code>isRendezVous</code> is set to <code>false</code> this 
	 * parameter has not any effect. If this peer is a rendez-vous peer the timeout 
	 * must be larger than zero to start a service.
	 * */
	public BTPeerNetwork(boolean isRendezVousPeer, int inqTimeout){
		int mode = -1;
		_bluetoothConnections = BTConnectionPool.getConnectionPool();
		
//		Sorry for that, I have to rename the constants in IS_Client and IS_Server in BTEndpoint.
		if( isRendezVousPeer ){
			mode = BTEndpoint.IS_CLIENT;
		} else {
			mode = BTEndpoint.IS_SERVER;
		}
		
		_endpoint = new BTEndpoint(mode, this, inqTimeout);
	}
	
	/**
	 * @param specifies the time out to wait between two inquiry processes, set to 0 implies infinit timeout.
	 * @param mode mode might have one of the following values: {@link BTEndpoint#IS_CLIENT IS_CLIENT},
	 * {@link BTEndpoint#IS_CLIENT IS_SERVER}, or {@link BTEndpoint#IS_CLIENT IS_CLIENT_AND_SERVER}
	 *  */
	public BTPeerNetwork(int mode, int inqTimeout){
		_bluetoothConnections = BTConnectionPool.getConnectionPool();
		
		_endpoint = new BTEndpoint(mode, this, inqTimeout);
	}
	

	/** Constructor starting a peer that can be either rendez-vous or a regular peer. Set 
	 * the parameter <code>isRendezVous</code> to <code>false</code> to start a 
	 * regular peer and to <code>true</code> to start a rendez-vous peer. The method
	 * <code>connect</code> has to be called manually, there will not be started any service.
	 * @param isRendezVousPeer set this parameter to <code>true</code> to start a 
	 * rendez-vous peer and else to <code>false</code>.
	 * */
	public BTPeerNetwork(boolean isRendezVousPeer){
		
		this(isRendezVousPeer, -1);
	}


	/** This constructor creates a RendezVousPeer inquirying its environment perodically.
	 * @param timeout timeout between to inquirys in milliseconds */
	public BTPeerNetwork(int timeout){
		this(true, timeout);
	}


	/** This method will establish connections to available peers. If the current peer
	 * is a rendez-vous peer this method is blocking until device and service discovery
	 * is terminated. If the current peer is a regular peer a 
	 * <code>NoPeerAvailableException</code> will never be thrown.
	 * @throws NoPeerAvailableException if there is no regular peer in the device's range.
	 * @return if the call was successful the return value is not <code>null</code>.
	 */
	public void connect() throws IOException {
		
		_endpoint.connect();
	}

	public void connect(int mode) throws IOException{
		
		// validating parameter
		if(mode == BTEndpoint.IS_CLIENT ||
			mode == BTEndpoint.IS_SERVER ||
			mode == BTEndpoint.IS_CLIENT_AND_SERVER){
			
			// delegate call
			_endpoint.connect(mode);
		
		} else {
			throw new IllegalArgumentException(
				"BTPeerNetwork.connect(int mode) requiers an int " + 
				" which values are: BTEndpoint.IS_CLIENT, BTEndpoint.IS_SERVER, and BTEndpoint.IS_CLIENT_AND_SERVER");
		}
	}

	/** This method sends a message by broadcast to all available devices, since
	 * there is - upt o now - no routing implemented. The parameters <code>pipe_name</code>, 
	 * <code>pipe_id</code>, and <code>pipe_type</code> are ignored!
	 * @return 0 send successful
	 * @return -1 IOException
	 * @return -2 NoPeerAvailableException
	 */
	public int send(
		String pipe_name,
		String pipe_id,
		String pipe_type,
		IMessage message) {
		
		try {
			_endpoint.sendMulticast(message, null);
		} catch (IOException e) {
			LOG.fatal("Can't send message (IOException)", e);
			return -1;
		}
		return 0;
	}


	/** @return <code>true</code> if the current peer is a rendez-vous peer else 
	 * <code>false</code>.
	 */
	public boolean isRendezVousServer() {
		return _endpoint.isRendezVousServer();
	}


	/** Sends a message <code>data</code> to the specified recipient <code>id</code>. 
	 * If the specified recipient <code>id</code> is <code>null</code> then the message
	 * is sent out as multicast to the default multicast group.
	 * @param id recipients identifier, by default the recipients Bluetooth device address
	 * as <code>java.lang.String</code> (12 chars, 0-9A-F). If <code>id</code> is 
	 * <code>null</code> the message is sent out as multicast to the default multicast group.
	 * @return always 0 (idea: compatibility to JXME: was probably a bad idea)
	 */ 
	public synchronized void send(String id, IMessage data) throws IOException {
			if( id == null ){
				_endpoint.sendMulticast(data, "default");
			} else {
				_endpoint.sendMessage(id, data);
			}
	}


	/** Adds a message listener to this peer. The listener's <code>process</code> method
	 * will be called each time a message has received. 
	 * @param msgListener listener to be added
	 */
	public void addMessageListener(IMessageListener msgListener) {
		super.addMessageListener(msgListener);
	}


	/** Removes the given listener from the list of the listeners.
	 * @param msgListener listener to be removed
	 */
	public void removeMessageListener(IMessageListener msgListener) {
		super.removeMessageListener(msgListener);
		
	}

	
	/** Returns the number of active connections this peer has.
	 * @return number of active connections */
	public int numberOfConnections(){
		return _endpoint.numberOfConnections();
	}
	
	
	/** This method creates an Instance of an Implementation of the interface 
	 * <code>ch.ethz.iks.jxme.msg.IMessage</code> that can be transmitted by this kind of peer.
	 * @return empty message
	 *  */
	public IMessage createMessage(){
		IMessage tmp = _endpoint.createMessage();
		return tmp;
	}


	/** The listener added by this method will be called if there has been established a new connection.
	 * The event passed to the listener contains the Bluetooth address and the user friendly name
	 * of the remote device.
	 * @param listener listener to be called if there is a new connection
	 */
	public void addConnectListener(IConnectListener listener){
		_bluetoothConnections.addConnectListener(listener);
	}
	
	
	/** Removes a given listener from list of connectListeners.
	 * @param listener listener to be removed
	 */
	public void removeConnectListener(IConnectListener listener){
		_bluetoothConnections.removeConnectListener(listener);
	}
	
	
	/** The listener added by this method will be called if a connection has been closed.
	 * The event passed to the listener contains the Bluetooth address and the user friendly name
	 * of the remote device that was removed.
	 * @param listener listener to be called if a connection was removed
	 */
	public void addDisconnectListener(IDisconnectListener listener){
		_bluetoothConnections.addDisconnectListener(listener);
	}
	
	
	/** Removes a given listener from list of disconnectListeners.
	 * @param listener listener to be removed
	 */
	public void removeDisconnectListener(IDisconnectListener listener){
		_bluetoothConnections.removeDisconnectListener(listener);
	}
	
	
	/** Closes all connections. */
	public void closeAllConnections(){
		_bluetoothConnections.closeAll();
	}
	
	
	/** Stopps the current peer. All connections are closed and all threads are
	 * stopped. */
	public void stop(){
		_endpoint.stop();
	}
	
	public void applyFilter(IConnectionFilter filter){
		_endpoint.applyFilter(filter);
	}
	
	public void withdrawFilter(IConnectionFilter filter){
		_endpoint.withdrawFillter();
	}
	
	/** retruns an int representing the role of the local BT device, possible values are
	 * 	{@link #NO_CONNECTION NO_CONNECTION}, 
	 *  	{@link #MASTER MASTER}, 
	 * 	{@link #SLAVE SLAVE}, and 
	 * 	{@link #MASTER_AND_SLAVE MASTER_AND_SLAVE} */
	public int getRole(){
		return _endpoint.getRole();
	}
	
	/** This method returns the value of the role as string representation.
	 * @return <code>java.lang.String</code> representation of the current role */
	public String getRoleString(){
		int role = _endpoint.getRole();
		
		switch(role){
			case NO_CONNECTION:
				return "NO CONNECTION";
			
			case MASTER:
				return "MASTER";

			case SLAVE:
				return "SLAVE";
				
			case MASTER_AND_SLAVE:
				return "MASTER AND SLAVE";
				
			default:
				return "status information about the role isn't valid!";
		}

	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jxme.IPeerNetwork#create(java.lang.String, java.lang.String, java.lang.String)
	 */
	public int create(String type, String name, String arg) {
		// TODO Auto-generated method stub
		return 0;
	}
}	
