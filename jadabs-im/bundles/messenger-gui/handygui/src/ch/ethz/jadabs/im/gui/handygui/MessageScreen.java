/*
 * Created on Aug 4, 2004
 *
 * $Id: MessageScreen.java,v 1.1 2005/04/11 08:27:44 afrei Exp $
 */
package ch.ethz.jadabs.im.gui.handygui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

import org.apache.log4j.Logger;


/**
 * This screen contains a Text-Box where the user can enter
 * the chat message
 */
public class MessageScreen extends TextBox
{
    private static Logger LOG;
    
    /** reference to the main MIDlet */
    private HandyguiMIDlet midlet;
    
    
    public MessageScreen(HandyguiMIDlet midlet, Command cmds[])
    {
        /**
         * TODO set text limit
         */
        // text box for 200 characters of any type        
        super("Enter message", "", 200, TextField.ANY);
        this.midlet = midlet;
        this.setCommandListener(midlet);
        // add commands
        for (int i=0; i<cmds.length; i++) {
            this.addCommand(cmds[i]);
        }  
        
        LOG = Logger.getLogger("MessageScreen");
    }
    
    
}
