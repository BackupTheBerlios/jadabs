/*
 * Created on Feb 20, 2004
 *
 */
package ch.ethz.iks.jadabs;

/**
 * @author andfrei
 *
 */
public interface IComponentContext
{

	/**
     * Returns a component registered with the given name.
     * 
	 * @param mainname - currently only support the mainclass name of a component
	 * @return
	 */
	Object getComponent(String mainname);
    
    Object getProperty(String name);
    
	public String getComponentVersion();
}
