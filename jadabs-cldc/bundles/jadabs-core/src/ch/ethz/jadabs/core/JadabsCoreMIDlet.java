/* 
 * Created on Dec 9th, 2004
 * 
 * $Id: JadabsCoreMIDlet.java,v 1.4 2005/02/18 21:12:30 printcap Exp $
 */
package ch.ethz.jadabs.core;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import org.apache.log4j.LogActivator;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.ethz.jadabs.jxme.JxmeActivator;
import ch.ethz.jadabs.jxme.microservices.MicroGroupServiceCoreActivator;
import ch.ethz.jadabs.jxme.microservices.MicroGroupServiceCoreImpl;
import ch.ethz.jadabs.jxme.services.impl.ServiceActivator;
import ch.ethz.jadabs.jxme.tcp.cldc.TCPActivator;
import ch.ethz.jadabs.osgi.j2me.OSGiContainer;

/**
 * This is the MIDlet class that represents the Jadabs-Core with 
 * is installed on the mobile device. 
 * 
 * @author Ren&eacute; M&uuml;ller
 * @version 1.0
 */
public class JadabsCoreMIDlet extends MIDlet 
                                  implements CommandListener, BundleActivator
{    
    /** Reference to Log4j window */
    private static Logger LOG;
    
    /** set to true if MIDlet-GUI was already initialized */
    private boolean alreadyInitialized = false;
    
    /** shared static variables */
    private JadabsCoreMIDlet instance;

    /** display of this midlet application */
    private Display display;   
    
    /** Reference to the end-point service */
    // private EndpointService endptsvc;
    
    /* commands */
    private Command exitCmd;    
    private Command connectCmd;
    
    /** a reference to the core component of the MicroGroup Service */
    private MicroGroupServiceCoreImpl microGroupServiceCore;  

    /** Constructor */
    public JadabsCoreMIDlet()
    {        
        // do some initialization stuff
        // this corresponds to the init.xargs descriptor from knopflerfish
        OSGiContainer osgicontainer = OSGiContainer.Instance();
        osgicontainer.setProperty("ch.ethz.jadabs.jxme.peeralias", this.getAppProperty("ch.ethz.jadabs.jxme.peeralias"));
        osgicontainer.setProperty("log4j.priority", this.getAppProperty("log4j.priority"));
        osgicontainer.setProperty("ch.ethz.jadabs.jxme.tcp.port", 
                                  this.getAppProperty("ch.ethz.jadabs.jxme.tcp.port"));
        
        osgicontainer.startBundle(new LogActivator());
        LOG = Logger.getLogger("ch.ethz.jadabs.core.JadabsCoreMIDlet");        
        
        // now bring up the entire Jxme stuff
        osgicontainer.startBundle(new JxmeActivator());
        osgicontainer.startBundle(new TCPActivator());
        osgicontainer.startBundle(new ServiceActivator());
        
        // finally start the micro group service 
        MicroGroupServiceCoreActivator mgsActivator = new MicroGroupServiceCoreActivator();
        osgicontainer.startBundle(mgsActivator);
        microGroupServiceCore = mgsActivator.getService();
        
        // dependend bundles started now we "fire up" ourselves
        osgicontainer.startBundle(this);
        instance = this;               
    }

    /** Handle starting the MIDlet */
    public void startApp()
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke startApp()");
        }
        if (!alreadyInitialized) {
            initGUI();
        }
        alreadyInitialized = true;
        display.setCurrent(Logger.getLogCanvas());
    }

    /** initialize GUI components */
    public void initGUI()
    {
        // obtain reference to Display singleton
        display = Display.getDisplay(this);        
        
        exitCmd = new Command("Shutdown Jadabs Core", Command.EXIT, 1);
        connectCmd = new Command("Connect", Command.SCREEN, 2);
        
        Logger.getLogCanvas().setDisplay(display);
        Logger.getLogCanvas().setPreviousScreen(Logger.getLogCanvas());
        Logger.getLogCanvas().setCommandAndListener(new Command[] {connectCmd, exitCmd}, this);
        display.setCurrent(Logger.getLogCanvas());
        
//        try {
//            wiring.waitforWakeupMessage();
//        } catch(IOException e) {
//            LOG.debug("waitforWakeupMessage() has failed!");
//        }        
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
        if (c == connectCmd) {
//            int port = 1234;
//            LOG.debug("connecting on port "+port);
//            try {
//                connection = wiring.connect(port);
//                Thread connectionThread = new Thread(new ConnectionThread());
//                connectionThread.start();
//            } catch(IOException e) {
//                LOG.error("cannot connect to port "+port+
//                          " on local loopback interface.");
//            }
//            
        } else if (c == exitCmd) {
            quitApp();
        }
    }
             
    
    /**
     * Called by the OSGi container when this bundle is started.
     * @param bc reference to context data of this bundle
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bc)
    {
        // get Endpoint service
        //ServiceReference sref = bc.getServiceReference("ch.ethz.jadabs.jxme.EndpointService");
        //endptsvc = (EndpointService)bc.getService(sref);
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