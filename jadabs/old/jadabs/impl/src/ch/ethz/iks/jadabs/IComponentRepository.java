/*
 * Created on Jul 8, 2003
 *
 * $Id: IComponentRepository.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.iks.jadabs;

import java.util.Hashtable;

/**
 * The IComponentRepository Interface shows the capabilities of the dynamic Component Repository (dCR).
 * Use this interface to query and take actions in the dCR.
 * 
 * @author andfrei
 */
public interface IComponentRepository {

	/**
	 * Insert an ComponentResource into the dCR.
	 * 
	 * @param extRes
	 */
	public void insert(IComponentResource copRes);

	/**
	 * Withdraw ComponentResource from the dCR.
	 * 
	 * @param copRes
	 */
	public void withdraw(IComponentResource copRes);

	/**
	 * Get ComponentResource by the component ID.
	 * 
	 * @param copID
	 * @return
	 */
	public IComponentResource getComponentResourceById(String copID);

	/**
	 * Get ComponentResource by the components main-classname.
	 * 
	 * @param classname
	 * @return
	 */
	public IComponentResource getComponentResourceByClassname(String classname);

	/**
	 * Get ComponentResource by the components codebase.
	 * 
	 * @param codebase
	 * @return
	 */
	public IComponentResource getComponentResourceByCodebase(String codebase);
		
	/**
	 * Return all the running components from the dCR
	 * @return
	 */
	public Hashtable getComponentResources();

}
