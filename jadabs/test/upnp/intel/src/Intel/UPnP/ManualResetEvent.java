// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ManualResetEvent.java

package Intel.UPnP;


public class ManualResetEvent
{

    public ManualResetEvent(boolean InitStateSignaled)
    {
        StateLock = new Object();
        State = false;
        InvalidState = false;
        State = InitStateSignaled;
    }

    public void dispose()
    {
        InvalidState = true;
    }

    public synchronized void SINGLE_SetEvent()
    {
        SetSignalState(true);
        byte byte0;
        try
        {
            notify();
        }
        catch(Exception e)
        {
            byte0 = 5;
        }
    }

    public synchronized void SetEvent()
    {
        SetSignalState(true);
        byte byte0;
        try
        {
            notifyAll();
        }
        catch(Exception e)
        {
            byte0 = 5;
        }
    }

    public void ResetEvent()
    {
        SetSignalState(false);
    }

    public synchronized boolean WaitForTrigger()
    {
        try
        {
            wait();
            boolean flag = true;
            return flag;
        }
        catch(Exception e)
        {
            boolean flag1 = false;
            return flag1;
        }
    }

    public synchronized boolean WaitForEvent()
    {
        if(InvalidState)
            return false;
        if(!GetSignalState())
        {
            boolean flag2;
            try
            {
                wait();
                boolean flag = true;
                return flag;
            }
            catch(IllegalMonitorStateException e)
            {
                boolean flag1 = false;
                return flag1;
            }
            catch(InterruptedException ee)
            {
                flag2 = false;
            }
            return flag2;
        } else
        {
            return true;
        }
    }

    public synchronized boolean WaitForEvent(long timeout)
    {
        if(InvalidState)
            return false;
        if(GetSignalState())
            return true;
        byte byte0;
        try
        {
            wait(timeout);
            boolean flag = true;
            return flag;
        }
        catch(Exception e)
        {
            byte0 = 5;
        }
        return false;
    }

    protected boolean GetSignalState()
    {
        boolean RetVal = false;
        RetVal = State;
        return RetVal;
    }

    protected void SetSignalState(boolean TheState)
    {
        State = TheState;
    }

    protected Object StateLock;
    protected boolean State;
    protected boolean InvalidState;
}
