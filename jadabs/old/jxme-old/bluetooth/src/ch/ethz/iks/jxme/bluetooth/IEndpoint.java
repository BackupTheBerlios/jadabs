/**
 * midas
 * ch.ethz.iks.jxme.bluetooth
 * IEndpoint.java
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
package ch.ethz.iks.jxme.bluetooth;

import java.io.IOException;
import java.net.UnknownHostException;

import ch.ethz.iks.jxme.IMessage;


/** This interface is planed to represent an endpoint offering methods to create and send messages. */
public interface IEndpoint {

	/** Factory method returning an empty implementation of the interface <code>IMessage</code> 
	 * @return endpoint specific implementation of the interface <code>IMessage</code>*/
	public IMessage createMessage();
	
	/** This method sends a given <code>IMessage message</code> to the specified receiver given by 
	 * its <code>identifier</code>.
	 * @param identifier <code>java.lang.String</code> identifying the receiver of this message. <b>By 
	 * convention if the identifier is <code>null</code> the message should be sent out as mulitcast</b>. 
	 * @param message to be sent to the specified receipient. 
	 * @throwsUnknownHostException if there is no connection the the host specified by 
	 * <code>identifier</code>.
	 * @throws NoPeerAvailableException if your device is not connected to any other device.
	 * @throws IOException if the send operation fails.
	 * */	public void sendMessage(String identifier, IMessage message) throws UnknownHostException, IOException;

	/** This method sends out a mulitcast message to all known connected devices. 
	 * @param message to be sent out by the multicast 
	 * @param groupName additional parameter specifying a group. This parameter will be analysed at the receivers.
	 * @throws NoPeerAvailableException if your device in not connected to any other device.
	 * @throws IOException if the send operation fails.
	 * */ 
	public void sendMulticast(IMessage message, String groupName) throws IOException;
}
