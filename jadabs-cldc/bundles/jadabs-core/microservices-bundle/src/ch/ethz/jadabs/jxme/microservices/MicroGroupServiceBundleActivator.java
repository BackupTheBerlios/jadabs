package ch.ethz.jadabs.jxme.microservices;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


/**
 * Bundle Activator for MicroService (service bundle sides)
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class MicroGroupServiceBundleActivator implements BundleActivator
{
    /** reference to the context of this bundle */
    static BundleContext bc;
    
    /** reference to the service object */ 
    MicroGroupServiceBundleImpl  service;
    
    /**
     * Start the MicroService bundle 
     * @param context reference to the context of this bundle 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception
    {
        MicroGroupServiceBundleActivator.bc = context; 
        service = new MicroGroupServiceBundleImpl(); 
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
}
