/*
 * Created on Feb 12, 2005
 *
 */
package ch.ethz.jadabs.servicemanager.micro.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.apache.log4j.LogActivator;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.ethz.jadabs.jxme.JxmeActivator;
import ch.ethz.jadabs.jxme.services.impl.ServiceActivator;
import ch.ethz.jadabs.osgi.j2me.OSGiContainer;
import ch.ethz.jadabs.servicemanager.micro.ServiceManagerActivator;
import ch.ethz.jadabs.servicemanager.micro.ServiceReferenceImpl;

/**
 * @author andfrei
 * 
 */
public class ServiceManagerMIDlet extends MIDlet 
	implements CommandListener, BundleActivator
{    
    /** Reference to Log4j window */
    private static Logger LOG;
    
    /** shared static variables */
    private static ServiceManagerMIDlet instance;

    /** Reference to the settings form */
    private SettingsForm settingsForm;
    
    private ServiceListView serviceListView;
    
    /** display of this midlet application */
    private Display display;   
    
    
    /* commands */
    private Command logCmd;
    private Command exitCmd;
    private Command messageCmd;
    private Command settingsCmd;
    private Command sendCmd;
    private Command detailCmd;
    private Command installCmd;
    
    private Command addCmd;
    private Command rmCmd;
       
    
    /** Constructor */
    public ServiceManagerMIDlet()
    {        
        // do some initialization stuff
        // this corresponds to the init.xargs descriptor from knopflerfish
        OSGiContainer osgicontainer = OSGiContainer.Instance();
        
        	// set context properties
        osgicontainer.setProperty("ch.ethz.jadabs.jxme.peeralias", 
                this.getAppProperty("ch.ethz.jadabs.jxme.peeralias"));
        osgicontainer.setProperty("log4j.priority", 
                this.getAppProperty("log4j.priority"));
        osgicontainer.setProperty("ch.ethz.jadabs.jxme.bt.rendezvouspeer",
                this.getAppProperty("ch.ethz.jadabs.jxme.bt.rendezvouspeer"));      
        
        	// install and start bundles
        osgicontainer.startBundle(new LogActivator());
        osgicontainer.startBundle(new JxmeActivator());
//      osgicontainer.startBundle(new BTActivator());   
        osgicontainer.startBundle(new ServiceActivator());
        osgicontainer.startBundle(new ServiceManagerActivator());     
        osgicontainer.startBundle(this);
        
        instance = this;
        
        LOG = Logger.getLogger("ServiceManagerMIDlet");
    }
    
    /*
     */
    protected void startApp() throws MIDletStateChangeException
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke startApp()");
        }

        // obtain reference to Display singleton
        display = Display.getDisplay(this);        
        
        logCmd = new Command("Log", Command.SCREEN, 1);
        exitCmd = new Command("Exit", Command.EXIT, 1);
        // Service List View commands
        detailCmd = new Command("Details", Command.ITEM, 1);
        installCmd = new Command("Install", Command.ITEM, 2);
        
        addCmd = new Command("Add", Command.ITEM, 3);
        rmCmd = new Command("Remove", Command.ITEM, 4);
        
//        settingsForm = new SettingsForm(this, new Command[] { 
//                	logCmd, exitCmd});
        
        serviceListView = new ServiceListView(this, new Command[]{
                logCmd, exitCmd, detailCmd, installCmd, addCmd, rmCmd});
        
        display.setCurrent(serviceListView);
        
        Logger.getLogCanvas().setDisplay(display);
        Logger.getLogCanvas().setPreviousScreen(serviceListView);   
    }

    /*
     */
    protected void pauseApp()
    {

    }

    /*
     */
    protected void destroyApp(boolean arg0) throws MIDletStateChangeException
    {

    }

    /**
     *
     */
    public void start(BundleContext bc)
    {
//        // get Endpoint service
//        ServiceReference sref = bc.getServiceReference("ch.ethz.jadabs.jxme.EndpointService");
//        endptsvc = (EndpointService)bc.getService(sref);
                      
    }

    /**
     * 
     */
    public void stop(BundleContext bc) 
    {
        
    }
    
    /** Quit the MIDlet */
    private void quitApp()
    {
        try
        {
            instance.destroyApp(true);
        } catch (MIDletStateChangeException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        instance.notifyDestroyed();
        instance = null;
        display = null;
    }
    
    public void sendMessage(String phoneNumber, String message) 
    {
//        LOG.debug("going to send message.");
//        Element[] elms = new Element[2];
//        elms[0] = new Element("to", phoneNumber.getBytes(), 
//                               null, Element.TEXTUTF8_MIME_TYPE);       
//        elms[1] = new Element("body", message.getBytes(), 
//                               null, Element.TEXTUTF8_MIME_TYPE);
//        Message msg = new Message(elms);
//        smsgateway.sendSM(msg);
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

        if (c == settingsCmd) 
        {
            display.setCurrent(settingsForm);
        } 
        else if (c == logCmd) 
        {
            Logger.getLogCanvas().setPreviousScreen(d);
            display.setCurrent(Logger.getLogCanvas());
        } 
        else if (c == detailCmd)
        {
            LOG.debug("show details");
        }
        else if (c == installCmd)
        {
            LOG.debug("install:" + serviceListView.getSelectedIndex());
        }
        else if (c == addCmd)
        {
            serviceListView.addService(new ServiceReferenceImpl(
                    "jadabs","testsvc","0.3.4"));
        }
        else if (c == rmCmd)
        {
            serviceListView.removeService(
                    serviceListView.getSelectedIndex());
        }
//        } 
//            else if (c == sendCmd) {            
//            String phoneNumber = settingsForm.getPhoneNumber();
//            Alert alert = new Alert("Information",
//                        "Sending message to "+phoneNumber+".", null,
//                        AlertType.WARNING);
//            alert.setTimeout(Alert.FOREVER);                                
//            display.setCurrent(alert, d);
//            sendMessage(phoneNumber, messageScreen.getString());            
        else if (c == exitCmd) 
        {
            quitApp();
        }
    }
}
