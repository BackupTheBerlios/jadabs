/**
 * JXMEbt
 * ch.ethz.iks.jxme.bluetooth
 * IConnectListener.java
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 *
 * 17.07.2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * */
package ch.ethz.iks.jxme.bluetooth;

/** To be notifed about new connections implement this interface and register
 * your implementation by the corresponding <code>IPeerNetwork</code>.
 */
public interface IConnectListener {

	/** This method is called to process a <code>ConnectEvent</code>.
	 * @param event this event gives information as BT_ADDR as 
	 * <code>java.lang.String</code> and (by defautl) the user friendly
	 * name.
	 * */
	public void processEvent(ConnectEvent event);

}
