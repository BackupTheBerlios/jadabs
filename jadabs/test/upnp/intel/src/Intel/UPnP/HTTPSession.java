// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   HTTPSession.java

package Intel.UPnP;

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

// Referenced classes of package Intel.UPnP:
//            AsyncSocket, HTTPMessage, ThreadPool

public class HTTPSession
{
    public class SessionEvent extends ActionEvent
    {

        public Object StateObject;

        public SessionEvent(Object src, int id, String cmd, Object State)
        {
            super(src, id, cmd);
            StateObject = State;
        }
    }

    protected class SessionListener
        implements ActionListener
    {

        public void actionPerformed(ActionEvent Args)
        {
            AsyncSocket.SocketEvent EventArg = (AsyncSocket.SocketEvent)Args;
            int id = EventArg.getID();
            if(id == HTTPSession.SESSION_CONNECT)
                ConnectSink();
            if(id == HTTPSession.SESSION_CONNECT_FAILED)
                ConnectSinkFailed();
            if(id == HTTPSession.SESSION_DATA_AVAILABLE)
                ReadSink(EventArg.BytesRead, EventArg.Buffer);
        }

        protected SessionListener()
        {
        }
    }


    public void addActionListener(ActionListener a)
    {
        ActionList.add(a);
    }

    public void removeAllActionListeners()
    {
        ActionList.clear();
    }

    protected void SetEvent(LinkedList TheList, ActionEvent EventArg)
    {
        ActionListener a;
        for(ListIterator enum = TheList.listIterator(); enum.hasNext(); a.actionPerformed(EventArg))
            a = (ActionListener)enum.next();

    }

    public HTTPSession(InetAddress localAddress, int localPort, InetAddress remoteAddress, int remotePort, ActionListener cb, ThreadPool pool, Object State)
    {
        ActionList = new LinkedList();
        addActionListener(cb);
        StateObject = State;
        MainBuffer = new byte[4096];
        Buffer = new ByteArrayOutputStream();
        TotalShouldRead = 0;
        MainSocket = new AsyncSocket(true, pool);
        MainSocket.addActionListenter(new SessionListener());
        MainSocket.ConnectTo(remoteAddress, remotePort);
    }

    public HTTPSession(Socket TheSocket, ThreadPool pool, ActionListener a)
    {
        ActionList = new LinkedList();
        MainBuffer = new byte[4096];
        Buffer = new ByteArrayOutputStream();
        TotalShouldRead = 0;
        if(a != null)
            addActionListener(a);
        MainSocket = new AsyncSocket(TheSocket, pool);
        MainSocket.addActionListenter(new SessionListener());
        MainSocket.Begin();
    }

    public InetAddress getRemoteIP()
    {
        return MainSocket.getRemoteIP();
    }

    public int getRemotePort()
    {
        return MainSocket.getRemotePort();
    }

    public InetAddress getSourceIP()
    {
        return MainSocket.getSourceIP();
    }

    public int getSourcePort()
    {
        return MainSocket.getSourcePort();
    }

    public int getSessionID()
    {
        return MainSocket.hashCode();
    }

    protected void ConnectSink()
    {
        SetEvent(ActionList, new SessionEvent(this, SESSION_CONNECT, "SESSION_CONNECT", StateObject));
        MainSocket.Begin();
    }

    protected void ConnectSinkFailed()
    {
        SetEvent(ActionList, new SessionEvent(this, SESSION_CONNECT_FAILED, "SESSION_CONNECT_FAILED", StateObject));
    }

    protected void ReadSink(int BytesReceived, byte buffer[])
    {
        Buffer.write(buffer, 0, BytesReceived);
        ProcessBuffer();
    }

    protected void ProcessBuffer()
    {
        int BSR = 0;
        boolean DONE = false;
        do
        {
            if(DONE)
                break;
            byte b[] = Buffer.toByteArray();
            BSR = HTTPMessage.SizeToRead(b, 0, b.length);
            if(BSR == 0)
            {
                BSR = b.length;
                DONE = true;
            }
            if(BSR <= Buffer.size())
            {
                byte WorkBuffer[] = new byte[BSR];
                System.arraycopy(b, 0, WorkBuffer, 0, BSR);
                SetEvent(ActionList, new SessionEvent(this, 10, "READ", HTTPMessage.Parse(WorkBuffer)));
                int oldSize = WorkBuffer.length;
                WorkBuffer = new byte[Buffer.size() - oldSize];
                if(WorkBuffer.length > 0)
                    System.arraycopy(b, oldSize, WorkBuffer, 0, WorkBuffer.length);
                Buffer.reset();
                try
                {
                    Buffer.write(WorkBuffer);
                }
                catch(Exception exception) { }
                if(Buffer.size() == 0)
                    DONE = true;
            } else
            {
                DONE = true;
            }
        } while(true);
    }

    public void Send(HTTPMessage Packet)
    {
        MainSocket.Send(Packet.GetRawPacket());
    }

    public void Close()
    {
        MainSocket.Close();
    }

    protected LinkedList ActionList;
    protected AsyncSocket MainSocket;
    protected byte MainBuffer[];
    protected ByteArrayOutputStream Buffer;
    protected int TotalShouldRead;
    public Object StateObject;
    public static int SESSION_CONNECT = 0;
    public static int SESSION_CONNECT_FAILED = 1;
    public static int SESSION_DATA_AVAILABLE = 10;

}
