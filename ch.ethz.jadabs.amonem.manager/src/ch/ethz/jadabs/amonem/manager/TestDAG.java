package ch.ethz.jadabs.amonem.manager;

import java.util.Enumeration;

/*
 * Created on 18.11.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author barbara
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class TestDAG {

	public static void main(String[] args) {
		System.out.println("TestDAG");
		DAGGroup root= new DAGGroup("root");
		DAGGroup Group2 = new DAGGroup("Group2");
		DAGGroup Group3 = new DAGGroup("Group3");
		DAGGroup Group4 = new DAGGroup("Group4");
		DAGPeer Peer1 =new DAGPeer("Peer1");
		DAGPeer Peer2 =new DAGPeer("Peer2");	
		DAGPeer Peer3 =new DAGPeer("Peer3");	
		DAGPeer Peer4 =new DAGPeer("Peer4");
		root.addChild(Group2);
		root.addChild(Group4);
		Group2.addChild(Group3);
		Group3.addChild(Peer1);
		Group4.addChild(Peer3);
		Group4.addChild(Peer4);
		root.addChild(Peer2);
		testGroupEnumeration(root);
		Group2.addChild(Peer3);
		Group2.addChild(Peer4);
		testPeerEnumeration(root);
		Peer1.addConnection(Peer2, 1);
		Peer1.deleteConnection(Peer2, 1);
		Group3.removeChild(Peer1);
		testPeerEnumeration(root);
		root.removeChild(Group4);
		testGroupEnumeration(root);
		testPeerEnumeration(root);
		
	}
	
	private static void testPeerEnumeration(DAGGroup Group){
		DAGIterator MyIterator= new DAGIterator(Group);
		MyIterator.newPeerEnumeration();
		System.out.println("Start Enumeration");
		while (MyIterator.hasMorePeers()){
			DAGPeer cur= (DAGPeer) MyIterator.getNextPeer();
			System.out.println("Peer " + cur.getName());
			Enumeration curParents= cur.getParents().elements();
			while (curParents.hasMoreElements()){
				System.out.println("    Parent: "+ ((DAGGroup) curParents.nextElement()).getName());
			}
		}
		System.out.println("ende Enumeration");	
	}
	
	private static void testGroupEnumeration(DAGGroup Group){
		DAGIterator MyIterator= new DAGIterator(Group);
		MyIterator.newGroupEnumeration();
		System.out.println("Start Group Enumeration");
		while (MyIterator.hasMoreGroups()){
			DAGGroup cur= (DAGGroup) MyIterator.getNextGroup();
			System.out.println("Group " + cur.getName());
		}
		System.out.println("ende Group Enumeration");	
	}
}
