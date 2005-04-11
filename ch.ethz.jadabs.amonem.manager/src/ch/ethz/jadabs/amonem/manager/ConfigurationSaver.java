/*
 * Created on 24.01.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ch.ethz.jadabs.amonem.manager;

import java.io.File;
import java.util.Enumeration;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Saves the current configuration stored in the Deploy-DAG. The files are
 * putted into a directory with the Configuration's Name.
 * 
 * @author barbara
 *
 */
public class ConfigurationSaver {
	private DomGenerator Generator= new DomGenerator();
	private PrintDomTree Printer= new PrintDomTree();
	private DAGMember ROOT;
	private String LocationName;
	private String ConfigName;
	private String FileSeparator;
	
	/**
	 * @param Root of the deploy DAG
	 * @param path of the location where it hase to be saved
	 * @param name of the configuration
	 */
	public boolean save(DAGMember root, String location, String Name){
		//System.out.println("start");
		ROOT = root;
		boolean success=false;
		ConfigName= Name;
		File newFile= new File(location, ConfigName);
		newFile.mkdir();
		FileSeparator= System.getProperty("file.separator");
		LocationName= location + ConfigName;
		//System.out.println(LocationName);
		success= savetotal();
		ROOT= root;
		DAGIterator Iterator= new DAGIterator(root);
		while (Iterator.hasMoreGroups()){
			// does nothing at the moment
			DAGGroup cur= (DAGGroup) Iterator.getNextGroup();
		}
		while (Iterator.hasMorePeers()){
			DAGPeer cur= (DAGPeer) Iterator.getNextPeer();
			success= savePeer(cur);
		}
		return success;
	}
	
	/**
	 * saves only one Peer, given by it's object
	 * 
	 * @param Location
	 * @param Peer
	 */
	public void saveOnlyPeer(String Location, DAGPeer Peer){
		Document Peerdoc = new DomGenerator().PeerDom(Peer);
		if (Peerdoc!=null){
			String FileName= Location+ Peer.getName()+".xml";
			//System.out.println("speichere nach: " + FileName);
			PrintDomTree.print(Peerdoc, FileName);
		}
	}
	
	/**
	 * this method saves the whole configuration in the CnofigName_total.xml
	 * file. 
	 * 
	 * @return saving successful
	 */
	private boolean savetotal(){
		boolean success= true;
		Document Totaldoc = new DomGenerator().DAGDom(ROOT);
		if (Totaldoc!=null){
			String FileName= LocationName+ FileSeparator+ ConfigName+ "_total.xml";
			//System.out.println(FileName);
			success= PrintDomTree.print(Totaldoc, FileName);
		} else {
			success= false;
		}
		return success;
	}
	
	/**
	 * this method saves one peer in the ConfigName_PeerName.xml file
	 * 
	 * @param peer to be saved
	 * @return saving successful
	 */
	private boolean savePeer(DAGPeer peer){
		boolean success= true;
		Document Peerdoc = new DomGenerator().PeerDom(peer);
		if (Peerdoc!=null){
			String FileName= LocationName+ FileSeparator+ ConfigName+"_"+ peer.getName()+".xml";
			success= PrintDomTree.print(Peerdoc, FileName);
		} else {
			success= false;
		}
		return success;
	}
	
	/**
	 * this method will save a group. but is not jet implemented
	 * 
	 * @return saving successful
	 */
	private boolean saveGroup(){
		// is not jet available
		return true;
	}

}
