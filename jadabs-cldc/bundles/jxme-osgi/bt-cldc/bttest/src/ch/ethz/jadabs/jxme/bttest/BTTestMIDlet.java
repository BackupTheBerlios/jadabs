/*
 * Created on Jul 22, 2004
 * $Id: BTTestMIDlet.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package ch.ethz.jadabs.jxme.bttest;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

import org.apache.log4j.LogActivator;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.ethz.jadabs.osgi.j2me.OSGiContainer;

/**
 * This is the MIDlet class of the jxme chat application.
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class BTTestMIDlet extends MIDlet implements CommandListener, BundleActivator
{
    private OSGiContainer osgicontainer;
    private Display display;
    private Logger LOG;
    
    private Command heapInfoCmd;
    private Command exitCmd;    
    private Command initBtCmd;
    private Command inqCmd;
    private Command startRFCOMMServiceCmd;
    private Command stopRFCOMMServiceCmd;
    private Command retrieveDevicesCmd;
    
    private BTTransport btTransport;
    
    
    /**
     * constructor sets up OSGi container and starts bundles
     */
    public BTTestMIDlet()
    {
        osgicontainer = OSGiContainer.Instance();
        osgicontainer.startBundle(new LogActivator());
        osgicontainer.startBundle(this);        
    }
    
    /**
     * Start Application
     */
    protected void startApp()
    {
        display = Display.getDisplay(this);                
        initBtCmd = new Command("Init BT", Command.SCREEN, 1);
        inqCmd = new Command("Start Inquiry", Command.SCREEN, 2);
        retrieveDevicesCmd = new Command("Retrieve Devices", Command.SCREEN, 3);
        startRFCOMMServiceCmd = new Command("Start RFCOMM Service", Command.SCREEN, 4);
        stopRFCOMMServiceCmd = new Command("Stop RFCOMM Service", Command.SCREEN, 5);        
        heapInfoCmd = new Command("Heap Info", Command.SCREEN, 5);
        exitCmd = new Command("Exit", Command.SCREEN, 6);
        LOG = Logger.getLogger("ChatMIDlet");
        Logger.getLogCanvas().setCommandAndListener(
                new Command[] { initBtCmd, inqCmd, retrieveDevicesCmd, 
                        		  startRFCOMMServiceCmd, stopRFCOMMServiceCmd,
                                heapInfoCmd, exitCmd}, this);	        
        display.setCurrent(Logger.getLogCanvas());    
       
        btTransport = new BTTransport();
    }

    /**
     * Called when the MIDlet is temprarily paused
     */
    protected void pauseApp()
    {
        // do nothing here        
    }

    /**
     * Called when application is about to be terminated
     * @param unconditional if set to true the MIDlet must terminate,
     *        if false the MIDlet may throw an MIDletStateChangeException
     *        it it does not want to be terminated 
     */
    protected void destroyApp(boolean unconditional)
    {
        // so far no clean up required
    }

    /**
     * Listener for GUI commands
     * @param c command that was issued
     * @param d displayable where the command was issued
     */
    public void commandAction(Command c, Displayable d)
    {
        if (c == heapInfoCmd) {
            System.gc();
            Runtime rt = Runtime.getRuntime();
            
            LOG.info(""+(rt.freeMemory()/1024)+"kB free of total "
                       +(rt.totalMemory()/1024)+" kB");
        } else if (c == exitCmd) {            
            destroyApp(true);
            notifyDestroyed();
        } else if (c == initBtCmd) {
            btTransport.init();
        } else if (c == retrieveDevicesCmd) {
            btTransport.retrieveDevices();
        } else if (c == startRFCOMMServiceCmd) {
            btTransport.startRFCOMMService();
        } else if (c == inqCmd) {
            btTransport.startInquiry();
        } else if (c == stopRFCOMMServiceCmd) {
            btTransport.stopRFCOMMService();
        }
       
    }

    /** 
     * The midlet is itself a bundle, thus it must also implement the 
     * start method from Bundle-Activator.
     * @param bc bundle context for this bundle 
     */
    public void start(BundleContext bc) throws Exception
    {
        
    }

    /**
     * The midlet is itself a bundle, thus it must also implement the 
     * stop method from Bundle-Activator
     * @param bc bundle context for this bundle 
     */
    public void stop(BundleContext bc) throws Exception
    {
        // so far nothing to be done here
        
    }

}