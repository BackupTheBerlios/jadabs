/*
 * Created on Nov 11, 2004
 *
 */
package gov.nist.sip.proxy;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


/**
 * @author andfrei
 * 
 */
public class ProxyActivator implements BundleActivator
{

    public static BundleContext bc;
    
    ProxyAdmin proxyadmin;
    
    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bc) throws Exception
    {

        ProxyActivator.bc = bc;

        final String configfile = "./configuration/gov/nist/sip/proxy/configuration.xml";
        
        
        proxyadmin = new ProxyAdmin();
        
        // register the proxyadmin
        ProxyActivator.bc.registerService(ProxyAdmin.class.getName(), 
                proxyadmin, null);        

        Thread thread = new Thread()
        {

            public void run()
            {
                try
                {
                    
                    proxyadmin.startProxy(configfile);
                    
                } catch (Exception e)
                {
                    System.out.println("ERROR: Set the configuration file flag: "
                            + "USE: -cf configuration_file_location.xml");
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
    public void stop(BundleContext bc) throws Exception
    {
        proxyadmin.stopProxy();
    }

}
