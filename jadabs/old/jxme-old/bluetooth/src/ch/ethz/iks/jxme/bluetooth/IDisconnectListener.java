/**
 * midas
 * ch.ethz.iks.jxme.bluetooth
 * IDisconnectListener.java
 * 
 * @author Daniel Kaeppeli, danielka@student.ethz.ch
 *
 * 17.07.2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * */
package ch.ethz.iks.jxme.bluetooth;

/** To be notified about lost and closed connection implement this interface
 * and register the listener by the corresponding implementation of
 * the interface <code>IPeerNetwork</code>.
 */
public interface IDisconnectListener {

	/** This method is called to process a disconnect event.
	 * */
	public void processEvent(DisconnectEvent event);

}
