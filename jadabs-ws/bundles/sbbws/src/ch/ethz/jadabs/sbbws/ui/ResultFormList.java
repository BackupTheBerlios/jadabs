package ch.ethz.jadabs.sbbws.ui;


import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.sbbws.com.SoapTransformation;
import ch.ethz.jadabs.sbbws.ksoap.TimetableOverview_kSOAP;
import ch.ethz.jadabs.sbbws.midlet.SBBMIDlet;


/**
 * ResultFormList shows the overview object returned by the SBB Server.
 * 4 connections are displayed on the screen. For each connection the
 * departure time, arrival time, duration and the number of changes is shown.
 *
 * @author Franz Maier
 */


public class ResultFormList extends List implements CommandListener 
{

    private static Logger LOG = Logger.getLogger("ResultFormList");
    
    private SBBMIDlet midlet;
    private DetailsList detailsList;
    private QueryForm query;
    private Display display;
    private SoapTransformation soap;

    private Command selectCmd;
    private Command backCmd;
    private Command sendCmd;
    private Command resultCmd;

    private String deptimes[] = new String[4];
    
    /**
     * Create new form for query settings
     *
     * @param midlet    parent MIDlet where this form is integrated into.
     * @param cmds      array with commands that can be triggered from this screen.
     * @param soapTrans holds the data returned by the SBB Server.
     */


    public ResultFormList(SBBMIDlet midlet, Command cmds[], SoapTransformation soapTrans) {
        super("Aktuelle Verbindungen:", List.IMPLICIT, new String[]{"Am  " + soapTrans.fDate}, null);
        resultCmd = cmds[2];
        sendCmd = cmds[3];
        soap = soapTrans;
        query = midlet.queryForm;
        this.midlet = midlet;

        // we have a problem with the soap instances, just store the dep times again
        deptimes[0] = soapTrans.getTimeDep().elementAt(0).toString();
        deptimes[1] = soapTrans.getTimeDep().elementAt(1).toString();
        deptimes[2] = soapTrans.getTimeDep().elementAt(2).toString();
        deptimes[3] = soapTrans.getTimeDep().elementAt(3).toString();
        
        this.append("Von: " + soapTrans.getFrom().elementAt(0).toString(), null);
        this.append("Nach: " + soapTrans.getTo().elementAt(0).toString(), null);
        this.append("", null);

        this.append("Ab: " + soapTrans.getTimeDep().elementAt(0).toString() +
                " An: " + soapTrans.getTimeArr().elementAt(0).toString(), null);
        this.append(" Dur: " + soapTrans.getDuration().elementAt(0).toString() +
                " U: " + soapTrans.getChanges().elementAt(0), null);

        this.append("Ab: " + soapTrans.getTimeDep().elementAt(1).toString() +
                " An: " + soapTrans.getTimeArr().elementAt(1).toString(), null);
        this.append(" Dur: " + soapTrans.getDuration().elementAt(1).toString() +
                " U: " + soapTrans.getChanges().elementAt(1), null);

        this.append("Ab: " + soapTrans.getTimeDep().elementAt(2).toString() +
                " An: " + soapTrans.getTimeArr().elementAt(2).toString(), null);
        this.append(" Dur: " + soapTrans.getDuration().elementAt(2).toString() +
                " U: " + soapTrans.getChanges().elementAt(2), null);

        this.append("Ab: " + soapTrans.getTimeDep().elementAt(3).toString() +
                " An: " + soapTrans.getTimeArr().elementAt(3).toString(), null);
        this.append(" Dur: " + soapTrans.getDuration().elementAt(3).toString() +
                " U: " + soapTrans.getChanges().elementAt(3), null);

        backCmd = new Command("Zurueck", Command.SCREEN, 1);
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
            int idx = 0;
            
            switch (getSelectedIndex()) {
            	case 4: idx = 0; break;
                case 5: idx = 0; break;
                case 6: idx = 1; break;
                case 7: idx = 1; break;
                case 8: idx = 2; break;
                case 9: idx = 2; break;
                case 10: idx = 3; break;
                case 11: idx = 3; break;
            default:
                break;
            }
            
            String soapMessage = soap.createSoapMessageFromQuery(soap.getFrom().elementAt(0).toString(),
                    soap.getTo().elementAt(0).toString(),
                    soap.fDate,
                    deptimes[idx].toString());
            
            midlet.sendSoapString(soapMessage);
//            Alert alert = new Alert("Information", "Ihre Anfrage wird an den SBB-Server gesendet!", null, AlertType.INFO);
//            alert.setTimeout(Alert.FOREVER);
//            display.setCurrent(alert, d);
            
            showResult();

        } else if (c == resultCmd) {
            showResult();
        }
    }
    
    private void showResult()
    {
        detailsList = new DetailsList(
                this.midlet, 
                this, 
                new Command[]{backCmd, selectCmd}, 
                soap);
        
        display = Display.getDisplay(midlet);
        display.setCurrent(detailsList);
    }
    
}
