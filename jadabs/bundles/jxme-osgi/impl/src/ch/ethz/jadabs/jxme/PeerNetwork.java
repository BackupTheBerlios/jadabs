/************************************************************************
 * $Id: PeerNetwork.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
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
 **********************************************************************/

package ch.ethz.jadabs.jxme;

import java.io.IOException;
import org.apache.log4j.Logger;


/**
 * This class is the first class instatiated by the application. Any application
 * that wants to become a JXME peer creates a singleton object of this class.
 * Doing so creates the Peer, the default PeerGroup and provides a default
 * GroupService to the application. Also the new Peer and the PeerGroup are
 * advertised on the network.
 */
public class PeerNetwork
{

    private static final Logger LOG = Logger.getLogger("ch.ethz.jadabs.jxme.PeerNetwork");

    /**
     * The group, when not specified, defaults to the <code>NetPeerGroup</code>.
     */
//    public static final String DEFAULT_GROUP = "urn:jxta:jxta-NetGroup";

    private static PeerNetwork INSTANCE = null;

    private String name;

    private ID gid = ID.DEFAULT_NETPEERGROUP_ID;

    private EndpointService endptSvc;
    
    private Peer peer;
    
    private PeerGroup group;
    
    /**
     * Constructor that initializes all singleton services and hence the JXME
     * Peer. It constructs the default PeerNetworkGroup and the corresponding
     * GroupService.
     *  
     */
    private PeerNetwork(String name, ID gid) throws IOException
    {
        /* Initialize the configurator and read properties file */

        if (gid != null)
        {
            this.gid = gid;
        }
        this.name = name;
    }

    public void init()
    {
        String defaultgroup = JxmeActivator.bc.getProperty("ch.ethz.jadabs.jxme.defaultgroup");
        if (defaultgroup == null)
            defaultgroup = PeerGroup.DEFAULT_GROUP;
        
        // Create the default PeerNetworkGroup
        group = new PeerGroup(defaultgroup, gid, "This a global group");

        // Create the Peer in the group gid
        ID peerID = new ID(NamedResource.PEER, gid);
        peer = new Peer(name, peerID);

        // Initialize the Endpoint which initializes the transports also.
        endptSvc = EndpointService.createInstance(peer);

        // register EndpointService as a regular jxta service
        JxmeActivator.bc.registerService(
                "ch.ethz.jadabs.jxme.EndpointService",
                endptSvc,
                null);
    }

    public Peer getPeer()
    {
        return peer;
    }
    
    public PeerGroup getPeerGroup()
    {
        return group;
    }
    
    /**
     * Returns GroupService associated with PeerNetwrok - World Peer Group.
     * GroupService can further be used for Group related operations.
     * 
     * @return GroupService for PeerNetwork.
     */
    public EndpointService getEndpointService()
    {
        return endptSvc;
    }
    
    /**
     * Create PeerNetwork singleton instance if not already present. This will
     * create the Peer in the default PeerNetworkGroup
     * 
     * @param name
     *            peer's name
     * @return the singleton instance
     */
    public static PeerNetwork createInstance(String peerName) throws IOException
    {
        return createInstance(peerName, ID.DEFAULT_NETPEERGROUP_ID);
    }

    /**
     * Create PeerNetwork singleton instance if not already present.
     * 
     * @param name
     *            peer's name
     * @param gid,
     *            the ID od the group in which this Peer needs to be created
     * @return the singleton instance
     */
    public static PeerNetwork createInstance(String peerName, ID gid) throws IOException
    {

        if (INSTANCE == null)
        {
            INSTANCE = new PeerNetwork(peerName, gid);
        }
        return INSTANCE;
    }

}