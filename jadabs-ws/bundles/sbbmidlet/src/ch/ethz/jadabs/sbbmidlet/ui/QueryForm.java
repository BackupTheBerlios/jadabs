package ch.ethz.jadabs.sbbmidlet.ui;


import ch.ethz.jadabs.sbbmidlet.SBBMIDlet;

import javax.microedition.lcdui.*;

/**
 * This form is initially shown to the user. The departure station,
 * arrival station, date and the time to travel can be inserted. Dependent
 * on the user input the user gets back a selection for the right stations,
 * or an overview object is returned by the SBB server.
 *
 * @author Franz Maier
 */

public class QueryForm extends Form {

    private SBBMIDlet midlet;
    private TextField fromField;
    private TextField toField;
    private TextField dateField;
    private TextField timeField;

    /**
     * Create new form for query settings
     *
     * @param midlet parent MIDlet where this form is integrated into
     * @param cmds   array with commands that can be triggered from this screen
     */
    public QueryForm(SBBMIDlet midlet, Command cmds[]) {
        super("SBB Verbindung suchen");
        this.midlet = midlet;
        this.setCommandListener(midlet);
        // add commands
        for (int i = 0; i < cmds.length; i++) {
            this.addCommand(cmds[i]);
        }

        // build-up form
        fromField = new TextField("Von:", "", 12, TextField.ANY);
        toField = new TextField("Nach:     ", "", 12, TextField.ANY);
        dateField = new TextField("Datum:", "", 12, TextField.ANY);
        timeField = new TextField("Zeit:", "", 12, TextField.ANY);

        dateField.setString("12.06.2005");
        timeField.setString("14.00");

        this.append(fromField);
        this.append(toField);
        this.append(dateField);
        this.append(timeField);
    }


    /**
     * Return the entered from field
     *
     * @return String representing the entered from location
     */
    public String getFromField() {
        return fromField.getString();

    }

    /**
     * Return the entered to field
     *
     * @return String representing the entered to location
     */
    public String getToField() {
        return toField.getString();
    }

    public void setFromField(String from) {
        fromField.setString(from);
    }

    public void setToField(String to) {
        toField.setString(to);
    }

    /**
     * Return the entered date field
     *
     * @return String representing the entered date
     */
    public String getDateField() {
        return dateField.getString();
    }

    /**
     * Return the entered time field
     *
     * @return String representing the entered time
     */
    public String getTimeField() {
        return timeField.getString();
    }

}
