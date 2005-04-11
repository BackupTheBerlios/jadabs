package ch.ethz.jadabs.amonem.manager;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/*
 * Created on 03.01.2005
 *
 */

/**
 * @author bam
 *
 * This class uses the Parser to parse an xml-File wich contains a saved
 * DAG. It extracts the DAGPeer-Informations and creates Stubs. This Stubs
 * are then returned to the manager.
 * 
 * TODO The group functionality is not jet implementet, because we are not
 * jet able to create groups.
 */
public class DAGParser {
	
	private Vector Skeletons= new Vector();
	private Vector Peers = new Vector();
	private String LOCAL_DEPLOY_PATH;
	
	/**
	 * parses the file reachable under the filename "name"
	 * 
	 * @param path name of the imported file 
	 * @return Vectro of Deploy Skeletons
	 */
	public Vector parse(String name){
		Parser ro= new Parser();
		Element rootelem= ro.parse(name);
		
		NodeList DAGPeers= rootelem.getElementsByTagName("DAGPeer");
		int stopPeers= DAGPeers.getLength();
		for (int i=0 ; i< stopPeers; i++){
				Element cur= (Element) DAGPeers.item(i);
				parsePeer(cur);
		}
		
/*		This has to be used when it is possible to create new Groups.
 * 
 * 		NodeList DAGGroups= rootelem.getElementsByTagName("DAGGroup");
		int stopGroups= DAGGroups.getLength();
		for (int i=0 ; i< stopGroups; i++){
				Node cur= DAGGroups.item(i);
				parseGroup(cur);
		}*/
		
		return Peers;
	}
	
	/**
	 * parses one DOM element (DAGPeer) and saves the information in
	 * a new AmonemDeploySekleton
	 * 
	 * @param peerElement to be parsed
	 */
	private void parsePeer(Element peer){
		String NameText="";
		String JavapathText="";
		String PlatformText="";
		NodeList Name= peer.getElementsByTagName("name");
		if (Name.getLength()>0){
			NameText= (Name.item(0)).getFirstChild().getNodeValue();
		}
		//System.out.println("Name "+NameText);
		DAGPeer tempPeer= new DAGPeer(NameText);
		
		NodeList DEPLOY_PATH= peer.getElementsByTagName("deploy-path");
		if (DEPLOY_PATH.getLength()>0){
			LOCAL_DEPLOY_PATH= (DEPLOY_PATH.item(0)).getFirstChild().getNodeValue();
		}
		tempPeer.setDEPLOY_PATH(LOCAL_DEPLOY_PATH);
		
		NodeList Javapath= peer.getElementsByTagName("javapath");
		if (Javapath.getLength()>0){
			JavapathText= (Javapath.item(0)).getFirstChild().getNodeValue();
		}
        tempPeer.setJavaPath(JavapathText);
        //System.out.println("JavaPath "+JavapathText);
        
		NodeList Platform= peer.getElementsByTagName("platform");
		if (Platform.getLength()>0){
			PlatformText= (Platform.item(0)).getFirstChild().getNodeValue();
			System.out.println("Platform "+PlatformText);
		}
		tempPeer.setPlatform(PlatformText);
		
		NodeList Bundles= peer.getElementsByTagName("bundles");
		int stop=Bundles.getLength();
		for (int i=0; i<stop; i++){
			Element Bundle= (Element) Bundles.item(0);
			NodeList bundlelist= Bundle.getElementsByTagName("bundle");
			int anzahlBundles= bundlelist.getLength();
			for (int k=0; k<anzahlBundles; k++){
				DAGBundle tempBundle= new DAGBundle();
				NodeList UUID= ((Element)bundlelist.item(k)).getElementsByTagName("uuid");
				if (UUID.getLength()>0){
					String UUIDText= (UUID.item(0)).getFirstChild().getNodeValue();
					//System.out.println(UUIDText);
					tempBundle.setUUID(UUIDText);
				}
				NodeList BundleName= ((Element)bundlelist.item(k)).getElementsByTagName("bundle-name");
				if (BundleName.getLength()>0){
					String BundleNameText= (BundleName.item(0)).getFirstChild().getNodeValue();
					//System.out.println(BundleNameText);
					tempBundle.setName(BundleNameText);
				}
				NodeList location= ((Element)bundlelist.item(k)).getElementsByTagName("update-location");
				if (location.getLength()>0){
					String LocationText= (location.item(0)).getFirstChild().getNodeValue();
					//System.out.println(LocationText);
					tempBundle.setUpdateLocation(LocationText);
				}
				tempPeer.setBundle(tempBundle);
			}
		}
		Peers.add(tempPeer);
	}

	/**
	 * will parse a group-DOM-element. not jet implemented
	 * 
	 * @param group
	 */
	private void parseGroup(Node group){
		//has to be implementet, if there is the possibility to have 
		// more than one group
	}
}
