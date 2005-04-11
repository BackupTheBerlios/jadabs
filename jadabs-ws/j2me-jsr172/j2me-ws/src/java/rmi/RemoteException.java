// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RemoteException.java

package java.rmi;

import java.io.IOException;

public class RemoteException extends IOException
{

    public RemoteException()
    {
    }

    public RemoteException(String s)
    {
        super(s);
    }

    public RemoteException(String s, Throwable ex)
    {
        super(s);
        detail = ex;
    }

    public String getMessage()
    {
        if(detail == null)
            return super.getMessage();
        else
            return super.getMessage() + "; nested exception is: \n\t" + detail.toString();
    }

    private static final long serialVersionUID = 0xb88c9d4edee47a22L;
    public Throwable detail;

    static 
    {
        serialVersionUID = 0xb88c9d4edee47a22L;
    }
}
