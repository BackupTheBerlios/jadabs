// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;


public class t
{

    public t()
    {
    }

    public t(String s, String s1)
    {
        a = s;
        _fldif = s1;
    }

    public String _mthfor()
    {
        return a;
    }

    public String a()
    {
        return _fldif;
    }

    public void _mthif(String s)
    {
        a = s;
    }

    public void a(String s)
    {
        _fldif = s;
    }

    public String _mthif()
    {
        StringBuffer stringbuffer = new StringBuffer(a);
        stringbuffer.append(": ");
        stringbuffer.append(_fldif);
        return stringbuffer.toString();
    }

    public String _mthdo()
    {
        StringBuffer stringbuffer = new StringBuffer(a);
        stringbuffer.append("=");
        stringbuffer.append(_fldif);
        return stringbuffer.toString();
    }

    private String a;
    private String _fldif;
}
