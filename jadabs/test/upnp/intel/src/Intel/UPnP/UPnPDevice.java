// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   UPnPDevice.java

package Intel.UPnP;

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.net.*;
import java.util.*;

// Referenced classes of package Intel.UPnP:
//            ThreadPool, UPnPService, NetworkInfo, SSDP, 
//            MiniWebServer, HTTPMessage, HTTPSession, DText, 
//            XMLTextReader, UPnPArgument, StringFormatter, HTTPRequestEvent, 
//            SSDPSearchEvent

public class UPnPDevice
{
    private class HandleEventSession
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            switch(e.getID())
            {
            case 0: // '\0'
                EventSessionSink((HTTPSession)e.getSource(), ((HTTPSession.SessionEvent)e).StateObject);
                break;
            }
        }

        private HandleEventSession()
        {
        }

        HandleEventSession(._cls1 x$1)
        {
            this();
        }
    }

    private class HandleWebRequest
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            OnWebRequest(((HTTPRequestEvent)e).HTTPRequest, ((HTTPRequestEvent)e).Session);
        }

        private HandleWebRequest()
        {
        }

        HandleWebRequest(._cls1 x$1)
        {
            this();
        }
    }

    private class HandleSSDP
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            if(e.getActionCommand().compareTo("OnSearch") == 0)
                OnSearch(((SSDPSearchEvent)e).Local, ((SSDPSearchEvent)e).Source, ((SSDPSearchEvent)e).SourcePort, ((SSDPSearchEvent)e).SearchTarget);
            if(e.getActionCommand().compareTo("OnRefresh") == 0)
                SendNotify();
        }

        private HandleSSDP()
        {
        }

        HandleSSDP(._cls1 x$1)
        {
            this();
        }
    }

    private class DeviceNetworkInfoHandler
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            NetworkInfo.NetworkEvent ne = (NetworkInfo.NetworkEvent)e;
            switch(ne.getID())
            {
            case 0: // '\0'
                NewDeviceInterfaceSink(ne.addr);
                break;
            }
        }

        private DeviceNetworkInfoHandler()
        {
        }

        DeviceNetworkInfoHandler(._cls1 x$1)
        {
            this();
        }
    }


    public String GetDeviceURN()
    {
        return _DeviceURN;
    }

    public void SetStandardDeviceType(String value)
    {
        _DeviceURN = String.valueOf(String.valueOf((new StringBuffer("urn:schemas-upnp-org:device:")).append(value).append(":").append(Version())));
    }

    public static UPnPDevice CreateRootDevice(int DeviceExpiration, double Version, String URL, String Root)
        throws Exception
    {
        return new UPnPDevice(DeviceExpiration, Version, URL, Root);
    }

    protected UPnPDevice()
    {
        POOL = new ThreadPool(5);
        EES = new HandleEventSession(null);
        ControlPointOnly = true;
        ServiceList = new ArrayList();
        WebServerTable = Collections.synchronizedMap(new HashMap());
        SSDPServerTable = Collections.synchronizedMap(new HashMap());
        parent = null;
        InterfaceToHost = null;
        Services = new UPnPService[0];
        EmbeddedDevices = new UPnPDevice[0];
        parent = null;
        ControlPointOnly = true;
        Services = new UPnPService[0];
    }

    public String toString()
    {
        return FriendlyName;
    }

    public void Advertise()
    {
        SendNotify();
    }

    protected UPnPDevice(int DeviceExpiration, double Version, String URL, String Root)
        throws Exception
    {
        POOL = new ThreadPool(5);
        EES = new HandleEventSession(null);
        ControlPointOnly = true;
        ServiceList = new ArrayList();
        WebServerTable = Collections.synchronizedMap(new HashMap());
        SSDPServerTable = Collections.synchronizedMap(new HashMap());
        parent = null;
        InterfaceToHost = null;
        Services = new UPnPService[0];
        EmbeddedDevices = new UPnPDevice[0];
        ControlPointOnly = false;
        ExpirationTimeout = DeviceExpiration;
        RootPath = Root;
        String VersionString = (new Double(Version)).toString();
        if(Version == (double)0)
        {
            Major = 1;
            Minor = 0;
        } else
        if(VersionString.indexOf(".") == 0)
        {
            Major = Integer.parseInt(VersionString);
            Minor = 0;
        } else
        {
            Major = Integer.parseInt(VersionString.substring(0, VersionString.indexOf(".")));
            Minor = Integer.parseInt(VersionString.substring(VersionString.indexOf(".") + 1));
        }
        Services = new UPnPService[0];
    }

    public String Version()
    {
        String v = String.valueOf(String.valueOf((new StringBuffer(String.valueOf(String.valueOf(String.valueOf(Major))))).append(".").append(String.valueOf(Minor))));
        Double dv = new Double(v);
        return dv.toString();
    }

    public void AddService(UPnPService service)
    {
        service.ParentDevice = this;
        if(ControlPointOnly)
        {
            if(!service.ControlURL.startsWith("http://"))
            {
                if(service.ControlURL.startsWith("/"))
                    service.ControlURL = service.ControlURL.substring(1);
                service.ControlURL = String.valueOf(BaseURL) + String.valueOf(service.ControlURL);
            }
            if(!service.SCPDURL.startsWith("http://"))
            {
                if(service.SCPDURL.startsWith("/"))
                    service.SCPDURL = service.SCPDURL.substring(1);
                service.SCPDURL = String.valueOf(BaseURL) + String.valueOf(service.SCPDURL);
            }
            if(!service.EventURL.startsWith("http://"))
            {
                if(service.EventURL.startsWith("/"))
                    service.EventURL = service.EventURL.substring(1);
                service.EventURL = String.valueOf(BaseURL) + String.valueOf(service.EventURL);
                service.EventCallbackURL = String.valueOf(BaseURL) + String.valueOf(service.EventURL);
            } else
            {
                try
                {
                    URL _url = new URL(service.EventCallbackURL);
                    service.EventCallbackURL = String.valueOf(BaseURL) + String.valueOf(_url.getPath().substring(1));
                }
                catch(Exception e)
                {
                    System.exit(2);
                }
            }
        }
        ServiceList.add(service);
        Object tlist[] = ServiceList.toArray();
        Services = new UPnPService[tlist.length];
        for(int x = 0; x < tlist.length; x++)
            Services[x] = (UPnPService)tlist[x];

    }

    public void StartDevice()
    {
        DeviceNetInfo = new NetworkInfo(new DeviceNetworkInfoHandler(null));
    }

    protected void NewDeviceInterfaceSink(InetAddress addr)
    {
        try
        {
            SSDP SSDPServer = new SSDP(addr, 1900, ExpirationTimeout, POOL);
            SSDPServer.addActionListener(new HandleSSDP(null));
            SSDPServerTable.put(addr.getHostAddress(), SSDPServer);
            SSDPServer.Start();
            String tmp = addr.getHostAddress();
            MiniWebServer WebServer = new MiniWebServer(tmp, NetworkInfo.GetFreePort(10000, 15000, addr), POOL);
            WebServer.addActionListener(new HandleWebRequest(null));
            WebServerTable.put(tmp, WebServer);
            WebServer.Start();
            SendNotify(addr);
        }
        catch(Exception exception) { }
    }

    protected void finalize()
    {
        StopDevice();
    }

    public void StopDevice()
    {
        InetAddress local[] = DeviceNetInfo.GetLocalAddresses();
        for(int i = 0; i < local.length; i++)
            SendBye(local[i]);

    }

    protected HTTPMessage[] BuildByePacket()
    {
        HTTPMessage msg[] = new HTTPMessage[3 + Services.length];
        for(int id = 0; id < Services.length; id++)
        {
            msg[id] = new HTTPMessage();
            msg[id].Directive = "NOTIFY";
            msg[id].DirectiveObj = "*";
            msg[id].AddTag("Host", "239.255.255.250:1900");
            msg[id].AddTag("NT", Services[id].GetServiceURN());
            msg[id].AddTag("NTS", "ssdp:byebye");
            msg[id].AddTag("USN", String.valueOf(String.valueOf((new StringBuffer("uuid:")).append(UniqueDeviceName).append("::").append(Services[id].GetServiceURN()))));
        }

        msg[Services.length] = new HTTPMessage();
        msg[Services.length].Directive = "NOTIFY";
        msg[Services.length].DirectiveObj = "*";
        msg[Services.length].AddTag("Host", "239.255.255.250:1900");
        msg[Services.length].AddTag("NT", GetDeviceURN());
        msg[Services.length].AddTag("NTS", "ssdp:byebye");
        msg[Services.length].AddTag("USN", String.valueOf(String.valueOf((new StringBuffer("uuid:")).append(UniqueDeviceName).append("::").append(GetDeviceURN()))));
        msg[Services.length + 1] = new HTTPMessage();
        msg[Services.length + 1].Directive = "NOTIFY";
        msg[Services.length + 1].DirectiveObj = "*";
        msg[Services.length + 1].AddTag("Host", "239.255.255.250:1900");
        msg[Services.length + 1].AddTag("NT", "uuid:".concat(String.valueOf(String.valueOf(UniqueDeviceName))));
        msg[Services.length + 1].AddTag("NTS", "ssdp:byebye");
        msg[Services.length + 1].AddTag("USN", "uuid:".concat(String.valueOf(String.valueOf(UniqueDeviceName))));
        msg[Services.length + 2] = new HTTPMessage();
        msg[Services.length + 2].Directive = "NOTIFY";
        msg[Services.length + 2].DirectiveObj = "*";
        msg[Services.length + 2].AddTag("Host", "239.255.255.250:1900");
        msg[Services.length + 2].AddTag("NT", "upnp:rootdevice");
        msg[Services.length + 2].AddTag("NTS", "ssdp:byebye");
        msg[Services.length + 2].AddTag("USN", String.valueOf(String.valueOf((new StringBuffer("uuid:")).append(UniqueDeviceName).append("::upnp:rootdevice"))));
        return msg;
    }

    protected HTTPMessage[] BuildNotifyPacket(InetAddress addr)
    {
        HTTPMessage msg[] = new HTTPMessage[3 + Services.length];
        MiniWebServer localserver = (MiniWebServer)WebServerTable.get(addr.getHostAddress());
        String BaseURL = String.valueOf(String.valueOf((new StringBuffer("http://")).append(localserver.getLocalIP()).append(":").append(String.valueOf(localserver.getLocalPort())).append("/")));
        for(int id = 0; id < Services.length; id++)
        {
            msg[id] = new HTTPMessage();
            msg[id].Directive = "NOTIFY";
            msg[id].DirectiveObj = "*";
            msg[id].AddTag("Host", "239.255.255.250:1900");
            msg[id].AddTag("NT", Services[id].GetServiceURN());
            msg[id].AddTag("NTS", "ssdp:alive");
            msg[id].AddTag("Location", BaseURL);
            msg[id].AddTag("USN", String.valueOf(String.valueOf((new StringBuffer("uuid:")).append(UniqueDeviceName).append("::").append(Services[id].GetServiceURN()))));
            msg[id].AddTag("Server", " Java/2 UPnP/1.0");
            msg[id].AddTag("Cache-Control", "max-age=".concat(String.valueOf(String.valueOf(String.valueOf(ExpirationTimeout)))));
        }

        msg[Services.length] = new HTTPMessage();
        msg[Services.length].Directive = "NOTIFY";
        msg[Services.length].DirectiveObj = "*";
        msg[Services.length].AddTag("Host", "239.255.255.250:1900");
        msg[Services.length].AddTag("NT", GetDeviceURN());
        msg[Services.length].AddTag("NTS", "ssdp:alive");
        msg[Services.length].AddTag("Location", BaseURL);
        msg[Services.length].AddTag("USN", String.valueOf(String.valueOf((new StringBuffer("uuid:")).append(UniqueDeviceName).append("::").append(GetDeviceURN()))));
        msg[Services.length].AddTag("Server", " Java/2 UPnP/1.0");
        msg[Services.length].AddTag("Cache-Control", "max-age=".concat(String.valueOf(String.valueOf(String.valueOf(ExpirationTimeout)))));
        msg[Services.length + 1] = new HTTPMessage();
        msg[Services.length + 1].Directive = "NOTIFY";
        msg[Services.length + 1].DirectiveObj = "*";
        msg[Services.length + 1].AddTag("Host", "239.255.255.250:1900");
        msg[Services.length + 1].AddTag("NT", "uuid:".concat(String.valueOf(String.valueOf(UniqueDeviceName))));
        msg[Services.length + 1].AddTag("NTS", "ssdp:alive");
        msg[Services.length + 1].AddTag("Location", BaseURL);
        msg[Services.length + 1].AddTag("USN", "uuid:".concat(String.valueOf(String.valueOf(UniqueDeviceName))));
        msg[Services.length + 1].AddTag("Server", " Java/2 UPnP/1.0");
        msg[Services.length + 1].AddTag("Cache-Control", "max-age=".concat(String.valueOf(String.valueOf(String.valueOf(ExpirationTimeout)))));
        msg[Services.length + 2] = new HTTPMessage();
        msg[Services.length + 2].Directive = "NOTIFY";
        msg[Services.length + 2].DirectiveObj = "*";
        msg[Services.length + 2].AddTag("Host", "239.255.255.250:1900");
        msg[Services.length + 2].AddTag("NT", "upnp:rootdevice");
        msg[Services.length + 2].AddTag("NTS", "ssdp:alive");
        msg[Services.length + 2].AddTag("Location", BaseURL);
        msg[Services.length + 2].AddTag("USN", String.valueOf(String.valueOf((new StringBuffer("uuid:")).append(UniqueDeviceName).append("::upnp:rootdevice"))));
        msg[Services.length + 2].AddTag("Server", " Java/2 UPnP/1.0");
        msg[Services.length + 2].AddTag("Cache-Control", "max-age=".concat(String.valueOf(String.valueOf(String.valueOf(ExpirationTimeout)))));
        return msg;
    }

    protected void SendNotify(InetAddress ip)
    {
        HTTPMessage packet[] = BuildNotifyPacket(ip);
        for(int i = 0; i < packet.length; i++)
        {
            ((SSDP)SSDPServerTable.get(ip.getHostAddress())).Multicast(packet[i]);
            ((SSDP)SSDPServerTable.get(ip.getHostAddress())).Multicast(packet[i]);
            ((SSDP)SSDPServerTable.get(ip.getHostAddress())).Multicast(packet[i]);
        }

    }

    protected void SendNotify()
    {
        InetAddress i[] = DeviceNetInfo.GetLocalAddresses();
        for(int y = 0; y < i.length; y++)
            SendNotify(i[y]);

    }

    protected void SendBye(InetAddress local)
    {
        HTTPMessage x[] = BuildByePacket();
        for(int y = 0; y < x.length; y++)
        {
            SSDP s = (SSDP)SSDPServerTable.get(local.getHostAddress());
            s.Multicast(x[y]);
            s.Multicast(x[y]);
            s.Multicast(x[y]);
        }

    }

    protected String GetDeviceXML()
    {
        String XML = "";
        XML = String.valueOf(String.valueOf(XML)).concat("<root xmlns=\"urn:schemas-upnp-org:device-1-0\">\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("  <specVersion>\r\n");
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <major>")).append(String.valueOf(Major)).append("</major>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <minor>")).append(String.valueOf(Minor)).append("</minor>\r\n"))));
        XML = String.valueOf(String.valueOf(XML)).concat("  </specVersion>\r\n");
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("  <URLBase>")).append(BaseURL).append("</URLBase>\r\n"))));
        XML = String.valueOf(String.valueOf(XML)).concat("  <device>\r\n");
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <deviceType>")).append(GetDeviceURN()).append("</deviceType>\r\n"))));
        if(PresentationURL != null)
            XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <presentationURL>")).append(PresentationURL).append("</presentationURL>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <friendlyName>")).append(FriendlyName).append("</friendlyName>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <manufacturer>")).append(Manufacturer).append("</manufacturer>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <manufacturerURL>")).append(ManufacturerURL).append("</manufacturerURL>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <modelDescription>")).append(ModelDescription).append("</modelDescription>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <modelName>")).append(ModelName).append("</modelName>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <modelNumber>")).append(ModelNumber).append("</modelNumber>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <modelURL>")).append(ModelURL).append("</modelURL>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <serialNumber>")).append(SerialNumber).append("</serialNumber>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <UDN>uuid:")).append(UniqueDeviceName).append("</UDN>\r\n"))));
        XML = String.valueOf(String.valueOf(XML)).concat("    <iconList>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("       <icon>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("          <mimetype>image/png</mimetype>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("          <width>16</width>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("          <height>16</height>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("          <depth>2</depth>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("          <url>./images/16-2.png</url>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("       </icon>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("    </iconList>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("    <serviceList>\r\n");
        for(int sid = 0; sid < Services.length; sid++)
            XML = String.valueOf(XML) + String.valueOf(Services[sid].GetServiceXML());

        XML = String.valueOf(String.valueOf(XML)).concat("    </serviceList>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("  </device>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("</root>\r\n");
        return XML;
    }

    public String GetRootDeviceXML(InetAddress localIP, int localPort)
    {
        String XML = "";
        XML = String.valueOf(String.valueOf(XML)).concat("<root xmlns=\"urn:schemas-upnp-org:device-1-0\">\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("  <specVersion>\r\n");
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <major>")).append(String.valueOf(Major)).append("</major>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <minor>")).append(String.valueOf(Minor)).append("</minor>\r\n"))));
        XML = String.valueOf(String.valueOf(XML)).concat("  </specVersion>\r\n");
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("  <URLBase>http://")).append(localIP.getHostAddress()).append(":").append(String.valueOf(localPort)).append("/").append("</URLBase>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(GetNonRootDeviceXML(localIP, localPort));
        XML = String.valueOf(String.valueOf(XML)).concat("</root>\r\n");
        return XML;
    }

    protected String GetNonRootDeviceXML(InetAddress localIP, int localPort)
    {
        String XML = "";
        XML = String.valueOf(String.valueOf(XML)).concat("  <device>\r\n");
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <deviceType>")).append(GetDeviceURN()).append("</deviceType>\r\n"))));
        if(PresentationURL != null)
            XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <presentationURL>")).append(PresentationURL).append("</presentationURL>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <friendlyName>")).append(FriendlyName).append("</friendlyName>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <manufacturer>")).append(Manufacturer).append("</manufacturer>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <manufacturerURL>")).append(ManufacturerURL).append("</manufacturerURL>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <modelDescription>")).append(ModelDescription).append("</modelDescription>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <modelName>")).append(ModelName).append("</modelName>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <modelNumber>")).append(ModelNumber).append("</modelNumber>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <modelURL>")).append(ModelURL).append("</modelURL>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <serialNumber>")).append(SerialNumber).append("</serialNumber>\r\n"))));
        XML = String.valueOf(XML) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <UDN>uuid:")).append(UniqueDeviceName).append("</UDN>\r\n"))));
        XML = String.valueOf(String.valueOf(XML)).concat("    <iconList>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("       <icon>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("          <mimetype>image/png</mimetype>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("          <width>16</width>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("          <height>16</height>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("          <depth>8</depth>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("          <url>/Adapter-16.png</url>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("       </icon>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("    </iconList>\r\n");
        XML = String.valueOf(String.valueOf(XML)).concat("    <serviceList>\r\n");
        for(int sid = 0; sid < Services.length; sid++)
            XML = String.valueOf(XML) + String.valueOf(Services[sid].GetServiceXML());

        XML = String.valueOf(String.valueOf(XML)).concat("    </serviceList>\r\n");
        if(EmbeddedDevices.length > 0)
        {
            XML = String.valueOf(String.valueOf(XML)).concat("    <deviceList>\r\n");
            for(int ei = 0; ei < EmbeddedDevices.length; ei++)
                XML = String.valueOf(XML) + String.valueOf(EmbeddedDevices[ei].GetNonRootDeviceXML(localIP, localPort));

            XML = String.valueOf(String.valueOf(XML)).concat("    </deviceList>\r\n");
        }
        XML = String.valueOf(String.valueOf(XML)).concat("  </device>\r\n");
        return XML;
    }

    protected boolean ContainsSearchTarget(String ST)
    {
        boolean RetVal = false;
        if(ST.compareTo("upnp:rootdevice") == 0)
            return true;
        if("uuid:".concat(String.valueOf(String.valueOf(UniqueDeviceName))).compareTo(ST) == 0)
            return true;
        if(GetDeviceURN().compareTo(ST) == 0)
            return true;
        int x = 0;
        do
        {
            if(x >= Services.length)
                break;
            if(Services[x].GetServiceURN().compareTo(ST) == 0 || Services[x].ServiceID.compareTo(ST) == 0)
            {
                RetVal = true;
                break;
            }
            x++;
        } while(true);
        return RetVal;
    }

    protected void OnSearch(InetAddress local, InetAddress source, int sourcePort, String SearchTarget)
    {
        if(ContainsSearchTarget(SearchTarget))
        {
            HTTPMessage msg = new HTTPMessage();
            msg.StatusCode = 200;
            msg.StatusData = "OK";
            msg.AddTag("ST", SearchTarget);
            msg.AddTag("USN", "uuid:".concat(String.valueOf(String.valueOf(UniqueDeviceName))));
            String ha = local.getHostAddress();
            int hp = ((MiniWebServer)WebServerTable.get(ha)).getLocalPort();
            msg.AddTag("Location", String.valueOf(String.valueOf((new StringBuffer("http://")).append(ha).append(":").append(String.valueOf(hp)).append("/"))));
            msg.AddTag("Server", " JDK/1.3.0 UPnP/1.0");
            msg.AddTag("EXT", "");
            msg.AddTag("Cache-Control", "max-age=".concat(String.valueOf(String.valueOf(String.valueOf(ExpirationTimeout)))));
            ((SSDP)SSDPServerTable.get(local.getHostAddress())).Unicast(msg, source, sourcePort);
        }
    }

    protected void EventSessionSink(HTTPSession sender, Object SObject)
    {
        HTTPMessage msg = (HTTPMessage)SObject;
        sender.Send(msg);
    }

    protected void OnWebRequest(HTTPMessage request, HTTPSession WebSession)
    {
        HTTPMessage response = new HTTPMessage();
        String MethodData = request.DirectiveObj;
        if(request.Directive.compareTo("GET") == 0)
        {
            try
            {
                response = Get(request.DirectiveObj, WebSession.getSourceIP(), WebSession.getSourcePort());
            }
            catch(Exception e)
            {
                response.StatusCode = 400;
                response.StatusData = e.toString();
            }
            WebSession.Send(response);
        }
        if(request.Directive.compareTo("POST") == 0)
        {
            try
            {
                response = Post(request.DirectiveObj, request.GetStringBuffer(), request.GetTag("SOAPACTION"));
            }
            catch(Exception e)
            {
                response.StatusCode = 500;
                String em = e.toString().replace('\r', ' ');
                response.StatusData = em;
                WebSession.Send(response);
            }
            WebSession.Send(response);
            return;
        }
        if(request.Directive.compareTo("UNSUBSCRIBE") == 0)
        {
            CancelEvent(MethodData, request.GetTag("SID"));
            response.StatusCode = 200;
            response.StatusData = "OK";
            WebSession.Send(response);
        }
        if(request.Directive.compareTo("SUBSCRIBE") == 0)
        {
            HTTPMessage Response = new HTTPMessage();
            HTTPMessage Response2 = null;
            String SID[] = new String[1];
            SID[0] = request.GetTag("SID");
            String NT = request.GetTag("NT");
            String Timeout = request.GetTag("Timeout");
            String CallbackURL = request.GetTag("Callback");
            if(Timeout.compareTo("") == 0)
            {
                Timeout = "7200";
            } else
            {
                Timeout = Timeout.substring(Timeout.indexOf("-") + 1).trim().toUpperCase();
                if(Timeout.compareTo("INFINITE") == 0)
                    Timeout = "0";
            }
            try
            {
                if(SID[0].compareTo("") != 0)
                    RenewEvents(MethodData.substring(1), SID[0], Timeout);
                else
                    Response2 = SubscribeEvents(SID, MethodData.substring(1), CallbackURL, Timeout);
            }
            catch(Exception e)
            {
                response.StatusCode = 500;
                String em = e.toString().replace('\r', ' ');
                response.StatusData = em;
                WebSession.Send(response);
                return;
            }
            if(Timeout.compareTo("0") == 0)
                Timeout = "Second-infinite";
            else
                Timeout = "Second-".concat(String.valueOf(String.valueOf(Timeout)));
            Response.StatusCode = 200;
            Response.StatusData = "OK";
            Response.AddTag("Server", "JDK/1.3.0 UPnP/1.0");
            Response.AddTag("SID", SID[0]);
            Response.AddTag("Timeout", Timeout);
            WebSession.Send(Response);
            if(Response2 != null)
            {
                URL cbURL[] = ParseEventURL(CallbackURL);
                for(boolean done = false; !done; done = true)
                {
                    for(int x = 0; x < cbURL.length; x++)
                    {
                        Response2.DirectiveObj = URLDecoder.decode(cbURL[x].getPath());
                        Response2.AddTag("Host", String.valueOf(String.valueOf((new StringBuffer(String.valueOf(String.valueOf(cbURL[x].getHost())))).append(":").append(String.valueOf(cbURL[x].getPort())))));
                        try
                        {
                            InetAddress RemoteAddress = InetAddress.getByName(cbURL[x].getHost());
                            int RemotePort = cbURL[x].getPort();
                            HTTPSession httpsession = new HTTPSession(null, 0, RemoteAddress, RemotePort, EES, POOL, Response2);
                        }
                        catch(UnknownHostException unknownhostexception) { }
                        done = true;
                    }

                }

            }
        }
    }

    protected URL[] ParseEventURL(String URLList)
    {
        DText parser = new DText();
        ArrayList TList = new ArrayList();
        int cnt = parser.DCOUNT(URLList, ">");
        for(int x = 1; x <= cnt; x++)
        {
            String temp = parser.FIELD(URLList, ">", x);
            try
            {
                temp = temp.substring(temp.indexOf("<") + 1);
                TList.add(new URL(temp));
            }
            catch(Exception exception) { }
        }

        URL RetVal[] = new URL[TList.size()];
        for(int x = 0; x < RetVal.length; x++)
            RetVal[x] = (URL)TList.get(x);

        return RetVal;
    }

    protected boolean RenewEvents(String MethodData, String SID, String Timeout)
        throws Exception
    {
        boolean IsOK = false;
        int id = 0;
        do
        {
            if(id >= Services.length)
                break;
            if(Services[id].IsMatchURL(Services[id].EventURL, MethodData))
            {
                if(!Services[id]._RenewEvent(SID, Timeout))
                    throw new Exception(String.valueOf(String.valueOf(SID)).concat(" is not a valid SID"));
                IsOK = true;
                break;
            }
            id++;
        } while(true);
        if(!IsOK)
            throw new Exception(String.valueOf(String.valueOf(MethodData)).concat(" is not a valid Event location"));
        else
            return true;
    }

    protected void CancelEvent(String MethodData, String SID)
    {
        int id = 0;
        do
        {
            if(id >= Services.length)
                break;
            if(Services[id].IsMatchURL(Services[id].EventURL, MethodData))
            {
                Services[id]._CancelEvent(SID);
                break;
            }
            id++;
        } while(true);
    }

    protected HTTPMessage SubscribeEvents(String SID[], String MethodData, String CallbackURL, String Timeout)
        throws Exception
    {
        boolean IsOK = false;
        HTTPMessage response2 = new HTTPMessage();
        int id = 0;
        do
        {
            if(id >= Services.length)
                break;
            if(Services[id].IsMatchURL(Services[id].EventURL, MethodData))
            {
                response2 = Services[id]._SubscribeEvent(SID, CallbackURL, Timeout);
                IsOK = true;
                break;
            }
            id++;
        } while(true);
        if(!IsOK)
            throw new Exception(String.valueOf(String.valueOf(MethodData)).concat(" is not a valid Event location"));
        else
            return response2;
    }

    public HTTPMessage Invoke(String Control, String XML, String SOAPACTION)
        throws Exception
    {
        String MethodTag = "";
        ArrayList VarList = new ArrayList();
        XMLTextReader XMLDoc = new XMLTextReader(XML);
        XMLDoc.Read();
        if(XMLDoc.getLocalName().compareTo("Envelope") == 0)
        {
            XMLDoc.Read();
            if(XMLDoc.getLocalName().compareTo("Body") == 0)
            {
                XMLDoc.Read();
                MethodTag = XMLDoc.getLocalName();
                XMLDoc.Read();
                for(; XMLDoc.getLocalName().compareTo(MethodTag) != 0 && XMLDoc.getLocalName().compareTo("Body") != 0; XMLDoc.Read())
                {
                    UPnPArgument VarArg = new UPnPArgument(XMLDoc.getLocalName(), StringFormatter.UnEscapeString(XMLDoc.ReadInnerXML()));
                    VarList.add(VarArg);
                }

            }
        }
        XMLDoc.dispose();
        Object RetVal = "";
        boolean found = false;
        HTTPMessage response = new HTTPMessage();
        int id = 0;
        id = 0;
        do
        {
            if(id >= Services.length)
                break;
            if(Services[id].IsMatchURL(Services[id].ControlURL, Control))
            {
                RetVal = Services[id].InvokeLocal(MethodTag, VarList);
                found = true;
                break;
            }
            id++;
        } while(true);
        if(!found)
            throw new Exception(String.valueOf(String.valueOf(MethodTag)).concat(" does not exist"));
        String Body = "";
        Body = "<s:Envelope\r\n";
        Body = String.valueOf(String.valueOf(Body)).concat("    xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\r\n");
        Body = String.valueOf(String.valueOf(Body)).concat("      s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\r\n");
        Body = String.valueOf(String.valueOf(Body)).concat("  <s:Body>\r\n");
        if(!SOAPACTION.endsWith("#QueryStateVariable\""))
        {
            Body = String.valueOf(Body) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <u:")).append(MethodTag).append("Response xmlns:u=\"").append(Services[id].GetServiceURN()).append("\">\r\n"))));
            if(RetVal != null)
                Body = String.valueOf(Body) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("     <_ReturnValue>")).append(StringFormatter.EscapeString(UPnPService.SerializeObjectInstance(RetVal))).append("</_ReturnValue>\r\n"))));
            for(int ID = 0; ID < VarList.size(); ID++)
                Body = String.valueOf(Body) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("      <")).append(((UPnPArgument)VarList.get(ID)).Name).append(">").append(StringFormatter.EscapeString(UPnPService.SerializeObjectInstance(((UPnPArgument)VarList.get(ID)).DataValue))).append("</").append(((UPnPArgument)VarList.get(ID)).Name).append(">\r\n"))));

        } else
        {
            Body = String.valueOf(Body) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    <u:")).append(MethodTag).append("Response xmlns:u=\"urn:schemas-upnp-org:control-1-0\">\r\n"))));
            Body = String.valueOf(Body) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("     <return>")).append(StringFormatter.EscapeString(UPnPService.SerializeObjectInstance(RetVal))).append("</return>\r\n"))));
        }
        Body = String.valueOf(Body) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("    </u:")).append(MethodTag).append("Response>\r\n"))));
        Body = String.valueOf(String.valueOf(Body)).concat("  </s:Body>\r\n");
        Body = String.valueOf(String.valueOf(Body)).concat("</s:Envelope>\r\n");
        response.StatusCode = 200;
        response.StatusData = "OK";
        response.AddTag("Content-Type", "text/xml");
        response.AddTag("EXT", "");
        response.AddTag("Server", "JDK/1.3.0 UPNP/1.0");
        response.SetStringBuffer(Body);
        return response;
    }

    protected HTTPMessage Post(String MethodData, String XML, String SOAPACTION)
    {
        HTTPMessage r = null;
        try
        {
            r = Invoke(MethodData.substring(1), XML, SOAPACTION);
        }
        catch(Exception e)
        {
            r = null;
        }
        return r;
    }

    protected HTTPMessage Get(String GetWhat, InetAddress localIP, int localPort)
    {
        HTTPMessage msg = new HTTPMessage();
        if(GetWhat.compareTo("/") == 0)
        {
            msg.StatusCode = 200;
            msg.StatusData = "OK";
            msg.AddTag("Content-Type", "text/xml");
            msg.SetStringBuffer(GetRootDeviceXML(localIP, localPort));
            return msg;
        }
        boolean SCPDok = false;
        int id = 0;
        do
        {
            if(id >= Services.length)
                break;
            if(GetWhat.compareTo(Services[id].SCPDFile()) == 0)
            {
                SCPDok = true;
                msg.StatusCode = 200;
                msg.StatusData = "OK";
                msg.AddTag("Content-Type", "text/xml");
                msg.SetStringBuffer(Services[id].GetSCPDXml());
                break;
            }
            id++;
        } while(true);
        if(SCPDok)
            return msg;
        try
        {
            FileInputStream fis = new FileInputStream(String.valueOf(RootPath) + String.valueOf(GetWhat.substring(1)));
            byte fbuffer[] = new byte[fis.available()];
            fis.read(fbuffer);
            fis.close();
            msg.StatusCode = 200;
            msg.StatusData = "OK";
            msg.AddTag("Content-Type", "application/octet-stream");
            if(GetWhat.endsWith(".htm") || GetWhat.endsWith(".html"))
                msg.AddTag("Content-Type", "text/html");
            if(GetWhat.endsWith(".xml"))
                msg.AddTag("Content-Type", "text/xml");
            msg.SetBodyBuffer(fbuffer);
        }
        catch(Exception e)
        {
            msg.StatusCode = 404;
            msg.StatusData = String.valueOf(String.valueOf(GetWhat)).concat(" not found");
        }
        return msg;
    }

    public static UPnPDevice Parse(String XML)
    {
        return Parse(XML, null);
    }

    public static UPnPDevice Parse(String XML, InetAddress Intfce)
    {
        XMLTextReader XMLDoc = new XMLTextReader(XML);
        UPnPDevice RetVal = new UPnPDevice();
        RetVal.InterfaceToHost = Intfce;
        XMLDoc.Read();
        if(XMLDoc.getLocalName().compareTo("root") == 0)
        {
            XMLDoc.Read();
            for(; XMLDoc.getLocalName().compareTo("root") != 0 && !XMLDoc.EOF(); XMLDoc.Read())
            {
                boolean def = true;
                if(XMLDoc.getLocalName().compareTo("specVersion") == 0)
                {
                    def = false;
                    XMLDoc.Read();
                    RetVal.Major = Integer.parseInt(XMLDoc.ReadString());
                    XMLDoc.Read();
                    RetVal.Minor = Integer.parseInt(XMLDoc.ReadString());
                    XMLDoc.Read();
                }
                if(XMLDoc.getLocalName().compareTo("URLBase") == 0)
                {
                    def = false;
                    RetVal.BaseURL = XMLDoc.ReadString();
                }
                if(XMLDoc.getLocalName().compareTo("device") == 0)
                {
                    def = false;
                    RetVal = ParseDevice(String.valueOf(String.valueOf((new StringBuffer("<device>\r\n")).append(XMLDoc.ReadInnerXML()).append("</device>"))), RetVal);
                }
                if(def)
                    XMLDoc.Skip();
            }

            XMLDoc.dispose();
            return RetVal;
        } else
        {
            return null;
        }
    }

    protected static UPnPDevice ParseDevice(String XML, UPnPDevice RetVal)
    {
        XMLTextReader XMLDoc = new XMLTextReader(XML);
        XMLDoc.Read();
        if(XMLDoc.getLocalName().compareTo("device") == 0)
        {
            XMLDoc.Read();
            for(; XMLDoc.getLocalName().compareTo("device") != 0; XMLDoc.Read())
            {
                boolean def = true;
                if(XMLDoc.getLocalName().compareTo("deviceList") == 0)
                {
                    def = false;
                    RetVal = ParseDeviceList(String.valueOf(String.valueOf((new StringBuffer("<deviceList>\r\n")).append(XMLDoc.ReadInnerXML()).append("</deviceList>"))), RetVal);
                }
                if(XMLDoc.getLocalName().compareTo("URLBase") == 0)
                {
                    def = false;
                    RetVal.BaseURL = XMLDoc.ReadString();
                }
                if(XMLDoc.getLocalName().compareTo("deviceType") == 0)
                {
                    def = false;
                    RetVal._DeviceURN = XMLDoc.ReadString();
                }
                if(XMLDoc.getLocalName().compareTo("friendlyName") == 0)
                {
                    def = false;
                    RetVal.FriendlyName = XMLDoc.ReadString();
                }
                if(XMLDoc.getLocalName().compareTo("manufacturer") == 0)
                {
                    def = false;
                    RetVal.Manufacturer = XMLDoc.ReadString();
                }
                if(XMLDoc.getLocalName().compareTo("manufacturerURL") == 0)
                {
                    def = false;
                    RetVal.ManufacturerURL = XMLDoc.ReadString();
                }
                if(XMLDoc.getLocalName().compareTo("modelDescription") == 0)
                {
                    def = false;
                    RetVal.ModelDescription = XMLDoc.ReadString();
                }
                if(XMLDoc.getLocalName().compareTo("modelName") == 0)
                {
                    def = false;
                    RetVal.ModelName = XMLDoc.ReadString();
                }
                if(XMLDoc.getLocalName().compareTo("modelNumber") == 0)
                {
                    def = false;
                    RetVal.ModelNumber = XMLDoc.ReadString();
                }
                if(XMLDoc.getLocalName().compareTo("modelURL") == 0)
                {
                    def = false;
                    RetVal.ModelURL = XMLDoc.ReadString();
                }
                if(XMLDoc.getLocalName().compareTo("serialNumber") == 0)
                {
                    def = false;
                    RetVal.SerialNumber = XMLDoc.ReadString();
                }
                if(XMLDoc.getLocalName().compareTo("UDN") == 0)
                {
                    def = false;
                    String TempString = XMLDoc.ReadString();
                    RetVal.UniqueDeviceName = TempString.substring(5);
                }
                if(XMLDoc.getLocalName().compareTo("UPC") == 0)
                {
                    def = false;
                    RetVal.ProductCode = XMLDoc.ReadString();
                }
                if(XMLDoc.getLocalName().compareTo("presentationURL") == 0)
                {
                    def = false;
                    RetVal.PresentationURL = XMLDoc.ReadString();
                }
                if(XMLDoc.getLocalName().compareTo("serviceList") == 0)
                {
                    def = false;
                    XMLDoc.Read();
                    for(; XMLDoc.getLocalName().compareTo("serviceList") != 0; XMLDoc.Read())
                        if(XMLDoc.getLocalName().compareTo("service") == 0)
                        {
                            String TempString = XMLDoc.ReadInnerXML();
                            TempString = String.valueOf(String.valueOf((new StringBuffer("<service>\r\n")).append(TempString).append("</service>")));
                            UPnPService service = UPnPService.Parse(TempString);
                            RetVal.AddService(service);
                        }

                }
                if(def)
                    XMLDoc.Skip();
            }

        }
        XMLDoc.dispose();
        return RetVal;
    }

    protected static UPnPDevice ParseDeviceList(String XML, UPnPDevice RetVal)
    {
        XMLTextReader XMLDoc = new XMLTextReader(XML);
        UPnPDevice EmbeddedDevice = null;
        XMLDoc.Read();
        if(XMLDoc.getLocalName().compareTo("deviceList") == 0)
        {
            XMLDoc.Read();
            for(; XMLDoc.getLocalName().compareTo("deviceList") != 0; XMLDoc.Read())
                if(XMLDoc.getLocalName().compareTo("device") == 0)
                {
                    EmbeddedDevice = new UPnPDevice();
                    EmbeddedDevice.BaseURL = RetVal.BaseURL;
                    EmbeddedDevice = ParseDevice(String.valueOf(String.valueOf((new StringBuffer("<device>\r\n")).append(XMLDoc.ReadInnerXML()).append("</device>"))), EmbeddedDevice);
                    RetVal.AddDevice(EmbeddedDevice);
                }

        }
        XMLDoc.dispose();
        return RetVal;
    }

    public void AddDevice(UPnPDevice device)
    {
        device.parent = this;
        UPnPDevice temp[] = new UPnPDevice[EmbeddedDevices.length + 1];
        System.arraycopy(EmbeddedDevices, 0, temp, 0, EmbeddedDevices.length);
        temp[EmbeddedDevices.length] = device;
        EmbeddedDevices = temp;
    }

    protected ThreadPool POOL;
    protected HandleEventSession EES;
    protected boolean ControlPointOnly;
    protected ArrayList ServiceList;
    protected Map WebServerTable;
    protected Map SSDPServerTable;
    protected NetworkInfo DeviceNetInfo;
    protected String RootPath;
    protected String _DeviceURN;
    public UPnPDevice parent;
    public InetAddress InterfaceToHost;
    public String FriendlyName;
    public String Manufacturer;
    public String ManufacturerURL;
    public String ModelDescription;
    public String ModelName;
    public String ModelNumber;
    public String ModelURL;
    public String SerialNumber;
    public String ProductCode;
    public String UniqueDeviceName;
    public int Major;
    public int Minor;
    public int ExpirationTimeout;
    public String BaseURL;
    public String PresentationURL;
    public UPnPService Services[];
    public UPnPDevice EmbeddedDevices[];
}
