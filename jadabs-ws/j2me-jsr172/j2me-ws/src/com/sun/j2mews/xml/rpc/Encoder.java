// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Encoder.java

package com.sun.j2mews.xml.rpc;

import java.io.OutputStream;
import javax.microedition.xml.rpc.Type;
import javax.xml.rpc.JAXRPCException;

public interface Encoder
{

    public abstract void encode(Object obj, Type type, OutputStream outputstream, String s)
        throws JAXRPCException;
}
