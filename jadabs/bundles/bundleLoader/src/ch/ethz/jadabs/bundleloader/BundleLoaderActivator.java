package ch.ethz.jadabs.bundleloader;


import java.util.Vector;
import java.io.File;
import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.ethz.jadabs.http.HttpDaemon;


/**
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class BundleLoaderActivator implements BundleActivator 
{
    
	protected static Logger LOG = Logger.getLogger(BundleLoaderImpl.class.getName());
	
	protected static BundleContext bc;
	protected static BundleLoaderImpl bundleLoader;
	static String repository;

	
	/**
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bc) throws Exception 
	{
		LOG.info("BundleLoader starting ...");			
		BundleLoaderActivator.bc = bc;
				
		String location = BundleLoaderActivator.bc.getBundle().getLocation();
	    repository = location.substring(location.indexOf(":")+1, location.indexOf("jadabs") - 1);
	    	    
		// register all system bundles
		Bundle [] bundles = BundleLoaderActivator.bc.getBundles();
		Vector sysBundles = new Vector();
		
		for (int i = 0; i < bundles.length; i++) {
			
			String loc = bundles[i].getLocation();			
			int pos = loc.lastIndexOf(File.separatorChar);
									
			if (pos > -1) {
				loc = loc.substring(pos+1);
				pos = loc.indexOf(".jar");							
				if (pos > -1) {
				 	loc = loc.substring(0,pos);	
				}
			} 
			sysBundles.add(loc);							
		}

		// OSGi API is provided by knopflerfish
		sysBundles.add(new String("osgi-framework-1.2"));
		// this is a hack
		sysBundles.add(new String("log4j-cdc-0.7.1-SNAPSHOT"));
		
		// start a http daemon to answer bundle loader requests
		new HttpDaemon(new BundleLoaderHandler()).start();
		
		// instanciate BundleLoader, register and start
		BundleLoaderActivator.bundleLoader = new BundleLoaderImpl(sysBundles);
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
	
