package ch.ethz.jadabs.sbbws.midlet;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import org.apache.log4j.LogActivator;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.microservices.MicroElement;
import ch.ethz.jadabs.jxme.microservices.MicroGroupServiceBundleActivator;
import ch.ethz.jadabs.jxme.microservices.MicroGroupServiceBundleImpl;
import ch.ethz.jadabs.jxme.microservices.MicroListener;
import ch.ethz.jadabs.jxme.microservices.MicroMessage;
import ch.ethz.jadabs.osgi.j2me.OSGiContainer;
import ch.ethz.jadabs.sbbws.com.SoapTransformation;
import ch.ethz.jadabs.sbbws.ui.AmbiguousResultList;
import ch.ethz.jadabs.sbbws.ui.ConfigurationForm;
import ch.ethz.jadabs.sbbws.ui.DetailsList;
import ch.ethz.jadabs.sbbws.ui.QueryForm;
import ch.ethz.jadabs.sbbws.ui.ResultFormList;

/**
 * SBBMIDlet is the main class for the SBB application on the mobile phone.
 * It implements the BundleActivator class as well as Listeners to handle
 * user events and the incoming message from the SBB Server. For the
 * different kind of information presented to the user some forms and lists
 * are created initially.
 *
 * @author Franz Maier
 */

public class SBBMIDlet extends MIDlet implements BundleActivator, MicroListener, CommandListener {

    private static Logger LOG = Logger.getLogger("SBBMIDlet");
    
    private SBBMIDlet instance;
    private SoapTransformation soapTransformation;
    private Display display;
    public QueryForm queryForm;
    private ResultFormList resultFormList;
    private DetailsList detailsList;
    private AmbiguousResultList ambiguousResultList;
    private ConfigurationForm configForm;
    
//    protected static final String SBB_WEBSERVICE_URL = "http://iknlab8.inf.ethz.ch:8081/axis/services/SBBWebService";
    protected static final String SBB_WEBSERVICE_URL = "http://wlab.ethz.ch:8080/axis/services/SBBWebService";
    protected static final String PIPE_NAME = "localpipe";
    
    
    private ServiceReference fServiceReference;
//    private EndpointService fEndptsvc;
//    private EndpointAddress fEndpoint;

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
    
    /** MicroGroupService service bundle actiavtor */
    private MicroGroupServiceBundleImpl groupService;

    private String pipeId;

    private String opipe = null;
    
    /**
     * Constructor
     */
    public SBBMIDlet() {

        OSGiContainer osgicontainer = OSGiContainer.Instance();
        osgicontainer.setProperty("ch.ethz.jadabs.jxme.peeralias", this.getAppProperty("ch.ethz.jadabs.jxme.peeralias"));
        osgicontainer.setProperty("log4j.priority", this.getAppProperty("log4j.priority"));
        osgicontainer.setProperty("ch.ethz.jadabs.microservices.bundleport", 
                this.getAppProperty("ch.ethz.jadabs.microservices.bundleport"));
        //        osgicontainer.setProperty("ch.ethz.jadabs.jxme.bt.rendezvouspeer", this.getAppProperty("ch.ethz.jadabs.jxme.bt.rendezvouspeer"));
        osgicontainer.startBundle(new LogActivator());
//        osgicontainer.startBundle(new JxmeActivator());
//        osgicontainer.startBundle(new BTActivator());
        
        // uncomment for local loopback 
        // start bundle part of MicroGroupService 
//        MicroGroupServiceBundleActivator mgsActivator = new MicroGroupServiceBundleActivator();        
//        osgicontainer.startBundle(mgsActivator);
//        groupService = mgsActivator.getService();   
//        groupService.wakeupCore(); 
                
        osgicontainer.startBundle(this);

        instance = this;
        LOG = Logger.getLogger("SBBMIDlet");
    }

    public void start(BundleContext bundleContext)
    {

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

        // uncomment for local loopback
//        try {
//            LOG.debug("do localpipe search");
//            
//	        // create pipe to communicate with core
//	        String ns[] = groupService.localSearch(NamedResource.PIPE, "Name", PIPE_NAME, 1 );
//	        
//	        if (ns.length > 0)
//	        {
//	            pipeId = ns[0];
//	            LOG.debug("found: "+pipeId);
////		        pipeId = groupService.create(NamedResource.PIPE, PIPE_NAME, "urn:jxta:uuid-0002:0001:04", Pipe.PROPAGATE);
////		        groupService.publish(NamedResource.PIPE, PIPE_NAME, pipeId);
//	                
//		        groupService.listen(pipeId, this);
//	        }
//	        else
//	            LOG.debug("no local-loopback");
//	        
//        } catch(IOException e) {
//           LOG.error("Error while registering listener to pipe '"+pipeId+"': "+e.getMessage());
//        }
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
            showResult();
        } else if (c == sendCmd) {
            soapTransformation.setDetailsQuery(false);
            String soapMessage = soapTransformation.
            	createSoapMessageFromQuery(
            	        queryForm.getFromField(), 
            	        queryForm.getToField(), 
            	        queryForm.getDateField(), 
            	        queryForm.getTimeField());
            
            sendSoapString(soapMessage);
            
//            Alert alert = new Alert("Information", "Ihre Anfrage wird an den SBB-Server gesendet!", null, AlertType.INFO);
//            alert.setTimeout(Alert.FOREVER);
//            display.setCurrent(alert, d);
            
            showResult();
            
        } else if (c == exitCmd) {
            quitApp();
        }
    }

    private void showResult()
    {
        if (soapTransformation.isAmbiguous()) {
            ambiguousResultList = new AmbiguousResultList(this, new Command[]{selectCmd, backCmd}, soapTransformation);
            display.setCurrent(ambiguousResultList);
        } else {
            resultFormList = new ResultFormList(this, new Command[]{saveCmd, logCmd, resultCmd, sendCmd}, soapTransformation);
            display.setCurrent(resultFormList);
        }
    }
    
    public void sendSoapString(String soapMsg)
    {
        if (configForm.getConnectionType() == ConfigurationForm.CONTYPE_HTTP)
            sendHttpSoap(soapMsg);
        else       
            sendJxmeMessage(soapMsg);
    }
    
    private void sendHttpSoap(String message)
    {
        
        try {
            byte[] soapMessage = message.getBytes();
            
            HttpConnection con = (HttpConnection)Connector.open(SBB_WEBSERVICE_URL);
            con.setRequestMethod(HttpConnection.POST);
            
            con.setRequestProperty("User-Agent", "ETHZ Jadabs SBBMIDlet for kSOAP/Axis");
            con.setRequestProperty("SOAPAction", "");
            con.setRequestProperty("Content-Type", "text/xml");
            con.setRequestProperty("Connection", "close");
            con.setRequestProperty("Content-Length", "" + soapMessage.length);
        
            OutputStream outputStream = con.openOutputStream();
            outputStream.write(soapMessage, 0, soapMessage.length);
            outputStream.close();
            
            
            InputStream is = con.openInputStream();
            
            soapTransformation.handleSoapObject(is);
                        
            is.close();
            con.close();
            
            LOG.debug("Received soap response from SBB Server");
            
   
        } catch(IOException ioe)
        {
            
        }
        
    }
    
    private void sendJxmeMessage(String message) 
    {        
        MicroElement elms[];
        if (opipe == null)
        	elms = new MicroElement[1];
        else
        {
            elms = new MicroElement[2];
            elms[1] = new MicroElement("OPIPE_TAG", opipe, "jxta");
        }
        
        elms[0] = new MicroElement("SOAP_REQUEST_TAG", message, "jxta");
        MicroMessage mmsg = new MicroMessage(elms);
        
        LOG.debug("sending JXTA message over pipe.");
        try {
            groupService.send(pipeId, mmsg);
        } catch(IOException e) {
            LOG.error("Error while sending JXTA message: "+e.getMessage());
        }
        
    }

    public void handleSearchResponse(NamedResource namedResource) {
    }

    public void handleMessage(MicroMessage message, String listenerId)
    {
        LOG.debug("incoming message \""+message.toXMLString()+"\", listenerId="+listenerId);                    
    
        String type = new String(message.getElement("type").getData());
        
        if (type.equals("reg"))
        {
            configForm.setConnectionType(ConfigurationForm.CONTYPE_JXME);
            opipe = new String(message.getElement("opipe").getData());
            
            
        } else if (type.equals("unreg"))
        {
            configForm.setConnectionType(ConfigurationForm.CONTYPE_HTTP);
            opipe = null;
        }
        
        
//        LOG.debug("Received soap response from SBB Server");
//        fResponseFromSBBServer = new String(msg.getElement("SOAP_RESPONSE_TAG").getData());
//        soapTransformation.handleSoapObject(fResponseFromSBBServer);
    }	

}
