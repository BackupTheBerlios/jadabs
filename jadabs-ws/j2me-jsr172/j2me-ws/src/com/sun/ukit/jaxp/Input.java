// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Input.java

package com.sun.ukit.jaxp;

import java.io.Reader;

class Input
{

    public Input(short buffsize)
    {
        chars = new char[buffsize];
        chLen = (char)chars.length;
    }

    public Input(char buff[])
    {
        chars = buff;
        chLen = (char)chars.length;
    }

    public Input()
    {
    }

    public String pubid;
    public String sysid;
    public Reader src;
    public char chars[];
    public char chLen;
    public char chIdx;
    public Input next;
}
