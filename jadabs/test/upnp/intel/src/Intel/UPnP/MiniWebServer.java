// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MiniWebServer.java

package Intel.UPnP;

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.util.*;

// Referenced classes of package Intel.UPnP:
//            HTTPSession, MiniWebServerAcceptor, HTTPRequestEvent, ThreadPool, 
//            HTTPMessage, AcceptSocketEvent

public class MiniWebServer
{
    private class SessionHandler
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            HTTPSession.SessionEvent se = (HTTPSession.SessionEvent)e;
            switch(se.getID())
            {
            case 10: // '\n'
                ReceiveSink((HTTPSession)se.getSource(), (HTTPMessage)se.StateObject);
                break;
            }
        }

        private SessionHandler()
        {
        }

        SessionHandler(._cls1 x$1)
        {
            this();
        }
    }

    private class RequestHandler
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            HandleRequest(((HTTPRequestEvent)e).HTTPRequest, ((HTTPRequestEvent)e).Session);
        }

        private RequestHandler()
        {
        }
    }

    private class AcceptHandler
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            HandleAccept(((AcceptSocketEvent)e).NewSocket);
        }

        private AcceptHandler()
        {
        }

        AcceptHandler(._cls1 x$1)
        {
            this();
        }
    }


    protected void HandleAccept(Socket NewSocket)
    {
        HTTPSession s = new HTTPSession(NewSocket, pool, new SessionHandler(null));
        SessionMap.put(s, s);
    }

    public String getLocalIP()
    {
        return ThisIP;
    }

    public int getLocalPort()
    {
        return ThisPort;
    }

    public MiniWebServer(String URL, ThreadPool POOL)
    {
        SessionMap = Collections.synchronizedMap(new HashMap());
        pool = POOL;
        try
        {
            URL _url = new URL(URL);
            ThisIP = _url.getHost();
            ThisPort = _url.getPort();
            EventList = new LinkedList();
        }
        catch(Exception exception) { }
    }

    public MiniWebServer(String IPAddress, int PortNum, ThreadPool POOL)
    {
        SessionMap = Collections.synchronizedMap(new HashMap());
        pool = POOL;
        ThisIP = IPAddress;
        ThisPort = PortNum;
        EventList = new LinkedList();
    }

    public boolean Start()
    {
        try
        {
            MainSocket = new ServerSocket(ThisPort, 25, InetAddress.getByName(ThisIP));
            MWSAcceptor = new MiniWebServerAcceptor(MainSocket);
            MWSAcceptor.addActionListener(new AcceptHandler(null));
            boolean flag = true;
            return flag;
        }
        catch(Exception e)
        {
            boolean flag1 = false;
            return flag1;
        }
    }

    public boolean Stop()
    {
        try
        {
            MWSAcceptor.dispose();
            boolean flag = true;
            return flag;
        }
        catch(Exception e)
        {
            boolean flag1 = false;
            return flag1;
        }
    }

    public void addActionListener(Object x)
    {
        EventList.add(x);
    }

    protected void ReceiveSink(HTTPSession sender, HTTPMessage msg)
    {
        SetEvent(msg, sender);
    }

    protected void SetEvent(HTTPMessage request, HTTPSession WebSession)
    {
        Object elist[] = EventList.toArray();
        for(int id = 0; id < elist.length; id++)
            ((ActionListener)elist[id]).actionPerformed(new HTTPRequestEvent(this, 0, "OnRequest", request, WebSession));

    }

    public void HandleRequest(HTTPMessage request, HTTPSession WebSession)
    {
        SetEvent(request, WebSession);
    }

    protected ServerSocket MainSocket;
    protected MiniWebServerAcceptor MWSAcceptor;
    protected LinkedList EventList;
    protected String ThisIP;
    protected int ThisPort;
    protected ThreadPool pool;
    protected Map SessionMap;
}
