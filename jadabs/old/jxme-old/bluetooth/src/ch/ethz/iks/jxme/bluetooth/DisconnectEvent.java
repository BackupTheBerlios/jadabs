/**
 * midas
 * ch.ethz.iks.jxme.bluetooth
 * DisconnectEvent.java
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

/** This event gives information about the device that is connected. These
 * information consists of BT_ADDR as <code>java.lang.String</code> and
 * the user friendly name.
 */
public class DisconnectEvent extends ConnectionEvent {
	
	
	/** Default constructor since information about the BT_ADDR 
	 * is always available.
	 * */
	public DisconnectEvent(String btAddress){
		super(btAddress);
	}
	
	
	/** Use this constructor if the user friendly name (or an identifier)
	 * is also available.
	 * */
	public DisconnectEvent(String btAddress, String friendlyName){
		super(btAddress, friendlyName);
	}

}
