/*
 * Created on Jan 26, 2005
 *
 */
package ch.ethz.jadabs.jxme.bundleservice;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.Peer;
import ch.ethz.jadabs.jxme.PeerGroup;
import ch.ethz.jadabs.jxme.PeerNetwork;
import ch.ethz.jadabs.jxme.services.GroupService;


/**
 * @author andfrei
 * 
 */
public class BundleServiceActivator implements BundleActivator
{

    static BundleContext bc;
    static GroupService groupService;
    
    BundleService bundleService;
    
    /*
     */
    public void start(BundleContext bc) throws Exception
    {
        BundleServiceActivator.bc = bc;
        
        // get GroupService
        // we assume this groupservice is the root group
        // TODO: has to be changed to take any groupservice
        ServiceReference sref = bc.getServiceReference("ch.ethz.jadabs.jxme.services.GroupService");
        groupService = (GroupService)bc.getService(sref);
        
        // get PeerNetwork
        sref = bc.getServiceReference("ch.ethz.jadabs.jxme.PeerNetwork");
        PeerNetwork peernetwork = (PeerNetwork)bc.getService(sref);
        
        Peer peer = peernetwork.getPeer();
        PeerGroup group = peernetwork.getPeerGroup();
                
        
        bundleService = new BundleService(peer, groupService);
        
        bc.registerService(BundleService.class.getName(), bundleService, null);
                
    }

    /*
     */
    public void stop(BundleContext bc) throws Exception
    {
        groupService.removeCoreService(BundleService.BUNDLESERVICE_NAME);
    }

}
