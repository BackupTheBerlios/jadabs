// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FaultDetailException.java

package javax.microedition.xml.rpc;

import javax.xml.namespace.QName;

public class FaultDetailException extends Exception
{

    public FaultDetailException(QName faultDetailName, Object faultDetail)
    {
        this.faultDetail = faultDetail;
        this.faultDetailName = faultDetailName;
    }

    public Object getFaultDetail()
    {
        return faultDetail;
    }

    public QName getFaultDetailName()
    {
        return faultDetailName;
    }

    private Object faultDetail;
    private QName faultDetailName;
}
