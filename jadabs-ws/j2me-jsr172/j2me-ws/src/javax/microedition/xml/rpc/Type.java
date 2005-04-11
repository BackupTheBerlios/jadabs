// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Type.java

package javax.microedition.xml.rpc;


public class Type
{

    Type(int value)
    {
        this.value = value;
    }

    public static final Type BOOLEAN = new Type(0);
    public static final Type BYTE = new Type(1);
    public static final Type SHORT = new Type(2);
    public static final Type INT = new Type(3);
    public static final Type LONG = new Type(4);
    public static final Type FLOAT = new Type(5);
    public static final Type DOUBLE = new Type(6);
    public static final Type STRING = new Type(7);
    public final int value;

}
