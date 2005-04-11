package ch.ethz.jadabs.amonem.manager;
import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
 * Created on 04.01.2005
 */

/**
 * @author barbara
 */
public class Parser {
	
	/**
	 * @param pathname of the xml-file
	 * @return Rootelement of the DOM-tree
	 */
	public Element parse(String name){
		Element E=null;
		try {
			Document D= DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(name));
			E= documentAnzeige(D);
		} catch (Exception e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return E;
	}
	
	/**
	 * @param document of the xml-file
	 * @return Rootelement of the DOM-tree
	 */
	private Element documentAnzeige(Document docu){
		Element E= docu.getDocumentElement();
		return E;
	}
}
