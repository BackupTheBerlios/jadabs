/*
 * ============================================================================
 * Copyright (c) 2003 Nokia. This material, including documentation and any
 * related computer programs, is protected by copyright controlled by Nokia. All
 * rights are reserved. Copying, including reproducing, storing, adapting or
 * translating, any or all of this material requires the prior written consent
 * of Nokia. This material also contains confidential information, which may not
 * be disclosed to others without the prior written consent of Nokia.
 * ============================================================================
 */
package com.nokia.phone.ri.sip.examples;

import java.util.*;
import java.io.*;
import javax.microedition.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

import javax.microedition.sip.*;

public class SendMessage extends MIDlet implements CommandListener, SipClientConnectionListener
{

    private Display display;

    private long startTime;

    private Form form;

    private TextField address;

    private TextField subject;

    private TextField message;

    private Command sendCmd;

    private Command exitCmd;

    public SendMessage()
    {
        System.out.println("MIDlet: SendMessage starting...");
        display = Display.getDisplay(this);
        form = new Form("Message example");
        address = new TextField("Address", "sip:user@10.128.0.50:5070", 30, TextField.LAYOUT_LEFT);
        subject = new TextField("Subject", "test", 30, TextField.LAYOUT_LEFT);
        message = new TextField("Message text", "test message...", 30, TextField.LAYOUT_LEFT);
        form.append(address);
        form.append(subject);
        form.append(message);
        sendCmd = new Command("Send", Command.ITEM, 1);
        form.addCommand(sendCmd);
        exitCmd = new Command("Exit", Command.EXIT, 1);
        form.addCommand(exitCmd);
        form.setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d)
    {
        if (c == sendCmd)
        {
            Thread t = new Thread()
            {

                public void run()
                {
                    sendMessage();
                }
            };
            t.start();
        }
        if (c == exitCmd)
        {
            destroyApp(true);
        }
    }

    public void startApp()
    {
        display.setCurrent(form);
        System.out.println("MIDlet: SendMessage startApp()");
    }

    private void sendMessage()
    {
        SipClientConnection sc = null;
        try
        {
            sc = (SipClientConnection) Connector.open(address.getString());
            sc.setListener(this);
            String text = message.getString();
            sc.initRequest("MESSAGE", null);
            sc.setHeader("Subject", subject.getString());
            sc.setHeader("Content-Type", "text/plain");
            sc.setHeader("Content-Length", "" + text.length());
            OutputStream os = sc.openContentOutputStream();
            os.write(text.getBytes());
            os.close(); // close and send out
            startTime = System.currentTimeMillis();

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void notifyResponse(SipClientConnection scc)
    {
        try
        {
            System.out.println("MIDlet: waited " + (System.currentTimeMillis() - startTime) + " secs");
            scc.receive(1);
            form.append("notifyResponse: " + scc.getStatusCode() + " " + scc.getReasonPhrase());
            scc.close();
        } catch (Exception ex)
        {
            form.append("MIDlet: exception " + ex.getMessage());
        }
    }

    public void pauseApp()
    {
        System.out.println("MIDlet: pauseApp()");
    }

    public void destroyApp(boolean b)
    {
        System.out.println("MIDlet: destroyApp()");
        notifyDestroyed();
    }

}

