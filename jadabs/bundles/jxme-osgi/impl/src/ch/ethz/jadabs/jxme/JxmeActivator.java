/*
 * Created on Jul 15, 2004
 *
 */
package ch.ethz.jadabs.jxme;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * @author andfrei
 * 
 */
public class JxmeActivator implements BundleActivator
{
    
    public static BundleContext bc;
    static PeerNetwork pnet;
    
    /*
     */
    public void start(BundleContext bc) throws Exception
    {
        JxmeActivator.bc = bc;
        
        // set hostname or peeralias
        String hostname = bc.getProperty("ch.ethz.jadabs.jxme.peeralias");
                
//        if (hostname == null)
//        {
//            InetAddress localhost = InetAddress.getLocalHost();
//            hostname = localhost.getHostName();
//        }
        
        pnet = PeerNetwork.createInstance(hostname);
        
        pnet.init();
        
        bc.registerService("ch.ethz.jadabs.jxme.PeerNetwork", pnet, null);
    }

    /*
     */
    public void stop(BundleContext bc) throws Exception
    {
        
    }

}
