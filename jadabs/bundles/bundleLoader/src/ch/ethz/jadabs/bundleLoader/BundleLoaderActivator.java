/*
 * Created on 14-Feb-2005
 */
package ch.ethz.jadabs.bundleLoader;


import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.ethz.jadabs.bundleLoader.api.BundleLoader;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class BundleLoaderActivator implements BundleActivator {

	protected static Logger LOG = Logger.getLogger(BundleLoaderImpl.class.getName());
	
	public static BundleContext bc;
	protected static BundleLoaderImpl bundleLoader;
	
	// protected static HttpDaemon httpDaemon;
	

	
	/**
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bc) throws Exception 
	{
		LOG.info("BundleLoader starting ...");			
		BundleLoaderActivator.bc = bc;
				
		// start a http daemon to answer bundle loader requests
//		httpDaemon = new HttpDaemon();
//		httpDaemon.addRequestHandler(new BundleLoaderHandler());
//		httpDaemon.start();
		
		// instanciate BundleLoader, register and start
		BundleLoaderActivator.bundleLoader = BundleLoaderImpl.getInstance();
		bc.registerService(BundleLoader.class.getName(), bundleLoader, null);
		bc.addBundleListener(BundleLoaderActivator.bundleLoader);
//		BundleLoaderActivator.bundleLoader.startup();

		
	}

	/**
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bc) throws Exception {
		BundleLoaderActivator.bc = null;
		BundleLoaderActivator.bundleLoader = null;		
	}

}
