/*
 * Created on 11.01.2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ch.ethz.jadabs.amonem.manager;

import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author bam
 * 
 * This class parses the information for the bundles out of a repository
 * file. In adwance this has to be downloaded, by the manager.
 */
public class RepoParser {
	
	private Vector Jars;
/*	
	public static void main(String[] args){
		RepoParser tester= new RepoParser();
		RepoParser.parse("repository.xml");
	}*/
	/**
	 * gets all infos needed out of a repositoryfile. then this Bundleinfos
	 * are stored in a Vector 
	 * @param Name
	 * @return Vector
	 */
	public Vector parse(String name){
		Parser ro= new Parser();
		Jars= new Vector();
		Element rootelem= ro.parse(name);
		NodeList BundleList= rootelem.getElementsByTagName("bundle");
		int stop= BundleList.getLength();
		for (int i=0 ; i< stop; i++){
				Element cur= (Element) BundleList.item(i);
				parseBundle(cur);
		}
		return Jars;
	}
	/**
	 * creates one new bundleobject if all information needed can be taken out of the file 
	 * @param bundleelement
	 */
	private void parseBundle(Element bundle){
		RepositorySkeleton skel= new RepositorySkeleton();
		String NameText="";
		String VersionText="";
		NodeList NAME= bundle.getElementsByTagName("bundle-name");
		int stop= NAME.getLength();
		for (int i=0; i<stop; i++){
			Node curName= (Element) NAME.item(i);
			Node child= curName.getFirstChild();
			if (child.getNodeType()== Element.TEXT_NODE){
				NameText= child.getNodeValue();
			}
		}
		NodeList Version= bundle.getElementsByTagName("bundle-version");
		if (Version.getLength()>0){
			VersionText= (Version.item(0)).getFirstChild().getNodeValue();
		}
		skel.setJar(NameText+"-"+VersionText);
		
		boolean uuid=false;
		NodeList UUID= bundle.getElementsByTagName("bundle-uuid");
		if (UUID.getLength()>0){
			String UUIDText= (UUID.item(0)).getFirstChild().getNodeValue();
			//System.out.println(UUIDText);
			skel.setUuid(UUIDText);
			uuid=true;
		}
		NodeList UpdateLocation= bundle.getElementsByTagName("bundle-updatelocation");
		if (UpdateLocation.getLength()>0){
			String UpdateText= (UpdateLocation.item(0)).getFirstChild().getNodeValue();
			//System.out.println(UpdateText);
			skel.setUpdatelocation(UpdateText);
		}
		if (uuid){
			Jars.add(skel);
		}
	}
}
