/*
 * Created on October 30, 2003
 *
 * $Id: ComponentResourceFactory.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.iks.jadabs;

/**
 * Concrete Factory for <code>IComponentResource</code> objects returning <code>ComponentResource</code> objects
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class ComponentResourceFactory implements IResourceFactory {

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IResourceFactory#createResource(java.lang.String, java.lang.String, java.lang.String, int)
	 */
	public IComponentResource createResource(String urnid, String codebase, String classname) {
		return new ComponentResource(urnid, codebase, classname);
	}
	
	public IComponentResource createResource(String urnid, String codebase, String classname, int version) {
			return new ComponentResource(urnid, codebase, classname, version);
	}
	
	/*public IComponentContext createContext(IComponentResource copRes) {
		return new ComponentContext(copRes);
	}*/

}
