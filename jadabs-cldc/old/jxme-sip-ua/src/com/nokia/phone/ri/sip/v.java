// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.sip.SipException;

// Referenced classes of package com.nokia.phone.ri.sip:
//            t, o, g, l, 
//            e, Protocol

public class v
{

    public v(String s)
        throws IllegalArgumentException
    {
        _fldfor = null;
        _fldchar = null;
        a = null;
        _fldnew = null;
        _fldif = null;
        _fldelse = null;
        _fldint = null;
        _fldcase = new Vector();
        _fldbyte = new Vector();
        _fldtry = null;
        _flddo = null;
        _fldtry = s;
        _mthbyte(s);
    }

    public v(String s, String s1)
        throws IllegalArgumentException
    {
        _fldfor = null;
        _fldchar = null;
        a = null;
        _fldnew = null;
        _fldif = null;
        _fldelse = null;
        _fldint = null;
        _fldcase = new Vector();
        _fldbyte = new Vector();
        _fldtry = null;
        _flddo = null;
        StringBuffer stringbuffer = new StringBuffer();
        if(s != null)
        {
            stringbuffer.append(s);
            stringbuffer.append(" ");
        }
        stringbuffer.append(s1);
        _fldtry = stringbuffer.toString();
        _mthbyte(_fldtry);
    }

    private void _mthbyte(String s)
        throws IllegalArgumentException
    {
        if(s == null)
            throw new NullPointerException("null address String");
        if(s.length() == 0)
            throw new IllegalArgumentException("Empty address");
        _fldtry = s;
        try
        {
            l l1 = g.a(_fldtry, (short)0);
            _fldchar = l1._fldtry;
            _fldfor = l1.a;
            a = l1._fldnew;
            _fldint = l1._fldif;
            _fldnew = l1._fldcase;
            _fldelse = l1._fldfor;
            _flddo = l1._flddo;
            _fldcase = l1._fldbyte;
            _fldif = l1._fldint;
        }
        catch(SipException sipexception)
        {
            throw new IllegalArgumentException(sipexception.getMessage());
        }
        catch(NumberFormatException numberformatexception)
        {
            throw new IllegalArgumentException(numberformatexception.getMessage());
        }
    }

    private void _mthint(String s)
        throws SipException
    {
        l l1 = g.a(s, (short)5);
    }

    private void a(String s, String s1)
        throws SipException
    {
        s = s.toLowerCase();
        if(_mthnew(s) != null)
        {
            throw new SipException("Duplicate parameter '" + s + "'", (byte)0);
        } else
        {
            _fldcase.addElement(new t(s, s1));
            return;
        }
    }

    private String a(String s, String s1, String s2, String s3)
    {
        StringBuffer stringbuffer = new StringBuffer();
        if(s != null && s.length() > 0)
        {
            stringbuffer.append(s);
            stringbuffer.append(":");
        }
        if(s1 != null)
        {
            stringbuffer.append(s1);
            stringbuffer.append("@");
        }
        if(s2 != null)
            stringbuffer.append(s2);
        if(s3 != null && s3.length() > 0)
        {
            Integer.parseInt(s3);
            stringbuffer.append(":");
            stringbuffer.append(s3);
        }
        return stringbuffer.toString();
    }

    public String _mthtry()
    {
        return _fldchar;
    }

    public void _mthfor(String s)
        throws IllegalArgumentException
    {
        if(s == null)
            throw new IllegalArgumentException("Illegal display name '" + s + "'");
        s = s.trim();
        v v1 = null;
        try
        {
            v1 = new v(s + " sip:host");
        }
        catch(Exception exception)
        {
            throw new IllegalArgumentException("Illegal display name '" + s + "'");
        }
        _fldchar = v1._mthtry();
        _fldif = null;
    }

    public String _mthint()
    {
        return _fldfor;
    }

    public void _mthtry(String s)
        throws IllegalArgumentException
    {
        if(s == null)
            throw new NullPointerException("Try to set null scheme");
        s = s.trim();
        if(s.length() == 0)
            throw new IllegalArgumentException("Empty scheme '" + s + "'");
        try
        {
            Vector vector = e.a(s);
            if(vector.size() != 2)
                throw new IllegalArgumentException("Illegal scheme '" + s + "'");
            g.a((o)vector.elementAt(0));
        }
        catch(Exception exception)
        {
            throw new IllegalArgumentException(exception.getMessage());
        }
        _fldfor = s;
        _flddo = a(_fldfor, a, _fldnew, _fldelse);
        _fldif = null;
    }

    public String a()
    {
        return a;
    }

    public void a(String s)
        throws IllegalArgumentException
    {
        if(s != null)
        {
            s = s.trim();
            if(s.length() == 0)
                throw new IllegalArgumentException("Empty user info");
        }
        v v1 = null;
        try
        {
            v1 = new v(a(_fldfor, s, _fldnew, _fldelse));
        }
        catch(Exception exception)
        {
            throw new IllegalArgumentException("Illegal user info '" + s + "'");
        }
        a = v1.a();
        _flddo = v1._mthnew();
        _fldif = null;
    }

    public String _mthnew()
    {
        return _flddo;
    }

    public void _mthif(String s)
        throws IllegalArgumentException
    {
        v v1 = null;
        try
        {
            v1 = new v(s);
        }
        catch(Exception exception)
        {
            throw new IllegalArgumentException("Illegal URI '" + s + "'");
        }
        _flddo = v1._mthnew();
        _fldfor = v1._mthint();
        a = v1.a();
        _fldnew = v1._mthif();
        _fldelse = Integer.toString(v1._mthfor());
        _fldif = null;
    }

    public String _mthif()
    {
        return _fldnew;
    }

    public void _mthdo(String s)
        throws IllegalArgumentException
    {
        if(s == null)
            throw new NullPointerException("try to set null host");
        if(s != null)
        {
            s = s.trim();
            if(s.length() == 0)
                throw new IllegalArgumentException("Empty host");
        }
        v v1 = null;
        try
        {
            v1 = new v(a(_fldfor, a, s, _fldelse));
        }
        catch(Exception exception)
        {
            throw new IllegalArgumentException("Illegal host '" + s + "'");
        }
        _fldnew = v1._mthif();
        _flddo = v1._mthnew();
        _fldif = null;
    }

    public int _mthfor()
    {
        if(_fldif != null)
            return 0;
        if(_fldelse == null)
            return 5060;
        else
            return Integer.parseInt(_fldelse);
    }

    public void a(int i)
        throws IllegalArgumentException
    {
        if(i < 0 || i > 65535)
            throw new IllegalArgumentException("Port number out of range '" + i + "'");
        if(i == 0)
            _fldelse = null;
        else
            _fldelse = Integer.toString(i);
        _flddo = a(_fldfor, a, _fldnew, _fldelse);
        _fldif = null;
    }

    public String _mthnew(String s)
    {
        for(Enumeration enumeration = _fldcase.elements(); enumeration.hasMoreElements();)
        {
            t t1 = (t)enumeration.nextElement();
            if(t1._mthfor().equals(s))
                return t1.a();
        }

        return null;
    }

    public void _mthif(String s, String s1)
        throws IllegalArgumentException
    {
        if(s != null)
        {
            s = s.trim();
            if(s.length() == 0)
                throw new IllegalArgumentException("Parameter name empty");
        } else
        {
            throw new NullPointerException("Parameter name null");
        }
        if(s1 != null)
            s1 = s1.trim();
        String s2 = _mthnew(s);
        if(s2 != null)
            _mthcase(s);
        StringBuffer stringbuffer = new StringBuffer(";");
        stringbuffer.append(s);
        if(s1 != null)
        {
            stringbuffer.append("=");
            stringbuffer.append(s1);
        }
        try
        {
            _mthint(stringbuffer.toString());
        }
        catch(Exception exception)
        {
            if(s2 != null)
            {
                _mthcase(s);
                try
                {
                    a(s, s2);
                }
                catch(SipException sipexception)
                {
                    Protocol._mthif("Impl. error SHOULD NOT; " + sipexception.getMessage());
                }
            }
            throw new IllegalArgumentException(exception.getMessage());
        }
        try
        {
            if(s1 == null)
                a(s, "");
            else
                a(s, s1);
        }
        catch(Exception exception1)
        {
            throw new IllegalArgumentException("INTERNAL ERROR: " + exception1.getMessage());
        }
    }

    public void _mthcase(String s)
    {
        if(s == null)
            return;
        Enumeration enumeration = _fldcase.elements();
        do
        {
            if(!enumeration.hasMoreElements())
                break;
            t t1 = (t)enumeration.nextElement();
            if(!t1._mthfor().equals(s))
                continue;
            _fldcase.removeElement(t1);
            break;
        } while(true);
    }

    public String[] _mthdo()
    {
        if(!_fldcase.isEmpty())
        {
            String as[] = new String[_fldcase.size()];
            for(int i = 0; i < _fldcase.size(); i++)
                as[i] = ((t)_fldcase.elementAt(i))._mthfor();

            return as;
        } else
        {
            return null;
        }
    }

    public String toString()
    {
        StringBuffer stringbuffer = new StringBuffer();
        Object obj = null;
        if(_fldif != null)
            return _fldif;
        if(_fldchar != null)
        {
            if(_fldchar.indexOf(" ") >= 0)
            {
                stringbuffer.append("\"");
                stringbuffer.append(_fldchar);
                stringbuffer.append("\"");
            } else
            {
                stringbuffer.append(_fldchar);
            }
            stringbuffer.append(" <");
        }
        if(_fldfor != null)
        {
            stringbuffer.append(_fldfor);
            if(_fldfor.length() > 0)
                stringbuffer.append(":");
        } else
        {
            stringbuffer.append("sip:");
        }
        if(a != null)
        {
            stringbuffer.append(a);
            stringbuffer.append("@");
        }
        if(_fldnew != null)
            stringbuffer.append(_fldnew);
        else
            return null;
        if(_fldelse != null)
        {
            stringbuffer.append(":");
            stringbuffer.append(_fldelse);
        }
        if(!_fldcase.isEmpty())
        {
            stringbuffer.append(";");
            Enumeration enumeration = _fldcase.elements();
            t t1 = null;
            do
            {
                if(!enumeration.hasMoreElements())
                    break;
                if(t1 != null)
                    stringbuffer.append(";");
                t1 = (t)enumeration.nextElement();
                stringbuffer.append(t1._mthfor());
                if(!t1.a().equals(""))
                {
                    stringbuffer.append("=");
                    stringbuffer.append(t1.a());
                }
            } while(true);
        }
        if(_fldchar != null)
            stringbuffer.append(">");
        if(stringbuffer.length() > 0)
            return stringbuffer.toString();
        else
            return null;
    }

    private String _fldfor;
    private String _fldchar;
    private String a;
    private String _fldnew;
    private String _fldif;
    private String _fldelse;
    private String _fldint;
    private Vector _fldcase;
    private Vector _fldbyte;
    private String _fldtry;
    private String _flddo;
}
