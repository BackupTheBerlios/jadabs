// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SSDPNotifyEvent.java

package Intel.UPnP;

import java.awt.event.ActionEvent;
import java.net.InetAddress;
import java.net.URL;

public class SSDPNotifyEvent extends ActionEvent
{

    public SSDPNotifyEvent(Object source, int id, String cmd, InetAddress SourceAddress, int port, boolean IsAlive, URL locuri, 
            String USN, String ST, int MA)
    {
        super(source, id, cmd);
        Source = SourceAddress;
        SourcePort = port;
        Alive = IsAlive;
        UniqueName = USN;
        LocationURL = locuri;
        SearchTarget = ST;
        MaxAge = MA;
    }

    public InetAddress Source;
    public int SourcePort;
    public boolean Alive;
    public String UniqueName;
    public URL LocationURL;
    public String SearchTarget;
    public int MaxAge;
}
