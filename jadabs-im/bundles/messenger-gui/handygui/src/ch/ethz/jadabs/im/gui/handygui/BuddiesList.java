/*
 * Created on Aug 4, 2004
 *
 * $Id: BuddiesList.java,v 1.1 2005/04/11 08:27:43 afrei Exp $
 */
package ch.ethz.jadabs.im.gui.handygui;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.List;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.im.api.IMContact;
import ch.ethz.jadabs.api.MessageCons;


/**
 *  List of Buddies (friends)
 *  Buddies can be jxme-peers or sip-peers
 */
public class BuddiesList extends List
{
    private static Logger LOG;
    
    /** reference to the main MIDlet */
    private HandyguiMIDlet midlet;
    
    public BuddiesList(HandyguiMIDlet midlet, Command cmds[])
    {
        // text box for 200 characters of any type 
        super("Buddies", Choice.IMPLICIT);
        this.midlet = midlet;
        this.setCommandListener(midlet);
        // add commands
        for (int i=0; i<cmds.length; i++) {
            this.addCommand(cmds[i]);
        }        
        
        LOG = Logger.getLogger("AboutScreen");
    }
    
	public void initBuddiesList() 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke initBuddyList()");
        }
	    // workaround because this.deleteAll() gives compile error
	    int size = this.size();
	    for (int i = 0; i<size; i++) 
	    {
	        this.delete(i);
	    }
		if (midlet.getImService() != null)
		{
		    IMContact [] ct = midlet.getImService().getBuddies();
			for (int i=0; i<ct.length; i++) {			
				if (ct[i].getStatus() == MessageCons.IM_STATUS_ONLINE) {
				    this.append(ct[i].getUsername(),HandyguiMIDlet.GREEN);  			    
				}
				else if (ct[i].getStatus() == MessageCons.IM_STATUS_BUSY) {
				    this.append(ct[i].getUsername(),HandyguiMIDlet.ORANGE); 
				}
				else if (ct[i].getStatus() == MessageCons.IM_STATUS_OFFLINE) {
				    this.append(ct[i].getUsername(),HandyguiMIDlet.RED);  
				}
				else if (ct[i].getStatus() == MessageCons.IM_STATUS_UNKNOWN) {
					// No Image !			   
				}
				else {
				    this.append(ct[i].getUsername(),HandyguiMIDlet.BLUE);  
				}
			}    
		}
	}    
}
