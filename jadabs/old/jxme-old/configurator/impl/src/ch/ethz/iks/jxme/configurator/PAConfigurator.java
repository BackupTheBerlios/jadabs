/*
 * Created on Nov 20, 2003
 * 
 * $Id: PAConfigurator.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.iks.jxme.configurator;

import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import nanoxml.XMLElement;
import nanoxml.XMLParseException;

/**
 * PAConfigurator allows to configure a Peer with its specific properties which are
 * the PeerAdvertisments.
 * 
 * @author daniel
 * @version $revision$
 */
public class PAConfigurator {

	XMLElement configFile = new XMLElement();

	static String m_peername;

	static String m_peerconfigfile;

	/**
	 * Initialize the PeerAdvertisment Configurator with an xml config file.
	 * 
	 * @param peerconfigfile - from the path where jadabs is started 
	 * TODO: should be in the component where it is needed
	 * @throws XMLParseException
	 * @throws IOException
	 */
	public PAConfigurator(String peername) throws IOException{
        m_peername = peername;
        m_peerconfigfile = peername + ".xml";
        
		FileReader reader = new FileReader(m_peerconfigfile);
		configFile.parseFromReader(reader);	
	}

	/** @return Peer ID defined in the current configuration file  */
	public String getPID(){
		Vector children = configFile.getChildren();
		Enumeration enum = children.elements();
		
		while( enum.hasMoreElements() ){
			XMLElement currentElement = (XMLElement)enum.nextElement();
			if (currentElement.getName().equalsIgnoreCase("PID")){
				return currentElement.getContent();
			}
		}
		
		return null;
	}
	
	/** @return name defined in the current configuration file */
	public String getName(){
		Vector children = configFile.getChildren();
		Enumeration enum = children.elements();
		
		while( enum.hasMoreElements() ){
			XMLElement currentElement = (XMLElement)enum.nextElement();
			if (currentElement.getName().equalsIgnoreCase("Name")){
				return currentElement.getContent();
			}
		}
		return null;
	}
	
	/**
	 * Returns the specified interfaces.
	 * 
	 * @return
	 */
	public String[] getInterfaces(){
		Vector children = configFile.getChildren();
		Vector interfaces = new Vector();
		Enumeration enum = children.elements();
		
		while( enum.hasMoreElements() ){
			XMLElement currentElement = (XMLElement)enum.nextElement();
			if (currentElement.getName().equalsIgnoreCase("Interface")){
				interfaces.add(currentElement.getAttribute("technology"));
			}
		}
		
		String[] interfaceNames = new String[interfaces.size()];
		interfaces.toArray(interfaceNames);
		
		return interfaceNames;
	}
	
	/**
	 * Return a Hashtable of parameters for the given interface.
	 * 
	 * @param interfaceName
	 * @return
	 */
	public Hashtable getParameters(String interfaceName){
		Vector children = configFile.getChildren();
		Enumeration enum = children.elements();
		Hashtable parameterList = new Hashtable();
		
		while( enum.hasMoreElements() ){
			XMLElement currentElement = (XMLElement)enum.nextElement();
			
			if (currentElement.getName().equalsIgnoreCase("Interface")){
				if(currentElement.getStringAttribute("technology").equalsIgnoreCase(interfaceName)){
					Vector parameters = currentElement.getChildren();
					Enumeration params = parameters.elements();
					
					while(params.hasMoreElements()){
						XMLElement parameter = (XMLElement)params.nextElement();
						if(parameter.getName().equalsIgnoreCase("Parameter")){
							parameterList.put(
										parameter.getStringAttribute("key"),
										parameter.getStringAttribute("value")
							);
						}
					}
					return parameterList;
				}
			}
		}
		
		return null;
	}
	
	public String toString(){
		return configFile.toString();
	}

}
