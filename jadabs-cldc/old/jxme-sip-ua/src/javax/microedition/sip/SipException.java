// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package javax.microedition.sip;

import java.io.IOException;

public class SipException extends IOException
{

    public SipException(byte byte0)
    {
        a(byte0);
    }

    public SipException(String s, byte byte0)
    {
        super(s);
        a(byte0);
    }

    public byte getErrorCode()
    {
        return a;
    }

    private void a(byte byte0)
    {
        if(byte0 < 0 || byte0 > 8)
            a = 0;
        else
            a = byte0;
    }

    public static final byte TRANSPORT_NOT_SUPPORTED = 1;
    public static final byte DIALOG_UNAVAILABLE = 2;
    public static final byte UNKNOWN_TYPE = 3;
    public static final byte UNKNOWN_LENGTH = 4;
    public static final byte INVALID_STATE = 5;
    public static final byte INVALID_OPERATION = 6;
    public static final byte TRANSACTION_UNAVAILABLE = 7;
    public static final byte INVALID_MESSAGE = 8;
    public static final byte GENERAL_ERROR = 0;
    private byte a;
}
