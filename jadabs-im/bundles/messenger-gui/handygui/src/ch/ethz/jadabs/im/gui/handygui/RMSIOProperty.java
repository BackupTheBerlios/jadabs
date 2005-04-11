package ch.ethz.jadabs.im.gui.handygui;

import java.util.Hashtable;
import org.apache.log4j.Logger;
import ch.ethz.jadabs.api.IOProperty;


/** 
 *  A Class for the I/O handling
 * 
 *  The IOProperty interface was introduced because on a handy reading and 
 *  writing to files is not possible and reading/writing data must be 
 *  treated differently than I/O-handling on a computer.
 * 
 *  All properties have to be specified in midlet.xml 
 *  The save(String property) ist not yet implemented.
 */
public class RMSIOProperty implements IOProperty 
{
    private static Logger LOG;
    
    /** reference to the main MIDlet */
    private HandyguiMIDlet midlet;
    
    private Hashtable properties;
    
    private String username;  
    private String password; 
    private String ipPort; 
    private String registrar;
    
    public RMSIOProperty(HandyguiMIDlet midlet) {
        this.midlet = midlet;
        properties = new Hashtable();
        LOG = Logger.getLogger("RMSIOProperty");  
    }
    
    public String getProperty(String key, String defaultValue) {
        if (properties.get(key) != null)
        {
            return (String) properties.get(key);
    	}
        else
        {
            return defaultValue;
        }
    }

    public void setProperty(String key, String value) 
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke setProperty()");
        }
        properties.remove(key);
		properties.put(key,value);
    }

    public void save(String property) 
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke save()");
        }
//		TODO write to file        
    }

    public void clear() 
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke clear()");
        }
        properties.clear();      
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IOProperty#load()
     */
    public void load() 
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke load()");
        }
        setUsername(midlet.getAppProperty("ch.ethz.jadabs.im.username"));
        setPassword(midlet.getAppProperty("ch.ethz.jadabs.im.password"));
        setIPPort(midlet.getAppProperty("ch.ethz.jadabs.im.ipport"));
        setRegistrar(midlet.getAppProperty("ch.ethz.jadabs.im.registrar"));   
    }    
    
    /**
	 * @return Returns the username.
	 */
    public String getUsername() 
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke getUsername()");
        }
		return this.username;
	}
    
	/**
	 * @param username The username to set.
	 */
	public void setUsername(String username) 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke setUsername()");
        }
		this.username = username;
		properties.remove("ch.ethz.jadabs.im.username");
		properties.put("ch.ethz.jadabs.im.username",username);
	}

	/**
	 * @return Returns the password.
	 */
	public String getPassword() 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke getPassword()");
        }
		return this.password;
	}
	
	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password) 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke setPassword()");
        }
		this.password = password;
		properties.remove("ch.ethz.jadabs.im.password");
		properties.put("ch.ethz.jadabs.im.password",password);
	}

	/**
	 * @return Returns the ipPort.
	 */
	public String getIPPort() 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke getIPPort()");
        }
		return this.ipPort;
	}

	/**
	 * @param port The ipPort to set.
	 */
	public void setIPPort(String ipPort) 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke setIPPort()");
        }
		this.ipPort = ipPort;
		properties.remove("ch.ethz.jadabs.im.ipport");
		properties.put("ch.ethz.jadabs.im.ipport",ipPort);
	}
	
	/**
	 * @return Returns the registrar.
	 */
	public String getRegistrar() 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke getRegistrar()");
        }
		return this.registrar;
	}

	/**
	 * @param registrar The registrar to set.
	 */
	public void setRegistrar(String registrar) 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke setRegistrar()");
        }
		this.registrar = registrar;
		properties.remove("ch.ethz.jadabs.im.registrar");
		properties.put("ch.ethz.jadabs.im.registrar",registrar);
	}

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.api.IOProperty#setPath(java.lang.String)
     */
    public void setPath(String path) {
//        TODO
        load();
    }



}
