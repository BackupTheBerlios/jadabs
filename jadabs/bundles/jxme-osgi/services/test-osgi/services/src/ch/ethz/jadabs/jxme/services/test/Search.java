/************************************************************************
 *
 * $Id: Search.java,v 1.1 2005/01/28 08:31:22 afrei Exp $
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
/**
 * Unit tests for PeerNetwork.search().
 */

package ch.ethz.jadabs.jxme.services.test;

import java.io.IOException;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.jxme.DiscoveryListener;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.PeerNetwork;
import ch.ethz.jadabs.jxme.services.GroupService;

public final class Search implements DiscoveryListener
{

    private static Logger LOG = Logger.getLogger("ch.ethz.jadabs.jxme.services.test.Search");
    
    private PeerNetwork sendPeer = null;

    private PeerNetwork recvPeer = null;

    private String sendPeerName = "sending peer";

    private String recvPeerName = "receiving peer";

    private String relayUrl = null;

    private byte[] persistentState = null;

    private int listenQueryId = 0;

    private int searchQueryId = 0;

    private String testName = null;

    private static int count = 0;

    private GroupService gs = null;

    boolean discovered = false;
    
    /**
     * 
     */
    public Search()
    {
    }

    public void testPositive(String title, GroupService groupsvc, String type, String query)
    {
        count++;
        LOG.info("\n********** Test " + count + " positive: " + title + " **********");

        try
        {
            //res = groupsvc.search(type, "Name", query, 1);
            
            NamedResource[] res = groupsvc.localSearch(type, "Name", query, 1);
            printNamedResource(res);
            
//            while (true)
//            {
                groupsvc.remoteSearch(type, "Name", query, 1, this);
                
                System.out.print(".");
                try
                {
                    Thread.sleep(5000);
                } catch (Throwable t)
                {
                    t.printStackTrace();
                }
                
//            }
//            LOG.info(" DONE");
//            if (res != null)
//            {
//                LOG.debug("search response: " + res.toString());
//
//                LOG.debug("** Test " + count + " PASSED **");
//            } else
//            {
//                LOG.debug("** Test " + count + " FAILED **");
//            }
        } catch (IOException e)
        {
            e.printStackTrace();
            LOG.debug("** Test " + count + " FAILED **");
        } catch (Exception e)
        {
            e.printStackTrace();
            LOG.debug("** Test " + count + " FAILED **");
        }
        
    }


    public void handleSearchResponse(NamedResource resource)
    {
        //LOG.info("Called handleResponse: resource: " + resource.toString());
        //res = resource;
        //discovered = true;
        LOG.info("found: " + resource.getName());
    }

    public void printNamedResource(NamedResource[] resources)
    {

        for( int i = 0; i < resources.length;i++)
        {
            LOG.info("found: " + resources[i].getName());
        }
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.DiscoveryListener#handleNamedResourceLoss(ch.ethz.jadabs.jxme.NamedResource)
     */
    public void handleNamedResourceLoss(NamedResource namedResource)
    {
        LOG.info("namedresouce lost: " + namedResource.getName());
        
    }
    
}