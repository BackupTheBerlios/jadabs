/**
 * midas
 * ch.ethz.iks.jxme
 * ConnectionPool.java
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 *
 * 25.06.2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * */
package ch.ethz.iks.jxme.bluetooth;

import java.util.Enumeration;

import javax.microedition.io.Connection;

public interface IConnectionPool {

	public static final int NO_CONNECTION = 0;
	public static final int MASTER = 1;
	public static final int SLAVE = 2;
	public static final int MASTER_AND_SLAVE = 3;
	
	/** returns all connection stored in this connection pool 
	 * @return Enumeration of ConnectionHandle 
	 * */
	Enumeration getAllConnections();
	
	/** adds a ConnectionHandle to the ConnectionPool */
	void addConnection(IConnectionHandle connection);
	
	/** remove a connection from the connection pool */
	void removeConnection(IConnectionHandle connection);
	
	/** 
	 * @return ConnectionHandle corresponding to the given identifier 
	 * */
	IConnectionHandle getConnection(String identifier);

	/** 
	 * @return identifier corresponding to given Connection.
	 * */
	String getIdentifier(Connection connection);

	/** This method returns the current numbers of 
	 * <code>IConnectionsHandles</code>s stored in this pool.
	 * @return size of current connection pool
	 * */
	int size();	
	
	/** Register a listener to be called if a new connection is established */
	public void addConnectListener(IConnectListener listener);
	
	/** Unregister the given listener. */
	public void removeConnectListener(IConnectListener listener);
	
	/** Register a listener to be called if a connection was closed. */
	public void addDisconnectListener(IDisconnectListener listener);
	
	/** Unregister given listener. */
	public void removeDisconnectListener(IDisconnectListener listener);
	
	/** close all connections of this peer */
	public void closeAll();
	
	/** retruns an int representing the role of the local BT device, possible values are
	 * 	{@link #NO_CONNECTION NO_CONNECTION}, 
	 *  	{@link #MASTER MASTER}, 
	 * 	{@link #SLAVE SLAVE}, and 
	 * 	{@link #MASTER_AND_SLAVE MASTER_AND_SLAVE} */
	public int getRole();
}
