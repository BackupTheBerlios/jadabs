/**
 * $Id: PipeService.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 *
 * Copyright (c) 2003 Sun Microsystems, Inc.  All rights reserved.
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
 *********************************************************
 */

package ch.ethz.jadabs.jxme.services.impl;

import ch.ethz.jadabs.jxme.Pipe;
import ch.ethz.jadabs.jxme.Peer;
import ch.ethz.jadabs.jxme.EndpointService;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.Service;

import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Category;

/**
 * Class provides functionality to manage pipes. It allows one to send messages
 * on pipes. Also a listener can be registered that wants to rceive messages
 * from the PipeService. This class is used by the GroupService currently.
 * Expected to be Singleton in the current implementation
 */
public class PipeService extends Service implements Listener
{

    private static final Category LOG = Category.getInstance("ch.ethz.jadabs.jxme.services.PipeService");

    private static PipeService INSTANCE = null;

    private static final String PIPESERVICE_NAME = "PipeService";

    private final ResolverService resService;

    private final EndpointService epService;

    private final Vector unResolvedOwnerIdList = new Vector();

    private Peer pipeOwner = null;

    /**
     * Constructor initializes this service and registers it with the other
     * services.
     * 
     * @param epService
     * @param resService
     */
    private PipeService(Peer peer, EndpointService epService, ResolverService resService)
    {
        super(peer, PIPESERVICE_NAME);
        this.resService = resService;
        this.epService = epService;

        //Register with the lower layer services
        if (resService != null)
        {
            resService.addListener(serviceName, this);
        }
        if (epService != null)
        {
            epService.addListener(serviceName, this);
        }
    }

    /**
     * Create PipeService instance.
     * 
     * @param epService
     *            EndpointService used by pipe service
     * @param resService
     *            ResolverService for resolving pipes.
     * @return the singleton instance.
     */
    public static PipeService createInstance(Peer peer, EndpointService epService, ResolverService resService)
    {
        if (INSTANCE == null)
        {
            INSTANCE = new PipeService(peer, epService, resService);
        }
        return INSTANCE;
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
    boolean resolve(Pipe pipe, int timeout) throws IOException
    {
        EndpointAddress[] URIList = pipe.getResolvedURIList();
        if (URIList == null)
        {
            String ownerId = pipe.getOwnerId();
            synchronized (this)
            {
                // See if its in the local cache.
                pipeOwner = (Peer)resService.localSearch(
                        pipe.getID().getGroupID(), 
                        NamedResource.PEER, Message.ID_TAG,
                        ownerId, 1, serviceName)[0];
                
                if (pipeOwner == null)
                {
                    unResolvedOwnerIdList.addElement(ownerId);
                    try
                    {
                        wait(timeout);
                    } catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
                if (pipeOwner != null && pipeOwner.getID().toString().equals(ownerId))
                {
                    URIList = pipeOwner.getURIList();
                }
            }
            if (URIList == null) { return false; }
            pipe.setResolvedURIList(URIList);
        }
        return true;
    }

    /**
     * Send data to the specified Pipe.
     * 
     * @param pipe,
     *            {@link Pipe}to which data is to be sent.
     * 
     * @param message
     *            a {@link Message}containing an array of {@link Element}s
     *            which contain application data that is to be sent.
     * 
     * @throws IOException
     *             if there is a problem sending the message
     */
    void send(Pipe pipe, Message message) throws IOException
    {
        String ownerId = pipe.getOwnerId();
        LOG.debug("Pipe owner id: " + ownerId);

        EndpointAddress[] URIList = pipe.getResolvedURIList();
        if (URIList == null) { throw new IOException("unresolved Pipe"); }

        try
        {
            sendToEndpoint(message, pipe, URIList);
        } catch (IOException e)
        {
            // Couldn't sent to the URIList of the given Peer. May be the Peer
            // is dead.
            // May be this pipe is no longer associated with the previous
            // URIList.
            // Hence the resolved Peer for this pipe is set to null.
            pipe.setResolvedURIList(null);
            throw e;
        }
    }

    /**
     * Handle Peer Advertisements returned from the ResolverService after
     * seraching the network
     */
    public void handleSearchResponse(NamedResource namedResource)
    {
        LOG.debug("handleSearchResponse called...");
        if (namedResource == null)
            return;
        synchronized (this)
        {
            pipeOwner = (Peer) namedResource;
            String peerId = pipeOwner.getValueof(Peer.IDTAG);

            if (unResolvedOwnerIdList.contains(peerId))
            {
                unResolvedOwnerIdList.removeElement(peerId);
                notifyAll();
            }
        }
    }

    /**
     * Adds the PipeService serviceName and pipe id to the URI and sends it to
     * the Endpoint.
     * 
     * @param message
     * @param URIList
     */
    private void sendToEndpoint(Message message, Pipe pipe, EndpointAddress[] URIList) throws IOException
    {

        for (int index = 0; index < URIList.length; index++)
        {
            URIList[index] = new EndpointAddress(URIList[index], serviceName, pipe.getID().toString());
        }
        epService.send(message, URIList);
    }

    /**
     * For multicasting messages. This call would send the message to the entire
     * Peer Network. Uses the multicast facility provided the Transport via the
     * Endpoint.
     * 
     * @param elm
     * @throws IOException
     */
    void propagate(Message message, Pipe pipe) throws IOException
    {

        EndpointAddress destURI = new EndpointAddress(null, "MULTICAST", 0, serviceName, pipe
                .getValueof(NamedResource.IDTAG));

        epService.propagate(message.getElements(), destURI);
    }

    /**
     * Handle messages coming from EndpointService
     */
    public void handleMessage(Message message, String serviceParam)
    {
        LOG.debug("handleMessage called...");

        if (serviceParam == null)
        {
            LOG.debug("No PipeId in Message: Can't handle Message at PipeService");
            return;
        }

        //Forward the message to the upper layer
        Listener listener = getListener(serviceParam);
        if (listener == null)
        {
            LOG.debug("No Pipe Listener Found for the Message at PipeService");
            return;
        }

        listener.handleMessage(message, serviceParam);
    }
}