// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Decoder.java

package com.sun.j2mews.xml.rpc;

import java.io.InputStream;
import javax.microedition.xml.rpc.Type;
import javax.xml.rpc.JAXRPCException;

public interface Decoder
{

    public abstract Object decode(Type type, InputStream inputstream, String s, long l)
        throws JAXRPCException;
}
