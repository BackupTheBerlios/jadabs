// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ServerException.java

package java.rmi;


// Referenced classes of package java.rmi:
//            RemoteException

public class ServerException extends RemoteException
{

    public ServerException(String s)
    {
        super(s);
    }

    public ServerException(String s, Exception ex)
    {
        super(s, ex);
    }

    private static final long serialVersionUID = 0xbdb8c9fdc1279006L;

    static 
    {
        serialVersionUID = 0xbdb8c9fdc1279006L;
    }
}
