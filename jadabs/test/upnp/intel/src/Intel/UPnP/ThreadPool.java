// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ThreadPool.java

package Intel.UPnP;

import java.util.ArrayList;
import java.util.LinkedList;

// Referenced classes of package Intel.UPnP:
//            ManualResetEvent

public class ThreadPool
{
    private class ThreadController
        implements Runnable
    {

        public void run()
        {
            while(TaskEvent.WaitForTrigger()) 
            {
                boolean more = true;
                while(more) 
                    more = RunNextTask();
            }
            int y = 5;
        }

        protected boolean RunNextTask()
        {
            boolean more = false;
            Object Args[];
            synchronized(TaskLock)
            {
                if(TaskList.size() > 0)
                    Args = (Object[])TaskList.remove(0);
                else
                    Args = null;
                if(TaskList.size() > 0)
                    more = true;
            }
            if(Args != null)
            {
                ThreadPoolRunnable t = (ThreadPoolRunnable)Args[0];
                t.run(Args[1]);
            }
            return more;
        }

        private ThreadController()
        {
        }

        ThreadController(._cls1 x$1)
        {
            this();
        }
    }

    public static interface ThreadPoolRunnable
    {

        public abstract void run(Object obj);
    }


    public ThreadPool(int MaxThreads)
    {
        TList = new ArrayList();
        ThreadManager = new ThreadController(null);
        TaskList = new LinkedList();
        TaskLock = new Object();
        TaskEvent = new ManualResetEvent(false);
        for(int i = 0; i < MaxThreads; i++)
        {
            TList.add(new Thread(new ThreadController(null)));
            ((Thread)TList.get(i)).start();
        }

    }

    public void QueueUserWorkItem(ThreadPoolRunnable trp, Object state)
    {
        Object Arg[] = new Object[2];
        Arg[0] = trp;
        Arg[1] = state;
        synchronized(TaskLock)
        {
            TaskList.addLast(((Object) (Arg)));
        }
        TaskEvent.SINGLE_SetEvent();
    }

    protected ArrayList TList;
    protected ThreadController ThreadManager;
    protected LinkedList TaskList;
    protected Object TaskLock;
    protected ManualResetEvent TaskEvent;
}
