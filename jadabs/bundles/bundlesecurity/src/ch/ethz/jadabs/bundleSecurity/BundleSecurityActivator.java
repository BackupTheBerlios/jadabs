/*
 * Created on Jul 6, 2005
 */
package ch.ethz.jadabs.bundleSecurity;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.ethz.jadabs.bundleLoader.api.BundleSecurity;

/**
 * @author otmar
 */
public class BundleSecurityActivator implements BundleActivator {
    
    private static Logger LOG = Logger.getLogger(BundleSecurityActivator.class.getName());
    
    protected static BundleSecurity security;
    public static BundleContext bc;

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bc) throws Exception {
        
        LOG.debug("starting bundlesecurity bundle...");
        BundleSecurityActivator.bc = bc;
//      instanciate BundleLoader, register and start
		BundleSecurityActivator.security = BundleSecurityImpl.getInstance();
		bc.registerService(BundleSecurity.class.getName(), security, null);
		LOG.info("BundleSecurity bundle started.");
		
//		if (LOG.isDebugEnabled()){
//		    Provider[] providers = java.security.Security.getProviders();
//			LOG.debug("Available providers:");
//			for (int i = 0; i < providers.length; i++) {
//	            LOG.debug(providers[i].getName() + ": " + providers[i].getInfo());
//	        }
//		}
		
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext arg0) throws Exception {
        
        security = null;
        bc = null;
    }

}
