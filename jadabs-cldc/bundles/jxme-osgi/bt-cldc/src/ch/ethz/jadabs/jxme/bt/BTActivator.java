/*
 * Created on Jul 22, 2004
 * 
 * $Id: BTActivator.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package ch.ethz.jadabs.jxme.bt;

import java.io.IOException;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.EndpointService;
import ch.ethz.jadabs.jxme.PeerNetwork;

/**
 * This is the activator for the BTTransport technology adapter.
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class BTActivator implements BundleActivator
{
    /** reference to the BT transport instance */
    BTTransport btTransport;
    
    /** reference to the BT discovery instance */
    BTDiscovery discovery;
        
    /** bundle context of the BT bundle */
    static BundleContext bc;
    
    /** reference to the endpoint service */
    static EndpointService endptsvc;
    
    /** reference to the peernetwork */
    static PeerNetwork peernetwork;
    
    /** EndPoint representing this BT-Interface */
    private EndpointAddress btEndpoint; 
    
    /**
     * Empty activator constructor
     */
    public BTActivator() 
    {
        // empty
    }
    
    /**
     * Called when this bundle is started so the Framework can perform the
     * bundle-specific activities necessary to start this bundle. This method
     * can be used to register services or to allocate any resources that this
     * bundle needs.
     * 
     * <p>
     * This method must complete and return to its caller in a timely manner.
     * 
     * @param context
     *            The execution context of the bundle being started.
     * @exception java.lang.Exception
     *                If this method throws an exception, this bundle is marked
     *                as stopped and the Framework will remove this bundle's
     *                listeners, unregister all services registered by this
     *                bundle, and release all services used by this bundle.
     * @see Bundle#start
     */
    public void start(BundleContext context) throws IOException 
    {
        BTActivator.bc = context;
        
        // get Jxme EndpointService
        ServiceReference sref = context.getServiceReference("ch.ethz.jadabs.jxme.EndpointService");
        endptsvc = (EndpointService)context.getService(sref);
        
        // get Jxme PeerNetwork
        sref = context.getServiceReference("ch.ethz.jadabs.jxme.PeerNetwork");
        peernetwork = (PeerNetwork)context.getService(sref);
        
        // set BT transport
        // determine wether this instances runs as a peer (StreamConnection)
        // or as a Rendez-Vous peer (StreamConnectionNotifier)
        String rdp = context.getProperty("ch.ethz.jadabs.jxme.bt.rendezvouspeer");        
        if ((rdp != null) && (rdp.toLowerCase().trim().equals("true"))) {
            // run BTTransport in 'host' mode         
            btTransport = new BTTransport(false);
            discovery = new BTDiscovery(btTransport);            
        } else {
            // run BTTransport in 'client' mode
            btTransport = new BTTransport(true);
        }
        btTransport.init(null, endptsvc);
        btEndpoint = btTransport.getEndpointAddress();
        
        // add BT Transport to EndpointService
        endptsvc.addTransport(btTransport.getEndpointAddress(), btTransport);
    }

    /**
     * Called when this bundle is stopped so the Framework can perform the
     * bundle-specific activities necessary to stop the bundle. In general, this
     * method should undo the work that the <tt>BundleActivator.start</tt>
     * method started. There should be no active threads that were started by
     * this bundle when this bundle returns. A stopped bundle should be stopped
     * and should not call any Framework objects.
     * 
     * <p>
     * This method must complete and return to its caller in a timely manner.
     * 
     * @param context
     *            The execution context of the bundle being stopped.
     * @exception java.lang.Exception
     *                If this method throws an exception, the bundle is still
     *                marked as stopped, and the Framework will remove the
     *                bundle's listeners, unregister all services registered by
     *                the bundle, and release all services used by the bundle.
     * @see Bundle#stop
     */
    public void stop(BundleContext context) throws Exception
    {
        if (btTransport != null) {
            btTransport.stop();
            endptsvc.removeTransport(btEndpoint);
        }
    }
}