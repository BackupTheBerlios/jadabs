/************************************************************************
 *
 * $Id: NamedResource.java,v 1.4 2004/12/20 21:25:33 afrei Exp $
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
import java.util.Hashtable;

import org.apache.log4j.Logger;

/**
 * This is an abstract class representing JXTA Network resources. A JXTA
 * resource can be one of the {@link #PEER},{@link #GROUP}or {@link #PIPE}
 *  
 */
public abstract class NamedResource
{

    private static final Logger LOG = Logger.getLogger("ch.ethz.jadabs.jxme.NamedResource");

    protected static final String LEASE_OFFSET_TAG = "loffset";
        
    public static final String COUNTTAG = "Count";

    public static final String PEERADVTAG = "PeerAdv";

    public static final String RESPONSESTAG = "Response";

    public static final String EXPIRATIONTAG = "Expiration";

    public static final String QUERYATTRTAG = "Attr";

    public static final String QUERYVALUETAG = "Value";

    public static final String URITAG = "Addr";

    public static final String TYPETAG = "Type";

    public static final String IDTAG = "id";

    public static final String NAMETAG = "Name";

    /**
     * CODAT RESOURCE
     */
    private static final String CODAT = "01";

    /**
     * Group resource.
     */
    public static final String GROUP = "02";

    /**
     * PEER RESOURCE
     */
    public static final String PEER = "03";

    /**
     * Pipe resource.
     */
    public static final String PIPE = "04";

    /**
     * Undefined resource.
     */
    public static final String OTHER = "99";
    
    /**
     * Wakeuptime for the chronDeamon to send out SAEs
     */
    protected int leaseoffset;

    /*
     * Hash table of advertisable attributes and their values
     */
    protected Hashtable attributes = new Hashtable();

    private long lastUsed;

    /**
     * The default constructor is protected. Applications mustn't call this
     * consructor directly.
     * 
     * @param type.
     *            One of {@link NamedResource.PEER},
     *            {@link NamedResource.GROUP}or {@link NamedResource.PIPE}or
     *            {@link NamedResource.OTHER}
     * 
     * @param name
     *            the name of the entity being created.
     * 
     * @param id
     *            JXTA ID for the resource.
     *  
     */
    protected NamedResource(String resourceType, String resourceName, ID id)
    {

        if (resourceType == null)
            throw new IllegalArgumentException("resourceType must be specified");

        if (id == null)
            throw new IllegalArgumentException("id must be specified when creating a resource");

        attributes.put(TYPETAG, resourceType);
        attributes.put(IDTAG, id.toString());
        attributes.put(NAMETAG, resourceName);

        LOG.debug("NamedResource created: type: " + resourceType + "  name: " + resourceName + "  id: "
                 + id.toString());
        
        
    }

    /**
     * Create an empty NamedResource object, filled using RevAdvertisement()
     * method of this class
     *  
     */
    protected NamedResource()
    {
    }

    /**
     * @return type. One of {@link #PEER},{@link #GROUP}or {@link #PIPE}or
     *         {@link #OTHER}
     *  
     */
    public String getType()
    {
        return (String) attributes.get(TYPETAG);
    }

    /**
     * @return resource name
     */
    public String getName()
    {
        return (String) attributes.get(NAMETAG);
    }

    /**
     * @return resource ID
     */

    public ID getID()
    {
        return new ID((String) attributes.get(IDTAG));
    }

    /**
     * @return list of advertisable attributes. TBD: kuldeep@jxta.org. Should
     *         this be public??
     */
    public Enumeration ListOfAttr()
    {
        Enumeration keyse = attributes.keys();
        return keyse;
    }

    /**
     * get corresponding value of attr
     * 
     * @param attr
     * @return value of attribute
     * 
     * TBD: kuldeep@jxta.org: Is this required? If so, should this be public?
     */
    public String getValueof(String attr)
    {
        if (attributes.containsKey(attr)) 
        { 
            return attributes.get(attr).toString(); 
        }
        
        LOG.warn(attr + " not found");
        
        return null;
    }

    /**
     * make an advertisment and return it to the resolver
     * 
     * @param attr
     * @param value
     * @param id
     * @param threshold
     * @return {@link Element}array
     */
    public abstract Element[] advertisement(String attr, String value, String id, String threshold);

    /**
     * create a named resource from the response received
     * 
     * @param elements
     *            of Resource.
     */
    public abstract void RevAdvertisment(Element[] elm);

    /**
     * get the last used time stamp
     * 
     * @return last used time
     */
    public long getLastUsed()
    {
        return lastUsed;
    }

    /**
     * setting last used time at now. This indicate that it is just been used.
     */
    public void touch()
    {
        lastUsed = System.currentTimeMillis();
    }
    
    /**
     * Return the LeaseOffset since the list has been set.
     * 
     * @return int in milliseconds
     */
    public int getLeaseOffset()
    {
        return leaseoffset;
    }
    
    /**
     * Set the LesaeTime for this peer. 
     * 
     * @param leaseoffset in milliseconds
     */
    public void setLeaseOffset(int leaseoffset)
    {
        this.leaseoffset = leaseoffset;
    }
    
    public boolean matches(String groupId, String type, String attr, String value)
    {
        if (getID().getGroupID().equals(groupId) && 
            getType().equals(type) && 
            matchesAttr(attr, value))
            
            return true;
        
        return false;
    }

    public boolean matchesAttr(String attr, String value)
    {
        // a very poore machting of substrings
        String thisvalue = getValueof(attr);
        if (thisvalue.indexOf(value) != -1 )
            return true;
        
        return false;
    }
    
    public String toString()
    {
        return getType() + " " + getName() + " " + getID().toString();
    }
}