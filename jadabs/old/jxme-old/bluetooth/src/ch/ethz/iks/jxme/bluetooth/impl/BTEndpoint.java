/**
 * midas
 * ch.ethz.iks.jxme.bluetooth.impl
 * BTEndpoint.java
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.Enumeration;

import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connection;

import org.apache.log4j.Logger;

import ch.ethz.iks.jxme.IMessage;
import ch.ethz.iks.jxme.IMessageListener;
import ch.ethz.iks.jxme.bluetooth.IConnectListener;
import ch.ethz.iks.jxme.bluetooth.IConnectionFilter;
import ch.ethz.iks.jxme.bluetooth.IConnectionHandle;
import ch.ethz.iks.jxme.bluetooth.IConnectionPool;
import ch.ethz.iks.jxme.bluetooth.IDisconnectListener;
import ch.ethz.iks.jxme.bluetooth.IEndpoint;
import ch.ethz.iks.jxme.impl.Element;
import ch.ethz.iks.jxme.impl.ElementParseException;
import ch.ethz.iks.jxme.impl.Message;
import ch.ethz.iks.jxme.impl.MessageParseException;
import ch.ethz.iks.jxme.utils.IObjectQueueListener;
import ch.ethz.iks.jxme.utils.ObjectQueue;

/**
 * This class wraps two different endpoints the client and the server endpoint
 * (client and server of the Bluetooth terminology).
 */
public class BTEndpoint
	implements IEndpoint, IObjectQueueListener, IConnectionPool {

	/** Defines the address acting as mutlicast address. */
	private static final String MULTICAST_ADDRESS = "000000000000";

	/** Defines the maximal time to live of a multicast package. */
	public static final int MAX_TIMT_TO_LIVE = 10;

	/**
	 * Pass IS_SERVER in the constructor <code>BTEndpoint(int mode)</code> to
	 * deploy a service record
	 */
	public static final int IS_SERVER = 1;

	/**
	 * Pass IS_CLIENT in the constructor <code>BTEndpoint(int mode)</code> to
	 * start the inquiry and service discovery process.
	 */
	public static final int IS_CLIENT = 2;

	/**
	 * Pass IS_CLIENT_AND_SERVER in the constructor <code>BTEndpoint(int mode)</code>
	 * start a peer offering the service and searching also other devices.
	 */
	public static final int IS_CLIENT_AND_SERVER = 3;

	protected static final String FORWARD_GROUP = "___forward___";

	private static IConnectionPool _connections =
		BTConnectionPool.getConnectionPool();
	private static ObjectQueue _unparsedMessages = new ObjectQueue();
	private static IMessageListener _parsedMessages = null;
	private static Logger LOG = Logger.getLogger(BTEndpoint.class.getName());

	private int _mode = -1;
	private BTEndpointServer _server = null;
	private BTEndpointClient _client = null;

	/**
	 * This constructor starts a BTEndpoint.
	 * 
	 * @param mode
	 *                    defines the mode of this endpoint valid values are:
	 *                    BTEndpoint.IS_SERVER, BTEndpoint.IS_CLIENT and
	 *                    BTEndpoint.IS_CLIENT_AND_SERVER
	 * @param messageQueue
	 *                    <code>IMessageQueue</code> to put the received messages
	 * @param timeout
	 *                    if mode is equals to <code>BTEndpoint.IS_CLIENT</code> then
	 *                    you can specify a timout between two inquirys. If the timeout
	 *                    value is smaller or equals to zero the inquiry will be
	 *                    executed exactly once else a inquiry thread will be created
	 *                    inquirying perodically the environment.
	 */
	public BTEndpoint(int mode, IMessageListener messageQueue, int timeout) {
		_mode = mode;
		_unparsedMessages.addListener(this);

		_parsedMessages = messageQueue;

		if (IS_SERVER == _mode || IS_CLIENT_AND_SERVER == _mode) {
			_server = BTEndpointServer.getBTEndpointServer(this);
		}
		if (IS_CLIENT == _mode || IS_CLIENT_AND_SERVER == _mode) {
			try {
				if (timeout <= 0) {
					_client = new BTEndpointClient(this);
				} else {
					_client = new BTEndpointClient(this, timeout);
				}
			} catch (Exception e) {
				LOG.fatal("Error by searching peers", e);
			}
		}
		if (_server == null && _client == null) {
			throw new IllegalArgumentException("value of mode is not in a valid range");
		}
	}

	/**
	 * If this endpoint represents a client the inquiry and service discovery
	 * procedures are executed. Otherwise representing a server this method
	 * will update the service disovery database and start the server thread
	 * waiting for incoming connections. This method is blocking until either
	 * device and service discovery is terminated or the server is started.
	 * It's is possible that an endpoint is representing a client and a server
	 * before using this feature make sure that your Bluetooth device is
	 * supporting scatternets.
	 */
	public void connect() throws IOException {
		if (_server != null) {
			_server.connect();
		}
		if (_client != null) {
			_client.connect();
		}
	}

	public void connect(int mode) throws IOException {
		if( (mode == IS_SERVER || mode == IS_CLIENT_AND_SERVER) && _server != null){
			_server.connect();
		}
		if( (mode == IS_CLIENT || mode == IS_CLIENT_AND_SERVER) && _client != null){
			_client.connect();
		}
	}
	
	/**
	 * This message returns an empty message implementing the interface <code>IMessage</code>
	 * that's transmission is supported by this endpoint.
	 * 
	 * @return empty message
	 */
	public IMessage createMessage() {
		return new Message();
	}

	/**
	 * This method takes a <code>IMessage</code> and sends it to the given
	 * identifier. The identifer is by default the remote devices user friendly
	 * name but can also be the Bluetooth device address. The see the details
	 * of the addressing schema see class <code>BTConnectionHandle</code> and
	 * there especially the method <code>String getIdentifier()</code> The
	 * allzero Bluetooth address (<code>000000000000</code>) is <b>always
	 * </b> accepted and will send the message as multicast. Before and after
	 * the message a header and a footer is inserted.
	 */
	public void sendMessage(String identifier, IMessage message)
		throws UnknownHostException, IOException {

		if (_connections.size() == 0) {
			LOG.fatal("No connection in connection pool");
			throw new IOException("No connection in connection pool");
		}

		if (identifier.equals(MULTICAST_ADDRESS)) {
			sendMulticast(message, null);
			return;
		}

		BTConnectionHandle conn =
			(BTConnectionHandle) _connections.getConnection(identifier);
		if (conn == null) {
			LOG.fatal("Unknown host (" + identifier + ")");
			sendMulticast(message, FORWARD_GROUP, identifier);
		}

		Element sender =
			new Element(conn.getLocalBTAddress().getBytes(), "sender");
		Element receiver =
			new Element(conn.getRemoteBTAddress().getBytes(), "receiver");

		message.setElement(sender);
		message.setElement(receiver);

		OutputStream out = conn.openOutputStream();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		message.writeMessage(buffer);
		BTHeader header =
			new BTHeader(
				conn.getLocalBTAddress(),
				conn.getRemoteBTAddress(),
				buffer.size(),
				false);
		BTFooter footer = new BTFooter();

		// write must be synchronized since there is one connection for
		// potentially several threads
		synchronized (out) {
			out.write(new String("1234").getBytes());
			header.writeHeader(out);
			out.write(buffer.toByteArray());
			footer.writeFooter(out);
			out.flush();
		}

	}

	/**
	 * This method takes a <code>IMessage</code> and sends it to all known
	 * addresses. Before and after the message a header and a footer is
	 * inserted. This method inserts the following Elements to the Message:
	 * <table>
	 * <th>Name</th>
	 * <th>Value</th>
	 * <th>Description</th>
	 * <tr>
	 * <td>sender</td>
	 * <td>BTAddress of sende</td>
	 * <td>Device Address of the sending device (originating this multicast)
	 * </td>
	 * </tr>
	 * <tr>
	 * <td>receiver</td>
	 * <td>BTAddress of</td>
	 * <td></td>
	 * </tr>
	 * <tr>
	 * <td>group</td>
	 * <td>String spec. the group</td>
	 * <td>if this element exits the message is a multicast to the given group
	 * </td>
	 * </tr>
	 * </table>
	 */
	public void sendMulticast(IMessage message, String groupName)
		throws IOException {
		sendMulticast(message, groupName, MULTICAST_ADDRESS);
	}

	protected synchronized void sendMulticast(
		IMessage message,
		String groupName,
		String receiversAddr)
		throws IOException {
		
		BTConnectionHandle conn = null;
		Enumeration connections = _connections.getAllConnections();

		if (_connections.size() == 0) {
			LOG.fatal("No connection in connection pool");
			throw new IOException("No connection in connection pool");
		}

		if (groupName == null) {
			groupName = "default";
		}

		if (LOG.isDebugEnabled()) {
			LOG.debug("\n\n\nstart sending multicast messages\n\n\n");
		}
		while (connections.hasMoreElements()) {
			conn = (BTConnectionHandle) connections.nextElement();

			Element sender =
				new Element(conn.getLocalBTAddress().getBytes(), "sender");
			Element receiver = new Element(MULTICAST_ADDRESS.getBytes(), "receiver");

			if (groupName != null) {
				Element group = new Element(groupName.getBytes(), "group");
				message.setElement(group);
			}

			message.setElement(sender);
			message.setElement(receiver);

			OutputStream out = conn.openOutputStream();
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			message.writeMessage(buffer);

			BTHeader header =
				new BTHeader(
					conn.getLocalBTAddress(),
					receiversAddr,
					buffer.size(),
					groupName != null
						? !groupName.equals(FORWARD_GROUP)
						: true);
			BTFooter footer = new BTFooter();

			// write must be synchronized since there is one connection for
			// potentially several threads
			synchronized (out) {
				out.write(new String("1234").getBytes());
				header.writeHeader(out);
				out.write(buffer.toByteArray());
				footer.writeFooter(out);
				out.flush();
			}
			if (LOG.isInfoEnabled()) {
				LOG.info("Sent message to " + conn.getRemoteBTAddress());
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("stop sending out mulitcast messages");
		}
	}

	public synchronized void forwardMessage(BTPackage pack)
		throws IOException {
		
		if(LOG.isDebugEnabled()){
			LOG.debug("BTEndpoint.forwardMessage");
		}
		
		if(pack.getHeader().getReceiver().equalsIgnoreCase(LocalDevice.getLocalDevice().getBluetoothAddress()) ||
				pack.getHeader().getReceiver().equalsIgnoreCase(LocalDevice.getLocalDevice().getFriendlyName())){
			return;
		}
		
		if (pack.getHeader().isMulticast()
			|| pack.getHeader().getReceiver().equalsIgnoreCase(MULTICAST_ADDRESS)) {
			forwardMulticastMessage(pack);
			return;
		}

		String sender = pack.getHeader().getSender();
		String receiver = pack.getHeader().getReceiver();
		if (LOG.isInfoEnabled()) {
			LOG.info("forwarding message from " + sender + " to " + receiver);
		}

		IConnectionHandle conn = _connections.getConnection(receiver);

		if (conn != null) {
			OutputStream out = conn.openOutputStream();

			BTHeader header =
				new BTHeader(
					conn.getLocalBTAddress(),
					conn.getRemoteBTAddress(),
					pack.getMessage().length,
					pack.getHeader().isMulticast());
			BTFooter footer = new BTFooter();

			synchronized (out) {
				out.write(new String("1234").getBytes());
				header.writeHeader(out);
				out.write(pack.getMessage());
				footer.writeFooter(out);
				out.flush();
			}
		} else {
			LOG.debug(
				"Unknown recipient " + receiver + " -> forward multicast");
			forwardMulticastMessage(pack);
		}
	}

	public synchronized void forwardMulticastMessage(BTPackage pack)
		throws IOException {
		String sender = pack.getHeader().getSender();
		
		if(LOG.isDebugEnabled()){
			LOG.debug("Header:\n" + pack.toString());
		}
		
		if (LOG.isInfoEnabled()) {
			LOG.info("forwarding multicast message from " + sender);
		}

		BTConnectionHandle conn = null;
		Enumeration connections = _connections.getAllConnections();

		while (connections.hasMoreElements()) {
			conn = (BTConnectionHandle) connections.nextElement();
			if (!conn.getRemoteBTAddress().equalsIgnoreCase(pack.getIncomingEdge())) {
				OutputStream out = conn.openOutputStream();

				BTHeader header =
					new BTHeader(
						pack.getHeader().getSender(),
						pack.getHeader().getReceiver(),
						pack.getMessage().length,
						pack.getHeader().isMulticast());
				BTFooter footer = new BTFooter();

				synchronized (out) {
					out.write(new String("1234").getBytes());
					header.writeHeader(out);
					out.write(pack.getMessage());
					footer.writeFooter(out);
					out.flush();
				}
			}
		}

	}

	/**
	 * This method is implementing interface <code>IConnectionPool</code>.
	 * Indeed this method does nothing else than taking a connection handle,
	 * adding a read thread and store all of them in a new connection handle.
	 * 
	 * @param conn
	 *                    already existing instance of any implementation of a <code>IConnecitonHandle</code>
	 */
	public void addConnection(IConnectionHandle conn) {
		ReadThread reader;
		try {
			reader = new ReadThread(_unparsedMessages, conn);
			BTReadThreadConnectionHandle newConn =
				new BTReadThreadConnectionHandle(conn, reader);
			new Thread(reader).start();
			_connections.addConnection(newConn);
		} catch (IOException e) {
			LOG.fatal(
				"Can't read from connection to " + conn.getRemoteBTAddress(),
				e);
		}
		if (LOG.isInfoEnabled()) {
			LOG.info(
				"new connection: "
					+ conn.getLocalBTAddress()
					+ " -> "
					+ conn.getRemoteBTAddress());
		}
	}

	/**
	 * Removes a given connection handle from the connection pool. Before the
	 * connection is removed it is closed.
	 * 
	 * @param conn
	 *                    connection to be closed/removed
	 */
	public void removeConnection(IConnectionHandle conn) {
		_connections.removeConnection(conn);
		try {
			conn.close();
		} catch (IOException e) {
			LOG.error(
				"error during closing connection to "
					+ conn.getIdentifier()
					+ " ["
					+ conn.getRemoteBTAddress()
					+ "]");
		}
	}

	/**
	 * Returns the number of active connections of this application.
	 * 
	 * @return number of active connections
	 */
	public int numberOfConnections() {
		return _connections.size();
	}

	/**
	 * This method is called by the queue that stores unparsed messages. This
	 * method will take a raw message and will create a message object out of
	 * it. It the message can not be parsed the thrown exception is logged and
	 * the message is deleted!
	 * 
	 * @param unparsedMessage
	 *                    this object need to be an instance of <code>BTPackage</code>!
	 */
	public void processEvent(Object unparsedMessage) {
		BTPackage pack = (BTPackage) unparsedMessage;
		ByteArrayInputStream in = new ByteArrayInputStream(pack.getMessage());
		IMessage msg = null;
		
		if(LOG.isDebugEnabled()){
			LOG.debug("PROCESS MESSAGE in ch.ethz.iks.jxme.bluetooth.impl.BTEndpoint");
			LOG.debug(pack.toString());
		}
		
		try {
			msg = Message.read(in);
		} catch (IOException e) {
			LOG.error("could not parse message", e);
			return;
		} catch (MessageParseException e) {
			LOG.error("could not parse message", e);
			return;
		} catch (ElementParseException e) {
			LOG.error("could not parse message", e);
			return;
		}
		_parsedMessages.processMessage(msg);
		if (_connections.size() > 1) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("forwarding multicast");
			}
			try {
				forwardMessage(pack);
			} catch (Exception e1) {
				LOG.error("can not forward multicast", e1);
			}
		}
	}

	/**
	 * Method returning an <code>java.util.Enumeration</code> representing
	 * all active connections of this application. The number of elements of
	 * this enumeration is equals to the number of connections given by the
	 * method <code>numberOfConnections()</code>.
	 * 
	 * @return enumeration constisting of implementations of the interface
	 *               <code>IConnectionHandle</code> by default this are objects of
	 *               type <code>BTReadThreadConnectionHandle</code>.
	 */
	public Enumeration getAllConnections() {
		return _connections.getAllConnections();
	}

	/**
	 * This method returns the corresponding connection handle to the given
	 * identifier.
	 * 
	 * @param identifier
	 *                    identifier identifying a connection
	 * @return connection corresponding to the identifer if there is one
	 *               otherwise <code>null</code>.
	 */
	public IConnectionHandle getConnection(String identifier) {
		return _connections.getConnection(identifier);
	}

	/**
	 * This method returns the corresponding identifier to a given connection
	 * handle.
	 * 
	 * @param connection
	 *                    connection handle
	 * @return identifier corresponding to the given conneciton
	 */
	public String getIdentifier(Connection connection) {
		return _connections.getIdentifier(connection);
	}

	/**
	 * Equals to method <code>numberOfConnections</code>
	 * 
	 * @return number of active connections of this application
	 */
	public int size() {
		return _connections.size();
	}

	/**
	 * This method tells if the current entpoint is a rendez-vous peer or not.
	 * 
	 * @return <code>true</code> if the current endpoint is a rendez-vous
	 *               server, else <code>false</code>
	 */
	public boolean isRendezVousServer() {
		return _mode == IS_CLIENT || _mode == IS_CLIENT_AND_SERVER;
	}

	/**
	 * The listener added by this method will be called if there has been
	 * established a new connection. The event passed to the listener contains
	 * the Bluetooth address and the user friendly name of the remote device.
	 * 
	 * @param listener
	 *                    listener to be called if there is a new connection
	 */
	public void addConnectListener(IConnectListener listener) {
		_connections.addConnectListener(listener);
	}

	/**
	 * Removes a given listener from list of connectListeners.
	 * 
	 * @param listener
	 *                    listener to be removed
	 */
	public void removeConnectListener(IConnectListener listener) {
		_connections.removeConnectListener(listener);

	}

	/**
	 * The listener added by this method will be called if a connection has
	 * been closed. The event passed to the listener contains the Bluetooth
	 * address and the user friendly name of the remote device that was
	 * removed.
	 * 
	 * @param listener
	 *                    listener to be called if a connection was removed
	 */
	public void addDisconnectListener(IDisconnectListener listener) {
		_connections.addDisconnectListener(listener);

	}

	/**
	 * Removes a given listener from list of disconnectListeners.
	 * 
	 * @param listener
	 *                    listener to be removed
	 */
	public void removeDisconnectListener(IDisconnectListener listener) {
		_connections.removeDisconnectListener(listener);
	}

	/**
	 * This method is iterating over all active connections of this application
	 * and will close all of them.
	 */
	public void closeAll() {
		_connections.closeAll();
	}

	/**
	 * This method will stop this endpoint by stopping all treads and closing
	 * all connetions.
	 */
	public void stop() {
		_unparsedMessages.stopQueue();
		//((MessageQueue)_parsedMessages).stopQueue();
		_connections.closeAll();
	}
	
	public void applyFilter(IConnectionFilter filter){
		_client.applyFilter(filter);
	}
	
	public void withdrawFillter(){
		_client.withdrawFilter();
	}

	/** This method returns an int representing the role of the local Bluetooth device.
	 * @see ch.ethz.iks.jxme.bluetooth.IConnectionPool#getRole()
	 */
	public int getRole() {
		return _connections.getRole();
	}
}
