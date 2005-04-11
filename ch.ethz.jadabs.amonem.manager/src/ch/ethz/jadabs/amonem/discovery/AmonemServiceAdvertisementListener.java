/*
 * Created on Feb 12, 2005
 *
 */
package ch.ethz.jadabs.amonem.discovery;

import ch.ethz.jadabs.amonem.manager.AmonemManagerImpl;
import ch.ethz.jadabs.amonem.manager.DAGGroup;
import ch.ethz.jadabs.amonem.manager.DAGPeer;
import ch.ethz.jadabs.servicemanager.ServiceAdvertisementListener;
import ch.ethz.jadabs.servicemanager.ServiceReference;


/**
 * @author andfrei
 * 
 */
public class AmonemServiceAdvertisementListener implements ServiceAdvertisementListener
{

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.servicemanager.ServiceAdvertisementListener#foundService(ch.ethz.jadabs.servicemanager.ServiceReference)
     */
    public void foundService(ServiceReference sref)
    {
        System.out.println("found new service: "+sref.getID());

//        LOG.debug("found new service: " + sref.getID());
        
        // work on the discoveryDAG, not the deployDAG
        DAGGroup root = AmonemManagerImpl.Instance().getDiscoveryROOT();
        DAGPeer peer = (DAGPeer)root.getElement(sref.getPeer());
        
        peer.addServiceReference(sref);
        
        AmonemManagerImpl.Instance().serviceReferenceAdded(sref);
    }

    public void removedService(ServiceReference sref)
    {
        
    }
}
