/* 
 * Created on Dec 9th, 2004
 * 
 * $Id: JadabsCoreTestMIDlet.java,v 1.1 2004/12/22 09:35:09 printcap Exp $
 */
package ch.ethz.jadabs.core.test;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import org.apache.log4j.LogActivator;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.ethz.jadabs.core.wiring.LocalWiringBundle;
import ch.ethz.jadabs.core.wiring.LocalWiringConnection;
import ch.ethz.jadabs.osgi.j2me.OSGiContainer;


/**
 * This is the Test MIDlet class that makes use of Jadabs-Core which 
 * is installed on the mobile device. 
 * 
 * @author Ren&eacute; M&uuml;ller
 * @version 1.0
 */
public class JadabsCoreTestMIDlet extends MIDlet 
                                  implements CommandListener, BundleActivator
{    
    /** Reference to Log4j window */
    private static Logger LOG;
    
    /** set to true if MIDlet-GUI was already initialized */
    private boolean alreadyInitialized = false;
    
    /** shared static variables */
    private JadabsCoreTestMIDlet instance;

    /** display of this midlet application */
    private Display display;   
    
    /** Reference to the end-point service */
    // private EndpointService endptsvc;
    
    /** Refernce to the message field */
    private MessageScreen messageScreen;
    
    /* commands */
    private Command logCmd;
    private Command exitCmd;        
    private Command messageCmd;
    private Command sendCmd;
    
    /** local wiring interface */
    private LocalWiringBundle wiring;
    
    /** local wiring connection */
    private LocalWiringConnection connection;
    
    
    
    /** Constructor */
    public JadabsCoreTestMIDlet()
    {        
        // do some initialization stuff
        // this corresponds to the init.xargs descriptor from knopflerfish
        OSGiContainer osgicontainer = OSGiContainer.Instance();
        osgicontainer.setProperty("ch.ethz.jadabs.jxme.peeralias", this.getAppProperty("ch.ethz.jadabs.jxme.peeralias"));
        osgicontainer.setProperty("log4j.priority", this.getAppProperty("log4j.priority"));
        osgicontainer.setProperty("ch.ethz.jadabs.jxme.bt.rendezvouspeer", 
                                  this.getAppProperty("ch.ethz.jadabs.jxme.bt.rendezvouspeer"));
        osgicontainer.startBundle(new LogActivator());
        LOG = Logger.getLogger("JadabsCoreTestMIDlet");
        osgicontainer.startBundle(this);
        instance = this;
        
         
    }

    /** Handle starting the MIDlet */
    public void startApp()
    {
        if (!alreadyInitialized) {
            initGUI();
        }
        display.setCurrent(Logger.getLogCanvas());
        alreadyInitialized = true;        
    }

    /** initialize GUI components */
    public void initGUI()
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke startApp()");
        }

        // obtain reference to Display singleton
        display = Display.getDisplay(this);        
        
        messageCmd = new Command("Message", Command.SCREEN, 1);
        sendCmd = new Command("Send", Command.SCREEN, 2);
        logCmd = new Command("Log", Command.SCREEN, 3);
        exitCmd = new Command("Exit", Command.EXIT, 1);
        messageScreen = new MessageScreen(this, new Command[] {
                sendCmd, logCmd, exitCmd    });
        
        Logger.getLogCanvas().setDisplay(display);
        Logger.getLogCanvas().setPreviousScreen(Logger.getLogCanvas());
        Logger.getLogCanvas().setCommandAndListener(new Command[] {
               messageCmd, exitCmd}, this);          
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
        } else if (c == sendCmd) {
            if (wiring.isConnected()) {
                String msg = messageScreen.getString();
                byte buffer[] = msg.getBytes();
                LOG.debug("writing message '"+msg+"'");
                try {
                    connection.sendBytes(buffer);
                } catch(IOException e) {
                    LOG.error("cannot send message!");
                }
            } else {
                LOG.debug("cannot send message as we are not connected!");
            }
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
        
        wiring = new LocalWiringBundle(1234);
        // wakeup core
//        try {
//            wiring.wakeupCore();
//        } catch (IOException e) {
//            LOG.debug("cannot wakeup core!");
//        }
        // now wait for core to connect to us
        try {
            wiring.waitforWakeupConnection();
        } catch(IOException e) {
            LOG.debug("waitforWakeupConnection failed!");
        }        
        connection = wiring.getConnection();
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