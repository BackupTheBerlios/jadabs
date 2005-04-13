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
 * Created on Jul 20, 2004
 *
 */
package ch.ethz.jadabs.jxme.udp.test;

import java.io.IOException;

//import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.EndpointService;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;


/**
 * @author andfrei
 *
 */
public class UDPTransportTest //extends TestCase 
	implements BundleActivator, Listener
{

    private static final Logger LOG = Logger.getLogger(UDPTransportTest.class.getName());
    
    EndpointService endptsvc;
    
//    /*
//     */
//    protected void setUp() throws Exception
//    {
//        super.setUp();
//    }
//
//    /*
//     */
//    protected void tearDown() throws Exception
//    {
//        super.tearDown();
//    }

    /*
     */
    public void start(BundleContext bc) throws Exception
    {
        ServiceReference sref = bc.getServiceReference(EndpointService.class.getName());
        endptsvc = (EndpointService)bc.getService(sref);
        
        endptsvc.addListener("testlistener",this);
        
        testSend();
    }

    /*
     */
    public void stop(BundleContext bc) throws Exception
    {
    }
    
    public void testSend()
    {
        Element[] elm = new Element[3];
        elm[0] = new Element("tag1", "hello", Message.JXTA_NAME_SPACE);
        elm[1] = new Element("tag2", "world", Message.JXTA_NAME_SPACE);
        elm[2] = new Element("tag3", "!", Message.JXTA_NAME_SPACE);
        Message msg = new Message(elm);
        
        LOG.debug("call now propagate message: " + msg);
                
        try
        {
            EndpointAddress endptlistener = new EndpointAddress(
                    "udp","129.132.177.109", 9000, "testlistener",null);
            
            // multicast udp
            endptsvc.propagate(elm, endptlistener);
            
            // unicast udp, not working yet!!
            //endptsvc.send(msg, new EndpointAddress[] {endptlistener});
            
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /* 
     */
    public void handleMessage(Message message, String listenerId)
    {
        LOG.debug("udptransporttest got message: "+message.toXMLString());
        LOG.debug("listener params: "+listenerId);
    }

    /* 
     */
    public void handleSearchResponse(NamedResource namedResource)
    {
        LOG.debug("called handleSearchResponse, not implemented");
    }

}
