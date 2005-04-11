package ch.ethz.jadabs.amonem.ui.perspective;


import ch.ethz.jadabs.amonem.GroupListener;
import ch.ethz.jadabs.amonem.manager.DAGGroup;
import ch.ethz.jadabs.amonem.manager.DAGMember;
import ch.ethz.jadabs.amonem.manager.DAGPeer;
import ch.ethz.jadabs.amonem.ui.views.GraphView;
import ch.ethz.jadabs.amonem.ui.views.PeerListView;
import ch.ethz.jadabs.amonem.ui.views.PropertyView;


public class GroupListenerImpl implements GroupListener{

	/**
     * 
     * This method is called to add a new member into discovery tree. The method
     * is callec by the manager.
     * 
     * @param dagmember Member to add
     * @return no return value
     */
	public void childAddedInDiscovery(DAGMember dagmember) {
		
		DAGGroup root = Controller.getDiscoveryRoot();
		if(root == null){
			return;
		}
		GraphView.update();
		PeerListView.addPeerInDiscoveryUpdate((DAGPeer)dagmember);
		
	}
	
	/**
     * 
     * Call this method to add a new member into deploy tree. This method is called
     * by the manager.
     * 
     * @param dagmember Member to add
     * @return no return value
     */
	public void childAddedInDeploy(DAGMember dagmember) {
		
		DAGGroup root = Controller.getDeployRoot();
		if(root == null){
			return;
		}
		PeerListView.addPeerInDeployUpdate((DAGPeer)dagmember);
		
	}

	/**
     * 
     * Call this method to delete a member.
     * This means delete the member from discovery AND deploy tree.
     * 
     * @param dagmember Member to delete
     * @return no return value
     */
	public void childDeleted(DAGMember dagmember){
		
		DAGGroup root = Controller.getDeployRoot();
		if(root == null){
			return;
		}
		PeerListView.deletePeerUpdate((DAGPeer)dagmember);
		PropertyView.deletePeerUpdate((DAGPeer)dagmember);
		
	}
	
	/**
     * 
     * Call this method to remove a member from discovery tree.
     * The member will remain in the deploy tree !
     * 
     * @param dagmember Member to remove
     * @return no return value
     */
	public void childRemoved(DAGMember dagmember) {
		
		DAGGroup root = Controller.getDiscoveryRoot();
		if(root == null){
			return;
		}
		GraphView.update();
		PeerListView.removePeerUpdate((DAGPeer)dagmember);
		
	}


}
