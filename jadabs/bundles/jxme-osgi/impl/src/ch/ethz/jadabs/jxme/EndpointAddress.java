/************************************************************************
 *
 * $Id: EndpointAddress.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 *
 * Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
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
 **********************************************************************/

package ch.ethz.jadabs.jxme;


/**
 * This is a Endpoint Address URI. The URI must be of the form
 * <code>tcp://host:port[/recipient/[recipientparam]]</code>.
 * This is a immutable class.
 * 
 * It is encouraged to use the method createEndpointURI to create a new URI from
 * its string description.
 */
public class EndpointAddress extends URI
{

    /** protocol, like tcp, udp, bt */
    protected String protocol = null;
    
    /** ip address, hostname, or protocol specifics of the endpoint */
    protected String host = null;

    /** port of the endpoint */
    protected int port;

    /**
     * receipient of the URI.
     */
    protected final String recipient;

    /**
     * param of the reciepient of the URI
     */
    protected final String recipientParam;

    /**
     * builds a new endpoint.
     * 
     * @param protocol
     *            protocol used to contact the endpoint
     * @param host
     *            host of the endpoint
     * @exception MalformedURIException
     *                the protocol is not tcp.
     */
    public EndpointAddress(String protocol, String host) throws MalformedURIException
    {
        this(protocol, host, -1, null, null);
    }
    
    /**
     * builds a new endpoint.
     * 
     * @param protocol
     *            protocol used to contact the endpoint
     * @param host
     *            host of the endpoint
     * @param port
     * @exception MalformedURIException
     *                the protocol is not tcp.
     */
    public EndpointAddress(String protocol, String host, int port) throws MalformedURIException
    {
        this(protocol, host, port, null, null);
    }

    /**
     * builds a new endpoint.
     * 
     * @param protocol
     *            protocol used to contact the endpoint
     * @param host
     *            host of the endpoint
     * @param port
     * @param recipient
     *            service of the endpoint
     * @exception MalformedURIException
     *                the protocol is not tcp.
     */
    public EndpointAddress(String protocol, String host, int port, String recipient) throws MalformedURIException
    {
        this(protocol, host, port, recipient, null);
    }

    /**
     * builds a new endpoint.
     * 
     * @param protocol
     *            protocol used to contact the endpoint
     * @param host
     *            host of the endpoint
     * @param port
     * @param recipient
     *            service of the endpoint
     * @param recipientParam
     *            the parameter of the service
     * @exception MalformedURIException
     *                the protocol is not tcp.
     */
    public EndpointAddress(String protocol, String host, int port, String recipient, String recipientParam)
            throws MalformedURIException
    {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.recipient = recipient;
        this.recipientParam = recipientParam;
    }

    /**
     * builds a new endpoint. From another endpoint by using its protocol
     * address and by setting another recipient and recipientParam.
     * 
     * @param uri
     *            the new URI will have the same protocol, host and port that
     *            uri.
     * @param recipient
     *            the recipient of the new URI. Can be null.
     * @param recipientParam
     *            the parameter of the service of the new URI. Must be null if
     *            recipientParam is null.
     * @exception MalformedURIException
     *                if there is a not null recipientParam but a null
     *                recipientParam
     */
    public EndpointAddress(EndpointAddress uri, String recipient, String recipientParam) throws MalformedURIException
    {
        if (recipient == null && recipientParam != null)
                throw new MalformedURIException("URI without service but with a parameter");
        
        if (uri != null)
        {
            this.protocol = uri.protocol;
            this.host = uri.host;
            this.port = uri.port;
        }
        this.recipient = recipient;
        this.recipientParam = recipientParam;
    }

    /**
     * builds a new endpoint from a string in the Endpoint Address URI ABNF
     * format.
     * 
     * @param uri
     * @exception MalformedURIException
     */
    public EndpointAddress(String uri) throws MalformedURIException
    {
        // protocol
        int idx = uri.indexOf("://");
        protocol = uri.substring(0, idx);
        uri = uri.substring(idx + 3);

        // host+port parsing
        idx = uri.indexOf('/');
        String hostport;
        if (idx > -1)
            hostport = uri.substring(0,idx);
        else
            hostport = uri;
            
        uri = uri.substring(idx + 1);
        
        // host
        boolean portgiven = false;
        idx = hostport.indexOf(':');
        if (idx < 1 )// throw new MalformedURIException("must be protocol://host:port");
            host = hostport;
        else
        {
            host = hostport.substring(0, idx);
            portgiven = true;            
        }

        hostport = hostport.substring(idx + 1);
        

        // port
        if (portgiven)
        {
            
            port = Integer.parseInt(hostport);
            
//	        idx = uri.indexOf('/');
//	        String str = uri;
//	        if (idx != -1)
//	        {
//	            str = uri.substring(0, idx);
//	            uri = uri.substring(idx + 1);
//	        }
//	        int p; // used to avoid the 'variable port might already have been
//	               // assigned' warning
//	        try
//	        {
//	            p = Integer.parseInt(str);
//	        } catch (NumberFormatException e)
//	        {
//	            p = -1;
//	        }
//	        // if (p < 0) throw new MalformedURIException("the port must be an positive integer");
//	        port = p;
        }

        // recipient
        idx = uri.indexOf('/');
        if (idx == -1 || uri.length() == 0)
        {
            recipient = null;
            recipientParam = null;
        } else
        {
            idx = uri.indexOf('/');
            if (idx == -1)
            {
                recipient = unencode(uri);
                recipientParam = null;
            } else
            {
                recipient = uri.substring(0, idx);
                uri = unencode(uri.substring(idx + 1));
                // recipientParam
                if (uri.length() != 0)
                    recipientParam = unencode(uri);
                else
                    recipientParam = null;
            }
        }
    }

    /**
     * builds a new endpoint from a string in the Endpoint Address URI ABNF
     * format.
     * 
     * @param uri
     * @exception MalformedURIException
     */
    public static EndpointAddress createEndpointURI(String uri) throws MalformedURIException
    {
        return new EndpointAddress(uri);
    }

    /**
     * returns the protocol 
     */
    public String getProtocol()
    {
        return protocol;
    }

    /**
     * returns the host
     */
    public String getHost()
    {
        return host;
    }

    /**
     * returns the port
     */
    public int getPort()
    {
        return port;
    }

    /**
     * returns the recipient or null if it is not defined.
     */
    public String getRecipient()
    {
        return recipient;
    }

    /**
     * returns the recipient parameter or null if it is not defined.
     */
    public String getRecipientParam()
    {
        return recipientParam;
    }

    /**
     * returns a string description of the uri.
     */
    public String toString()
    {
        StringBuffer str = new StringBuffer(protocol + "://");
        str.append(host);
        if (port > 0)
            str.append(':').append(port);
        
        if (recipient != null)
        {
            str.append('/').append(encode(recipient)).append('/');
            if (recipientParam != null) str.append(encode(recipientParam));
        }
        return str.toString();
    }

    /**
     * return true if the two object are an EndpointAddress with the same value.
     */
    public boolean equals(Object anObject)
    {
        if (!(anObject instanceof EndpointAddress)) return false;
        EndpointAddress uri = (EndpointAddress) anObject;
        
        if (!protocol.equals(uri.protocol)) return false;
        if (!host.equals(uri.host)) return false;
        if (port != uri.port) return false;
        if (recipient == null) return uri.recipient == null;
        if (!recipient.equals(uri.recipient)) return false;
        if (recipient == null) return uri.recipient == null;
        if (recipientParam == null) return uri.recipientParam == null;
        return recipientParam.equals(uri.recipientParam);
    }
}