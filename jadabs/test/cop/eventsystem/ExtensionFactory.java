/*
 * Created on Jul 8, 2003
 *
 */
package ch.ethz.iks.cop.eventsystem;

import java.util.Hashtable;

import ch.ethz.iks.cop.IComponentRepository;

/**
 * @author andfrei
 */
public class ExtensionFactory {

	private static ExtensionFactory extfactory;

	private IComponentRepository extSvc;

	/**
	 * 
	 */
	public ExtensionFactory() {
		super();
		
		extfactory = this;
		
	}

	public static ExtensionFactory Instance(){
		
		if (extfactory == null)
			extfactory = new ExtensionFactory();

		return extfactory;

	}

	public IComponentRepository createExtensionService(Hashtable properties){
		
		// per default, initialize factory with Local ExtensionService
		extSvc = new ESComponentLoader();
		
		return extSvc;
	}

	public IComponentRepository getExtensionService(){
		
		return extSvc;
	}
}
