/**
 *  Copyright (c) 2001 Sun Microsystems, Inc.  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in
 *     the documentation and/or other materials provided with the
 *     distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *     if any, must include the following acknowledgment:
 *        "This product includes software developed by the
 *        Sun Microsystems, Inc. for Project JXTA."
 *     Alternately, this acknowledgment may appear in the software itself,
 *     if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Sun", "Sun Microsystems, Inc.", "JXTA" and "Project JXTA" must
 *     not be used to endorse or promote products derived from this
 *     software without prior written permission. For written
 *     permission, please contact Project JXTA at http://www.jxta.org.
 *
 *  5. Products derived from this software may not be called "JXTA",
 *     nor may "JXTA" appear in their name, without prior written
 *     permission of Sun.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL SUN MICROSYSTEMS OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of Project JXTA.  For more
 *  information on Project JXTA, please see
 *  <http://www.jxta.org/>.
 *
 *  This license is based on the BSD license adopted by the Apache Foundation.
 *
 *  $Id: Cache.java,v 1.6 2005/04/03 16:32:13 printcap Exp $
 */

package ch.ethz.jadabs.jxme.services.impl;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.Peer;

/**
 * Cache has hashtables in it for pipes ,peers and groups and also a table for
 * application related resources. It uses the LRU mechanism for replacing
 * entries Has its own mechnism for limiting the size of the cache tables.
 */
class Cache
{

    private static final Logger LOG = Logger.getLogger("ch.ethz.jadabs.jxme.services.Cache");

    private static final int DEFAULT_CACHE_SIZE = 10;

    private static Cache cache = new Cache();

    private int numPipeEntries = 0;

    private int numPeerEntries = 0;

    private int numGroupEntries = 0;

    private int numContentEntries = 0;

    private int cacheSize;

    private Hashtable peerTable = null;

    private Hashtable groupTable = null;

    private Hashtable pipeTable = null;

    private Hashtable otherTable = null;

//    private Random random = new Random();

    private Cache()
    {
        cacheSize = 0;
        try
        {
            String cacheSizeStr = System.getProperty("ch.ethz.jadabs.jxme.services.cacheSize");
            cacheSize = Integer.parseInt(cacheSizeStr);
        } catch (Exception e)
        { /* ignore */
        }
        if (cacheSize < 1)
        {
            cacheSize = DEFAULT_CACHE_SIZE;
        }

        peerTable = new Hashtable(cacheSize);
        pipeTable = new Hashtable(cacheSize);
        groupTable = new Hashtable(cacheSize);
        otherTable = new Hashtable(cacheSize);
    }

    static Cache createInstance()
    {
        return cache;
    }

    /**
     * Adding advertisments to the Cache
     * 
     * @param resource
     *            resource adv to be added
     * @return boolean 
     * 				true if entry already available, false if new added
     */

    synchronized boolean addResource(NamedResource resource)
    {
//        LOG.debug("Adding: type: " + resource.getType() + " name:" + resource.getName() + " id: " + resource.getID()
//                + " gid: " + resource.getID().getGroupID());

        String type = resource.getType();


        boolean available = false;
        
        if (type.equals(NamedResource.PIPE))
        {
            available = pipeTable.containsKey(resource.getID());
            
            numPipeEntries = putInTable(pipeTable, resource, numPipeEntries);
        
        } else if (type.equals(NamedResource.PEER))
        {
            available = peerTable.containsKey(resource.getID());
            
            numPeerEntries = putInTable(peerTable, resource, numPeerEntries);
        } else if (type.equals(NamedResource.GROUP))
        {
            available = groupTable.containsKey(resource.getID());
            
            numGroupEntries = putInTable(groupTable, resource, numGroupEntries);
        } else
        {
            available = otherTable.containsKey(resource.getID());
            
            numContentEntries = putInTable(otherTable, resource, numContentEntries);
        }
        
        return available;
    }

    /**
     * Removing advertisments from the Cache
     * 
     * @param resource
     *            resource adv to be added
     */
    synchronized void removeResource(NamedResource resource)
    {
        LOG.debug("Removing: type: " + resource.getType() + " name:" + resource.getName() + " id: " + resource.getID()
                + " gid: " + resource.getID().getGroupID());

        String type = resource.getType();
        
        if (type.equals(NamedResource.PIPE))
        {
            pipeTable.remove(resource.getID());
        } else if (type.equals(NamedResource.PEER))
        {
            peerTable.remove(resource.getID());
        } else if (type.equals(NamedResource.GROUP))
        {
            groupTable.remove(resource.getID());
        } else
        {
            otherTable.remove(resource.getID());
        }
    }
    
    /**
     * small method to put an advertisments in the cache tables- used internally
     * 
     * @param table
     *            which table- wht type of resource
     * @param resource -
     *            the resource advertisment
     * @param count -
     *            what is the number of entries in that table
     * @return
     */
    private int putInTable(Hashtable table, NamedResource resource, int count)
    {
        if (table.containsKey(resource.getID()))
        {
            /* do nothing, the old value is going to be updated */
        } else if (count < cacheSize)
        {
            count++;
        } else
        {
            removeOlderEntry(table);
        }
        resource.touch();
        table.put(resource.getID(), resource);
        return count;
    }
   
    /**
     * removing the entry with min time stamp - LRU based
     * 
     * @param table -
     *            table in which entry has to be replaced
     */

    private void removeOlderEntry(Hashtable table)
    {
        Enumeration elmse = table.elements();
        // called when the table is full, so there is always at least one
        // element
        NamedResource resMinTime = (NamedResource) elmse.nextElement();

        while (elmse.hasMoreElements())
        {
            NamedResource res = (NamedResource) elmse.nextElement();
            if (resMinTime.getLastUsed() > res.getLastUsed())
            {
                resMinTime = res;
            }
        }
        table.remove(resMinTime.getID());
    }

    /**
     * removing entries- currently not being used but could be if needed
     * 
     * @param type
     *            Named resource type
     * @param key
     *            id to remove on
     */
    /*
     * void removeResource (String type, ID key) { LOG.debug("removing entry of
     * type:" + type + " ID: " + key); if(type.equals(NamedResource.PIPE)){
     * if(pipeTable.containsKey(key)){ pipeTable.remove(key); numPipeEntries--; } }
     * else if(type.equals(NamedResource.PEER)){ if(peerTable.containsKey(key)){
     * peerTable.remove(key); numPeerEntries--; } } else
     * if(type.equals(NamedResource.GROUP)){ if(groupTable.containsKey(key)){
     * groupTable.remove(key); numGroupEntries--; } } else
     * if(type.equals(NamedResource.OTHER)){ if(otherTable.containsKey(key)){
     * otherTable.remove(key); numContentEntries--; } } }
     */

    /**
     * Get one advertisment from local Cache - accepts GroupId but havent
     * implemented as yet
     * 
     * @param type
     *            Peer,Pipe,Group
     * @param attr
     *            searching on name, desc
     * @param value
     *            value of name, desc
     * @param groupId
     *            groupId for identifying which group the resource should be in
     * @return resource if advertisment found
     */
    //    NamedResource getResource(String groupId, String type, String attr,
    // String value)
    //    {
    //        NamedResource res = null;
    //        NamedResource[] cachedRes = getResources(groupId, type, attr, value);
    //        if (cachedRes.length > 0)
    //        {
    //            res = cachedRes[random.nextInt(cachedRes.length)];
    //        }
    //        return res;
    //    }
    /**
     * Get advertisments from local Cache - accepts GroupId but havent
     * implemented as yet
     * 
     * @param type
     *            Peer,Pipe,Group
     * @param attr
     *            searching on name, desc
     * @param value
     *            value of name, desc
     * @param groupId
     *            groupId for identifying which group the resource should be in
     * @return resource if advertisment found
     */
    synchronized NamedResource[] getResources(String reqpeerId, String groupId, String type, String attr, String value)
    {
//        LOG.debug("searching for :  type: " + type + ", group: " + groupId + " " + attr + ": " + value);
        Enumeration en = null;
        if (type.equals(NamedResource.PIPE))
        {
            en = pipeTable.elements();
        } else if (type.equals(NamedResource.PEER))
        {
            en = peerTable.elements();
        }
        if (type.equals(NamedResource.GROUP))
        {
            en = groupTable.elements();
        }
        if (type.equals(NamedResource.OTHER))
        {
            en = otherTable.elements();
        }

        Vector resList = new Vector();
        if (en != null)
        {
            while (en.hasMoreElements())
            {
                NamedResource res = (NamedResource) en.nextElement();
                //String attribute = res.getValueof(attr);

                //                if (value.equals(attribute) &&
                // res.getID().getGroupID().equals(groupId))

                //                LOG.debug("res ID: " + res.getID()+" reqpeerId: " +
                // reqpeerId);

                // attribute.indexOf(value) != -1
                if (res.matchesAttr(attr, value) && 
                        (groupId == null || res.getID().getGroupID().equals(groupId))
                        && !res.getID().equals(reqpeerId))
                {
                    resList.addElement((NamedResource) res);
//                    LOG.debug("found " + (NamedResource) res);
                }
            }
        }
        
//        Object[] oar = resList.toArray();
//        int arsize = resList.size();
//        Object[] oar
        
        NamedResource[] nrar = new NamedResource[resList.size()];
        for (int i = 0; i < resList.size(); i++)
        {
            nrar[i] = (NamedResource) resList.elementAt(i);
        }
        return (NamedResource[]) nrar;
    }

    /**
     * method for searching a resource with its ID and its type
     * 
     * @param type
     * @param id
     * @return the resource or null if it does not exist
     */
    private NamedResource getResourceById(String type, String id)
    {
        if (type.equals(NamedResource.PIPE))
        {
            return (NamedResource) pipeTable.get(id);
        } else if (type.equals(NamedResource.PEER)) 
        { 
            return (NamedResource) peerTable.get(id); 
        }
        if (type.equals(NamedResource.GROUP)) 
        { 
            return (NamedResource) groupTable.get(id); 
        }
        if (type.equals(NamedResource.OTHER)) { return (NamedResource) otherTable.get(id); }

        return null;
    }

    /**
     * gets the URIs of the neighbors you need to fwd the message to
     * 
     * @param noOfNeighbors
     * @param myID
     *            the ID of the current Peer. It is used to remove it from the
     *            list of neighbors
     * @return list of URIs
     */
    synchronized EndpointAddress[][] getNeighbors(int noOfNeighbors, String myID)
    {
        // i am in my cache
        noOfNeighbors = (noOfNeighbors < peerTable.size() ? noOfNeighbors : peerTable.size() - 1);
        //        LOG.debug("PeerTable size: " + peerTable.size() + " #ngbrs: " +
        // noOfNeighbors);
        EndpointAddress[][] neighborURI = new EndpointAddress[noOfNeighbors][];

        Enumeration enelms = peerTable.elements();
        for (int i = 0; i < noOfNeighbors; i++)
        {
            Peer neighbor = (Peer) enelms.nextElement();
            if (myID.equals(neighbor.getID()))
            {
                continue; // neighbors only, not me
            }
            EndpointAddress[] peerURI = neighbor.getURIList();
            neighborURI[i] = new EndpointAddress[peerURI.length];
            for (int ndx = 0; ndx < peerURI.length; ndx++)
            {
                neighborURI[i][ndx] = peerURI[ndx];
            }
        }
        return neighborURI;
    }

    /**
     * Dumps the cache into a string.
     */
    /*
     * public String toString() { return
     * dumpTable("peerTable",numPeerEntries,peerTable) + "\n" +
     * dumpTable("groupTable",numGroupEntries,groupTable) + "\n" +
     * dumpTable("pipeTable",numPipeEntries,pipeTable) + "\n" +
     * dumpTable("otherTable",numContentEntries,otherTable); }
     */

    /**
     * Dumps a table into a string.
     * 
     * @param name
     *            name of the table.
     * @param noEntries
     *            number of expected entries.
     * @param table
     *            table to dump
     * @return a string with name, the number of elements followed by the list
     *         of the element.
     */
    /*
     * private String dumpTable(String name, int noEntries, Hashtable table) {
     * String result = name + ": " + noEntries + " entries"; Enumeration enum =
     * table.keys(); while (enum.hasMoreElements()) { Object key =
     * enum.nextElement() ; result += "\n <" + key + ">: " + table.get(key); }
     * return result; }
     */
}