// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import java.util.*;
import javax.microedition.sip.SipException;

// Referenced classes of package com.nokia.phone.ri.sip:
//            o, e

public class q
{
    private class a
    {

        public void a(boolean flag, char c)
        {
            _flddo = flag;
            _fldif = c;
        }

        public boolean a(String s)
        {
            if(s == null)
                return false;
            if(s.length() == 0)
                return false;
            else
                return a.containsKey(s);
        }

        public String _mthif(String s)
        {
            if(!a(s))
                return null;
            else
                return (String)a.get(s);
        }

        public Enumeration _mthdo()
        {
            if(!_mthfor())
                return null;
            else
                return a.elements();
        }

        public String[] a()
        {
            if(!_mthfor())
                return null;
            Enumeration enumeration = a.keys();
            String as[] = new String[a.size()];
            for(short word0 = 0; enumeration.hasMoreElements(); word0++)
                as[word0] = (String)enumeration.nextElement();

            return as;
        }

        public boolean _mthfor()
        {
            return a.size() != 0;
        }

        public void _mthdo(String s)
        {
            if(s == null)
                return;
            if(s.length() == 0)
                return;
            if(!_mthfor())
            {
                return;
            } else
            {
                a.remove(s);
                return;
            }
        }

        public void _mthif()
        {
            a.clear();
        }

        public void a(String s, String s1)
            throws IllegalArgumentException, SipException
        {
            if(s == null)
                throw new NullPointerException("parameter name null");
            if(s.length() == 0)
                throw new IllegalArgumentException("parameter name empty");
            if(s1 == null)
            {
                throw new NullPointerException("parameter value null");
            } else
            {
                a.put(s, s1);
                return;
            }
        }

        public String toString()
        {
            if(!_mthfor())
                return "";
            StringBuffer stringbuffer = new StringBuffer();
            Enumeration enumeration = a.keys();
            do
            {
                if(!enumeration.hasMoreElements())
                    break;
                String s = (String)enumeration.nextElement();
                String s1 = (String)a.get(s);
                if(s1 != null)
                {
                    if(!_flddo)
                        stringbuffer.append(_fldif);
                    stringbuffer.append(s);
                    if(s1.length() != 0)
                    {
                        stringbuffer.append('=');
                        stringbuffer.append(s1);
                    }
                    if(_flddo && enumeration.hasMoreElements())
                        stringbuffer.append(_fldif);
                }
            } while(true);
            return stringbuffer.toString();
        }

        private Hashtable a;
        private char _fldif;
        private boolean _flddo;

        public a()
        {
            this(false, ';');
        }

        public a(boolean flag, char c)
        {
            a = null;
            _fldif = ';';
            _flddo = false;
            a = new Hashtable();
            _fldif = c;
            _flddo = flag;
        }
    }


    public q(String s, String s1)
        throws IllegalArgumentException
    {
        _fldint = false;
        _fldif = -1;
        _fldnew = 59;
        _mthcase(s);
        if(s1 == null)
            throw new NullPointerException("header value is null");
        if(_fldint)
            _fldtry = new a(true, ',');
        else
            _fldtry = new a();
        try
        {
            _mthint(s1);
        }
        catch(SipException sipexception)
        {
            throw new IllegalArgumentException(sipexception.getMessage());
        }
    }

    private void _mthcase(String s)
        throws IllegalArgumentException
    {
        if(s == null)
            throw new NullPointerException("header name is null");
        s = s.trim();
        a(s);
        _flddo = s;
        if(_flddo.equals("WWW-Authenticate") || _flddo.equals("Proxy-Authenticate") || _flddo.equals("Proxy-Authorization") || _flddo.equals("Authorization"))
        {
            _fldint = true;
            _fldnew = 44;
            if(_fldtry != null)
                _fldtry.a(true, (char)_fldnew);
        } else
        {
            _fldint = false;
            _fldnew = 59;
            if(_fldtry != null)
                _fldtry.a(false, (char)_fldnew);
        }
    }

    private void a(String s)
        throws IllegalArgumentException
    {
        Vector vector = null;
        try
        {
            vector = e.a(s);
        }
        catch(Exception exception)
        {
            throw new IllegalArgumentException("Illegal name '" + s + "'; " + exception.getMessage());
        }
        if(vector.size() != 2)
            throw new IllegalArgumentException("Illegal name '" + s + "'");
        o o1 = (o)vector.elementAt(0);
        if(o1._fldcase != 1 && o1._fldcase != 5)
            throw new IllegalArgumentException("Illegal name '" + s + "'");
        else
            return;
    }

    private void _mthbyte(String s)
        throws IllegalArgumentException
    {
        Vector vector = null;
        try
        {
            vector = e.a(s);
        }
        catch(Exception exception)
        {
            throw new IllegalArgumentException("Illegal parameter value '" + s + "'; " + exception.getMessage());
        }
        if(vector.size() == 1)
            return;
        o o1 = (o)vector.elementAt(0);
        if(o1._fldcase != 1 && o1._fldcase != 5 && o1._fldcase != 3 && o1._fldcase != 7 && o1._fldcase != 6)
            throw new IllegalArgumentException("3Illegal parameter value '" + s + "'");
        else
            return;
    }

    public void _mthdo(String s)
        throws IllegalArgumentException
    {
        _mthcase(s);
    }

    public String _mthdo()
    {
        return _flddo;
    }

    public String _mthif()
    {
        return _fldchar;
    }

    public String _mthfor()
    {
        StringBuffer stringbuffer = new StringBuffer();
        if(_fldtry._mthfor())
        {
            stringbuffer.append(_fldchar);
            if(_fldint)
                stringbuffer.append(" ");
            stringbuffer.append(_fldtry.toString());
        } else
        {
            stringbuffer.append(_fldchar);
        }
        return stringbuffer.toString();
    }

    public void _mthelse(String s)
        throws IllegalArgumentException
    {
        String s1;
        s1 = null;
        if(s == null)
            throw new NullPointerException("Value null");
        s = s.trim();
        if(s.length() == 0)
            throw new IllegalArgumentException("Empty value");
        try
        {
            if(_fldint)
            {
                Vector vector = e.a(s);
                o o1 = (o)vector.elementAt(0);
                if(vector.size() > 2 || o1._fldcase != 1 && o1._fldcase != 5)
                {
                    throw new IllegalArgumentException("Illegal auth scheme: " + s);
                } else
                {
                    _fldchar = s;
                    return;
                }
            }
        }
        catch(SipException sipexception)
        {
            throw new IllegalArgumentException(sipexception.getMessage());
        }
        s1 = _mthfor(s);
        if(!s1.equals(s))
        {
            throw new IllegalArgumentException("Possibly trying to set value with parameters '" + s + "'");
        } else
        {
            _fldchar = s1;
            return;
        }
    }

    public String _mthnew(String s)
    {
        return _fldtry._mthif(s);
    }

    public String[] a()
    {
        return _fldtry.a();
    }

    public void a(String s, String s1)
        throws IllegalArgumentException
    {
        if(s == null)
            throw new NullPointerException("Parameter name null");
        s = s.trim();
        if(s.length() == 0)
            throw new IllegalArgumentException("Empty parameter name");
        a(s);
        try
        {
            if(s1 != null)
            {
                _mthbyte(s1);
                if(_fldtry.a(s))
                    _mthchar(s);
                if(s1.length() == 0)
                    _fldtry.a(s, "\"\"");
                else
                    _fldtry.a(s, s1);
            } else
            {
                if(_fldtry.a(s))
                    _mthchar(s);
                _fldtry.a(s, "");
            }
        }
        catch(Exception exception)
        {
            throw new IllegalArgumentException(exception.getMessage());
        }
    }

    public void _mthchar(String s)
    {
        if(s != null)
            _fldtry._mthdo(s);
    }

    public String toString()
    {
        StringBuffer stringbuffer = new StringBuffer(_flddo);
        stringbuffer.append(": ");
        if(_fldtry._mthfor())
        {
            stringbuffer.append(_fldchar);
            if(_fldint)
                stringbuffer.append(" ");
            stringbuffer.append(_fldtry.toString());
        } else
        {
            stringbuffer.append(_fldchar);
        }
        return stringbuffer.toString();
    }

    private String _mthfor(String s)
        throws SipException
    {
        int k;
        if(_fldint)
        {
            int i = s.indexOf(' ');
            if(i <= 0)
                throw new SipException("Invalid authentication header value", (byte)0);
            else
                return s.substring(0, i).trim();
        }
        int j = s.indexOf('<');
        k = s.indexOf('>');
        if(j == -1 && k == -1)
        {
            int l = s.indexOf(';');
            if(l > 0)
                return s.substring(0, l).trim();
            if(l == 0)
                throw new SipException("no header value", (byte)0);
            else
                return s.trim();
        }
        return s.substring(0, k + 1).trim();
        StringIndexOutOfBoundsException stringindexoutofboundsexception;
        stringindexoutofboundsexception;
        throw new SipException("Invalid header value", (byte)0);
    }

    private void _mthint(String s)
        throws SipException
    {
        s = s.trim();
        if(s.length() == 0)
            throw new SipException("Header value empty", (byte)0);
        _fldchar = _mthfor(s);
        if(_fldchar == s)
            return;
        int i = s.indexOf('>');
        int j = -1;
        if(_fldint)
        {
            _fldnew = 44;
            j = _fldchar.length();
        } else
        if(i == -1)
            j = s.indexOf(';');
        else
            j = s.indexOf(';', i);
        if(j == -1)
            return;
        Vector vector = e.a(s.substring(j));
        Object obj = null;
        StringBuffer stringbuffer = new StringBuffer();
        _fldif = 1;
        for(short word0 = 0; word0 < vector.size(); word0++)
        {
            o o1 = (o)vector.elementAt(word0);
            switch(_fldif)
            {
            case 3: // '\003'
            default:
                break;

            case 1: // '\001'
                switch(o1._fldcase)
                {
                case 1: // '\001'
                case 5: // '\005'
                    stringbuffer.append(o1._fldfor);
                    break;

                case 2: // '\002'
                    if(o1._fldint == '=')
                        _fldif = 2;
                    else
                    if(o1._fldint == '>')
                    {
                        if(stringbuffer.length() > 0)
                            a(stringbuffer, "");
                        _fldif = 3;
                    } else
                    if(o1._fldint == _fldnew)
                    {
                        if(stringbuffer.length() > 0)
                            a(stringbuffer, "");
                    } else
                    {
                        _mthtry("Illegal parameter name '" + o1._fldfor + "'");
                    }
                    break;

                case -1: 
                    if(stringbuffer.length() > 0)
                        a(stringbuffer, "");
                    _fldif = 3;
                    break;

                case 0: // '\0'
                case 3: // '\003'
                case 4: // '\004'
                default:
                    _mthtry("Illegal parameter name '" + o1._fldfor + "'");
                    break;
                }
                break;

            case 2: // '\002'
                switch(o1._fldcase)
                {
                case 1: // '\001'
                case 3: // '\003'
                case 5: // '\005'
                case 6: // '\006'
                case 7: // '\007'
                    if(stringbuffer.length() == 0)
                        _mthtry("Empty parameter name, for '" + o1._fldfor + "'");
                    if(o1._fldcase == 7)
                        o1._fldfor = new String("\"" + o1._fldfor + "\"");
                    a(stringbuffer, o1._fldfor);
                    _fldif = 1;
                    break;

                case 2: // '\002'
                case 4: // '\004'
                default:
                    _mthtry("Empty parameter value, for parameter '" + stringbuffer + "'");
                    break;
                }
                break;
            }
        }

    }

    private void a(StringBuffer stringbuffer, String s)
        throws SipException
    {
        _mthif(stringbuffer.toString());
        _fldtry.a(stringbuffer.toString(), s);
        stringbuffer.setLength(0);
    }

    private static void _mthtry(String s)
        throws SipException
    {
        throw new SipException(s, (byte)0);
    }

    private void _mthif(String s)
        throws SipException
    {
        if(_fldtry.a(s))
            throw new SipException("Duplicate header parameter '" + s + "'", (byte)0);
        else
            return;
    }

    private String _flddo;
    private String _fldchar;
    private a _fldtry;
    private boolean _fldint;
    private final short a = 0;
    private final short _fldfor = 1;
    private final short _fldcase = 2;
    private final short _fldbyte = 3;
    private short _fldif;
    private short _fldnew;
}
