package ch.ethz.jadabs.sbbmidlet.ui;


import ch.ethz.jadabs.sbbmidlet.SBBMIDlet;
import ch.ethz.jadabs.sbbmidlet.communication.SoapTransformation;

import javax.microedition.lcdui.*;


/**
 * This form displays the list of possible "from" and "to" stations.
 * If the user is no sure about the exact name of the "from" or "to"
 * station, he can select the proper values from the given list. 
 *
 * @author Franz Maier
 */

public class AmbiguousResultList extends Form implements CommandListener {

    private SBBMIDlet midlet;
    private Display display;
    private QueryForm query;
    private ChoiceGroup fromChoiceGroup;
    private ChoiceGroup toChoiceGroup;

    private Command saveCmd;
    private Command backCmd;

    /**
     * Shows ambiguous from or to stations. The user has to select a unique from
     * and to station which will be saved to the query form.
     *
     * @param midlet    parent MIDlet where this form is integrated into
     * @param cmds      array with commands that can be triggered from this screen
     * @param soapTrans holds the values to be displayed on screen
     */

    public AmbiguousResultList(SBBMIDlet midlet, Command cmds[], SoapTransformation soapTrans) {
        super("Eindeutige Bhf.-Auswahl!");
        this.midlet = midlet;
        query = midlet.queryForm;
        backCmd = new Command("Zurück", Command.SCREEN, 1);
        saveCmd = new Command("Speichern", Command.SCREEN, 1);

        fromChoiceGroup = new ChoiceGroup("VON:", ChoiceGroup.EXCLUSIVE, new String[]{"first"}, null);
        toChoiceGroup = new ChoiceGroup("NACH:", ChoiceGroup.EXCLUSIVE, new String[]{"second"}, null);
        fromChoiceGroup.delete(0);
        toChoiceGroup.delete(0);

        for (int i = 0; i < soapTrans.getFrom().size(); i++) {
            fromChoiceGroup.append(soapTrans.getFrom().elementAt(i).toString(), null);
        }
        for (int i = 0; i < soapTrans.getTo().size(); i++) {
            toChoiceGroup.append(soapTrans.getTo().elementAt(i).toString(), null);
        }

        this.append(fromChoiceGroup);
        this.append(toChoiceGroup);
        this.addCommand(saveCmd);
        setCommandListener(this);
    }

    private void save() {
        //to be implemented!
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCmd) {
            display = Display.getDisplay(midlet);
            display.setCurrent(query);
        } else if (c == saveCmd) {
            midlet.queryForm.setFromField(fromChoiceGroup.getString(fromChoiceGroup.getSelectedIndex()));
            midlet.queryForm.setToField(toChoiceGroup.getString(toChoiceGroup.getSelectedIndex()));
            display = Display.getDisplay(midlet);
            display.setCurrent(query);
            // save();
        }
    }
}
