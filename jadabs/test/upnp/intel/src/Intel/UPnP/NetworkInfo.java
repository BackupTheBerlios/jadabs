// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   NetworkInfo.java

package Intel.UPnP;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.*;
import java.util.*;

// Referenced classes of package Intel.UPnP:
//            LifeTimeMonitor

public class NetworkInfo
{
    private class InterfacePollerHandler
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            PollInterface((LifeTimeMonitor)e.getSource(), ((LifeTimeMonitor.LifeTimeMonitorEvent)e).State);
        }

        private InterfacePollerHandler()
        {
        }

        InterfacePollerHandler(._cls1 x$1)
        {
            this();
        }
    }

    public class NetworkEvent extends ActionEvent
    {

        public InetAddress addr;

        public NetworkEvent(Object src, int id, String cmd, InetAddress a)
        {
            super(src, id, cmd);
            addr = a;
        }
    }


    public void addActionListener(ActionListener a)
    {
        ActionList.add(a);
    }

    public NetworkInfo()
    {
        this(null);
    }

    protected void TriggerNewInterface(InetAddress addr)
    {
        Object a[] = ActionList.toArray();
        for(int i = 0; i < a.length; i++)
            if(a[i] != null)
                ((ActionListener)a[i]).actionPerformed(new NetworkEvent(this, 0, "NEW_INTERFACE", addr));

    }

    protected void TriggerOldInterface(InetAddress addr)
    {
        Object a[] = ActionList.toArray();
        for(int i = 0; i < a.length; i++)
            ((ActionListener)a[i]).actionPerformed(new NetworkEvent(this, 1, "OLD_INTERFACE", addr));

    }

    public NetworkInfo(ActionListener listener)
    {
        NewList = new ArrayList();
        DisList = new ArrayList();
        InterfacePoller = new LifeTimeMonitor();
        AddressTable = new ArrayList();
        ActionList = new ArrayList();
        addActionListener(listener);
        InterfacePoller.addActionListener(new InterfacePollerHandler(null));
        InetAddress i[] = null;
        try
        {
            HostName = InetAddress.getLocalHost().getHostName();
            i = InetAddress.getAllByName(HostName);
        }
        catch(UnknownHostException ehe)
        {
            return;
        }
        for(int x = 0; x < i.length; x++)
        {
            AddressTable.add(i[x]);
            TriggerNewInterface(i[x]);
        }

        InterfacePoller.Add(this, 1);
    }

    public InetAddress[] GetLocalAddresses()
    {
        InetAddress RetVal[] = new InetAddress[AddressTable.size()];
        Object t[] = AddressTable.toArray();
        for(int i = 0; i < t.length; i++)
            RetVal[i] = (InetAddress)t[i];

        return RetVal;
    }

    private void PollInterface(LifeTimeMonitor sender, Object obj)
    {
        try
        {
            InetAddress HostInfo[] = InetAddress.getAllByName(HostName);
            ArrayList CurrentAddressTable = new ArrayList();
            for(int i = 0; i < HostInfo.length; i++)
                CurrentAddressTable.add(HostInfo[i]);

            ArrayList OldAddressTable = AddressTable;
            AddressTable = CurrentAddressTable;
            ListIterator enum = CurrentAddressTable.listIterator();
            do
            {
                if(!enum.hasNext())
                    break;
                InetAddress addr = (InetAddress)enum.next();
                if(!OldAddressTable.contains(addr))
                    TriggerNewInterface(addr);
            } while(true);
            enum = OldAddressTable.listIterator();
            do
            {
                if(!enum.hasNext())
                    break;
                InetAddress addr = (InetAddress)enum.next();
                if(!CurrentAddressTable.contains(addr))
                    TriggerOldInterface(addr);
            } while(true);
        }
        catch(Exception exception) { }
    }

    public static int GetFreePort(int min, int max, InetAddress OnThisIP)
    {
        Random g = new Random();
        boolean IsOK = true;
        int val;
        do
        {
            val = g.nextInt(max - min) + min;
            try
            {
                ServerSocket test = new ServerSocket(val, 0, OnThisIP);
                test.close();
            }
            catch(Exception e)
            {
                IsOK = false;
            }
        } while(!IsOK);
        return val;
    }

    protected ArrayList NewList;
    protected ArrayList DisList;
    private LifeTimeMonitor InterfacePoller;
    private String HostName;
    private ArrayList AddressTable;
    private ArrayList ActionList;

}
