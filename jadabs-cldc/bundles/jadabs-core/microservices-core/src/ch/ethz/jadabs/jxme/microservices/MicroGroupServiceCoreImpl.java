/*
 * Created on Jan 16, 2005
 *
 * $Id: MicroGroupServiceCoreImpl.java,v 1.7 2005/05/06 15:50:31 afrei Exp $
 */
package ch.ethz.jadabs.jxme.microservices;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.core.wiring.ConnectionNotifee;
import ch.ethz.jadabs.core.wiring.LocalWiringConnection;
import ch.ethz.jadabs.core.wiring.LocalWiringCore;
import ch.ethz.jadabs.jxme.DiscoveryListener;
import ch.ethz.jadabs.jxme.ID;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.Peer;
import ch.ethz.jadabs.jxme.PeerGroup;
import ch.ethz.jadabs.jxme.Pipe;
import ch.ethz.jadabs.jxme.services.GroupService;


/**
 * This class is the implementation of the MicroGroupService on the core
 * side, i.e. the core skeleton.
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class MicroGroupServiceCoreImpl implements ConnectionNotifee
{

    /** Apache Log4J logger to be used in the MicroGroupServiceCoreImpl */
    private static Logger LOG = Logger.getLogger("ch.ethz.jadabs.jxme.microservices.MicroGroupServiceCoreImpl");
       
    /** the wiring protocol to connect to the bundles */
    private LocalWiringCore wiring;
    
    /** list containg all workers, i.e. BundWorker instances */
    private Vector workerList = new Vector();
    
    /** flag indication shut down of MicroGroup service */
    private boolean aborted = false;
    
    /** the JXME group service to use */
    private GroupService groupService;
    
    /** 
     * Hashtable containing NamedResource instances that are needed by the bundles served 
     * by this core.
     * (key: JXTA-ID string, table contains CacheItems) 
     */
    private Hashtable namedResourceTable = new Hashtable();    
    
    /** 
     * Hashtable containing all pending searches, i.e. SearchDiscoveryListeners as values 
     * and their search handles (Integer) as keys. 
     */
    private Hashtable pendingSearchesTable = new Hashtable();
    
    /**
     * Constructor creates new MicroGroupServiceCoreImpl instance
     */
    public MicroGroupServiceCoreImpl(GroupService groupService)
    {
        wiring = new LocalWiringCore(this);
        this.groupService = groupService;
        
    }
   
    /**
     * Connect to bundle at specified port number
     * @param port port where bundle has registered on push registry
     * @throws IOException if bundle cannot be connected
     */
    public void connect(int port) throws IOException 
    {
        wiring.connect(port);
    }
    
    /** 
     * looks up Pipe object given a Pipe ID
     * @param pipeIdString ID as String representation of pipe
     * @return Pipe object or null if pipe does not exist 
     */
    public synchronized Pipe lookupPipeByID(String pipeIdString) 
    {            
        CacheItem item = (CacheItem)namedResourceTable.get(pipeIdString);
        if ((item != null) && (item.res instanceof Pipe)) {
            return (Pipe)item.res;
        } else {
            return null;
        }
    }
    
    /** 
     * Registers a local GroupServiceListener with a pipe. This allows receiving 
     * locally sent messages. 
     * 
     * @param pipeIdString ID as String representation of pipe
     * @param localListener local GroupServiceListener that is registered 
     */
    public synchronized void registerLocalListener(String pipeIdString, 
            		GroupServiceListener localListener) 
    {
        CacheItem item = (CacheItem)namedResourceTable.get(pipeIdString);
        if ((item != null) && (item.res instanceof Pipe)) {
            item.registeredWorkers.addElement(localListener);            
        }
    }
    
    /** 
     * Unregisters a local GroupServiceListener worker from a pipe
     * specified by the bundle worker (removes all GroupServiceListeners
     * from that bundle worker registered with the specified pipe). 
     * 
     * @param pipeIdString ID as String representation of pipe
     * @param bundleWorker bundle worker that unregisters on this pipe 
     */
    public synchronized void unregisterLocalListener(String pipeIdString, 
            		BundleWorker bundleWorker) 
    {
        CacheItem item = (CacheItem)namedResourceTable.get(pipeIdString);
        if ((item != null) && (item.res instanceof Pipe)) {
            Enumeration listeners = item.registeredWorkers.elements();
            while (listeners.hasMoreElements()) {
                GroupServiceListener listener = (GroupServiceListener)listeners.nextElement();
                if (listener.bundleWorker == bundleWorker) {
                    item.registeredWorkers.removeElement(listener);
                }
            }                        
        }
    }    
    
    /**
     * Forward message send by one worker to all registered local listeners (possibly including
     * sender)
     * @param pipeIdString JXTA-ID string of pipe
     * @param message message to be forwarded
     */
    public synchronized void forwardMessageToLocalListeners(String pipeIdString, Message message) {
        CacheItem item = (CacheItem)namedResourceTable.get(pipeIdString);
            
        if ((item != null) && (item.res instanceof Pipe)) {
            Enumeration workers = item.registeredWorkers.elements();
            while (workers.hasMoreElements()) {
                GroupServiceListener listener =  (GroupServiceListener)workers.nextElement();
                listener.handleMessage(message, "");                
            }            
        }
    }
        
    /** 
     * looks up PeerGroup object given a Pipe ID
     * @param peerGroupIdString ID as String representation of PeerGroup
     * @return PeerGroup object or null if pipe does not exist 
     */
    public synchronized PeerGroup lookupPeerGroupByID(String peerGroupIdString) 
    {
        CacheItem item = (CacheItem)namedResourceTable.get(peerGroupIdString);
        if ((item != null) && (item.res instanceof PeerGroup)) {
            return (PeerGroup)item.res;
        } else {
            return null;
        }
    }
        
    /** 
     * looks up Peer object given a Peer ID
     * @param peerIdString ID as String representation of Peer
     * @return Peer object or null if peer does not exist 
     */
    public synchronized Peer lookupPeerByID(String peerIdString) 
    {
        CacheItem item = (CacheItem)namedResourceTable.get(peerIdString);
        if ((item != null) && (item.res instanceof Peer)) { 
            return (Peer)item.res;
        } else {
            return null;
        }
    }
    
    /** 
     * looks up OTHER (other than Peer, PeerGroup and Pipe) object given an ID
     * @param idString ID as String representation of OTHER
     * @return NamedResource of type OTHER object or null if not existing 
     */
    public synchronized NamedResource lookupOtherByID(String idString) 
    {
        CacheItem item = (CacheItem)namedResourceTable.get(idString);
        if ((item != null) && !(item.res instanceof Peer) && 
            !(item.res instanceof PeerGroup) && !(item.res instanceof Pipe)) { 
            return item.res;
        } else {
            return null;
        }
    }
    
    public synchronized void registerLocally(NamedResource res, Listener listener)
    {
        if (res != null) {
				String jxtaId = res.toString();
				CacheItem item = new CacheItem();
				item.refCount++;
				item.res = res;
				item.list = listener;
				
				// put resource into cache table
				namedResourceTable.put(jxtaId, item);	
        }
    }
    
    /**
     * Dereferences (decrement reference counter) specified NamedResource.
     * As soon as reference counter reaches zero the element is removed 
     * from the list. 
     * @param idString ID as string representation of the NamedResource
     */
    public synchronized void dereferenceNamedResource(String idString) {
        CacheItem item = (CacheItem)namedResourceTable.get(idString);
        if (item != null) {
            item.refCount--;
            if (item.refCount == 0) {
                // remove element from list 
                namedResourceTable.remove(idString);
            }
        }        
    }
    
    
    
    /** shut down core */
    public void stop()
    {
        // nothing to be done yet.
    }

    /** (non-Javadoc)
     * @see ch.ethz.jadabs.core.wiring.ConnectionNotifee#connectionEstablished(ch.ethz.jadabs.core.wiring.LocalWiringConnection)
     */
    public void connectionEstablished(LocalWiringConnection connection)
    {
        LOG.debug("new connection established. dispatching new worker.");
        BundleWorker worker = new BundleWorker(connection);
        Thread workerThread = new Thread(worker);
        
        // add worker to worker list
        synchronized(this) {
            workerList.addElement(worker);
        }
        
        // dispatch worker
        workerThread.start();
    }
    
    /*
     * 
     *  inner classes 
     *  
     */
    
    /**
     * Inner class that represents the worker thread which is created for each wire connection. 
     * 
     * @author Ren&eacute; M&uuml;ller
     */
    private class BundleWorker implements Runnable {
        
        /** the local wiring connection this worker is managing */
        private LocalWiringConnection connection;
        
        /**
         * Create new worker but which can be brought to live by calling 
         * its  <code>run</code> method
         * @param connection local wiring connection this worker will be using 
         */
        public BundleWorker(LocalWiringConnection connection) {
            this.connection = connection;                    
        }
        
        /**
         * This is the life of a worker (thread body)
         * @see java.lang.Runnable#run()
         */
        public void run()
        {
            while (!aborted) {
                
                try {
                    // block and wait for a message from the client (i.e. the bundle)
                    byte[] message = connection.receiveBytes();
                    ByteArrayInputStream bin = new ByteArrayInputStream(message);
                    DataInputStream din = new DataInputStream(bin);
                    
                    // read header  
                    short requestNumber = din.readShort();
                    short groupNumber = din.readShort();
                    short requestType = din.readShort();
                    short messageLength = din.readShort();
//                    System.out.println("requestType = "+requestType);
                    
                    // read rest of message and dispatch according to the request type
                    switch (requestType) {                    
                    // PUBLISH request
                    case Constants.PUBLISH:
                        String resourceType = din.readUTF();
                    		String resourceName = din.readUTF();
                    		String idString = din.readUTF();
                    		dispatchPublish(requestNumber, groupNumber, resourceType, resourceName, idString, false);                    		
                        break;
                    
                    // PUBLISH_REMOTE request
                    case Constants.PUBLISH_REMOTE:
                        resourceType = din.readUTF();
                			resourceName = din.readUTF();
                			idString = din.readUTF();
                			dispatchPublish(requestNumber, groupNumber, resourceType, resourceName, idString, true);                        
                        break;
                    
                    // LOCAL_SEARCH request
                    case Constants.LOCAL_SEARCH:
                        resourceType = din.readUTF();
            				String attribute = din.readUTF();
            				String value = din.readUTF();
            				int threshold = din.readInt();
            				dispatchLocalSearch(requestNumber, groupNumber, resourceType, attribute, value, threshold);
                        break;
                    
                    // REMOTE_SEARCH request
                    case Constants.REMOTE_SEARCH:
                        resourceType = din.readUTF();
        						attribute = din.readUTF();
        						value = din.readUTF();
        						threshold = din.readInt();
        						dispatchRemoteSearch(requestNumber, groupNumber, resourceType, attribute, value, threshold);
                        break;
                    
                    // CANCEL_SEARCH request
                    case Constants.CANCEL_SEARCH:      
                        int searchHandle = din.readInt();
    							dispatchCancelSearch(requestNumber, groupNumber, searchHandle); 
    							break;
                    
                    // CREATE request
                    case Constants.CREATE:
                        resourceType = din.readUTF();
            				resourceName = din.readUTF();
            				String precookedIdString = din.readUTF();
            				String argument = din.readUTF();
            				dispatchCreate(requestNumber, groupNumber, resourceType, resourceName, precookedIdString, argument);
                        break;
                    
                    // JOIN request
                    case Constants.JOIN:
                        String groupIdString = din.readUTF();
                    		String password = din.readUTF();
                    		dispatchJoin(requestNumber, groupNumber, groupIdString, password);
                        break;
                    
                    // SEND request
                    case Constants.SEND:
                        String pipeIdString = din.readUTF();
                    		Message mm = Message.read(din);
                    		dispatchSend(requestNumber, groupNumber, pipeIdString, mm);
                        break;
                    
                    // LISTEN request
                    case Constants.LISTEN:
                        pipeIdString = din.readUTF();
                        dispatchListen(requestNumber, groupNumber, pipeIdString);
                        break;
                    
                    // RESOLVE request
                    case Constants.RESOLVE:
                        pipeIdString = din.readUTF();
                    		int timeout = din.readInt();
                    		dispatchResolve(requestNumber, groupNumber, pipeIdString, timeout);
                        break;
                    
                    // CLOSE request 
                    case Constants.CLOSE:
                        pipeIdString = din.readUTF();
                    		dispatchClose(requestNumber, groupNumber, pipeIdString);
                        break;
                    
                    default:
                        LOG.error("bundle connection("+connection+"): invalid request type");                    
                    }
                    
                    din.close();
                    bin.close();                    
                } catch(IOException e) {
                    LOG.error("IOException in dispatcher connection("+connection+")");
                    aborted = true;
                }                
            }           
        }    
        
        /*
         * 
         * Dispatcher Methods
         * 
         */

        /**
         * Process PUBLISH message 
         * @param requestNumber sequence number vale of this request
         * @param groupNumber  internal group identifier
         * @param resourceType type of named resource (NamedResource.PEER, 
         *                     NamedResource.GROUP, NamedResource.PIPE or
         *                     NamedResource.OTHER)
         * @param resourceName Name string of named resource
         * @param idString     String representation of JXTA-ID
         * @param remote		 true = equivalent of a GroupService.remotePublish()
         *                     false = equivalent of a GroupService.publish();
         */
        public void dispatchPublish(short requestNumber, short groupNumber, String resourceType, 
                                    String resourceName, String idString, boolean remote)
        {
            boolean error = true;
            NamedResource namedResource = null;
            if (resourceType.equals(NamedResource.PEER)) {                
                namedResource = lookupPeerByID(idString);
            } else if (resourceType.equals(NamedResource.GROUP)) {
                namedResource = lookupPeerGroupByID(idString);
            } else if (resourceType.equals(NamedResource.PIPE)) {
                namedResource = lookupPipeByID(idString);
            } else {
                // resource type assumed OTHER (???)
                namedResource = lookupOtherByID(idString);
            }
            if (namedResource != null) {
                if (remote) {
                    groupService.remotePublish(namedResource);
                    LOG.debug("remote publishing '"+namedResource.toString());
                } else {
                    groupService.publish(namedResource);
                    LOG.debug("publishing '"+namedResource.toString());
                }
                error = false;
            } else {
                LOG.error("named resource "+idString+" not found.");
            }
            
            // Prepare reply
            byte[] reply = null;
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                DataOutputStream dout = new DataOutputStream(bout);
                dout.writeBoolean(true);	// is reply message
                dout.writeShort(requestNumber);
                dout.writeShort(groupNumber);
                dout.writeShort(remote?Constants.PUBLISH_REMOTE:Constants.PUBLISH);
                dout.writeBoolean(error);
                dout.writeShort(10);		// message length                 
                dout.close();
                bout.close();
                reply = bout.toByteArray();
            } catch (IOException e) {
                /* cannot happen since is byte array output stream */ 
            }            
            try {
                connection.sendBytes(reply);
            } catch(IOException e) {
                LOG.error("cannot send PUBLISH_REPLY.");
            }             
        }

        /**
         * Process CLOSE message 
         * @param requestNumber sequence number vale of this request
         * @param groupNumber internal group identifier
         * @param pipeIdString JXTA-ID string of pipe to be closed
         */
        public void dispatchClose(short requestNumber, short groupNumber, String pipeIdString)
        {
            boolean error = true;
                        
            Pipe pipe = lookupPipeByID(pipeIdString);
            dereferenceNamedResource(pipeIdString);
            unregisterLocalListener(pipeIdString, this);                    
            if (pipe == null) {
                LOG.error("Pipe with ID '"+pipeIdString+"' not found.");                
            } else {                
	            try {
	                groupService.close(pipe);
	                error = false;
	            } catch (IOException e) {
	            	LOG.error("Cannot close pipe "+pipeIdString);
	        		}
            }
            LOG.debug("Pipe with ID '"+pipeIdString+"' closed (locally).");
            
            // Prepare reply
            byte[] reply = null;
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                DataOutputStream dout = new DataOutputStream(bout);
                dout.writeBoolean(true);	// is reply message
                dout.writeShort(requestNumber);
                dout.writeShort(groupNumber);
                dout.writeShort(Constants.CLOSE);
                dout.writeBoolean(error);
                dout.writeShort(10);		// message length                 
                dout.close();
                bout.close();
                reply = bout.toByteArray();
            } catch (IOException e) {
                /* cannot happen since is byte array output stream */ 
            }            
            try {
                connection.sendBytes(reply);
            } catch(IOException e) {
                LOG.error("cannot send CLOSE_REPLY.");
            } 
        }

        /**
         * Process RESOLVE message 
         * @param requestNumber sequence number vale of this request
         * @param groupNumber internal group identifier
         * @param pipeIdString JAXTA-ID ID string to be resolved
         * @param timeout timeout to wait until result available in ms
         */
        public void dispatchResolve(short requestNumber, short groupNumber, String pipeIdString, int timeout)
        {
            boolean error = false;
            boolean success = false;
            Pipe pipe = lookupPipeByID(pipeIdString);
            if (pipe == null) {
                LOG.error("Pipe with ID '"+pipeIdString+"' not found.");                
            } else {                
	            try {
	                success = groupService.resolve(pipe, timeout);
	                error = false;
	            } catch (IOException e) {
	            	LOG.error("Cannot resolve pipe "+pipeIdString);
	        		}
            }
            // Prepare reply
            byte[] reply = null;
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                DataOutputStream dout = new DataOutputStream(bout);
                dout.writeBoolean(true);	// is reply message
                dout.writeShort(requestNumber);
                dout.writeShort(groupNumber);
                dout.writeShort(Constants.RESOLVE);
                dout.writeBoolean(error);
                dout.writeShort(11);		// messgae length
                dout.writeBoolean(success);
                dout.close();
                bout.close();
                reply = bout.toByteArray();
            } catch (IOException e) {
                /* cannot happen since is byte array output stream */ 
            }            
            try {
                connection.sendBytes(reply);
            } catch(IOException e) {
                LOG.error("cannot send RESOLVE_REPLY.");
            }
            
        }

        /**
         * Process LISTEN message 
         * @param requestNumber sequence number vale of this request
         * @param groupNumber internal group identifier
         * @param pipeIdString JXTA-ID String of pipe to be listened on
         */
        public void dispatchListen(short requestNumber, short groupNumber, String pipeIdString)
        {
            boolean error = true;
            Pipe pipe = lookupPipeByID(pipeIdString); 
            
            
            if (pipe == null) {
                LOG.error("Pipe with ID '"+pipeIdString+"' not found.");                
            } else {                
	            try {
	                GroupServiceListener listener = new GroupServiceListener(requestNumber, groupNumber, 
                           pipeIdString, this, connection);
	                groupService.listen(pipe, listener);
	                registerLocalListener(pipeIdString, listener);
	                LOG.debug("Listener added to Pipe "+pipeIdString);
	                error = false;
	            } catch (IOException e) {
	            	LOG.error("Cannot register listener to pipe "+pipeIdString);
	        		}
            }
            
            // Prepare reply
            byte[] reply = null;
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                DataOutputStream dout = new DataOutputStream(bout);
                dout.writeBoolean(true);	// is reply message
                dout.writeShort(requestNumber);
                dout.writeShort(groupNumber);
                dout.writeShort(Constants.LISTEN);
                dout.writeBoolean(error);
                dout.writeShort(10);		// message length                 
                dout.close();
                bout.close();
                reply = bout.toByteArray();
            } catch (IOException e) {
                /* cannot happen since is byte array output stream */ 
            }            
            try {
                connection.sendBytes(reply);
            } catch(IOException e) {
                LOG.error("cannot send LISTEN_REPLY.");
            }
        }

        /**
         * Process SEND message 
         * @param requestNumber sequence number vale of this request
         * @param groupNumber internal group identifier
         * @param pipeIdString JXTA ID string of pipe where the message is sent with 
         * @param message JXTA message 
         */
        public void dispatchSend(short requestNumber, short groupNumber, String pipeIdString, Message message)
        {
            
            
            
            boolean error = true;
            CacheItem item = (CacheItem)namedResourceTable.get(pipeIdString);          
            if (item == null) {
                LOG.error("Pipe with ID '"+pipeIdString+"' not found.");                
            } else {
                forwardMessageToLocalListeners(pipeIdString, message);
                
	            item.list.handleMessage(message, null);
            }
            
//            boolean error = true;
//            Pipe pipe = lookupPipeByID(pipeIdString);            
//            if (pipe == null) {
//                LOG.error("Pipe with ID '"+pipeIdString+"' not found.");                
//            } else {
//	            try {
//	                groupService.send(pipe, message);
//	                LOG.debug("sending message over pipe");
//	                error = false;
//	            } catch(IOException e) {
//	                LOG.error("groupservice: cannot send massage over pipe '"+pipe.toString()+"'");
//	                e.printStackTrace();
//	            }
//            }            
            LOG.debug("Sending message to '"+pipeIdString+"'.");
            
            // Prepare reply
//            byte[] reply = null;
//            try {
//                ByteArrayOutputStream bout = new ByteArrayOutputStream();
//                DataOutputStream dout = new DataOutputStream(bout);
//                dout.writeBoolean(true);	// is reply message
//                dout.writeShort(requestNumber);
//                dout.writeShort(groupNumber);
//                dout.writeShort(Constants.SEND);
//                dout.writeBoolean(error);
//                dout.writeShort(10);		// message length                 
//                dout.close();
//                bout.close();
//                reply = bout.toByteArray();
//            } catch (IOException e) {
//                /* cannot happen since is byte array output stream */ 
//            }            
//            try {
//                connection.sendBytes(reply);
//            } catch(IOException e) {
//                LOG.error("cannot send SEND_REPLY.");
//            }  
//            
//            // forward messages to local listeners
//            forwardMessageToLocalListeners(pipeIdString, message);
        }

        /**
         * Process JOIN message 
         * @param requestNumber sequence number vale of this request
         * @param groupNumber internal group identifier
         * @param groupIdString JXTA ID string of PeerGroup to join
         * @param password password required to join this group
         */
        public void dispatchJoin(short requestNumber, short groupNumber, String groupIdString, String password)
        {
            // TODO implement me   
            LOG.fatal("JOIN has not been implemented yet.");
        }

        /**
         * Process CREATE message 
         * @param requestNumber sequence number vale of this request
         * @param groupNumber internal group identifier
         * @param resourceType type of named resource (NamedResource.PEER, 
         *                     NamedResource.GROUP, NamedResource.PIPE or
         *                     NamedResource.OTHER)
         * @param resourceName name string of this new NamedResource
         * @param precookedIdString prelimary ID string
         * @param argument additional argument for NamedResource
         */
        public void dispatchCreate(short requestNumber, short groupNumber, String resourceType, String resourceName, String precookedIdString, String argument)
        {
            boolean error = true;
            String jxtaId = null;
            // create new resource 
            NamedResource res = groupService.create(resourceType, resourceName, new ID(precookedIdString), argument);            
            if (res != null) {
					jxtaId = res.getID().toString();
					LOG.debug("Pipe created with ID "+jxtaId);
					CacheItem item = new CacheItem();
					item.refCount++;
					item.res = res;
					
					// put resource into cache table
					namedResourceTable.put(jxtaId, item);
					error = false;		
            }
            
            // Prepare reply
            byte[] reply = null;
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                DataOutputStream dout = new DataOutputStream(bout);
                dout.writeBoolean(true);	// is reply message
                dout.writeShort(requestNumber);
                dout.writeShort(groupNumber);
                dout.writeShort(Constants.CREATE);
                dout.writeBoolean(error);
                dout.writeShort(0);		// dummy length
                dout.writeUTF(jxtaId);
                dout.close();
                bout.close();
                reply = bout.toByteArray();
                reply[8] = (byte)(0xff & (reply.length>>8));
                reply[9] = (byte)(0xff & reply.length);
            } catch (IOException e) {
                /* cannot happen since is byte array output stream */ 
            }            
            try {
                connection.sendBytes(reply);
            } catch(IOException e) {
                LOG.error("cannot send CREATE_REPLY.");
            }
        }

        /**
         * Process CANCEL message 
         * @param requestNumber sequence number vale of this request
         * @param groupNumber internal group identifier
         * @param searchHandle handle identifiying the search to be cancelled
         */
        public void dispatchCancelSearch(short requestNumber, short groupNumber, int searchHandle)
        {
            boolean error = true;            
            GroupServiceDiscoveryListener listener = null;
            
            // lookup GroupServiceDiscoveryListener that matches searchHandle
            listener = (GroupServiceDiscoveryListener)pendingSearchesTable.get(new Integer(searchHandle));
            if (listener != null) {
                groupService.cancelSearch(listener);
                error = false;
            }
            
            // Prepare reply
            byte[] reply = null;
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                DataOutputStream dout = new DataOutputStream(bout);
                dout.writeBoolean(true);	// is reply message
                dout.writeShort(requestNumber);
                dout.writeShort(groupNumber);
                dout.writeShort(Constants.CANCEL_SEARCH);
                dout.writeBoolean(error);
                dout.writeShort(10);		// message length                 
                dout.close();
                bout.close();
                reply = bout.toByteArray();
            } catch (IOException e) {
                /* cannot happen since is byte array output stream */ 
            }            
            try {
                connection.sendBytes(reply);
            } catch(IOException e) {
                LOG.error("cannot send CANCEL_SEARCH_REPLY.");
            }
        }

        /**
         * Process CANCEL message 
         * @param requestNumber sequence number vale of this request
         * @param groupNumber internal group identifier
         * @param resourceType type of named resource (NamedResource.PEER, 
         *        NamedResource.GROUP, NamedResource.PIPE or
         *        NamedResource.OTHER)
         * @param attribute the name of the attribute to search for.  
         * @param value an expression specifying the items being searched for and also
         *        limiting the scope of items to be returned.
         * @param threshold the maximum number of responses allowed from any one peer.
         */
        public void dispatchRemoteSearch(short requestNumber, short groupNumber, String resourceType, String attribute, String value, int threshold)
        {
            boolean error = true;
            int searchhandle = -1;
            try {              
                searchhandle = MicroGroupServiceCoreImpl.getNextSearchHandle();
                GroupServiceDiscoveryListener listener = new GroupServiceDiscoveryListener(requestNumber, 
                        groupNumber, searchhandle, connection);
                groupService.remoteSearch(resourceType, attribute, value, threshold, listener);                 
                pendingSearchesTable.put(new Integer(searchhandle), listener);
                error = false;                
            } catch (IOException e) {
	            	LOG.error("Error while performing remote serach ("+e.getMessage()+")");
        		}
            
            // Prepare reply
            byte[] reply = null;
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                DataOutputStream dout = new DataOutputStream(bout);
                dout.writeBoolean(true);	// is reply message
                dout.writeShort(requestNumber);
                dout.writeShort(groupNumber);
                dout.writeShort(Constants.REMOTE_SEARCH);
                dout.writeBoolean(error);               
                dout.writeShort(12);		// message length
                dout.writeInt(searchhandle);
                dout.close();
                bout.close();
                reply = bout.toByteArray();
            } catch (IOException e) {
                /* cannot happen since is byte array output stream */ 
            }            
            try {
                connection.sendBytes(reply);
            } catch(IOException e) {
                LOG.error("cannot send REMOTE_SEARCH_REPLY.");
            }
            
        }

        /**
         * Process LOCAL_SEARCH message 
         * @param requestNumber sequence number vale of this request
         * @param groupNumber internal group identifier
         * @param resourceType type of named resource (NamedResource.PEER, 
         *        NamedResource.GROUP, NamedResource.PIPE or
         *        NamedResource.OTHER)
         * @param attribute the name of the attribute to search for.  
         * @param value an expression specifying the items being searched for and also
         *        limiting the scope of items to be returned.
         * @param threshold the maximum number of responses allowed from any one peer.
         */
        public void dispatchLocalSearch(short requestNumber, short groupNumber, String resourceType, String attribute, String value, int threshold)
        {
            boolean error = true;
            NamedResource[] results = null;
            try {
                results = groupService.localSearch(resourceType, attribute, value, threshold);
                error = false;
                
            } catch (IOException e) {
	            	LOG.error("Error while performing local serach. type="+resourceType+", "+
	            	        "attr="+attribute+", value="+value);
        		}
            
            // Prepare reply
            byte[] reply = null;
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                DataOutputStream dout = new DataOutputStream(bout);
                dout.writeBoolean(true);	// is reply message
                dout.writeShort(requestNumber);
                dout.writeShort(groupNumber);
                dout.writeShort(Constants.LOCAL_SEARCH);
                dout.writeBoolean(error);
                dout.writeShort(0);		// dummy length
                if (!error) {
                    // write JXTA-IDs of resources found
                    dout.writeShort(results.length);
                    for(int i=0; i<results.length; i++) {
                        dout.writeUTF(results[i].toString());
                    }
                } else {
                    // error during search occurred
                    dout.writeShort(0);
                }
                dout.close();
                bout.close();
                reply = bout.toByteArray();
                reply[8] = (byte)(0xff & (reply.length>>8));
                reply[9] = (byte)(0xff & reply.length);
            } catch (IOException e) {
                /* cannot happen since is byte array output stream */ 
            }            
            try {
                connection.sendBytes(reply);
            } catch(IOException e) {
                LOG.error("cannot send LOCAL_SEARCH_REPLY.");
            }            
        }
    }
    
    /** 
     * Instances of this class subscribe with the GroupService and forward
     * all messsages received to the MicroGroupService.  
     * @author Ren&eacute; M&uuml;ller
     */
    private class GroupServiceListener implements Listener {

        private short requestNumber;
        private short groupNumber;
        private String pipeIdString;
        private LocalWiringConnection connection;
        BundleWorker bundleWorker;
        
        /** create new listener */
        GroupServiceListener(short requestNumber, short groupNumber, String pipeIdString, 
                             BundleWorker bundleWorker, LocalWiringConnection connection) {
            this.requestNumber = requestNumber;
            this.groupNumber = groupNumber;
            this.pipeIdString = pipeIdString;
            this.bundleWorker = bundleWorker;
            this.connection = connection;            
        }
        
        /** 
         * New message received 
         * @param message the message itself
         * @param listenerId associated listener
         * @see ch.ethz.jadabs.jxme.Listener#handleMessage(ch.ethz.jadabs.jxme.Message, java.lang.String)
         */
        public void handleMessage(Message message, String listenerId)
        {
            // Prepare MESSAGE_ASYNC_MSG message
            byte[] reply = null;
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                DataOutputStream dout = new DataOutputStream(bout);
                dout.writeBoolean(false);	// is an ASYNC_MSG message (not reply message)
                dout.writeShort(requestNumber);
                dout.writeShort(groupNumber);
                dout.writeShort(Constants.MESSAGE);
                dout.writeShort(0);		// dummy length
                dout.writeUTF(pipeIdString);
                message.write(dout);
                dout.writeUTF(listenerId);    
                dout.close();
                bout.close();
                reply = bout.toByteArray();
                reply[7] = (byte)(0xff & (reply.length>>8));

                reply[8] = (byte)(0xff & reply.length);
            } catch (IOException e) {
                /* cannot happen since is byte array output stream */ 
                LOG.debug("could not send message");
            }  
            
            try {
                connection.sendBytes(reply);
            } catch(IOException e) {
                LOG.error("cannot send MESSAGE_ASYNC_MSG.");
            }            
        }

        /**
         * Response for a search received (this method is currently not implemented)
         * @param namedResource resource found 
         * @see ch.ethz.jadabs.jxme.Listener#handleSearchResponse(ch.ethz.jadabs.jxme.NamedResource)
         */
        public void handleSearchResponse(NamedResource namedResource)
        {
            // TODO implement me
            // Andreas: What is needed here?
        }        
    }
    
    
    /** 
     * Instances of this class subscribe with the GroupService and forward
     * all messsages received to the MicroGroupService.  
     * @author Ren&eacute; M&uuml;ller
     */
    private class GroupServiceDiscoveryListener implements DiscoveryListener {

        private short requestNumber;
        private short groupNumber;        
        private int searchHandle;
        private LocalWiringConnection connection;
        
        /** create new listener */
        GroupServiceDiscoveryListener(short requestNumber, short groupNumber,  
                             int searchhandle, LocalWiringConnection connection) {
            this.requestNumber = requestNumber;
            this.groupNumber = groupNumber;
            this.searchHandle = searchhandle;
            this.connection = connection;            
        }

        /** 
         * The specified named resource was found. 
         * @param namedResource the resource that was found
         * @see ch.ethz.jadabs.jxme.DiscoveryListener#handleSearchResponse(ch.ethz.jadabs.jxme.NamedResource)
         */
        public void handleSearchResponse(NamedResource namedResource)
        {            
            // Prepare SEARCH_ASYNC_MSG message
            byte[] reply = null;
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                DataOutputStream dout = new DataOutputStream(bout);
                dout.writeBoolean(false);	// is an ASYNC_MSG message (not reply message)
                dout.writeShort(requestNumber);
                dout.writeShort(groupNumber);
                dout.writeShort(Constants.SEARCH_RESPONSE);
                dout.writeShort(0);		// dummy length
                dout.writeInt(searchHandle);
                dout.writeUTF(namedResource.getType());
                dout.writeUTF(namedResource.getName());
                dout.writeUTF(namedResource.toString());  // get JXTA-ID string
                dout.close();
                bout.close();
                reply = bout.toByteArray();
                reply[7] = (byte)(0xff & (reply.length>>8));
                reply[8] = (byte)(0xff & reply.length);
            } catch (IOException e) {
                /* cannot happen since is byte array output stream */ 
            }            
            try {
                connection.sendBytes(reply);
            } catch(IOException e) {
                LOG.error("cannot send SEARCH_ASYNC_MSG.");
            }
        }

        /**
         * The specified named resource was lost (e.g. turned off)
         * @param namedResource the named resource that was turned off 
         * @see ch.ethz.jadabs.jxme.DiscoveryListener#handleNamedResourceLoss(ch.ethz.jadabs.jxme.NamedResource)
         */
        public void handleNamedResourceLoss(NamedResource namedResource)
        {
            // Prepare NAMED_RESOURCE_LOSS_ASYNC_MSG message
            byte[] reply = null;
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                DataOutputStream dout = new DataOutputStream(bout);
                dout.writeBoolean(false);	// is an ASYNC_MSG message (not reply message)
                dout.writeShort(requestNumber);
                dout.writeShort(groupNumber);
                dout.writeShort(Constants.NAME_RESOURCE_LOSS);
                dout.writeShort(0);		// dummy length
                dout.writeInt(searchHandle);
                dout.writeUTF(namedResource.getType());
                dout.writeUTF(namedResource.getName());
                dout.writeUTF(namedResource.toString());  // get JXTA-ID string
                dout.close();
                bout.close();
                reply = bout.toByteArray();
                reply[7] = (byte)(0xff & (reply.length>>8));
                reply[8] = (byte)(0xff & reply.length);
            } catch (IOException e) {
                /* cannot happen since is byte array output stream */ 
            }            
            try {
                connection.sendBytes(reply);
            } catch(IOException e) {
                LOG.error("cannot send NAMED_RESOURCE_LOSS_ASYNC_MSG.");
            }            
        }                
    }
    
    
    /** instances of this class are used as elements in the named resources lists */
    private class CacheItem {
        NamedResource res;
        String id;
        int refCount = 0;
        Listener list;
        
        /** only for pipes: vector contains BundleWorkers that have registered with this pipe */
        Vector registeredWorkers = new Vector();
    }    
    
    /** sequence number used as remote search handles */
    private static int nextSearchHandle = 0;
    
    /**
     * Get next handle (identifier) for remote searches
     * @return next search handle available
     */
    protected synchronized static int getNextSearchHandle() {
        return nextSearchHandle++;
    }
}
