package ch.ethz.jadabs.sbbmidlet.ui;


import ch.ethz.jadabs.sbbmidlet.SBBMIDlet;

import javax.microedition.lcdui.*;

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

    /**
     * @param midlet parent MIDlet where this form is integrated into
     * @param cmds   array with commands that can be triggered from this screen
     */

    public ConfigurationForm(SBBMIDlet midlet, Command cmds[]) {
        super("Konfiguration:");
        this.midlet = midlet;
        query = midlet.queryForm;
        backCmd = new Command("Zurück", Command.SCREEN, 1);
        saveCmd = new Command("Speichern", Command.SCREEN, 1);
        String info = "Copyright ETH Zürich, Wireless-Lab, Andreas Frei, Stefan Vogt, Franz Maier, contact: frei@inf.ethz.ch";
        this.addCommand(backCmd);
        this.addCommand(saveCmd);
        setCommandListener(this);

        transportChoiceGroup = new ChoiceGroup("Verbindung Auswählen:", ChoiceGroup.EXCLUSIVE, new String[]{"BT", "HTTP"}, null);
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
}
