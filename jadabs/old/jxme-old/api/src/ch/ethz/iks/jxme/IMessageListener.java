/**
 * midas
 * ch.ethz.iks.jxme.msg
 * IMessageListener.java
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
 * $Id: IMessageListener.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 * */
package ch.ethz.iks.jxme;

/** This interface defines a listener listening for incoming messages. If the system receives
 * a message it will call all registered listener's <code>processMessage(...)</code> method.
 */
public interface IMessageListener {

	
	/** This message is called by the system after receiving a new message.
	 * @param message message that have recently been received */
	void processMessage(IMessage message);

}
