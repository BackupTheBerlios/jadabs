/*
 * Created on Feb 8, 2005
 *
 * $Id: MessageScreen.java,v 1.1 2005/02/17 17:29:16 printcap Exp $
 */
package ch.ethz.jadabs.jxme.microservices.testbundle;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;


/**
 * This screen contains a Text-Box where the user can enter
 * a message
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class MessageScreen extends TextBox
{
    /** reference to the main MIDlet */
    private TestBundleMIDlet midlet;
    
    /** Refernee to the message field */
    private MessageScreen messageScreen;
    
    /**
     * Create new message screen that is associated to the specified midlet
     * @param midlet parent MIDlet
     * @param cmds list of commands that should be visible from this screen
     */
    public MessageScreen(TestBundleMIDlet midlet, Command cmds[])
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
