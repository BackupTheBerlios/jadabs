// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.sip.SipException;

// Referenced classes of package com.nokia.phone.ri.sip:
//            t, j

public class a
{

    public a()
    {
        _fldbyte = false;
        a = false;
        _fldif = new Vector();
        _fldtry = new Vector();
    }

    public a(Vector vector, Vector vector1, byte abyte0[])
    {
        _fldbyte = false;
        a = false;
        _fldtry = vector;
        if(_mthdo((String)_fldtry.elementAt(0)))
        {
            _fldbyte = true;
            a = false;
        } else
        {
            _fldbyte = false;
            a = true;
        }
        if(vector1 != null)
            _fldif = vector1;
        else
            _fldif = new Vector();
        _fldint = abyte0;
    }

    public void a(String s, String s1)
    {
        s = s.trim();
        t t1 = new t(s, s1);
        t t2 = a(s, true);
        if(t2 != null)
        {
            _fldif.insertElementAt(t1, _fldif.indexOf(t2));
            return;
        } else
        {
            _fldif.addElement(t1);
            return;
        }
    }

    public String a(String s)
    {
        t t1 = a(s, true);
        if(t1 != null)
            return t1.a();
        else
            return null;
    }

    public void a(String s, String s1, boolean flag)
        throws SipException
    {
        t t1 = null;
        s = s.trim();
        t1 = a(s, flag);
        if(t1 != null)
            t1.a(s1);
        else
            a(s, s1);
    }

    private t a(String s, boolean flag)
    {
        s = s.trim();
        String s1 = "";
        if(s.length() == 1)
            s1 = j._mthif(s);
        else
            s1 = j.a(s);
        if(flag)
        {
            for(short word0 = 0; word0 < _fldif.size(); word0++)
            {
                t t1 = (t)_fldif.elementAt(word0);
                if(t1._mthfor().equals(s) || t1._mthfor().equals(s1))
                    return t1;
            }

        } else
        {
            short word1 = (short)(_fldif.size() - 1);
            for(short word2 = word1; word2 >= 0; word2--)
            {
                t t2 = (t)_fldif.elementAt(word2);
                if(t2._mthfor().equals(s) || t2._mthfor().equals(s1))
                    return t2;
            }

        }
        return null;
    }

    private Vector _mthint(String s)
    {
        Vector vector = null;
        s = s.trim();
        String s1 = "";
        if(s.length() == 1)
            s1 = j._mthif(s);
        else
            s1 = j.a(s);
        for(int i = 0; i < _fldif.size(); i++)
        {
            t t1 = (t)_fldif.elementAt(i);
            if(!t1._mthfor().equals(s) && !t1._mthfor().equals(s1))
                continue;
            if(vector == null)
                vector = new Vector();
            vector.addElement(t1);
        }

        return vector;
    }

    public Vector _mthif(String s)
    {
        return _mthint(s);
    }

    public String _mthfor(String s)
    {
        t t1 = a(s, true);
        if(t1 != null)
        {
            _fldif.removeElement(t1);
            return t1.a();
        } else
        {
            return null;
        }
    }

    public void _mthnew(String s)
    {
        Vector vector = _mthint(s);
        if(vector != null)
        {
            t t1;
            for(Enumeration enumeration = vector.elements(); enumeration.hasMoreElements(); _fldif.removeElement(t1))
                t1 = (t)enumeration.nextElement();

        }
    }

    public void a(byte abyte0[])
        throws SipException
    {
        _fldint = abyte0;
    }

    public byte[] a()
    {
        return _fldint;
    }

    public boolean _mthdo()
    {
        return _fldbyte;
    }

    public boolean _mthif()
    {
        return a;
    }

    public String toString()
    {
        StringBuffer stringbuffer = new StringBuffer();
        short word0 = 0;
        word0 = (short)_fldtry.size();
        for(short word1 = 0; word1 < word0; word1++)
        {
            stringbuffer.append(_fldtry.elementAt(word1));
            if(word1 < word0 - 1)
                stringbuffer.append(" ");
        }

        stringbuffer.append("\r\n");
        word0 = (short)_fldif.size();
        for(short word2 = 0; word2 < word0; word2++)
        {
            stringbuffer.append(((t)_fldif.elementAt(word2))._mthif());
            stringbuffer.append("\r\n");
        }

        stringbuffer.append("\r\n");
        if(_fldint != null)
            stringbuffer.append(new String(_fldint));
        return stringbuffer.toString();
    }

    public static boolean _mthdo(String s)
    {
        return !s.startsWith("SIP/2.0");
    }

    public boolean a(a a1)
    {
        String s = toString().trim();
        String s1 = a1.toString().trim();
        return s.compareTo(s1) == 0;
    }

    protected Vector _fldtry;
    protected Vector _fldif;
    protected byte _fldint[];
    protected boolean _fldbyte;
    protected boolean a;
    public boolean _fldcase;
    public int _fldnew;
    public String _fldfor;
    public int _flddo;
}
