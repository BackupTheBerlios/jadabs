/*
 * Created on Jan 16, 2005
 *
 * $Id: MicroGroupServiceBundleImpl.java,v 1.2 2005/01/16 22:43:28 printcap Exp $
 */
package ch.ethz.jadabs.jxme.microservices;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.core.wiring.ConnectionNotifee;
import ch.ethz.jadabs.core.wiring.LocalWiringBundle;
import ch.ethz.jadabs.core.wiring.LocalWiringConnection;


/**
 * This class is the implementation of the MicroGroupService on the bundle
 * side, i.e. the bundle stub.
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class MicroGroupServiceBundleImpl implements MicroGroupService,
                           ConnectionNotifee
{
    /** Apache Log4J logger to be used in the MicroGroupServiceBundleImpl */
    private static Logger LOG = Logger.getLogger("MicroGroupServiceBundleImpl");
    
    /** the wiring protocol to connect to the core */
    private LocalWiringBundle wiring;
    
    /** the wiring connection to the core itself */
    private LocalWiringConnection connection;
    
    /** flag signaling dispatcher thread to shutdown */
    private boolean shutdown = false;
    
    /** next sequence number to be used in requests */
    private short nextSequenceNumber = 0;
    
    /** response currently received  (-1 = none) */
    private short requestResponse = -1;        
    
    /** array containing data received from core */
    private byte[] responseData;
    
    
    /**
     * Constructor creates new MicroGroupService BundleImpl
     */
    public MicroGroupServiceBundleImpl()
    {
        int port = -1;
        try {
            String portString = MicroGroupServiceBundleActivator.bc.getProperty("ch.ethz.jadabs.microservices.bundleport");
            if (portString != null) {
                port = Integer.parseInt(portString);
            }
        } catch(NumberFormatException e) {  }
        if (port < 0) {
            LOG.fatal("Listening port 'ch.ethz.jadsbs.microservices.bundleport' invalid or not specified.");
            return;
        }
        wiring = new LocalWiringBundle(port, this);        
    }       
    
    /**
     * Publishing a resource in the network using the resolver service into the 
     * local cache.
     * 
     * @param resourceType. 
     * 				One of {@link MicroGroupService.PEER},
     *            {@link MicroGroupService.GROUP}or {@link MicroGroupService.PIPE}or
     *            {@link MicroGroupService.OTHER}
     * @param resourceName 
     * 				the name of the entity being created 
     * @param stringID
     *            JXTA ID string for the resource
     */
    public void publish(String resourceType, String resourceName, String stringID)
    {
        // prepare message and sent it
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);
        short myRequest;
        synchronized(connection) {
            myRequest = nextSequenceNumber++;
            try {
                out.writeShort(myRequest);
                out.writeShort(Constants.PUBLISH);
                out.writeUTF(resourceType);
                out.writeUTF(resourceName);
                out.writeUTF(stringID);
                connection.sendBytes(bout.toByteArray());
            } catch (IOException e) { 
                LOG.debug("cannot send publish message.");
                return;
            }
            
            // wait for return, i.e. ack
            while (requestResponse != myRequest) {
                try {
                    connection.wait();
                } catch (InterruptedException e) { }
            }      
            // also notify dispatcher thread
            connection.notifyAll();
            requestResponse = -1;
        }
    }

    /**
     * Publishing a resource in the network using the resolver service by sending
     * an advertisement message.
     * 
     * @param resourceType. 
     * 				One of {@link MicroGroupService.PEER},
     *            {@link MicroGroupService.GROUP}or {@link MicroGroupService.PIPE}or
     *            {@link MicroGroupService.OTHER}
     * @param resourceName 
     * 				the name of the entity being created 
     * @param stringID
     *            JXTA ID string for the resource
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#remotePublish(java.lang.String, java.lang.String, java.lang.String)
     */
    public void remotePublish(String resourceType, String resourceName, String stringID)
    {
        //  prepare message and sent it
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);
        short myRequest;
        synchronized(connection) {
            myRequest = nextSequenceNumber++;
            try {
                out.writeShort(myRequest);
                out.writeShort(Constants.PUBLISH_REMOTE);
                out.writeUTF(resourceType);
                out.writeUTF(resourceName);
                out.writeUTF(stringID);
                connection.sendBytes(bout.toByteArray());
            } catch (IOException e) { 
                LOG.debug("cannot send publish message.");
                return;
            }
            
            // wait for return, i.e. ack
            while (requestResponse != myRequest) {
                try {
                    connection.wait();
                } catch (InterruptedException e) { }
            }      
            // also notify dispatcher thread
            connection.notifyAll();
            requestResponse = -1;
        }    
    }

    /*
     * Search for Peers, Groups, Pipes or Content resources defined by
     * Applications.
     * <p>
     * 
     * First, it searches in the local cache. If a match is found, NamedResource
     * is returned as the matching value. If a match is not found in the local
     * cache, query is propagated to peer's neighbor based on ResolverService
     * and a null value is returned.
     * 
     * @param type
     *            one of {@link NamedResource.PEER},
     *            {@link NamedResource.GROUP},{@link NamedResource.PIPE} or
     *            {@link NamedResource.OTHER}
     * 
     * @param attribute
     *            the name of the attribute to search for. This is one of the
     *            fields defined by a NamedResource and advertisements are
     *            indexed one. For example <code>NAME</code> or
     *            <code>ID</code> are usually used to search resources by name
     *            or id.
     * 
     * @param value
     *            an expression specifying the items being searched for and also
     *            limiting the scope of items to be returned. This is usually a
     *            simple regular expression such as, for example,
     *            <code>TicTacToe*</code> to search for all entities with
     *            names that begin with TicTacToe.
     * 
     * @param threshold
     *            the maximum number of responses allowed from any one peer.
     * 
     * @return JXTA-ID string if a match was found, null other wise.
     * 
     * @throws IOException
     *             if a communication error occurs with the the JXTA network
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#localSearch(java.lang.String, java.lang.String, java.lang.String, int)
     */
    public String[] localSearch(String type, String attribute, String value, int threshold) throws IOException
    {
        // prepare message and sent it
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);
        short myRequest;
        byte response[];
        String result[];
        synchronized(connection) {
            myRequest = nextSequenceNumber++;
            out.writeShort(myRequest);
            out.writeShort(Constants.LOCAL_SEARCH);
            out.writeUTF(type);
            out.writeUTF(attribute);
            out.writeUTF(value);
            out.writeInt(threshold);
            connection.sendBytes(bout.toByteArray());
            
            // wait for return, i.e. ack
            while (requestResponse != myRequest) {
                try {
                    connection.wait();
                } catch (InterruptedException e) { }
            }      
            response = responseData;
            // also notify dispatcher thread
            connection.notifyAll();
            requestResponse = -1;
        } 
        DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));
        din.readShort();	// skip request field
        boolean exception = din.readBoolean();
        if (exception) {
            throw new IOException(din.readUTF());
        }
        int itemCount = din.readInt();
        result = new String[itemCount];
        for (int i=0; i<itemCount; i++) {
            result[i] = din.readUTF();
        }        
        din.close();        
        return result;
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#remoteSearch(java.lang.String, java.lang.String, java.lang.String, int, ch.ethz.jadabs.jxme.microservices.MicroDiscoveryListener)
     */
    public void remoteSearch(String type, String attribute, String value, int threshold, MicroDiscoveryListener listener)
            throws IOException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#cancelSearch(ch.ethz.jadabs.jxme.microservices.MicroDiscoveryListener)
     */
    public void cancelSearch(MicroDiscoveryListener listener)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#create(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public String create(String resourceType, String resourceName, String precookedID, String arg)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#join(java.lang.String, java.lang.String)
     */
    public MicroGroupService join(String groupID, String password)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#send(java.lang.String, ch.ethz.jadabs.jxme.microservices.MicroMessage)
     */
    public void send(String pipeID, MicroMessage data) throws IOException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#listen(java.lang.String, ch.ethz.jadabs.jxme.microservices.MicroListener)
     */
    public void listen(String pipeID, MicroListener listener) throws IOException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#resolve(java.lang.String, int)
     */
    public boolean resolve(String pipeID, int timeout) throws IOException
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#close(java.lang.String)
     */
    public void close(String stringID) throws IOException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroListener#handleMessage(ch.ethz.jadabs.jxme.microservices.MicroMessage, java.lang.String)
     */
    public void handleMessage(MicroMessage message, String listenerId)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.microservices.MicroListener#handleSearchResponse(java.lang.String)
     */
    public void handleSearchResponse(String namedResourceName)
    {
        // TODO Auto-generated method stub

    }
    
    /** 
     * Stop MicroGroupService bundle.
     */
    public void stop()
    {
        shutdown = true;
        if (wiring.isConnected()) {
            connection.close();
            wiring.close();
        }
    }
    
    /** 
     * Called by the local wiring when the core connects to this 
     * 
     * @see ch.ethz.jadabs.core.wiring.ConnectionNotifee#connectionEstablished(ch.ethz.jadabs.core.wiring.LocalWiringConnection)
     */
    public void connectionEstablished(LocalWiringConnection connection)
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("core established a connection to this bundle.");
            LOG.debug("starting dispatcher thread.");
        }
        this.connection = connection;
        (new Thread(new DispatcherThread())).start();
    }
    
    /** This thread reads data from the input stream an calls the appropriate methods */
    private class DispatcherThread implements Runnable {     
        /** runbody of thread */
        public void run() {
            while (!shutdown) { 
                byte d[];
                try {
                    d = connection.receiveBytes();
                } catch(IOException e) {
                    LOG.error("error in dispatcher thread when reading data.");
                    return;
                }
                short request = (short)((d[0]<<8) | d[1]);
                synchronized(connection) {
                    while (requestResponse != -1) {
                        // wait until other threads have processed *last* response
                        try {
                            connection.wait();
                        } catch(InterruptedException e) { }
                    }
                    responseData = d;
                    requestResponse = request;
                    connection.notifyAll();
                }
            }
        }    
    }
    
    
    /* helper methods for marshalling and unmarshalling */ 
    /**
     * Send marshalled parameters to core 
     * @param out ByteArrayOutputStream to send 
     */
    public void send(ByteArrayOutputStream out) 
    {
        synchronized(connection) {
            byte []data = out.toByteArray();
            data[0] = (byte)(0xff & (nextSequenceNumber >> 8));
            data[1] = (byte)(0xff & nextSequenceNumber);
            nextSequenceNumber++;
            try {
                connection.sendBytes(data);
            } catch(IOException e) {
                MicroGroupServiceBundleImpl.LOG.error("cannot send data.");
            }
        }
    }
}
