/*
 * Created on Jan 16, 2005
 *
 * $Id: MicroGroupServiceBundleImpl.java,v 1.5 2005/02/17 23:06:43 printcap Exp $
 */
package ch.ethz.jadabs.jxme.microservices;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

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
    private static Logger LOG = Logger.getLogger("ch.ethz.jadabs.jxme.microservices.MicroGroupServiceBundleImpl");
    
    /** the wiring protocol to connect to the core */
    private LocalWiringBundle wiring;
    
    /** the wiring connection to the core itself */
    private LocalWiringConnection connection;
    
    /** flag signaling dispatcher thread to shutdown */
    private boolean shutdown = false;
    
    /** internal group number of this group */
    private int groupNumber = 0;
    
    /** next sequence number to be used in requests */
    private short nextSequenceNumber = 0;
    
    /** response currently received  (-1 = none) */
    private short requestResponse = -1;        
    
    /** array containing data received from core */
    private byte[] responseData;
    
    /** table stores registered listeners (MicroDiscoveryListeners) */
    private Hashtable registeredMicroDiscoveryListeners;
    
    /** table stores registered listeners (MicroListeners) */
    private Hashtable registeredMircoListeners;
    
    
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
        registeredMicroDiscoveryListeners = new Hashtable();
        registeredMircoListeners = new Hashtable();
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
                out.writeShort(groupNumber);
                out.writeShort(Constants.PUBLISH);
                out.writeShort(0);	// dummy length
                out.writeUTF(resourceType);
                out.writeUTF(resourceName);
                out.writeUTF(stringID);
                out.close();
                bout.close();
                byte[] array = bout.toByteArray();
                array[6] = (byte)((array.length >> 8) & 0xff);
                array[7] = (byte)(array.length & 0xff);
                connection.sendBytes(array);
            } catch (IOException e) { 
                LOG.debug("cannot send PUBLISH message.");
                return;
            }
            
            // wait for reply
            while (requestResponse != myRequest) {
                try {
                    connection.wait();
                } catch (InterruptedException e) { }
            }  
            if (responseData[7] == 1) {
                LOG.error("published failed.");
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
                out.writeShort(groupNumber);
                out.writeShort(Constants.PUBLISH_REMOTE);
                out.writeShort(0);		// dummy length
                out.writeUTF(resourceType);
                out.writeUTF(resourceName);
                out.writeUTF(stringID);
                out.close();
                bout.close();
                byte[] array = bout.toByteArray();
                array[6] = (byte)((array.length >> 8) & 0xff);
                array[7] = (byte)(array.length & 0xff);
                connection.sendBytes(array);
            } catch (IOException e) { 
                LOG.debug("cannot send REMOTE_PUBLISH message.");
                return;
            }
            
            // wait for reply
            while (requestResponse != myRequest) {
                try {
                    connection.wait();
                } catch (InterruptedException e) { }
            }      
            if (responseData[7] == 1) {
                LOG.error("published failed.");
            }            
            // also notify dispatcher thread
            connection.notifyAll();
            requestResponse = -1;
        }    
    }

    /**
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
            out.writeShort(groupNumber);
            out.writeShort(Constants.LOCAL_SEARCH);
            out.writeShort(0);	// dummy length
            out.writeUTF(type);
            out.writeUTF(attribute);
            out.writeUTF(value);
            out.writeInt(threshold);
            out.close();
            bout.close();
            byte[] array = bout.toByteArray();
            array[6] = (byte)((array.length >> 8) & 0xff);
            array[7] = (byte)(array.length & 0xff);
            connection.sendBytes(array);
            
            // wait for reply
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
        // process response
        DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));
        din.readBoolean(); 	// skip, it is a reply message anyway
        short request = din.readShort();
        if (myRequest != request) {
            // match request field
            String error = "invalid reply received to LOCAL_SEARCH: request# do not match. "+
            	"expected: "+myRequest+", received: "+request;             
            LOG.error(error);
            throw new IOException(error);
        }
        short groupNr = din.readShort();
        short requestType = din.readShort();
        if (requestType != Constants.LOCAL_SEARCH) {
            String error = "invalid reply received to LOCAL_SEARCH: request type does not match. "+
         			"expected: "+Constants.LOCAL_SEARCH+", received: "+requestType;             
            LOG.error(error);
            throw new IOException(error);
        }
        boolean exception = din.readBoolean();
        if (exception) {
            String error = "localSearch failed";             
            LOG.error(error);
            throw new IOException(error);
        }
        din.readShort(); // skip message length
        short itemCount = din.readShort();
        result = new String[itemCount];
        for (int i=0; i<itemCount; i++) {
            result[i] = din.readUTF();
        }        
        din.close();        
        return result;
    }

    /**
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
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#remoteSearch(java.lang.String, java.lang.String, java.lang.String, int, ch.ethz.jadabs.jxme.microservices.MicroDiscoveryListener)
     */
    public void remoteSearch(String type, String attribute, String value, int threshold, MicroDiscoveryListener listener)
            throws IOException
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
            out.writeShort(groupNumber);
            out.writeShort(Constants.REMOTE_SEARCH);
            out.writeShort(0);	// dummy length
            out.writeUTF(type);
            out.writeUTF(attribute);
            out.writeUTF(value);
            out.writeInt(threshold);
            out.close();
            bout.close();
            byte[] array = bout.toByteArray();
            array[6] = (byte)((array.length >> 8) & 0xff);
            array[7] = (byte)(array.length & 0xff);
            connection.sendBytes(bout.toByteArray());
            
            // wait for response
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
        // process response
        DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));
        din.readBoolean(); 	// skip, it is a reply message anyway
        short request = din.readShort();
        if (myRequest != request) {
            // match request field
            String error = "invalid reply received to REMOTE_SEARCH: request# do not match. "+
            	"expected: "+myRequest+", received: "+request;             
            LOG.error(error);
            throw new IOException(error);
         }
         short groupNr = din.readShort();
         short requestType = din.readShort();
         if (requestType != Constants.REMOTE_SEARCH) {
             String error = "invalid reply received to REMOTE_SEARCH: request type does not match. "+
             	"expected: "+Constants.REMOTE_SEARCH+", received: "+requestType;             
             LOG.error(error);
             throw new IOException(error);
         }
         boolean exception = din.readBoolean();
         if (exception) {
            String error = "remoteSearch failed";             
            LOG.error(error);
            throw new IOException(error);
         }          
         int searchHandle = din.readInt();
         
         // register listener associated with this request 
         registeredMicroDiscoveryListeners.put(new Integer(searchHandle), listener);
    }

    /** 
     * Cancel search and unregister specified listener 
     * @param listener to unregister
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#cancelSearch(ch.ethz.jadabs.jxme.microservices.MicroDiscoveryListener)
     */
    public void cancelSearch(MicroDiscoveryListener listener)
    {
        // find listener locally 
        // (this is really ugly but on J2ME/MIDP there does not appear to exist a better solution)
        Enumeration keys= registeredMicroDiscoveryListeners.keys();
        Integer key = null; 
        boolean found = false;
        while (keys.hasMoreElements() && !found) {
            key = (Integer)keys.nextElement();
            if (registeredMicroDiscoveryListeners.get(key) == listener) {
                found = true;
            }
        }
        if (!found) {
            LOG.debug("cancelSearch listener not registered.");
            return;
        }
        int search_handle = key.intValue();
        
        //  prepare message and sent it
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);
        short myRequest;
        synchronized(connection) {
            myRequest = nextSequenceNumber++;
            try {
                out.writeShort(myRequest);                
                out.writeShort(groupNumber);
                out.writeShort(Constants.CANCEL_SEARCH);
                out.writeShort(0);		// dummy length
                out.writeInt(search_handle);
                out.close();
                bout.close();
                byte[] array = bout.toByteArray();
                array[6] = (byte)((array.length >> 8) & 0xff);
                array[7] = (byte)(array.length & 0xff);
                connection.sendBytes(array);
            } catch (IOException e) { 
                LOG.debug("cannot send CANCEL_SEARCH message.");
                return;
            }
            
            // remove listener locally 
            registeredMicroDiscoveryListeners.remove(key);
            
            // wait for reply
            while (requestResponse != myRequest) {
                try {
                    connection.wait();
                } catch (InterruptedException e) { }
            }      
            if (responseData[7] == 1) {
                LOG.error("cancel failed.");
            }            
            // also notify dispatcher thread
            connection.notifyAll();
            requestResponse = -1;
        }    
    }

    /**
     * Create and publish a {@link NamedResource#GROUP}
     * {@link NamedResource#PIPE}or a resource defined by Applications.
     * Typically, a resource defined by an application should be created by the
     * application itself.
     * 
     * @param resourceType
     *            one of {@link NamedResource#GROUP},
     *            {@link NamedResource#PIPE}or {@link NamedResource#OTHER}
     * 
     * @param resourceName
     *            the name of the resource being created, need not be unique
     * 
     * 
     * @param precookedID
     *            pre-defined id string  of the resource being created. Can be null.
     * 
     * @param arg
     *            an optional arg depending upon the type of resource being
     *            created. For example, for {@link NamedResource#PIPE}, this
     *            would be the type of {@link NamedResource#PIPE}that is to be
     *            created. For example, <code>JxtaUniCast</code> and
     *            <code>JxtaPropagate</code> are commonly-used values. This
     *            parameter can be <code>null</code>.
     * 
     * @return JXTA-ID string
     *  
     */
    public String create(String resourceType, String resourceName, String precookedID, String arg)
    {
        // prepare message and sent it
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);
        short myRequest;
        byte response[];
        String jxtaID = null;
        try {
            synchronized(connection) {        
                myRequest = nextSequenceNumber++;
                out.writeShort(myRequest);
                out.writeShort(groupNumber);
                out.writeShort(Constants.CREATE);
                out.writeShort(0);	// dummy length
                out.writeUTF(resourceType);
                out.writeUTF(resourceName);
                out.writeUTF(precookedID);
                out.writeUTF(arg);
                out.close();
                bout.close();
                byte[] array = bout.toByteArray();
                array[6] = (byte)((array.length >> 8) & 0xff);
                array[7] = (byte)(array.length & 0xff);
                connection.sendBytes(array);
		        
                // wait for reply
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
            // process response
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));
            din.readBoolean(); 	// skip, it is a reply message anyway
            short request = din.readShort();
            if (myRequest != request) {
                // match request field
                String error = "invalid reply received to CREATE: request# do not match. "+
		        			"expected: "+myRequest+", received: "+request;             
                LOG.error(error);
                throw new IOException(error);
            }
            short groupNr = din.readShort();
            short requestType = din.readShort();
            if (requestType != Constants.CREATE) {
                String error = "invalid reply received to CREATE: request type does not match. "+
		     			"expected: "+Constants.CREATE+", received: "+requestType;             
                LOG.error(error);
                throw new IOException(error);
            }
            boolean exception = din.readBoolean();
            din.readShort();	// skip length
            if (exception) {
                String error = "create failed";             
                LOG.error(error);
                throw new IOException(error);
            }
            jxtaID = din.readUTF();
            din.close();
        } catch(IOException e) { 
            LOG.error("IOException during create!");
        }		  
		  return jxtaID;
    }

    /**
     * Join a peer group and publishes peer's advertisement in the peer group.
     * 
     * A peer can join a group by issuing this request. Currently there is no
     * leave command, but could decide to leave the group if there are no more
     * active clients using that group.
     * 
     * <b> Note this method is not implemented yet. </b>
     * 
     * @param groupID
     *            JXTA-ID string of group to join. The group to be joined can be got by either: Creating
     *            it using the {@linl #create}or Searching a group
     *            advertisement using the {@link #search}
     * 
     * @param password
     *            the password required to join the group, if one is required.
     *            Otherwise, it is ignored. (Note: currently it is always
     *            ignored.
     * 
     * @return returns a new GroupService handler for the group joined. (currently it always returns zero)
     */
    public MicroGroupService join(String groupID, String password)
    {
        // prepare message and sent it
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);
        short myRequest;
        byte response[];
        String jxtaID = null;
        try {
            synchronized(connection) {        
                myRequest = nextSequenceNumber++;
                out.writeShort(myRequest);
                out.writeShort(groupNumber);
                out.writeShort(Constants.JOIN);
                out.writeShort(0);	// dummy length
                out.writeUTF(groupID);
                out.writeUTF(password);
                out.close();
                bout.close();
                byte[] array = bout.toByteArray();
                array[6] = (byte)((array.length >> 8) & 0xff);
                array[7] = (byte)(array.length & 0xff);
                connection.sendBytes(array);
		        
                // wait for reply
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
            // process response
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));
            din.readBoolean(); 	// skip, it is a reply message anyway
            short request = din.readShort();
            if (myRequest != request) {
                // match request field
                String error = "invalid reply received to JOIN: request# do not match. "+
		        			"expected: "+myRequest+", received: "+request;             
                LOG.error(error);
                throw new IOException(error);
            }
            short groupNr = din.readShort();
            short requestType = din.readShort();
            if (requestType != Constants.CREATE) {
                String error = "invalid reply received to JOIN: request type does not match. "+
		     			"expected: "+Constants.JOIN+", received: "+requestType;             
                LOG.error(error);
                throw new IOException(error);
            }
            boolean exception = din.readBoolean();
            din.readShort();	// skip length
            if (exception) {
                String error = "join failed";             
                LOG.error(error);
                throw new IOException(error);
            }
            int newgroupNumber = din.readShort();
            din.close();
        } catch(IOException e) { 
            LOG.error("IOException during create!");
        }		  
        return null;
    }

    /**
     * Send data to the specified Pipe.
     * 
     * @param pipeID,
     *            JXTA-ID string of {@link Pipe} to which data is to be sent.
     * 
     * @param data
     *            a {@link Message}containing an array of {@link Element}s
     *            which contain application data that is to be sent.
     * 
     * @throws IOException
     *             if there is a problem sending the message
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#send(java.lang.String, ch.ethz.jadabs.jxme.microservices.MicroMessage)
     */
    public void send(String pipeID, MicroMessage data) throws IOException
    {
        //  prepare message and sent it
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);
        short myRequest;
        synchronized(connection) {
            myRequest = nextSequenceNumber++;
            try {
                out.writeShort(myRequest);
                out.writeShort(groupNumber);
                out.writeShort(Constants.SEND);
                out.writeShort(0);		// dummy length
                out.writeUTF(pipeID);
                data.write(out);
                out.close();
                bout.close();
                byte[] array = bout.toByteArray();
                array[6] = (byte)((array.length >> 8) & 0xff);
                array[7] = (byte)(array.length & 0xff);
                connection.sendBytes(array);
            } catch (IOException e) { 
                LOG.debug("cannot send SEND message.");
                return;
            }
            
            // wait for reply
            while (requestResponse != myRequest) {
                try {
                    connection.wait();
                } catch (InterruptedException e) { }
            }      
            if (responseData[7] == 1) {
                LOG.error("send failed.");
            }            
            // also notify dispatcher thread
            connection.notifyAll();
            requestResponse = -1;
        }    
    }

    /**
     * Register a listener for the pipe and start listening on the pipe.
     * 
     * @param pipeID
     *            JXTA-ID {@link Pipe}on which to listen for incoming messages
     * 
     * @param listener
     *            listener for incoming messages.
     * 
     * @throws IOException
     *             if a communication error occurs
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#listen(java.lang.String, ch.ethz.jadabs.jxme.microservices.MicroListener)
     */
    public void listen(String pipeID, MicroListener listener) throws IOException
    {
        //  prepare message and sent it
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);
        short myRequest;
        synchronized(connection) {
            myRequest = nextSequenceNumber++;
            try {
                out.writeShort(myRequest);
                out.writeShort(groupNumber);
                out.writeShort(Constants.LISTEN);
                out.writeShort(0);		// dummy length
                out.writeUTF(pipeID);
                out.close();
                bout.close();
                byte[] array = bout.toByteArray();
                array[6] = (byte)((array.length >> 8) & 0xff);
                array[7] = (byte)(array.length & 0xff);
                connection.sendBytes(array);
            } catch (IOException e) { 
                LOG.debug("cannot send LISTEN message.");
                return;
            }
                                   
            // wait for reply
            while (requestResponse != myRequest) {
                try {
                    connection.wait();
                } catch (InterruptedException e) { }
            }      
            if (responseData[7] == 1) {
                LOG.error("listen failed.");
            } else {
                registeredMircoListeners.put(pipeID, listener);
            }
            // also notify dispatcher thread
            connection.notifyAll();
            requestResponse = -1;
        }         
    }

    /**
     * resolves an output pipe.
     * 
     * Waits for timeout period to resolve a pipe and returns back true if a
     * pipe is resolved, false other wise.
     * 
     * @param pipeID
     *            JXTA-ID of {@link Pipe} on which to listen for incoming messages
     * 
     * @param timeout
     *            in ms
     * 
     * @return true if a pipe is resolved, false otherwise.
     * 
     * @throws IOException
     *             if a communication error occurs
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#resolve(java.lang.String, int)
     */
    public boolean resolve(String pipeID, int timeout) throws IOException
    {
        // prepare message and sent it
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);
        short myRequest;
        byte response[];
        boolean resolved = false;
        try {
            synchronized(connection) {        
                myRequest = nextSequenceNumber++;
                out.writeShort(myRequest);
                out.writeShort(groupNumber);
                out.writeShort(Constants.RESOLVE);
                out.writeShort(0);	// dummy length
                out.writeUTF(pipeID);
                out.writeInt(timeout);
                out.close();
                bout.close();
                byte[] array = bout.toByteArray();
                array[6] = (byte)((array.length >> 8) & 0xff);
                array[7] = (byte)(array.length & 0xff);
                connection.sendBytes(array);
		        
                // wait for reply
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
            // process response
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(response));
            din.readBoolean(); 	// skip, it is a reply message anyway
            short request = din.readShort();
            if (myRequest != request) {
                // match request field
                String error = "invalid reply received to RESOLVE: request# do not match. "+
		        			"expected: "+myRequest+", received: "+request;             
                LOG.error(error);
                throw new IOException(error);
            }
            short groupNr = din.readShort();
            short requestType = din.readShort();
            if (requestType != Constants.CREATE) {
                String error = "invalid reply received to RESOLVE: request type does not match. "+
		     			"expected: "+Constants.RESOLVE+", received: "+requestType;             
                LOG.error(error);
                throw new IOException(error);
            }
            boolean exception = din.readBoolean();
            din.readShort();	// skip length
            if (exception) {
                String error = "resolve failed";             
                LOG.error(error);
                throw new IOException(error);
            }
            resolved = din.readBoolean();
            din.close();
        } catch(IOException e) { 
            LOG.error("IOException during create!");
        }	
        return resolved;
    }

    /**
     * Close a resource such as input Pipe. It removes any
     * listeners added for resource
     * 
     * @param stringID 
     * 				JXTA-ID string of resource to be closed
     * @throws IOException
     *             if a communication error occurs.
     * @see ch.ethz.jadabs.jxme.microservices.MicroGroupService#close(java.lang.String)
     */
    public void close(String stringID) throws IOException
    {
        // prepare message and sent it
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);
        short myRequest;
        synchronized(connection) {
            myRequest = nextSequenceNumber++;
            try {
                out.writeShort(myRequest);
                out.writeShort(groupNumber);
                out.writeShort(Constants.CLOSE);
                out.writeShort(0);	// dummy length
                out.writeUTF(stringID);
                out.close();
                bout.close();
                byte[] array = bout.toByteArray();
                array[6] = (byte)((array.length >> 8) & 0xff);
                array[7] = (byte)(array.length & 0xff);
                connection.sendBytes(array);
            } catch (IOException e) { 
                LOG.debug("cannot send CLOSE message.");
                return;
            }
            
            // locally unregister listener
            registeredMircoListeners.remove(stringID);
            
            // wait for reply
            while (requestResponse != myRequest) {
                try {
                    connection.wait();
                } catch (InterruptedException e) { }
            }  
            if (responseData[7] == 1) {
                LOG.error("close failed.");
            }
            // also notify dispatcher thread
            connection.notifyAll();
            requestResponse = -1;
        }
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
                if (d.length < 5) {
                    LOG.error("message with invalid length received from core (length="+d.length+")");
                    continue;
                }
                boolean isreply = d[0]==1;
                short request = (short)((d[1]<<8) | d[2]);
                short groupNumber = (short)((d[3]<<8) | d[4]);
                short type = (short)((d[5]<<8) | d[6]);
                if (isreply) {
                    // (synchronous) reply message                    
                    synchronized(connection) {
                        
                        while (requestResponse != -1) {
                            // wait until other threads have processed *last* response
                            try {
                                connection.wait();
                            } catch(InterruptedException e) { }                        
                        }
                        responseData = d;		// add message data to global buffer
                        requestResponse = request;
                        connection.notifyAll();
                    }
                } else {
                    // (asynchronous) message)
                    try{
                       DataInputStream din = new DataInputStream(new ByteArrayInputStream(d, 9, d.length-9));
	                    switch(type) {
	                    case Constants.SEARCH_RESPONSE:
	                    case Constants.NAME_RESOURCE_LOSS:
	                        int searchhandle = din.readInt();
	                        String resourceType = din.readUTF();
	                    		String resourceName = din.readUTF();
	                    		String resourceId   = din.readUTF();
	                    		din.close();
	                    		MicroDiscoveryListener listener = 
	                    		    	(MicroDiscoveryListener)registeredMicroDiscoveryListeners.get(
	                    		    	        new Integer(searchhandle));
	                    		if (listener != null) {
	                    		    if (type == Constants.SEARCH_RESPONSE) {
	                    		        listener.handleSearchResponse(resourceType, resourceName, resourceId);
	                    		    } else {
	                    		        listener.handleNamedResourceLoss(resourceType, resourceName, resourceId);
	                    		    }
	                    		}
	                        break;	                   
	                    case Constants.MESSAGE:
	                        String pipeID = din.readUTF();	                    		
	                    		MicroMessage micromessage = MicroMessage.read(din);
	                    		String listenerID = din.readUTF();
	                    		MicroListener l = (MicroListener)registeredMircoListeners.get(pipeID);
	                    		if (l != null) {
	                    		    l.handleMessage(micromessage, listenerID);
	                    		}
	                        break;
	                    default:
	                        LOG.error("skipping invalid ASYNC_MSG received, invalid type "+type);                    	
	                    }
                    } catch (IOException e) { /* cannot happen since stream is ByteArrayInputStream */ }                 
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
    
    /**
     * Wake up bundle core using local loopback wiring
     */
    public void wakeupCore() 
    {
        try {
            wiring.wakeupCore();
        } catch(IOException e) {
            LOG.error("cannot wake up core (IOException)");            
        }
    }
}