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
 * Created on Jul 15, 2004
 *
 */
package ch.ethz.jadabs.jxme.tcp.cldc;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.EndpointService;


/**
 * TCP transport for JXME. Note that his implementation is
 * for CLDC only. For J2SE use the transport implementation
 * of the "main" Jadabs tree. The Socket management on
 * CLDC is different from J2SE, since there is no Socket
 * and no ServerSocket classes. All network connections
 * (server and client) must be created using the 
 * <code>Connector</code> factory. 
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class TCPActivator implements BundleActivator
{
    /** logger to be used in activator */
    static Logger LOG = Logger.getLogger("ch.ethz.jadabs.jxme.tcp.cldc.TCPActivator");
    
    /** reference to bundle's context in the container */
    static BundleContext bc;
    
    /** default TCP listening port, used if no other specified */
    static int TCP_PORT_DEFAULT = 9001;
    
    /** listening port that is actually being used */
    static int port = 0;
    
    /** Endpoint this transport is attached to */
    static EndpointService endptsvc;
    
    /** Endpoint address of this transport */
    static EndpointAddress endptadr;
    
    /**
     * start TCP transport (creation, intialization and registration) 
     * @param bc context this bundle is embedded into
     */
    public void start(BundleContext bc) throws Exception
    {
        // obtain reference to EndpointService 
        ServiceReference sref = bc.getServiceReference("ch.ethz.jadabs.jxme.EndpointService");
        endptsvc = (EndpointService)bc.getService(sref);
        
        // create EndpointAddress for TCP
        String addr = bc.getProperty("ch.ethz.jadabs.jxme.peeralias");
        if (addr == null) {
            LOG.fatal("error starting TCP-Transport: property 'ch.ethz.jxme.peeralias' unspecified.");
            return;
        }
        
        // figure out port number
        String portstr = bc.getProperty("ch.ethz.jadabs.jxme.tcp.port");    
        if (portstr != null) {
            port = Integer.parseInt(portstr);
        } else {
            port = TCP_PORT_DEFAULT;
        }
        
        // craete endpoint for TCP technology 
        EndpointAddress endptadr = new EndpointAddress("tcp", addr, port );
        
        // bring up transport and initialize it 
        TCPTransport tcptransport = new TCPTransport();
        tcptransport.init(endptadr, endptsvc);
        
        // add transport
        endptsvc.addTransport(endptadr, tcptransport);                
    }

    /** 
     * stop TCP transport (remove endpoint address) 
     * @param bc context this bundle is embedded into
     */
    public void stop(BundleContext bc) throws Exception
    {
        endptsvc.removeTransport(endptadr);
    }
}
