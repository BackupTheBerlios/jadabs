/*
 * Created on Aug 15, 2003
 *
 * $Id: IDFactory.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.iks.id;

import ch.ethz.iks.jadabs.Jadabs;

/**
 * @author andfrei
 *
 */
public class IDFactory {

	private static IDFactory idfactory;

	private int id = 0;

	public IDFactory(){
		idfactory = this;
	}

	public static IDFactory Instance(){
		
		if (idfactory != null)
			return idfactory;
		else 
			return new IDFactory();		
	}
	
	/**
	 * Get a new  ID for an extension, basicaly the clname should be unique. This method puts only the
	 * the initial peername to the front.
	 * 
	 * @param clname
	 * @return
	 */
	public String newExtensionID(String clname){
		String peername = (String)Jadabs.Instance().getProperty(Jadabs.PEERNAME);
		
		String uuid = "event:"+peername+":"+ (id++);
		
		return uuid;
	}
	
}
