/*
 * Created on Aug 4, 2004
 *
 * $Id: MessageScreen.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package ch.ethz.jadabs.smartmessenger;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;


/**
 * This screen contains a Text-Box where the user can enter
 * the chat message
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class MessageScreen extends TextBox
{
    /** reference to the main MIDlet */
    private SmartMessengerMIDlet midlet;
    
    /**
     * Create new message screen that is associated to the specified midlet
     * @param midlet parent MIDlet
     * @param cmds list of commands that should be visible from this screen
     */
    public MessageScreen(SmartMessengerMIDlet midlet, Command cmds[])
    {
        // text box for 200 characters of any type 
        super("Enter message", "", 200, TextField.ANY);
        this.midlet = midlet;
        this.setCommandListener(midlet);
        // add commands
        for (int i=0; i<cmds.length; i++) {
            this.addCommand(cmds[i]);
        }               
    }
    
}
