/*
 * $Id: ChatMain.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package ch.ethz.jadabs.jxme.bt.btchat;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;


/**
 * Main MIDlet class that execute BlueChat application.
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * 
 * <pre>
 * BlueChat example application. Originally published in Java Developer's
 * Journal (volume 9 issue 2). Updated by Ben Hui on www.benhui.net. Copyright:
 * (c) 2003-2004 Author: Ben Hui
 * 
 * YOU ARE ALLOWED TO USE THIS CODE FOR EDUCATIONAL, PERSONAL TRAINNING,
 * REFERENCE PURPOSE. YOU MAY DISTRIBUTE THIS CODE AS-IS OR MODIFIED FORM.
 * HOWEVER, YOU CANNOT USE THIS CODE FOR COMMERCIAL PURPOSE. THIS INCLUDE, BUT
 * NOT LIMITED TO, PRODUCING COMMERCIAL SOFTWARE, CONSULTANT SERVICE,
 * PROFESSIONAL TRAINNING MATERIAL.
 * </pre>
 * 
 * @author Ben Hui
 * @version 1.0
 */
public class ChatMain extends MIDlet implements BTListener, CommandListener
{

    // shared static variables
    public static ChatMain instance;

    public static Display display;

    // debug flag
    public static boolean isDebug = false;

    // Chat app GUI components
    private InputUI inputui;
    private MessageUI messageui;
    private NameUI nameui;

    // Bluetooth network layer for BlueChat app
    private NetLayer btnet;

    /** Constructor */
    public ChatMain()
    {       
        instance = this;
    }

    /** Handle starting the MIDlet */
    public void startApp()
    {
        log("invoke startApp()");

        // obtain reference to Display singleton
        display = Display.getDisplay(this);

        // initialize the GUI component, and prompt for name input
        inputui = new InputUI();
        messageui = new MessageUI();
        nameui = new NameUI();
        display.setCurrent(nameui);

    }

    /** Handle pausing the MIDlet */
    public void pauseApp()
    {
        log("invoke pauseApp()");
    }

    /** Handle destroying the MIDlet */
    public void destroyApp(boolean unconditional)
    {
        log("invoke destroyApp()");
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
     * Handle event/activity from Bluetooth Network layer. This class is an
     * implementation of BTListener; therefore, it handles all the bluetooth
     * network event that received by NetLayer. The list of possible event are
     * defined in BTListener.EVENT_XXX.
     * 
     * @param event
     *            event type. see NetLayer constants
     * @param param1
     *            parameter 1 is usually the remote EndPoint that trigger the
     *            action
     * @param param2
     *            parameter 2 is usually the argument of the action
     */
    public void handleAction(String event, Object param1, Object param2)
    {
        log("invoke handleAction. action=" + event);

        if (event.equals(BTListener.EVENT_JOIN))
        {
            // a new user has join the chat room
            EndPoint endpt = (EndPoint) param1;
            String msg = endpt.remoteName + " joins the chat room";
            ChatPacket packet = new ChatPacket(NetLayer.SIGNAL_HANDSHAKE, endpt.remoteName, msg);

            // display the join message on screen
            messageui.msgs.addElement(packet);
            messageui.repaint();

        } else if (event.equals(BTListener.EVENT_SENT))
        {
            // nothing to do
        } else if (event.equals(BTListener.EVENT_RECEIVED))
        {
            // a new message has received from a remote user
            EndPoint endpt = (EndPoint) param1;
            ChatPacket msg = (ChatPacket) param2;
            // render this message on screen
            messageui.msgs.addElement(msg);
            messageui.repaint();

        } else if (event.equals(BTListener.EVENT_LEAVE))
        {
            // a user has leave the chat room
            EndPoint endpt = (EndPoint) param1;
            String msg = endpt.remoteName + " leaves the chat room";
            ChatPacket packet = new ChatPacket(NetLayer.SIGNAL_TERMINATE, endpt.remoteName, msg);
            // display the leave message on screen
            messageui.msgs.addElement(packet);
            messageui.repaint();

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
        log("invoke commandAction. command=" + c.getLabel());
        if (d == inputui && c.getLabel().equals("Send"))
        {
            String msg = inputui.getString();
            // send the message to all connected BlueChat remote EndPoints
            btnet.sendString(msg);

            // update the message screen to reflect the entered message.
            // create a dummy packet object to hold the entered message.
            ChatPacket packet = new ChatPacket(NetLayer.SIGNAL_MESSAGE, btnet.localName, msg);
            messageui.msgs.addElement(packet);
            display.setCurrent(messageui);
            messageui.repaint();

        } else if (d == nameui && (c.getLabel().equals("Chat") || c.getLabel().equals("Chat (Debug)")))
        {
            // turn on debug logging on screen
            // see log() method (there are several)
            if (c.getLabel().equals("Chat (Debug)")) ChatMain.isDebug = true;

            // user enters virtual chat room.
            // create and initialize Bluetooth network layer
            btnet = new NetLayer();
            String localName = nameui.text.getString();
            log("set local nick name to " + localName);

            // initialize the network layer. This will start the local BlueChat
            // server
            btnet.init(localName, this);

            // search for existing BlueChat nodes
            btnet.query();

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
            messageui.msgs.removeAllElements();
            messageui.repaint();

        } else if (d == messageui && c.getLabel().equals("Exit"))
        {
            // disconnect from the virtual chat room.
            // this will send out TERMINATE signal to all connected
            // remote EndPoints, wait for the TERMINATE_ACK signal, and
            // disconnect all connections.
            btnet.disconnect();
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
            System.gc();		// start GC before output of free memory.
            String msg= "free "+((int) (Runtime.getRuntime().freeMemory() / 1024))+
                        " kB of "+((int) (Runtime.getRuntime().totalMemory() / 1024))+
                        " kB";
            ChatMain.gui_log("", msg);
        }
    }

    private static void log(String s)
    {
        System.out.println("ChatMain: " + s);

        // "M" means Main class
        if (ChatMain.isDebug) ChatMain.gui_log("M", s);
    }

    public static void gui_log(String source, String s)
    {
        ChatPacket packet = new ChatPacket(NetLayer.SIGNAL_MESSAGE, source, s);
        instance.messageui.msgs.addElement(packet);
        instance.messageui.repaint();

    }
}