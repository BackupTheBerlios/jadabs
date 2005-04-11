package ch.ethz.jadabs.sbbmidlet;


import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;


/**
 * Date: 13.12.2004 22:11:16
 * To change this template use Options | File Templates.
 *
 * @author Franz Maier
 */

public class Screen extends TextBox {

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
    public Screen(SBBMIDlet midlet, Command cmds[]) {
        // text box for 200 characters of any type
        super("Details anzeigen", "", 200, TextField.ANY);
        this.midlet = midlet;
        this.setCommandListener(midlet);
        // add commands
        for (int i = 0; i < cmds.length; i++) {
            this.addCommand(cmds[i]);
        }
    }


}
