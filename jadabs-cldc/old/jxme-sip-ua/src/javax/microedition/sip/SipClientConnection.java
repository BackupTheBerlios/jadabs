// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package javax.microedition.sip;

import java.io.IOException;

// Referenced classes of package javax.microedition.sip:
//            SipConnection, SipException, SipConnectionNotifier, SipClientConnectionListener, 
//            SipRefreshListener

public interface SipClientConnection
    extends SipConnection
{

    public abstract void initRequest(String s, SipConnectionNotifier sipconnectionnotifier)
        throws IllegalArgumentException, SipException;

    public abstract void setRequestURI(String s)
        throws IllegalArgumentException, SipException;

    public abstract void initAck()
        throws SipException;

    public abstract SipClientConnection initCancel()
        throws SipException;

    public abstract boolean receive(long l)
        throws SipException, IOException;

    public abstract void setListener(SipClientConnectionListener sipclientconnectionlistener)
        throws IOException;

    public abstract int enableRefresh(SipRefreshListener siprefreshlistener)
        throws SipException;

    public abstract void setCredentials(String s, String s1, String s2)
        throws SipException;
}
