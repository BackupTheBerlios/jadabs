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
 */
package ch.ethz.jadabs.mservices.smtpgw.testtcp;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.EndpointService;
import ch.ethz.jadabs.jxme.Message;


/**
 * @author andfrei
 *
 */
public class SMTPGatewayServiceTest implements BundleActivator
{

    /*
     */
    public void start(BundleContext bc) throws Exception
    {
        
        ServiceReference sref = bc.getServiceReference(EndpointService.class.getName());
        EndpointService endptsvc = (EndpointService)bc.getService(sref);
        
        String toAddress = bc.getProperty("ch.ethz.jadabs.mservices.smtpgw.test.to");
        String fromAddress = bc.getProperty("ch.ethz.jadabs.mservices.smtpgw.test.from");
        String subject = bc.getProperty("ch.ethz.jadabs.mservices.smtpgw.test.subject");
        String body = bc.getProperty("ch.ethz.jadabs.mservices.smtpgw.test.body");
        String host = bc.getProperty("ch.ethz.jadabs.mservices.smtpgw.test.gatewayhost");
        int port = -1;
        try {
            port = Integer.parseInt(bc.getProperty(
                    "ch.ethz.jadabs.mservices.smtpgw.test.gatewayport"));
        } catch(Exception e) { }
        
        if (toAddress == null) {
            throw new Exception("Cannot proceed: property ch.ethz.jadabs.mservices.smtpgw.test.to not specified!");        
        }
        if (fromAddress == null) {
            throw new Exception("Cannot proceed: property ch.ethz.jadabs.mservices.smtpgw.test.from not specified!");        
        }
        if (subject == null) {
            throw new Exception("Cannot proceed: property ch.ethz.jadabs.mservices.smtpgw.test.subject not specified!");        
        }
        if (body == null) {
            throw new Exception("Cannot proceed: property ch.ethz.jadabs.mservices.smtpgw.test.body not specified!");        
        }
        if (host == null) {
            throw new Exception("Cannot proceed: property ch.ethz.jadabs.mservices.smtpgw.test.gatewayhost not specified!");
        }
        if (port < 1024) {
            throw new Exception("Cannot proceed: property ch.ethz.jadabs.mservices.smtpgw.test.gatewayport invalid!"); 
    	  }
        Message msg = createMailMessage(fromAddress, toAddress, subject, body);
                       
        EndpointAddress endptlistener = new EndpointAddress(
                "tcp",host, port, "smtpgateway",null);
        
        endptsvc.send(msg, new EndpointAddress[] {endptlistener});
        
    }

    /*
     */
    public void stop(BundleContext bc) throws Exception
    {
    }

    public Message createMailMessage(String from, String to, 
            String subject, String body)
    {
     
        Element[] elms = new Element[4];
        elms[0] = new Element("from", from.getBytes(), null, Element.TEXTUTF8_MIME_TYPE);
        elms[1] = new Element("to", to.getBytes(), null, Element.TEXTUTF8_MIME_TYPE);
        elms[2] = new Element("subject", subject.getBytes(), null, Element.TEXTUTF8_MIME_TYPE);
        elms[3] = new Element("body", body.getBytes(), null, Element.TEXTUTF8_MIME_TYPE);
        
        return new Message(elms);
    }
}
