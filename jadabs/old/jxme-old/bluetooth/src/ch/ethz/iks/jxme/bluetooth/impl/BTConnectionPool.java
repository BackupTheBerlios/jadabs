/**
 * midas
 * ch.ethz.iks.jxme.bluetooth.impl
 * BTConnectionPool.java
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
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.Connection;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.ethz.iks.jxme.bluetooth.ConnectEvent;
import ch.ethz.iks.jxme.bluetooth.DisconnectEvent;
import ch.ethz.iks.jxme.bluetooth.IConnectListener;
import ch.ethz.iks.jxme.bluetooth.IConnectionHandle;
import ch.ethz.iks.jxme.bluetooth.IConnectionPool;
import ch.ethz.iks.jxme.bluetooth.IDisconnectListener;

/** The class BTConnectionPool manages, this class is a Singleton. */
public class BTConnectionPool implements IConnectionPool {
	
	public static Logger LOG = Logger.getLogger( BTConnectionPool.class.getName() );
	
	/**  */
	private static BTConnectionPool _connectionPool = null;
	private static ConnectQueue _connectQueue = null;
	private static DisconnectQueue _disconnectQueue = null;

	/** HCI v16.x --- actual devices mai-2003 --- supports max 7 connection at time
	 * @see  http://sourceforge.net/mailarchive/message.php?msg_id=4169685 
	 * 
	 * ID -> ConnectionHandle
	 * */
	private static Hashtable _connections = new Hashtable(7);
	
	/** This hashtable is needed to acces a connection handle where only the MAC address is known.  
	 * 
	 * MAC -> ID
	 * */
	private static Hashtable _mac2id = new Hashtable();

	/** Vice versa to <code>_mac2id</code>  
	 * 
	 * ID -> MAC
	 * */
	private static Hashtable _id2mac = new Hashtable();


	/** Since this class implements a singleton patter it's constructor is <code>private</code>
	 * */
	private BTConnectionPool(){
		if(_connectQueue == null){
			_connectQueue = new ConnectQueue();
		}
		if(_disconnectQueue == null){
			_disconnectQueue = new DisconnectQueue();
		}
	}	
	
	
	/** To get a reference to the connection pool call this static method.
	 * @return BTConnectionPool of this VM */
	public synchronized static IConnectionPool getConnectionPool(){
		if( _connectionPool == null ){
			_connectionPool = new BTConnectionPool();
		}
		return _connectionPool;
	}
	
	
	/** 
	 * @return Iterator to iterate over all ConnectionHandles stored in the ConnectionPool
	 * @see ch.ethz.iks.jxme.bluetooth.ConnectionPool#getAllConnections()
	 */
	public Enumeration getAllConnections() {
		return _connections.elements();
	}


	/** This method adds a ConnectionHandle to the ConnectionPool
	 * @see ch.ethz.iks.jxme.bluetooth.ConnectionPool#addConnection(ch.ethz.iks.jxme.bluetooth.ConnectionHandle)
	 */
	public synchronized void addConnection(IConnectionHandle connection) {
		String id = null;
		String mac = null;
		
		// add connection to connection pool
		_connections.put(connection.getIdentifier(), connection);
		
		id = connection.getIdentifier();
		mac = connection.getRemoteBTAddress();
		
		_mac2id.put(mac, id);
		_id2mac.put(id, mac);
		
		if( LOG.isDebugEnabled() ){
			LOG.debug("added connection: " + connection.getLocalBTAddress() + " -> " + connection.getRemoteBTAddress());
		}
		
		// throw new connect event
		try {
			_connectQueue.putEvent(new ConnectEvent(connection.getRemoteBTAddress(), connection.getIdentifier()));
		} catch (InterruptedException e) {
			LOG.error("Can't notify connectListeners", e);
		}
		if( LOG.isDebugEnabled() ){
			logConnections(Level.DEBUG);
		}
	}


	/** This method returns a ConnectionHandle corresponding to the given identifier.
	 * @return ch.ethz.iks.jxme.bluetooth.ConnectionHandle
	 * @see ch.ethz.iks.jxme.bluetooth.ConnectionPool#getConnection(java.lang.String)
	 */
	public synchronized IConnectionHandle getConnection(String identifier) {
		IConnectionHandle handle = (IConnectionHandle)_connections.get(identifier);
		if(handle == null){
			// if the identifier is the MAC address 
			//   then find the corresponding identifier and return the connection
			//   else no connection available
			
			String id = (String)_mac2id.get(identifier);
			if(id != null){
				handle = (IConnectionHandle)_connections.get(id);
			}
		}
		
		if( LOG.isDebugEnabled() ){
			if(handle != null){ 
				LOG.debug("return connection [" + identifier + "]: " + handle.getLocalBTAddress() + 
															" -> " + handle.getRemoteBTAddress());
			} else {
				LOG.debug("There doesn't exist any connection matching to the given identifier [" + identifier + "].");
			}
		}
		
		return handle;
	}


	/** This method returns an identifier corresponding to a given connection
	 * @return java.lang.String, <code>null</code> if the specified connection can't be found
	 * @see ch.ethz.iks.jxme.bluetooth.ConnectionPool#getIdentifier(javax.microedition.io.Connection)
	 */
	public synchronized String getIdentifier(Connection connection) {
		// check all connections
		Enumeration connections = _connections.elements();
		IConnectionHandle currentConnection = null;
		boolean foundConnection = false;
		
		while( connections.hasMoreElements() ){
			currentConnection = (IConnectionHandle)connections.nextElement(); 
			if( currentConnection.getConnection().equals(connection) ){
				foundConnection = true;
				break;
			}
		}
		if( foundConnection ){
			return currentConnection.getIdentifier();
		} else {
			return null;
		}
	}

	
	/** This method returns the number of connections managed by this connection pool.
	 * @return number of connection managed by this connection pool
	 * */
	public int size(){
		return _connections.size();	
	}


	/** This method closes a given connection and removes it from the connection pool
	 * @param connection <code>IConnnectionHandle</code> to be removed from
	 * the connection pool
	 */
	public void removeConnection(IConnectionHandle connection) {
		String identifier = connection.getIdentifier();
		String btAddress = connection.getRemoteBTAddress();
		if(LOG.isDebugEnabled()){
			LOG.debug("remove connection: " + connection.getLocalBTAddress() + " -> " + connection.getRemoteBTAddress());
		}
		
		_connections.remove(connection);
		_id2mac.remove((String)_mac2id.get(btAddress));
		_mac2id.remove(btAddress);
		
		try{
			connection.close();
		} catch(Exception e){
			LOG.error("error during closing connection to " + identifier + " [" + btAddress + "]", e);
		}
		
		// throw new disconnectEvent
		try {
			_disconnectQueue.putEvent( new DisconnectEvent(btAddress, identifier) );
		} catch (InterruptedException e1) {
			LOG.error("Can't notify disconnectListeners", e1);
		}
		if( LOG.isDebugEnabled() ){
			logConnections(Level.DEBUG);
		}
	}
	
	
	/** Since it is possible that the lost of a connection is deteced in a class not having
	 * a reference to the connection pool it is necessary to offer a method to allow 
	 * to remove connections also if there are no references to the connection pool.
	 * This makes sence since there exists exactly one connection pool per 
	 * JVM.
	 * @param conn connection to be removed from the connection pool.
	 *  */
	public synchronized static void staticRemoveConnection(IConnectionHandle connection){
		String identifier = connection.getIdentifier();
		String btAddress = connection.getRemoteBTAddress();
		if(LOG.isDebugEnabled()){
			LOG.debug("remove connection: " + connection.getLocalBTAddress() + " -> " + connection.getRemoteBTAddress());
		}
		
		if(identifier != null){
			_connections.remove(identifier);
			String id = (String)_mac2id.get(btAddress);
	
			if(id != null){
			_id2mac.remove(id);
			}
			
			if(btAddress != null){
				_mac2id.remove(btAddress);
			}
		}
		
		// throw new disconnect event
		try {
			_disconnectQueue.putEvent( new DisconnectEvent(btAddress, identifier) );
		} catch (InterruptedException e1) {
			LOG.error("Can't notify disconnect listeners.", e1);
		}
		
		try {
			connection.close();
		} catch (IOException e) {
			LOG.error("error while closing connection to " + identifier + " [" + btAddress + "]");
		}
		
		if( LOG.isDebugEnabled() ){
			logConnections(Level.DEBUG);
		}
	}
	
	
	/** The listener added by this method will be called if there has been established a new connection.
	 * The event passed to the listener contains the Bluetooth address and the user friendly name
	 * of the remote device.
	 * @param listener listener to be called if there is a new connection
	 */
	public void addConnectListener(IConnectListener listener){
		_connectQueue.addListener(listener);
	}
	
	
	/** Removes a given listener from list of connectListeners.
	 * @param listener listener to be removed
	 */
	public void removeConnectListener(IConnectListener listener){
		_connectQueue.removeListener(listener);
	}
	
	
	/** The listener added by this method will be called if a connection has been closed.
	 * The event passed to the listener contains the Bluetooth address and the user friendly name
	 * of the remote device that was removed.
	 * @param listener listener to be called if a connection was removed
	 */
	public void addDisconnectListener(IDisconnectListener listener){
		_disconnectQueue.addListener(listener);
	}
	
	
	/** Removes a given listener from list of disconnectListeners.
	 * @param listener listener to be removed
	 */
	public void removeDisconnectListener(IDisconnectListener listener){
		_disconnectQueue.removeListener(listener);
	}

	/** Closes all connections of this device by calling the <code>close</code> methode of
	 * the <code>IConnectionHandle</code>s <code>StreamConnection</code>.
	 */
	public void closeAll() {
		Enumeration connections = _connections.elements();
		
		if( LOG.isDebugEnabled() ){
			logConnections(Level.DEBUG);
		}
		
		if( LOG.isInfoEnabled() ){
			LOG.info("closing all connections ...");
		}
		
		while( connections.hasMoreElements() ){
			IConnectionHandle currentConnection = (IConnectionHandle)connections.nextElement();
			String btAddress = currentConnection.getRemoteBTAddress();
			try {
				if( LOG.isDebugEnabled() ){
						LOG.debug("close connection to " + currentConnection.getIdentifier() +
											" [" + currentConnection.getRemoteBTAddress() + "]");
				}
				// generate a disconnect event and remove the connection from the hashtable
				try {
					_disconnectQueue.putEvent(new DisconnectEvent(currentConnection.getRemoteBTAddress(), currentConnection.getIdentifier()));
				} catch (InterruptedException e1) {
					LOG.error("Can't notify disconnect listeners.", e1);
				}
				//currentConnection.getConnection().close();
				_id2mac.remove((String)_mac2id.get(btAddress));
				_mac2id.remove(btAddress);
				_connections.remove(currentConnection.getIdentifier());
				currentConnection.close();
				
			} catch (IOException e) {
				LOG.error("Can't close connection to " + currentConnection.getIdentifier() + 
									" [" + currentConnection.getRemoteBTAddress() + "]");
			}
		}
	}
	
	/** This method writes the information about the current connections to log.
	 * @param level Level to log the current connections. */
	protected static void logConnections(Level level){
		logConnections(LOG, level);
	}
	
	/** This method writes the information about the current connections to log defined by the given logger.
	 * @param level Level to log the current connections
	 * @param logger Logger to write the log to */
	public static void logConnections(Logger logger, Level level){
		//Priority prio = Priority.toPriority(level.toInt());
		
		StringBuffer buffer = new StringBuffer();
		Enumeration enum = _connections.elements();
		
		buffer.append("\nConnections:\n");
		
		while(enum.hasMoreElements()){
			IConnectionHandle currentHandle = (IConnectionHandle)enum.nextElement();
			buffer.append(currentHandle.getIdentifier() + " [" + currentHandle.getRemoteBTAddress() + "]\n");
		}
		
		//logger.log(prio, buffer.toString());
		logger.debug(buffer.toString());
	}
	
	public int getRole(){
		int masters = 0;
		int slaves = 0;
		int size = size();
		
		if(size == 0){
			return NO_CONNECTION;
		}
		
		Enumeration enum = _connections.elements();
		
		while(enum.hasMoreElements()){
			IConnectionHandle currentConnection = (IConnectionHandle)enum.nextElement();
			if(currentConnection.isMaster()){
				masters++;
			}else{
				slaves++;
			}
		}
		
		if((masters > 0) && (slaves > 0)){
			return MASTER_AND_SLAVE;
		}
		
		if((masters > 0) && (slaves == 0)){
			return MASTER;
		}
		
		if((masters == 0) && (slaves > 0)){
			return SLAVE;
		}
		
		LOG.error("Illeagal state this should never happen!");
		throw new RuntimeException("BTConnectionPool.getRole reached an illegal state!");
	}
	
}
