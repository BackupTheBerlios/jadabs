package ch.ethz.iks.proxy.cop;


import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.iks.evolution.cop.UpgradeableComponentResource;
import ch.ethz.iks.jadabs.ComponentResource;


/**
 * This kind of component maintains its binaries in a FOLDER on the filesystem rather than a in JarFile.
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class ProxyComponentResource extends ComponentResource {

	private static Logger LOG = Logger.getLogger(ProxyComponentResource.class);
	
	
	protected ProxyComponentResource(String urnid, String codebase, String classname) {
		super(urnid, codebase, classname);
	}


	protected ProxyComponentResource(String urnid, String codebase, String classname, int version) {
		super(urnid, codebase, classname, version);
	}
	
	// have to override this method to prevent from searching jars
	public Vector getComponentDeps() { 
		return new Vector();
		//deps.add(this.getOriginalComponent().getCodeBase());
        //deps.add("evolution.jar");
	}
	
	//TODO replace by initClassLoader
	public void setClassLoader(ProxyLoader loader) {
		this.extClassLoader = loader;
	}
	
	protected void initClassLoader() {
		// TODO: init the proxyloader here instead of inside UpgradeableComponentResource
	}
	
	/**
	 * @param className
	 * @return true if the class with the given <code>className</code> belongs ot this component
	 */
	public boolean contains(String className) {
		boolean found = false;
		
		if (this.extClassLoader instanceof ProxyLoader) {
			found = ((ProxyLoader)this.extClassLoader).knownsClass(className);
		} else {
			int length = 0;
			LOG.info(" Searching for content of ComponentResource is NOT yet implemented! "+this.getCodeBase());
			return false; 
		}
		return found;
	}
	
	/**
	 * @param originalComponent the component that is hidden by this proxy
	 */
	public void setOriginalComponent(UpgradeableComponentResource originalComponent) {
		this.originalComponent = originalComponent;
	}

	/**
	 * @return the component that is hidden by this proxy
	 */
	public UpgradeableComponentResource getOriginalComponent() {
		return originalComponent;
	}

	private UpgradeableComponentResource originalComponent;
	

	/* (non-Javadoc)
	 * @see ch.ethz.iks.cop.IComponentResource#getClassName()
	 */
	public String getClassName() {
		return null; // Treat proxis as Library components, e.g. do not try to start them
	}

}
