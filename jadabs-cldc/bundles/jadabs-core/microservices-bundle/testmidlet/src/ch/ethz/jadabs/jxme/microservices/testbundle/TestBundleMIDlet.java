/* 
 * Created on Feb 8, 2005
 * 
 * $Id: TestBundleMIDlet.java,v 1.1 2005/02/17 17:29:16 printcap Exp $
 */
package ch.ethz.jadabs.jxme.microservices.testbundle;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import org.apache.log4j.LogActivator;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.ethz.jadabs.core.wiring.ConnectionNotifee;
import ch.ethz.jadabs.core.wiring.LocalWiringConnection;
import ch.ethz.jadabs.jxme.microservices.MicroGroupServiceBundleActivator;
import ch.ethz.jadabs.jxme.microservices.MicroGroupServiceBundleImpl;
import ch.ethz.jadabs.osgi.j2me.OSGiContainer;


/**
 * This is the Test MIDlet class that makes use of Jadabs Micro-Groupservice Core Bundle
 * which is installed on the mobile device. 
 * 
 * @author Ren&eacute; M&uuml;ller
 * @version 1.0
 */
public class TestBundleMIDlet extends MIDlet 
                              implements CommandListener, BundleActivator, ConnectionNotifee
{    
    /** Reference to Log4j window */
    private static Logger LOG;
    
    /** set to true if MIDlet-GUI was already initialized */
    private boolean alreadyInitialized = false;
    
    /** shared static variables */
    private TestBundleMIDlet instance;
    
    /** MicroGroupService service bundle actiavtor */
    private MicroGroupServiceBundleImpl groupService;
    

    /** display of this midlet application */
    private Display display;       
    
    /** Reference to the message field */
    private MessageScreen messageScreen;
    
    /* commands */
    private Command logCmd;
    private Command wakeupCoreCmd;
    private Command exitCmd;        
    private Command messageCmd;
    private Command sendCmd;
    
    
    /** Constructor */
    public TestBundleMIDlet()
    {        
        // do some initialization stuff
        // this corresponds to the init.xargs descriptor from knopflerfish
        OSGiContainer osgicontainer = OSGiContainer.Instance();
        osgicontainer.setProperty("log4j.priority", this.getAppProperty("log4j.priority"));
        osgicontainer.setProperty("ch.ethz.jadabs.microservices.bundleport", 
                                  this.getAppProperty("ch.ethz.jadabs.microservices.bundleport"));
        osgicontainer.startBundle(new LogActivator());
        LOG = Logger.getLogger("ch.ethz.jadabs.jxme.microservices.testbundle.TestBundleMIDlet");
        
        // start bundle part of MicroGroupService 
        MicroGroupServiceBundleActivator mgsActivator = new MicroGroupServiceBundleActivator();        
        osgicontainer.startBundle(mgsActivator);
        groupService = mgsActivator.getService();        
        
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
        display.setCurrent(Logger.getLogCanvas());
        alreadyInitialized = true;               
    }

    /** initialize GUI components */
    public void initGUI()
    {
        // obtain reference to Display singleton
        display = Display.getDisplay(this);        
        
        messageCmd = new Command("Message", Command.SCREEN, 2);
        sendCmd = new Command("Send", Command.SCREEN, 3);
        logCmd = new Command("Log", Command.SCREEN, 4);
        wakeupCoreCmd = new Command("Wakeup Core", Command.SCREEN, 1);
        exitCmd = new Command("Exit", Command.EXIT, 1);
        messageScreen = new MessageScreen(this, new Command[] {
                sendCmd, logCmd, exitCmd    });
        
        Logger.getLogCanvas().setDisplay(display);
        Logger.getLogCanvas().setPreviousScreen(Logger.getLogCanvas());
        Logger.getLogCanvas().setCommandAndListener(new Command[] {
               wakeupCoreCmd, messageCmd, exitCmd}, this);          
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
        } else if (c == logCmd) {
            display.setCurrent(Logger.getLogCanvas());
        } else if (c == wakeupCoreCmd) {
            LOG.debug("wake up core...");
            // try to wake up core 
            // we postpone it to here to make sure that the GUI, logging facility
            // etc. are set up properly. 
            groupService.wakeupCore();
        } else if (c == sendCmd) {
            display.setCurrent(Logger.getLogCanvas());
//            if (wiring.isConnected()) {
//                String msg = messageScreen.getString();
//                byte buffer[] = msg.getBytes();
//                LOG.debug("writing message '"+msg+"'");
//                try {                    
//                    connection.sendBytes(buffer);
//                } catch(IOException e) {
//                    LOG.error("cannot send message!");
//                }                
//            } else {
//                LOG.debug("cannot send message as we are not connected!");
//            }
//            LOG.debug("writing done.");            
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
        
//        wiring = new LocalWiringBundle(1234, this);
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

    /** 
     * Invoked when a TCP connection was accepted.
     * @param connection LocalWiringConnection from this new connection
     * @see ch.ethz.jadabs.core.wiring.ConnectionNotifee#connectionEstablished(ch.ethz.jadabs.core.wiring.LocalWiringConnection)
     */
    public void connectionEstablished(LocalWiringConnection connection)
    {
//        this.connection = connection;
        if (LOG.isDebugEnabled()) {
            LOG.debug("new wiring connection established.");
        }        
    }
}