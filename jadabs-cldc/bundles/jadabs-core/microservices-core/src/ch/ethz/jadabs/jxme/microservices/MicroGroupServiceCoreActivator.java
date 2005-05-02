package ch.ethz.jadabs.jxme.microservices;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.PeerNetwork;
import ch.ethz.jadabs.jxme.services.GroupService;


/**
 * Bundle Activator for MicroService (service bundle sides)
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class MicroGroupServiceCoreActivator implements BundleActivator
{
    /** reference to the context of this bundle */
    static BundleContext context;
    
    /** reference to the service object */ 
    private MicroGroupServiceCoreImpl  service;
    
    /** reference to the "real" group service */
    private GroupService groupService;
    
    /**
     * Start the MicroService bundle 
     * @param context reference to the context of this bundle 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception
    {
        MicroGroupServiceCoreActivator.context = context; 
        
        ServiceReference sref = context.getServiceReference("ch.ethz.jadabs.jxme.services.GroupService");
        groupService = (GroupService)context.getService(sref);        
        service = new MicroGroupServiceCoreImpl(groupService); 
    }

    /**
     * Stop the MicroService bundle 
     * @param context reference to the context of this bundle 
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception
    {        
        service.stop();
        // get GC a change to collect the object 
        service = null;
    }
    
    /**
     * Return the service object of the MicroGroupService 
     * @return service object of this bundle 
     */
    public MicroGroupServiceCoreImpl getService() {
        return service;
    }    
    
    public GroupService getGroupService()
    {
        return groupService;
    }
}
