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


/**
 * The class <code>ConnectionEvent</code> is designed to notify the application
 * layer about new and/or lost connections. This class is wrapping information about
 * the counterparty such as Bluetooth address and Friendly Name.
 */
public class ConnectionEvent extends EventBasisClass {

	private String _btAddress = null;
	private String _friendlyName = null;


	/** @param btAddress counterpartys Bluetooth device address <code>java.lang.String</code>. */
	public ConnectionEvent(String btAddress){
		_btAddress = btAddress;
	}
	
	
	/** @param btAddress counterpartys Bluetooth device address as <code>java.lang.String</code>.
	 *  @param friendlyName Friendly Name ouf the counterpartiys Bluetooth device. */
	public ConnectionEvent(String btAddress, String friendlyName){
		this(btAddress);
		_friendlyName = friendlyName;
	}
	

	/** @return counterpartys Bluetooth device address, this method returns never <code>null</code>. */
	public String getBluetoothAddress(){
		return _btAddress;
	}


	/** @return Friendly Name ouf counterpartys Bluetooth device address, can be <code>null</code>
	 * if there is no device name available. */
	public String getFriendlyName(){
		return _friendlyName;
	}
}
