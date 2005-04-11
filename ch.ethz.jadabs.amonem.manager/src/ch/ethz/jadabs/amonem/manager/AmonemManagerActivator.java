/*
 * Copyright (c) 2003-2004, Jadabs project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * - Neither the name of the Jadabs project nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Created on Apr 2, 2004
 *
 */
package ch.ethz.jadabs.amonem.manager;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.amonem.AmonemManager;
import ch.ethz.jadabs.jxme.PeerGroup;
import ch.ethz.jadabs.jxme.PeerNetwork;
import ch.ethz.jadabs.jxme.services.GroupService;
import ch.ethz.jadabs.remotefw.FrameworkManager;
import ch.ethz.jadabs.servicemanager.ServiceManager;


//import ch.ethz.jadabs.remotefw.FrameworkManager;

/**
 * @author bam
 *  
 */
public class AmonemManagerActivator implements BundleActivator
{

    private Logger LOG = Logger.getLogger(AmonemManagerActivator.class.getName());
    
    static BundleContext bc;
    
    static PeerGroup pGroup;
    static GroupService gServ;
    /* remote fw manager */
    static FrameworkManager fwManager;
    
    static ServiceManager serviceManager;
    
//    static ch.ethz.jadabs.bundleloader.BundleLoader bundleLoader;

    static AmonemManager amonemManager;
        
    public void start(BundleContext bc) throws Exception
    {
        // add context
        AmonemManagerActivator.bc = bc;
//        AmonemManagerActivator.peername = bc.getProperty("ch.ethz.jadabs.jxme.peeralias");
        
        ServiceReference sref = bc.getServiceReference(GroupService.class.getName());
        gServ = (GroupService)bc.getService(sref);
        
        // FrameworkManager
        sref = bc.getServiceReference(FrameworkManager.class.getName());
        fwManager = (FrameworkManager) bc.getService(sref);
//        rmanager.getFrameworks();

        // Peernetwork
        sref = bc.getServiceReference(PeerNetwork.class.getName());
        PeerNetwork pnet = (PeerNetwork) bc.getService(sref);
        
        // PundleLoader
//        sref = bc.getServiceReference(ch.ethz.jadabs.bundleloader.BundleLoader.class.getName());
//        bundleLoader = (BundleLoader)bc.getService(sref);
//        
//        bundleLoader.addListener();
        
        // ServiceManager
        sref = AmonemManagerActivator.bc.getServiceReference(ServiceManager.class.getName());
        serviceManager = (ServiceManager)bc.getService(sref);
        
        
        // get The "worldgroup"
        pGroup = pnet.getPeerGroup();
        
        // create manager
        amonemManager = AmonemManagerImpl.Instance();
        
        // move to GUI to be started
//      amonemManager.start(pGroup, gServ, fwManager);
//      amonemManager.start();

        //register service
        bc.registerService(AmonemManager.class.getName(), amonemManager, null);
        
        
    }

    public void stop(BundleContext context) throws Exception
    {
        bc = null;
//        amonemDisc.stop();
        amonemManager = null;
    }

}