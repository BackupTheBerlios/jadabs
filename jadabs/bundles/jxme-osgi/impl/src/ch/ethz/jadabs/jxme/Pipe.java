/************************************************************************
 *
 * $Id: Pipe.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
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
 * JXTA pre-resolved Pipe.
 *  
 */
public final class Pipe extends NamedResource
{

    private static final Logger LOG = Logger.getLogger("ch.ethz.jadabs.jxme.Pipe");

    // OwnerID and TYPE are advertisable hence tags defined
    public static final String OWNERIDTAG = "ownerId";

    public static final String TYPE = "typeOfPipe";

    public static final String PROPAGATE = "propagate";

    public static final String UNICAST = "unicast";

    private String ownerId;

    private EndpointAddress[] resolvedURIList = null;

    /**
     * Pipe Constructor
     * 
     * @param name
     * @param id
     * @param ownerId
     */
    public Pipe(String name, ID id, ID ownerId, String pipeType)
    {

        super(NamedResource.PIPE, name, id);
        attributes.put(OWNERIDTAG, ownerId.toString());
        attributes.put(TYPE, pipeType);
    }

    /**
     * Create an empty Pipe object, filled using RevAdvertisement() method of
     * this class
     *  
     */
    public Pipe()
    {
    }

    /**
     * Get owner of the pipe
     * 
     * @return String
     */
    public String getOwnerId()
    {

        if (attributes.get(OWNERIDTAG) != null) { return (String) attributes.get(OWNERIDTAG); }
        return null;
    }

    /**
     * Set owner of the pipe
     * 
     * @param ownerId
     */
    public void setOwnerId(ID ownerId)
    {
        attributes.put(OWNERIDTAG, ownerId.toString());
    }

    /**
     * Gets the URIList of the resolved peer
     * 
     * @return Vector
     */
    public EndpointAddress[] getResolvedURIList()
    {
        return resolvedURIList;
    }

    /**
     * Stores the URIList of the resolved peer
     * 
     * @param resolvedURIList
     */
    public void setResolvedURIList(EndpointAddress[] resolvedURIList)
    {
        this.resolvedURIList = resolvedURIList;
    }

    /*
     * public String toString () { return name + " " + id; }
     */

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    /*
     * public boolean equals (Object obj) { if (obj == this) { return true; } if
     * (!(obj instanceof Pipe)) { return false; } Pipe arg = (Pipe) obj; return
     * ownerId.equals(arg.ownerId) && id.equals(arg.id) && type.equals(arg.type) &&
     * name.equals(arg.name); }
     */

    /**
     * creating an advertisment for pipe in response to a query
     * 
     * @param attr
     * @param value
     * @param queryId-
     *            query Id
     * @param threshold
     * @return
     */
    public Element[] advertisement(String attr, String value, String queryId, String threshold)
    {
        LOG.info("Pipe:advertisement");
        int numAttr = attributes.size();
        Element[] elm = new Element[numAttr + 6];
        elm[0] = new Element(Message.MESSAGE_TYPE_TAG, Message.REQUEST_RESOLVE, Message.JXTA_NAME_SPACE);
        elm[1] = new Element(Message.TYPE_TAG, NamedResource.PIPE, Message.JXTA_NAME_SPACE);
        elm[2] = new Element(Message.ATTRIBUTE_TAG, attr.getBytes(), Message.JXTA_NAME_SPACE, null);
        elm[3] = new Element(Message.VALUE_TAG, value, Message.JXTA_NAME_SPACE);
        elm[4] = new Element(Message.THRESHOLD_TAG, threshold, Message.JXTA_NAME_SPACE);
        elm[5] = new Element(Message.REQUESTID_TAG, queryId, Message.JXTA_NAME_SPACE);
        Enumeration keys = attributes.keys();

        int ndx = 6;
        while (keys.hasMoreElements())
        {
            String tag = (String) keys.nextElement();
            elm[ndx++] = new Element(tag, ((String) attributes.get(tag)).getBytes(), Message.JXTA_NAME_SPACE, null);
        }
        return elm;
    }

    /**
     * making a pipe resource from the pipe advertisment
     * 
     * @param elm -
     *            list of elements from the response mesg
     */
    public void RevAdvertisment(Element[] elm)
    {

        int numElement = elm.length;
        String elName = null;
        for (int i = 4; i < numElement; i++)
        {
            elName = new String(elm[i].getName());
            attributes.put(elName, new String(elm[i].getData()));
        }
    }
}