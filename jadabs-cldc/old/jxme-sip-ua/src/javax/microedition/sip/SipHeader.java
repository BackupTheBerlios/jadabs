// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package javax.microedition.sip;

import com.nokia.phone.ri.sip.q;

public class SipHeader
{

    public SipHeader(String s, String s1)
        throws IllegalArgumentException
    {
        a = new q(s, s1);
    }

    public void setName(String s)
        throws IllegalArgumentException
    {
        a._mthdo(s);
    }

    public String getName()
    {
        return a._mthdo();
    }

    public String getValue()
    {
        return a._mthif();
    }

    public String getHeaderValue()
    {
        return a._mthfor();
    }

    public void setValue(String s)
        throws IllegalArgumentException
    {
        a._mthelse(s);
    }

    public String getParameter(String s)
    {
        return a._mthnew(s);
    }

    public String[] getParameterNames()
    {
        return a.a();
    }

    public void setParameter(String s, String s1)
        throws IllegalArgumentException
    {
        a.a(s, s1);
    }

    public void removeParameter(String s)
    {
        a._mthchar(s);
    }

    public String toString()
    {
        return a.toString();
    }

    private q a;
}
