/*
 * Created on Aug 4, 2004
 *
 * $Id: SelectReceiverList.java,v 1.1 2005/04/11 08:27:44 afrei Exp $
 */
package ch.ethz.jadabs.im.gui.handygui;

import java.util.Enumeration;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.List;

import org.apache.log4j.Logger;

/** 
 *  A screen where the user has to select a receiver for the next message
 */
public class SelectReceiverList extends List
{
    private static Logger LOG;
    
    /** reference to the main MIDlet */
    private HandyguiMIDlet midlet;
    
    private String phoneNumber;
    private String message;
    
    
    public SelectReceiverList(HandyguiMIDlet midlet, Command cmds[])
    {
          
        super("Select receiver", Choice.IMPLICIT);
        this.midlet = midlet;
        this.setCommandListener(midlet);
        // add commands
        for (int i=0; i<cmds.length; i++) {
            this.addCommand(cmds[i]);
        }  
        
        LOG = Logger.getLogger("SelectReceiverList");
    }
    
    public void initReceiverList() 
    {
        // workaround because this.deleteAll() gives compile error
	    int size = this.size();
	    for (int i = 0; i<size; i++) 
	    {
	        this.delete(i);
	    }
        for (Enumeration en = midlet.getPossibleReceivers().elements(); en.hasMoreElements();)
        {
            this.append((String) en.nextElement(), null);
        }
    }
    
}
