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

public class ReceiveMessage extends MIDlet implements CommandListener, SipServerConnectionListener
{

    private Display display;

    private long startTime;

    private Form form;

    private TextField receivePort;

    private Command receiveCmd;

    private Command exitCmd;

    SipConnectionNotifier scn = null;

    SipServerConnection ssc = null;

    public ReceiveMessage()
    {
        System.out.println("MIDlet: ReceiveMessage starting...");
        display = Display.getDisplay(this);
        form = new Form("Receive Message");
        receivePort = new TextField("Give receive port:", "sip:5060", 30, TextField.LAYOUT_LEFT);
        form.append(receivePort);
        receiveCmd = new Command("Start", Command.ITEM, 1);
        exitCmd = new Command("Exit", Command.EXIT, 1);
        form.addCommand(receiveCmd);
        form.addCommand(exitCmd);
        form.setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d)
    {
        if (c == receiveCmd)
        {
            Thread t = new Thread()
            {

                public void run()
                {
                    receiveMessage();
                }
            };
            t.start();
        }
        if (c == exitCmd)
        {
            if (scn != null)
            {
                try
                {
                    scn.close();
                } catch (IOException iox)
                {
                }
            }
            destroyApp(true);
        }
    }

    public void startApp()
    {
        display.setCurrent(form);
        System.out.println("MIDlet: ReceiveMessage startApp()");
    }

    public void receiveMessage()
    {
        try
        {
            if (scn != null)
                scn.close();
            scn = (SipConnectionNotifier) Connector.open(receivePort.getString());
            scn.setListener(this);
            form.append("Listening... in port: " + scn.getLocalPort());
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void notifyRequest(SipConnectionNotifier scn)
    {
        try
        {
            ssc = scn.acceptAndOpen();
            if (ssc.getMethod().equals("MESSAGE"))
            {
                String contentType = ssc.getHeader("Content-Type");
                String contentLength = ssc.getHeader("Content-Length");
                int length = Integer.parseInt(contentLength);
                if ((contentType != null) && contentType.equals("text/plain"))
                {
                    InputStream is = ssc.openContentInputStream();
                    int i = 0;
                    byte testBuffer[] = new byte[length];
                    i = is.read(testBuffer);

                    String tmp = new String(testBuffer, 0, i);

                    StringItem st = new StringItem("Subject:", ssc.getHeader("Subject"));
                    form.append(st);
                    st = new StringItem("Message:", tmp);
                    form.append(st);
                }
                ssc.initResponse(200);
                ssc.send();
            }

        } catch (IOException ex)
        {
            form.append("Exception: " + ex.getMessage());
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

