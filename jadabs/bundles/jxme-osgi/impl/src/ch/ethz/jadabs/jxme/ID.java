/************************************************************************
 *
 * $Id: ID.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
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
 * This license is based on the BSD license adopted by the Apache Foundation.
 *********************************************************************************/

package ch.ethz.jadabs.jxme;

import java.util.Random;

/**
 * NOTE: This is a place holder. Need to borrow implementation from JXTA-J2SE.
 * 
 * A <code>ID</code> is a 128-bit universally unique identifier. The most
 * significant long can be decomposed into the following unsigned fields:
 * 
 * <pre>
 * 
 *  
 *   
 *    0xFFFFFFFF00000000 time_low
 *    0x00000000FFFF0000 time_mid
 *    0x000000000000F000 version
 *    0x0000000000000FFF time_hi
 *    
 *   
 *  
 * </pre>
 * 
 * The least significant long can be decomposed into the following unsigned
 * fields:
 * 
 * <pre>
 * 
 *  
 *   
 *    0xC000000000000000 variant
 *    0x3FFF000000000000 clock_seq
 *    0x0000FFFFFFFFFFFF node
 *    
 *   
 *  
 * </pre>
 * 
 * The variant field must be 0x2. The version field must be either 0x1 or 0x4.
 * If the version field is 0x4, then the most significant bit of the node field
 * must be set to 1, and the remaining fields are set to values produced by a
 * cryptographically strong pseudo-random number generator. If the version field
 * is 0x1, then the node field is set to an IEEE 802 address, the clock_seq
 * field is set to a 14-bit random number, and the time_low, time_mid, and
 * time_hi fields are set to the least, middle and most significant bits
 * (respectively) of a 60-bit timestamp measured in 100-nanosecond units since
 * midnight, October 15, 1582 UTC.
 *  
 */

// TBD kuldeep@jxta.org : Need to change the format and entire implementation to
// be UUID
// compliant. Currently, it is all hacks, Unuqueness is not promised.
// TBD masdhup: The API is fixed, but the generation of unique UUID is still to
// be done.
// For the current implementation an ID object is made up of four parts
// PREFIX, groupID, resourceID and resourceType. The groupID and resourceIDs
// must themselves be UUIDs.Problem....groupID
// in case of default groups like NetPeer Group is a String and not long!
public final class ID
{

    // The default Id for Bootstrapping into NetPeerGroup
    // This has to be made JXTA spec compatible
    public static final ID DEFAULT_NETPEERGROUP_ID = new ID("urn:jxta:uuid-0000:0000:02");
//    public static final ID JXTANULL_ID = new ID("Null");
//    public static final ID WORLDGROUP_ID = new ID("WolrdGroup");
//    public static final ID DEFAULT_NETPEERGROUP_ID = new ID("NetGroup");

    private static final String PREFIX = "urn:jxta:uuid-";

    private long groupID;

    private long resourceID;

    private String resourceType;

    /**
     * ID instance creation from ID provided in a String format.
     * 
     * @param stringID,
     *            ID in the String format as got by calling toString() method
     */
    public ID(String stringID)
    {

        if (stringID == null) { throw new IllegalArgumentException("Null ID"); }
        int hyphenIndex = stringID.indexOf('-');
        if (hyphenIndex == -1) { throw new IllegalArgumentException("Malformed ID"); }
        int colonIndex = stringID.indexOf(':', hyphenIndex + 1);
        if (colonIndex == -1) { throw new IllegalArgumentException("Malformed ID"); }
        try
        {
            groupID = Long.parseLong(stringID.substring(hyphenIndex + 1, colonIndex));
        } catch (Throwable t)
        {
            throw new IllegalArgumentException("Malformed ID: " + t.getMessage());
        }
        int nextColonIndex = stringID.indexOf(':', colonIndex + 1);
        if (nextColonIndex == -1) { throw new IllegalArgumentException("Malformed ID"); }
        try
        {
            resourceID = Long.parseLong(stringID.substring(colonIndex + 1, nextColonIndex));
        } catch (Throwable t)
        {
            throw new IllegalArgumentException("Malformed ID: " + t.getMessage());
        }
        resourceType = stringID.substring(nextColonIndex + 1, stringID.length());
    }

    /**
     * Creates a new ID instance for the given resourceType in the given group
     * 
     * @param resourceType
     *            can be any of NamedResource.PEER, NamedResource.PIPE
     *            NamedResource.GROUP, NamedResource.OTHER
     * @param gid
     *            is the ID of the group inside which this resource is being
     *            created.
     */
    public ID(String resourceType, ID gid)
    {

        if (gid != null)
            groupID = Long.parseLong(gid.getGroupID());
        resourceID = generateUUID();
        this.resourceType = resourceType;
    }

    public ID(ID gid, long resourceID, String resourceType)
    {

        if (gid != null)
            groupID = Long.parseLong(gid.getGroupID());
        
        this.resourceID = resourceID;
        this.resourceType = resourceType;
    }
    
    /**
     * Get the groupID part of this ID
     * 
     * @return
     */
    public String getGroupID()
    {

        return Long.toString(groupID);
    }

    /**
     * Set the groupID part of this ID. When a resource is advertised in a
     * group. The groupID part of the resource is set to the group id in which
     * the resource is advertised.
     */
    public void setGroupID(String groupID)
    {

        this.groupID = Long.parseLong(groupID);
    }

    /**
     * Get the resourceID part of this ID
     * 
     * @return
     */
    public String getResourceID()
    {

        return Long.toString(resourceID);
    }

    /**
     * Generates a UUID which is a unique long. Currently generates a random
     * number
     * 
     * @return UUID in long format
     */
    private long generateUUID()
    {

        Random randNum = new Random(System.currentTimeMillis());
        return Math.abs(randNum.nextLong());
    }

    /**
     * Returns the ID in String format
     */
    public String toString()
    {

        return PREFIX + Long.toString(groupID) + ":" + Long.toString(resourceID) + ":" + resourceType;
    }

    /**
     * Compares this ID to the specified object. The result is true if and only
     * if the argument is not null and is a ID object that represents the same
     * Id.
     * 
     * @param anObject -
     *            the object to compare this ID against.
     * @return true if the ID are equal; false otherwise.
     */
    public boolean equals(Object anObject)
    {
        if (anObject instanceof ID)
        {
            ID anId = (ID) anObject;
            return groupID == anId.groupID && resourceID == anId.resourceID && resourceType.equals(anId.resourceType);
        }
        return false;
    }

    /**
     * Returns a hash code for this ID.
     */
    public int hashCode()
    {
        //FIXME: does not know if this algorithm is a good one
        return (int) (groupID ^ (resourceID << 16)) ^ resourceType.hashCode();
    }

}