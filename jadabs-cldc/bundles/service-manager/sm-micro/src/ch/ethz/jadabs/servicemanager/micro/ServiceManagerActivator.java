/*
 * Created on Feb 12, 2005
 *
 */
package ch.ethz.jadabs.servicemanager.micro;

//import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.Pipe;
import ch.ethz.jadabs.jxme.services.GroupService;
import ch.ethz.jadabs.servicemanager.ServiceAdvertisementListener;
import ch.ethz.jadabs.servicemanager.ServiceListener;
import ch.ethz.jadabs.servicemanager.ServiceManager;
import ch.ethz.jadabs.servicemanager.ServiceReference;


/**
 * @author andfrei
 * 
 */
public class ServiceManagerActivator implements BundleActivator, 
	ServiceManager, 
	Listener
{

//    private static Logger LOG = Logger.getLogger("ServiceManagerActivator");
    
    private static String GM_PIPE_NAME_DEFAULT = "gmpipe";
    private static long GM_PIPE_ID_DEFAULT = 23;
    
    static BundleContext bc;

    private ServiceManagerActivator serviceManager;
    
    static GroupService groupService;
    static Pipe groupPipe;
    

    static String peername;
        
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
    }

    /*
     */
    public void stop(BundleContext context) throws Exception
    {
        
    }

    //---------------------------------------------------
    // implement ServiceManager interface
    //---------------------------------------------------
    
    /*
     */
    public boolean getServiceAdvertisements(String peername, String filter, ServiceAdvertisementListener serviceListener)
    {
        return false;
    }

    /*
     */
    public void removeListener(ServiceAdvertisementListener serviceListener)
    {
        
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
    public boolean getService(String fromPeer, ServiceReference sref, ServiceListener listener)
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
//        LOG.debug("got message:");
    }

    /*
     */
    public void handleSearchResponse(NamedResource namedResource)
    {
        
    }

}
