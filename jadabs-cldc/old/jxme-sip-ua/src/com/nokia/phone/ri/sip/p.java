// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import javax.microedition.sip.*;

// Referenced classes of package com.nokia.phone.ri.sip:
//            u, t, s, a, 
//            Protocol, c, f

public class p
    implements SipClientConnectionListener
{
    protected class b
    {

        protected SipRefreshListener _fldfor;
        protected SipAddress _fldcase;
        protected Timer _fldif;
        protected u _fldint;
        protected SipHeader _fldtry[];
        protected f _fldnew;
        protected int _fldbyte;
        protected int _flddo;
        protected int a;

        protected b()
        {
            _fldcase = null;
            _fldif = null;
            _fldint = null;
            _fldbyte = 0;
            _flddo = 0;
            a = 0;
        }
    }

    protected class a extends TimerTask
    {

        public void run()
        {
            String s1 = a._fldint.a("Content-Type");
            byte abyte0[] = a._fldint.a();
            if(s1 != null && abyte0 != null && abyte0.length > 0)
            {
                OutputStream outputstream = p.this.a(a, _fldif, s1, abyte0.length, a._flddo);
                try
                {
                    outputstream.write(abyte0);
                    outputstream.close();
                }
                catch(IOException ioexception)
                {
                    ioexception.printStackTrace();
                }
            } else
            {
                p.this.a(a, _fldif, null, 0, a._flddo);
            }
        }

        b a;
        SipHeader _fldif[];

        a(b b1, SipHeader asipheader[])
        {
            a = b1;
            _fldif = asipheader;
        }
    }


    private p()
    {
        _fldfor = 2000;
        _fldif = new Vector(1);
        _flddo = new Hashtable(1);
    }

    public static p a()
    {
        if(a == null)
            a = new p();
        return a;
    }

    protected int a(SipRefreshListener siprefreshlistener, s s1, int i)
        throws SipException
    {
        b b1 = (b)_flddo.get(new Integer(i));
        if(b1 != null)
            return b1._fldbyte;
        b1 = new b();
        b1._fldfor = siprefreshlistener;
        b1._fldint = (u)s1._fldint;
        b1._fldcase = s1._fldif;
        b1._fldbyte = b1.hashCode();
        String s2 = s1.l.a("CSeq");
        b1.a = Integer.parseInt(s2.substring(0, s2.indexOf(' ')));
        try
        {
            b1._flddo = a(s1.l);
            Vector vector = s1.l._mthif("Contact");
            if(vector != null)
            {
                b1._fldtry = new SipHeader[vector.size()];
                for(int j = 0; j < vector.size(); j++)
                {
                    t t1 = (t)vector.elementAt(j);
                    b1._fldtry[j] = new SipHeader(t1._mthfor(), t1.a());
                    Protocol._mthif("RefreshHandler: adding " + b1._fldtry[j]);
                }

            }
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
        _flddo.put(new Integer(b1._fldbyte), b1);
        Protocol._mthif("RefreshHandler: initialized new refresh " + b1._fldbyte);
        return b1._fldbyte;
    }

    protected void a(int i, c c1, f f1)
    {
        Integer integer = new Integer(i);
        b b1 = (b)_flddo.get(integer);
        if(b1 != null)
        {
            int j = c1._mthnew();
            String s1 = c1._mthint();
            Protocol._mthif("RefreshHandler.notifyResponse(): " + i + " status code: " + j);
            if(j < 200)
                return;
            if(j >= 200 && j < 300)
            {
                try
                {
                    if(s1.equals("REGISTER"))
                    {
                        a(b1, c1);
                    } else
                    {
                        b1._flddo = a(((com.nokia.phone.ri.sip.a) (c1)));
                        long l = b1._flddo * 1000 - _fldfor;
                        Protocol._mthif("RefreshHandler.notifyResponse(): started " + s1 + " refresh " + i + " expires = " + b1._flddo + " sec, delay = " + l / 1000L + " sec");
                        a a1 = new a(b1, null);
                        b1._fldnew = f1;
                        synchronized(this)
                        {
                            if(b1._fldif != null)
                                b1._fldif.cancel();
                            b1._fldif = new Timer();
                            b1._fldif.schedule(a1, l);
                        }
                        a(b1, j, c1._mthfor());
                    }
                }
                catch(Exception exception)
                {
                    exception.printStackTrace();
                    a(b1, 1, exception.getMessage());
                    _flddo.remove(integer);
                }
            } else
            {
                a(b1, j, c1._mthfor());
                _flddo.remove(integer);
            }
        }
    }

    public void notifyResponse(SipClientConnection sipclientconnection)
    {
        try
        {
            sipclientconnection.receive(0L);
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }

    private int a(com.nokia.phone.ri.sip.a a1)
    {
        String s1;
        s1 = a1.a("Expires");
        if(s1 == null)
            break MISSING_BLOCK_LABEL_17;
        return Integer.parseInt(s1);
        Exception exception;
        exception;
        return -1;
    }

    private void a(b b1, c c1)
    {
        try
        {
            Vector vector = c1._mthif("Contact");
            int i = 0x7fffffff;
            if(vector != null)
            {
                for(int j = 0; j < b1._fldtry.length; j++)
                {
                    SipHeader sipheader = a(vector, b1._fldtry[j]);
                    if(sipheader == null)
                        continue;
                    int k = 0x7fffffff;
                    String s1 = sipheader.getParameter("expires");
                    if(s1 != null)
                        k = Integer.parseInt(s1);
                    if(k < i)
                        i = k;
                }

                long l = i * 1000 - _fldfor;
                Protocol._mthif("RefreshHandler: started REGISTER refresh " + b1._fldbyte + " expires = " + i + " sec, delay = " + l / 1000L + " sec");
                a a1 = new a(b1, b1._fldtry);
                synchronized(this)
                {
                    if(b1._fldif != null)
                        b1._fldif.cancel();
                    b1._fldif = new Timer();
                    b1._fldif.schedule(a1, l);
                }
                a(b1, c1._mthnew(), c1._mthfor());
                return;
            }
        }
        catch(Exception exception)
        {
            Protocol._mthif("RefreshHandler: problem in response removing refresh task");
            a(b1._fldbyte);
        }
    }

    private SipHeader a(Vector vector, SipHeader sipheader)
    {
        for(int i = 0; i < vector.size(); i++)
        {
            t t1 = (t)vector.elementAt(i);
            SipHeader sipheader1 = new SipHeader(t1._mthfor(), t1.a());
            SipAddress sipaddress = new SipAddress(sipheader1.getValue());
            SipAddress sipaddress1 = new SipAddress(sipheader.getValue());
            if(sipaddress.getURI().equals(sipaddress1.getURI()))
                return sipheader1;
        }

        return null;
    }

    private void a(b b1, int i, String s1)
    {
        try
        {
            b1._fldfor.refreshEvent(b1._fldbyte, i, s1);
        }
        catch(Exception exception)
        {
            Integer integer = new Integer(b1._fldbyte);
            exception.printStackTrace();
            Protocol._mthif("RefreshHelper: Problem with refresh listener callback, removing the refresh task!");
            if(b1._fldif != null)
                b1._fldif.cancel();
            _flddo.remove(integer);
        }
    }

    public void a(int i)
    {
        Integer integer = new Integer(i);
        b b1 = (b)_flddo.get(integer);
        if(b1 != null)
        {
            if(b1._fldif != null)
                b1._fldif.cancel();
            a(b1, b1._fldtry, null, 0, 0);
            _flddo.remove(integer);
            a(b1, 0, "refresh stopped");
        }
    }

    public OutputStream a(int i, String as[], String s1, int j, int k)
    {
        b b1;
        if(k == 0)
        {
            a(i);
            return null;
        }
        b1 = (b)_flddo.get(new Integer(i));
        if(b1 == null)
            break MISSING_BLOCK_LABEL_371;
        if(k > 0)
            b1._flddo = k;
        if(b1._fldif != null)
            b1._fldif.cancel();
        if(as != null)
        {
            Vector vector = null;
            if(b1._fldtry != null)
            {
                vector = new Vector(b1._fldtry.length + as.length);
                for(int l = 0; l < b1._fldtry.length; l++)
                {
                    b1._fldtry[l].setParameter("expires", "0");
                    vector.addElement(b1._fldtry[l]);
                }

            } else
            {
                vector = new Vector(as.length);
            }
            for(int i1 = 0; i1 < as.length; i1++)
            {
                SipHeader sipheader = new SipHeader("Contact", as[i1]);
                for(int k1 = 0; k1 < vector.size(); k1++)
                {
                    SipHeader sipheader2 = (SipHeader)vector.elementAt(k1);
                    SipAddress sipaddress = new SipAddress(sipheader.getValue());
                    SipAddress sipaddress1 = new SipAddress(sipheader2.getValue());
                    if(sipaddress.getURI().equals(sipaddress1.getURI()))
                        vector.removeElementAt(k1);
                }

                vector.addElement(sipheader);
            }

            b1._fldtry = new SipHeader[vector.size()];
            for(int j1 = 0; j1 < vector.size(); j1++)
                try
                {
                    SipHeader sipheader1 = (SipHeader)vector.elementAt(j1);
                    b1._fldtry[j1] = sipheader1;
                }
                catch(Exception exception1) { }

        }
        OutputStream outputstream = a(b1, b1._fldtry, s1, j, k);
        return outputstream;
        Exception exception;
        exception;
        exception.printStackTrace();
        a(i);
        return null;
        return null;
    }

    public synchronized OutputStream a(b b1, SipHeader asipheader[], String s1, int i, int j)
    {
        s s2;
        String s3;
        s2 = null;
        if(b1._fldnew != null)
            s2 = new s(b1._fldnew);
        else
            try
            {
                s2 = new s("user@host", 0, false);
                s2._fldif = b1._fldcase;
            }
            catch(IOException ioexception)
            {
                ioexception.printStackTrace();
            }
        s3 = b1._fldint._mthchar();
        OutputStream outputstream;
        if(s3.equals("REGISTER"))
        {
            s2.initRequest(s3, null);
            u u1 = (u)s2.l;
            s2.l = b1._fldint;
            s2._fldint = b1._fldint;
            b1.a++;
            s2.l._mthnew("Contact");
            if(j == 0)
            {
                s2.addHeader("Contact", "*");
            } else
            {
                for(int k = 0; k < asipheader.length; k++)
                    s2.addHeader("Contact", asipheader[k].getHeaderValue());

            }
            if(j != -1)
                s2.setHeader("Expires", Integer.toString(j));
            s2.setHeader("CSeq", b1.a + " " + s3);
            s2.l.a("Via", u1.a("Via"), true);
            Protocol._mthif("RefreshTask: Refreshing " + b1._fldbyte + " method: " + b1._fldint._mthchar());
        } else
        {
            s2.initRequest(s3, null);
            u u2 = (u)s2.l;
            s2.l = b1._fldint;
            s2._fldint = b1._fldint;
            s2.l.a("To", u2.a("To"), true);
            b1.a++;
            s2.l.a("CSeq", b1.a + " " + b1._fldint._mthchar(), true);
            s2.l.a("Via", u2.a("Via"), true);
            if(j >= 0)
                s2.setHeader("Expires", "" + j);
            Protocol._mthif("RefreshTask: Refreshing " + b1._fldbyte + " method: " + b1._fldint._mthchar());
        }
        s2.H = b1._fldbyte;
        s2.D = this;
        s2.setListener(this);
        if(s1 == null || i <= 0)
            break MISSING_BLOCK_LABEL_583;
        s2.setHeader("Content-Type", "" + s1);
        s2.setHeader("Content-Length", "" + i);
        outputstream = s2.openContentOutputStream();
        return outputstream;
        s2.send();
        return null;
        Exception exception;
        exception;
        exception.printStackTrace();
        return null;
    }

    private b _mthif(u u1)
    {
        for(Enumeration enumeration = _flddo.elements(); enumeration.hasMoreElements();)
        {
            b b1 = (b)enumeration.nextElement();
            String s1 = b1._fldint.a("Call-ID");
            String s2 = u1.a("Call-ID");
            if(s1.equals(s2))
            {
                String s3 = b1._fldint.a("Event");
                String s4 = u1.a("Event");
                if(s3.equals(s4))
                {
                    String s5 = b1._fldint.a("From");
                    SipHeader sipheader = new SipHeader("from", s5);
                    String s6 = sipheader.getParameter("tag");
                    String s7 = u1.a("To");
                    SipHeader sipheader1 = new SipHeader("to", s7);
                    String s8 = sipheader1.getParameter("tag");
                    if(s6.equals(s8))
                        return b1;
                }
            }
        }

        return null;
    }

    protected void a(u u1)
    {
        b b1;
        b1 = _mthif(u1);
        if(b1 == null)
            return;
        Integer integer;
        String s1;
        integer = new Integer(b1._fldbyte);
        s1 = u1.a("Subscription-State");
        if(s1 == null)
            return;
        String s3;
        SipHeader sipheader = new SipHeader("foo", s1);
        String s2 = sipheader.getValue();
        if(s2 != null && s2.equals("terminated"))
        {
            if(b1._fldif != null)
                b1._fldif.cancel();
            _flddo.remove(integer);
            a(b1, 0, sipheader.getParameter("reason"));
            break MISSING_BLOCK_LABEL_296;
        }
        if(!s2.equals("active"))
            break MISSING_BLOCK_LABEL_296;
        s3 = sipheader.getParameter("expires");
        if(s3 == null || s3.equals(""))
            return;
        try
        {
            int i = Integer.parseInt(s3);
            long l = i * 1000 - _fldfor;
            Protocol._mthif("RefreshHandler.gotNotify(): started refresh " + b1._fldbyte + " expires = " + i + " sec, delay = " + l / 1000L + " sec");
            a a1 = new a(b1, null);
            synchronized(this)
            {
                if(b1._fldif != null)
                    b1._fldif.cancel();
                b1._fldif = new Timer();
                b1._fldif.schedule(a1, l);
            }
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }

    protected int _fldfor;
    private static p a = null;
    private Vector _fldif;
    private Hashtable _flddo;

}
