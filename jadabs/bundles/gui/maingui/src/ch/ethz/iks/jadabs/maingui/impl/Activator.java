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
package ch.ethz.iks.jadabs.maingui.impl;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.iks.jadabs.maingui.SwtManager;
import ch.ethz.jadabs.remotefw.FrameworkManager;

/**
 * @author andfrei
 *  
 */
public class Activator implements BundleActivator
{

    static BundleContext bc;

    static SwtManagerImpl ui;
    
    static MainGUI maingui;

    /* RemoteFramework */
    static FrameworkManager rmanager;

    static String peername;
    
//    static PeerNetwork peernetwork;
//    static GroupService wgsvc;
    
    /*
     *
     */
    public void start(BundleContext bc) throws Exception
    {
        // add context
        Activator.bc = bc;
        Activator.peername = bc.getProperty("ch.ethz.jadabs.jxme.peeralias");
        
        // instantiate the service
        ui = new SwtManagerImpl();
        ui.start();

        // FrameworkManager
        ServiceReference srefrm = Activator.bc.getServiceReference(FrameworkManager.class.getName());
        rmanager = (FrameworkManager) bc.getService(srefrm);

//        // Get WorldPeerGroup
//        ServiceReference srefwg = ShellActivator.bc.getServiceReference("WorldPeerGroup");
//        wgsvc = (GroupService)ShellActivator.bc.getService(srefwg);
//        
//        // get PeerNetwork
//        ServiceReference srefpnet = bc.getServiceReference(PeerNetwork.class.getName());
//        if (srefpnet == null)
//        {
//            throw new BundleException("Can't start RemoteFramework, peernetwork not running !");
//        }
//        
//        peernetwork = (PeerNetwork) bc.getService(srefpnet);
        
        
        //register service
        bc.registerService(SwtManager.class.getName(), ui, new Hashtable());
        //register service as a singleton... need a PID?

        maingui = new MainGUI();
        ui.exec(maingui, false);
        
    }

    /*
     *
     */
    public void stop(BundleContext context) throws Exception
    {
        bc = null;
        ui.dispose();
        ui = null;
    }

}