package ch.ethz.jadabs.sbbws.midlet;


import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;


/**
 * Shows information to the user while sending
 * and receiving the message from the SBB server.
 *
 * @author Franz Maier
 */

public class InfoScreen extends TextBox {

    /**
     * reference to the main MIDlet
     */
    private SBBMIDlet midlet;

    /**
     * Create new message screen that is associated to the specified midlet
     *
     * @param midlet parent MIDlet
     * @param cmds   list of commands that should be visible from this screen
     */
    public InfoScreen(SBBMIDlet midlet, Command cmds[]) {
        super("Anfrage wird an SBB-Server versendet!", "", 200, TextField.ANY);
        this.midlet = midlet;
        this.setCommandListener(midlet);
        // add commands
        for (int i = 0; i < cmds.length; i++) {
            this.addCommand(cmds[i]);
        }
    }
}
