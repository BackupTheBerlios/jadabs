/*
 * Created on Aug 4, 2004
 *
 * $Id: AboutScreen.java,v 1.1 2005/04/11 08:27:44 afrei Exp $
 */
package ch.ethz.jadabs.im.gui.handygui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;

import org.apache.log4j.Logger;

/**
 *  Shows informations about the Jadabs IM Group
 */
public class AboutScreen extends Form 
{
    private static Logger LOG;
    
    /** reference to the main MIDlet */
    private HandyguiMIDlet midlet;    
    
    
    public AboutScreen(HandyguiMIDlet midlet, Command cmds[]) 
    {
        super("About IM Messenger");
        this.midlet = midlet;
        this.setCommandListener(midlet);
        // add commands
        for (int i=0; i<cmds.length; i++) {
            this.addCommand(cmds[i]);
        }
        
        LOG = Logger.getLogger("AboutScreen");
        
        try {
            // load the logo image
            Image logo = Image.createImage("/res/logo_jadabs.png"); 
            this.append(logo);
        } catch (java.io.IOException err) {
            LOG.debug("failed loading image logo_jadabs.png");
        }    
        
        this.append("Jadabs\n" + "Instant Messenger\n\n" + "http://wlab.ethz.ch/jadabs-im\n\n"
				+ "Francois Terrier\n" + "Jean-Luc Geering\n" + "Janneth Malibago\n"
				+ "Supervisor: Andreas Frei\n");    
    }  
}
