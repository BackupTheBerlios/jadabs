// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SSDP.java

package Intel.UPnP;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.util.LinkedList;

// Referenced classes of package Intel.UPnP:
//            SSDPNotifyEvent, SSDPSearchEvent, ThreadPool, HTTPMessage

public class SSDP
    implements ThreadPool.ThreadPoolRunnable
{
    private class AutoRefresh
        implements Runnable
    {

        public void run()
        {
            long duration = ExpirationSeconds * 1000;
            duration = (long)((double)duration * 0.40000000000000002D);
            do
                try
                {
                    Thread.sleep(duration);
                    OnRefresh();
                }
                catch(Exception exception) { }
            while(true);
        }

        private AutoRefresh()
        {
        }

        AutoRefresh(._cls1 x$1)
        {
            this();
        }
    }


    public InetAddress getLocalIP()
    {
        return localip;
    }

    public SSDP(InetAddress IP, int PORT, int expiration, ThreadPool pool)
        throws Exception
    {
        localip = IP;
        POOL = pool;
        ReceiveSocket = new MulticastSocket(PORT);
        ReceiveSocket.setSoTimeout(25);
        EventList = new LinkedList();
        UnicastSocket = new DatagramSocket(0, IP);
        ExpirationSeconds = expiration;
        ReceiveSocket.setInterface(IP);
    }

    public void addActionListener(Object x)
    {
        EventList.add(x);
    }

    protected void OnRefresh()
    {
        Object list[] = EventList.toArray();
        for(int x = 0; x < list.length; x++)
            ((ActionListener)list[x]).actionPerformed(new ActionEvent(this, 0, "OnRefresh"));

    }

    protected void OnNotify(InetAddress src, int port, boolean Alive, URL locuri, String USN, String ST, int MaxAge)
    {
        Object list[] = EventList.toArray();
        for(int x = 0; x < list.length; x++)
            ((ActionListener)list[x]).actionPerformed(new SSDPNotifyEvent(this, 1, "OnNotify", src, port, Alive, locuri, USN, ST, MaxAge));

    }

    protected void OnSearch(InetAddress local, InetAddress src, int port, String ST)
    {
        Object list[] = EventList.toArray();
        for(int x = 0; x < list.length; x++)
            ((ActionListener)list[x]).actionPerformed(new SSDPSearchEvent(this, 2, "OnSearch", local, src, port, ST));

    }

    public void Start()
    {
        try
        {
            ReceiveSocket.joinGroup(InetAddress.getByName("239.255.255.250"));
            ReceiveSocket.setTimeToLive(4);
            POOL.QueueUserWorkItem(this, null);
            NotifyRefreshThread = new Thread(new AutoRefresh(null));
            NotifyRefreshThread.start();
        }
        catch(Exception exception) { }
    }

    public void Stop()
    {
        if(NotifyRefreshThread != null)
        {
            NotifyRefreshThread.interrupt();
            NotifyRefreshThread = null;
        }
        try
        {
            ReceiveSocket.leaveGroup(InetAddress.getByName("239.255.255.250"));
        }
        catch(Exception exception) { }
    }

    public void Multicast(HTTPMessage msg)
    {
        try
        {
            MulticastSocket msock = new MulticastSocket(1900);
            msock.setInterface(ReceiveSocket.getInterface());
            msock.setTimeToLive(4);
            InetAddress addr = InetAddress.getByName("239.255.255.250");
            msock.joinGroup(addr);
            byte buffer[] = msg.GetRawPacket();
            DatagramPacket p = new DatagramPacket(buffer, 0, buffer.length, addr, 1900);
            msock.send(p);
            msock.leaveGroup(addr);
        }
        catch(Exception exception) { }
    }

    public void Unicast(HTTPMessage msg, InetAddress destAddr, int destPort)
    {
        try
        {
            byte buffer[] = msg.GetRawPacket();
            DatagramPacket p = new DatagramPacket(buffer, 0, buffer.length, destAddr, destPort);
            UnicastSocket.send(p);
        }
        catch(Exception e)
        {
            return;
        }
    }

    public void run(Object state)
    {
        try
        {
            DatagramPacket p = new DatagramPacket(new byte[8192], 8192);
            ReceiveSocket.receive(p);
            UPnPMessage = HTTPMessage.Parse(p.getData(), p.getOffset(), p.getLength());
            ProcessPacket(UPnPMessage, p.getAddress(), p.getPort());
        }
        catch(Exception exception) { }
        POOL.QueueUserWorkItem(this, null);
    }

    protected void ProcessPacket(HTTPMessage msg, InetAddress SourceAddress, int SourcePort)
    {
        String ST = "";
        int MaxAge = 0;
        String USN = msg.GetTag("USN");
        USN = USN.substring(USN.indexOf(":") + 1);
        if(USN.indexOf("::") != -1)
        {
            ST = USN.substring(USN.lastIndexOf("::") + 2);
            USN = USN.substring(0, USN.indexOf("::"));
        }
        String NTS = msg.GetTag("NTS").toUpperCase();
        boolean Alive;
        if(NTS.compareTo("SSDP:ALIVE") == 0)
        {
            Alive = true;
            String ma = msg.GetTag("Cache-Control").trim();
            if(ma.compareTo("") != 0)
                MaxAge = Integer.parseInt(ma.substring(ma.indexOf("=") + 1).trim());
        } else
        {
            Alive = false;
        }
        if(msg.Directive.compareTo("NOTIFY") == 0)
        {
            URL locuri;
            try
            {
                locuri = new URL(msg.GetTag("Location"));
            }
            catch(Exception le)
            {
                locuri = null;
            }
            OnNotify(SourceAddress, SourcePort, Alive, locuri, USN, ST, MaxAge);
        }
        if(msg.Directive.compareTo("M-SEARCH") == 0)
            OnSearch(getLocalIP(), SourceAddress, SourcePort, msg.GetTag("ST"));
    }

    HTTPMessage UPnPMessage;
    ThreadPool POOL;
    protected MulticastSocket ReceiveSocket;
    protected DatagramSocket UnicastSocket;
    protected Thread NotifyRefreshThread;
    protected LinkedList EventList;
    protected int ExpirationSeconds;
    protected InetAddress localip;
}
