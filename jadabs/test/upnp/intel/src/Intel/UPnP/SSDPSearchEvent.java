// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SSDPSearchEvent.java

package Intel.UPnP;

import java.awt.event.ActionEvent;
import java.net.InetAddress;

public class SSDPSearchEvent extends ActionEvent
{

    public SSDPSearchEvent(Object xSrc, int yID, String zCmd, InetAddress local, InetAddress origin, int Port, String ST)
    {
        super(xSrc, yID, zCmd);
        Source = origin;
        SourcePort = Port;
        SearchTarget = ST;
        Local = local;
    }

    public InetAddress Local;
    public InetAddress Source;
    public int SourcePort;
    public String SearchTarget;
}
