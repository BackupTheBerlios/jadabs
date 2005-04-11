package ch.ethz.jadabs.sbbmidlet.ui;


import ch.ethz.jadabs.sbbmidlet.SBBMIDlet;
import ch.ethz.jadabs.sbbmidlet.communication.SoapTransformation;

import javax.microedition.lcdui.*;


/**
 * Displays details for the selected connection. All information
 * provided by the SBB server for one particular connection is
 * displayed on the screen.
 *
 * @author Franz Maier
 */

public class DetailsList extends List implements CommandListener {

    private SBBMIDlet midlet;
    private ResultFormList resultFormList;
    private Command backCmd;
    private Display display;

    /**
     * Create new form for query settings
     *
     * @param midlet    parent MIDlet where this form is integrated into
     * @param list      result form for overview object.
     * @param cmds      array with commands that can be triggered from this screen
     * @param soapTrans SoapTransformation which holds the values returned by the
     *                  user request.
     */

    public DetailsList(SBBMIDlet midlet, ResultFormList list, Command cmds[], SoapTransformation soapTrans) {
        super("Ausgewählte Verbindung:", List.IMPLICIT, new String[]{"Am  31.01.2005"}, null);
        resultFormList = list;
        this.midlet = midlet;
        backCmd = cmds[0];
        this.addCommand(backCmd);
        setCommandListener(this);

        String to = "";
        for (int i = 0; i < soapTrans.getTo().size(); i++) {
            if (i != 0) {
                to += " - ";
            }
            to += soapTrans.getTo().elementAt(i).toString();
        }

        String dep = "";
        for (int i = 0; i < soapTrans.getTimeDep().size(); i++) {
            if (i != 0) {
                dep += " - ";
            }
            dep += soapTrans.getTimeDep().elementAt(i).toString();
        }

        String arr = "";
        for (int i = 0; i < soapTrans.getTimeArr().size(); i++) {
            if (i != 0) {
                arr += " - ";
            }
            arr += soapTrans.getTimeArr().elementAt(i).toString();
        }

        String plat = "";
        for (int i = 0; i < soapTrans.getPlatform().size(); i++) {
            if (i % 2 == 0 && i != 0) {
                plat += "   ";
            }
            if (i != 0 && i % 2 != 0) {
                plat += "/";
            }
            plat += soapTrans.getPlatform().elementAt(i).toString();
        }

        String trav = "";
        for (int i = 0; i < soapTrans.getTravelWith().size(); i++) {
            if (i != 0) {
                trav += " - ";
            }
            trav += soapTrans.getTravelWith().elementAt(i).toString();
        }

        String comment = "";
        for (int i = 0; i < soapTrans.getComments().size(); i++) {
            if (i != 0) {
                comment += " - ";
            }
            comment += soapTrans.getComments().elementAt(i).toString();
        }

        this.append("Von: " + soapTrans.getFrom().elementAt(0).toString(), null);
        this.append("Nach: " + soapTrans.getTo().lastElement().toString(), null);
        this.append("Via: " + to, null);
        this.append("Ab: " + arr, null);
        this.append("An: " + dep, null);
        this.append("Dauer: " + soapTrans.getDetailsDuration(), null);
        this.append("Umst.: " + Integer.toString(soapTrans.getDetailsChanges()), null);
        this.append("Platform: " + plat, null);
        this.append("Reise mit: " + trav, null);
        this.append("Kommentar: " + comment, null);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCmd) {
            display = Display.getDisplay(midlet);
            display.setCurrent(resultFormList);
        }
    }
}
