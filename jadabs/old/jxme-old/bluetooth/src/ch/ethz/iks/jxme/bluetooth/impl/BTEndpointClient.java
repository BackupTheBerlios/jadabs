/**
 * midas
 * ch.ethz.iks.jxme.bluetooth.impl
 * BTEndpointClient.java
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 *
 * 03.07.2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * 
 * $Id: BTEndpointClient.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 * */
package ch.ethz.iks.jxme.bluetooth.impl;

import java.io.IOException;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import org.apache.log4j.Logger;

import ch.ethz.iks.jxme.bluetooth.IConnectionFilter;
import ch.ethz.iks.jxme.bluetooth.IConnectionHandle;
import ch.ethz.iks.jxme.bluetooth.IConnectionPool;

/** This class is representing an endpoint being a Bluetooth client. In other words this class is searching for
 * other devices an services. The service and device discovery can be intiated manually by calling the 
 * <code>connect()</code> me
 * 
 * */
public class BTEndpointClient implements DiscoveryListener {
	
	private static Logger LOG = Logger.getLogger( BTEndpointClient.class.getName() );
	
	private int[] _attrSet = {0, 3, 4, 0x100};
	private UUID[] _uuids = null;
	private LocalDevice _localDevice = null;
	private DiscoveryAgent _agent = null; 
	private RemoteDevice[] _devices = null;
	private int _deviceCounter = 0;
	private String[] _services = null;
	private int _serviceCounter = 0;
	private IConnectionPool _connectionPool = null;
	private InquiryThread _inqThread = null;
	
	private boolean inquiryFlag = false;
	private boolean threadedFlag = false;
	private boolean serviceDiscoveryFlag = false;
	
	protected IConnectionFilter _filter = new DefaultFilter();
	
	
	/** This constructor starts automatically an <code>InquiryThread</code> searching periodically for reachable
	 * devices, so the call of method <code>void connect()</code> is not necessary. 
	 * @param connectionPool connection pool to put in new connections
	 * @param timeoutBetweenInquirys defines the timeout to wait between the end of one inquiry
	 * and the start of the next inquiry.
	 * */
	public BTEndpointClient( IConnectionPool connectionPool, int timeoutBetweenInquirys) throws BluetoothStateException{
		this(connectionPool);
		threadedFlag = true;
		_inqThread = new InquiryThread( timeoutBetweenInquirys );
//		_inqThread.start();
	}
	
	/**  This constructor will not start */
	public BTEndpointClient( IConnectionPool connectionPool ) throws BluetoothStateException{
		_uuids = new UUID[1];
		_uuids[0] = new UUID("8800", true);
		_localDevice = LocalDevice.getLocalDevice();
		_connectionPool = connectionPool ;
		if(LOG.isInfoEnabled()){
			LOG.info("Local device " + 
					LocalDevice.getLocalDevice().getFriendlyName() + " [" +
					LocalDevice.getLocalDevice().getBluetoothAddress() + "]");
		}
	}

	/** Searches other peers and opens connection to the found peers
	 *  @throws NoPeerAvailableException if there is no Bluetooth device reachable or if
	 *  the reachable devices don't offer the searched service. */
	public void connect() throws  IOException{
		
		// the first time the connect() method is called for threaded inquiry
		// the inq thread has to be started
		if(threadedFlag && _inqThread != null){
			_inqThread.start();
			threadedFlag = false;
			return;
		}
		
		_agent = _localDevice.getDiscoveryAgent();
		
		// creating new and empty buffers
		_devices = new RemoteDevice[10];
		_services = new String[10];
		_deviceCounter = 0;
		_serviceCounter = 0;
		
		// start inquiry
		_agent.startInquiry(DiscoveryAgent.GIAC, this);
		if(LOG.isInfoEnabled()){
			LOG.info("start inquiry");
		}
		
		// waiting for device discovery completed
		synchronized(this){
			try {
				while(!inquiryFlag){
					this.wait();
				}
				inquiryFlag = false;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if( _deviceCounter <= 0){
//			LOG.error("No peer available, throw NoPeerAvailableException");
			throw new IOException("No Bluetooth device found!");
		}
		
		// start service discovery on discovered devices
		for( int index = 0; index < _deviceCounter; index++ ){
			int transactionId = _agent.searchServices( _attrSet, _uuids, _devices[index], this);
			if( transactionId != -1 ){
				synchronized( this ){
					try {
						while(!serviceDiscoveryFlag){
							this.wait();
						}
						serviceDiscoveryFlag = false;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		//open connection to detected services
		if( _serviceCounter > 0){
			// connect to a service
			int index = 0;
			
			for(index = 0; index < _serviceCounter; index++){
				if(_filter.acceptsConnection(_services[index])){
				if( LOG.isDebugEnabled()){
					LOG.debug("try to connect to: " + _services[index]);
				}
				// try to connect to a remote device 
				try {
					StreamConnection conn = (StreamConnection)Connector.open( _services[index] );
					RemoteDevice remoteDevice = RemoteDevice.getRemoteDevice( conn );
					BTConnectionHandle handle = new BTConnectionHandle( conn, remoteDevice, true);
					_connectionPool.addConnection( handle );
					if(LOG.isInfoEnabled()){
						LOG.info("Connected: " + _localDevice.getFriendlyName() + "[" +handle.getLocalBTAddress() + "] -> " + 
																handle.getIdentifier() + " [" + handle.getRemoteBTAddress() + "]");
					}
				} catch (IOException e) {
					LOG.warn("Can't connect to: " + _services[index]);
				}
			}
			}
		} else {
			// no services available
			throw new IOException("No JXMEbt Service found on available devices!");
		}
		
		_deviceCounter = 0;
		_devices = null;
		_serviceCounter = 0;
		_services = null;
	}

	public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
		try {
			String friendlyName = remoteDevice.getFriendlyName(true);
			IConnectionHandle conn = _connectionPool.getConnection(friendlyName);
			if( (friendlyName != null) && ( conn == null ) ){
				_devices[_deviceCounter++] = remoteDevice;
			}
		} catch (IOException e) {
			LOG.fatal("Can't get Friendly Name of device " + remoteDevice.getBluetoothAddress(), e);
		}
		if(LOG.isInfoEnabled()){
			LOG.info( "discovered: " + remoteDevice.getBluetoothAddress() );
		}
	}

	public void servicesDiscovered(int transactionId, ServiceRecord[] serviceRecords) {
		
		//ServiceRecord[] records = new ServiceRecord[serviceRecords.length];
		int index = 0;
		
		if(serviceRecords.length > 0){
			// get only the first occurrence of the service in practice I of got 2 -> error
			//for( index = 0; index < serviceRecords.length; index++ ){
				try{
					String serviceURL = serviceRecords[index].getConnectionURL(
					  								ServiceRecord.NOAUTHENTICATE_NOENCRYPT, 
					  								false);
					// device offers searched service, but check if it offers also this service as StreamConnection
					if( serviceURL.startsWith("btspp") ){
						if(LOG.isInfoEnabled()){
							LOG.info("service discovered: " + serviceURL);
						}
						_services[_serviceCounter++] = serviceURL;
					}
				} catch(ArrayIndexOutOfBoundsException e){
					if(LOG.isInfoEnabled()){
						LOG.info("found no service on device");
					}
				}
			//}
		} else {
			if(LOG.isInfoEnabled()){
				LOG.info("found no service on device");
			}
		}
	}

	public void serviceSearchCompleted(int transactionId, int responseCode) {
		if(LOG.isInfoEnabled()){
			LOG.info("end of Service Discovery");
		}
		synchronized( this ){
			serviceDiscoveryFlag = true;
			this.notifyAll();
		}
	}

	public void inquiryCompleted(int arg0) {
		if(LOG.isInfoEnabled()){
			LOG.info("inquiry completed");
		}
		synchronized( this ){
			inquiryFlag = true;
			this.notifyAll();
		}
	}


	/* ************************************************************************************************** */
	/*                                                      Inquiry Thread                                                                       */
	/* ************************************************************************************************** */
	private class InquiryThread extends Thread{
		
		private long _timeout = -1;
		private boolean _isRunning = true;
		
		/** Constructor specifying the timeout in milliseconds to wait between two inquiry processes.
		 * @param timeout timeout in milliseconds */
		public InquiryThread( long timeout ){
			_timeout = timeout;
		}
		
	
		/** Thread's worker method. This method calls periodically the connect method of the class 
		 * <code>BTEndpointClient</code> to detect new devices and services. */
		public void run(){
			while( _isRunning ){
				try {
				 	
					connect();

				} catch (IOException e) {
					LOG.warn("There is no other device reachable.");
				} 
				finally{
					long time = System.currentTimeMillis();
					while( System.currentTimeMillis() - time < _timeout ){
						synchronized(this){
							try {
								this.wait(_timeout);
							} catch (InterruptedException e1) {
								LOG.error("interrupted", e1);
							}
						}
					}
				}
			}
		}
		
		
		/** This method sets the exit condition to stop the thread.  */
		public void stopThread(){
			_isRunning = false;
			synchronized(this){
				this.notifyAll();
			}
		}
	}
	
	public void applyFilter(IConnectionFilter filter){
		_filter = filter;
	}
	
	public void withdrawFilter(){
		_filter = new DefaultFilter();
	}
	
	private class DefaultFilter implements IConnectionFilter{

		public boolean acceptsConnection(String uri) {
			return true;
		}
	
	}

}
