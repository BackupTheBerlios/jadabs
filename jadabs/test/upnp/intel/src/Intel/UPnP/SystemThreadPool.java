// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SystemThreadPool.java

package Intel.UPnP;

import java.util.ArrayList;
import java.util.LinkedList;

// Referenced classes of package Intel.UPnP:
//            ManualResetEvent

public class SystemThreadPool
{
    private class ThreadInfo
    {

        public int getTaskCount()
        {
            int c = 0;
            synchronized(TaskLock)
            {
                c = TaskQueue.size();
            }
            return c;
        }

        public void AddTask(ThreadPoolRunnable r, Object s)
        {
            Object Task[] = new Object[2];
            Task[0] = r;
            Task[1] = s;
            synchronized(TaskLock)
            {
                TaskQueue.addLast(((Object) (Task)));
            }
            event.SetEvent();
        }

        public void Kill()
        {
            Abort = true;
            event.dispose();
            t.interrupt();
        }

        public Thread t;
        public boolean Abort;
        public ManualResetEvent event;
        public LinkedList TaskQueue;
        protected Object TaskLock;

        public ThreadInfo()
        {
            Abort = false;
            event = new ManualResetEvent(false);
            TaskQueue = new LinkedList();
            TaskLock = new Object();
            t = new Thread(new WorkerThread(null));
            t.start();
        }
    }

    public static interface ThreadPoolRunnable
    {

        public abstract void run(Object obj);
    }


    public SystemThreadPool(int NumThreads)
    {
        TList = new ArrayList();
        idx = 0;
        for(int i = 0; i < NumThreads; i++)
            TList.add(new ThreadInfo());

    }

    protected synchronized int GetNewIndex()
    {
        int RetVal = idx;
        idx++;
        if(idx >= TList.size())
            idx = 0;
        return RetVal;
    }

    protected ThreadInfo GetThread()
    {
        return (ThreadInfo)TList.get(GetNewIndex());
    }

    public void QueueUserWorkItem(ThreadPoolRunnable trp, Object state)
    {
        GetThread().AddTask(trp, state);
    }

    protected ArrayList TList;
    protected int idx;
}
