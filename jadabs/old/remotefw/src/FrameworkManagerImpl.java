package ch.ethz.jadabs.remotefw.impl;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;

import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.MalformedURIException;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.remotefw.BundleInfo;
import ch.ethz.jadabs.remotefw.Framework;
import ch.ethz.jadabs.remotefw.FrameworkManager;
import ch.ethz.jadabs.remotefw.RemoteFrameworkListener;

/**
 * @author rjan, andfrei
 */
public class FrameworkManagerImpl implements FrameworkManager, Listener
{

    private static Logger LOG = Logger.getLogger(FrameworkManagerImpl.class.getName());

    //---------------------------------------------------
    // constants, de-,serialization
    //---------------------------------------------------
    protected static final String ELEM_DATA = "elem_data";

    protected static final String FILENAME = "filename";

    protected static final String RMANAGER = "rmanager";

    protected static final String INFO_REQ = "info_req";

    protected static final String INFO_ACK = "info_ack";

    protected static final String ENDPOINT_SVC_NAME = "remotefw";
    
    //---------------------------------------------------
    // Instant fields
    //---------------------------------------------------
    private static FrameworkManagerImpl fwmanager;
    private Vector listeners = new Vector();
    
    private Hashtable frameworks = new Hashtable();

    private Framework local;

    private FrameworkManagerImpl()
    {
        
        FrameworkManagerActivator.endptsvc.addListener(ENDPOINT_SVC_NAME,this);
        
//        // subscribe remoteFWEvent-Request filter
//        RemoteFWEventImpl event = new RemoteFWEventImpl();
//        event.setSlavePeerName(RemoteFWActivator.peerName);
//        event.addAttribute(RMANAGER, INFO_REQ);
//        rfwreqfilter = new FilterImpl(event);
//        ActivatorRemoteFWActivator.subscribe(rfwreqfilter, rfwereqlistener);
//
//        // subscribe remoteFWEvent-Ack filter
//        event = new RemoteFWEventImpl();
//        event.setSlavePeerName(RemoteFWActivator.peerName);
//        event.addAttribute(RMANAGER, INFO_ACK);
//        rfwackfilter = new FilterImpl(event);
//        ActivatorRemoteFWActivator.subscribe(rfwackfilter, rfweacklistener);
//
//        // subscribe remoteCommandEvent filter
//        RemoteCommandEvent rcomevent = new RemoteCommandEvent();
//        rcomevent.setSlavePeerName(RemoteFWActivator.peerName);
//        rfcmdfilter = new FilterImpl(rcomevent);
//        ActivatorRemoteFWActivator.subscribe(rfcmdfilter, rfcmdlistener);
        
//        // get the already found peers
//        for (Enumeration en = Activator.jaclRemoteFWActivator.getPeers(); en.hasMoreElements();)
//        {
//            String peername = (String) en.nextElement();
//            frameworks.put(peername, new RemoteFramework(peername, ActivatorRemoteFWActivator));
//        }

        // add the framework of the local peer
        local = new LocalFramework(FrameworkManagerActivator.peerName);
        frameworks.put(FrameworkManagerActivator.peerName, local);
        
        initLocalBundleListeners();
    }

    public static FrameworkManagerImpl Singleton()
    {
        if (fwmanager == null)
            fwmanager = new FrameworkManagerImpl();
        
        return fwmanager;
    }
    
    public void initLocalBundleListeners()
    {
        
        // register a bundlelistener for sending out bundleevents
        FrameworkManagerActivator.bc.addBundleListener(new BundleListener(){

            public void bundleChanged(BundleEvent bevent)
            {
                BundleInfo binfo = new BundleInfo(bevent.getBundle());
                
                fwmanager.sendElements(binfo.marshal());
      
            }
            
        });
       
    }

    /*
     *  
     */
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
     *  
     */
    public Enumeration getFrameworks()
    {
        return frameworks.elements();
    }

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

    public Framework getFrameworkByPeername(String name)
    {
        return (Framework) frameworks.get(name);
    }

    /*
     *  
     */
    public void addPeer(String newPeer)
    {
        Framework rframework = new RemoteFramework(newPeer);
        frameworks.put(newPeer, rframework);

        for (Enumeration en = listeners.elements(); en.hasMoreElements();)
        {
            ((RemoteFrameworkListener) en.nextElement()).enterFrameworkEvent(rframework);
        }
    }

    /*
     *  
     */
    public void JaclLostPeerEvent(String lostPeer)
    {
        if (LOG.isDebugEnabled())
            LOG.debug("Removing " + lostPeer);

        Framework rframework = (Framework) frameworks.get(lostPeer);

        if (rframework != null)
        {
            frameworks.remove(lostPeer);

            // notify all listeners of the leave
            for (Enumeration en = listeners.elements(); en.hasMoreElements();)
            {
                ((RemoteFrameworkListener) en.nextElement()).leaveFrameworkEvent(rframework);
            }
        }
    }

    //
    // EventListeners for request and acks
    //

//    class RemoteFWEventReqListener implements EventListener
//    {
//
//        public void processEvent(Event event)
//        {
//            // traverse dispatching of the event to the appropriate
//            // RemoteFramework instance
//
//            LOG.debug("got req: " + event.toXMLString());
//
//            RemoteFWEvent rfwreq = (RemoteFWEvent) event;
//            String remotepeer = rfwreq.getMasterPeerName();
//
//            RemoteFWEvent rfwack = new RemoteFWEventImpl(RemoteFWActivator.b_context.getBundles());
//            rfwack.addAttribute(RMANAGER, INFO_ACK);
//            rfwack.setSlavePeerName(remotepeer);
//
//            ActivatorRemoteFWActivator.publish(rfwack);
//        }
//    }

//    class RemoteFWEventAckListener implements EventListener
//    {
//
//        public void processEvent(Event event)
//        {
//            LOG.debug("got ack: " + event.toXMLString());
//
//            RemoteFWEvent rfwack = (RemoteFWEvent) event;
//            String remotepeer = rfwack.getMasterPeerName();
//
//            RemoteFramework rfw = (RemoteFramework) getFrameworkByPeername(remotepeer);
//            rfw.rfwevent = rfwack;
//
//            synchronized (rfw.worker)
//            {
//                rfw.worker.notify();
//            }
//
//        }
//    }

    private void handleFWcommand(int command, Message msg)
    {

	    int bid;
	    Element element;
	
//	    switch (command) {
//	    case INSTALL:
//	        //if (LOG.isDebugEnabled())
//	        //LOG.debug("got an INSTALL command: " +
//	        // rcomevent.toXMLString());
//	        element = msg.getElement(ELEM_DATA);
//	        byte[] data = element.getData();
//	        String filename = (String) rcomevent.getAttributeValue(FILENAME);
//	
//	        try
//	        {
//	            //        	            File file = new File("c:\\tmp\\"+filename);
//	            //        	            FileOutputStream fo = new FileOutputStream(file);
//	            //        	            fo.write(data);
//	            //        	            fo.close();
//	
//	            //        	            RandomAccessFile raf = new RandomAccessFile(filename,
//	            // "rw");
//	            //        	            raf.write(data);
//	
//	            ByteArrayInputStream bin = new ByteArrayInputStream(data);
//	            EventsystemActivator.b_context.installBundle(filename, bin);
//	
//	            //        	        } catch (IOException e) {
//	        } catch (BundleException e)
//	        {
//	            LOG.error("couldn't install new remote bundle: " + filename, e);
//	        }
//	
//	        break;
//	    case UNINSTALL:
//	        if (LOG.isDebugEnabled())
//	            LOG.debug("got an UNINSTALL command: " + rcomevent.toXMLString());
//	
//	        element = rcomevent.getDataElement(ELEM_DATA);
//	        bid = element.getIntData();
//	
//	        try
//	        {
//	            EventsystemActivator.b_context.getBundle(bid).uninstall();
//	        } catch (BundleException e1)
//	        {
//	            LOG.error("couldn't uninstall the bundle: " + bid, e1);
//	        }
//	
//	        break;
//	    case STOP:
//	        if (LOG.isDebugEnabled())
//	            LOG.debug("got an STOP command: " + rcomevent.toXMLString());
//	
//	        element = rcomevent.getDataElement(ELEM_DATA);
//	        bid = element.getIntData();
//	
//	        try
//	        {
//	            EventsystemActivator.b_context.getBundle(bid).stop();
//	        } catch (BundleException e1)
//	        {
//	            LOG.error("couldn't uninstall the bundle: " + bid, e1);
//	        }
//	        
//	        break;
//	    case START:
//	        if (LOG.isDebugEnabled())
//	            LOG.debug("got an START command: " + rcomevent.toXMLString());
//	
//	        element = rcomevent.getDataElement(ELEM_DATA);
//	        bid = element.getIntData();
//	
//	        try
//	        {
//	            EventsystemActivator.b_context.getBundle(bid).start();
//	        } catch (BundleException e1)
//	        {
//	            LOG.error("couldn't uninstall the bundle: " + bid, e1);
//	        }
//	        
//	        break;
//	    }
    }

    private void sendElements(Element[] elms)
    {
        // set destination of sending elements
        EndpointAddress endptlistener;
        try
        {
            endptlistener = new EndpointAddress(
                    null, ENDPOINT_SVC_NAME, null);
            
            FrameworkManagerActivator.endptsvc.propagate(elms, endptlistener);
            
        } catch (MalformedURIException e)
        {
            LOG.debug("endptlistener not correct");
        } catch (IOException e)
        {
            LOG.debug("error in sending message");
        }
        
    }
    
    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.Listener#handleMessage(ch.ethz.jadabs.jxme.Message, java.lang.String)
     */
    public void handleMessage(Message message, String listenerId)
    {
        LOG.debug("received message to be handled in FrameworkMngr: " + message) ;       
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.Listener#handleSearchResponse(ch.ethz.jadabs.jxme.NamedResource)
     */
    public void handleSearchResponse(NamedResource namedResource)
    {
        // TODO Auto-generated method stub
        
    }
    
}

