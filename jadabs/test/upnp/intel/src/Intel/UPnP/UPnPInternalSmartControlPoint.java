// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   UPnPInternalSmartControlPoint.java

package Intel.UPnP;

import java.awt.AWTEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EventObject;
import java.util.Hashtable;

// Referenced classes of package Intel.UPnP:
//            NetworkInfo, LifeTimeMonitor, UPnPControlPoint, UPnPDeviceFactory, 
//            UPnPDevice, ThreadPool, SSDPNotifyEvent

public class UPnPInternalSmartControlPoint
{
    private class cpListener
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            if(e.getID() == UPnPControlPoint.UPNP_SEARCH_RESULT)
            {
                UPnPControlPoint.SearchResult sr = (UPnPControlPoint.SearchResult)e;
                UPnPControlPointSearchSink(sr.ResponseIP, sr.ResponsePort, sr.LocalIP, sr.Location, sr.USN, sr.ST, sr.MaxAge);
            }
            if(e.getID() == UPnPControlPoint.UPNP_NOTIFY)
            {
                SSDPNotifyEvent sne = (SSDPNotifyEvent)e;
                SSDPNotifySink(sne.Source, sne.SourcePort, null, sne.LocationURL, sne.Alive, sne.UniqueName, sne.SearchTarget, sne.MaxAge);
            }
        }

        private cpListener()
        {
        }

        cpListener(._cls1 x$1)
        {
            this();
        }
    }

    private class netInfoListener
        implements ActionListener
    {

        public void actionPerformed(ActionEvent actionevent)
        {
        }

        private netInfoListener()
        {
        }

        netInfoListener(._cls1 x$1)
        {
            this();
        }
    }

    private class updateClockListener
        implements ActionListener
    {

        public void actionPerformed(ActionEvent actionevent)
        {
        }

        private updateClockListener()
        {
        }

        updateClockListener(._cls1 x$1)
        {
            this();
        }
    }

    private class lifetimeClockListener
        implements ActionListener
    {

        public void actionPerformed(ActionEvent actionevent)
        {
        }

        private lifetimeClockListener()
        {
        }

        lifetimeClockListener(._cls1 x$1)
        {
            this();
        }
    }

    private class dvFacListener
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            if(e.getID() == UPnPDeviceFactory.DEVICE_CREATED)
            {
                UPnPDeviceFactory.FactoryEvent fe = (UPnPDeviceFactory.FactoryEvent)e;
                DeviceFactoryCreationSink((UPnPDeviceFactory)e.getSource(), fe.device, fe.DeviceLocation);
            }
            if(e.getID() == UPnPDeviceFactory.DEVICE_CREATED_FAILED)
                System.out.println(((UPnPDeviceFactory.FactoryEvent)e).FailedReason.toString());
        }

        private dvFacListener()
        {
        }

        dvFacListener(._cls1 x$1)
        {
            this();
        }
    }

    public class InternalCPEvent extends ActionEvent
    {

        public UPnPDevice Device;

        public InternalCPEvent(Object src, UPnPDevice device)
        {
            super(src, UPnPInternalSmartControlPoint.DEVICE_ADDED, "DEVICE_ADDED");
            Device = device;
        }

        public InternalCPEvent(Object src, UPnPDevice device, Object Removed)
        {
            super(src, UPnPInternalSmartControlPoint.DEVICE_REMOVED, "DEVICE_REMOVED");
            Device = device;
        }

        public InternalCPEvent(Object src, UPnPDevice device, Object blank, Object Updated)
        {
            super(src, UPnPInternalSmartControlPoint.DEVICE_UPDATED, "DEVICE_UPDATED");
            Device = device;
        }
    }

    private class DeviceInfo
    {

        public UPnPDevice Device;
        public Date NotifyTime;
        public String UDN;
        public URL BaseURL;
        public int MaxAge;
        public InetAddress LocalEP;
        public InetAddress SourceEP;
        public URL PendingBaseURL;
        public int PendingMaxAge;
        public InetAddress PendingLocalEP;
        public InetAddress PendingSourceEP;

        private DeviceInfo()
        {
        }

        DeviceInfo(._cls1 x$1)
        {
            this();
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

    public UPnPInternalSmartControlPoint(ThreadPool pool)
    {
        hostNetworkInfo = new NetworkInfo();
        deviceTable = new Hashtable();
        deviceTableLock = new Object();
        activeDeviceList = Collections.synchronizedList(new ArrayList());
        deviceLifeTimeClock = new LifeTimeMonitor();
        deviceUpdateClock = new LifeTimeMonitor();
        EventList = new ArrayList();
        genericControlPoint = new UPnPControlPoint(pool);
        deviceFactory = new UPnPDeviceFactory(pool);
        genericControlPoint.addActionListener(new cpListener(null));
        deviceFactory.addActionListener(new dvFacListener(null));
        deviceLifeTimeClock.addActionListener(new lifetimeClockListener(null));
        deviceUpdateClock.addActionListener(new updateClockListener(null));
        hostNetworkInfo.addActionListener(new netInfoListener(null));
        genericControlPoint.FindDeviceAsync("upnp:rootdevice");
    }

    private void UPnPControlPointSearchSink(InetAddress sourceIP, int sourcePort, InetAddress localIP, URL LocationURL, String USN, String SearchTarget, int MaxAge)
    {
        synchronized(deviceTableLock)
        {
            if(!deviceTable.containsKey(USN))
            {
                DeviceInfo deviceInfo = new DeviceInfo(null);
                deviceInfo.Device = null;
                deviceInfo.UDN = USN;
                deviceInfo.NotifyTime = new Date();
                deviceInfo.BaseURL = LocationURL;
                deviceInfo.MaxAge = MaxAge;
                deviceInfo.LocalEP = localIP;
                deviceInfo.SourceEP = sourceIP;
                deviceTable.put(USN, deviceInfo);
                deviceFactory.CreateDevice(deviceInfo.BaseURL, deviceInfo.MaxAge);
            }
        }
    }

    private void DeviceFactoryCreationSink(UPnPDeviceFactory sender, UPnPDevice device, String locationURL)
    {
        if(!deviceTable.containsKey(device.UniqueDeviceName))
            return;
        synchronized(deviceTableLock)
        {
            if(((DeviceInfo)deviceTable.get(device.UniqueDeviceName)).Device != null)
                return;
            DeviceInfo deviceInfo = (DeviceInfo)deviceTable.get(device.UniqueDeviceName);
            deviceInfo.Device = device;
            deviceTable.put(device.UniqueDeviceName, deviceInfo);
            deviceLifeTimeClock.Add(device.UniqueDeviceName, device.ExpirationTimeout);
            activeDeviceList.add(device);
        }
        TriggerEvent(new InternalCPEvent(this, device));
    }

    public UPnPDevice[] GetCurrentDevices()
    {
        Object x[] = activeDeviceList.toArray();
        UPnPDevice RetVal[] = new UPnPDevice[x.length];
        for(int i = 0; i < x.length; i++)
            RetVal[i] = (UPnPDevice)x[i];

        return RetVal;
    }

    private void SSDPNotifySink(InetAddress sourceIP, int sourcePort, InetAddress localIP, URL LocationURL, boolean IsAlive, String USN, String SearchTarget, 
            int MaxAge)
    {
        if(SearchTarget.compareTo("upnp:rootdevice") != 0)
            return;
        UPnPDevice removedDevice = null;
        if(!IsAlive)
        {
            synchronized(deviceTableLock)
            {
                if(!deviceTable.containsKey(USN))
                    return;
                DeviceInfo deviceInfo = (DeviceInfo)deviceTable.get(USN);
                removedDevice = deviceInfo.Device;
                deviceTable.remove(USN);
                deviceLifeTimeClock.Remove(deviceInfo);
                deviceUpdateClock.Remove(deviceInfo);
                activeDeviceList.remove(removedDevice);
            }
            if(removedDevice != null)
                TriggerEvent(new InternalCPEvent(this, removedDevice, null));
        } else
        {
            synchronized(deviceTableLock)
            {
                if(!deviceTable.containsKey(USN))
                {
                    DeviceInfo deviceInfo = new DeviceInfo(null);
                    deviceInfo.Device = null;
                    deviceInfo.UDN = USN;
                    deviceInfo.NotifyTime = new Date();
                    deviceInfo.BaseURL = LocationURL;
                    deviceInfo.MaxAge = MaxAge;
                    deviceInfo.LocalEP = localIP;
                    deviceInfo.SourceEP = sourceIP;
                    deviceTable.put(USN, deviceInfo);
                    deviceFactory.CreateDevice(deviceInfo.BaseURL, deviceInfo.MaxAge);
                } else
                {
                    DeviceInfo deviceInfo = (DeviceInfo)deviceTable.get(USN);
                    if(deviceInfo.Device != null)
                        if(deviceInfo.BaseURL.equals(LocationURL))
                        {
                            deviceUpdateClock.Remove(deviceInfo);
                            deviceInfo.PendingBaseURL = null;
                            deviceInfo.PendingMaxAge = 0;
                            deviceInfo.PendingLocalEP = null;
                            deviceInfo.PendingSourceEP = null;
                            deviceInfo.NotifyTime = new Date();
                            deviceTable.put(USN, deviceInfo);
                            deviceLifeTimeClock.Add(deviceInfo.UDN, MaxAge);
                        } else
                        {
                            Date CheckDate = new Date(deviceInfo.NotifyTime.getTime() + (long)10000);
                            if(CheckDate.before(new Date()))
                            {
                                deviceInfo.PendingBaseURL = LocationURL;
                                deviceInfo.PendingMaxAge = MaxAge;
                                deviceInfo.PendingLocalEP = localIP;
                                deviceInfo.PendingSourceEP = sourceIP;
                                deviceUpdateClock.Add(deviceInfo.UDN, 3);
                            }
                        }
                }
            }
        }
    }

    private void DeviceLifeTimeClockSink(LifeTimeMonitor sender, Object obj)
    {
        DeviceInfo deviceInfo;
        synchronized(deviceTableLock)
        {
            if(!deviceTable.containsKey(obj))
                return;
            deviceInfo = (DeviceInfo)deviceTable.get(obj);
            deviceTable.remove(obj);
            deviceUpdateClock.Remove(obj);
            if(activeDeviceList.contains(deviceInfo.Device))
                activeDeviceList.remove(deviceInfo.Device);
            else
                deviceInfo.Device = null;
        }
        if(deviceInfo.Device != null)
            TriggerEvent(new InternalCPEvent(this, deviceInfo.Device, null));
    }

    private void DeviceUpdateClockSink(LifeTimeMonitor sender, Object obj)
    {
        DeviceInfo deviceInfo;
        synchronized(deviceTableLock)
        {
            if(!deviceTable.containsKey(obj))
                return;
            deviceInfo = (DeviceInfo)deviceTable.get(obj);
            if(deviceInfo.PendingBaseURL == null)
                return;
            deviceInfo.BaseURL = deviceInfo.PendingBaseURL;
            deviceInfo.MaxAge = deviceInfo.PendingMaxAge;
            deviceInfo.SourceEP = deviceInfo.PendingSourceEP;
            deviceInfo.LocalEP = deviceInfo.PendingLocalEP;
            deviceInfo.NotifyTime = new Date();
            deviceLifeTimeClock.Add(deviceInfo.UDN, deviceInfo.MaxAge);
        }
        TriggerEvent(new InternalCPEvent(this, deviceInfo.Device, null, null));
    }

    private UPnPControlPoint genericControlPoint;
    private NetworkInfo hostNetworkInfo;
    private Hashtable deviceTable;
    private Object deviceTableLock;
    private java.util.List activeDeviceList;
    private LifeTimeMonitor deviceLifeTimeClock;
    private LifeTimeMonitor deviceUpdateClock;
    private UPnPDeviceFactory deviceFactory;
    public static int DEVICE_ADDED = 1;
    public static int DEVICE_REMOVED = 2;
    public static int DEVICE_UPDATED = 3;
    private ArrayList EventList;




}
