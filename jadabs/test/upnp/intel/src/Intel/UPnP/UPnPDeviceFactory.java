// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   UPnPDeviceFactory.java

package Intel.UPnP;

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.net.*;
import java.util.*;

// Referenced classes of package Intel.UPnP:
//            LifeTimeMonitor, HTTPMessage, HTTPSession, UPnPService, 
//            ThreadPool, UPnPDevice

public class UPnPDeviceFactory
{
    private class DeviceRequestListener
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            if(e.getID() == HTTPSession.SESSION_CONNECT)
            {
                HTTPSession s = (HTTPSession)e.getSource();
                s.Send((HTTPMessage)s.StateObject);
            }
            if(e.getID() == HTTPSession.SESSION_CONNECT_FAILED)
            {
                Lifetime.Remove(_temp);
                TriggerEvent(new FactoryEvent(_temp, _temp.DUrl, new Exception("Cannot connect to ".concat(String.valueOf(String.valueOf(((HTTPMessage)((HTTPSession)e.getSource()).StateObject).GetTag("Host")))))));
            }
            if(e.getID() == HTTPSession.SESSION_DATA_AVAILABLE)
            {
                HTTPSession.SessionEvent se = (HTTPSession.SessionEvent)e;
                HTTPMessage msg = (HTTPMessage)se.StateObject;
                HandleRequest(msg, (HTTPSession)e.getSource());
            }
        }

        private DeviceRequestListener()
        {
        }

        DeviceRequestListener(._cls1 x$1)
        {
            this();
        }
    }

    private class Connector
        implements ThreadPool.ThreadPoolRunnable
    {

        public void run(Object state)
        {
            HTTPMessage req = (HTTPMessage)state;
            InetAddress d = null;
            try
            {
                d = InetAddress.getByName(((URL)req.StateObject).getHost());
            }
            catch(Exception e)
            {
                Lifetime.Remove(_temp);
                TriggerEvent(new FactoryEvent(_temp, _temp.DUrl, new Exception("Cannot resolve ".concat(String.valueOf(String.valueOf(((URL)req.StateObject).getHost()))))));
                return;
            }
            int p = ((URL)req.StateObject).getPort();
            WebSession = new HTTPSession(null, 0, d, p, new DeviceRequestListener(null), POOL, req);
        }

        private Connector()
        {
        }

        Connector(._cls1 x$1)
        {
            this();
        }
    }

    private class ServiceRequestListener
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            if(e.getID() == HTTPSession.SESSION_CONNECT)
            {
                HTTPSession s = (HTTPSession)e.getSource();
                s.Send((HTTPMessage)s.StateObject);
            }
            if(e.getID() == HTTPSession.SESSION_CONNECT_FAILED)
            {
                Lifetime.Remove(_temp);
                TriggerEvent(new FactoryEvent(_temp, _temp.DUrl, new Exception("Cannot connect to ".concat(String.valueOf(String.valueOf(((HTTPMessage)((HTTPSession)e.getSource()).StateObject).GetTag("Host")))))));
            }
            if(e.getID() == HTTPSession.SESSION_DATA_AVAILABLE)
            {
                HTTPSession.SessionEvent se = (HTTPSession.SessionEvent)e;
                HTTPMessage msg = (HTTPMessage)se.StateObject;
                HandleService(msg, (HTTPSession)e.getSource());
            }
        }

        private ServiceRequestListener()
        {
        }

        ServiceRequestListener(._cls1 x$1)
        {
            this();
        }
    }

    private class LifetimeListener
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            LifeTimeMonitor.LifeTimeMonitorEvent ee = (LifeTimeMonitor.LifeTimeMonitorEvent)e;
            TriggerEvent(new FactoryEvent(ee.State, ((UPnPDeviceFactory)ee.State).DUrl, new Exception("Could not load all documents")));
        }

        private LifetimeListener()
        {
        }

        LifetimeListener(._cls1 x$1)
        {
            this();
        }
    }

    private class factoryListener
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            if(e.getID() == UPnPDeviceFactory.DEVICE_CREATED)
                HandleFactory((UPnPDeviceFactory)e.getSource(), ((FactoryEvent)e).device, ((UPnPDeviceFactory)e.getSource()).DUrl);
            if(e.getID() == UPnPDeviceFactory.DEVICE_CREATED_FAILED)
                TriggerEvent(new FactoryEvent(_temp, ((UPnPDeviceFactory)e.getSource()).DUrl, ((FactoryEvent)e).FailedReason));
        }

        private factoryListener()
        {
        }

        factoryListener(._cls1 x$1)
        {
            this();
        }
    }

    public class FactoryEvent extends ActionEvent
    {

        public Exception FailedReason;
        public String DeviceLocation;
        public UPnPDevice device;

        public FactoryEvent(Object src, int id, String c)
        {
            super(src, id, c);
            FailedReason = null;
            DeviceLocation = null;
            device = null;
        }

        public FactoryEvent(Object src, String l, Exception e)
        {
            super(src, UPnPDeviceFactory.DEVICE_CREATED_FAILED, "DEVICE_CREATED_FAILED");
            FailedReason = null;
            DeviceLocation = null;
            device = null;
            FailedReason = e;
        }

        public FactoryEvent(Object src, String l, UPnPDevice d)
        {
            super(src, UPnPDeviceFactory.DEVICE_CREATED, "DEVICE_CREATED");
            FailedReason = null;
            DeviceLocation = null;
            device = null;
            device = d;
        }
    }


    public void addActionListener(ActionListener a)
    {
        EventList.add(a);
    }

    public void TriggerEvent(ActionEvent e)
    {
        Object x[] = EventList.toArray();
        for(int i = 0; i < x.length; i++)
            ((ActionListener)x[i]).actionPerformed(e);

    }

    public UPnPDeviceFactory(ThreadPool pool)
    {
        EventList = new ArrayList();
        Lifetime = new LifeTimeMonitor();
        port = 0;
        CBLock = new Object();
        CreateTable = Collections.synchronizedMap(new HashMap());
        SessionTable = Collections.synchronizedMap(new HashMap());
        POOL = pool;
        _temp = this;
        Lifetime.addActionListener(new LifetimeListener(null));
    }

    public UPnPDeviceFactory(ThreadPool pool, URL DescLocation, int MaxSeconds, ActionListener al)
    {
        this(pool);
        DUrl = DescLocation.toString();
        HTTPMessage req = new HTTPMessage();
        req.Directive = "GET";
        req.AddTag("Host", String.valueOf(String.valueOf((new StringBuffer(String.valueOf(String.valueOf(DescLocation.getHost())))).append(":").append(String.valueOf(DescLocation.getPort())))));
        req.DirectiveObj = URLDecoder.decode(DescLocation.getPath());
        req.StateObject = DescLocation;
        MaxAge = MaxSeconds;
        addActionListener(al);
        Lifetime.Add(this, 25);
        POOL.QueueUserWorkItem(new Connector(null), req);
    }

    protected void HandleRequest(HTTPMessage msg, HTTPSession TheSession)
    {
        TheSession.removeAllActionListeners();
        if(msg.StatusCode == 200)
        {
            TempDevice = UPnPDevice.Parse(msg.GetStringBuffer());
            TempDevice.ExpirationTimeout = MaxAge;
            if(TempDevice != null)
            {
                ServiceNum = FetchServiceCount(TempDevice);
                if(ServiceNum == 0)
                {
                    Lifetime.Remove(this);
                    TriggerEvent(new FactoryEvent(this, DUrl, TempDevice));
                } else
                {
                    FetchServiceDocuments(TempDevice, TheSession.getRemoteIP(), TheSession.getRemotePort());
                }
            }
        } else
        {
            TriggerEvent(new FactoryEvent(this, DUrl, new Exception("Device returned HTTP fault: ".concat(String.valueOf(String.valueOf(msg.StatusData))))));
        }
        TheSession.Close();
    }

    protected int FetchServiceCount(UPnPDevice device)
    {
        int Count = 0;
        Count = device.Services.length;
        if(device.EmbeddedDevices.length > 0)
        {
            for(int x = 0; x < device.EmbeddedDevices.length; x++)
                Count += FetchServiceCount(device.EmbeddedDevices[x]);

        }
        return Count;
    }

    protected void FetchServiceDocuments(UPnPDevice device, InetAddress RemoteIP, int RemotePort)
    {
        for(int x = 0; x < device.Services.length; x++)
        {
            HTTPMessage request2 = new HTTPMessage();
            URL TempUri;
            try
            {
                TempUri = new URL(device.Services[x].SCPDURL);
            }
            catch(Exception e)
            {
                TempUri = null;
            }
            if(TempUri != null)
            {
                request2.Directive = "GET";
                request2.DirectiveObj = URLDecoder.decode(TempUri.getPath());
                request2.AddTag("Host", String.valueOf(String.valueOf((new StringBuffer(String.valueOf(String.valueOf(RemoteIP.getHostAddress())))).append(":").append(String.valueOf(RemotePort)))));
                request2.StateObject = device.Services[x];
                HTTPSession TempSession = new HTTPSession(null, 0, RemoteIP, RemotePort, new ServiceRequestListener(null), POOL, request2);
                SessionTable.put(TempSession, TempSession);
            }
        }

        if(device.EmbeddedDevices.length > 0)
        {
            for(int y = 0; y < device.EmbeddedDevices.length; y++)
                FetchServiceDocuments(device.EmbeddedDevices[y], RemoteIP, RemotePort);

        }
    }

    protected void HandleService(HTTPMessage msg, HTTPSession TheSession)
    {
        TheSession.Close();
        int id = 0;
        boolean IsOK = false;
        synchronized(CBLock)
        {
            if(msg.StatusCode == 200)
            {
                if(TheSession.StateObject == null)
                    return;
                UPnPService z = (UPnPService)((HTTPMessage)TheSession.StateObject).StateObject;
                boolean flag;
                if(z.ParentDevice.FriendlyName.startsWith("X10"))
                    flag = true;
                ((UPnPService)((HTTPMessage)TheSession.StateObject).StateObject).ParseSCPD(msg.GetStringBuffer());
                ServiceNum--;
                if(ServiceNum == 0)
                    IsOK = true;
            } else
            {
                System.out.println(msg.StatusData);
            }
        }
        if(IsOK)
        {
            Lifetime.Remove(_temp);
            TriggerEvent(new FactoryEvent(this, DUrl, TempDevice));
        }
    }

    public void CreateDevice(URL DescLocation, int MaxSeconds)
    {
        UPnPDeviceFactory x = new UPnPDeviceFactory(POOL, DescLocation, MaxSeconds, new factoryListener(null));
        CreateTable.put(x, x);
    }

    protected void HandleFactory(UPnPDeviceFactory Factory, UPnPDevice device, String URL)
    {
        CreateTable.remove(Factory);
        Factory.Shutdown();
        TriggerEvent(new FactoryEvent(this, URL, device));
    }

    public void Shutdown()
    {
        WebSession.Close();
    }

    protected ArrayList EventList;
    protected HTTPSession WebSession;
    protected UPnPDevice TempDevice;
    protected LifeTimeMonitor Lifetime;
    protected int MaxAge;
    protected String DUrl;
    protected int port;
    protected UPnPDeviceFactory _temp;
    protected Object CBLock;
    protected Map CreateTable;
    protected Map SessionTable;
    protected int ServiceNum;
    protected ThreadPool POOL;
    public static int DEVICE_CREATED = 1;
    public static int DEVICE_CREATED_FAILED = -1;

}
