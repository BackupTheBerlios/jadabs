/************************************************************************
 *
 * $Id: ResolverService.java,v 1.3 2005/01/19 10:00:05 afrei Exp $
 *
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *       Sun Microsystems, Inc. for Project JXTA."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA"
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact Project JXTA at http://www.jxta.org.
 *
 * 5. Products derived from this software may not be called "JXTA",
 *    nor may "JXTA" appear in their name, without prior written
 *    permission of Sun.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Project JXTA.  For more
 * information on Project JXTA, please see
 * <http://www.jxta.org/>.
 *
 * This license is based on the BSD license adopted by the Apache
 * Foundation.
 **********************************************************************/

package ch.ethz.jadabs.jxme.services.impl;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.jxme.DiscoveryListener;
import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.EndpointService;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.MalformedURIException;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.Peer;
import ch.ethz.jadabs.jxme.PeerGroup;
import ch.ethz.jadabs.jxme.Pipe;
import ch.ethz.jadabs.jxme.Service;

/**
 * Class ResolverService- it basically searches for resources and returns the
 * advertisments to the user. Also deals with queries coming in from other peers
 * and makes response for them or fwds them to neighbors if needed. It handles
 * responses it gets from the other peers in response to its queries.
 * ResolverService is the only class that deals with the Cache class. It also
 * has a publish mechanism for publishing resources to the Cache.
 */

public class ResolverService extends Service implements Runnable, Listener
{

    private static final Logger LOG = Logger.getLogger("ch.ethz.jadabs.jxme.services.ResolverService");

    //---------------------------------------------------
    // Message tags
    //---------------------------------------------------
    
    //public static final String RESOLVER_RESPONSE = "ResolverResponse";
    
    
    //---------------------------------------------------
    // ResolverService static Fields
    //---------------------------------------------------
    
    private static final String RESSERVICE_NAME = "ResolverService";

    private static final int DEFAULT_NEIGHBORS = 3;

    private static final int DEFAULT_CACHE_SIZE = 20;

    private static final int DEFAULT_HOP_COUNT = 7;
    
    private static final int DEFAULT_BACKOFF = 2;
    
    private static final int DEFAULT_PEER_REFRESH = 5*1000;
    
    private static ResolverService resService = null;

    private static int nextRequestId = -1;

    private static int hopCount;

    private static int neighbors;

    private static int cacheSize;
    
    /**
     * Backoffnumber, the number of times a peer may send a SAE with its
     * wakeuptime.
     */
    protected int backoffno;

    private int peerrefresh;
    
    private final Hashtable queryTable = new Hashtable(); // already seen
                                                          // queries

    private final Hashtable queries = new Hashtable(); 
    	// [(listener, Query)]
    
    private EndpointService epService; // Endpoint Service

    private EndpointAddress[][] seedURI = null;

//    private EndpointAddress mcastURI;

    private String peerId = null;

    private boolean running = true;
    
    private Cache cache; // cache handler
    

    /**
     * This is the default constructor for Resolver service , intiates a cache ,
     * already seen queries table.Also sends hello mesg for connecting to the
     * Jxta network
     * 
     * @param seedPeerList
     *            list of URIs of the seed peer
     * @param peer
     *            peer is my peer object.
     * @param epService
     *            EndpointService Singleton Handler
     */

    private ResolverService(Peer peer, EndpointService epService)
    {
        super(peer, RESSERVICE_NAME);

        // init fields
        initFields();
        
        
        this.epService = epService;
        epService.addListener(serviceName, this); //adding listener to end
                                                  // point service

        cache = Cache.createInstance(); //creating cache
        cache.addResource(peer);
        

        // get SeedPeerList
        EndpointAddress[] seedPeerList = getSeedURIs();
        
        if (seedPeerList != null && seedPeerList.length > 0)
        {
            int seedListSize = (neighbors > seedPeerList.length ? seedPeerList.length : neighbors);
            seedURI = new EndpointAddress[seedListSize][1];
            for (int i = 0; i < seedListSize; i++)
            {
                seedURI[i][0] = seedPeerList[i];
            }
        }
//        try
//        {
//            mcastURI = new EndpointAddress(null, null, 0);
//        } catch (Exception e)
//        {
//            e.printStackTrace();
//        }

//        myPeer = peer;
        peerId = peer.getID().toString(); //TBD kuldeep - Is this needed?
//        peername = myPeer.getName();
    }

    /**
     * There needs to be one instance of ResolverService to resolve queries and
     * issue queries for Jxta peers. A Singleton object
     * 
     * @param peer
     *            my peer object
     * @param seedList -
     *            list of URIs of the seed peers
     * @param epService
     *            Singleton EndpointService Handler
     * 
     * @return createInstance returns the same instance of this service.
     */
    public static ResolverService createInstance(Peer peer, EndpointService epService)
    {
        if (resService == null)
        {
            resService = new ResolverService(peer, epService);
        }
        return resService;
    }
    
    private void initFields()
    {
        // hopcounts
        hopCount = 0;
        try
        {
            String str = ServiceActivator.bc.getProperty("ch.ethz.jadabs.jxme.services.hopCount");
            hopCount = Integer.parseInt(str);
        } catch (Throwable t)
        {
            /* do nothing */
        }
        if (hopCount < 1)
            hopCount = DEFAULT_HOP_COUNT;

        // neigbors
        neighbors = 0;
        try
        {
            String str = ServiceActivator.bc.getProperty("ch.ethz.jadabs.jxme.services.noNeighbors");
            neighbors = Integer.parseInt(str);
        } catch (Throwable t)
        {
            /* do nothing */
        }
        if (neighbors < 1)
            neighbors = DEFAULT_NEIGHBORS;

        // cacheSize
        cacheSize = 0;
        try
        {
            String str = ServiceActivator.bc.getProperty("ch.ethz.jadabs.jxme.services.queryTableSize");
            cacheSize = Integer.parseInt(str);
        } catch (Throwable t)
        {
            /* do nothing */
        }
        if (cacheSize < 1)
            cacheSize = DEFAULT_CACHE_SIZE;
                
        // backoff
        backoffno = 0;
        try
        {
            String str = ServiceActivator.bc.getProperty("ch.ethz.jadabs.jxme.services.backoff");
            backoffno = Integer.parseInt(str);
        } catch (Throwable t)
        {
            /* do nothing */
        }
        if (backoffno < 1)
            backoffno = DEFAULT_BACKOFF;
        
        // peerrefresh
        peerrefresh = DEFAULT_PEER_REFRESH;
        try
        {
            String str = ServiceActivator.bc.getProperty("ch.ethz.jadabs.jxme.services.peerrefresh");
            peerrefresh = Integer.parseInt(str);
        } catch (Throwable t)
        {
            /* do nothing */
        }
    }
    
    private void fireLostNamedResouce(NamedResource namedr)
    {
        String groupid = namedr.getID().getGroupID();
        
//        Listener listener = getListener(groupid);
//        if (listener instanceof GroupServiceImpl)
//        {
//            for (Enumeration en = ((GroupServiceImpl)listener).discListeners.elements();
//            	en.hasMoreElements();)
//            {
//                ((DiscoveryListener) en.nextElement()).
//                	handleNamedResourceLoss(namedr);
//        
//            }
//        }
        
        Vector listeners = ((GroupServiceImpl)ServiceActivator.groupService).discListeners;
        
        for (Enumeration en = listeners.elements(); en.hasMoreElements();)
	    {
	        ((DiscoveryListener) en.nextElement()).
	        	handleNamedResourceLoss(namedr);
	    }
        
    }
    
    private void sendPeerKeepAlive()
    {
        Peer peer = ServiceActivator.peernetwork.getPeer();
        PeerGroup group = ServiceActivator.peernetwork.getPeerGroup();
        
        remotePublish(peer, group);
    }

    public void run()
    {
        Peer peer = ServiceActivator.peernetwork.getPeer();
        peer.setLeaseOffset(peerrefresh);
        
        while(running)
	    {

            // send peer alive to update lease in all peergroups
            sendPeerKeepAlive();
                        
            /*
             * check the peer list for peers, that haven't responded for
             * three cycles
             */
            NamedResource[] nres = cache.getResources("", null, NamedResource.PEER, "Name","");
            for (int i = 0; i < nres.length; i++)
            {
                Peer chkpeer = (Peer)nres[i];
                                
                if ( !peer.getID().equals(chkpeer.getID()) &&
                     (chkpeer.getLastUsed() + backoffno * chkpeer.getLeaseOffset() < System.currentTimeMillis()))
                {
                    
                    // remove from cache
                    cache.removeResource(chkpeer);
                    
                    // notify registered listeners
                    fireLostNamedResouce(chkpeer);
                    
                    LOG.debug("lost peer in cache after timeout");
                    
                }
            }
         
            try
            {
                Thread.sleep(peerrefresh);
            } catch (InterruptedException ie)
            {
                LOG.error("JaclDiscovery:chronThread interrupted", ie);
                ie.printStackTrace();
            }
        }
    }
    
    
    void stopPeerRefresh()
    {
        running = false;
    }
    
    /**
     * searches resources in JXTA Network.
     * 
     * @param groupId
     *            to specify the search has to be made in which group
     * @param type
     *            type of Named Resource. One of {@link NamedResource#PEER}or
     *            {@link NamedResource#GROUP}or {@link NamedResource#PIPE}or
     *            {@link NAMEDRESOURCE#OTHER}.
     * 
     * @param attr
     *            What value are we searching on as in name,type,desc
     * @param query
     *            Value of the attr
     * @param threshold
     *            number of responces expected from each peer
     * 
     * @return resource the named resource being searched for
     * @throws IOException
     */
    NamedResource[] localSearch(String groupId, String type, String attr, 
            String value, int threshold, String serviceHandler)
            throws IOException
    {
        NamedResource[] cachedResources = cache.getResources("", groupId, type, attr, value);
//        if (cachedResources != null)
//        {
//            cachedResource.touch();
////            return resource;
//        }
        
        return cachedResources;
    }

    void remoteSearch(String groupId, String type, String attr, 
            String value, int threshold, String serviceHandler, 
            DiscoveryListener listener)
    	throws IOException
    {
        
        // register listener for the query arguments
        Query query = new Query(groupId, type, attr, value, 
                serviceHandler,listener);
        
        queries.put(listener, query);
        
	    //if resource not found in local cache
	    String requestId = getNextRequestId();
	    EndpointAddress[] myURIs = peer.getURIList();
	    Element[] elm = new Element[9 + myURIs.length];
	    //Element[] elm = new Element[9];
	                                
	
	    elm[0] = new Element(Message.MESSAGE_TYPE_TAG, Message.REQUEST_SEARCH,
	            Message.JXTA_NAME_SPACE);
	    elm[1] = new Element(Message.TYPE_TAG, type, 
	            Message.JXTA_NAME_SPACE);
	    elm[2] = new Element(Message.ATTRIBUTE_TAG, attr, 
	            Message.JXTA_NAME_SPACE);
	    elm[3] = new Element(Message.VALUE_TAG, value, 
	            Message.JXTA_NAME_SPACE);
	    elm[4] = new Element(Message.THRESHOLD_TAG, Integer.toString(threshold), 
	            Message.JXTA_NAME_SPACE);
	    elm[5] = new Element(Message.REQUESTID_TAG, requestId, 
	            Message.JXTA_NAME_SPACE);
	    elm[6] = new Element(Message.ID_TAG, peer.getID().toString(), 
	            Message.JXTA_NAME_SPACE);
	    elm[7] = new Element(Message.GROUP_ID_TAG, groupId, 
	            Message.JXTA_NAME_SPACE);
	    elm[8] = new Element(Message.HOPCOUNT_TAG, Integer.toString(DEFAULT_HOP_COUNT), 
	            Message.JXTA_NAME_SPACE);
	
	    for (int i = 0; i < myURIs.length; i++)
	    {
	        EndpointAddress uri = myURIs[i];
	        uri = new EndpointAddress(uri, serviceName, serviceHandler);
	        elm[9 + i] = new Element(NamedResource.URITAG + String.valueOf(i), uri.toString().getBytes(),
	                Message.JXTA_NAME_SPACE, null);
	    }
	    
	    send(elm, serviceHandler);
    }
    
    void cancelSearch(DiscoveryListener listener)
    {
        queries.remove(listener);
    }
    
    void publish(NamedResource res)
    {
        // cache first the resource locally
        cache.addResource(res);
    }
    /**
     * publish resource advertisment to the cache- has to be edited for diff
     * cache algorithm
     * 
     * @param res
     *            resource object to be cached
     */
    void remotePublish(NamedResource res, PeerGroup group)
    {
        // cache first the resource locally
        publish(res);
        
        // create Message Advertisement
//        Element[] elm = AdvertisementFactory.createAdvertisement(res, group);
        
        Element[] elm = res.advertisement("test","test","test",Integer.toString(1));
        
        try
        {
            // remotePublish should propagate the advertisement, no single send
            // required
            
            // add ResolverService as Service Handler to the URI
//            EndpointAddress[] uris = ServiceActivator.peernetwork.getPeer().getURIList();
//            for (int i = 0; i < uris.length; i++)
//            {
//                uris[i] = new EndpointAddress(uris[i], RESSERVICE_NAME, null);
//            }
//            epService.send(elm, uris);
            
            // propagagte advertisement
            epService.propagate(elm, new EndpointAddress(null, RESSERVICE_NAME, null));
            
        } catch (IOException e)
        {
            LOG.debug("advertisement could not be sent on an endpoint:"+e.getMessage());
        }
        
    }

    /**
     * Sends {@link Element}to next servicelayer.
     * 
     * If this peer is an island and SeedPeers are defined, it makes use of
     * the Seed URLs to send query Messages.
     * 
     * <p>
     * If no seed peers are defined and if this peer is an island, it resorts to
     * multicast, if supported by transport, to propagate query message.
     * 
     * <P>
     * If the network is established and this peer has neighbors, send() uses
     * neighbor information to send query message.
     * 
     * @param elm
     *            query message elements
     * @param serviceHandler
     *            of the service that generated query message.
     */
    private void send(Element[] elm, String serviceHandler) throws IOException
    {
        EndpointAddress[][] neighborURI = cache.getNeighbors(neighbors, peerId);
        if (neighborURI.length == 0)
        {
            // on bootstrapping there are no neighbors. Try seedhosts, if
            // defined
            neighborURI = seedURI;
        }
        
        //LOG.debug ("neighbor URI: " + URI.toString(neighborURI));
        //LOG.debug ("seed URI: " + URI.toString(seedURI));
        if (neighborURI != null && neighborURI.length != 0)
        {
            for (int i = 0; i < neighborURI.length; i++)
            {
                EndpointAddress[] uriList = neighborURI[i];
                for (int ndx = 0; ndx < uriList.length; ndx++)
                {
                    uriList[ndx] = new EndpointAddress(uriList[ndx], serviceName, serviceHandler);
                }
                try
                {
                    epService.send(elm, uriList);
                } catch (IOException e)
                {
                    LOG.warn("could not send message to URI!");
                }
            }
        } 
//        else 
//        {
       // always send also as multicast
            // I am an ISLAND. Try multicast if it works.
        
        try {
            EndpointAddress endptaddr = new EndpointAddress(null, serviceName, serviceHandler);
            epService.propagate(elm, endptaddr);
        }catch(IOException io)
        {
            LOG.warn("IOException in propagating message");
        }
//        } // else
    }

    /**
     * GetNextRequestId generates new id for each query
     * 
     * @return new id for query message
     */
    private static synchronized String getNextRequestId()
    {
        if (++nextRequestId < 0)
        {
            nextRequestId = 0;
        }
        String id = Integer.toString(nextRequestId);
        return id;
    }

    public static EndpointAddress[] getSeedURIs()
    {
        EndpointAddress[] seedarray;
        
        // read seedURIs to initialize connection
        String seeduristr = ServiceActivator.bc.getProperty("ch.ethz.jadabs.jxme.seedURIs");
        Vector seeduris = new Vector();
        
        int startindex = 0;
        int endindex;
        boolean finished = false;
        while (!finished && seeduristr != null)
        {
            endindex = seeduristr.indexOf(",", startindex);
            String seeduri;
            if (endindex == -1)
            {
                seeduri = seeduristr.substring(startindex);
                finished = true;
            }
            else
                seeduri = seeduristr.substring(startindex, endindex);
            
            startindex = endindex + 1;

            try
            {
                seeduris.add(new EndpointAddress(seeduri));
            } catch (MalformedURIException e)
            {
                LOG.error("seedURI wrong format: " + seeduri);
            }
        }
        
        seedarray = new EndpointAddress[seeduris.size()];
        for (int i = 0; i< seeduris.size(); i++)
            seedarray[i] = (EndpointAddress)seeduris.elementAt(i);

        
        return seedarray;
    }
    
    /**
     * handleMessage handles message passed from the endpoint service
     * 
     * @param message
     *            Message received
     */
    public void handleMessage(Message msg, String listenerId)
    {
        String messageType = popString(Message.MESSAGE_TYPE_TAG, msg);

        if (messageType.equals(Message.REQUEST_SEARCH))
        {
            handleRequest(msg, listenerId);
        } else if (messageType.equals(Message.REQUEST_RESOLVE))
        {
            handleResponse(msg, listenerId);
        } else
        {
            LOG.error("Ignoring message type: " + messageType);
        }
    }
    
    
    /**
     * Gets a response to a query sent earlier and creates a new pipe or peer
     * obj depending on the response and returns the Named Resource object to
     * the service which had asked for it.
     * 
     * @param msg
     *            message from the handle message
     * @param listenerId
     *            service parameters to further demux the mesg going up
     */
    private void handleResponse(Message msg, String listenerId)
    {
        String requestId = popString(Message.REQUESTID_TAG, msg);
        String type = popString(Message.TYPE_TAG, msg);
        
        NamedResource res = null;

        if (type.equals(NamedResource.PEER))
        {
            res = (Peer) new Peer();
        }

        //FIXME conversion of advertisment to resource object is done in each
        // of the resource
        // thus while converting to resource object we create an Id from the
        // byte array in the message recd
        // and while storing to cache, I store it hashed on the String version
        // of the ID..thus need to convert
        //ID again to String. My other option is to store the resource hashed
        // on ID object but then for every
        //comparison in the cache , i need to convert the ID to string as the
        // ID in the message is in a string
        // Its a trade off between when to do the conversion. Can be modified
        // later:)

        else if (type.equals(NamedResource.GROUP))
        {
            res = (PeerGroup) new PeerGroup();
        } else if (type.equals(NamedResource.PIPE))
        {
            res = (Pipe) new Pipe();
        }
        
        boolean available = false;
        
        if (res != null)
        {
            res.RevAdvertisment(msg.getElements());
            
            // resolved adv is its own peer, don't handle
            if (res.getID().equals(peer.getID()))
            {
                LOG.debug("got its own peer advertisement");
                return;
            }
            available = cache.addResource(res);
            res.touch();
        }

        if (!available)
        {
	        for(Enumeration en = queries.elements(); en.hasMoreElements();)
	        {
	            Query query = (Query)en.nextElement();
	            
	            if (res.matches(query.groupId, query.type, query.attr,query.value))
	                query.listener.handleSearchResponse(res);
	        }
        }
//        Listener listener = getListener(listenerId);
//        if (listener != null){
//            listener.handleSearchResponse(res);
//        }
        
    }

    // FOR DEBUGING
//    private void dumpQueryTable()
//    {
//        synchronized (queryTable)
//        {
//            LOG.debug("Table size is: " + queryTable.size());
//            Enumeration enum = queryTable.keys();
//            while (enum.hasMoreElements())
//            {
//                LOG.debug("querytable: " + (String) enum.nextElement());
//            }
//        }
//    }

    /**
     * Handles the search requests and sends fwd to neighbors if need arises (
     * in case it doesnt have the response) deals with already sent queries(as
     * in ignores them)
     * 
     * 
     * @param msg
     *            someone else's query mesg received by the peer
     */
    // NOTE: Kuldeep: I have fixed this method to some extent with hacks. Need
    // to revisit it
    // for a cleaner implementation.
    private void handleRequest(Message msg, String serviceHandler)
    {
        String queryId = popString(Message.REQUESTID_TAG, msg);
        String pId = popString(Message.ID_TAG, msg);
        if (peerId.equals(pId))
        {
            return;
        }
        if (pId == null)
        {
            return;
        }

        synchronized (queryTable)
        {
            if (queryTable.containsKey(pId + queryId))
            {
//                dumpQueryTable();
                return; // query from the peer has been seen
            }
            
            queryTable.put(pId + queryId, pId);
//            dumpQueryTable();
        }

        String gid = popString(Message.GROUP_ID_TAG, msg);
        String type = popString(Message.TYPE_TAG, msg);
        String attr = popString(Message.ATTRIBUTE_TAG, msg);
        String value = popString(Message.VALUE_TAG, msg);
        int threshold = popInt(Message.THRESHOLD_TAG, msg, 1); // TBD: currently
                                                               // ignored
        int hc = popInt(Message.HOPCOUNT_TAG, msg, 0);

        LOG.debug("-----------" + type + " query " + queryId + " from " + pId + "::" + gid + ", " + attr + " = "
                + value);

        // query now the cache for available resources
        NamedResource[] res;
        res = cache.getResources(pId, gid, type, attr, value);
        if (res != null)
        {
            LOG.debug("Response found: " + res.getClass().getName() + ": " + res);
        } else
        {
            LOG.debug("No response found");
        }
        
        if (res != null)
        {
            // take the first advertisement
            // TODO return a list of matching local advertisements
            
            
            int numElement = msg.getElementCount();
            EndpointAddress[] list = new EndpointAddress[1]; // TBD FIX IT.
                                                             // hardcoded for
                                                             // the moment. Need
                                                             // to fix it.
            
            
            // search for the first EndpointURI and send it back to that,
            // TODO search for an appropriate EndpointURI matching an own one
            Element[] queryElm = msg.getElements();
            for (int i = 0; i < numElement; i++)
            {
                String elName = new String(queryElm[i].getName());
                if (elName.startsWith(NamedResource.URITAG))
                {
                    String URIString = new String(queryElm[i].getData());
                    try
                    {
                        list[0] = EndpointAddress.createEndpointURI(URIString);
                        break;
                    } catch (Exception e)
                    {
                        System.err.println();
                    }
                }
            }
            
            try
            {
                for(int i = 0; i < res.length; i++)
                {
	                Element[] elm = res[i].advertisement(attr, value, queryId, Integer.toString(threshold));
	                Message m = new Message(elm);
	                LOG.debug("-------------send " + popString(Message.MESSAGE_TYPE_TAG, m) + " "
	                           + popString(Message.REQUESTID_TAG, m) + ":" + popString(Message.TYPE_TAG, m) + " to "
	                           + list[0]);
	                epService.send(elm, list);
                }
            } catch (Exception e)
            {
                LOG.error("Failed to respond: " + e);
                e.printStackTrace();
            }
        }
        // no resource found sending message fwd
        else
        {
            if (hc == 0)
            {                
                return;
            }
            hc--;

            // send the same request but with the hop count
            // decremented, there is no need to create a new array.
            Element[] elm = msg.getElements();

            for (int i = 0; i < elm.length; i++)
            {
                if (Message.HOPCOUNT_TAG.equals(elm[i].getName()))
                {
                    elm[i] = new Element(Message.HOPCOUNT_TAG, Integer.toString(hc), Message.JXTA_NAME_SPACE);
                    break;
                }
            }
            try
            {
                send(elm, serviceHandler);
            } catch (IOException e)
            {
                LOG.debug(e.getMessage());
            }
        }//if query not seen before
    }// end of method


    /*
     */
    public void handleSearchResponse(NamedResource namedResource)
    {
    }
    
    class Query
    {
        String groupId;
        String type;
        String attr;
        String value;
        String serviceHandler; 
        DiscoveryListener listener;
        
        Query(String groupId, String type, String attr, 
            String value, String serviceHandler, 
            DiscoveryListener listener)
        {
            this.groupId = groupId;
            this.type = type;
            this.attr = attr;
            this.value = value;
            this.serviceHandler = serviceHandler;
            this.listener = listener;
        }
    }
}