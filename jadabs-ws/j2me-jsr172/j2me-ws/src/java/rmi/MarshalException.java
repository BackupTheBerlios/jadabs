// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   MarshalException.java

package java.rmi;


// Referenced classes of package java.rmi:
//            RemoteException

public class MarshalException extends RemoteException
{

    public MarshalException(String s)
    {
        super(s);
    }

    public MarshalException(String s, Exception ex)
    {
        super(s, ex);
    }

    private static final long serialVersionUID = 0x565e821426c57db0L;

    static 
    {
        serialVersionUID = 0x565e821426c57db0L;
    }
}
