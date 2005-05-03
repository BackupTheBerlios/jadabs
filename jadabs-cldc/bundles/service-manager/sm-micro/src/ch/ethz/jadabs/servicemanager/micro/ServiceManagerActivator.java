/*
 * Created on Feb 12, 2005
 *
 */
package ch.ethz.jadabs.servicemanager.micro;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.ethz.jadabs.jxme.DiscoveryListener;
import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.ID;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.Peer;
import ch.ethz.jadabs.jxme.Pipe;
import ch.ethz.jadabs.jxme.microservices.MicroGroupServiceCoreActivator;
import ch.ethz.jadabs.jxme.microservices.MicroGroupServiceCoreImpl;
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
    
    private ServiceAdvertisementListener listener;
    
    private Hashtable services = new Hashtable();
        
    private String PLATFORM_DESCRIPTOR =
        "Platform/id:nokia6600.wlab.ethz.ch, " +
    	"name:nokia6600, version:0.1.0, provider-name:ETHZ-IKS; " +
    "Property/name:processor, value:arm9; " +
    "Property/name:os, value:linux; " +
    "Property/name:display, value:176x208; " +
    "Property/name:vm, value:cldc/midp; " +
    "Property/name:vm-version, value:1.0.1; " +
    "OSGiContainer/id:j2me-osgi; " +
    "NetIface/type:bt-jsr82, connection:dynamic, " +
        "name:bt-hotspot; " +
    "NetIface/type:gsm, connection:dynamic, " +
        "name:GSM";
    
    private String DEFAULT_PLATFORM_FILTER = 
        " |"+PLATFORM_DESCRIPTOR+" | OPD,PRO";
    
    // micro-group service
    /** a reference to the core component of the MicroGroup Service */
    private MicroGroupServiceCoreImpl microGroupServiceCore;  
    
//    private GroupService groupService;

    protected static final String PIPE_NAME = "localpipe";
    protected static final String PIPE_ID = "urn:jxta:uuid-0000:0001:04";
    
    private Pipe pipe;
    private LocalListener localListener;
    
    private RemoteListener remoteListener;
    
    //---------------------------------------------------
    // Implement BundleActivator interface
    //---------------------------------------------------
    
    /*
     */
    public void start(BundleContext context) throws Exception
    {
        ServiceManagerActivator.bc = context;
        serviceManager = this;
        
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
//        serviceManager.initRepoCache();
        
        // register servicemanager
        bc.registerService("ch.ethz.jadabs.servicemanager.ServiceManager", serviceManager, null);
        
        // set listener
        groupService.listen(groupPipe, serviceManager);
        
        // for simulation: to setup tcp connection
//        remoteListener = new RemoteListener();
//        groupService.remoteSearch(NamedResource.PEER, "Name", "", 1, remoteListener);
        
        groupService.addDiscoveryListener(this);
        
        serviceManager.start();
        
        
        // create local loopback        
        sref = bc.getServiceReference("ch.ethz.jadabs.jxme.microservices.MicroGroupServiceCoreImpl");
        microGroupServiceCore = (MicroGroupServiceCoreImpl)bc.getService(sref);    
        
        pipe = (Pipe)groupService.create(NamedResource.PIPE, PIPE_NAME, new ID(PIPE_ID), Pipe.PROPAGATE);
        
        localListener = new LocalListener();
        microGroupServiceCore.registerLocally(pipe, localListener);
        
//        try {
//            groupService.listen(pipe, localListener);
//        } catch(IOException e) {
//           LOG.error("Error while registering listener to pipe '"+PIPE_NAME+"': "+e.getMessage());
//        }
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
    public boolean getServiceAdvertisements(String peername, String filter,
            ServiceAdvertisementListener listener)
    {
        // Hack, or simplification for J2ME: 
        // we do allow only one listener, replaces the old one
        this.listener = listener;
        
        if (filter == null)
            filter = DEFAULT_PLATFORM_FILTER;
        
        sendRequest(ANYPEER, FILTER_REQ, SERVICE_FILTER, filter);
        
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
        
            if (type.equals(FILTER_ACK))
            {     
		         String uuid = new String(message.getElement(SERVICE_ID).getData());	
		         String adv = new String(message.getElement(ADV_DESCRIPTOR).getData());

		         LOG.info("uuid: "+ uuid);
		         
		         if (!services.containsKey(uuid))
		         {
		   
		             ServiceReferenceImpl sref = new ServiceReferenceImpl(uuid, frompeer, adv);
		  
		             services.put(uuid, sref);
				     
				     listener.foundService(sref);
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
            
            listener.removedService((ServiceReference)todelete.get(srefid));
        }
    }
    
    /*
     */
    public void handleSearchResponse(NamedResource namedResource)
    {
        LOG.info("found named resource: "+ namedResource.getName());
        
        if (namedResource instanceof Peer)
        {            
            serviceManager.getServiceAdvertisements(namedResource.getName(), "|OPD", null);
            
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
            
            sendRequest(ANYPEER, FILTER_REQ, SERVICE_FILTER, DEFAULT_PLATFORM_FILTER);
//               
            // sleep until next discovery cycle
            try {
                Thread.sleep(DISCOVERY_INTERVAL_MS);
            } catch (InterruptedException e) {
            	// do nothing
            }
        }
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.servicemanager.ServiceManager#addProvidingService(java.lang.String)
     */
    public void addProvidingService(String uuid)
    {
        // TODO Auto-generated method stub
        
    }
    
    class LocalListener implements Listener
    {

        /* (non-Javadoc)
         * @see ch.ethz.jadabs.jxme.Listener#handleMessage(ch.ethz.jadabs.jxme.Message, java.lang.String)
         */
        public void handleMessage(Message message, String listenerId)
        {
            LOG.debug("got message: "+message.toXMLString());
            
            Element elm = message.getElement("OPIPE_TAG");
            
            if (elm != null)
            {
                String opipe = new String(elm.getData());
                try
                {
                    groupService.send(groupPipe, message);
                } catch (IOException e)
                {
                    LOG.debug("could not forward local message to remote pipe");
                }
            }
        }

        /* (non-Javadoc)
         * @see ch.ethz.jadabs.jxme.Listener#handleSearchResponse(ch.ethz.jadabs.jxme.NamedResource)
         */
        public void handleSearchResponse(NamedResource namedResource)
        {
            // TODO Auto-generated method stub
            
        }
        
    }
    
    class RemoteListener implements DiscoveryListener, Listener
    {
        
        public void handleSearchResponse(NamedResource namedResource)
        {
            LOG.debug("found namedresource: " + namedResource.getName());

            
        }

	    public void handleMessage(Message message, String listenerId)
	    {
	        LOG.debug("RemoteListener: "+ message.toXMLString());
	    }

        /* (non-Javadoc)
         * @see ch.ethz.jadabs.jxme.DiscoveryListener#handleNamedResourceLoss(ch.ethz.jadabs.jxme.NamedResource)
         */
        public void handleNamedResourceLoss(NamedResource namedResource)
        {
            LOG.info("namedresouce lost: " + namedResource.getName());
        }
        
    }
    
}
