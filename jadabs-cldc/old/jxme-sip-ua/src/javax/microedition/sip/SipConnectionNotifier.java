// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package javax.microedition.sip;

import java.io.IOException;
import java.io.InterruptedIOException;
import javax.microedition.io.Connection;

// Referenced classes of package javax.microedition.sip:
//            SipException, SipServerConnection, SipServerConnectionListener

public interface SipConnectionNotifier
    extends Connection
{

    public abstract SipServerConnection acceptAndOpen()
        throws IOException, InterruptedIOException, SipException;

    public abstract void setListener(SipServerConnectionListener sipserverconnectionlistener)
        throws IOException;

    public abstract String getLocalAddress()
        throws IOException;

    public abstract int getLocalPort()
        throws IOException;
}
