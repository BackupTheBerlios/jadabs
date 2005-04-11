/*
 * Created on Aug 4, 2004
 *
 * $Id: RemoveBuddyForm.java,v 1.1 2005/04/11 08:27:43 afrei Exp $
 */
package ch.ethz.jadabs.im.gui.handygui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;

import org.apache.log4j.Logger;

/** 
 *  A screen where the user is asked if he really want to remove the selected buddy
 */
public class RemoveBuddyForm extends Form 
{
    private static Logger LOG;
    
    /** reference to the main MIDlet */
    private HandyguiMIDlet midlet;  
    
    private StringItem buddyNameField;  
    
    /**
    */
    public RemoveBuddyForm(HandyguiMIDlet midlet, Command cmds[]) 
    {
        super("Remove Buddy");
        this.midlet = midlet;
        this.setCommandListener(midlet);
        // add commands
        for (int i=0; i<cmds.length; i++) {
            this.addCommand(cmds[i]);
        }
        
        LOG = Logger.getLogger("RemoveBuddyForm");  
        
        this.append("Do you really want to remove");
        buddyNameField = new StringItem(null,null); 
        this.append(buddyNameField);
        this.append("?");
               
    }

    /**
     * @param string The Name of the buddy which should be removed
     */
    public void setBuddyName(String buddyName) {
        this.buddyNameField.setText(buddyName);
    }  
    
   
}
