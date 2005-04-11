/*
 * Created on Aug 4, 2004
 *
 * $Id: HistoryForm.java,v 1.1 2005/04/11 08:27:43 afrei Exp $
 */
package ch.ethz.jadabs.im.gui.handygui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;

import org.apache.log4j.Logger;

/**
 *  Shows the messages history
 *  Format: 
 *  "<< " + phoneNumber + ": " + sent message + "\n"
 *  ">> " + phoneNumber + ": " + received message + "\n"
 */
public class HistoryForm extends Form 
{
    private static Logger LOG;
    
    /** reference to the main MIDlet */
    private HandyguiMIDlet midlet;    
    
    
    public HistoryForm(HandyguiMIDlet midlet, Command cmds[]) 
    {
        super("Messages History");
        this.midlet = midlet;
        this.setCommandListener(midlet);
        // add commands
        for (int i=0; i<cmds.length; i++) {
            this.addCommand(cmds[i]);
        }
        
        LOG = Logger.getLogger("HistoryForm"); 
    }  
}
