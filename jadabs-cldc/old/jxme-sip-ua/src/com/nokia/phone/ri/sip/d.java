// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import java.util.Timer;
import java.util.Vector;
import javax.microedition.sip.SipAddress;
import javax.microedition.sip.SipHeader;

// Referenced classes of package com.nokia.phone.ri.sip:
//            u, t, Protocol, a, 
//            x, c

public abstract class d
{

    public d()
    {
        _fldgoto = null;
        l = null;
        o = null;
        _fldcase = new Timer();
        _fldnull = 500;
        b = 0;
        a = null;
        j = null;
        _flddo = null;
        d = null;
        _fldint = 1;
    }

    public void _mthif()
    {
        a = null;
        j = null;
        _flddo = null;
        d = null;
        _fldcase.cancel();
    }

    public static String a(String s)
    {
        String s1 = null;
        if(s == null)
            return null;
        try
        {
            SipHeader sipheader = new SipHeader("Via", s);
            s1 = sipheader.getParameter("branch");
        }
        catch(Exception exception)
        {
            return null;
        }
        return s1;
    }

    public static int a(a a1)
    {
        Object obj = null;
        u u1 = (u)a1;
        if(u1._mthtry())
        {
            String s = u1.a("Route");
            SipAddress sipaddress1 = new SipAddress(s);
            u1._fldnew = sipaddress1.getPort();
            u1._fldfor = sipaddress1.getHost();
            Protocol._mthif("BaseTransaction.routeRequest: Loose routing to " + u1._fldfor + ":" + u1._fldnew);
            return 1;
        } else
        {
            SipAddress sipaddress = new SipAddress(u1._mthbyte().toString());
            Protocol._mthif("BaseTransaction.routeRequest using RequestURI address:\n\t" + sipaddress.getHost() + ":" + sipaddress.getPort());
            u1._fldnew = sipaddress.getPort();
            u1._fldfor = sipaddress.getHost();
            return 1;
        }
    }

    public static int a(a a1, a a2)
    {
        Vector vector = a2._mthif("Via");
        if(vector.size() > 0)
        {
            t t1 = (t)vector.elementAt(0);
            String s = t1.a();
            Protocol._mthif("BaseTransaction.routeResponse using Via:\n\t" + s);
            String s1 = "sip:" + s.substring(s.indexOf(' ') + 1);
            SipAddress sipaddress1 = new SipAddress(s1);
            a1._fldnew = sipaddress1.getPort();
            a1._fldfor = sipaddress1.getHost();
        } else
        {
            Vector vector1 = a2._mthif("Contact");
            if(vector1.size() > 0)
            {
                t t2 = (t)vector1.elementAt(0);
                Protocol._mthif("BaseTransaction.routeResponse using Contact:\n\t" + t2.a());
                SipHeader sipheader = new SipHeader("Contact", t2.a());
                SipAddress sipaddress = new SipAddress(sipheader.getValue());
                a1._fldnew = sipaddress.getPort();
                a1._fldfor = sipaddress.getHost();
            } else
            {
                Protocol._mthif("BaseTransaction.routeResponse missing route information ?");
            }
        }
        return 1;
        Exception exception;
        exception;
        exception.printStackTrace();
        return -1;
    }

    protected void a(short word0)
    {
        _fldint = word0;
    }

    public abstract void a();

    public abstract int _mthdo();

    protected short _fldint;
    protected static final short _fldfor = 1;
    protected static final short _fldbyte = 2;
    protected static final short _fldnew = 3;
    protected static final short k = 4;
    protected static final short _fldif = 5;
    protected static final short m = 6;
    protected static final short f = 7;
    protected static final short _fldtry = 8;
    protected static final Object n = new Object();
    public String _fldgoto;
    public String l;
    public String o;
    protected Timer _fldcase;
    protected static final int i = 500;
    protected static final int g = 4000;
    protected static final int e = 5000;
    protected int _fldnull;
    protected static final int c = 32000;
    protected static final int _fldvoid = 15000;
    protected static final int _fldlong = 500;
    protected static final int _fldelse = 5000;
    protected static final int _fldchar = 5000;
    protected int b;
    protected static x h = null;
    protected u a;
    protected u j;
    protected c _flddo;
    protected a d;

}
