/*
 * Created on Nov 11, 2004
 *
 */
package gov.nist.sip.proxy.gui;

import gov.nist.sip.proxy.Proxy;
import gov.nist.sip.proxy.ProxyActivator;
import gov.nist.sip.proxy.ProxyDebug;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;


/**
 * @author andfrei
 * 
 */
public class ProxyLauncherActivator implements BundleActivator
{

    public static BundleContext bc;
    
    ProxyLauncher plauncher;
    
    static Proxy proxy;
    
    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bc) throws Exception
    {
        ProxyLauncherActivator.bc = bc;
        
        final String configfile = "bundle://" + ProxyActivator.bc.getBundle().getBundleId() + ":0"
        + "/gov/nist/sip/proxy/configuration/configuration.xml";
        
        Thread thread = new Thread()
        {

            public void run()
            {
                try
                {
                    // the Proxy:
                    ProxyLauncher proxyLauncher = new ProxyLauncher(configfile);
                    //proxyLauncher.start();
                    ProxyDebug.println("Proxy ready to work");
                    
                    ServiceReference sref = ProxyLauncherActivator.bc.
                		getServiceReference(Proxy.class.getName());
                    proxy = (Proxy)ProxyLauncherActivator.bc.getService(sref);
                    
                } catch (Exception e)
                {
                    System.out.println("ERROR: Set the configuration file flag: " + "USE: -cf configuration_file_location.xml");
                    System.out.println("ERROR, the proxy can not be started, " + " exception raised:\n");
                    e.printStackTrace();
                }

            }
        };

        thread.start();
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext arg0) throws Exception
    {

    }

}
