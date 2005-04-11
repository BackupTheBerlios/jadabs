/*
 * Created on Nov 11, 2004
 *
 */
package gov.nist.sip.proxy;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.ethz.jadabs.im.db.UserDB;

/**
 * @author andfrei
 * 
 */
public class ProxyActivator implements BundleActivator
{
	
	public static BundleContext bc;
	private static UserDB userDB;
	ProxyAdmin proxyadmin;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bc) throws Exception {
		ProxyActivator.bc = bc;
		
		final String configFile = System.getProperty("ch.ethz.jadabs.sip.proxy.config_file");
		final String cayenneConfigFile = System.getProperty("ch.ethz.jadabs.sip.proxy.cayenne_config_file");
		
		proxyadmin = new ProxyAdmin();
		
		// register the proxyadmin
		ProxyActivator.bc.registerService(ProxyAdmin.class.getName(), proxyadmin, null);        
		
		Thread thread = new Thread() {
			public void run() {
				//TODO try catch for config file !
				userDB = new UserDB(cayenneConfigFile);
				try {
					proxyadmin.startProxy(configFile);
				}
				catch (Exception e) {
					System.out.println("ERROR: Set the configuration file flag: "
							+ "USE: the -Dch.ethz.jadabs.sip.proxy.config_file System Property");
					System.out.println("ERROR, the proxy can not be started, " + " exception raised:\n");
					e.printStackTrace();
				}
			}
		};
		thread.start();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bc) throws Exception {
		proxyadmin.stopProxy();
	}
	public static UserDB getUserDB() {
		return userDB;
	}
}
