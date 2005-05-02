package ch.ethz.jadabs.sbbws.ui;


import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

import ch.ethz.jadabs.sbbws.midlet.SBBMIDlet;

/**
 * To configure which transport should be used.
 * Additionally some copyright info is displayed.
 *
 * @author Franz Maier
 */

public class ConfigurationForm extends Form implements CommandListener {

    private SBBMIDlet midlet;
    private Display display;
    private QueryForm query;
    private ChoiceGroup transportChoiceGroup;
    private Command saveCmd;
    private Command backCmd;

    public static int CONTYPE_JXME = 0;
    public static int CONTYPE_HTTP = 1;
    
    /**
     * @param midlet parent MIDlet where this form is integrated into
     * @param cmds   array with commands that can be triggered from this screen
     */

    public ConfigurationForm(SBBMIDlet midlet, Command cmds[]) {
        super("Konfiguration:");
        this.midlet = midlet;
        query = midlet.queryForm;
        backCmd = new Command("Zurueck", Command.SCREEN, 1);
        saveCmd = new Command("Speichern", Command.SCREEN, 1);
        String info = "Copyright ETH Zuerich,\n Andreas Frei, Stefan Vogt, Franz Maier,\n contact: frei@inf.ethz.ch";
        this.addCommand(backCmd);
        this.addCommand(saveCmd);
        setCommandListener(this);

        transportChoiceGroup = new ChoiceGroup("Verbindung Auswaehlen:", 
                ChoiceGroup.EXCLUSIVE, 
                new String[]{"JXME", "HTTP"}, 
                null);
        transportChoiceGroup.setSelectedIndex(1, true);
        this.append(transportChoiceGroup);
        this.append("                                                                                                        ");
        this.append(info);
    }

    private void save() {
        //not yet implemented!
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCmd) {
            display = Display.getDisplay(midlet);
            display.setCurrent(query);
        } else if (c == saveCmd) {
            display = Display.getDisplay(midlet);
            display.setCurrent(query);
            // save();
        }
    }
    
    /**
     * Compare connection Type against the CONTYPE_JXME, and CONTYPE_HTTP
     * @return
     */
    public int getConnectionType()
    {
        return transportChoiceGroup.getSelectedIndex(); 
    }
    
    public void setConnectionType(int type)
    {
        if (type == CONTYPE_JXME)
            transportChoiceGroup.setSelectedIndex(0, true);
        else
            transportChoiceGroup.setSelectedIndex(1, true);
    }
}
