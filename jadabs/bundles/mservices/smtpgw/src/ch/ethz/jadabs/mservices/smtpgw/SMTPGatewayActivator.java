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
 * Created on Jul 21, 2004
 * 
 * $Id: SMTPGatewayActivator.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.jadabs.mservices.smtpgw;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.EndpointService;

/**
 * This is the activator for the SMTPGateway Service.
 * 
 * @author andfrei
 * @author Ren&eacute; M&uuml;ller
 */
public class SMTPGatewayActivator implements BundleActivator
{
    /** reference to the context of this bundle */
    static BundleContext bc;
    
    /** the SMTP gateway service */
    SMTPGatewayService smtpgw;

    /**
     * Called when the bundle has to be started
     * @param bc reference to the bundle's context
     */
    public void start(BundleContext bc) throws Exception
    {
        SMTPGatewayActivator.bc = bc;               
        
        // register SMPTGateway
        smtpgw = new SMTPGatewayService();
        SMTPGatewayActivator.bc.registerService(SMTPGatewayService.class.getName(), smtpgw, null);

        // register SMTPGateway in EndpointService
        ServiceReference sref = bc.getServiceReference(EndpointService.class.getName());
        EndpointService endptsvc = (EndpointService)bc.getService(sref);
        
        endptsvc.addListener("smtpgateway", smtpgw);
    }

    /**
     * Called when the bundle has to be stopped 
     * @param bc reference to the bundle's context
     */
    public void stop(BundleContext bc) throws Exception
    {
        // Rene: Andreas, what is this supposed to do?
        //Thread.currentThread().setContextClassLoader(oldLoader);
    }
}