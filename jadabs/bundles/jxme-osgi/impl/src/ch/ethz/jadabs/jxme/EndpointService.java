/**
 * $Id: EndpointService.java,v 1.2 2004/11/19 08:16:36 afrei Exp $
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

/*
 * Endpoint Service
 * 
 */

package ch.ethz.jadabs.jxme;



import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

/**
 * Endpoint Serice is responsible for end-2-end communication.
 */

public class EndpointService extends Service implements Listener
{

    private static final Logger LOG = Logger.getLogger("ch.ethz.jadabs.jxme.EndpointService");

    private static EndpointService INSTANCE = null;

    private static final String EPSERVICE_NAME = "EndpointService";

    final Hashtable transports = new Hashtable();

    /**
     * Constructor
     * 
     * @param peer
     *            my peer
     */
    private EndpointService(Peer peer)
    {
        super(peer, EPSERVICE_NAME);
    }

    /**
     * Create EndpointService singleton instance
     * 
     * @param peer
     *            my peer
     * @return an instance of EndpointService
     */
    public static EndpointService createInstance(Peer peer)
    {
        if (INSTANCE == null)
        {
            INSTANCE = new EndpointService(peer);
        }
        return INSTANCE;
    }

    
    public void addTransport(EndpointAddress uri, Transport transport)
    {
        transports.put(uri, transport);
        
        // add uri to Peer URI list
        peer.addURI(uri);
    }
    
    
    public void removeTransport(EndpointAddress uri)
    {
        transports.remove(uri);
        
        // remove URI from Peers URI list
        peer.removeURI(uri);
    }
    
    
    
    /**
     * Send Message using the EndpointService
     * 
     * @param message
     * @param pipe
     * @return
     */
    public void send(Message message, EndpointAddress[] URIList) throws IOException
    {
        Element[] elm = message.getElements();
        send(elm, URIList);
    }

    public void send(Message message, EndpointAddress uri) throws IOException
    {
        send(message.getElements(), new EndpointAddress[]{uri});
    }
    
    /**
     * Send the element array to the destination Peer who's URIList is given
     * (unicast)
     * 
     * @param elm
     * @param destURI
     * @throws IOException
     */
    public void send(Element[] elm, EndpointAddress[] URIList) throws IOException
    {
        boolean msgsent = false;
        
//        EndpointAddress[] myURIList = myPeer.getURIList();
        
        // Add source and destination address elements in the message
        Element[] elmNew = new Element[elm.length + 2];
        for (int index = 0; index < elm.length; index++) {
            elmNew[index] = elm[index];
        }
        
//        LOG.debug("" + message.getElementCount() + " elements send to " + URI.toString(URIList) + " from " + URI.toString(myURIList));
//        String str = "";
//        java.util.Enumeration enum = transports.keys();
//        while (enum.hasMoreElements())
//            str = str + enum.nextElement() + " ";
        
        
        
        // Send on any one transport of the destination Peer
        int interfacesIndex = 0;
        while (interfacesIndex < URIList.length)
        {
        	EndpointAddress destURI = URIList[interfacesIndex++];
            
            // We don't need it, just for JXTA compatibility

            // Add destination address
            elmNew[elm.length] = new Element(
                    Message.ENDPOINTDEST_TAG, 
                    destURI.toString().getBytes(),
                    Message.JXTA_NAME_SPACE, null);
            
            // check for an appropriate transport
            Enumeration transURIen = transports.keys();
          	for (;transURIen.hasMoreElements();)
          	{
          	    EndpointAddress urisrc = (EndpointAddress) transURIen.nextElement();
          	  
          	    if (urisrc.getProtocol().equals(destURI.getProtocol()))
          	    {
		            elmNew[elm.length + 1] = new Element(
		                    Message.ENDPOINTSRC_TAG, 
		                    urisrc.toString().getBytes(),
		                    Message.JXTA_NAME_SPACE, null);
                	
                	try
                    {
	                	// send message through this transport
	                	Transport trans = (Transport)transports.get(urisrc);
	              	    trans.send(new Message(elmNew), destURI);
	              	    
	              	    msgsent = true;
                    } catch (IOException e)
                    {
                        // try another interface, if this one failed
                        LOG.error("The interface of destination " + destURI + " failed: " + e);
                        
                        continue;
                    }
          	    }    
          	    
            }          	
        }
        
        if (!msgsent)
  	        throw new IOException("No Unicast Transport Found");
        
    }

    /**
     * Multicast the given Element Array to the entire Peer Network using all
     * the available transports
     * 
     * @param message
     * @param pipe
     * @return
     */
//    public void propagate(Message message, EndpointAddress destURI) throws IOException
//    {
//        Element[] elm = message.getElementList();
//        propagate(elm, destURI);
//    }

    /**
     * Multicast the given element array over the PeerNetwork.
     * The destURI must contain at least the serviceName and its serviceParameters to
     * which the message has to be sent. If also the additional protocol is
     * given the message is only broadcasted on the specified protocol. For tcp
     * this would been broadcasting to all open connections. This propagate does
     * not care about the address and port!
     * 
     * @param message, to be sent
     * @param destURI, must contain at least serviceName and serviceParameters
     * @throws IOException
     *             if the message could not be propagated on any transport. This
     *             case will also occur if no transport supports multicast.
     */
    public void propagate(Element[] elm, EndpointAddress destURI) throws IOException
    {
        boolean propagated = false;

        Element[] elmNew = new Element[elm.length + 2];
        for (int index = 0; index < elm.length; index++) {
            elmNew[index] = elm[index];
        }
        
        // for JXTA compatibility.
        // Add destination address element to the message
        elmNew[elm.length] = new Element(
                Message.ENDPOINTDEST_TAG,
                destURI.toString().getBytes(),
                Message.JXTA_NAME_SPACE, null);
        
        // add source address element to the message
        Enumeration transen = transports.keys();
      	for (;transen.hasMoreElements();)
      	{
      	    EndpointAddress uriaddr = (EndpointAddress)transen.nextElement();
      	    
      	    // set src address if the destURI matches the available transport URIs
      	    if (  (destURI.protocol == null) ||
      	          destURI.protocol.equals(uriaddr.protocol) )
      	    {
    	    	
		        elmNew[elm.length + 1] = new Element(
		                Message.ENDPOINTSRC_TAG, uriaddr.toString().getBytes(),
		                Message.JXTA_NAME_SPACE, null);
		        
		        Message msg = new Message(elmNew);
		        
		        Transport transport = (Transport)transports.get(uriaddr);
		        transport.propagate(msg);
		        propagated = true;
      	    }
      	       
      	}
      	
        // No transport could propagate it
        if (!propagated) throw new IOException("No Multicast Transport Found");
    }

    /**
     * Handles Message passed from transport
     * 
     * @param {@link Message}
     *            from sending peer
     * @param listenerid
     *            is id of message handler
     */
    public void handleMessage(Message message, String listenerid)
    {        
        EndpointAddress destURI = null;
        // Remove source and destination address elements from the message
        int elmCount = message.getElementCount();
        Element[] elmNew = new Element[elmCount - 2];

       int i = 0;
        for (int index = 0; index < elmCount; index++)
        {

            Element elm = message.getElements()[i];
            String name = elm.getName();
            String nameSpace = elm.getNameSpace();

            if (name.equals(Message.ENDPOINTSRC_TAG) && nameSpace.equals(Message.JXTA_NAME_SPACE))
            {
                continue;
            }
            if (name.equals(Message.ENDPOINTDEST_TAG) && nameSpace.equals(Message.JXTA_NAME_SPACE))
            {
                // do nothing for the moment
                try
                {
                    destURI = EndpointAddress.createEndpointURI(new String(elm.getData()));
                } catch (MalformedURIException e)
                {
                    // Can't find destination URI
                    LOG.error("Destination URI not proper in the message : Endpoint dropped the message");
                    return;
                }
                continue;
            }
            elmNew[i++] = elm;
        }

        if (destURI != null)
        {

            //Forward the message to the upper layer
            Listener listener = getListener(destURI.getRecipient());
            if (listener != null)
            {
                Message newMessage = new Message(elmNew);
                listener.handleMessage(newMessage, destURI.getRecipientParam());
            }
        }

    }

    /*
     */
    public void handleSearchResponse(NamedResource namedResource)
    {
    }

}