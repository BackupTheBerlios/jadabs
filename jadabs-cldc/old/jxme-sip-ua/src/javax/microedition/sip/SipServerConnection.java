// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package javax.microedition.sip;


// Referenced classes of package javax.microedition.sip:
//            SipConnection, SipException

public interface SipServerConnection
    extends SipConnection
{

    public abstract void initResponse(int i)
        throws IllegalArgumentException, SipException;

    public abstract void setReasonPhrase(String s)
        throws SipException, IllegalArgumentException;
}
