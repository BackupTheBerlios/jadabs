/*
 * Created on Jan 26, 2005
 *
 */
package ch.ethz.jadabs.jxme.bundleservice.test;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.bundleservice.BundleService;


/**
 * @author andfrei
 * 
 */
public class BundleServiceTestActivator implements BundleActivator
{

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bc) throws Exception
    {

        ServiceReference sref = bc.getServiceReference(BundleService.class.getName());
        
        BundleService bundleService = (BundleService)bc.getService(sref);
        
        if (bc.getProperty("ch.ethz.jadabs.jxme.peeralias").equals("peer1"))
            bundleService.sendTestString("hallo");
        
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bc) throws Exception
    {

    }

}
