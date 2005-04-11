// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   JAXRPCException.java

package javax.xml.rpc;


public class JAXRPCException extends RuntimeException
{

    public JAXRPCException()
    {
    }

    public JAXRPCException(String message)
    {
        super(message);
    }

    public JAXRPCException(String message, Throwable cause)
    {
        super(message);
        this.cause = cause;
    }

    public JAXRPCException(Throwable cause)
    {
        super(cause != null ? cause.toString() : null);
        this.cause = cause;
    }

    public Throwable getLinkedCause()
    {
        return cause;
    }

    private Throwable cause;
}
