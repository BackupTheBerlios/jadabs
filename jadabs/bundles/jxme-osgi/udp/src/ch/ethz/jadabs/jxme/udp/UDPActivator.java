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
 * Created on May 4, 2004
 *
 */
package ch.ethz.jadabs.jxme.udp;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.EndpointService;
import ch.ethz.jadabs.jxme.PeerNetwork;
import ch.ethz.jadabs.jxme.Transport;

/**
 * @author andfrei
 * 
 */
public class UDPActivator implements BundleActivator
{
    
    private static final String DEFAULT_MULTICAST_ADDR = "224.0.1.95";
    private static final int DEFAULT_MULTICAST_PORT = 9000;
    
    static BundleContext bc;
//    static String hostname;
    
    Transport udptrans;
    
    EndpointAddress udpendpoint;
    static EndpointService endptsvc;
    static PeerNetwork peernetwork;
    
    public UDPActivator()
    {
        
    }
    
    /*
     */
    public void start(BundleContext context) throws Exception
    {
        UDPActivator.bc = context;
        
        // get Jxme EndpointService
        ServiceReference sref = context.getServiceReference("ch.ethz.jadabs.jxme.EndpointService");
        endptsvc = (EndpointService)context.getService(sref);
        
        // get Jxme PeerNetwork
        sref = context.getServiceReference("ch.ethz.jadabs.jxme.PeerNetwork");
        peernetwork = (PeerNetwork)context.getService(sref);
        
        String mcastaddr = (String)context.getProperty("ch.ethz.jadabs.jxme.multicastAddr");
        if (mcastaddr == null)
            mcastaddr = DEFAULT_MULTICAST_ADDR;
        
        String mcastportstr = (String)context.getProperty("ch.ethz.jadabs.jxme.multicastPort");
        int mcastport = DEFAULT_MULTICAST_PORT;
        if (mcastportstr != null)
            mcastport = Integer.parseInt(mcastportstr);
        
        udpendpoint = new EndpointAddress("udp",mcastaddr, mcastport);
        udptrans = new UDPTransport();
        udptrans.init(udpendpoint, endptsvc);
        
        // add UDPTransport to EndpointService
        endptsvc.addTransport(udpendpoint, udptrans);
    }

    /*
     */
    public void stop(BundleContext context) throws Exception
    {
        udptrans.stop();
        
        endptsvc.removeTransport(udpendpoint);
    }

}
