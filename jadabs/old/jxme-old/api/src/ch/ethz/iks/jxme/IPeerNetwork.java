/**
 * midas
 * ch.ethz.iks.jxme.msg
 * IPeerNetwork.java
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 *
 * 06.06.2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * 
 * $Id: IPeerNetwork.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 * */
package ch.ethz.iks.jxme;

import java.io.IOException;
import java.util.Enumeration;


/** This interface defines the methods a  peer must implement. */
public interface IPeerNetwork {

	/** Peer constant */
	String PEER = "peer";
	/** Group constant */
	String GROUP = "group";
	/** Pipe constant */
	String PIPE = "pipe";

	/** JXME Peername Alias */
    String JXME_PEERNAME = "jxme.peername";
    
	/** Close an input pipe
	 *  @param name  name of the pipe
	 *  @param id    id of the pipe, can be <code>null</code>
	 *  @param type	 the type of the Pipe. JxtaUnicast and JxtaPropagate are two commonly-used values, for example.
	 *  */
	int close(String name, String id, String type);


	/** Connects to a remote peer that is specified by the given URL.
	 *  @param uri 		URI of the remote device. This must be the Bluetooth device address of the counterpart.
	 *  @param state 	A byte array that represents the persistent state of a connection to the PeerNetwork. Can be <code>null</code>
	 * 	@return 		A byte array that represents the persistent state of a connection to the PeerNetwork.
	 *  @exception NoPeerAvailableException		will be thrown if there is no Peer reachable.
	 *  */
//	byte[] connect(String uri, byte[] state) throws NoPeerAvailableException;
	
	/**  Connects to any remote peer offering JXME-Peer-Service. If no peer is available a NoPeerAvailableException will be thrown
	 *  @return 		A byte array that represents the persistent state of a connection to the PeerNetwork.*/
	void connect() throws IOException;


	/** Creates a peer, group or pipe. This method can not be applied in the ad-hoc-mode. 
	 *  @param type one of PEER, GROUP or PIPE
	 *  @param name the name of the entity being created
	 *  @param arg an optional arg depending upon the type of entity being created. For example, for PIPE, this would be the 
	 *                 type of PIPE that is to be created. For example, JxtaUniCast and JxtaPropagate are commonly-used values. 
	 *                 This parameter can be <code>null</code>.
	 *  */
	int create(String type, String name, String arg);


	/** Search for Peers, Groups or Pipes.
	 *  @param type one of PEER, GROUP or PIPE
	 *  @param query an expression specifying the items being searched for and also limiting the scope of items to be returned. 
	 *               This is usually a simple regular expression such as, for example, TicTacToe* to search for all entities 
	 *               with names that begin with TicTacToe.
	 *  */
//	int search(String type, String query);

	/** Send data to the specified Pipe. 
	 *  @param pipe_name
	 *  @param pipe_id
	 *  @param pipe_type
	 *  @param message
	 *  */
//	int send(String pipe_name, String pipe_id, String pipe_type, IMessage message)  throws IOException, NoPeerAvailableException ;


	/**
	 * Send data to the specified Pipe.
	 *
	 * @param id the peer or pipe id to which data is to be sent. 
	 * @param data a {@link IMessage} containing an array of 
	 * {@link IElement}s which contain application data that is to 
	 * be sent
	 *
	 * @return query id that can be used to match responses, if any
	 *
	 * @throws IOException if there is a problem in sending
	 * @throws IllegalArgumentException if id is null
	 */
	void send(String id, IMessage data) throws IOException;


	/** This method gives the status of the current peer. A peer can be a rendez-vous peer or not.
	 * @return <code>true</code> if this peer is a rendez-vous peer else <code>false</code> */
//	boolean isRendezVousServer();


	/** Adds a MessageListener to the process received messages.
	 * @param msglistener listener to be registered
	 */
	void addMessageListener(IMessageListener msglistener);

	/**
	 * Removes a given <code>MessageListener</code>
	 * 
	 * @param msglistener listener to be removed
	 */
	void removeMessageListener(IMessageListener msglistener);

	/**
	 * Enumerate through the registered listeners.
	 * 
	 * @return
	 */
	public Enumeration getMessageListeners();
	
	/**
	 * Process the message under the registered listeners.
	 * 
	 * @param message
	 */
	public void processMessage(IMessage message);
}
