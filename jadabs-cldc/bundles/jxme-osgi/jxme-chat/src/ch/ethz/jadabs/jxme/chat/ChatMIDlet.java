/*
 * $Id: ChatMIDlet.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package ch.ethz.jadabs.jxme.chat;

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

import ch.ethz.jadabs.jxme.EndpointService;
import ch.ethz.jadabs.jxme.JxmeActivator;
import ch.ethz.jadabs.jxme.bt.BTActivator;
import ch.ethz.jadabs.osgi.j2me.OSGiContainer;
import ch.ethz.jadabs.jxme.chat.ChatCommunication;
import ch.ethz.jadabs.jxme.chat.ChatListener;
import ch.ethz.jadabs.jxme.chat.InputUI;
import ch.ethz.jadabs.jxme.chat.MessageUI;
import ch.ethz.jadabs.jxme.chat.NameUI;

/**
 * Main MIDlet class that execute BlueChat application.
 * 
 * <p> This is the BlueChat application from Ben Hui that was slightly adapted in
 * order to be run over Jadabs-Jxme.</p>
 * 
 * <pre>
 * BlueChat example application. Originally published in Java Developer's
 * Journal (volume 9 issue 2). Updated by Ben Hui on www.benhui.net. Copyright:
 * (c) 2003-2004 Author: Ben Hui<
 * 
 * YOU ARE ALLOWED TO USE THIS CODE FOR EDUCATIONAL, PERSONAL TRAINNING,
 * REFERENCE PURPOSE. YOU MAY DISTRIBUTE THIS CODE AS-IS OR MODIFIED FORM.
 * HOWEVER, YOU CANNOT USE THIS CODE FOR COMMERCIAL PURPOSE. THIS INCLUDE, BUT
 * NOT LIMITED TO, PRODUCING COMMERCIAL SOFTWARE, CONSULTANT SERVICE,
 * PROFESSIONAL TRAINNING MATERIAL.
 * </pre>
 * 
 * @author Ben Hui
 * @author Ren&eacute; M&uuml;ller
 * @version 1.0
 */
public class ChatMIDlet extends MIDlet implements ChatListener, CommandListener,
																  BundleActivator
{    
    
    /** shared static variables */
    public static ChatMIDlet instance;

    /** display of this midlet application */
    public static Display display;

    /** debug flag */
    public static boolean isDebug = false;

    /** Chat app GUI component: GUI for entering messages */
    private InputUI inputui;

    /** Chat app GUI component: GUI representing chat log */ 
    private MessageUI messageui;

    /** Chat app GUI component: GUI for entering nickname */
    private NameUI nameui;
    
    /** Reference to Log4j window */
    private static Logger LOG;

    /** BT chat network layer */
    private ChatCommunication chat;
    
    /** EndpointService used in the chat application */
    private EndpointService endptsvc;

    /** Constructor */
    public ChatMIDlet()
    {        
        // do some initialization stuff
        // this corresponds to the init.xargs descriptor from knopflerfish
        OSGiContainer osgicontainer = OSGiContainer.Instance();
        osgicontainer.setProperty("ch.ethz.jadabs.jxme.hostname", this.getAppProperty("ch.ethz.jadabs.jxme.hostname"));
        osgicontainer.setProperty("log4j.priority", this.getAppProperty("log4j.priority"));
        osgicontainer.setProperty("ch.ethz.jadabs.jxme.bt.rendezvouspeer", 
                                  this.getAppProperty("ch.ethz.jadabs.jxme.bt.rendezvouspeer"));
        osgicontainer.startBundle(new LogActivator());
        osgicontainer.startBundle(new JxmeActivator());
        osgicontainer.startBundle(new BTActivator());
        osgicontainer.startBundle(this);
        instance = this;
        LOG = Logger.getLogger("ChatMIDlet");
    }

    /** Handle starting the MIDlet */
    public void startApp()
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke startApp()");
        }

        // obtain reference to Display singleton
        display = Display.getDisplay(this);

        // initialize the GUI component, and prompt for name input
        inputui = new InputUI();
        messageui = new MessageUI();
        nameui = new NameUI();
        display.setCurrent(nameui);
        
        Logger.getLogCanvas().setDisplay(display);
        Logger.getLogCanvas().setPreviousScreen(nameui);
        
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
     * BlueChat chat action handler gets called when some thing 
     * happens in chat. 
     * 
     * @param event
     *            must be <code>EVENT_JOIN</code>, <code>EVENT_LEAVE</code>,
     *            <code>EVENT_RECEIVED</code> or <code>EVENT_SENT</code>
     * @param param String parameter of the action: 
     *            if <code>EVENT_JOIN</code> or </code>EVENT_LEAVE</code>
     *            then param is equal to the nickname, if <code>EVENT_SENT</code>
     *            or <code>EVENT_RECEIVED</code> then param is equal to 
     *            "nickname: message"
     */
    public void handleAction(String event, String param)
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoke handleAction. action=" + event);
        }

        if (event.equals(ChatListener.EVENT_JOIN))
        {
            // a new user has join the chat room
            messageui.addEntry(param+" joins the chat room");
        } else if (event.equals(ChatListener.EVENT_SENT))
        {
            // nothing to do
        } else if (event.equals(ChatListener.EVENT_RECEIVED))
        {
            // a new message has received from a remote user
            // render this message on screen
            messageui.addEntry(param);

        } else if (event.equals(ChatListener.EVENT_LEAVE))
        {
            // a user has leave the chat room
            messageui.addEntry(param+" leaves the chat room");
        }
    }

    /**
     * Handle user action from BlueChat application.
     * 
     * @param c
     *            GUI command
     * @param d
     *            GUI display object
     */
    public void commandAction(Command c, Displayable d)
    {
        if (LOG.isDebugEnabled()) {            
            LOG.debug("invoke commandAction. command=" + c.getLabel());            
        }
        if (c.getLabel().equals("Log")) 
        {
            Logger.getLogCanvas().setPreviousScreen(d);
            display.setCurrent(Logger.getLogCanvas());
        }
        else if (d == inputui && c.getLabel().equals("Send"))
        {
            String msg = inputui.getString();
            // send the message to chat
            chat.sendMessage(msg);

            // update the message screen to reflect the entered message.
            messageui.addEntry(chat.getNickname()+": "+msg);
            display.setCurrent(messageui);
        } else if (d == nameui && (c.getLabel().equals("Chat") || c.getLabel().equals("Chat (Debug)")))
        {
            // turn on debug logging on screen
            // see log() method (there are several)
            if (c.getLabel().equals("Chat (Debug)")) ChatMIDlet.isDebug = true;

            // user enters virtual chat room.            
            String localName = nameui.text.getString();
            if (LOG.isDebugEnabled()) {
                LOG.debug("set local nick name to " + localName);
            }
            chat.enterChat(localName);

            // switch screen to message screen
            display.setCurrent(messageui);

        } else if (d == inputui && c.getLabel().equals("Back"))
        {
            // just does nothing and return to message screen
            display.setCurrent(messageui);

        } else if (d == messageui && c.getLabel().equals("Write"))
        {
            // enter input screen
            display.setCurrent(inputui);
            inputui.showUI(); // clear the input text field

        } else if (d == messageui && c.getLabel().equals("Clear"))
        {
            // clear the history of message and refresh the message screen
            messageui.removeAllEntries();

        } else if (d == messageui && c.getLabel().equals("Exit"))
        {
            // disconnect from the virtual chat room.
            // this will send out TERMINATE signal to all connected
            // remote EndPoints, wait for the TERMINATE_ACK signal, and
            // disconnect all connections.
            
            chat.leaveChat();
            quitApp();

        } else if (d == messageui && c.getLabel().equals("About BlueChat"))
        {
            Alert alert = new Alert("About",
                    "BlueChat 1.0. By Ben Hui (c) 2004. Visit www.benhui.net for more mobile dev resources.", null,
                    AlertType.INFO);
            alert.setTimeout(Alert.FOREVER);
            display.setCurrent(alert, messageui);

        } else if (d == messageui && c.getLabel().equals("Heap Size"))
        {
            /* size command was issued */
            System.gc(); // start GC before output of free memory.
            String msg = "free " + ((int) (Runtime.getRuntime().freeMemory() / 1024)) + " kB of "
                    + ((int) (Runtime.getRuntime().totalMemory() / 1024)) + " kB";
            LOG.info(msg);
        } else if (d == messageui && c.getLabel().equals("Log")) 
        {
            Logger.getLogCanvas().setPreviousScreen(d);
            display.setCurrent(Logger.getLogCanvas());
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
        ServiceReference sref = bc.getServiceReference("ch.ethz.jadabs.jxme.EndpointService");
        endptsvc = (EndpointService)bc.getService(sref);
        
        // setup chat communication and register this service */
        chat = new ChatCommunication(this, endptsvc);
        endptsvc.addListener("jxmechat", chat);                       
    }

    /**
     * Called by the OSGi container when this bundle is stopped.
     * @param bc reference to context data of this bundle
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bc) 
    {
        // remove chat communication servce from service registry 
        endptsvc.removeListener("jxmechat");
    }
}