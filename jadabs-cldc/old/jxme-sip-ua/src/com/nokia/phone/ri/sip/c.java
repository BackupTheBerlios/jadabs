// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import java.util.Vector;

// Referenced classes of package com.nokia.phone.ri.sip:
//            a, j

public class c extends a
{

    public c()
    {
        _fldelse = null;
        _fldtry.addElement("SIP/2.0");
        _fldbyte = false;
        a = true;
    }

    public c(Vector vector, Vector vector1, byte abyte0[])
    {
        super(vector, vector1, abyte0);
        _fldelse = null;
        _fldchar = Integer.parseInt((String)vector.elementAt(1));
    }

    public void a(int i)
    {
        _fldchar = i;
        _mthif(i);
    }

    private void _mthif(int i)
    {
        _fldtry = new Vector(3);
        _fldtry.addElement("SIP/2.0");
        _fldtry.addElement(Integer.toString(i));
        _fldelse = j.a(i);
        _fldtry.addElement(_fldelse);
    }

    public int _mthnew()
    {
        return _fldchar;
    }

    public void _mthtry(String s)
    {
        _fldelse = s;
        _fldtry.setElementAt(s, 2);
    }

    public String _mthfor()
    {
        return _fldelse;
    }

    public String _mthint()
    {
        String s;
        s = a("CSeq");
        if(s == null)
            break MISSING_BLOCK_LABEL_30;
        String s1 = s.substring(s.indexOf(' '));
        return s1.trim();
        Exception exception;
        exception;
        return null;
        return null;
    }

    int _fldchar;
    String _fldelse;
}
