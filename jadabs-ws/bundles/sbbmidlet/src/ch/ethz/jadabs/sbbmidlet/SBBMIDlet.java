package ch.ethz.jadabs.sbbmidlet;


import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;

import ch.ethz.jadabs.osgi.j2me.OSGiContainer;
import ch.ethz.jadabs.jxme.*;
import ch.ethz.jadabs.jxme.bt.BTActivator;
import ch.ethz.jadabs.sbbmidlet.ui.*;
import ch.ethz.jadabs.sbbmidlet.communication.SoapTransformation;


import org.apache.log4j.LogActivator;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.io.IOException;

/**
 * SBBMIDlet is the main class for the SBB application on the mobile phone.
 * It implements the BundleActivator class as well as Listeners to handle
 * user events and the incoming message from the SBB Server. For the
 * different kind of information presented to the user some forms and lists
 * are created initially.
 *
 * @author Franz Maier
 */

public class SBBMIDlet extends MIDlet implements BundleActivator, Listener, CommandListener {

    private SBBMIDlet instance;
    private SoapTransformation soapTransformation;
    private Display display;
    public QueryForm queryForm;
    private ResultFormList resultFormList;
    private DetailsList detailsList;
    private AmbiguousResultList ambiguousResultList;
    private ConfigurationForm configForm;


    private static Logger LOG;
    private ServiceReference fServiceReference;
    private EndpointService fEndptsvc;
    private EndpointAddress fEndpoint;

    public String fResponseFromSBBServer = "";

    /* commands */
    private Command sendCmd;
    private Command logCmd;
    private Command exitCmd;
    private Command selectCmd;
    private Command backCmd;
    private Command saveCmd;
    private Command resultCmd;
    private Command configCmd;


    /**
     * Constructor
     */
    public SBBMIDlet() {

        OSGiContainer osgicontainer = OSGiContainer.Instance();
        osgicontainer.setProperty("ch.ethz.jadabs.jxme.peeralias", this.getAppProperty("ch.ethz.jadabs.jxme.peeralias"));
        osgicontainer.setProperty("log4j.priority", this.getAppProperty("log4j.priority"));
        osgicontainer.setProperty("ch.ethz.jadabs.jxme.bt.rendezvouspeer", this.getAppProperty("ch.ethz.jadabs.jxme.bt.rendezvouspeer"));
        osgicontainer.startBundle(new LogActivator());
        osgicontainer.startBundle(new JxmeActivator());
        osgicontainer.startBundle(new BTActivator());
        osgicontainer.startBundle(this);

        instance = this;
        LOG = Logger.getLogger("SBBMIDlet");
    }

    public void start(BundleContext bundleContext) {

        // get Endpoint service
        fServiceReference = bundleContext.getServiceReference("ch.ethz.jadabs.jxme.EndpointService");
        fEndptsvc = (EndpointService) bundleContext.getService(fServiceReference);

        fEndptsvc.addListener("sbbmidlet", this);
        LOG.debug(fEndptsvc.getListener("sbbmidlet"));
    }

    public void stop(BundleContext bundleContext) {
    }

    /**
     * Handle starting the MIDlet
     */
    public void startApp() {

        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke startApp()");
        }

        soapTransformation = new SoapTransformation();
        display = Display.getDisplay(this);
        sendCmd = new Command("Senden", Command.SCREEN, 1);
        logCmd = new Command("Log", Command.SCREEN, 3);
        exitCmd = new Command("Beenden", Command.EXIT, 1);
        selectCmd = new Command("Auswahl", Command.ITEM, 2);
        backCmd = new Command("Zurueck", Command.SCREEN, 2);
        saveCmd = new Command("Speichern", Command.SCREEN, 1);
        resultCmd = new Command("Ergebnis", Command.SCREEN, 1);
        configCmd = new Command("Konfiguration", Command.SCREEN, 2);

        queryForm = new QueryForm(this, new Command[]{sendCmd, logCmd, exitCmd, resultCmd, configCmd});
        configForm = new ConfigurationForm(this, new Command[]{selectCmd, backCmd});

        display.setCurrent(queryForm);
        Logger.getLogCanvas().setDisplay(display);
        Logger.getLogCanvas().setPreviousScreen(queryForm);

    }

    /**
     * Handle pausing the MIDlet
     */
    public void pauseApp() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke pauseApp()");
        }
    }

    /**
     * Handle destroying the MIDlet
     */
    public void destroyApp(boolean unconditional) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke destroyApp()");
        }
    }

    /**
     * Quit the MIDlet
     */
    private void quitApp() {
        instance.destroyApp(true);
        instance.notifyDestroyed();
        instance = null;
        display = null;
    }


    /**
     * Handle user action
     *
     * @param c GUI command
     * @param d GUI display object that triggered the command
     */
    public void commandAction(Command c, Displayable d) {
        if (c == configCmd) {
            display.setCurrent(configForm);
        } else if (c == logCmd) {
            Logger.getLogCanvas().setPreviousScreen(d);
            display.setCurrent(Logger.getLogCanvas());
        } else if (c == resultCmd) {
            if (soapTransformation.isAmbiguous()) {
                ambiguousResultList = new AmbiguousResultList(this, new Command[]{selectCmd, backCmd}, soapTransformation);
                display.setCurrent(ambiguousResultList);
            } else {
                resultFormList = new ResultFormList(this, new Command[]{saveCmd, logCmd, resultCmd, sendCmd}, soapTransformation);
                display.setCurrent(resultFormList);
            }
        } else if (c == sendCmd) {
            soapTransformation.setDetailsQuery(false);
            String soapMessage = soapTransformation.
            	createSoapMessageFromQuery(
            	        queryForm.getFromField(), 
            	        queryForm.getToField(), 
            	        queryForm.getDateField(), 
            	        queryForm.getTimeField());
            sendMessage(soapMessage);
            Alert alert = new Alert("Information", "Ihre Anfrage wird an den SBB-Server gesendet!", null, AlertType.INFO);
            alert.setTimeout(Alert.FOREVER);
            display.setCurrent(alert, d);
        } else if (c == exitCmd) {
            quitApp();
        }
    }

    public void sendMessage(String message) {

        Element[] elms = new Element[1];
        elms[0] = new Element("SOAP_REQUEST_TAG", message, Element.TEXTUTF8_MIME_TYPE);
        try {
            fEndpoint = new EndpointAddress("btspp", "anybody", -1, "sbbservice", null);
            fEndptsvc.propagate(elms, fEndpoint);

        } catch (MalformedURIException mue) {
            LOG.debug(mue.getMessage());
        } catch (IOException ioe) {
            LOG.debug(ioe.getMessage());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("sending soap message (\"" + message + "\")");
        }
    }

    public void handleSearchResponse(NamedResource namedResource) {
    }

    public void handleMessage(Message msg, String str) {
        LOG.debug("Received soap response from SBB Server");
        fResponseFromSBBServer = new String(msg.getElement("SOAP_RESPONSE_TAG").getData());
        soapTransformation.handleDifferentSoapObjects(fResponseFromSBBServer);
    }
}
