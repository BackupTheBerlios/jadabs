// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import java.util.Vector;
import javax.microedition.sip.*;

// Referenced classes of package com.nokia.phone.ri.sip:
//            s, Protocol, a, i, 
//            c, u, m

public class f
    implements SipDialog
{

    protected f(Protocol protocol)
        throws SipException
    {
        _flddo = 0;
        _fldtry = 0;
        _fldelse = 0;
        _fldif = false;
        a = null;
        String s1 = protocol.getMethod();
        if(!s1.equals("INVITE") && !s1.equals("SUBSCRIBE") && !s1.equals("REFER"))
            throw new SipException("No new dialog for method '" + s1 + "'", (byte)0);
        int j = protocol.getStatusCode();
        _fldnull = protocol._fldint.a("Call-ID");
        if(j >= 101 && j < 200)
            _flddo = 1;
        else
        if(j >= 200 && j < 300)
            _flddo = 2;
        else
            _flddo = 0;
        a = new Vector(3);
    }

    protected f(s s1)
        throws SipException
    {
        this(((Protocol) (s1)));
        Protocol._mthif("-------- Creation of a UAC Dialog ----------");
        String s2 = s1._fldint.a("CSeq");
        try
        {
            _fldtry = Integer.parseInt(s2.substring(0, s2.indexOf(' ')));
        }
        catch(Exception exception) { }
        _fldgoto = s1._fldint.a("Contact");
        SipHeader sipheader = new SipHeader("From", s1._fldint.a("From"));
        _fldcase = sipheader.getParameter("tag");
        _fldint = (new SipAddress(sipheader.getValue())).getURI();
        SipHeader sipheader1 = new SipHeader("To", s1.getHeader("To"));
        _fldvoid = sipheader1.getParameter("tag");
        _fldchar = (new SipAddress(sipheader1.getValue())).getURI();
        String s3 = s1.getHeader("Contact");
        if(s3 != null)
        {
            _fldlong = (new SipAddress(s3)).getURI();
        } else
        {
            Protocol._mthif("WARNING Dialog: using To instead of Contact as remote target");
            _fldlong = _fldchar;
        }
        StringBuffer stringbuffer = new StringBuffer(_fldnull);
        stringbuffer.append(_fldcase);
        stringbuffer.append(_fldvoid);
        _fldbyte = stringbuffer.toString();
        String as[] = s1.getHeaders("Record-Route");
        if(as != null)
        {
            _fldfor = new String[as.length];
            for(short word0 = 0; word0 < as.length; word0++)
                _fldfor[word0] = as[as.length - 1 - word0];

        }
    }

    protected f(i j)
        throws SipException
    {
        this(((Protocol) (j)));
        Protocol._mthif("-------- Creation of a UAS Dialog ----------");
        String s1 = j._fldint.a("CSeq");
        try
        {
            _fldelse = Integer.parseInt(s1.substring(0, s1.indexOf(' ')));
        }
        catch(Exception exception) { }
        _fldgoto = j.l.a("Contact");
        SipHeader sipheader = new SipHeader("To", j.getHeader("To"));
        _fldcase = sipheader.getParameter("tag");
        _fldint = (new SipAddress(sipheader.getValue())).getURI();
        SipHeader sipheader1 = new SipHeader("From", j._fldint.a("From"));
        _fldvoid = sipheader1.getParameter("tag");
        _fldchar = (new SipAddress(sipheader1.getValue())).getURI();
        String s2 = j._fldint.a("Contact");
        if(s2 != null)
        {
            _fldlong = (new SipAddress(s2)).getURI();
        } else
        {
            Protocol._mthif("WARNING Dialog: using From instead of Contact as remote target");
            _fldlong = _fldchar;
        }
        StringBuffer stringbuffer = new StringBuffer(_fldnull);
        stringbuffer.append(_fldcase);
        stringbuffer.append(_fldvoid);
        _fldbyte = stringbuffer.toString();
        _fldfor = j.getHeaders("Record-Route");
    }

    protected f(i j, s s1)
    {
        _flddo = 0;
        _fldtry = 0;
        _fldelse = 0;
        _fldif = false;
        a = null;
        Protocol._mthif("-------- Creation of a UAS Dialog (SUBSCRIBE/NOTIFY) ----------");
        _fldnull = s1._fldint.a("Call-ID");
        _flddo = 2;
        String s2 = s1._fldint.a("CSeq");
        try
        {
            _fldelse = Integer.parseInt(s2.substring(0, s2.indexOf(' ')));
        }
        catch(Exception exception) { }
        _fldgoto = s1._fldint.a("Contact");
        SipHeader sipheader = new SipHeader("To", j.getHeader("To"));
        _fldcase = sipheader.getParameter("tag");
        _fldint = (new SipAddress(sipheader.getValue())).getURI();
        SipHeader sipheader1 = new SipHeader("From", j._fldint.a("From"));
        _fldvoid = sipheader1.getParameter("tag");
        _fldchar = (new SipAddress(sipheader1.getValue())).getURI();
        String s3 = j._fldint.a("Contact");
        if(s3 != null)
        {
            _fldlong = (new SipAddress(s3)).getURI();
        } else
        {
            Protocol._mthif("WARNING Dialog: using From instead of Contact as remote target");
            _fldlong = _fldchar;
        }
        StringBuffer stringbuffer = new StringBuffer(_fldnull);
        stringbuffer.append(_fldcase);
        stringbuffer.append(_fldvoid);
        _fldbyte = stringbuffer.toString();
        String as[] = j.getHeaders("Record-Route");
        if(as != null)
        {
            _fldfor = new String[as.length];
            for(short word0 = 0; word0 < as.length; word0++)
                _fldfor[word0] = as[as.length - 1 - word0];

        }
        a = new Vector(3);
    }

    protected static String a(a a1)
    {
        SipHeader sipheader = new SipHeader("To", a1.a("To"));
        String s1 = sipheader.getParameter("tag");
        SipHeader sipheader1 = new SipHeader("From", a1.a("From"));
        String s2 = sipheader1.getParameter("tag");
        String s3 = a1.a("Call-ID");
        StringBuffer stringbuffer = new StringBuffer(s3);
        stringbuffer.append(s1);
        stringbuffer.append(s2);
        return stringbuffer.toString();
    }

    protected void a(m m)
    {
        _fldnew = m;
    }

    protected m _mthif()
    {
        return _fldnew;
    }

    public SipClientConnection getNewClientConnection(String s1)
        throws SipException, IllegalArgumentException
    {
        if(_flddo != 2)
            throw new SipException("Can't get new ClientConnection in this state", (byte)5);
        else
            return new s(this, s1);
    }

    public boolean isSameDialog(SipConnection sipconnection)
    {
        Protocol protocol = (Protocol)sipconnection;
        if(protocol.q == null || _fldbyte == null)
            return false;
        return _fldbyte.equals(protocol.q.getDialogID());
    }

    public byte getState()
    {
        return _flddo;
    }

    protected void a(byte byte0)
    {
        _flddo = byte0;
    }

    public String getDialogID()
    {
        return _fldbyte;
    }

    protected String _mthdo()
    {
        return _fldcase;
    }

    protected String _mthfor()
    {
        return _fldvoid;
    }

    protected String a()
    {
        return _fldnull;
    }

    protected void a(int j, c c1, a a1)
    {
        Protocol._mthif("Dialog: updateSubscriptions() on SUBSCRIBE-response " + j);
        if(j >= 200)
            if(j >= 200 && j < 299)
            {
                SipHeader sipheader = new SipHeader("Expires", c1.a("Expires"));
                String s2 = sipheader.getValue();
                int k = Integer.parseInt(s2);
                String s3 = a1.a("Event");
                if(k > 0)
                    a(s3);
            } else
            if(j >= 300)
            {
                String s1 = a1.a("Event");
                _mthdo(s1);
            }
    }

    protected void a(u u1)
    {
        Protocol._mthif("Dialog: updateSubscriptions() on NOTIFY request");
        SipHeader sipheader = new SipHeader("Subscription-State", u1.a("Subscription-State"));
        String s1 = u1.a("Event");
        if(sipheader.getValue().equals("terminated"))
        {
            _mthdo(s1);
            if(a.isEmpty())
                _flddo = 0;
        } else
        if(sipheader.getValue().equals("active"))
            a(s1);
        else
        if(!sipheader.getValue().equals("pending"));
    }

    private String _mthif(String s1)
    {
        SipHeader sipheader = new SipHeader("Event", s1);
        String s2 = sipheader.getValue();
        String s3 = sipheader.getParameter("id");
        String s4 = null;
        if(s3 == null)
            s4 = s2;
        else
            s4 = s2 + ";" + s3;
        return s4;
    }

    private void a(String s1)
    {
        String s2 = _mthif(s1);
        if(!a.contains(s2))
            a.addElement(s2);
    }

    private void _mthdo(String s1)
    {
        String s2 = _mthif(s1);
        if(a.contains(s2))
            a.removeElement(s2);
    }

    private String _fldbyte;
    private String _fldcase;
    protected String _fldint;
    protected String _fldlong;
    protected String _fldchar;
    protected String _fldvoid;
    private String _fldnull;
    protected String _fldfor[];
    private byte _flddo;
    protected int _fldtry;
    protected int _fldelse;
    private boolean _fldif;
    protected String _fldgoto;
    private Vector a;
    private m _fldnew;
}
