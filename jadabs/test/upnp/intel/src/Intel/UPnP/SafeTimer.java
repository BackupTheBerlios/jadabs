// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SafeTimer.java

package Intel.UPnP;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class SafeTimer
{
    private class TimerHandler extends TimerTask
    {

        public void run()
        {
            TimerSink();
        }

        private TimerHandler()
        {
        }

        TimerHandler(._cls1 x$1)
        {
            this();
        }
    }


    public void addActionListener(ActionListener a)
    {
        ActionList.add(a);
    }

    public void TriggerEvent()
    {
        Object al[] = ActionList.toArray();
        for(int i = 0; i < al.length; i++)
            ((ActionListener)al[i]).actionPerformed(new ActionEvent(this, 0, "OnElapsed"));

    }

    public SafeTimer()
    {
        Interval = 0;
        AutoReset = false;
        ActionList = new ArrayList();
    }

    public SafeTimer(int Milliseconds, boolean Auto)
    {
        this();
        Interval = Milliseconds;
        AutoReset = Auto;
    }

    public void Start()
    {
        TObject = new Timer();
        TObject.schedule(new TimerHandler(null), Interval);
    }

    public void Stop()
    {
        if(TObject != null)
            TObject.cancel();
    }

    protected void TimerSink()
    {
        if(AutoReset)
            TObject.schedule(new TimerHandler(null), Interval);
        TriggerEvent();
    }

    public int Interval;
    public boolean AutoReset;
    protected Timer TObject;
    protected ArrayList ActionList;
}
