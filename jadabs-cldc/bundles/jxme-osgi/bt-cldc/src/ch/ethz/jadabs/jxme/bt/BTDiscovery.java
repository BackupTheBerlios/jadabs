/*
 * Created on Aug 1, 2004
 *
 * $Id: BTDiscovery.java,v 1.2 2004/12/22 12:39:31 afrei Exp $
 */
package ch.ethz.jadabs.jxme.bt;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

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


/**
 * This class is repsonsible to lookup suitable Bluetooth devices, i.e.
 * Computers and SmartPhones.
 *  
 * @author Ren&eacute; M&uuml;ller
 */
public class BTDiscovery implements DiscoveryListener, Runnable
{
//    /** sleep time of discovery thread between two polling cycles in ms */
//    private static int DISCOVERY_SLEEP_TIME_MS = 60000;
    
    /** sleep time of discovery thread between two polling cycles in ms */
    private static int discosleeptime = 60000;
    
    /** Log4j logger to be used */
    private Logger LOG = Logger.getLogger("BTDeviceDiscovery");
    
    /** access to the local BT interface */
    private LocalDevice localDevice;
    
    /** used for BT inquiry */ 
    private DiscoveryAgent discoveryAgent;
    
    /** vector containing the RemoteDevices that were discovered */
    private Vector cachedDevices; 
    
    /** vector containing yet unconnected services */
    private Vector unconnectedServices; 
    
    /** transport instance that contains the connectin lsit */
    private BTTransport btTransport;
    
    /** true if we are about to terminate the discovery thread */
    private boolean aborting = false;
    
    /** true if device discovery phase is done */
    private boolean deviceDiscoveryDone = false;
    
    /** true if service discovery phase is done */
    private boolean serviceDiscoveryDone = false;
    
    /**
     * Create new Bluetooth Device Discovery Instance
     * @param transport instance that contains the connection list 
     */
    public BTDiscovery(BTTransport transport) 
    {
        btTransport = transport;
        cachedDevices = new Vector();
        unconnectedServices = new Vector();
        
        String dst = BTActivator.bc.getProperty("ch.ethz.jadabs.jxme.bt.discoverysleep");
        if (dst != null)
            discosleeptime = Integer.parseInt(dst);
        
        
        try {
            localDevice = LocalDevice.getLocalDevice();
            discoveryAgent = localDevice.getDiscoveryAgent();
            
            // we set ourselves NOT_DISCOVERABLE such that we cannot 
            // be connected by someone, thus it is certain that we keep
            // the Master Role. 
            // (recommendation from the Guide "Games of Bluetooth: Recommendations
            // for Game Developers" by Nokia) 
            localDevice.setDiscoverable(DiscoveryAgent.NOT_DISCOVERABLE);
        } catch (BluetoothStateException e) {
            LOG.error("cannot initialize BT Stack: "+e.getMessage());
            return;
        }
        
        // start discovery thread 
        Thread discoveryThread = new Thread(this);
        discoveryThread.start();
    }
    
    
    /**
     * Runbody of the BTDiscovery thread 
     */
    public void run()
    {
        while (!aborting) {            
            try {
                
                // start device discovery
                if (LOG.isDebugEnabled()) {
                    LOG.debug("device discovery started");
                }
                deviceDiscoveryDone = false;
                serviceDiscoveryDone = false;
                cachedDevices.removeAllElements();
                synchronized(this) {
                    discoveryAgent.startInquiry(DiscoveryAgent.GIAC, this);
                    while (!deviceDiscoveryDone) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) { }
                    }
                }

                // were we terminated in the meantime?
                if (aborting) {
                    return;
                }
                
                if (LOG.isDebugEnabled()) {
                    LOG.debug("devices found: "+cachedDevices.size());
                    LOG.debug("service discovery started");
                }
                
                // start service discovery
                unconnectedServices.removeAllElements();
                // connecting to newly found devices
                // do the service search on all devices found
                UUID[] u = new UUID[1];
                u[0] = BTTransport.JXME_BT_UUID;
                
                // Retrieved service record should include (service name: 0x0100)
                int attrbs[] = { 0x0100 };
                Enumeration devices = cachedDevices.elements();
                while (devices.hasMoreElements()) {
                    RemoteDevice device = (RemoteDevice)devices.nextElement();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Probing BT Device "+device.getBluetoothAddress()+
                                  " for JxmeBtService");
                    }
                    synchronized(this) {
                        // start search on discovery agent 
                        serviceDiscoveryDone = false;
                        discoveryAgent.searchServices(attrbs, u, device, this);
                        while (!serviceDiscoveryDone) {
                            try {
                                this.wait();
                            } catch(InterruptedException e) { }
                        }
                    }                    
                }
                
                // were we terminated in the meantime?
                if (aborting) {
                    return;
                }
                                
                // now connect to all new devices found that provide the service
                devices = unconnectedServices.elements();
                while (devices.hasMoreElements()) {
                    ServiceRecord record = (ServiceRecord)devices.nextElement();
                    
                    // get connection url
                    // this device does not insist do be master --> 'false'
                    String connectionURL = record.getConnectionURL(
                            ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                    
                    // maybe the URL contains no name or an other problem 
                    if (connectionURL == null)
                        continue;
                    
                    try {
                        // open connection 
                        StreamConnection conn =	(StreamConnection)Connector.open(connectionURL);
                        
                        // create connection wrapper
                        BTConnection connection = new BTConnection(conn, 
                                RemoteDevice.getRemoteDevice(conn), btTransport);
                        
                        // add connection to connection pool 
                        btTransport.addConnection(connection);
                        if (!btTransport.containsConnectionTo(connection.getRemoteBTAddress())) {
                            LOG.error("error in string compare, this does not work!");
                        }
                        
                        // start worker 
                        connection.startWorker();
                    
                    } catch(IOException e) {
                        LOG.error("cannot connect to "+connectionURL+": ");
                        e.printStackTrace();
                    }                
                }
                
            
            } catch (BluetoothStateException e) { 
                LOG.error("error during BT discovery: "+e.getMessage());
            }
            
            // were we terminated in the meantime?
            if (aborting) {
                return;
            }
            
            // wait some time until re initialising whole discovery process
            try {
                Thread.sleep(discosleeptime);
            } catch (InterruptedException e) { } 
        }
    }
    
    /** 
     * Stop and abort discovery thead 
     */
    public void abort()
    {
        synchronized(this) {
            deviceDiscoveryDone = true;
            serviceDiscoveryDone = true;
            aborting = true;
            this.notifyAll();
        }        
    }
    
    /**
     * Called when a device was discovered in the device discovery process.
     * @param device remote device that was found
     * @param cod (Class of Device) = remote device type 
     * @see javax.bluetooth.DiscoveryListener#deviceDiscovered(javax.bluetooth.RemoteDevice, javax.bluetooth.DeviceClass)
     */
    public void deviceDiscovered(RemoteDevice device, DeviceClass cod)
    {
        // Filter CoD: only keep devices that are either phones or computers 
        int major = cod.getMajorDeviceClass();
        if ((major == 0x0200) || (major == 0x0100)) {
            // only accept either phones or computers
            // add it to the list of not already stored and not already connected
            if ((!cachedDevices.contains(device)) &&
                (!btTransport.containsConnectionTo(device.getBluetoothAddress()))) {
                cachedDevices.addElement(device);
            }
        }

    }

    /** 
     * Called when service(s) are found during a service search. 
     * @param transID transaction ID of the service serach that is posting the result
     * @param services a list of services found during the search request 
     * @see javax.bluetooth.DiscoveryListener#servicesDiscovered(int, javax.bluetooth.ServiceRecord[])
     */
    public void servicesDiscovered(int transID, ServiceRecord[] services)
    {
        // A service was found on the device. Only the first is interesting
        // (i.e. there is only one return because we searched only for one)
        // Add the service to the list such that it will be connected to next
        unconnectedServices.addElement(services[0]);
    }

    /** 
     * Called when a service search is completed or was terminated because 
     * of an error
     * @param transID transaction ID identifying the request which initiated the 
     *        service search
     * @param respCode response code that indicates the status of the transaction 
     * @see javax.bluetooth.DiscoveryListener#serviceSearchCompleted(int, int)
     */
    public void serviceSearchCompleted(int transID, int respCode)
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("serviceSearchCompleted()");
        }
        // inquiry completed for whatever reason        
        synchronized(this) {
            serviceDiscoveryDone = true;
            this.notify();	// wakeup discovery thread
        }
    }

    /**
     * Called when an inquiry is completed.
     * @param discType type of request that was completed: either 
     * <code>INQUIRY_COMPLETED</code>, <code>INQUIRY_TERMINATED</code> or 
     * <code>INQUIRY_ERROR</code>
     * @see javax.bluetooth.DiscoveryListener#inquiryCompleted(int)
     */
    public void inquiryCompleted(int discType)
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("inquiryCompleted()");
        }
        // Inquiry is completed for whatevery reason
        synchronized(this) {
            deviceDiscoveryDone = true;
           this.notify(); // wakeup discovery thread 
        }
    }
}
