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
 * Created on 31.05.2004
 */
package ch.ethz.jadabs.remotefw.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;

import JSX.ObjectWriter;

import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.Peer;
import ch.ethz.jadabs.remotefw.BundleInfo;
import ch.ethz.jadabs.remotefw.BundleInfoListener;
import ch.ethz.jadabs.remotefw.Framework;

//import com.thoughtworks.xstream.XStream;

/**
 * @author rjan, andfrei
 */
public class LocalFramework implements Framework, BundleListener
{

    private static Logger LOG = Logger.getLogger(LocalFramework.class.getName());

    private NamedResource namedResource;

    private Vector bundleinfolisteners = new Vector();
    
    public LocalFramework(NamedResource namedResource)
    {
        this.namedResource = namedResource;

        LOG.debug("created LocalFramework stub for: " + namedResource.getName());
        
        FrameworkManagerActivator.bc.addBundleListener(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.remotefw.RemoteFramework#startBundle(long)
     */
    public boolean startBundle(long bid)
    {
        try
        {
            FrameworkManagerActivator.bc.getBundle(bid).start();
            return true;
        } catch (BundleException e)
        {
            LOG.error("couldn't start bundle: " + bid);
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.remotefw.RemoteFramework#stopBundle(long)
     */
    public boolean stopBundle(long bid)
    {
        try
        {
            FrameworkManagerActivator.bc.getBundle(bid).stop();
            
            return true;
        } catch (BundleException e)
        {
            LOG.error("couldn't start bundle: " + bid);
            return false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.remotefw.RemoteFramework#uninstallBundle(long)
     */
    public boolean uninstallBundle(long bid)
    {
        try
        {
            FrameworkManagerActivator.bc.getBundle(bid).uninstall();
            
            return true;
        } catch (BundleException e)
        {
            LOG.error("couldn't start bundle: " + bid);
            return false;
        }
    }

    /**
     *	Install Bundle locally
     */
    public long installBundle(String location)
    {
        try
        {
            LOG.debug("install local bundle: "+location);
            File file = new File(location);
            FileInputStream fin = new FileInputStream(file);
            Bundle bundle = FrameworkManagerActivator.bc.installBundle(file.getName(), fin);
            return bundle.getBundleId();
        } catch (BundleException e)
        {
            LOG.error("couldn't install bundle: " + location, e);
        } catch (FileNotFoundException e)
        {
            LOG.error("file not found: " + location, e);
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.remotefw.RemoteFramework#getBundles()
     */
    public long[] getBundles()
    {
        
        Bundle[] bundles = FrameworkManagerActivator.bc.getBundles();
        long[]  bids = new long[bundles.length];

        for(int i = 0; i < bundles.length ; i++)
        {
            bids[i] = ((Bundle)bundles[i]).getBundleId();
            
        }

        return bids;
    }

    /**
     * Returns Wrapped Bundle.
     */
    public BundleInfo getBundleInfo(long bid)
    {
        return new BundleInfo(FrameworkManagerActivator.bc.getBundle(bid));
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.remotefw.RemoteFramework#getBundleName(long)
     */
    public String getBundleName(long bid)
    {
        
        Bundle bundle = FrameworkManagerActivator.bc.getBundle(bid);
        
        String bname = (String)bundle.getHeaders().get(Constants.BUNDLE_NAME);
        
        return bname;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.remotefw.RemoteFramework#getBundleLocation(long)
     */
    public String getBundleLocation(long bid)
    {
        return "not implemented";
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.remotefw.RemoteFramework#getBundleState(long)
     */
    public int getBundleState(long bid)
    {
        Bundle bundle = FrameworkManagerActivator.bc.getBundle(bid);
        
        return bundle.getState();
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.remotefw.RemoteFramework#getProperty(java.lang.String)
     */
    public String getProperty(String property)
    {
        return FrameworkManagerActivator.bc.getProperty(property);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.remotefw.RemoteFramework#getPeername()
     */
    public String getPeername()
    {
        return namedResource.getName();
    }

    /**
     * Add BundleListener, dispatches to the local 
     * <code>BundleContext.addBundleListener</code>
     * 
     */
    public void addBundleInfoListener(BundleInfoListener bil)
    {
        bundleinfolisteners.add(bil);
    }
    
    /**
     * Remove BundleListener, dispatches to the local 
     * <code>BundleContext.removeBundleListener</code>
     * 
     */
    public void removeBundleInfoListener(BundleInfoListener bil)
    {
        bundleinfolisteners.remove(bil);
    }

    public void bundleChanged( BundleEvent event)
    {        
        BundleInfo binfo = new BundleInfo(event.getBundle());
        
        for(Enumeration en = bundleinfolisteners.elements();
        	en.hasMoreElements(); )
        {
            // inform local bundlelistener
            ((BundleInfoListener)en.nextElement()).bundleChanged(
                    FrameworkManagerActivator.peer.getName(), binfo);
            
            // send bundle change to remote peers
            // xstream version
//            XStream xstream = new XStream();
//			String bxml = xstream.toXML(binfo);
			
			
	        try {
	            // JSX
		        StringWriter strwbins = new StringWriter();
//		        ObjOut out = new ObjOut(
//		                new PrintWriter(
//		                        strwbins, true));
//		        String bxml = strwbins.toString();
			
		        ObjectWriter out = new ObjectWriter(new PrintWriter(
                        strwbins, true));
		        out.writeObject(binfo);
		        out.close();
		        String bxml = strwbins.toString();
		        
	            Element[] elms = new Element[3];
	            
	            elms[0] = new Element(FrameworkManagerActivator.MSG_TYPE,
	                    Integer.toString(FrameworkManagerActivator.INFO_UPD), 
	                    Message.JXTA_NAME_SPACE);
	            elms[1] = new Element(FrameworkManagerActivator.FWNAME,
	                    namedResource.getName(), Message.JXTA_NAME_SPACE);
	            elms[2] = new Element(FrameworkManagerActivator.ELEM_DATA,
	                    bxml, Message.JXTA_NAME_SPACE);
	            
	            FrameworkManagerActivator.sendMessage((Peer)namedResource, elms);
	            
	        } catch (IOException e)
	        {
	            LOG.error("couldn't send message");
	        }
        }
    }
    
    public void refresh()
    {
        
    }
    
    protected void finalize()
    {
        FrameworkManagerActivator.bc.removeBundleListener(this);
    }
}