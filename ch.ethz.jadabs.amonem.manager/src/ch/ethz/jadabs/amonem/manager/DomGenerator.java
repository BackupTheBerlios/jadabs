package ch.ethz.jadabs.amonem.manager;
import java.util.Enumeration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


/*
 * Created on 30.12.2004
 */

/**
 * This class generates a org.w3c.dom.Document (DOM) structure out of a 
 * DAG. The main function "DAGDom" gets the root of a DAG Structure, and
 * then it creates Elements for each DAGPeer, DAGGroup, DAGBundle, ...
 * @author bam
 *
 */
public class DomGenerator {

	private Document doc;
	private Element DAGMember1= null;
	private DAGIterator Iterator;
	
	public Document DAGDom(DAGMember root){
		Iterator= new DAGIterator(root);
		DocumentBuilderFactory factory;
		DocumentBuilder builder;
		try{
			factory= DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			Document doc= builder.newDocument();
			Element top= doc.createElement("root");
			
			//System.out.println("Gererator: root erzeugt");
			while (Iterator.hasMoreGroups()){
				DAGGroup cur= (DAGGroup) Iterator.getNextGroup();
				top.appendChild(saveGroup(cur, doc));
			}
			while (Iterator.hasMorePeers()){
				DAGPeer cur= (DAGPeer) Iterator.getNextPeer();
				top.appendChild(savePeer(cur, doc));
			}
			doc.appendChild(top);
			System.out.println(top);
			return doc;
		} catch (Exception e){
			System.out.println(" Exception aufgetreten");
		}
		return doc;
	}
	/**
	 * creates a w3c document out of a DAGPeer
	 * @param peer
	 * @return document
	 */
	public Document PeerDom(DAGPeer peer){
		DocumentBuilderFactory factory;
		DocumentBuilder builder;
		try{
			factory= DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			Document doc= builder.newDocument();
			Element top= doc.createElement("root");
			if (peer!=null){
				top.appendChild(savePeer(peer, doc));
			}
			doc.appendChild(top);
			return doc;
		} catch (Exception e){
			System.out.println(" Exception aufgetreten");
		}
		return doc;
	}
	
	/**
	 * This method should be extended, when the group functionality is available
	 * @param DAGGroup
	 * @param document
	 */
	
	private Element saveGroup(DAGGroup group, Document doc) {	
		Element Group= doc.createElement("DAGGroup");
		if (group.getName()!=null){
			Element name= doc.createElement("name");
			name.appendChild(doc.createTextNode(group.getName()));
			Group.appendChild(name);
		}
/*		Enumeration children= (group.getParents()).elements();
		while (children.hasMoreElements()){
			saveGroupChildren(Group, (DAGMember)children.nextElement());
		}*/
		return Group;
	}

	/**
	 * @param DAGPeer
	 * @param document
	 */
	private Element savePeer(DAGPeer peer, Document doc) {
		Element DAGMember= doc.createElement("DAGPeer");
		
		Element name= doc.createElement("name");
		name.appendChild(doc.createTextNode(peer.getName()));
		DAGMember.appendChild(name);
		
		if (peer.getJavaPath()!=null){
			Element Deploypath= doc.createElement("deploy-path");
			Deploypath.appendChild(doc.createTextNode(peer.getDEPLOY_PATH()));
			DAGMember.appendChild(Deploypath);
		}
		
		if (peer.getJavaPath()!=null){
			Element Javapath= doc.createElement("javapath");
			Javapath.appendChild(doc.createTextNode(peer.getJavaPath()));
			DAGMember.appendChild(Javapath);
		}

		if (peer.getPlatform()!= null){
			Element Platform= doc.createElement("platform");
			Platform.appendChild(doc.createTextNode(peer.getPlatform()));
			DAGMember.appendChild(Platform);
		}
		
		Element Bundles= doc.createElement("bundles");
		Enumeration bundles= (peer.getBundles()).elements();
		while (bundles.hasMoreElements()){
			Bundles.appendChild(savePeerBundle((DAGBundle)bundles.nextElement(), doc));
		}
		DAGMember.appendChild(Bundles);
		
		Element Parents = doc.createElement("parents");
		Enumeration parents= (peer.getParents()).elements();
		while (parents.hasMoreElements()){
			Parents.appendChild(saveParent((DAGMember)parents.nextElement(), doc));
		}
		DAGMember.appendChild(Parents);
		//System.out.println("Peer angefügt");
		return DAGMember;
	}
	
	/**
	 * 
	 * @param DAGBundle
	 * @param document
	 * @return Element
	 */
	private Element savePeerBundle(DAGBundle bundle, Document doc){
		Element bundleElem=doc.createElement("bundle");
		
		if (bundle.getName()!= null){
			Element name= doc.createElement("bundle-name");
			name.appendChild(doc.createTextNode(bundle.getName()));
			bundleElem.appendChild(name);
		}
		if (bundle.getUUID()!=null){
			Element UUID= doc.createElement("uuid");
			UUID.appendChild(doc.createTextNode(bundle.getUUID()));
			bundleElem.appendChild(UUID);
		}
		if (bundle.getUpdateLocation()!=null){
			Element Location= doc.createElement("update-location");
			Location.appendChild(doc.createTextNode(bundle.getUpdateLocation()));
			bundleElem.appendChild(Location);
		}
		return bundleElem;
	}
	/**
	 * 
	 * @param parent
	 * @param document
	 * @return Element
	 */
	private Element saveParent(DAGMember parent, Document doc){
		if (parent.getName()!= null){
			Element parentElem=doc.createElement("parent");
			parentElem.appendChild(doc.createTextNode(parent.getName()));
			return parentElem;
		}
		return null;
	}
	/**
	 * 
	 * @param child
	 * @param document
	 * @return Element
	 */
	private Element saveGroupChildren(DAGMember child, Document doc){
		if (child.getName()!=null){
			Element childElem=doc.createElement("child");
			childElem.appendChild(doc.createTextNode(child.getName()));
			return childElem;
		}
		return null;
	}
	/**
	 * only for test purposes
	 * @return document
	 */
	public Document generateTest(){
		DocumentBuilderFactory factory;
		DocumentBuilder builder;
		try{
			factory= DocumentBuilderFactory.newInstance();
			builder = factory.newDocumentBuilder();
			doc= builder.newDocument();
		} catch (Exception e){
			// do nothing
		}
		if (doc != null){
			Element root= doc.createElement("date");
			
			Element DAGMember1= doc.createElement("DAGMember");
			
			Element type= doc.createElement("Group");
			type.appendChild(doc.createTextNode("rootGroup"));
			DAGMember1.appendChild(type);
			
			Element properties= doc.createElement("Properties");
			properties.appendChild(doc.createTextNode("started"));
			DAGMember1.appendChild(properties);
			
			root.appendChild(DAGMember1);
			
			Element DAGMember2= doc.createElement("DAGMember");
			
			Element type2= doc.createElement("Group");
			type2.appendChild(doc.createTextNode("A"));
			DAGMember2.appendChild(type2);
			
			Element properties2= doc.createElement("Properties");
			properties2.appendChild(doc.createTextNode("stopped"));
			DAGMember2.appendChild(properties2);
			
			root.appendChild(DAGMember2);
			doc.appendChild(root);
		}
		return doc;
	}
	
}
