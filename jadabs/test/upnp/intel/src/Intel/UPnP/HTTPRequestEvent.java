// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   HTTPRequestEvent.java

package Intel.UPnP;

import java.awt.event.ActionEvent;

// Referenced classes of package Intel.UPnP:
//            HTTPMessage, HTTPSession

public class HTTPRequestEvent extends ActionEvent
{

    public HTTPRequestEvent(Object src, int id, String cmd, HTTPMessage request, HTTPSession current)
    {
        super(src, id, cmd);
        HTTPRequest = request;
        Session = current;
    }

    public HTTPMessage HTTPRequest;
    public HTTPSession Session;
}
