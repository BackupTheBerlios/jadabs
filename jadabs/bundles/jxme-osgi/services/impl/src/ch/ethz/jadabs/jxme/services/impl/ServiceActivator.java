/*
 * Copyright (C) 2004 Andreas Frei
 *
 * This library is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; either version 2.1 
 * of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 *
 */
package ch.ethz.jadabs.jxme.services.impl;


import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.EndpointService;
import ch.ethz.jadabs.jxme.Peer;
import ch.ethz.jadabs.jxme.PeerGroup;
import ch.ethz.jadabs.jxme.PeerNetwork;
import ch.ethz.jadabs.jxme.services.GroupService;


/**
 *      
 * The ServiceActivator starts the extended JXME services by creating the
 * ResolverService and PipeService to the already generated PeerGroup.
 * 
 * The {@link ch.ethz.jadabs.jxme.Peer Peer} and {@link ch.ethz.jadabs.jxme.PeerGroup 
 * PeerGroup} is then published in the GroupService.
 * 
 * The {@link ResolverService Resolver} is used to query for advertisement, whereas with the 
 * {@link PipeService PipeService} connections to the other nodes in the same group can be opened.
 * 
 * @author andfrei
 *
 */
public class ServiceActivator implements BundleActivator
{
    private static Logger LOG = Logger.getLogger("ch.ethz.jadabs.jxme.services.ServiceActivator");
    
    static BundleContext bc;
    static GroupService groupService;
    static PeerNetwork peernetwork;
    
    ResolverService resServ;
    
    /*
     */
    public void start(BundleContext bc) throws Exception
    {
        ServiceActivator.bc = bc;
        
        // get EndpointService
        ServiceReference sref = bc.getServiceReference("ch.ethz.jadabs.jxme.EndpointService");
        EndpointService endptsvc = (EndpointService)bc.getService(sref);
        
        // get PeerNetwork
        sref = bc.getServiceReference("ch.ethz.jadabs.jxme.PeerNetwork");
        peernetwork = (PeerNetwork)bc.getService(sref);
        
        Peer peer = peernetwork.getPeer();
        PeerGroup group = peernetwork.getPeerGroup();
        
        // Initialize other core services and connect to the JXME Network
        resServ = ResolverService.createInstance(peer, endptsvc);
        PipeService pipeService = PipeService.createInstance(peer, endptsvc, resServ);

        // Create the default GroupService for teh default group
        groupService = GroupServiceImpl.createInstance(peer, group, resServ, pipeService);

        // register GroupService
        bc.registerService(
                "ch.ethz.jadabs.jxme.services.GroupService",
                groupService, null);
                
        // Also publish this Peer and the PeerGroup on the network
        groupService.remotePublish(peer);
        groupService.remotePublish(group);
        
        // start resolverservice Thread
        Thread resolverThread = new Thread(resServ);
        resolverThread.start();
        
    }

    /*
     */
    public void stop(BundleContext bc) throws Exception
    {
        resServ.stopPeerRefresh();
    }
}
