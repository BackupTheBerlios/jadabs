// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   UPnPControlPoint.java

package Intel.UPnP;

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

// Referenced classes of package Intel.UPnP:
//            NetworkInfo, LifeTimeMonitor, SSDP, SSDPNotifyEvent, 
//            HTTPMessage, AsyncSocket, ThreadPool, ManualResetEvent, 
//            UPnPDevice

public class UPnPControlPoint
{
    private class NetInfoListener
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            NetworkInfo.NetworkEvent ne = (NetworkInfo.NetworkEvent)e;
            switch(ne.getID())
            {
            case 0: // '\0'
                NewInterface((NetworkInfo)e.getSource(), ne.addr);
                // fall through

            case 1: // '\001'
            default:
                return;
            }
        }

        private NetInfoListener()
        {
        }

        NetInfoListener(._cls1 x$1)
        {
            this();
        }
    }

    private class FindListener
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            AsyncSocket.SocketEvent se = (AsyncSocket.SocketEvent)e;
            if(e.getID() == AsyncSocket.SOCKET_DATA_AVAILABLE)
            {
                HTTPMessage msg = HTTPMessage.Parse(se.Buffer, 0, se.BytesRead);
                HandleAsyncSearch((AsyncSocket)e.getSource(), msg, se.remoteAddr, se.remotePort);
            }
        }

        private FindListener()
        {
        }

        FindListener(._cls1 x$1)
        {
            this();
        }
    }

    private class SSDPListener
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            switch(e.getID())
            {
            case 1: // '\001'
                SSDPNotifyEvent ne = (SSDPNotifyEvent)e;
                HandleNotify((SSDP)e.getSource(), ne.Source, ne.SourcePort, ne.LocationURL, ne.Alive, ne.UniqueName, ne.SearchTarget, ne.MaxAge);
                break;
            }
        }

        private SSDPListener()
        {
        }

        SSDPListener(._cls1 x$1)
        {
            this();
        }
    }

    private class LifetimeListener
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            LifeTimeMonitor.LifeTimeMonitorEvent lme = (LifeTimeMonitor.LifeTimeMonitorEvent)e;
        }

        private LifetimeListener()
        {
        }

        LifetimeListener(._cls1 x$1)
        {
            this();
        }
    }

    protected class DeviceNode
    {

        public UPnPDevice TheDevice;
        public URL URL;

        protected DeviceNode()
        {
        }
    }

    protected class SearchNode
    {

        public String SearchTarget;
        public URL Location;
        public InetAddress LocalAdd;
        public int LocalPort;
        public InetAddress ResponseAdd;
        public int ResponsePort;
        public String USN;
        public int MaxAge;

        protected SearchNode()
        {
        }
    }

    public class SearchResult extends ActionEvent
    {

        public InetAddress ResponseIP;
        public int ResponsePort;
        public InetAddress LocalIP;
        public URL Location;
        public String USN;
        public String ST;
        public int MaxAge;

        public SearchResult(Object src, int id, String cmd, InetAddress fromIP, int fromPort, InetAddress recIP, 
                URL DescriptionLocation, String _USN, String _ST, int _MaxAge)
        {
            super(src, id, cmd);
            ResponseIP = fromIP;
            ResponsePort = fromPort;
            LocalIP = recIP;
            Location = DescriptionLocation;
            USN = _USN;
            ST = _ST;
            MaxAge = _MaxAge;
        }
    }


    public void TriggerEvent(ActionEvent e)
    {
        Object a[] = EventList.toArray();
        for(int i = 0; i < a.length; i++)
            ((ActionListener)a[i]).actionPerformed(e);

    }

    public void addActionListener(ActionListener e)
    {
        EventList.add(e);
    }

    public UPnPControlPoint(ThreadPool pool)
    {
        EventList = new ArrayList();
        POOL = pool;
        CreateTable = Collections.synchronizedMap(new HashMap());
        SSDPTable = Collections.synchronizedMap(new HashMap());
        NetInfo = new NetworkInfo(new NetInfoListener(null));
        SyncData = Collections.synchronizedList(new ArrayList());
        SSDPSessions = Collections.synchronizedMap(new HashMap());
        Lifetime = new LifeTimeMonitor();
        Lifetime.addActionListener(new LifetimeListener(null));
    }

    protected void NewInterface(NetworkInfo sender, InetAddress Intfce)
    {
        try
        {
            SSDP SSDPServer = new SSDP(Intfce, 1900, 65535, POOL);
            SSDPServer.addActionListener(new SSDPListener(null));
            SSDPTable.put(Intfce.getHostAddress(), SSDPServer);
            SSDPServer.Start();
        }
        catch(Exception exception) { }
    }

    protected void HandleNotify(SSDP source, InetAddress localaddr, int localport, URL LocationURL, boolean IsAlive, String USN, String ST, 
            int MaxAge)
    {
        TriggerEvent(new SSDPNotifyEvent(this, 1, "OnNotify", localaddr, localport, IsAlive, LocationURL, USN, ST, MaxAge));
    }

    public void FindDeviceAsync(String SearchTarget)
    {
        HTTPMessage request = new HTTPMessage();
        request.Directive = "M-SEARCH";
        request.DirectiveObj = "*";
        request.AddTag("HOST", "239.255.255.250:1900");
        request.AddTag("MAN", "\"ssdp:discover\"");
        request.AddTag("MX", "5");
        request.AddTag("ST", SearchTarget);
        InetAddress LocalAddresses[] = NetInfo.GetLocalAddresses();
        InetAddress mcg = null;
        try
        {
            mcg = InetAddress.getByName("239.255.255.250");
        }
        catch(Exception exception) { }
        for(int id = 0; id < LocalAddresses.length; id++)
            try
            {
                MulticastSocket SendSocket = new MulticastSocket(0);
                SendSocket.joinGroup(mcg);
                SendSocket.setInterface(LocalAddresses[id]);
                AsyncSocket as = new AsyncSocket(SendSocket, POOL);
                SSDPSessions.put(as, as);
                as.addActionListenter(new FindListener(null));
                as.Begin();
                as.SendTo(request.GetRawPacket(), mcg, 1900);
                as.SendTo(request.GetRawPacket(), mcg, 1900);
                Lifetime.Add(as, 3);
            }
            catch(Exception exception1) { }

    }

    protected void HandleAsyncSearch(AsyncSocket sender, HTTPMessage msg, InetAddress originIP, int originPort)
    {
        String USN = "";
        String ST = "";
        int MaxAge = 0;
        USN = msg.GetTag("USN");
        USN = USN.substring(USN.indexOf(":") + 1);
        if(USN.indexOf("::") != -1)
        {
            ST = USN.substring(USN.lastIndexOf("::") + 2);
            USN = USN.substring(0, USN.indexOf("::"));
        }
        String ma = msg.GetTag("Cache-Control").trim();
        if(ma.compareTo("") != 0)
            MaxAge = Integer.parseInt(ma.substring(ma.indexOf("=") + 1).trim());
        URL Location;
        try
        {
            Location = new URL(msg.GetTag("Location"));
        }
        catch(Exception l)
        {
            Location = null;
        }
        TriggerEvent(new SearchResult(this, UPNP_SEARCH_RESULT, "UPNP_SEARCH_RESULT", originIP, originPort, sender.getSourceIP(), Location, USN, ST, MaxAge));
    }

    protected ArrayList EventList;
    protected ThreadPool POOL;
    protected NetworkInfo NetInfo;
    protected java.util.List SyncData;
    protected ManualResetEvent SyncCallback;
    protected DeviceNode SyncDevice;
    protected Map CreateTable;
    protected Map SSDPTable;
    protected Map SSDPSessions;
    protected LifeTimeMonitor Lifetime;
    public static int UPNP_NOTIFY = 1;
    public static int UPNP_SEARCH_RESULT = 10;

}
