/*
 * Created on Aug 4, 2004
 *
 * $Id: AddBuddyForm.java,v 1.1 2005/04/11 08:27:43 afrei Exp $
 */
package ch.ethz.jadabs.im.gui.handygui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

import org.apache.log4j.Logger;

/**
 *  A from where you can input the name and the host of a new buddy
 */
public class AddBuddyForm extends Form 
{
    private static Logger LOG;
    
    /** reference to the main MIDlet */
    private HandyguiMIDlet midlet;    
    
    private TextField buddyNameField;  
    private TextField hostField; 
    
    public AddBuddyForm(HandyguiMIDlet midlet, Command cmds[]) 
    {
        super("Add Buddy");
        this.midlet = midlet;
        this.setCommandListener(midlet);
        // add commands
        for (int i=0; i<cmds.length; i++) {
            this.addCommand(cmds[i]);
        }
        
        LOG = Logger.getLogger("AddBuddyForm");  
        
        buddyNameField = new TextField("Buddyname", null, 64,TextField.ANY); 
        this.append(buddyNameField);
        hostField = new TextField("Host", null, 64,TextField.ANY); 
        this.append(hostField);	
        
    }  
    
    
    
    
	/**
	 * @return Returns the buddyname.
	 */
    public String getBuddyName() 
    {		
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke getBuddyName()");
        }
        return buddyNameField.getString();
	}
    
	/**
	 * @param buddyname The buddyname to set.
	 */
	public void setBuddyName(String buddyname) 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke setBuddyName()");
        }
		this.buddyNameField.setString(buddyname);
	}


    /**
     * @return Returns the host.
     */
    public String getHost() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke getUsername()");
        }
        return hostField.getString();
    }
    
    /**
     * @param host The host to set.
     */
    public void setHost(String host) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke setHost()");
        }
		this.hostField.setString(host);
    }
}
