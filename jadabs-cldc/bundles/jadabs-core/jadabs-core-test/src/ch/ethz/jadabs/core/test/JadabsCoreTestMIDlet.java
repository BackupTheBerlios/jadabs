/* 
 * Created on Dec 9th, 2004
 * 
 * $Id: JadabsCoreTestMIDlet.java,v 1.5 2005/02/18 08:58:33 printcap Exp $
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

import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.Pipe;
import ch.ethz.jadabs.jxme.microservices.MicroElement;
import ch.ethz.jadabs.jxme.microservices.MicroGroupServiceBundleActivator;
import ch.ethz.jadabs.jxme.microservices.MicroGroupServiceBundleImpl;
import ch.ethz.jadabs.jxme.microservices.MicroListener;
import ch.ethz.jadabs.jxme.microservices.MicroMessage;
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
    private Command wakeupCoreCmd;
    private Command createPipe;
    private Command addListener;
    private Command closePipe;
    private Command exitCmd;        
    private Command messageCmd;
    private Command sendCmd;
    
    /** the MigroGroupServiceBundle servce object */
    private MicroGroupServiceBundleImpl groupService;
    
    /** the name of the pipe */
    private final String PIPE_NAME = "testpipe";
    
    /** ID of the pipe */
    private String id;
    
    
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
        osgicontainer.setProperty("ch.ethz.jadabs.microservices.bundleport", 
                                  this.getAppProperty("ch.ethz.jadabs.microservices.bundleport"));
        osgicontainer.startBundle(new LogActivator());
        LOG = Logger.getLogger("ch.ethz.jadabs.core.test.JadabsCoreTestMIDlet");
        
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
        createPipe = new Command("Create Pipe, Publish", Command.SCREEN, 5);
        addListener = new Command("Add Listener", Command.SCREEN, 6);
        closePipe = new Command("Close Pipe", Command.SCREEN, 7);
        exitCmd = new Command("Exit", Command.EXIT, 1);
        messageScreen = new MessageScreen(this, new Command[] {
                sendCmd, logCmd, exitCmd    });
        
        Logger.getLogCanvas().setDisplay(display);
        Logger.getLogCanvas().setPreviousScreen(Logger.getLogCanvas());
        Logger.getLogCanvas().setCommandAndListener(new Command[] {
               wakeupCoreCmd, messageCmd, exitCmd, createPipe, addListener, closePipe}, this);          
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
            groupService.wakeupCore();                   
        } else if (c == createPipe) {
            LOG.debug("create pipe "+PIPE_NAME+"...");
            id = groupService.create(NamedResource.PIPE, PIPE_NAME, "urn:jxta:uuid-0002:0001:04", Pipe.PROPAGATE);
            LOG.debug("pipe with id:"+id+" created.");
            groupService.publish(NamedResource.PIPE, PIPE_NAME, id);
            LOG.debug("pipe "+id+" published.");            
        } else if (c == addListener) {
            LOG.debug("add listener to pipe "+id+".");
            try {
	            groupService.listen(id, new MicroListener() {
	                public void handleMessage(MicroMessage message, String listenerId)
	                {
	                    LOG.debug("incoming message \""+message.toString()+"\", listenerId="+listenerId);                    
	                }	             
	            });
            } catch(IOException e) {
               LOG.error("Error while registering listener to pipe '"+id+"': "+e.getMessage());
            }
        } else if (c == closePipe) {
            LOG.debug("closing pipe "+id+" (removing listener)");
            try {
                groupService.close(id);
            } catch(IOException e) {
                LOG.error("Error while closing pipe '"+id+"': "+e.getMessage());
            }
        } else if (c == sendCmd) {
            display.setCurrent(Logger.getLogCanvas());
            String msg = messageScreen.getString();
            
            MicroElement elms[] = new MicroElement[1];
            elms[0] = new MicroElement("text", msg, "jxta");
            MicroMessage message = new MicroMessage(elms);
            
            LOG.debug("sending JXTA message over pipe.");
            try {
                groupService.send(id, message);
            } catch(IOException e) {
                LOG.error("Error while sending JXTA message: "+e.getMessage());
            }
            LOG.debug("sending done.");            
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