package ch.ethz.jadabs.bundleLoader;

import java.io.File;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


/**
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class BundleLoaderActivator implements BundleActivator {
    
	protected static Logger LOG = Logger.getLogger(BundleLoader.class.getName());
	protected static BundleContext bc;
	protected static BundleLoader bundleLoader;
	
	/**
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bc) throws Exception {
		System.out.println("BundleLoader starting ...");			
		BundleLoaderActivator.bc = bc;
		
		System.out.println(bc.getBundle().getLocation());

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
		
		// instanciate BundleLoader, register and start
		BundleLoaderActivator.bundleLoader = new BundleLoader(sysBundles);
		bc.registerService(IBundleLoader.class.getName(), bundleLoader, null);
		BundleLoaderActivator.bundleLoader.start();
		
		
			
	}

	/**
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bc) throws Exception {
		ServiceReference sref;
		
		BundleLoaderActivator.bc = null;
		BundleLoaderActivator.bundleLoader = null;		
	}

}
