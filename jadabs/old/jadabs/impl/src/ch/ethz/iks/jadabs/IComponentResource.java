/*
 * Created on Sep 4, 2003
 *
 * $Id: IComponentResource.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.iks.jadabs;

/**
 * @author andfrei
 *
 */
public interface IComponentResource {
	/**
	 * Return the unique ID of the component.
	 * 
	 * @return String
	 */
	public String getCopID();
	/**
	 * Return the CodeBase of the extension, usually a .jar file.
	 * @return String
	 */
	public String getCodeBase();
	/**
	 * Return the className of the extension, the name of the aspect.
	 * 
	 * @return String
	 */
	public String getClassName();
	
	/**
	 * Return the version number of the component.
	 * 
	 * @return int
	 */
	public int getVersion();
	
    public void setExtResLocation(String res);
    
    /**
     * Return the Path where the resource in the filesystem is stored.
     * 
     * @return
     */
    public String getExtResLocation();
    
    /**
     * Return the instantiated component (service).
     * 
     * @return
     */
    public IComponent getExtObject();
    
	/**
	 * Add an Extension Resource Dependency, befor instantiating this extension all dependencies
	 * should alread be in the system.
	 * 
	 * @param extRes
	 */
	public void addCopResDependency(ComponentResource copRes);
	
	//---------------------------------------
    // Delegate methods for IComponent methods
    //---------------------------------------
	public void initComponent() throws InstantiationException, IllegalAccessException;
	
	public void startComponent(String[] args);
	
	public void stopComponent();
    
    public void disposeComponent();
	
    //---------------------------------------
    // Status methods
    //---------------------------------------
	/**
	 * Check if ComponentResource contains a service component which than 
	 * can be initialized and started.
	 *
	 */
	public boolean isService();
	
	/** 
	 * Checks if the component is initialized.
	 * 
	 * @return
	 */
	public boolean isInitialized();
	
	/**
	 * Checks if the component is already started.
	 * 
	 * @return
	 */
	public boolean isStarted();
    
    /**
     * Checks if the component has been stopped.
     * 
     * @return
     */
    public boolean isStopped();
    
}