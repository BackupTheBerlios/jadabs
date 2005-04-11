/*
 * Created on Dec 23, 2004
 * 
 * This class implements the BundleInfoListener functionality from jadabs.remotefw.
 * 
 * There are two methods which are mandatory to implement: bundleChanged and
 * allBundlesChanged.
 * These Events are propagated to the AmonemManager, new (= unknown) bundles of a peer
 * are inserted into the discoveryDAG.
 * 
 */
package ch.ethz.jadabs.amonem.discovery;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.amonem.AmonemManager;
import ch.ethz.jadabs.amonem.deploy.AmonemDeploy;
import ch.ethz.jadabs.amonem.manager.DAGBundle;
import ch.ethz.jadabs.amonem.manager.DAGGroup;
import ch.ethz.jadabs.amonem.manager.DAGPeer;
import ch.ethz.jadabs.remotefw.BundleInfo;
import ch.ethz.jadabs.remotefw.BundleInfoListener;
import ch.ethz.jadabs.remotefw.Framework;


/**
 * 
 * This class implements the BundleInfoListener functionality from jadabs.remotefw.
 * 
 * There are two methods which are mandatory to implement: bundleChanged and
 * allBundlesChanged.
 * These Events are propagated to the AmonemManager, new (= unknown) bundles of a peer
 * are inserted into the discoveryDAG.
 * 
 * @author bam
 */
public class AmonemBundleInfoListener implements BundleInfoListener {
    
    private Logger LOG = Logger.getLogger(AmonemDeploy.class.getName());
    
    private AmonemManager amonemManager;
    
    
    /**
     * 
     * @param amonemManager The AmonemManager that has to be informed on changes.
     */
    public AmonemBundleInfoListener(AmonemManager amonemManager) {
        // amonemManager needed to propagate events
        this.amonemManager = amonemManager;
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.remotefw.BundleInfoListener#bundleChanged(java.lang.String, ch.ethz.jadabs.remotefw.BundleInfo)
     */
    
    /**
     *
     * @param peerName The name of the peer of which a bundle changed
     * @param bundleInfo The ch.ethz.jadabs.remotefw.BundleInfo object
     */
    public void bundleChanged(String peerName, BundleInfo bundleInfo) {
        
        LOG.debug("bundleChanged event received. Peer: " + peerName);
        
        // work on the discoveryDAG, not the deployDAG
        DAGGroup root = amonemManager.getDiscoveryROOT();
        DAGPeer peer = (DAGPeer)root.getElement(peerName);
        DAGBundle bundle = peer.getBundle(bundleInfo.bid);
        
        if(bundle == null) {
            // if this is a new bundle, insert it into the discoveryDAG
            LOG.debug("Bundle (" + bundleInfo + ") ist null, mache ein neues. Peer: " + peerName);
            add_new_bundle(peer, bundleInfo);
        }
        else {
            // if this is a known bundle, update its state
            LOG.debug("Bundle (" + bundleInfo + ") gefunden, update. Peer: " + peerName);
            bundle.setState(bundleInfo.state);
        }
        
        /*
         * in all cases, inform manager about change (propagate event)
         */
        amonemManager.bundleChanged(peerName);
        
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.remotefw.BundleInfoListener#allBundlesChanged(java.lang.String, ch.ethz.jadabs.remotefw.Framework)
     */
    
    /**
     * 
     * @param peerName The name of the peer of which a bundle changed
     * @param fw The ch.ethz.jadabs.remotefw.Framework object
     */
    public void allBundlesChanged(String peerName, Framework fw) {

        LOG.debug("allBundlesChanged event received. Peer: " + peerName);
        
        // work on the discoveryDAG, not the deployDAG
        DAGGroup root = amonemManager.getDiscoveryROOT();
        DAGPeer peer = (DAGPeer)root.getElement(peerName);
        long bids[] = fw.getBundles();
        int i;
        
        // iterate over all installed bundles
        for (i = 0; i < bids.length; i++) {
            DAGBundle bundle = peer.getBundle(bids[i]);
            
            if(bundle == null) {
                add_new_bundle(peer, fw.getBundleInfo(bids[i]));
            }
            else {
                bundle.setState(fw.getBundleState(bids[i]));
            }

        }

        // inform the manager about the change (propagate event)
        amonemManager.bundleChanged(peerName);
        
    }
    
    
    /**
     * Adds the info of a bundle to the peer. This id done by filling a DAGBundle object with
     * the necessary information and then adding it to the peer.
     * 
     * @param dp The DAGPeer object the bundle has to be added to
     * @param bi The ch.ethz.jadabs.remotefw.BundleInfo object containing the information for the new peer
     */
    private void add_new_bundle(DAGPeer dp, BundleInfo bi) {
        
        DAGBundle tmpBundle = new DAGBundle();
        
        tmpBundle.setBundleID(bi.bid);
        tmpBundle.setName(bi.name);
        tmpBundle.setState(bi.state);
        dp.setBundle(tmpBundle);
        
    }

}
