/*
 * Created on Feb 12, 2005
 *
 */
package ch.ethz.jadabs.servicemanager.micro.ui;

import java.io.IOException;

import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.Datagram;
import javax.microedition.io.DatagramConnection;
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
import ch.ethz.jadabs.jxme.bt.BTActivator;
import ch.ethz.jadabs.jxme.microservices.MicroGroupServiceCoreActivator;
import ch.ethz.jadabs.jxme.services.impl.ServiceActivator;
import ch.ethz.jadabs.jxme.tcp.cldc.TCPActivator;
import ch.ethz.jadabs.osgi.j2me.OSGiContainer;
import ch.ethz.jadabs.servicemanager.ServiceAdvertisementListener;
import ch.ethz.jadabs.servicemanager.ServiceManager;
import ch.ethz.jadabs.servicemanager.ServiceReference;
import ch.ethz.jadabs.servicemanager.micro.ServiceManagerActivator;

/**
 * @author andfrei
 * 
 */
public class ServiceManagerMIDlet extends MIDlet 
	implements CommandListener, BundleActivator, 
		ServiceAdvertisementListener
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
    
    private ServiceManager serviceManager;
    
    /* commands */
    private Command logCmd;
    private Command exitCmd;
    private Command messageCmd;
    private Command settingsCmd;
    private Command sendCmd;
    private Command detailCmd;
    private Command installCmd;
    private Command startCmd;
          
    
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
        osgicontainer.setProperty("ch.ethz.jadabs.jxme.tcp.port", 
                this.getAppProperty("ch.ethz.jadabs.jxme.tcp.port"));
        osgicontainer.setProperty("ch.ethz.jadabs.jxme.seedURIs", 
                this.getAppProperty("ch.ethz.jadabs.jxme.seedURIs"));
        
        	// install and start bundles
        osgicontainer.startBundle(new LogActivator());
        osgicontainer.startBundle(new JxmeActivator());
        
        // use Bluetooth
        osgicontainer.startBundle(new BTActivator());   
        // use TCP, for simulation
//        osgicontainer.startBundle(new TCPActivator());
        
        osgicontainer.startBundle(new ServiceActivator());
                
        // startup MicroGroupService for local loop-back
        osgicontainer.startBundle(new MicroGroupServiceCoreActivator());
        
        // startup remote JXME
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

        startCmd = new Command("Start", Command.ITEM, 3);
        
//        settingsForm = new SettingsForm(this, new Command[] { 
//                	logCmd, exitCmd});
        
        serviceListView = new ServiceListView(this, new Command[]{
                logCmd, exitCmd, detailCmd, installCmd, startCmd});
        
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
        // get ServiceManager service
        org.osgi.framework.ServiceReference sref = bc.getServiceReference("ch.ethz.jadabs.servicemanager.ServiceManager");
        serviceManager = (ServiceManager)bc.getService(sref);
        
        serviceManager.getServiceAdvertisements(ServiceManager.ANYPEER,
                null, this);
        
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
            ServiceReference sref = serviceListView.getSelectedServiceReference();
            
            LOG.debug("install:" + sref.getDownloadURL());
            
            installOTAService(sref);
        }
        else if (c == startCmd)
        {
            
            ServiceReference sref = serviceListView.getSelectedServiceReference();
            
            LOG.debug("start:" + sref.getName());
            
            startOTAService(sref);
        }
//        else if (c == addCmd)
//        {
//            serviceListView.addService(new ServiceReferenceImpl(
//                    "jadabs","testsvc","0.3.4"));
//        }
//        else if (c == rmCmd)
//        {
//            serviceListView.removeService(
//                    serviceListView.getSelectedIndex());
//        }
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
    
    private void installOTAService(ServiceReference sref)
    {
        String url = sref.getDownloadURL();
        
        try{
            this.platformRequest(url);
        } catch (ConnectionNotFoundException e) {
            LOG.error("could not install: "+url);               
        }
    }
    
    private void startOTAService(ServiceReference sref)
    {
        String port = sref.getProperty("midp-port");
        String localURL = "datagram://127.0.0.1:"+port;
       
        
        DatagramConnection conn = null;
        try {
            conn = (DatagramConnection)Connector.open(localURL);
            byte[] data = "Wakeup Polly!".getBytes();
            Datagram dgrm = conn.newDatagram(data, data.length);
            conn.send(dgrm);
        } catch (IOException e) {
            LOG.error("cannot open datagram socket in server mode: "+e);
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (Exception e) { }
            }
        } 
    }
    
    public void foundService(ch.ethz.jadabs.servicemanager.ServiceReference sref)
    {
       serviceListView.foundService(sref);
    }
  
    
    public void removedService(ch.ethz.jadabs.servicemanager.ServiceReference sref)
    {
        serviceListView.removedService(sref);
    }

}
