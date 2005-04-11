/*
 * Created on Aug 4, 2004
 *
 * $Id: SettingsForm.java,v 1.1 2005/04/11 08:27:44 afrei Exp $
 */
package ch.ethz.jadabs.im.gui.handygui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

import org.apache.log4j.Logger;


/** 
 *  A screen where the SIP settings can be specified
 */
public class SettingsForm extends Form 
{
    private static Logger LOG;
    
    /** reference to the main MIDlet */
    private HandyguiMIDlet midlet;
    
    private TextField usernameField;  
    private TextField passwordField; 
    private TextField ipPortField; 
    private TextField registrarField; 
    

    
    /**
     * @param midlet parent MIDlet where this form is integrated into
     * @param cmds array with commands that can be triggered from this screen
     */
    public SettingsForm(HandyguiMIDlet midlet, Command cmds[]) 
    {
        super("Settings");
        this.midlet = midlet;
        this.setCommandListener(midlet);
        // add commands
        for (int i=0; i<cmds.length; i++) {
            this.addCommand(cmds[i]);
        }          
        
        LOG = Logger.getLogger("SettingsForm");
    }  
    
    public void initSettings(String username, String password, String ipPort, String registrar) 
	{
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke initSettings()");
        }
        // build-up form
        usernameField = new TextField("Username", username, 64,TextField.ANY); 
        this.append(usernameField);
        passwordField = new TextField("Password", password, 64,TextField.PASSWORD); 
        this.append(passwordField);
        ipPortField = new TextField("IP:Port", ipPort, 64,TextField.ANY); 
        this.append(ipPortField);
        registrarField = new TextField("Registrar", registrar, 64,TextField.ANY); 
        this.append(registrarField);	
	}
    
    
    
	/**
	 * @return Returns the username.
	 */
    public String getUsername() 
    {		
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke getUsername()");
        }
        return usernameField.getString();
	}
    
	/**
	 * @param username The username to set.
	 */
	public void setUsername(String username) 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke setUsername()");
        }
		this.usernameField.setString(username);
	}

	/**
	 * @return Returns the password.
	 */
	public String getPassword() 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke getPassword()");
        }
		return passwordField.getString();
	}
	
	/**
	 * @param password The password to set.
	 */
	public void setPassword(String password) 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke setPassword()");
        }
		this.passwordField.setString(password);
	}

	/**
	 * @return Returns the ipPort.
	 */
	public String getIPPort() 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke getIPPort()");
        }
		return ipPortField.getString();
	}

	/**
	 * @param port The ipPort to set.
	 */
	public void setIPPort(String ipPort) 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke setIPPort()");
        }
		this.ipPortField.setString(ipPort);
	}
	
	/**
	 * @return Returns the registrar.
	 */
	public String getRegistrar() 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke getRegistrar()");
        }
		return registrarField.getString();
	}

	/**
	 * @param registrar The registrar to set.
	 */
	public void setRegistrar(String registrar) 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke setRegistrar()");
        }
		this.registrarField.setString(registrar);
	}

}
