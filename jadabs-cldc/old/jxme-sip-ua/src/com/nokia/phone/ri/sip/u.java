// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import java.util.Vector;
import javax.microedition.sip.SipAddress;

// Referenced classes of package com.nokia.phone.ri.sip:
//            a, c, t

public class u extends a
{

    public u(Vector vector, Vector vector1, byte abyte0[])
    {
        super(vector, vector1, abyte0);
        _fldgoto = false;
        _fldlong = null;
        _fldnull = null;
        _fldnull = ((String)vector.elementAt(0)).trim();
        _fldlong = new SipAddress((String)vector.elementAt(1));
    }

    public u(String s, SipAddress sipaddress, Vector vector)
    {
        _fldgoto = false;
        _fldlong = null;
        _fldnull = null;
        a(s, sipaddress, vector);
    }

    public String _mthchar()
    {
        return _fldnull;
    }

    public void a(SipAddress sipaddress)
    {
        _fldlong = sipaddress;
        a(_fldnull, sipaddress, _fldif);
    }

    public SipAddress _mthbyte()
    {
        return _fldlong;
    }

    public void a(boolean flag)
    {
        _fldgoto = flag;
    }

    public boolean _mthtry()
    {
        return _fldgoto;
    }

    public c _mthcase()
    {
        c c1 = new c();
        c1.a("From", a("From"));
        c1.a("To", a("To"));
        c1.a("Call-ID", a("Call-ID"));
        c1.a("CSeq", a("CSeq"));
        Vector vector = _mthif("Via");
        if(vector.size() > 0)
        {
            for(int i = vector.size() - 1; i >= 0; i--)
                c1.a("Via", ((t)vector.elementAt(i)).a());

        }
        return c1;
    }

    private void a(String s, SipAddress sipaddress, Vector vector)
    {
        Vector vector1 = new Vector(3);
        _fldbyte = true;
        a = false;
        vector1.addElement(s);
        vector1.addElement(sipaddress.toString());
        vector1.addElement("SIP/2.0");
        _fldlong = sipaddress;
        _fldnull = s;
        _fldtry = vector1;
        if(vector != null)
            _fldif = vector;
    }

    boolean _fldgoto;
    SipAddress _fldlong;
    String _fldnull;
}
