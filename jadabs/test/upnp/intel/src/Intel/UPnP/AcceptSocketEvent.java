// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AcceptSocketEvent.java

package Intel.UPnP;

import java.awt.event.ActionEvent;
import java.net.Socket;

public class AcceptSocketEvent extends ActionEvent
{

    public AcceptSocketEvent(Object src, int id, String cmd, Socket TheSocket)
    {
        super(src, id, cmd);
        NewSocket = TheSocket;
    }

    public Socket NewSocket;
}
