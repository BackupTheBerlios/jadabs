/************************************************************************
 *
 * $Id: Peer.java,v 1.5 2005/02/13 12:36:26 afrei Exp $
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

import java.util.Enumeration;

import org.apache.log4j.Logger;

/**
 * JXTA Peer
 *  
 */
public final class Peer extends NamedResource
{

    private static final Logger LOG = Logger.getLogger("ch.ethz.jadabs.jxme.Peer");
    
    public static final String DESCTAG = "desc";

//    private EndpointAddress[] URIList = new EndpointAddress[0];

    
    /**
     * Create an empty Peer object, filled using RevAdvertisement() method of
     * this class
     *  
     */
    public Peer()
    {
    }

    public Peer(String name, ID id)
    {
        super(NamedResource.PEER, name, id);
    }

    /**
     * Constructor that creates a Peer given the Peer ID that should be used
     * 
     * @param URIList
     * @param name
     * @param id
     */
    public Peer(EndpointAddress[] URIList, String name, ID id)
    {

        this(name, id);
        // The URIList should be converted into a String format from an array
        // format before
        // putting in the Hashtable. Otherwise getValueof() method of Named
        // Resource will throw error
        // The resolver will wrongly put the URIList in the advertisement.

        attributes.put(URITAG, URIList);
//        this.URIList = URIList;
        
    }

    public void addURI(EndpointAddress endptaddr)
    {
        EndpointAddress[] list = (EndpointAddress[])attributes.get(URITAG);
        
        int length = 0;
        if (list != null)
            length = list.length;
        
        EndpointAddress[] newlist = new EndpointAddress[length + 1];
         
        for (int i = 0; i < length; i++)
        {
            newlist[i] = list[i];
        }
        
        newlist[length] = endptaddr;
        
        attributes.put(URITAG, newlist);
    }
    
    public void removeURI(EndpointAddress endptaddr)
    {
        
        EndpointAddress[] list = (EndpointAddress[])attributes.get(URITAG);
        
        int length = 0;
        if (list != null)
            length = list.length;
        
        EndpointAddress[] newlist = new EndpointAddress[length - 1];
        int ndx = 0;
        for (int i = 0; i < length ; i++)
        {
            if (!list[i].equals(endptaddr))
                newlist[ndx++] = list[i];
        }
        
        attributes.put(URITAG, newlist);
    }
        
    /**
     * @return an array of URI
     */
    public EndpointAddress[] getURIList()
    {
        return (EndpointAddress[]) attributes.get(URITAG);
    }

    /**
     * creating an advertisment for the resource
     */

    public synchronized Element[] advertisement(String attr, String value, String id, String threshold)
    {        
        int numAttr = this.attributes.size();
        
        int URIsize = 0;
        int elmsize;
    	if (getURIList() != null)
    	{
    	    URIsize = getURIList().length;
    	    elmsize = numAttr + URIsize + 8 - 1;
    	}
    	else
    	    elmsize = numAttr + 8;
        	
        String numURI = String.valueOf(URIsize);
//        System.out.println("size: "+elmsize);
        Element[] elm = new Element[elmsize]; // -1, dont count attributes Element from URITAG
        
        elm[0] = new Element(Message.MESSAGE_TYPE_TAG, Message.REQUEST_RESOLVE, Message.JXTA_NAME_SPACE);
        elm[1] = new Element(Message.TYPE_TAG, NamedResource.PEER, Message.JXTA_NAME_SPACE);
        elm[2] = new Element(Message.ATTRIBUTE_TAG, attr, Message.JXTA_NAME_SPACE);
        elm[3] = new Element(Message.VALUE_TAG, value, Message.JXTA_NAME_SPACE);
        elm[4] = new Element(Message.THRESHOLD_TAG, threshold, Message.JXTA_NAME_SPACE);
        elm[5] = new Element(Message.REQUESTID_TAG, id, Message.JXTA_NAME_SPACE);
        elm[6] = new Element(Message.NUM_URI_TAG, numURI, Message.JXTA_NAME_SPACE);
        elm[7] = new Element(super.LEASE_OFFSET_TAG, Integer.toString(leaseoffset), Message.JXTA_NAME_SPACE);
        
        Enumeration keys = this.attributes.keys();
        Enumeration data = this.attributes.elements();
        
        int ndx = 8;
        //System.out.println ("#attr: " + numAttr + " #elm: " + elm.length);
        // there seems to be a bug in java with this ndx++ in the loop combined with an if ?!
        for ( ;keys.hasMoreElements(); )
        {
            //ndx++;
            String tag = (String) keys.nextElement();
            Object dataobj = data.nextElement();
            
            
            if (tag.equals(URITAG))
            {
                EndpointAddress[] URIList = (EndpointAddress[])dataobj;
                for (int i = 0; i < URIList.length; i++)
                {
                    
                    elm[ndx] = new Element(URITAG + String.valueOf(i), URIList[i].toString().getBytes(),
                            Message.JXTA_NAME_SPACE, null);
                    
                    ndx = ndx+1;
                    
                }
            }
            else
            {
//                System.out.println("index: "+ndx+", tag: "+tag+","+((String)dataobj).getBytes());
                elm[ndx] = new Element(tag, ((String)dataobj).getBytes(), Message.JXTA_NAME_SPACE, null);
                ndx = ndx + 1;
            }
        }
        
        return elm;
    }

    //    /**
    //     * checking whether peer has the URI or not
    //     *
    //     * @param EndpointAddr
    //     * @return
    //     */
    //    public boolean hasURI(EndpointAddress EndpointAddr)
    //    {
    //        for (int i = 0; i < URIList.length; i++)
    //        {
    //            if (EndpointAddr.equals(URIList[i])) { return true; }
    //        }
    //        return false;
    //    }

    /**
     * making the resource back from the element list
     * 
     * @param elm -
     *            element array
     */
    public void RevAdvertisment(Element[] elm)
    {
        int numElement = elm.length;
        int j = 0;
        //EndpointAddress[] list = new EndpointAddress[1];
        for (int i = 0; i < numElement; i++)
        {
            String elName = new String(elm[i].getName());
            String data = new String(elm[i].getData());
            
            if (elName.equals(Message.NUM_URI_TAG))
            {
                //list = new URI[Integer.parseInt(data)]; // what that ?
            } else if (elName.equals(LEASE_OFFSET_TAG))
            {
                leaseoffset = Integer.parseInt(data);
            } else if (elName.startsWith(URITAG))
            {
                String URIString = data;
                try
                {
                    EndpointAddress uri = EndpointAddress.createEndpointURI(URIString);
                    // the list is not initialized.

//                    list[0] = uri;
                    addURI(uri);
                    //URIList = list;
                } catch (MalformedURIException e)
                {
                    e.printStackTrace();
                }
            } else
            {
                this.attributes.put(elName, new String(elm[i].getData()));
            }
        }
        //System.out.println ("URIList: " + URIList);
    }
}