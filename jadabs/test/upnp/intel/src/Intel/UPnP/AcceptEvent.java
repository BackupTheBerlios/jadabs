// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   AcceptEvent.java

package Intel.UPnP;

import java.awt.event.ActionEvent;

public class AcceptEvent extends ActionEvent
{

    public AcceptEvent(Object src, int id, String cmd)
    {
        super(src, id, cmd);
    }
}
