// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SOAPDecoder.java

package com.sun.j2mews.xml.rpc;

import java.util.Vector;

class TypedVector extends Vector
{

    TypedVector(int type, boolean nillable)
    {
        this.type = type;
        this.nillable = nillable;
    }

    public int type;
    public boolean nillable;
}
