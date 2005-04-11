package ch.ethz.jadabs.amonem.ui.perspective;


import ch.ethz.jadabs.amonem.BundleListener;
import ch.ethz.jadabs.amonem.manager.DAGPeer;
import ch.ethz.jadabs.amonem.ui.views.PeerListView;
import ch.ethz.jadabs.amonem.ui.views.PropertyView;


public class BundleListenerImpl implements BundleListener{

	/**
     * 
     * This method is called by the manager after a bundle event.
     * 
     *  
     * @param peer A bundle of this peer caused the event
     * @return no return value
     */
	public void bundleChanged(DAGPeer peer) {
		
		PeerListView.bundleUpdateInDiscovery(peer);
		PeerListView.bundleUpdateInDeploy(peer);
		PropertyView.update();
		
	}

}

