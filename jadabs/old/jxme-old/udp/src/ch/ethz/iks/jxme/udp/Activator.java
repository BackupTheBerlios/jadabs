/*
 * Created on May 4, 2004
 *
 */
package ch.ethz.iks.jxme.udp;

import java.io.IOException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.ethz.iks.jxme.IPeerNetwork;
import ch.ethz.jadabs.osgiaop.AOPContext;
import ch.ethz.jadabs.osgiaop.AOPServiceRegistration;


/**
 * @author andfrei
 * 
 */
public class Activator implements BundleActivator
{

    static BundleContext bc;
    
    private IPeerNetwork udpnet;
    AOPServiceRegistration udpnetsvcreg;
    
    public Activator()
    {
        
    }
    
    /*
     */
    public void start(BundleContext context) throws Exception
    {
        Activator.bc = context;
        
        System.out.println("called UDPPeerNetwork.start()");
        String peername = (String)context.getProperty(
                IPeerNetwork.JXME_PEERNAME);
        
//        if (Benchmark.w_proxy)
//        {
            // setup as AOPProxy - nanning
            udpnetsvcreg = ((AOPContext) context).registerAOPService(
                    IPeerNetwork.class,
                    UDPPeerNetwork.Instance(), null);
            udpnet = (IPeerNetwork)udpnetsvcreg.getProxy();
//        }
//        else {
            // normal service registration
//            udpnet = UDPPeerNetwork.Instance();
//        }
        
        // start service
        try {
            udpnet.connect();
        } catch (IOException ioe) {
            throw new Exception("could not open UDP connection: ", ioe);
        }
        
        // register this peernetwork
        context.registerService(IPeerNetwork.class.getName(), udpnet, null);
    }

    /*
     */
    public void stop(BundleContext context) throws Exception
    {
        udpnet.close(null, null, null);
    }

}
