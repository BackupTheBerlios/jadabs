/* 
 * Created on Aug 4, 2004
 * 
 * $Id: SmartMessengerMIDlet.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package ch.ethz.jadabs.smartmessenger;

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

import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.EndpointService;
import ch.ethz.jadabs.jxme.JxmeActivator;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.bt.BTActivator;
import ch.ethz.jadabs.mservices.smsgateway.SMSGatewayActivator;
import ch.ethz.jadabs.mservices.smsgateway.SMSGatewayService;
import ch.ethz.jadabs.mservices.smsservice.SMSServiceActivator;
import ch.ethz.jadabs.osgi.j2me.OSGiContainer;

/**
 * This is the MIDlet class of the SmartMessenger application.
 * 
 * @author Ren&eacute; M&uuml;ller
 * @version 1.0
 */
public class SmartMessengerMIDlet extends MIDlet 
                                  implements CommandListener, BundleActivator
{    
    /** Reference to Log4j window */
    private static Logger LOG;
    
    /** shared static variables */
    private SmartMessengerMIDlet instance;

    /** display of this midlet application */
    private Display display;   
    
    /** Reference to the settings form */
    private SettingsForm settingsForm;
    
    /** Refernee to the message field */
    private MessageScreen messageScreen;
    
    /** Reference to the end-point service */
    private EndpointService endptsvc;
    
    /** Reference to SMS Gateway instance */
    private SMSGatewayService smsgateway;
    
    /* commands */
    private Command messageCmd;
    private Command settingsCmd;
    private Command sendCmd;
    private Command logCmd;
    private Command exitCmd;        
    

    /** Constructor */
    public SmartMessengerMIDlet()
    {        
        // do some initialization stuff
        // this corresponds to the init.xargs descriptor from knopflerfish
        OSGiContainer osgicontainer = OSGiContainer.Instance();
        osgicontainer.setProperty("ch.ethz.jadabs.jxme.peeralias", this.getAppProperty("ch.ethz.jadabs.jxme.peeralias"));
        osgicontainer.setProperty("log4j.priority", this.getAppProperty("log4j.priority"));
        osgicontainer.setProperty("ch.ethz.jadabs.jxme.bt.rendezvouspeer", 
                                  this.getAppProperty("ch.ethz.jadabs.jxme.bt.rendezvouspeer"));
        osgicontainer.setProperty("ch.ethz.jadabs.mservices.smsgateway.emailsuffix", 
                this.getAppProperty("ch.ethz.jadabs.mservices.smsgateway.emailsuffix"));
        osgicontainer.setProperty("ch.ethz.jadabs.mservices.smsgateway.senderaddress", 
                this.getAppProperty("ch.ethz.jadabs.mservices.smsgateway.senderaddress"));        
        osgicontainer.startBundle(new LogActivator());
        osgicontainer.startBundle(new JxmeActivator());
        osgicontainer.startBundle(new BTActivator());        
        osgicontainer.startBundle(new SMSServiceActivator());
        osgicontainer.startBundle(new SMSGatewayActivator());
        
        osgicontainer.startBundle(this);
        instance = this;
        LOG = Logger.getLogger("SmartMessengerMIDlet");
    }

    /** Handle starting the MIDlet */
    public void startApp()
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke startApp()");
        }

        // obtain reference to Display singleton
        display = Display.getDisplay(this);        
        
        messageCmd = new Command("Message", Command.SCREEN, 1);
        settingsCmd = new Command("Settings", Command.SCREEN, 1);
        sendCmd = new Command("Send", Command.SCREEN, 2);
        logCmd = new Command("Log", Command.SCREEN, 3);
        exitCmd = new Command("Exit", Command.EXIT, 1);
        
        settingsForm = new SettingsForm(this, new Command[] {messageCmd, 
                	sendCmd, logCmd, exitCmd});
        messageScreen = new MessageScreen(this, new Command[] {settingsCmd,
                	sendCmd, logCmd, exitCmd});
        display.setCurrent(settingsForm);
        
        Logger.getLogCanvas().setDisplay(display);
        Logger.getLogCanvas().setPreviousScreen(settingsForm);        
    }

    /** Handle pausing the MIDlet */
    public void pauseApp()
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke pauseApp()");
        }
    }

    /** Handle destroying the MIDlet */
    public void destroyApp(boolean unconditional)
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke destroyApp()");
        }
    }

    /** Quit the MIDlet */
    private void quitApp()
    {
        instance.destroyApp(true);
        instance.notifyDestroyed();
        instance = null;
        display = null;
    }


    /**
     * Handle user action from SmartMessenger application
     * 
     * @param c
     *            GUI command 
     * @param d
     *            GUI display object that triggered the command
     */
    public void commandAction(Command c, Displayable d)
    {
        if (c == messageCmd) {
            display.setCurrent(messageScreen);
        } else if (c == settingsCmd) {
            display.setCurrent(settingsForm);
        } else if (c == logCmd) {
            Logger.getLogCanvas().setPreviousScreen(d);
            display.setCurrent(Logger.getLogCanvas());
        } else if (c == sendCmd) {            
            String phoneNumber = settingsForm.getPhoneNumber();
            Alert alert = new Alert("Information",
                        "Sending message to "+phoneNumber+".", null,
                        AlertType.WARNING);
            alert.setTimeout(Alert.FOREVER);                                
            display.setCurrent(alert, d);
            sendMessage(phoneNumber, messageScreen.getString());            
        } else if (c == exitCmd) {
            quitApp();
        }
    }
         
    
    public void sendMessage(String phoneNumber, String message) 
    {
        LOG.debug("going to send message.");
        Element[] elms = new Element[2];
        elms[0] = new Element("to", phoneNumber.getBytes(), 
                               null, Element.TEXTUTF8_MIME_TYPE);       
        elms[1] = new Element("body", message.getBytes(), 
                               null, Element.TEXTUTF8_MIME_TYPE);
        Message msg = new Message(elms);
        smsgateway.sendSM(msg);
    }
    
    
    /**
     * Called by the OSGi container when this bundle is started.
     * @param bc reference to context data of this bundle
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bc)
    {
        // get Endpoint service
        ServiceReference sref = bc.getServiceReference("ch.ethz.jadabs.jxme.EndpointService");
        endptsvc = (EndpointService)bc.getService(sref);
        
        // get SMS Gateway service
        sref = bc.getServiceReference("ch.ethz.jadabs.mservices.smsgateway.SMSGateway");
        smsgateway = (SMSGatewayService)bc.getService(sref);                
    }

    /**
     * Called by the OSGi container when this bundle is stopped.
     * @param bc reference to context data of this bundle
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bc) 
    {
        // remove chat communication servce from service registry 
//        endptsvc.removeListener("jxmechat");
    }
}