/*
 * Created on Sep 3, 2003
 *
 * $Id: ComponentManager.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.iks.jadabs;

import ch.ethz.iks.logger.ILogger;
import ch.ethz.iks.logger.Logger;

/**
 * The ComponentManager (CM) controls the initial Local Component Loader and the dynamic Component Repository (dCR).
 * Therfore the Component Manager is a singleton.
 * 
 * To use the dCR create use the ComponentManager.Instance() method. The dCR can also be used independent of 
 * the Component Manager, but the Local Component Loader will then not be initialized and an own loader has to
 * be supported.
 * 
 * To insert a component do following steps:
 * 1. ComponentManager.Instance(); // will also start the Local Component Loader
 * 2. ComponentRepository.Instance().insert(componentResource); // initializes the component.
 * 
 * @author andfrei
 *
 */
public class ComponentManager {

	private static ILogger LOG = Logger.getLogger(ComponentManager.class);
	
	/** REPOSITORY_INTERVAL is the timeinterval of the Local Repository Checker Thread to
	 * check changes in the repository.
	 */
	private static final int REPOSITORY_INTERVAL = 5000;
	
	private static ComponentManager copmgr; // Component Manager is a singleton
	
	protected ComponentRepository dCopRep;
	
	protected LocalComponentLoader lcoploader;
	
	private ComponentManager()
    {	
		// init dynamic Component Repository
		dCopRep = ComponentRepository.Instance();	
	}

	public static ComponentManager Instance(){
		
		if (copmgr == null)
			copmgr = new ComponentManager();
			
		return copmgr;
	}
	
	public boolean startLocalComponentLoader(){
		
        lcoploader = new LocalComponentLoader((String)Jadabs.Instance().getProperty(Jadabs.PCOPREP));
        
        boolean loaderstarted;
        
		// init local component loader
		if (Jadabs.Instance().getProperties().containsKey(Jadabs.REPOSITORY_LOAD_ONCE))
            loaderstarted = lcoploader.startLoader(0);
		else
            loaderstarted = lcoploader.startLoader(REPOSITORY_INTERVAL);
        
        return loaderstarted;
	}

	/**
	 * Returns the LocalComponentLoader.
	 * 
	 * TODO: change this to return an interface ComponentLoader, ComponentLoader can be itself a component
	 * 
	 * @return
	 */
	public LocalComponentLoader getComponentLoader(){
		return lcoploader;
	}

	public void stop(){
		
		// stop the local loader to disable the startup of new components
		lcoploader.stopLoader();
		
		// stop all components
		dCopRep.withdrawAll();
	}

}
