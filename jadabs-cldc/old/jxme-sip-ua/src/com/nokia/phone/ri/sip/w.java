// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import java.io.IOException;
import java.util.Hashtable;
import javax.microedition.sip.SipAddress;
import javax.microedition.sip.SipHeader;

// Referenced classes of package com.nokia.phone.ri.sip:
//            r, m, Protocol, x, 
//            a

public class w
{

    public w(x x1, int i, boolean flag)
        throws IOException
    {
        _flddo = 0;
        _fldif = null;
        a = null;
        _fldfor = null;
        _fldint = false;
        _fldnew = x1;
        _fldif = new r(x1, i);
        _flddo = _fldif.a();
        _fldfor = new Hashtable();
        _fldint = flag;
    }

    public int _mthfor()
    {
        return _flddo;
    }

    public String _mthint()
    {
        return _fldif._mthdo();
    }

    public SipAddress _mthdo()
    {
        return a;
    }

    public void a(SipAddress sipaddress)
    {
        a = sipaddress;
    }

    public r _mthif()
    {
        return _fldif;
    }

    public synchronized boolean _mthif(m m1)
    {
        if(!_fldint)
        {
            String s = m1.a();
            if(s == null)
                s = "";
            if(_fldfor.isEmpty())
            {
                _fldfor.put(s, m1);
                return true;
            } else
            {
                return false;
            }
        }
        String s1 = m1.a();
        if(_fldfor.containsKey(s1))
        {
            return false;
        } else
        {
            _fldfor.put(s1, m1);
            return true;
        }
    }

    public synchronized void a(m m1)
    {
        String s = m1.a();
        if(!_fldint)
        {
            Protocol._mthif("ListeningPoint.removeNotifier: port " + _flddo);
            _fldif._mthif();
            _fldif = null;
            _fldnew.a(_flddo);
        } else
        {
            Protocol._mthif("ListeningPoint.removeNotifier: shared " + s);
            _fldfor.remove(s);
        }
    }

    public synchronized m a(a a1)
    {
        String s = a1.a("Accept-Contact");
        SipHeader sipheader = null;
        String s1 = null;
        if(s != null)
        {
            sipheader = new SipHeader("Accept-Contact", s);
            if(sipheader != null)
                s1 = sipheader.getParameter("type");
        }
        if(s1 != null)
            Protocol._mthif("ListeningPoint.getNotifierFor:\n\t\t" + sipheader + "\n\t\tsearch key = " + s1);
        else
            Protocol._mthif("ListeningPoint.getNotifierFor port: " + _flddo);
        if(!_fldint)
        {
            if(_fldfor.containsKey(""))
                return (m)_fldfor.get("");
            if(s1 == null)
                return null;
            else
                return (m)_fldfor.get(s1);
        }
        if(s1 == null)
        {
            return null;
        } else
        {
            m m1 = (m)_fldfor.get(s1);
            return m1;
        }
    }

    public boolean a()
    {
        return _fldint;
    }

    private int _flddo;
    private r _fldif;
    private SipAddress a;
    private Hashtable _fldfor;
    private boolean _fldint;
    private x _fldnew;
}
