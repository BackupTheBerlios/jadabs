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
 */

package ch.ethz.jadabs.remotefw.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.Peer;
import ch.ethz.jadabs.remotefw.BundleInfo;
import ch.ethz.jadabs.remotefw.BundleInfoListener;
import ch.ethz.jadabs.remotefw.Framework;

/**
 * @author rjan, andfrei
 */
public class RemoteFramework implements Framework
{

    private static Logger LOG = Logger.getLogger(RemoteFramework.class.getName());

    //---------------------------------------------------
    // Instant fields
    //---------------------------------------------------

    private NamedResource namedResource;

    // lazy initialization of bundles
    private Hashtable bundles = new Hashtable();

    protected Vector bundleinfolisteners = new Vector();

    /**
     * Construct a RemoteFramework stub from the given peername.
     * 
     * @param peername
     * @param eventsvc
     */
    public RemoteFramework(NamedResource namedResource)
    {
        this.namedResource = namedResource;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.remotefw.IRemoteFramework#startBundle(long)
     */
    public boolean startBundle(long bid)
    {
        Element[] elms = new Element[2];
        
        elms[0] = new Element(FrameworkManagerActivator.MSG_TYPE, 
                Integer.toString(FrameworkManagerActivator.START), 
                Message.JXTA_NAME_SPACE);
        elms[1] = new Element(FrameworkManagerActivator.ELEM_DATA, 
                Long.toString(bid), Message.JXTA_NAME_SPACE);

        FrameworkManagerActivator.sendMessage((Peer)namedResource,elms);

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.remotefw.IRemoteFramework#stopBundle(long)
     */
    public boolean stopBundle(long bid)
    {
        Element[] elms = new Element[2];
        
        elms[0] = new Element(FrameworkManagerActivator.MSG_TYPE, 
                Integer.toString(FrameworkManagerActivator.STOP), 
                Message.JXTA_NAME_SPACE);
        elms[1] = new Element(FrameworkManagerActivator.ELEM_DATA, 
                Long.toString(bid), Message.JXTA_NAME_SPACE);

        FrameworkManagerActivator.sendMessage((Peer)namedResource, elms);

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.remotefw.IRemoteFramework#uninstallBundle(long)
     */
    public boolean uninstallBundle(long bid)
    {
        Element[] elms = new Element[2];
        
        elms[0] = new Element(FrameworkManagerActivator.MSG_TYPE, 
                Integer.toString(FrameworkManagerActivator.UNINSTALL), 
                Message.JXTA_NAME_SPACE);
        elms[1] = new Element(FrameworkManagerActivator.ELEM_DATA, 
                Long.toString(bid), Message.JXTA_NAME_SPACE);

        FrameworkManagerActivator.sendMessage((Peer)namedResource, elms);

        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.remotefw.IRemoteFramework#installBundle(java.lang.String)
     */
    public long installBundle(String location)
    {
        File file = new File(location);
        String filename = file.getName();
        LOG.debug("jar file to be sent: " + filename);

        try
        {
            // append file data
            //byte[] data = getBytesFromFile(file);
            byte[] data = read2list(file);
            
            LOG.debug("file data size:"+data.length);
            
            Element[] elms = new Element[3];
            
            elms[0] = new Element(FrameworkManagerActivator.MSG_TYPE, 
                    Integer.toString(FrameworkManagerActivator.INSTALL), 
                    Message.JXTA_NAME_SPACE);
            elms[1] = new Element(FrameworkManagerActivator.FILENAME, 
                    filename, Message.JXTA_NAME_SPACE);
            elms[2] = new Element(FrameworkManagerActivator.ELEM_DATA, 
                    data, Message.JXTA_NAME_SPACE, Element.TEXTUTF8_MIME_TYPE);

            FrameworkManagerActivator.sendMessage((Peer)namedResource, elms);
            
        } catch (IOException ioe)
        {
            LOG.error("couldn't append File to Event", ioe);
        }

        return -1;
    }

    /**
     * To do anything on a bundle on the RemoteFramework the list of bundles
     * have to be obtained.
     *  
     */
    public long[] getBundles()
    {
        System.out.println("RemoteFramework, getBundles");
        
        if (bundles.size() == 0)
        {
            // request bundles from this peer
            Element[] elms = new Element[1];
            
            elms[0] = new Element(FrameworkManagerActivator.MSG_TYPE,
                    Integer.toString(FrameworkManagerActivator.INFO_REQ), 
                    Message.JXTA_NAME_SPACE);

            FrameworkManagerActivator.sendMessage((Peer)namedResource, elms);

            return null;
        } else
        {
            long[] bids = new long[bundles.size()];
            int i = 0;
            for (Enumeration en = bundles.elements(); en.hasMoreElements(); )
            {
                BundleInfo binfo = (BundleInfo) en.nextElement();
                bids[i++] = binfo.bid;
            }
            return bids;
        }
        
    }

    public BundleInfo getBundleInfo(long bid)
    {
        return (BundleInfo) bundles.get(new Long(bid));
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.remotefw.IRemoteFramework#getBundleName(long)
     */
    public String getBundleName(long bid)
    {
        if (bundles.containsKey(new Long(bid)))
            return ((BundleInfo) bundles.get(new Long(bid))).name;

        else
            return "not found";
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.remotefw.IRemoteFramework#getBundleLocation(long)
     */
    public String getBundleLocation(long bid)
    {
        return "not implemented";
    }

    /*
     *  
     */
    public int getBundleState(long bid)
    {
        if (bundles.containsKey(new Long(bid)))
            return ((BundleInfo) bundles.get(new Long(bid))).state;

        else
            return -1;
    }

    /*
     */
    public String getProperty(String property)
    {
        return null;
    }

    /*
     */
    public String getPeername()
    {
        return namedResource.getName();
    }

    /**
     * Clean the internal state to query the remote peer again.
     */
    public void refresh()
    {
        bundles.clear();
    }

    /**
     * Reads a file storing intermediate data into a list. Fast method.
     * 
     * @param file
     *            the file to be read
     * @return a file data
     */
    public static byte[] read2list(File file) throws IOException
    {
        InputStream in = null;
        byte[] buf = null; // output buffer
        int bufLen = 20000 * 1024;
        try
        {
            in = new BufferedInputStream(new FileInputStream(file));
            buf = new byte[bufLen];
            byte[] tmp = null;
            int len = 0;
            List data = new ArrayList(24); // keeps peaces of data
            while ((len = in.read(buf, 0, bufLen)) != -1)
            {
                tmp = new byte[len];
                System.arraycopy(buf, 0, tmp, 0, len); // still need to do copy
                data.add(tmp);
            }
            /*
             * This part os optional. This method could return a List data for
             * further processing, etc.
             */
            len = 0;
            if (data.size() == 1)
                return (byte[]) data.get(0);
            for (int i = 0; i < data.size(); i++)
                len += ((byte[]) data.get(i)).length;
            buf = new byte[len]; // final output buffer
            len = 0;
            for (int i = 0; i < data.size(); i++)
            { // fill with data
                tmp = (byte[]) data.get(i);
                System.arraycopy(tmp, 0, buf, len, tmp.length);
                len += tmp.length;
            }
        } finally
        {
            if (in != null)
                try
                {
                    in.close();
                } catch (Exception e)
                {
                }
        }
        return buf;
    }

    /**
     * Add BundleListener, dispatches to the local
     * <code>BundleContext.addBundleListener</code>
     *  
     */
    public void addBundleInfoListener(BundleInfoListener bl)
    {
        bundleinfolisteners.add(bl);
    }

    /**
     * Remove BundleListener, dispatches to the local
     * <code>BundleContext.removeBundleListener</code>
     *  
     */
    public void removeBundleInfoListener(BundleInfoListener bl)
    {
        bundleinfolisteners.remove(bl);
    }
    
    /**
     * Dispatch a remote BundleInfo to the registered BundleInfoListeners.
     * 
     * @param binfo
     */
    protected void bundleChanged(BundleInfo binfo)
    {

        for (Enumeration en = bundleinfolisteners.elements(); en.hasMoreElements();)
        {
            ((BundleInfoListener) en.nextElement()).
            	bundleChanged(namedResource.getName(), binfo);
        }
    }

    /**
     * Initiate all bundles changed
     *
     */
    protected void allBundlesChanged(Vector bundleinfos)
    {
        for(Enumeration en = bundleinfos.elements(); en.hasMoreElements();)
        {
            BundleInfo binfo = (BundleInfo)en.nextElement();
            bundles.put(new Long(binfo.bid), binfo);         
        }
        
        for (Enumeration en = bundleinfolisteners.elements(); en.hasMoreElements();)
        {
            ((BundleInfoListener) en.nextElement()).
            	allBundlesChanged(namedResource.getName(), this);
        }
    }
    
}