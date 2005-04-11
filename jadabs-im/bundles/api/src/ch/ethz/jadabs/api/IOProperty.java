/*
 * Created on Jan 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.ethz.jadabs.api;

/**
 * @author jannethm
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface IOProperty 
{
    public void setPath(String path);
    
	public String getProperty(String property, String def);
	
	public void setProperty(String property, String value);
	
	public void save(String comment);
	
	public void clear();

	public void load();
}
