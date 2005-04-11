// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Operation.java

package javax.microedition.xml.rpc;

import com.sun.j2mews.xml.rpc.OperationImpl;
import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;

// Referenced classes of package javax.microedition.xml.rpc:
//            Element, FaultDetailHandler

public class Operation
{

    protected Operation()
    {
    }

    public static Operation newInstance(QName name, Element input, Element output)
    {
        return new OperationImpl(name, input, output);
    }

    public static Operation newInstance(QName name, Element input, Element output, FaultDetailHandler faultDetailHandler)
    {
        return new OperationImpl(name, input, output, faultDetailHandler);
    }

    public void setProperty(String s, String s1)
        throws IllegalArgumentException
    {
    }

    public Object invoke(Object inParams)
        throws JAXRPCException
    {
        return null;
    }

    public static final String SOAPACTION_URI_PROPERTY = "javax.xml.rpc.soap.http.soapaction.uri";

    static 
    {
        SOAPACTION_URI_PROPERTY = "javax.xml.rpc.soap.http.soapaction.uri";
    }
}
