// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LifeTimeMonitor.java

package Intel.UPnP;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

// Referenced classes of package Intel.UPnP:
//            SafeTimer

public class LifeTimeMonitor
{
    private class SafeTimerHandler
        implements ActionListener
    {

        public void actionPerformed(ActionEvent e)
        {
            OnTimedEvent();
        }

        private SafeTimerHandler()
        {
        }

        SafeTimerHandler(._cls1 x$1)
        {
            this();
        }
    }

    public class LifeTimeMonitorEvent extends ActionEvent
    {

        public Object State;

        public LifeTimeMonitorEvent(Object src, int id, String cmd, Object state)
        {
            super(src, id, cmd);
            State = state;
        }
    }


    public void addActionListener(ActionListener a)
    {
        ActionList.add(a);
    }

    protected void TriggerEvent(Object x)
    {
        Object c[] = ActionList.toArray();
        for(int i = 0; i < c.length; i++)
            ((ActionListener)c[i]).actionPerformed(new LifeTimeMonitorEvent(this, 0, "Expired", x));

    }

    public LifeTimeMonitor()
    {
        MonitorList = new TreeMap();
        MonitorLock = new Object();
        SafeNotifyTimer = new SafeTimer();
        ActionList = new ArrayList();
        SafeNotifyTimer.addActionListener(new SafeTimerHandler(null));
        SafeNotifyTimer.AutoReset = false;
    }

    public void OnTimedEvent()
    {
        ArrayList eventList = new ArrayList();
        synchronized(MonitorLock)
        {
            for(; MonitorList.size() > 0 && ((Date)MonitorList.firstKey()).compareTo(new Date((new Date()).getTime() + (long)1000)) < 0; MonitorList.remove(MonitorList.firstKey()))
                eventList.add(MonitorList.get(MonitorList.firstKey()));

            if(MonitorList.size() > 0)
            {
                SafeNotifyTimer.Interval = (int)(((Date)MonitorList.firstKey()).getTime() - (new Date()).getTime());
                SafeNotifyTimer.Start();
            }
        }
        for(ListIterator enum = eventList.listIterator(); enum.hasNext(); TriggerEvent(enum.next()));
    }

    public boolean Remove(Object obj)
    {
        if(obj == null)
            return false;
        boolean RetVal = false;
        synchronized(MonitorLock)
        {
            Iterator enum = MonitorList.values().iterator();
            boolean HasNext = enum.hasNext();
            boolean Match = false;
            Object ThisObject = null;
            do
            {
                if(!HasNext)
                    break;
                ThisObject = enum.next();
                try
                {
                    Comparable c = (Comparable)ThisObject;
                    if(c.compareTo(obj) == 0)
                        Match = true;
                    else
                        Match = false;
                }
                catch(ClassCastException cce)
                {
                    if(ThisObject == obj)
                        Match = true;
                    else
                        Match = false;
                }
                if(Match)
                {
                    SafeNotifyTimer.Stop();
                    RetVal = true;
                    enum.remove();
                    break;
                }
                Match = false;
                HasNext = enum.hasNext();
            } while(true);
        }
        OnTimedEvent();
        return RetVal;
    }

    public void Add(Object obj, int secondTimeout)
    {
        if(obj == null)
            return;
        synchronized(MonitorLock)
        {
            SafeNotifyTimer.Stop();
            Iterator enum = MonitorList.values().iterator();
            do
            {
                if(!enum.hasNext())
                    break;
                if(((Comparable)enum.next()).compareTo(obj) != 0)
                    continue;
                enum.remove();
                break;
            } while(true);
            Date eventTriggerTime;
            for(eventTriggerTime = new Date((new Date()).getTime() + (long)(secondTimeout * 1000)); MonitorList.containsKey(eventTriggerTime); eventTriggerTime = new Date(eventTriggerTime.getTime() + (long)1));
            MonitorList.put(eventTriggerTime, obj);
        }
        OnTimedEvent();
    }

    private TreeMap MonitorList;
    private Object MonitorLock;
    private SafeTimer SafeNotifyTimer;
    private ArrayList ActionList;
}
