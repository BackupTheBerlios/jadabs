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

public class OriginatingINVITE extends MIDlet implements CommandListener, SipClientConnectionListener,
        SipServerConnectionListener
{

    private Display display;

    private long startTime;

    private Form form;

    private TextField address;

    private Command startCmd;

    private Command restartCmd;

    private Command byeCmd;

    private Command exitCmd;

    private SipDialog dialog;

    private StringItem str;

    // <i><b>using static SDP content as an example</b></i>
    private String sdp = "v=0\no=sippy 2890844730 2890844732 IN IP4 host.example.com\ns=example code\nc=IN IP4 host.example.com\nt=0 0\nm=message 54344 SIP/TCP\na=user:sippy";

    public OriginatingINVITE()
    {
        // <i><b>Initialize MIDlet display</b></i>
        display = Display.getDisplay(this);
        // <i><b>create a Form for progess info printings</b></i>
        form = new Form("Session example");
        address = new TextField("INVITE:", "sip:user@10.128.0.50:5070", 40, TextField.LAYOUT_LEFT);
        form.append(address);
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
                    startSession();
                }
            };
            t.start();

            //	    startSession();
            return;
        } else if (c == exitCmd)
        {
            destroyApp(true);
            return;
        } else if (c == byeCmd)
        {
            form.removeCommand(byeCmd);
            form.addCommand(restartCmd);
            sendBYE();
            return;
        } else if (c == restartCmd)
        {
            stopListener();
            form.removeCommand(restartCmd);
            form.addCommand(startCmd);
            form.deleteAll();
            form.append(address);
            return;
        }
    }

    public void startApp()
    {
        display.setCurrent(form);
    }

    private void startSession()
    {
        SipClientConnection scc = null;
        try
        {
            // <i><b>start a listener for incoming requests</b></i>
            startListener();
            // <i><b>open SIP connection with remote user</b></i>
            scc = (SipClientConnection) Connector.open(address.getString());
            scc.setListener(this);
            // <i><b>initialize INVITE request</b></i>
            scc.initRequest("INVITE", scn);
            scc.setHeader("Content-Length", "" + sdp.length());
            scc.setHeader("Content-Type", "application/sdp");
            OutputStream os = scc.openContentOutputStream();
            os.write(sdp.getBytes());
            os.close(); // <i><b>close and send</b></i>
            str = new StringItem("Inviting... ", scc.getHeader("To"));
            form.append(str);
        } catch (Exception ex)
        {
            ex.printStackTrace();
            // <i><b>handle IOException</b></i>
        }
    }

    /**
     * Handle incoming response here
     */
    public void notifyResponse(SipClientConnection scc)
    {
        int statusCode = 0;
        boolean received = false;
        try
        {
            scc.receive(0); // <i><b>fetch resent response</b></i>
            statusCode = scc.getStatusCode();
            str = new StringItem("Response: ", statusCode + " " + scc.getReasonPhrase());
            form.append(str);
            if (statusCode < 200)
            {
                dialog = scc.getDialog();
                form.append("Early-Dialog state: " + dialog.getState());
            }
            if (statusCode == 200)
            {
                String contentType = scc.getHeader("Content-Type");
                String contentLength = scc.getHeader("Content-Length");
                int length = Integer.parseInt(contentLength);
                if (contentType.equals("application/sdp"))
                {
                    //
                    // <i><b>handle SDP here</b></i>
                    //
                }
                dialog = scc.getDialog(); // <i><b>save dialog info</b></i>
                form.append("Dialog state: " + dialog.getState());

                scc.initAck(); // <i><b>initialize and send ACK</b></i>
                scc.send();
                str = new StringItem("Session established: ", scc.getHeader("Call-ID"));
                form.append(str);
                scc.close();
            } else if (statusCode >= 300)
            {
                str = new StringItem("Session failed: ", scc.getHeader("Call-ID"));
                form.append(str);
                form.removeCommand(byeCmd);
                form.addCommand(restartCmd);
                scc.close();
            }
        } catch (IOException ioe)
        {
            // <i><b>handle e.g. transaction timeout here</b></i>
            str = new StringItem("No answer: ", ioe.getMessage());
            form.append(str);
            form.removeCommand(byeCmd);
            form.addCommand(restartCmd);
        }
    }

    /**
     * end session with BYE
     */
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
                    {
                        form.append("Session closed successfully...");
                        form.append("Dialog state: " + dialog.getState());
                    } else
                        form.append("Error: " + sc.getReasonPhrase());
                }
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

    public void shutdown()
    {
        destroyApp(false);
    }

    private SipConnectionNotifier scn;

    private SipServerConnection ssc = null;

    public void notifyRequest(SipConnectionNotifier sn)
    {
        try
        {
            ssc = scn.acceptAndOpen(); // <i><b>blocking</b></i>
            if (ssc.getMethod().equals("BYE"))
            {
                // <i><b>respond 200 OK to BYE</b></i>
                ssc.initResponse(200);
                ssc.send();
                str = new StringItem("Other side hang-up!", "");
                form.append(str);
            }
            form.append("Closing notifier...");
            form.removeCommand(byeCmd);
            form.addCommand(restartCmd);
            scn.close();
        } catch (IOException ex)
        {
            // <i><b>handle IOException</b></i>
        }
    }

    private void startListener()
    {
        try
        {
            if (scn != null)
                scn.close();
            // <i><b>listen to requests on port 5060</b></i>
            scn = (SipConnectionNotifier) Connector.open("sip:5080");
            scn.setListener(this);
        } catch (IOException ex)
        {
            // <i><b>handle IOException</b></i>
        }
    }

    private void stopListener()
    {
        try
        {
            if (scn != null)
                scn.close();
            scn = null;
        } catch (IOException ex)
        {
            // <i><b>handle IOException</b></i>
        }
    }
}

