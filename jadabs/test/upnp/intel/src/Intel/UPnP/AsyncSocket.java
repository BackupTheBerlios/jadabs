// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AsyncSocket.java

package Intel.UPnP;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.*;
import javax.swing.JOptionPane;

// Referenced classes of package Intel.UPnP:
//            ThreadPool

public class AsyncSocket
{
    protected class ReaderClass
        implements ThreadPool.ThreadPoolRunnable
    {

        public void run(Object state)
        {
            ReadSink(state);
        }

        protected ReaderClass()
        {
        }
    }

    protected class ConnectorClass
        implements ThreadPool.ThreadPoolRunnable
    {

        public void run(Object state)
        {
            ConnectSink(state);
        }

        protected ConnectorClass()
        {
        }
    }

    public class SocketEvent extends ActionEvent
    {

        public int BytesRead;
        public byte Buffer[];
        public InetAddress remoteAddr;
        public InetAddress localAddr;
        public int remotePort;
        public int localPort;

        public SocketEvent(Object src, InetAddress _remoteIP, int _remotePort)
        {
            super(src, AsyncSocket.SOCKET_CONNECT_FAILED, "SOCKET_CONNECT_FAILED");
            BytesRead = 0;
            localAddr = null;
            localPort = -1;
            remoteAddr = _remoteIP;
            remotePort = _remotePort;
        }

        public SocketEvent(Object src, int id, String cmd, InetAddress localaddr, InetAddress remoteaddr, int localport, 
                int remoteport)
        {
            super(src, id, cmd);
            BytesRead = 0;
            BytesRead = -1;
            Buffer = null;
            localAddr = localaddr;
            remoteAddr = remoteaddr;
            localPort = localport;
            remotePort = remoteport;
        }

        public SocketEvent(Object src, int id, String cmd, int bytesRead, byte buffer[])
        {
            super(src, id, cmd);
            BytesRead = 0;
            BytesRead = bytesRead;
            Buffer = buffer;
            localAddr = ((AsyncSocket)src).getSourceIP();
            localPort = ((AsyncSocket)src).getSourcePort();
            remoteAddr = ((AsyncSocket)src).getRemoteIP();
            remotePort = ((AsyncSocket)src).getRemotePort();
        }

        public SocketEvent(Object src, int id, String cmd, int bytesRead, byte buffer[], InetAddress fromIP, 
                int fromPort)
        {
            super(src, id, cmd);
            BytesRead = 0;
            BytesRead = bytesRead;
            Buffer = buffer;
            localAddr = ((AsyncSocket)src).getSourceIP();
            localPort = ((AsyncSocket)src).getSourcePort();
            remoteAddr = fromIP;
            remotePort = fromPort;
        }
    }


    public AsyncSocket(boolean IsTCP, ThreadPool pool)
    {
        ActionList = new LinkedList();
        ReaderCallback = new ReaderClass();
        buffer = new byte[4096];
        TCPSocket = IsTCP;
        POOL = pool;
    }

    public AsyncSocket(Socket TheSocket, ThreadPool pool)
    {
        ActionList = new LinkedList();
        ReaderCallback = new ReaderClass();
        buffer = new byte[4096];
        TCPSocket = true;
        POOL = pool;
        TSocket = TheSocket;
        DSocket = null;
    }

    public AsyncSocket(DatagramSocket TheSocket, ThreadPool pool)
    {
        ActionList = new LinkedList();
        ReaderCallback = new ReaderClass();
        buffer = new byte[4096];
        TCPSocket = false;
        POOL = pool;
        TSocket = null;
        DSocket = TheSocket;
    }

    public InetAddress getSourceIP()
    {
        if(TSocket != null)
            return TSocket.getLocalAddress();
        else
            return DSocket.getLocalAddress();
    }

    public InetAddress getRemoteIP()
    {
        if(TSocket != null)
            return TSocket.getInetAddress();
        else
            return null;
    }

    public int getSourcePort()
    {
        if(TSocket != null)
            return TSocket.getLocalPort();
        else
            return DSocket.getLocalPort();
    }

    public int getRemotePort()
    {
        if(TSocket != null)
            return TSocket.getPort();
        else
            return -1;
    }

    public void addActionListenter(ActionListener a)
    {
        ActionList.add(a);
    }

    protected void SetEvent(LinkedList TheList, ActionEvent EventArg)
    {
        ActionListener a;
        for(ListIterator enum = TheList.listIterator(); enum.hasNext(); a.actionPerformed(EventArg))
            a = (ActionListener)enum.next();

    }

    public void ConnectSink(Object StateObject)
    {
        Object state[] = (Object[])StateObject;
        InetAddress IPAddress = (InetAddress)state[0];
        int Port = ((Integer)state[1]).intValue();
        try
        {
            TSocket = new Socket(IPAddress, Port);
            SetEvent(ActionList, new SocketEvent(this, SOCKET_CONNECTED, "SOCKET_CONNECTED", 0, null));
        }
        catch(Exception e)
        {
            SetEvent(ActionList, new SocketEvent(this, IPAddress, Port));
        }
    }

    public void Close()
    {
        if(TSocket != null)
            try
            {
                TSocket.close();
            }
            catch(Exception exception) { }
    }

    public void ConnectTo(InetAddress IPAddress, int Port)
    {
        Object StateObject[] = new Object[2];
        StateObject[0] = IPAddress;
        StateObject[1] = new Integer(Port);
        POOL.QueueUserWorkItem(new ConnectorClass(), ((Object) (StateObject)));
    }

    public void SendTo(byte SendBuffer[], InetAddress destAddr, int destPort)
    {
        if(DSocket != null)
            try
            {
                DatagramPacket p = new DatagramPacket(SendBuffer, SendBuffer.length, destAddr, destPort);
                DSocket.send(p);
            }
            catch(Exception e)
            {
                System.exit(-1);
            }
    }

    public void Send(byte SendBuffer[])
    {
        if(TSocket != null)
            try
            {
                TSocket.getOutputStream().write(SendBuffer);
            }
            catch(Exception e)
            {
                JOptionPane.showMessageDialog(null, "SocketException");
                System.exit(-1);
            }
    }

    public void Begin()
    {
        POOL.QueueUserWorkItem(ReaderCallback, null);
    }

    public void ReadSink(Object state)
    {
        DatagramPacket tp = new DatagramPacket(new byte[8192], 8192);
        byte byte0;
        if(TSocket != null)
            try
            {
                TSocket.setSoTimeout(25);
                InputStream s = TSocket.getInputStream();
                int BytesRead = s.read(buffer);
                if(BytesRead <= 0)
                {
                    Thread.currentThread();
                    Thread.sleep(1L);
                    throw new InterruptedException("Read 0 bytes");
                }
                SetEvent(ActionList, new SocketEvent(this, SOCKET_DATA_AVAILABLE, "SOCKET_DATA_AVAILABLE", BytesRead, buffer));
            }
            catch(SocketException ss)
            {
                SetEvent(ActionList, new SocketEvent(this, SOCKET_DISCONNECTED, "SOCKET_DISCONNECTED", getSourceIP(), getRemoteIP(), getSourcePort(), getRemotePort()));
                return;
            }
            catch(Exception e)
            {
                byte0 = 0;
            }
        if(DSocket != null)
            try
            {
                DSocket.setSoTimeout(25);
                DSocket.receive(tp);
                int zzz = DSocket.getReceiveBufferSize();
                SetEvent(ActionList, new SocketEvent(this, SOCKET_DATA_AVAILABLE, "SOCKET_DATA_AVAILABLE", tp.getLength(), tp.getData(), tp.getAddress(), tp.getPort()));
            }
            catch(SocketException s)
            {
                return;
            }
            catch(Exception e)
            {
                byte0 = 5;
            }
        POOL.QueueUserWorkItem(ReaderCallback, null);
    }

    public static int SOCKET_CONNECTED = 0;
    public static int SOCKET_CONNECT_FAILED = 1;
    public static int SOCKET_DISCONNECTED = -1;
    public static int SOCKET_DATA_AVAILABLE = 10;
    protected boolean TCPSocket;
    protected Socket TSocket;
    protected DatagramSocket DSocket;
    protected ThreadPool POOL;
    protected LinkedList ActionList;
    protected ReaderClass ReaderCallback;
    protected byte buffer[];

}
