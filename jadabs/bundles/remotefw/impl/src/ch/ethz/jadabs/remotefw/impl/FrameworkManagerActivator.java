/*
 * Copyright (c) 2003-2005, Jadabs project
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
 * 
*/

package ch.ethz.jadabs.remotefw.impl;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceReference;

import JSX.ObjectReader;
import ch.ethz.jadabs.jxme.DiscoveryListener;
import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.EndpointService;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.MalformedURIException;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.Peer;
import ch.ethz.jadabs.jxme.PeerNetwork;
import ch.ethz.jadabs.jxme.services.GroupService;
import ch.ethz.jadabs.remotefw.BundleInfo;
import ch.ethz.jadabs.remotefw.Framework;
import ch.ethz.jadabs.remotefw.FrameworkManager;
import ch.ethz.jadabs.remotefw.RemoteFrameworkListener;

//import com.thoughtworks.xstream.XStream;

/**
 * Implements the basic OSGi BundleActivator behaviour and activates the Remote
 * Framework of the Jadabs Ad-Hoc Communication Layer (JACL).
 * 
 * @author rjan, andfrei
 */
public class FrameworkManagerActivator 
	implements BundleActivator, FrameworkManager, DiscoveryListener, Listener
{

    protected static Logger LOG = Logger.getLogger(FrameworkManagerActivator.class.getName());
    
    //---------------------------------------------------
    // constants, de-,serialization
    //---------------------------------------------------
    protected static final String ENDPOINT_SVC_NAME = "remotefw";
    
    protected static final String MSG_TYPE = "msg_type";
    
    protected static final int INFO_REQ = 1;

    protected static final int INFO_ACK = 2;
    
    protected static final int INFO_UPD = 3;
    
    static final int INSTALL 	= 6;

    static final int UNINSTALL 	= 7;

    static final int STOP 		= 8;

    static final int START 		= 9;
    
    protected static final String BUNDLES = "bundleinfos";
    
    protected static final String FWNAME = "fwname";
    
    protected static final String ELEM_DATA = "elem_data";

    protected static final String FILENAME = "filename";




    
    //---------------------------------------------------
    // static fields
    //---------------------------------------------------
    protected static BundleContext bc;

    protected static PeerNetwork peernetwork;
    protected static GroupService groupsvc;
    protected static EndpointService endptsvc;

    private static String peerName;
    protected static Peer peer;

    private static Vector listeners = new Vector();
    
    private static Hashtable frameworks = new Hashtable(); //[(NamedResource.getName(),Framework)]

    private static Framework local;
    
    protected static boolean chronThreadRunning = true;

    /**
     * start the bundle, this method is called by the OSGi implementation.
     * 
     * @param bc
     *            the bundle context of the OSGi framework.
     * @throws Exception
     */
    public void start(BundleContext bc) throws Exception
    {
        LOG.debug("starting RemoteFramework ... ");
        
        FrameworkManagerActivator.bc = bc;
        peerName = bc.getProperty("ch.ethz.jadabs.jxme.peeralias");
        
        // get PeerNetwork
        ServiceReference srefpnet = bc.getServiceReference(PeerNetwork.class.getName());
        if (srefpnet == null)
        {
            LOG.debug("Can't start RemoteFramework, peernetwork not running !");
            throw new BundleException("Can't start RemoteFramework, peernetwork not running !");
        }
        
        peernetwork = (PeerNetwork) bc.getService(srefpnet);
        peer = peernetwork.getPeer();

        // get EndpointService
        ServiceReference srefesvc = bc.getServiceReference("ch.ethz.jadabs.jxme.EndpointService");
        if (srefesvc == null)
        {
            LOG.debug("Can't start RemoteFramework, endpointservice not running !");
            throw new BundleException("Can't start RemoteFramework, endpointservice not running !");
        }
        
        endptsvc = (EndpointService) bc.getService(srefesvc);
        endptsvc.addListener(ENDPOINT_SVC_NAME, this);

        // get GroupService
        ServiceReference srefgsvc = bc.getServiceReference("ch.ethz.jadabs.jxme.services.GroupService");
        if (srefgsvc == null)
        {
            LOG.debug("Can't start RemoteFramework, endpointservice not running !");
            throw new BundleException("Can't start RemoteFramework, groupservice not running !");
        }
        
        groupsvc = (GroupService) bc.getService(srefgsvc);
        
        // subscribe in groupsvc for discovery events
        groupsvc.addDiscoveryListener(this);
        
        // add the framework of the local peer
        local = new LocalFramework(peer);
        frameworks.put(peer.getName(), local);
        
        // register peers already in cache
        initResources();
        
        // register for new comming peers in the peergroup
        groupsvc.remoteSearch(NamedResource.PEER, "Name", "", 1, this);

        // register FrameworkManager as a service
        bc.registerService(FrameworkManager.class.getName(), this, null);

    }
    
    /**
     * stops the bundle, this method is called by the OSGi implementation.
     * 
     * @param bc
     *            the bundle context of the OSGi framework.
     * @throws Exception
     */
    public void stop(BundleContext bc) throws Exception
    {
        FrameworkManagerActivator.chronThreadRunning = false;

    }
    
    private void initResources()
    {
        try
        {
            NamedResource[] res = groupsvc.localSearch(NamedResource.PEER, 
                    "Name", "", 1);
            
            for(int i = 0; i < res.length; i++)
            {
                String name = res[i].getName();
                if (!name.equals(peer.getName()))
                {
                	LOG.debug("found in cache: "+res[i].getName());
                	
                	Framework rframework = new RemoteFramework(res[i]);
                    frameworks.put(name, rframework);
                }
            }
            
        } catch (IOException e)
        {
            LOG.debug("exception in querying the local cache.");
        }
    }
    
    public void initLocalBundleListeners()
    {
        
        // register a bundlelistener for sending out bundleevents
        FrameworkManagerActivator.bc.addBundleListener(new BundleListener(){

            public void bundleChanged(BundleEvent bevent)
            {
                BundleInfo binfo = new BundleInfo(bevent.getBundle());
                
                //TODO:send message for bundle change to registerer
                //fwmanager.sendElements(binfo.marshal());
      
            }
            
        });
       
    }
    
    protected static void sendMessage(Peer peer, Element[] elms)
    {
        // send it to the specified remoteframework
        EndpointAddress[] urilist = peer.getURIList();
        if (urilist.length == 0)
            LOG.debug("urilist is null");
        
        EndpointAddress endptaddr;
        try
        {
            endptaddr = new EndpointAddress(
                    urilist[0], FrameworkManagerActivator.ENDPOINT_SVC_NAME, null);

            FrameworkManagerActivator.endptsvc.send(elms, 
                    new EndpointAddress[]{endptaddr});
            
        } catch (MalformedURIException e1)
        {
            LOG.debug("malformed endpointaddress");
        } catch (IOException e)
        {
            LOG.debug("couldn't send message");
        }
    }
    
    //---------------------------------------------------
    // Implement DiscoveryListener Interface
    //---------------------------------------------------
    /**
     * 
     */
    public void handleSearchResponse(NamedResource namedResource)
    {
        
        if (!frameworks.containsKey(namedResource.getName()))
        {
            LOG.info("handleSearchResponse: " + namedResource.getName());
            
            Framework rframework = new RemoteFramework(namedResource);
        
	        frameworks.put(namedResource.getName(), rframework);
	
	        for (Enumeration en = listeners.elements(); en.hasMoreElements();)
	        {
	            ((RemoteFrameworkListener) en.nextElement()).enterFrameworkEvent(rframework);
	        }
        }
    }
    
    /*
     */
    public void handleNamedResourceLoss(NamedResource namedResource)
    {
        LOG.debug("handle named resource loss");
        
        Framework fw = (Framework)frameworks.get(namedResource.getName());
        
        for (Enumeration en = listeners.elements(); en.hasMoreElements();)
        {
            ((RemoteFrameworkListener) en.nextElement()).leaveFrameworkEvent(fw);
        }
    }
    
    //---------------------------------------------------
    // Implement FrameworkManager Interface
    //---------------------------------------------------
    
//    public Enumeration getPeerGroups()
//    {
//        
//    }
    
    /*
     */
    public Enumeration getFrameworks()
    {
        return frameworks.elements();
    }

    public void addListener(RemoteFrameworkListener listener)
    {
        listeners.add(listener);
    }

    /*
     *  
     */
    public void removeListener(RemoteFrameworkListener listener)
    {
        listeners.remove(listener);
    }

    /*
     *  
     */
    public Framework getLocalFramework()
    {
        return local;
    }

    /*
     */
    public Enumeration getFrameworkByProperty(String property, String value)
    {
        Vector result = new Vector();
        for (Enumeration en = frameworks.elements(); en.hasMoreElements();)
        {
            Framework rf = (Framework) en.nextElement();
            if (rf.getProperty(property) == value)
            {
                result.add(rf);
            }
        }
        return result.elements();
    }

    /*
     */
    public Framework getFrameworkByPeername(String name)
    {
        return (Framework) frameworks.get(name);
    }

    //---------------------------------------------------
    // Implement JXME-Listener Interface
    //---------------------------------------------------
    /*
     */
    public void handleMessage(Message message, String listenerId)
    {
        
//        LOG.debug("got framework message: "+message.toXMLString());
        
        
        int type = Integer.parseInt(new String(message.getElement(MSG_TYPE).getData()));
        
        switch (type) {
	    case INFO_REQ:
	        LOG.debug("assemble an binfo answer with all bundles: ");
	        
	        Bundle[] bundles = FrameworkManagerActivator.bc.getBundles();
	        
	        // vector version
//	        Vector binfos = new Vector();
//	        for(int i = 0; i < bundles.length; i++)
//	        {
//	            binfos.add(new BundleInfo(bundles[i]));
//	        }
	        
	        // array version
	        BundleInfo[] binfoarr = new BundleInfo[bundles.length];
	        for(int i = 0; i < bundles.length; i++)
	        {
	            binfoarr[i] = new BundleInfo(bundles[i]);
	        }
	        
			// xstream version
//			XStream xstream = new XStream();
//			String bxml = xstream.toXML(binfoarr);
	        
			// JSX
	        try {
		        StringWriter strwbins = new StringWriter();
//		        ObjOut out = new ObjOut(
//		                new PrintWriter(
//		                        strwbins, true));
//		        
//		        out.writeObject(binfoarr);
		        
		        
		        JSX.ObjectWriter out = new JSX.ObjectWriter(new PrintWriter(
                        strwbins, true));
		        out.writeObject(binfoarr);
		        out.close();
		        
		        
		        String bxml = strwbins.toString();
		        LOG.debug("stream: "+ bxml);
	
		        
				Element[] elms = new Element[3];
				
				elms[0] = new Element(MSG_TYPE,
	                    Integer.toString(INFO_ACK), 
	                    Message.JXTA_NAME_SPACE);
				elms[1] = new Element(FWNAME, peer.getName(),Message.JXTA_NAME_SPACE);
				elms[2] = new Element(BUNDLES,bxml,Message.JXTA_NAME_SPACE);
	        
			
			// TODO select the right destination address, just propagate for the moment
//			try
//			{
			    EndpointAddress endptaddr = new EndpointAddress(
	                    null, FrameworkManagerActivator.ENDPOINT_SVC_NAME, null);
				FrameworkManagerActivator.endptsvc.propagate(elms,
				        endptaddr);
	        } catch (MalformedURIException e1)
	        {
	            LOG.error("malformed endpointaddress");
	        } catch (IOException e)
	        {
	            LOG.error("couldn't send message");
	        }
			
	        break;
	        
	    case INFO_ACK:
	        LOG.debug("got bundleinfo response: ");
	        
	        String fwname = new String(message.getElement(FWNAME).getData());
	        String bxmlresp = new String(message.getElement(BUNDLES).getData());
	        
            // xstream version
//            XStream xstreamresp = new XStream();
//            BundleInfo[] binforesp = (BundleInfo[])xstreamresp.fromXML(bxmlresp);
            
	        // JSX
	        try {
//		        ObjIn in = new ObjIn(new StringReader(bxmlresp));
//		        Object obj = in.readObject();
	            
	            Object obj = new ObjectReader(new StringReader(bxmlresp)).readObject();

		        
		        BundleInfo[] binforesp = (BundleInfo[])obj;

	            // get as vector
	//    	    Vector remotebundles = (Vector)xstreamresp.fromXML(bxmlresp);
	    	                
	            Vector remotebundles = new Vector();
	            for (int k = 0; k < binforesp.length; k++)
	                remotebundles.add(binforesp[k]);
	            
	    	    RemoteFramework rfw = (RemoteFramework)frameworks.get(fwname);
	    	    if (rfw != null)
	    	        rfw.allBundlesChanged(remotebundles);
    	    
	        }
	        catch (IOException ioe)
	        {
	            LOG.error("could not parse message");
	        } catch (ClassNotFoundException e)
            {
	            LOG.error("could not create class from message type1");
            }
	        
    	    break;
    	    
	    case INFO_UPD:
	        LOG.debug("got bundleinfo update: ");
	        
	        String fwnameupd = new String(message.getElement(FWNAME).getData());
	        String dataupd = new String(message.getElement(ELEM_DATA).getData());
	        
	        // xstream version
//            XStream xstreamupd = new XStream();
//            BundleInfo binfo = (BundleInfo)xstreamupd.fromXML(dataupd);
            
	        // JSX
	        try {
	            
//		        ObjIn binobj = new ObjIn(new StringReader(dataupd));
//		        Object obj2 = binobj.readObject();
		        
		        Object obj2 = new ObjectReader(new StringReader(dataupd)).readObject();

		        
		        BundleInfo binfo = (BundleInfo)obj2;
		        
	    	    RemoteFramework rfwupd = (RemoteFramework)frameworks.get(fwnameupd);
	    	    if (rfwupd != null)
	    	        rfwupd.bundleChanged(binfo);
    	    
	        }
	        catch (IOException ioe)
	        {
	            LOG.error("could not parse message");
	        } catch (ClassNotFoundException e)
	        {
	            LOG.error("could not create class from message type");
	        }
    	    
    	    break;
    	    
	    case INSTALL:

	        byte[] data = message.getElement(ELEM_DATA).getData();
	        String filename = new String(message.getElement(FILENAME).getData());
	
	        try
	        {
	            //        	            File file = new File("c:\\tmp\\"+filename);
	            //        	            FileOutputStream fo = new FileOutputStream(file);
	            //        	            fo.write(data);
	            //        	            fo.close();
	
	            //        	            RandomAccessFile raf = new RandomAccessFile(filename,
	            // "rw");
	            //        	            raf.write(data);
	
	            ByteArrayInputStream bin = new ByteArrayInputStream(data);
	            bc.installBundle(filename, bin);
	
	            //        	        } catch (IOException e) {
	        } catch (BundleException e)
	        {
	            LOG.error("couldn't install new remote bundle: " + filename, e);
	        }
	
	        break;
    	    case UNINSTALL:
    	        LOG.debug("got an UNINSTALL command: ");

    	        int bidunin = Integer.parseInt(new String(message.getElement(ELEM_DATA).
    	                getData()));
    	
    	        try
    	        {
    	            bc.getBundle(bidunin).uninstall();
    	        } catch (BundleException e1)
    	        {
    	            LOG.error("couldn't uninstall the bundle: ", e1);
    	        }
    	
    	        break;
    	    case STOP:
    	        LOG.debug("got a STOP command: ");
    	
    	        int bidstop = Integer.parseInt(new String(message.getElement(ELEM_DATA).
    	                getData()));
    	
    	        try
    	        {
    	            bc.getBundle(bidstop).stop();
    	        } catch (BundleException e1)
    	        {
    	            LOG.error("couldn't uninstall the bundle: ", e1);
    	        }
    	        
    	        break;
	    case START:
	        LOG.debug("got an START command: ");
	
	        long bid = Long.parseLong(new String(message.getElement(ELEM_DATA)
	                .getData()));
	
	        try
	        {
	            bc.getBundle(bid).start();
	        } catch (BundleException e1)
	        {
	            LOG.error("couldn't uninstall the bundle: " + bid, e1);
	        }
	        
	        break;

        }
    }


}