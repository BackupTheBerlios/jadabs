/*
 * Created on October 30, 2003
 *
 * $Id: IResourceFactory.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.iks.jadabs;

/**
 * Abstract factory that encapsulates the creation of implementations of <code>IComponentResource</code> objects.
 * Adds the flexibility to change the implementation of or the way the Component resources are created
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */ 
public interface IResourceFactory {
	
	public IComponentResource createResource(String urnid, String codebase, String classname);
	
	public IComponentResource createResource(String urnid, String codebase, String classname, int version);

	//public IComponentContext createContext(IComponentResource copRes);
}
