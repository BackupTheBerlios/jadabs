// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MiniWebServerAcceptor.java

package Intel.UPnP;

import java.awt.event.ActionListener;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

// Referenced classes of package Intel.UPnP:
//            AcceptSocketEvent

public class MiniWebServerAcceptor
    implements Runnable
{

    public MiniWebServerAcceptor(ServerSocket MainSocket)
    {
        EventList = new LinkedList();
        AcceptorThread = new Thread(this);
        WebSocket = MainSocket;
        AcceptorThread.start();
    }

    public void dispose()
    {
        AcceptorThread.interrupt();
    }

    public void addActionListener(Object actionListener)
    {
        EventList.addLast(actionListener);
    }

    protected void SetEvent(Socket NewSocket)
    {
        Object elist[] = EventList.toArray();
        for(int id = 0; id < elist.length; id++)
            ((ActionListener)elist[id]).actionPerformed(new AcceptSocketEvent(this, 0, "Accept", NewSocket));

    }

    public void run()
    {
        do
            try
            {
                SetEvent(WebSocket.accept());
            }
            catch(Exception e)
            {
                return;
            }
        while(true);
    }

    protected Thread AcceptorThread;
    protected ServerSocket WebSocket;
    protected LinkedList EventList;
}
