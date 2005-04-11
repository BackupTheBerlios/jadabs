/*
 * Created on Aug 4, 2004
 *
 * $Id: NeighboursList.java,v 1.1 2005/04/11 08:27:44 afrei Exp $
 */
package ch.ethz.jadabs.im.gui.handygui;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.List;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.im.api.IMContact;


/**
 *  List of neighbours i.e. jxme peers
 */
public class NeighboursList extends List
{
    private static Logger LOG;
    
    /** reference to the main MIDlet */
    private HandyguiMIDlet midlet;
    
    /**
     */
    public NeighboursList(HandyguiMIDlet midlet, Command cmds[])
    {
        // text box for 200 characters of any type 
        super("Neighbours", Choice.IMPLICIT);
        this.midlet = midlet;
        this.setCommandListener(midlet);
        // add commands
        for (int i=0; i<cmds.length; i++) {
            this.addCommand(cmds[i]);
        }  
        
        LOG = Logger.getLogger("NeighboursList");  
    }
    
	public void initNeighbourList() 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke initNeighbourList()");
	    }
	    // workaround because this.deleteAll() gives compile error
	    int size = this.size();
	    for (int i = 0; i<size; i++) 
	    {
	        this.delete(i);
	    }
	    if (midlet.getImService() != null)
	    {
	        IMContact [] ct = midlet.getImService().getNeighbours();
			for (int i=0; i<ct.length; i++) {
			    this.append(ct[i].getUsername(),HandyguiMIDlet.BLUE);
			}    
	    }
	}    
}
