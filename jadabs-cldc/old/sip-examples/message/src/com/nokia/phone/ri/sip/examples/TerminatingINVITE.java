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

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.sip.*;

public class TerminatingINVITE extends MIDlet implements CommandListener, SipServerConnectionListener
{

    private Display display;

    private long startTime;

    private Form form;

    private TextField receivePort;

    private Command startCmd;

    private Command restartCmd;

    private Command byeCmd;

    private Command exitCmd;

    private boolean active = false;

    private SipDialog dialog;

    private StringItem str;

    // <i><b>using static SDP as an example</b></i>
    private String sdp = "v=0\no=sippy 2890844730 2890844732 IN IP4 host.example.com\ns=example code\nc=IN IP4 host.example.com\nt=0 0\nm=message 54344 SIP/TCP\na=user:sippy";

    SipConnectionNotifier scn = null;

    public TerminatingINVITE()
    {
        // <i><b>Initialize MIDlet display</b></i>
        display = Display.getDisplay(this);
        form = new Form("Session example");
        receivePort = new TextField("SipConnectionNotifier on port:", "sip:5070", 30, TextField.LAYOUT_LEFT);
        form.append(receivePort);
        byeCmd = new Command("Bye", Command.ITEM, 1);
        restartCmd = new Command("Restart", Command.ITEM, 1);
        startCmd = new Command("Start", Command.ITEM, 1);
        form.addCommand(startCmd);
        exitCmd = new Command("Exit", Command.EXIT, 1);
        form.addCommand(exitCmd);
        form.setCommandListener(this);

    }

    public void commandAction(Command c, Displayable d)
    {
        if (c == startCmd)
        {
            form.deleteAll();
            form.removeCommand(startCmd);
            form.addCommand(byeCmd);

            Thread t = new Thread()
            {

                public void run()
                {
                    startListener();
                }
            };
            t.start();

            return;
        } else if (c == exitCmd)
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
            return;
        } else if (c == byeCmd)
        {
            sendBYE();
            form.removeCommand(byeCmd);
            form.addCommand(restartCmd);
            return;
        } else if (c == restartCmd)
        {
            form.removeCommand(restartCmd);
            form.addCommand(startCmd);
            form.deleteAll();
            form.append(receivePort);
            return;
        }
    }

    public void startApp()
    {
        display.setCurrent(form);
    }

    private void startListener()
    {

        try
        {
            if (scn != null)
                scn.close();
            // <i><b>start a listener for incoming request</b></i>
            scn = (SipConnectionNotifier) Connector.open(receivePort.getString());
            scn.setListener(this);
            form.append("Listening on port: " + scn.getLocalPort());
        } catch (IOException ex)
        {
            // <i><b>handle IOException</b></i>
        }
    }

    /**
     * Handle incoming Requests
     */
    public void notifyRequest(SipConnectionNotifier scn)
    {
        SipServerConnection ssc = null;
        try
        {
            ssc = scn.acceptAndOpen(); // <i><b>blocking</b></i>
            if (ssc.getMethod().equals("INVITE"))
            {
                // <i><b>handle content</b></i>
                String contentType = ssc.getHeader("Content-Type");
                String contentLength = ssc.getHeader("Content-Length");
                int length = Integer.parseInt(contentLength);
                if (contentType.equals("application/sdp"))
                {
                    InputStream is = ssc.openContentInputStream();
                    byte content[] = new byte[length];
                    is.read(content);
                    String sc = new String(content);
                    // <i><b>parse m= line from SDP, as an example</b></i>
                    int m = sc.indexOf("m=");
                    String media = sc.substring(m, sc.indexOf('\n', m));
                    str = new StringItem("media is: ", media);
                    form.append(str);
                    //
                    // <i><b>handle media here</b></i>
                    //

                    // <i><b>initialize and send 180 response</b></i>
                    ssc.initResponse(180);
                    ssc.send();
                    // <i><b>inform user about the session here...</b></i>
                }
                // <i><b>accept automatically and initialize 200
                // response</b></i>
                ssc.initResponse(200);
                ssc.setHeader("Content-Length", "" + sdp.length());
                ssc.setHeader("Content-Type", "application/sdp");
                OutputStream os = ssc.openContentOutputStream();
                os.write(sdp.getBytes());
                os.close(); // <i><b>close and send</b></i>
                // save Dialog
                dialog = ssc.getDialog();
                form.append("Dialog state: " + dialog.getState());
                ssc.close();

                // <i><b>Wait for otherside to ACK</b></i>
                form.append("Waiting for ACK...");
            } else if (ssc.getMethod().equals("ACK"))
            {
                str = new StringItem("Session established: ", ssc.getHeader("Call-ID"));
                form.append(str);
                ssc.close();
                form.append("Dialog state: " + dialog.getState());
                // <i><b>Wait for otherside to send BYE</b></i>
                form.append("Waiting for BYE...");
            } else if (ssc.getMethod().equals("BYE"))
            {
                ssc.initResponse(200);
                ssc.send();
                str = new StringItem("Session closed: ", ssc.getHeader("Call-ID"));
                form.append(str);
                form.append("Dialog state: " + dialog.getState());
                ssc.close();
                form.removeCommand(byeCmd);
                form.addCommand(restartCmd);
            } else if (ssc.getMethod().equals("CANCEL"))
            {
                ssc.initResponse(200);
                ssc.send();
                str = new StringItem("Session canceled: ", ssc.getHeader("Call-ID"));
                form.append(str);
                ssc.close();
                form.removeCommand(byeCmd);
                form.addCommand(restartCmd);
            }
        } catch (IOException ex)
        {
            ex.printStackTrace();
            // <i><b>handle IOException</b></i>
        }
    }

    private void sendBYE()
    {
        if (dialog != null)
        {
            try
            {
                SipClientConnection sc = dialog.getNewClientConnection("BYE");
                sc.send();
                str = new StringItem("user hang-up: ", "BYE sent...");
                form.append(str);
                boolean gotit = sc.receive(10000);
                if (gotit)
                {
                    if (sc.getStatusCode() == 200)
                        form.append("Session closed successfully...");
                    else
                        form.append("Error: " + sc.getReasonPhrase());
                }
                form.append("Dialog state: " + dialog.getState());
                sc.close();
            } catch (IOException iox)
            {
                form.append("Exception: " + iox.getMessage());
            }
        } else
        {
            form.append("No dialog information!");
        }
    }

    public void pauseApp()
    {
        // pause
    }

    public void destroyApp(boolean b)
    {
        notifyDestroyed();
    }

}

