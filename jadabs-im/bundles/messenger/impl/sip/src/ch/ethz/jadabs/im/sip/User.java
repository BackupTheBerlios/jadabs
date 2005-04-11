package ch.ethz.jadabs.im.sip;

import java.util.Iterator;
import java.util.Vector;

/*
 * Created on Nov 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

/**
 * @author franz
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class User {
	private String name;
	private Vector sipNames = new Vector();
	private int status;
	
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void sendMessage() {
		
	}

	/**
	 * @param uri
	 */
	public void addSIPAddress(String uri) {
		// sip address must be correct !!!
		sipNames.addElement(uri);
	}
	
	public Vector getSIPAddresses() {
		return sipNames;
	}
	
	public String getXMLTag() {
		StringBuffer sb = new StringBuffer();
		sb.append("\t<buddy name=\""+name+"\">\n");
		Iterator i = sipNames.iterator();
		while (i.hasNext()) {
			sb.append("\t\t<sip alias=\""+i.next().toString()+"\"/>\n");
		}
		sb.append("\t<buddy/>\n");
		return sb.toString();
	}
	
	public String toString() {
		return getXMLTag();
	}
}
