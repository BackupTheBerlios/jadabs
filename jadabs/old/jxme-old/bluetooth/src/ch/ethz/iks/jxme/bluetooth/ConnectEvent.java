/**
 * midas
 * ch.ethz.iks.jxme.bluetooth
 * ConnectEvent.java
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

/** This class extends the class <code>ConnectionEvent</code> without adding
 * any new functionality! The class <code>ConnectionEvent</code> has been
 * extended to have events of type <code>ConnectEvent</code> and 
 * <code>DisconnectEvent</code>.
 */
public class ConnectEvent extends ConnectionEvent {
	
	
	/** Default constructor. The information about the counter party's
	 * BT_ADDR is always avaiable.
	 * @param btAddress counterpartys Bluetooth device address 
	 * <code>java.lang.String</code>.
	 * */
	public ConnectEvent(String btAddress){
		super(btAddress);
	}
	
	
	/** Use this constructor if the counter party's user friendly name
	 *  is available.
	 *  @param btAddress counterpartys Bluetooth device address as <code>java.lang.String</code>.
	 *  @param friendlyName Friendly Name ouf the counterpartiys Bluetooth device. */
	public ConnectEvent(String btAddress, String friendlyName){
		super(btAddress, friendlyName);
	}
}
