/*
 * Created on Feb 12, 2005
 *
 */
package ch.ethz.jadabs.servicemanager.micro;

import org.apache.log4j.Logger;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.ethz.jadabs.jxme.DiscoveryListener;
import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.Peer;
import ch.ethz.jadabs.jxme.Pipe;
import ch.ethz.jadabs.jxme.services.GroupService;
import ch.ethz.jadabs.servicemanager.ServiceAdvertisementListener;
import ch.ethz.jadabs.servicemanager.ServiceManager;
import ch.ethz.jadabs.servicemanager.ServiceReference;


/**
 * @author andfrei
 * 
 */
public class ServiceManagerActivator extends Thread
	implements BundleActivator, ServiceManager, 
	Listener, DiscoveryListener
{

    private static Logger LOG = Logger.getLogger("ServiceManagerActivator");
    
    private static String GM_PIPE_NAME_DEFAULT = "gmpipe";
    private static long GM_PIPE_ID_DEFAULT = 23;
    
    private int DISCOVERY_INTERVAL_MS = 4000;
    
    static BundleContext bc;

    private ServiceManagerActivator serviceManager;
    
    static GroupService groupService;
    static Pipe groupPipe;
    

    static String peername;
        
    static boolean running = true;
    
    private Vector serviceListeners = new Vector();
    
    private Hashtable services = new Hashtable();
    
    
    //---------------------------------------------------
    // Implement BundleActivator interface
    //---------------------------------------------------
    
    /*
     */
    public void start(BundleContext context) throws Exception
    {
        ServiceManagerActivator.bc = context;
        
        peername = bc.getProperty("ch.ethz.jadabs.jxme.peeralias");
                
        //  set pipe name
        String gmpipeName;
        if ((gmpipeName = bc.getProperty("ch.ethz.jadabs.servicemanager.gmpipe.name")) == null)
            gmpipeName = GM_PIPE_NAME_DEFAULT;
        
        // set pipe id
        long gmpipeID = GM_PIPE_ID_DEFAULT;
        String prop;
        if ((prop = bc.getProperty("ch.ethz.jadabs.servicemanager.gmpipe.id")) != null)
                gmpipeID = Long.parseLong(prop);
        
        // GroupService
        org.osgi.framework.ServiceReference sref = bc.getServiceReference(
                "ch.ethz.jadabs.jxme.services.GroupService");
        groupService = (GroupService)bc.getService(sref);
        
        // Create Pipe
        groupPipe = groupService.createGroupPipe(gmpipeName, gmpipeID);
        
        // create and publish ServiceManager
        serviceManager = this;
//        serviceManager.initRepoCache();
        
        // register servicemanager
        bc.registerService("ch.ethz.jadabs.servicemanager.ServiceManager", serviceManager, null);
        
        // set listener
        groupService.listen(groupPipe, serviceManager);
        
        groupService.addDiscoveryListener(this);
        
        serviceManager.start();
    }

    /*
     */
    public void stop(BundleContext context) throws Exception
    {
        
    }

    
    private void sendRequest(String peername, String svcType, String name, String value)
    {        
//        addListener(filter, serviceListener);
        
        Element[] elm = new Element[4];
        
        elm[0] = new Element(SERVICE_TYPE, svcType, Message.JXTA_NAME_SPACE);
        elm[1] = new Element(name, value, Message.JXTA_NAME_SPACE);
        elm[2] = new Element(SERVICE_TO_PEER, peername, Message.JXTA_NAME_SPACE);
        elm[3] = new Element(SERVICE_FROM_PEER, ServiceManagerActivator.peername, Message.JXTA_NAME_SPACE);
        
        try
        {
//            LOG.info("send servicemanager message");
            
            ServiceManagerActivator.groupService.send(
                    ServiceManagerActivator.groupPipe, 
                    new Message(elm));
            
        } catch (IOException e)
        {
//            LOG.error("error in sending message");
        }
    }
    
    //---------------------------------------------------
    // implement ServiceManager interface
    //---------------------------------------------------
    
    /*
     */
    public boolean getServiceAdvertisements(String peername, String filter)
    {
        sendRequest(peername, SERVICE_REQ, SERVICE_FILTER, filter);
        
        return true;
    }
    

    /*
     */
    public void addProvidingService(ServiceReference sref)
    {
        
    }

    /*
     */
    public void removeProvidingService(ServiceReference sref)
    {
        
    }

    /*
     */
    public boolean getService(String fromPeer, ServiceReference sref)
    {
        return false;
    }

    /*
     */
    public boolean istartService(String toPeer, ServiceReference sref)
    {
        return false;
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.servicemanager.ServiceManager#addServiceAdvertisementListener(ch.ethz.jadabs.servicemanager.ServiceAdvertisementListener)
     */
    public void addServiceAdvertisementListener(ServiceAdvertisementListener svcListener)
    {
        serviceListeners.addElement(svcListener);
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.servicemanager.ServiceManager#removeServiceAdvertisementListener(ch.ethz.jadabs.servicemanager.ServiceAdvertisementListener)
     */
    public void removeServiceAdvertisementListener(ServiceAdvertisementListener svcListener)
    {
        serviceListeners.removeElement(svcListener);  
    }
    
    private void notifyFoundService(ServiceReference sref)
    {
        for (Enumeration en = serviceListeners.elements(); en.hasMoreElements();)
        {
            ServiceAdvertisementListener listener = (ServiceAdvertisementListener)en.nextElement();
            listener.foundService(sref);
        }
    }
    
    private void notifyRemovedService(ServiceReference sref)
    {
        for (Enumeration en = serviceListeners.elements(); en.hasMoreElements();)
        {
            ServiceAdvertisementListener listener = (ServiceAdvertisementListener)en.nextElement();
            listener.removedService(sref);
        }
    }
    
    //---------------------------------------------------
    // Implement Listener interface
    //---------------------------------------------------
    
    /*
     */
    public void handleMessage(Message message, String listenerId)
    {
//        LOG.debug("got message:"+message.toXMLString());
        
        String type = new String(message.getElement(SERVICE_TYPE).getData());
        
        String topeer = new String(message.getElement(SERVICE_TO_PEER).getData());
        String frompeer = new String(message.getElement(SERVICE_FROM_PEER).getData());
        
//        LOG.debug("type: "+ type +" topeer: "+topeer+" frompeer: "+frompeer);
        
        if (topeer.equals(ServiceManagerActivator.peername) || topeer.equals(ANYPEER))
        {
        
            if (type.equals(SERVICE_ACK))
            {                
		         String uuid = new String(message.getElement(UUID).getData());
		         String durl = new String(message.getElement(DOWNLOAD_URL).getData());
		         String port = new String(message.getElement("port").getData());
		         
		         LOG.info("uuid: "+ uuid);
		         
		         if (!services.containsKey(uuid))
		         {
		             
		             ServiceReferenceImpl sref = new ServiceReferenceImpl(uuid, frompeer, "");
				     sref.durl = durl;
				     sref.port = port;
				     
		             services.put(uuid, sref);
				     
				     notifyFoundService(sref);
		         }
		         
            }
        }
    }

    private void delPeerResources(String peer)
    {
        Hashtable todelete = new Hashtable();
        
        for (Enumeration en = services.elements(); en.hasMoreElements();)
        {
            ServiceReference sref = (ServiceReference)en.nextElement();
            
            if (sref.getPeer().equals(peer))
                todelete.put(sref.getID(), sref);
        }
        
        for (Enumeration en = todelete.keys(); en.hasMoreElements();)
        {
            String srefid = (String)en.nextElement();
            
            services.remove(srefid);
            
            notifyRemovedService((ServiceReference)todelete.get(srefid));
        }
    }
    
    /*
     */
    public void handleSearchResponse(NamedResource namedResource)
    {
        LOG.info("found named resource: "+ namedResource.getName());
        
        if (namedResource instanceof Peer)
        {
            LOG.info("resource is peer .");
            
            serviceManager.getServiceAdvertisements(namedResource.getName(), "|OPD");
            
        }
            
    }

    //---------------------------------------------------
    // implement DiscoveryListener interface
    //---------------------------------------------------
    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.DiscoveryListener#handleNamedResourceLoss(ch.ethz.jadabs.jxme.NamedResource)
     */
    public void handleNamedResourceLoss(NamedResource namedResource)
    {
        // TODO remove all services provided by that peer
        LOG.info("lost namedRes: "+ namedResource.getName());
        if (namedResource instanceof Peer)
            delPeerResources(namedResource.getName());
    }

    
    public void run()
    {
        
        while (running) {
            
            // send discovery message 
            getServiceAdvertisements(ServiceManager.ANYPEER, "|OPD");
                        
            // sleep until next discovery cycle
            try {
                Thread.sleep(DISCOVERY_INTERVAL_MS);
            } catch (InterruptedException e) {
            	// do nothing
            }
        }
    }
}
