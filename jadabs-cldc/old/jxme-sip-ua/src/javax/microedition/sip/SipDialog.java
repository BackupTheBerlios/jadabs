// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package javax.microedition.sip;


// Referenced classes of package javax.microedition.sip:
//            SipException, SipClientConnection, SipConnection

public interface SipDialog
{

    public abstract SipClientConnection getNewClientConnection(String s)
        throws IllegalArgumentException, SipException;

    public abstract boolean isSameDialog(SipConnection sipconnection);

    public abstract byte getState();

    public abstract String getDialogID();

    public static final byte EARLY = 1;
    public static final byte CONFIRMED = 2;
    public static final byte TERMINATED = 0;
}
