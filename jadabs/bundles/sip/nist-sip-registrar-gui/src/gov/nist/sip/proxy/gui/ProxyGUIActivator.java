/*
 * Created on Nov 11, 2004
 *
 */
package gov.nist.sip.proxy.gui;

import gov.nist.sip.proxy.ProxyActivator;
import gov.nist.sip.proxy.ProxyAdmin;
import gov.nist.sip.proxy.ProxyDebug;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * @author andfrei
 *  
 */
public class ProxyGUIActivator implements BundleActivator
{

    public static BundleContext bc;

    ProxyLauncher plauncher;

    static ProxyAdmin proxyadmin;
    

    /*
     * (non-Javadoc)
     * 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bc) throws Exception
    {
        ProxyGUIActivator.bc = bc;


        ServiceReference sref = ProxyGUIActivator.bc.getServiceReference(ProxyAdmin.class.getName());
        proxyadmin = (ProxyAdmin) ProxyGUIActivator.bc.getService(sref);
        
        Thread thread = new Thread()
        {

            public void run()
            {
                try
                {
                    // we need a synchronization between the registrar package,
                    // for the moment just wait some time
//                    Thread.sleep(5000);
                    
                    while (!proxyadmin.PROXY_STARTED)
                    {
                        synchronized(proxyadmin.syncstart)
                        {
                            System.out.println("is waiting for proxy to bestarted");
                            proxyadmin.syncstart.wait();
                        }
                    }
                                        
                    if (proxyadmin.proxy == null)
                        throw new Exception("proxy is null and maybe not initialized!");
                    
                    // the Proxy:
                    ProxyLauncher proxyLauncher = new ProxyLauncher();
                    //proxyLauncher.start();
                    ProxyDebug.println("Proxy ready to work");

                                        
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
    public void stop(BundleContext arg0) throws Exception
    {

    }

}