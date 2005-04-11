// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FaultDecoder.java

package com.sun.j2mews.xml.rpc;

import java.io.InputStream;
import javax.microedition.xml.rpc.FaultDetailHandler;
import javax.xml.rpc.JAXRPCException;

public interface FaultDecoder
{

    public abstract Object decodeFault(FaultDetailHandler faultdetailhandler, InputStream inputstream, String s, long l)
        throws JAXRPCException;
}
