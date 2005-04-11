package ch.ethz.jadabs.sbbmidlet.ui;


import ch.ethz.jadabs.sbbmidlet.*;
import ch.ethz.jadabs.sbbmidlet.communication.SoapTransformation;

import javax.microedition.lcdui.*;


/**
 * ResultFormList shows the overview object returned by the SBB Server.
 * 4 connections are displayed on the screen. For each connection the
 * departure time, arrival time, duration and the number of changes is shown.
 *
 * @author Franz Maier
 */


public class ResultFormList extends List implements CommandListener {

    private SBBMIDlet midlet;
    private DetailsList detailsList;
    private QueryForm query;
    private Display display;
    private SoapTransformation soap;

    private Command selectCmd;
    private Command backCmd;
    private Command sendCmd;
    private Command resultCmd;

    /**
     * Create new form for query settings
     *
     * @param midlet    parent MIDlet where this form is integrated into.
     * @param cmds      array with commands that can be triggered from this screen.
     * @param soapTrans holds the data returned by the SBB Server.
     */


    public ResultFormList(SBBMIDlet midlet, Command cmds[], SoapTransformation soapTrans) {
        super("Aktuelle Verbindungen:", List.IMPLICIT, new String[]{"Am  " + "31.01.2005"}, null);
        resultCmd = cmds[2];
        sendCmd = cmds[3];
        soap = soapTrans;
        query = midlet.queryForm;
        this.midlet = midlet;

        this.append("VON: " + soapTrans.getFrom().elementAt(0).toString(), null);
        this.append("Nach: " + soapTrans.getTo().elementAt(0).toString(), null);
        this.append("", null);

        this.append("Ab: " + soapTrans.getTimeDep().elementAt(0).toString() +
                " An: " + soapTrans.getTimeArr().elementAt(0).toString() +
                " Dur: " + soapTrans.getDuration().elementAt(0).toString() +
                " U: " + soapTrans.getChanges().elementAt(0), null);

        this.append("Ab: " + soapTrans.getTimeDep().elementAt(1).toString() +
                " An: " + soapTrans.getTimeArr().elementAt(1).toString() +
                " Dur: " + soapTrans.getDuration().elementAt(1).toString() +
                " U: " + soapTrans.getChanges().elementAt(1), null);

        this.append("Ab: " + soapTrans.getTimeDep().elementAt(2).toString() +
                " An: " + soapTrans.getTimeArr().elementAt(2).toString() +
                " Dur: " + soapTrans.getDuration().elementAt(2).toString() +
                " U: " + soapTrans.getChanges().elementAt(2), null);

        this.append("Ab: " + soapTrans.getTimeDep().elementAt(3).toString() +
                " An: " + soapTrans.getTimeArr().elementAt(3).toString() +
                " Dur: " + soapTrans.getDuration().elementAt(3).toString() +
                " U: " + soapTrans.getChanges().elementAt(3), null);

        backCmd = new Command("Zurück", Command.SCREEN, 1);
        selectCmd = new Command("Details", Command.SCREEN, 1);

        //this.addCommand(selectCmd);
        this.addCommand(sendCmd);
        this.addCommand(resultCmd);
        this.addCommand(backCmd);
        this.addCommand(cmds[0]);
        setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == backCmd) {
            display = Display.getDisplay(midlet);
            display.setCurrent(query);
        } else if (c == sendCmd) {
            soap.setDetailsQuery(true);
            String soapMessage = soap.createSoapMessageFromQuery(soap.getFrom().elementAt(0).toString(),
                    soap.getTo().elementAt(0).toString(),
                    "30.01.2005",
                    soap.getTimeDep().elementAt(getSelectedIndex() - 4).toString());

            midlet.sendMessage(soapMessage);
            Alert alert = new Alert("Information", "Ihre Anfrage wird an den SBB-Server gesendet!", null, AlertType.INFO);
            alert.setTimeout(Alert.FOREVER);
            display.setCurrent(alert, d);

        } else if (c == resultCmd) {
            detailsList = new DetailsList(this.midlet, this, new Command[]{backCmd, selectCmd}, soap);
            display = Display.getDisplay(midlet);
            display.setCurrent(detailsList);
        }
    }
}
