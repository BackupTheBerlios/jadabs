/*
 * Created on Jul 22, 2004
 *
 * $Id: BTTransport.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package ch.ethz.jadabs.jxme.bttest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import org.apache.log4j.Logger;

/**
 * This is the BTTransport layer of the test application
 * @author muellerr
 */
public class BTTransport implements DiscoveryListener 
{
    private static Logger LOG = Logger.getLogger("BTTransport");
    private static final String serviceURL = 
        "btspp://localhost:41d53d8182c04f0e8e5cc52cae0415c3;authenticate=false;authorize=false;encrypt=false;name=JxmeBtServer";
    
    private LocalDevice ld;
    private DiscoveryAgent agent;
    private StreamConnectionNotifier service;
    private ListenerThread listenerThread;
    private static final UUID PUBLIC_BROWSE_GROUP = new UUID(0x1002);
    private static final int BROWSE_GROUP_LIST = 0x005;
    
    
    public BTTransport()
    {
        // empty
    }
    
    public void init() 
    {
        LOG.info("init()");
        listProperty("bluetooth.api.version");
        listProperty("bluetooth.l2cap.receiveMTU.max");
        listProperty("bluetooth.connected.devices.max");
        listProperty("bluetooth.connected.inquiry");
        listProperty("bluetooth.connected.page");
        listProperty("bluetooth.connected.inquiry.scan");
        listProperty("bluetooth.connected.page.scan");
        listProperty("bluetooth.master.switch");
        listProperty("bluetooth.sd.trans.max");
        listProperty("bluetooth.sd.attr.retrievable.max");
        
        try {
            ld = LocalDevice.getLocalDevice();
            LOG.info("getLocalDevice()");
            LOG.info("Bluetooth Address: "+ld.getBluetoothAddress());
            LOG.info("Friendly Name: "+ld.getFriendlyName());
            
            ld.setDiscoverable(DiscoveryAgent.GIAC);
            int discoverable = ld.getDiscoverable();
            switch(discoverable) {
            case DiscoveryAgent.GIAC:
                LOG.info("discoverable: GIAC");
                break;
            case DiscoveryAgent.LIAC:
                LOG.info("discoverable: LIAC");
                break;
            case DiscoveryAgent.NOT_DISCOVERABLE:
                LOG.info("discoverable: not discoverable");
                break;
            default:
            }
            DeviceClass deviceClass = ld.getDeviceClass();           
            LOG.info("service class: "+deviceClass.getServiceClasses());
            LOG.info("major device class: "+deviceClass.getMajorDeviceClass());
            LOG.info("minor device class: "+deviceClass.getMinorDeviceClass());
            
            agent = ld.getDiscoveryAgent();
            
        } catch(BluetoothStateException e) {
            LOG.error("BluetoothStackException: "+e.getMessage());
        }
        
        
    }
    
    public void startInquiry() 
    {	
        LOG.info("startInquiry()");
        try {
            agent.startInquiry(DiscoveryAgent.GIAC, this);
        } catch (BluetoothStateException e) {
            LOG.fatal("cannot start inquiry: "+e.getMessage());
            e.printStackTrace();
        }        
    }
    
    public void retrieveDevices() 
    {
        LOG.info("retrieveDevices()");
        RemoteDevice devices[] = agent.retrieveDevices(DiscoveryAgent.CACHED);
        LOG.info(""+devices.length+" BT devices found.");
        for (int i=0; i<devices.length; i++) {
            String address = devices[i].getBluetoothAddress();
            String name = "ERROR unkown";
            try {
                // true = always ask device            
                name = devices[i].getFriendlyName(true);
            } catch (IOException e) { name = "ERROR: "+e.getMessage(); }
            LOG.info("Device["+i+"]: "+address+" alias "+name);
        }
    }
    
    public void startRFCOMMService() 
    {
        LOG.info("Start RFCOMM Service");
        LOG.info("service URL: "+serviceURL);
        
        try {
            ld.setDiscoverable(DiscoveryAgent.GIAC);
        } catch(BluetoothStateException e) {
            LOG.error("Cannot set discovery mode to GIAC, continuing...");
        }
        
        listenerThread = new ListenerThread();        
        (new Thread(listenerThread)).start();
    }
    
    public void stopRFCOMMService() 
    {
        LOG.info("Stopping RFCOMM service");
        listenerThread.close();        
    }
    
    void listProperty(String prop) 
    {
        LOG.info(prop+"="+LocalDevice.getProperty(prop));
    }
    
    
    class ListenerThread implements Runnable 
    {
        private boolean aborting;
        public void run() 
        {	
            aborting = false;
            try {
            	service = (StreamConnectionNotifier)Connector.open(serviceURL);
            	
            	// Add the service to the 'Public Browse Group'
            	ServiceRecord rec = ld.getRecord(service);
            	DataElement element = new DataElement(DataElement.DATSEQ);
            	element.addElement(new DataElement(DataElement.UUID, 
            	        PUBLIC_BROWSE_GROUP));
            	rec.setAttributeValue(BROWSE_GROUP_LIST, element);
            	
        		} catch(IOException e) {
                LOG.fatal("Cannot open StreamConnectionNotifier: "+e.getMessage());
                e.printStackTrace();
            }
        		LOG.info("Service created.");
        		
            while (!aborting) {
                try {
                    LOG.info("Waiting for connections.");
                    
                    StreamConnection conn = service.acceptAndOpen();
                    ConnectionWorker connectionWorker = new ConnectionWorker(conn);
                    connectionWorker.start();                                        
                } catch (IOException e) {
                    if (!aborting) {
                        LOG.error("Cannot open connection: "+e.getMessage());
                        e.printStackTrace();
                    }
                }                
            }
        }
        
        public void close() 
        {
            if (!aborting) {
                synchronized(this) {
                    aborting = true;
                }
                
                // This is kind of a hack, since we hope that closing the 
                // stream connection listener wakes up the blocked thread
                // waiting on acceptAndOpen()
                try {
                    service.close();                    
                } catch (IOException e) {
                    // Hmm, there is nothing we can do about that here.
                }
            }
            
        }
    }

    class ConnectionWorker extends Thread {
        private StreamConnection conn;
        public ConnectionWorker(StreamConnection c) 
        {
            this.conn = c;
        }
        
        public void run() 
        {	
            try {
	            RemoteDevice remoteDevice = RemoteDevice.getRemoteDevice(conn);
	            
	            LOG.info("remote device connected");
	            LOG.info("   "+remoteDevice.getBluetoothAddress()+" alias "+
	                     remoteDevice.getFriendlyName(false));
	            
	            InputStream in = conn.openInputStream();
	            OutputStream out = conn.openOutputStream();
	            
	            byte lengthbuffer[] = new byte[1];
	            in.read(lengthbuffer);
	            LOG.info("reading "+lengthbuffer[0]+" bytes");
	            byte msg[] = new byte[lengthbuffer[0]];
	            int readBytes = 0;
	            while (readBytes < lengthbuffer[0]) {
	                int count = in.read(msg, readBytes, msg.length-readBytes);
	                if (count == -1) {
	                    throw new IOException("Input Stream closed");
	                }
	                readBytes += count;
	            }
	            String message = new String(msg);
	            LOG.info("received message \""+message+"\"");
	            LOG.info("sending echo ("+lengthbuffer[0]+" bytes)");
	            out.write(lengthbuffer);
	            out.write(msg);

	            LOG.info("closing connection...");
	            in.close();
	            out.close();
	            conn.close();
            } catch(IOException e) {
               LOG.error("Exception occured in connection worker: "+e.getMessage());
               e.printStackTrace();  
            }
        }
    }

    /**
     * Called when a device is found during an inquiry. 
     * An inquiry searches for devices that are discoverable. 
     * The same device may be returned multiple times.
     * @param btdevice the device that was found during the inquiry
     * @param cod the service classes, major device class, 
     *        and minor device class of the remote device 
     */
    public void deviceDiscovered(RemoteDevice btdevice, DeviceClass cod)
    {
        try {
            LOG.info("BT device discovered: "+btdevice.getBluetoothAddress()+
                     " alias "+btdevice.getFriendlyName(false));
            
        } catch (IOException e) {
            LOG.error("error in deviceDiscovered: "+e.getMessage());
        }        
    }

    /** 
     * Called when service(s) are found during a service search.
     * @param transID the transaction ID of the service search that is posting the result
     * @param service a list of services found during the search request
     */
    public void servicesDiscovered(int transID, ServiceRecord[] service)
    {
        LOG.info("services discovered: ");
        LOG.info("  transID = "+transID);        
    }

    /**
     * Called when a service search is completed or was terminated because of an error. 
     * @param transID the transaction ID identifying the request which initiated the service search
     * @param respCode the response code that indicates the status of the transaction
     */
    public void serviceSearchCompleted(int transID, int respCode)
    {
        LOG.info("service search completed: ");
        LOG.info("  transID = "+transID);        
    }

    /**
     * Called when an inquiry is completed
     * @param discType the type of request that was completed; 
     *        either INQUIRY_COMPLETED, INQUIRY_TERMINATED, or INQUIRY_ERROR
     */
    public void inquiryCompleted(int discType)
    {
        if (discType == DiscoveryListener.INQUIRY_COMPLETED) {
            LOG.info("inquiryCompleted: INQUIRY_COMPLETED");
        } else if (discType == DiscoveryListener.INQUIRY_TERMINATED) {
            LOG.info("inquiryCompleted: INQUIRY_TERMINATED");
        } else {
            LOG.info("inquiryCompleted: INQUIRY_ERROR");
        }
        
    }
    
}
