// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package javax.microedition.sip;

import java.io.*;
import javax.microedition.io.Connection;

// Referenced classes of package javax.microedition.sip:
//            SipException, SipDialog

public interface SipConnection
    extends Connection
{

    public abstract void send()
        throws IOException, InterruptedIOException, SipException;

    public abstract void setHeader(String s, String s1)
        throws SipException, IllegalArgumentException;

    public abstract void addHeader(String s, String s1)
        throws SipException, IllegalArgumentException;

    public abstract void removeHeader(String s)
        throws SipException;

    public abstract String[] getHeaders(String s);

    public abstract String getHeader(String s);

    public abstract String getMethod();

    public abstract String getRequestURI();

    public abstract int getStatusCode();

    public abstract String getReasonPhrase();

    public abstract SipDialog getDialog();

    public abstract InputStream openContentInputStream()
        throws IOException, SipException;

    public abstract OutputStream openContentOutputStream()
        throws IOException, SipException;
}
