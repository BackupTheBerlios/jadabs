/************************************************************************
 *
 * $Id: GroupServiceImpl.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
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
import java.util.Vector;

import ch.ethz.jadabs.jxme.DiscoveryListener;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.ID;
import ch.ethz.jadabs.jxme.Service;

import ch.ethz.jadabs.jxme.Peer;
import ch.ethz.jadabs.jxme.Pipe;
import ch.ethz.jadabs.jxme.PeerGroup;
import ch.ethz.jadabs.jxme.services.GroupService;

import org.apache.log4j.Logger;

/**
 * GroupService - provides Group Management. GroupService maintains group
 * specific state and makes sure that all the activities are restricted with in
 * a group. Defines scope of activities.
 */
public class GroupServiceImpl extends Service implements GroupService
{

    private static final Logger LOG = Logger.getLogger("ch.ethz.jadabs.jxme.services.GroupService");

    private final ResolverService resServ;

    private final PipeService pipeServ;

    private final PeerGroup myGroup;

    private Vector discListeners;

    /**
     * Constructor initializes this service and registers it with the other
     * services.
     * 
     * @param resServ
     * @param pipeServ
     */
    private GroupServiceImpl(Peer peer, PeerGroup group, ResolverService resServ, PipeService pipeServ)
    {
        super(peer, group.getID().toString());

        this.resServ = resServ;
        this.pipeServ = pipeServ;
        myGroup = group;
        discListeners = new Vector();

        // register group in cache
        Cache.createInstance().addResource(group);
        
        //Register with the lower layer services
        resServ.addListener(serviceName, this);
        pipeServ.addListener(serviceName, this);
    }

    /**
     * Create GroupService instance.
     * 
     * @param ResolverService
     * @param PipeService
     * @return new instance of GroupService
     */
    public static GroupServiceImpl createInstance(Peer peer, PeerGroup group, ResolverService resServ, PipeService pipeServ)
    {

        return new GroupServiceImpl(peer, group, resServ, pipeServ);
    }

    /**
     * Publishing a resource in the network using the resolver service
     * 
     * @param resource
     *            advertisement to be published
     */
    public void publish(NamedResource res)
    {
        // Should we put the check that pipes should be published only in the
        // group
        // in which they are created !

        if (res != null)
        {
            // Set the group ID of the resource to this group. So the
            // advertisement
            // of the resource is published only in this group.
            resServ.publish(res);
        }
    }

    public void remotePublish(NamedResource res)
    {

        if (res != null)
        {
            resServ.remotePublish(res, myGroup);
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
     * @return NamedResource if a match was found, null other wise.
     * 
     * @throws IOException
     *             if a communication error occurs with the the JXTA network
     */
    public NamedResource[] localSearch(String type, String attribute, 
            String value, int threshold) throws IOException
    {

        return (NamedResource[]) resServ.localSearch(myGroup.getID().getResourceID(), type, attribute,
                value, threshold, serviceName);
    }
    
    public void remoteSearch(String type, String attribute, 
            String value, int threshold, DiscoveryListener listener) throws IOException
    {

        resServ.remoteSearch(myGroup.getID().getResourceID(), type, attribute,
                value, threshold, serviceName, listener);
    }
    
    public void cancelSearch(DiscoveryListener listener)
    {
        resServ.cancelSearch(listener);
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
     *            pre-defined id of the resource bieng created. Can be null.
     * 
     * @param arg
     *            an optional arg depending upon the type of resource being
     *            created. For example, for {@link NamedResource#PIPE}, this
     *            would be the type of {@link NamedResource#PIPE}that is to be
     *            created. For example, <code>JxtaUniCast</code> and
     *            <code>JxtaPropagate</code> are commonly-used values. This
     *            parameter can be <code>null</code>.
     * 
     * @return NamedResource.
     *  
     */
    public NamedResource create(String resourceType, String resourceName, ID precookedID, String arg)
    {
        ID id = null;
        NamedResource resource = null;

        if (precookedID == null)
            id = new ID(resourceType, myGroup.getID());
        else
        {
            id = precookedID;
            id.setGroupID(myGroup.getID().getResourceID());
        }
        
        // create Peer
        if (resourceType.equals(NamedResource.PEER))
        {
            LOG.error("Cannot create resource of type:" + resourceType);
            throw new IllegalArgumentException("Peer cannot be created.");
        }
        // create Pipe
        else if (resourceType.equals(NamedResource.PIPE))
        {
            if (!arg.equals(Pipe.UNICAST) && !arg.equals(Pipe.PROPAGATE)) { 
                throw new IllegalArgumentException( "Invalid Pipe Type"); }
            resource = new Pipe(resourceName, id, peer.getID(), arg);
        } 
        // create Group
        else if (resourceType.equals(NamedResource.GROUP))
        {
            resource = new PeerGroup(resourceName, id, arg);
        }
        
        if (resource != null)
        {
            resServ.publish(resource);
        }
        
        return resource;
    }

    /**
     * Join a peer group and publishes peer's advertisement in the peer group.
     * 
     * A peer can join a group by issuing this request. Currently there is no
     * leave command, but could decide to leave the group if there are no more
     * active clients using that group.
     * 
     * @param Group
     *            to join. The group to be joined can be got by either: Creating
     *            it using the {@linl #create}or Searching a group
     *            advertisement using the {@link #search}
     * 
     * @param password
     *            the password required to join the group, if one is required.
     *            Otherwise, it is ignored. (Note: currently it is always
     *            ignored.
     * 
     * @return returns a new GroupService handler for the group joined.
     */
    public GroupService join(PeerGroup group, String password)
    {
        if (group == null) { return null; }

        // Create a new Peer advertisement for this group and publish it
        ID peerID = new ID(NamedResource.PEER, group.getID());
        Peer newpeer = new Peer(peer.getURIList(), peer.getName(), peerID);

        GroupServiceImpl groupService = new GroupServiceImpl(peer, group, resServ, pipeServ);
        // Publish myPeer advertisement in this new group
        groupService.publish(peer);

        return groupService;
    }

    /**
     * Send data to the specified Pipe.
     * 
     * @param pipe,
     *            {@link Pipe}to which data is to be sent.
     * 
     * @param data
     *            a {@link Message}containing an array of {@link Element}s
     *            which contain application data that is to be sent.
     * 
     * @throws IOException
     *             if there is a problem sending the message
     */
    public void send(Pipe pipe, Message data) throws IOException
    {

        if (!((pipe.getID().getGroupID()).equals(myGroup.getID().getResourceID())))
            throw new IOException("ERROR: this pipe is not for this group");

        pipeServ.send(pipe, data);
    }

    /**
     * Register a listener for the pipe and start listening on the pipe.
     * 
     * @param pipe
     *            {@link Pipe}on which to listen for incoming messages
     * 
     * @param listener
     *            listener for incoming messages.
     * 
     * @throws IOException
     *             if a communication error occurs
     */
    public void listen(Pipe pipe, Listener listener) throws IOException
    {

        if (!((pipe.getID().getGroupID()).equals(myGroup.getID().getResourceID())))
            throw new IOException("ERROR: this pipe is not for this group");

        pipeServ.addListener(pipe.getID().toString(), listener);
    }

    /**
     * resolves an output pipe.
     * 
     * Waits for timeout period to resolve a pipe and returns back true if a
     * pipe is resolved, false other wise.
     * 
     * @param pipe
     *            {@link Pipe}on which to listen for incoming messages
     * 
     * @param timeout
     *            in ms
     * 
     * @return true if a pipe is resolved, false otherwise.
     * 
     * @throws IOException
     *             if a communication error occurs
     */
    public boolean resolve(Pipe pipe, int timeout) throws IOException
    {
        if (!((pipe.getID().getGroupID()).equals(myGroup.getID().getResourceID())))
            throw new IOException("ERROR: this pipe is not for this group");

        return pipeServ.resolve(pipe, timeout);
    }

    /**
     * Close a {@link NamedResource}, such as input Pipe. It removes any
     * listeners added for resource
     * 
     * @param {@link NamedResource}
     *            to be closed
     * 
     * @throws IOException
     *             if a communication error occurs.
     */
    public void close(NamedResource res) throws IOException
    {

        if (!((res.getID().getGroupID()).equals(myGroup.getID().getResourceID()))) { throw new IOException(
                "ERROR: this resource is not for this group"); }
        if (res.getType().equals(NamedResource.PIPE))
        {
            pipeServ.removeListener(res.getID().toString());
        }
    }

    /*
     * not implemented for Group Service as never talks directly to Endpoint
     * Service
     */
    public void handleMessage(Message msg, String listenerID)
    {
        LOG.debug("handle Message called. WHY? ");
    }

    public void handleSearchResponse(NamedResource namedResource)
    {
        LOG.debug("discovered: " + namedResource.toString());
        for (int i = 0; i < discListeners.size(); i++)
        {
            DiscoveryListener listener = (DiscoveryListener) discListeners.elementAt(i);
            listener.handleSearchResponse(namedResource);
        }
    }

    /**
     * Adds a {@link DiscoveryListener}for discoveries in a peer group.
     * 
     * @param listener
     *            {@link DiscoveryListener}for handling query responces.
     */
    public void addDiscoveryListener(DiscoveryListener listener)
    {
        if (listener != null)
        {
            discListeners.addElement(listener);
        }
    }

    /**
     * Removes a {@link DiscoveryListener}
     * 
     * @param {@link DiscoveryListener}
     *            registered for handling query responses.
     */
    public void removeDiscoveryListener(DiscoveryListener listener)
    {
        if (listener != null)
        {
            discListeners.removeElement(listener);
        }
    }
}