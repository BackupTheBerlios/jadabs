package ch.ethz.jadabs.amonem.manager;
import java.util.Enumeration;
import java.util.Vector;

/*
 * Created on 21.11.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * This class generates Iterators for all Groups and all Peers in a DAG
 * 
 * @author barbara
 */
public class DAGIterator {

	private Vector allPeers= new Vector();
	private DAGMember ROOT;
	private Enumeration myPeerEnumeration;
	private Vector allGroups= new Vector();
	private Enumeration myGroupEnumeration;
	
	/**
	 * @param root of the DAG
	 */
	public DAGIterator(DAGMember root){
		//System.out.println("neuer Iterator");
		ROOT =root;
		//System.out.println("es hat "+ allPeers.size()+ " Peers.");
		getAllPeers();
		myPeerEnumeration = allPeers.elements();
		getAllGroups();
		myGroupEnumeration = allGroups.elements();
	}
	
	/**
	 * creates a List of all Peers
	 *
	 */
	public void newPeerEnumeration(){
		allPeers.clear();
		getAllPeers();
		myPeerEnumeration = allPeers.elements();
	}
	
	/**
	 * 
	 * @return returns the next DAGPeer of the Enumeration
	 */
	public DAGPeer getNextPeer(){
		if (myPeerEnumeration.hasMoreElements()){
			return (DAGPeer) myPeerEnumeration.nextElement();
		}
		else return null;
	}
	
	/**
	 * 
	 * @return boolean
	 */
	public boolean hasMorePeers(){
		if (myPeerEnumeration.hasMoreElements())
			return true;
		else return false;
	}
	
	/**
	 * fills the Vector allPeers
	 *
	 */
	private void getAllPeers(){
		Vector currentChildren= new Vector();
		if (ROOT.getType() == 1){
			currentChildren = ((DAGGroup) ROOT).getChildren();
			Enumeration iter =currentChildren.elements();
			while (iter.hasMoreElements()){
				DAGMember cur = (DAGMember) iter.nextElement();
				if (cur.getType()==1){
					getPeers((DAGGroup) cur);
				}
				else{
					allPeers.add(cur);
				}
			}
		}
	}
	
	/**
	 * adds all Peers of this group to allPeers
	 * @param Group 
	 */
	private void getPeers(DAGGroup Group){
		Vector currentChildren= new Vector();
		currentChildren = Group.getChildren();
		Enumeration iter = currentChildren.elements();
		while (iter.hasMoreElements()){
			DAGMember cur = (DAGMember) iter.nextElement();
			if (cur.getType()==1){
				getPeers((DAGGroup) cur);
			}
			else{
				if (!PeerIsMember((DAGPeer) cur))
					allPeers.add(cur);
			}
		}
	}
	
	/**
	 * 
	 * @param searched peer 
	 * @return found
	 */
	public boolean PeerIsMember(DAGPeer peer){
		return allPeers.contains(peer);
	}
	
	/**
	 * produces a Vector of all Groups
	 *
	 */
	public void newGroupEnumeration(){
		//System.out.println("Groupenumeration");
		allPeers.clear();
		getAllGroups();
		myGroupEnumeration = allGroups.elements();
	}
	
	/**
	 * 
	 * @return next DAGGroup
	 */
	public DAGGroup getNextGroup(){
		if (myGroupEnumeration.hasMoreElements()){
			return (DAGGroup) myGroupEnumeration.nextElement();
		}
		else return null;
	}
	/**
	 * 
	 * @return boolean
	 */
	public boolean hasMoreGroups(){
		if (myGroupEnumeration.hasMoreElements())
			return true;
		else return false;
	}
	/**
	 * fills the Vector of all Groups
	 *
	 */
	private void getAllGroups(){
		if (ROOT.getType() == 1){
			//System.out.println("getAllGroups");
			if (!GroupIsMember((DAGGroup) ROOT)){
				allGroups.add(ROOT);
			}
			getGroups((DAGGroup) ROOT);
		}
	}
	
	/**
	 * gets all Groups of a Group
	 * @param Group
	 */
	private void getGroups(DAGGroup Group){
		Vector currentChildren= new Vector();
		currentChildren = Group.getChildren();
		Enumeration iter = currentChildren.elements();
		while (iter.hasMoreElements()){
			DAGMember cur = (DAGMember) iter.nextElement();
			if (cur.getType()==1){
				getGroups((DAGGroup) cur);
				if (!GroupIsMember((DAGGroup) cur)){
					int pos = allGroups.size();
					//System.out.println(pos);
					allGroups.add(pos,cur);
				}
			}
		}
	}
	
	/**
	 * checks if a group is member of another group
	 * @param group
	 * @return boolean
	 */
	public boolean GroupIsMember(DAGGroup group){
		return allGroups.contains(group);
	}
}
