// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import java.io.IOException;
import java.util.EmptyStackException;
import java.util.Vector;
import javax.microedition.sip.*;

// Referenced classes of package com.nokia.phone.ri.sip:
//            Protocol, o, t, u, 
//            m, a, c, x, 
//            e, f, d, p

public class s extends Protocol
    implements SipClientConnection
{

    protected s(String s1, int i, boolean flag)
        throws IOException, IllegalArgumentException
    {
        z = null;
        C = null;
        A = null;
        H = 0;
        D = null;
        String s2 = "sip:" + s1;
        SipAddress sipaddress = null;
        try
        {
            sipaddress = new SipAddress(s2);
        }
        catch(Exception exception)
        {
            throw new IllegalArgumentException(exception.getMessage());
        }
        String s3 = sipaddress.getParameter("transport");
        if(s3 != null)
        {
            s3 = s3.toLowerCase();
            String s4 = new String("UDP");
            s4 = s4.toLowerCase();
            if(!s3.equals(s4))
                throw new SipException("Transport " + s3 + " not supported", (byte)1);
        }
        E = new Vector();
        if(!_fldelse._mthbyte())
            _fldelse.a(0, null);
        z = _fldelse._mthnew();
        G = _fldelse._mthtry();
        B = _fldelse._mthfor();
        F = _fldelse._mthcase();
        if(sipaddress.getHost().equals("localhost"))
            sipaddress.setHost(z);
        _fldif = new SipAddress(sipaddress.toString());
        C = _fldelse._mthif();
        w = 10;
        this.i = 0;
    }

    private s()
    {
        z = null;
        C = null;
        A = null;
        H = 0;
        D = null;
        E = new Vector();
        C = _fldelse._mthif();
        w = 10;
        i = 0;
    }

    protected s(f f1, String s1)
        throws SipException, IllegalArgumentException
    {
        this();
        q = f1;
        initRequest(s1, null);
        Protocol._mthif("ClientConnection created from Dialog");
    }

    protected s(f f1)
    {
        this();
        q = f1;
    }

    private void _mthdo(String s1)
        throws IllegalArgumentException
    {
        Vector vector = null;
        try
        {
            vector = e.a(s1);
        }
        catch(Exception exception)
        {
            throw new IllegalArgumentException("Illegal method '" + s1 + "'; " + exception.getMessage());
        }
        if(vector.size() != 2)
            throw new IllegalArgumentException("Illegal method '" + s1 + "'");
        o o1 = (o)vector.elementAt(0);
        if(o1._fldcase != 1 && o1._fldcase != 5)
            throw new IllegalArgumentException("Illegal method '" + s1 + "'");
        else
            return;
    }

    protected u a(String s1, m m1)
    {
        SipHeader sipheader = null;
        SipAddress sipaddress = null;
        SipAddress sipaddress1 = null;
        String s2 = null;
        String s3 = null;
        String s4 = null;
        String as[] = null;
        boolean flag = false;
        if(s1.equals("INVITE") || s1.equals("REGISTER") || s1.equals("SUBSCRIBE") || s1.equals("REFER"))
            if(m1 != null)
                s2 = m1._mthif().toString();
            else
            if(q != null)
                s2 = new String(q._fldgoto);
        String s5;
        if(s1.equals("ACK"))
            s5 = C + ";branch=" + "z9hG4bK" + e.o;
        else
            s5 = C + ";branch=" + "z9hG4bK" + e.l;
        if(q != null)
            try
            {
                SipAddress sipaddress2 = new SipAddress(q._fldint);
                sipheader = new SipHeader("From", sipaddress2.toString());
                sipheader.setParameter("tag", q._mthdo());
                sipaddress = new SipAddress(q._fldlong);
                sipaddress1 = new SipAddress(q._fldchar);
                if(q._mthfor() != null)
                    sipaddress1.setParameter("tag", q._mthfor());
                if(q._fldfor != null)
                {
                    SipAddress sipaddress4 = new SipAddress(q._fldfor[0]);
                    if(sipaddress4.getParameter("lr") != null)
                    {
                        as = q._fldfor;
                        flag = true;
                    } else
                    {
                        sipaddress = new SipAddress(sipaddress4.getURI());
                        as = new String[q._fldfor.length];
                        if(q._fldfor.length > 1)
                        {
                            for(int i = 1; i < q._fldfor.length; i++)
                                as[i - 1] = q._fldfor[i];

                        }
                        as[as.length - 1] = q._fldlong;
                        flag = false;
                    }
                }
                s3 = q.a();
                q._fldtry++;
                s4 = q._fldtry + " " + s1;
            }
            catch(Exception exception)
            {
                throw new IllegalArgumentException(exception.getMessage());
            }
        else
            try
            {
                SipAddress sipaddress3 = new SipAddress(F + " " + B);
                sipheader = new SipHeader("From", sipaddress3.toString());
                sipheader.setParameter("tag", "" + Math.abs(C.hashCode() + B.hashCode()));
                s3 = e.l + "@" + z;
                s4 = "1 " + s1;
                sipaddress = _fldif;
                sipaddress1 = new SipAddress(sipaddress.getScheme() + ":" + sipaddress.getUser() + "@" + sipaddress.getHost());
                SipAddress sipaddress5 = _fldelse._mthint();
                if(sipaddress5 != null)
                {
                    as = new String[1];
                    if(sipaddress5.getParameter("lr") != null)
                    {
                        as[0] = new String(sipaddress5.toString());
                        flag = true;
                    } else
                    {
                        as[0] = sipaddress.toString();
                        sipaddress = sipaddress5;
                    }
                }
            }
            catch(Exception exception1)
            {
                throw new IllegalArgumentException(exception1.getMessage());
            }
        Vector vector = new Vector(4);
        u u1;
        try
        {
            vector.addElement(new t("From", sipheader.getHeaderValue()));
            vector.addElement(new t("To", sipaddress1.toString()));
            if(s2 != null)
                vector.addElement(new t("Contact", s2));
            u1 = new u(s1, sipaddress, vector);
            u1.a("Call-ID", s3, true);
            u1.a("CSeq", s4, true);
            u1.a("Max-Forwards", "70", true);
            u1.a("Via", s5, true);
            if(as != null)
            {
                for(int j = as.length - 1; j >= 0; j--)
                    u1.a("Route", as[j]);

                u1.a(flag);
            }
        }
        catch(Exception exception2)
        {
            throw new IllegalArgumentException(exception2.getMessage());
        }
        return u1;
    }

    public void initRequest(String s1, SipConnectionNotifier sipconnectionnotifier)
        throws SipException
    {
        if(i != 0)
            throw new SipException("Can not initialize request in this state", (byte)5);
        if(s1 == null)
            throw new NullPointerException("Method is null");
        if(s1.length() == 0)
            throw new IllegalArgumentException("Method is empty");
        _mthdo(s1);
        e = _fldelse.a(this, s1, null);
        u u1 = null;
        if(sipconnectionnotifier != null && (sipconnectionnotifier instanceof m))
            u1 = a(s1, (m)sipconnectionnotifier);
        else
            u1 = a(s1, ((m) (null)));
        _fldint = u1;
        l = u1;
        i = 1;
    }

    public void setRequestURI(String s1)
        throws SipException, IllegalArgumentException
    {
        if(i != 1)
            throw new SipException("Request-URI can not be set in this state", (byte)5);
        SipAddress sipaddress = new SipAddress(s1);
        if(sipaddress.getDisplayName() != null)
        {
            throw new IllegalArgumentException("Display name not allowed in Request-URI");
        } else
        {
            ((u)l).a(sipaddress);
            return;
        }
    }

    public void initAck()
        throws SipException
    {
        if(this.i != 4)
            throw new SipException("ACK can not be initialized in this state", (byte)5);
        if(_fldint == null)
            throw new SipException("Original request missing", (byte)6);
        String s1 = ((u)_fldint)._mthchar();
        if(!s1.equals("INVITE"))
        {
            throw new SipException("ACK can not be applied to non-INVITE request", (byte)6);
        } else
        {
            u u1 = a("ACK", ((m) (null)));
            String s2 = _fldint.a("CSeq");
            int i = Integer.parseInt(s2.substring(0, s2.indexOf(' ')));
            u1.a("CSeq", i + " " + "ACK", true);
            l = u1;
            this.i = 1;
            return;
        }
    }

    public SipClientConnection initCancel()
        throws SipException
    {
        if(this.i != 3)
            throw new SipException("CANCEL can not be initialized in this state", (byte)5);
        if(_fldint == null)
            throw new SipException("Original request missing", (byte)6);
        String s1 = ((u)_fldint)._mthchar();
        if(!s1.equals("INVITE"))
            throw new SipException("CANCEL can not be applied to non-INVITE request", (byte)6);
        s s2 = new s();
        s2._fldint = _fldint;
        s2._fldif = _fldif;
        SipHeader sipheader = new SipHeader("Via", _fldint.a("Via"));
        s2.e = _fldelse.a(s2, "CANCEL", sipheader.getParameter("branch"));
        Vector vector = new Vector(1);
        try
        {
            vector.addElement(new t("From", _fldint.a("From")));
            vector.addElement(new t("To", _fldint.a("To")));
            vector.addElement(new t("Call-ID", _fldint.a("Call-ID")));
            vector.addElement(new t("Max-Forwards", "70"));
            vector.addElement(new t("Via", _fldint.a("Via")));
            String s3 = _fldint.a("Route");
            if(s3 != null)
                vector.addElement(new t("Route", s3));
            s2.l = new u("CANCEL", _fldif, vector);
        }
        catch(Exception exception)
        {
            throw new SipException(exception.getMessage(), (byte)6);
        }
        String s4 = _fldint.a("CSeq");
        int i = Integer.parseInt(s4.substring(0, s4.indexOf(' ')));
        s2.l.a("CSeq", i + " " + "CANCEL", true);
        this.i = 3;
        s2.i = 1;
        return s2;
    }

    public boolean receive(long l)
        throws SipException, IOException
    {
        s s1 = this;
        JVM INSTR monitorenter ;
        if(this.i != 3)
            throw new SipException("Can not receive message in this state", (byte)5);
        if(d)
        {
            d = false;
            throw new IOException("Client transaction timeout");
        }
        if(l <= 0L) goto _L2; else goto _L1
_L1:
        if(!E.isEmpty()) goto _L4; else goto _L3
_L3:
        l = a(l);
        if(l > 0L || !E.isEmpty()) goto _L1; else goto _L5
_L5:
        return false;
_L2:
        if(!E.isEmpty()) goto _L4; else goto _L6
_L6:
        false;
        s1;
        JVM INSTR monitorexit ;
        return;
_L4:
        o = (a)E.firstElement();
        E.removeElement(o);
        this.l = o;
        int i = getStatusCode();
        a(i, (c)this.l);
        if(i < 200)
            this.i = 3;
        else
        if(i >= 200 && i < 300)
            this.i = 4;
        else
        if(i >= 300)
            this.i = 4;
        true;
        s1;
        JVM INSTR monitorexit ;
        return;
        EmptyStackException emptystackexception;
        emptystackexception;
        emptystackexception.printStackTrace();
        false;
        s1;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        throw exception;
    }

    private void a(int i, c c1)
    {
        if(q == null)
            q = _fldelse.a(this);
        else
        if(i >= 200 && i < 300)
        {
            if(getMethod().equals("BYE"))
                q.a((byte)0);
            else
            if(getMethod().equals("NOTIFY"))
            {
                SipHeader sipheader = new SipHeader("Subscription-State", _fldint.a("Subscription-State"));
                if(sipheader.getValue().equals("terminated"))
                    q.a((byte)0);
            } else
            {
                q.a((byte)2);
            }
        } else
        if(i >= 300)
            q.a((byte)0);
        if(getMethod().equals("SUBSCRIBE"))
            try
            {
                q.a(i, c1, _fldint);
            }
            catch(Exception exception)
            {
                Protocol._mthif("ClientConnection: Error updating the dialog info; " + exception.getMessage());
                Protocol._mthif("ClientConnection: Terminate the dialog state");
                q.a((byte)0);
            }
        if(H > 0 && D != null)
            D.a(H, c1, q);
    }

    private int a(long l)
        throws IOException
    {
        s s1 = this;
        JVM INSTR monitorenter ;
        Protocol._mthif("ClientConnection: " + e._fldgoto + " waiting for message...");
        long l1 = System.currentTimeMillis();
        try
        {
            wait(l);
            l = a(l1, l);
            break MISSING_BLOCK_LABEL_78;
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
            l = a(l1, l);
        }
        return (int)l;
        if(d)
        {
            d = false;
            throw new IOException("Client transaction timeout");
        }
        Protocol._mthif("ClientConnection: " + e._fldgoto + " running");
        (int)l;
        s1;
        JVM INSTR monitorexit ;
        return;
        Exception exception1;
        exception1;
        throw exception1;
    }

    private long a(long l, long l1)
    {
        long l2 = l1 - (System.currentTimeMillis() - l);
        if(l2 < 0L)
            return 0L;
        else
            return l2;
    }

    protected void a(c c1)
    {
        synchronized(this)
        {
            E.addElement(c1);
            notify();
        }
        try
        {
            if(A != null)
                A.notifyResponse(this);
        }
        catch(Exception exception) { }
    }

    protected void _mthfor()
    {
        synchronized(this)
        {
            d = true;
            notify();
        }
        try
        {
            if(A != null)
                A.notifyResponse(this);
        }
        catch(Exception exception) { }
    }

    public void setListener(SipClientConnectionListener sipclientconnectionlistener)
        throws IOException
    {
        _mthif();
        A = sipclientconnectionlistener;
    }

    public int enableRefresh(SipRefreshListener siprefreshlistener)
        throws SipException
    {
        if(siprefreshlistener == null)
            return 0;
        if(i != 1)
        {
            throw new SipException("Can not set refresh in this state", (byte)5);
        } else
        {
            D = p.a();
            H = D.a(siprefreshlistener, this, H);
            return H;
        }
    }

    public void setCredentials(String s1, String s2, String s3)
        throws SipException
    {
        if(i != 1)
            throw new SipException("Can not set credentials in this state", (byte)5);
        if(s1 == null)
            throw new NullPointerException("User name is null");
        if(s2 == null)
            throw new NullPointerException("Password is null");
        if(s3 == null)
        {
            throw new NullPointerException("Realm is null");
        } else
        {
            Protocol._mthif("setCredentials: not implemented yet!");
            return;
        }
    }

    public void close()
    {
        if(e != null)
            _fldelse.a(e);
        e = null;
        super.close();
    }

    private String G;
    private String B;
    private String F;
    private Vector E;
    private String z;
    private String C;
    private SipClientConnectionListener A;
    protected int H;
    protected p D;
}
