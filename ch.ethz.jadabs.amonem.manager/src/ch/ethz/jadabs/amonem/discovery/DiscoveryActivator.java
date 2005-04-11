/*
 * ====================================================================
 * BAM @09.02.2005
 * --------------------------------------------------------------------
 * 
 * This activator is no longer used. AmonemDiscovery is started by
 * the AmonemManager at "boot time" so no activator is needed.
 * 
 * We used it while implementing/testing AMONEM
 * 
 * ====================================================================
 * 
 * 
 * Created on Apr 2, 2004
 *
 */
package ch.ethz.jadabs.amonem.discovery;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.amonem.manager.DAGGroup;
import ch.ethz.jadabs.jxme.PeerGroup;
import ch.ethz.jadabs.jxme.PeerNetwork;
import ch.ethz.jadabs.jxme.services.GroupService;
import ch.ethz.jadabs.remotefw.FrameworkManager;


//import ch.ethz.jadabs.remotefw.FrameworkManager;

/**
 * @author bam
 *  
 */
public class DiscoveryActivator //implements BundleActivator
{

    private Logger LOG = Logger.getLogger(DiscoveryActivator.class.getName());
    
    static BundleContext bc;
    static String peername;
    
    /* remote fw manager */
    static FrameworkManager FWManager;
    
    static AmonemDiscovery amonemDisc;
    
    
    /**
     * 
     * @param bc
     * @throws Exception
     */
    public void start(BundleContext bc) throws Exception
    {
        // add context
        DiscoveryActivator.bc = bc;
        DiscoveryActivator.peername = bc.getProperty("ch.ethz.jadabs.jxme.peeralias");
        
        ServiceReference sRefGS = DiscoveryActivator.bc.getServiceReference("ch.ethz.jadabs.jxme.services.GroupService");
        GroupService gServ = (GroupService) DiscoveryActivator.bc.getService(sRefGS);
        
        // FrameworkManager
        ServiceReference sRefRM = DiscoveryActivator.bc.getServiceReference(FrameworkManager.class.getName());
        FWManager = (FrameworkManager) bc.getService(sRefRM);
//        rmanager.getFrameworks();

        // Peernetwork
        ServiceReference sRefPNet = DiscoveryActivator.bc.getServiceReference(PeerNetwork.class.getName());
        PeerNetwork pnet = (PeerNetwork) bc.getService(sRefPNet);
        
        // get The "worldgroup"
        PeerGroup pGroup = pnet.getPeerGroup();
        
//        LOG.debug("peergroup name:" + pGroup.getName());

        // dummy root for DAG, has to be passed by manager in real life...
        DAGGroup myDAG = new DAGGroup("rootGroup");
        myDAG.setResource(pGroup);
        
//        amonemDisc = new AmonemDiscovery(myDAG);
//        amonemDisc.start(pGroup, gServ, FWManager);

        //register service
        bc.registerService(AmonemDiscovery.class.getName(), amonemDisc, null);
        
        
        
    }

    
    /**
     * 
     * @param context
     * @throws Exception
     */
    public void stop(BundleContext context) throws Exception
    {
        bc = null;
//        amonemDisc.stop();
        amonemDisc = null;
    }

}