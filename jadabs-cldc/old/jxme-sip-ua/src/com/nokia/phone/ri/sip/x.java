// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import com.sun.midp.main.Configuration;
import java.io.IOException;
import java.util.*;
import javax.microedition.sip.SipAddress;
import javax.microedition.sip.SipHeader;

// Referenced classes of package com.nokia.phone.ri.sip:
//            w, d, n, f, 
//            c, u, k, Protocol, 
//            m, i, s, a, 
//            b, r

public class x
{

    private x()
    {
        _fldint = "127.0.0.1";
        _fldfor = false;
        _fldnull = false;
        _fldlong = null;
        _fldcase = null;
        e = 0;
        d = new Hashtable();
        d.put("name", "client");
        _fldnew = new Hashtable();
        _fldnew.put("name", "server");
        _fldtry = new Hashtable();
        _fldbyte = new Hashtable();
        _flddo = Configuration.getProperty("com.nokia.phone.ri.sip.user_address");
        c = Configuration.getProperty("com.nokia.phone.ri.sip.display_name");
        if(_flddo == null || _flddo.trim().length() == 0)
            throw new IllegalArgumentException("SIP user address not configured");
        try
        {
            String s1 = Configuration.getProperty("com.nokia.phone.ri.sip.outbound_proxy");
            if(s1 != null)
                _fldcase = new SipAddress(s1);
            Protocol._mthif("Configuration Outbound Proxy: " + _fldcase.toString());
        }
        catch(Exception exception)
        {
            _fldcase = null;
        }
        try
        {
            String s2 = Configuration.getProperty("com.nokia.phone.ri.sip.local_port");
            e = Integer.parseInt(s2);
            Protocol._mthif("Configuration Local Port: " + e);
        }
        catch(Exception exception1)
        {
            e = 0;
        }
        Protocol._mthif("Configuration Display Name: " + c);
        Protocol._mthif("Configuration User Address: " + _flddo);
        SipAddress sipaddress = null;
        try
        {
            sipaddress = new SipAddress(_flddo);
        }
        catch(Exception exception2)
        {
            throw new IllegalArgumentException("Invalid SIP user address; " + exception2.getMessage());
        }
        _fldchar = sipaddress.getUser();
        Protocol._mthif("Configuration User Name: " + _fldchar);
    }

    public static synchronized x _mthdo()
    {
        if(b == null)
            b = new x();
        return b;
    }

    public w a(int j, m m1)
        throws IOException
    {
        w w1;
        _fldnull = true;
        _fldfor = false;
        w1 = null;
        if(_fldgoto == null)
        {
            Protocol._mthif("TxHandler: opening shared system point");
            _fldgoto = new w(this, e, true);
            e = _fldgoto._mthfor();
            StringBuffer stringbuffer = new StringBuffer("SIP/2.0");
            stringbuffer.append("/");
            stringbuffer.append("UDP");
            stringbuffer.append(" ");
            stringbuffer.append(_fldgoto._mthint());
            stringbuffer.append(":");
            stringbuffer.append(_fldgoto._mthfor());
            _fldlong = stringbuffer.toString();
            _fldint = _fldgoto._mthint();
            Protocol._mthif("TxHandler: system point open, local Via is:\n\t" + stringbuffer);
            _fldgoto.a(new SipAddress("sip:" + _fldchar + "@" + _fldint + ":" + e));
            _fldbyte.put(new Integer(_fldgoto._mthfor()), _fldgoto);
        }
        if(j == -1)
            j = e;
        if(m1 == null)
            break MISSING_BLOCK_LABEL_466;
        w1 = (w)_fldbyte.get(new Integer(j));
        if(w1 == null)
            break MISSING_BLOCK_LABEL_355;
        Protocol._mthif("TxHandler: found existing listening point");
        if(!w1._mthif(m1))
            throw new Exception("Could not bind new SipConnectionNotifier with application identifier type: '" + m1.a() + "' to listening point at port " + j);
        _fldfor = true;
        _fldnull = false;
        a();
        return w1;
        Protocol._mthif("TxHandler: new listening point");
        w1 = new w(this, j, false);
        w1.a(new SipAddress("sip:" + _fldchar + "@" + _fldint + ":" + w1._mthfor()));
        if(!w1._mthif(m1))
            throw new Exception("TxHandler: fatal internal error, could not add notifier to newly created listening point!");
        _fldbyte.put(new Integer(w1._mthfor()), w1);
        _fldfor = true;
        _fldnull = false;
        a();
        break MISSING_BLOCK_LABEL_513;
        Object obj;
        obj;
        throw obj;
        obj;
        _fldfor = false;
        _fldnull = false;
        throw new IOException(((Exception) (obj)).getMessage());
        return w1;
    }

    public boolean _mthbyte()
    {
        synchronized(this)
        {
            try
            {
                if(_fldnull)
                {
                    Protocol._mthif("Waiting for TxHandler...");
                    wait();
                }
            }
            catch(Exception exception)
            {
                exception.printStackTrace();
            }
        }
        return _fldfor;
    }

    private void a()
    {
        synchronized(this)
        {
            notifyAll();
        }
    }

    public String _mthfor()
    {
        return _flddo;
    }

    public String _mthtry()
    {
        return _fldchar;
    }

    public String _mthcase()
    {
        return c;
    }

    public SipAddress _mthint()
    {
        return _fldcase;
    }

    public String _mthnew()
    {
        return _fldint;
    }

    public String _mthif()
    {
        return _fldlong;
    }

    public synchronized void a(d d1)
    {
        d d2 = null;
        Protocol._mthif("TxHandler: try to remove transaction " + d1._fldgoto);
        Hashtable hashtable = d;
        d2 = (d)hashtable.get(d1._fldgoto);
        if(d2 == null)
        {
            hashtable = _fldnew;
            d2 = (d)hashtable.get(d1._fldgoto);
        }
        if(d2 != null)
        {
            Protocol._mthif("TxHandler: removing transaction: " + d1._fldgoto);
            hashtable.remove(d1._fldgoto);
            d1._mthif();
            Protocol._mthif("TxHandler: " + (String)hashtable.get("name") + " table size = " + (hashtable.size() - 1));
        } else
        {
            Protocol._mthif("TxHandler: could not remove transaction");
        }
    }

    private String _mthif(int j)
    {
        a.setSeed(System.currentTimeMillis() - (long)j);
        return Integer.toString(Math.abs(a.nextInt()));
    }

    public n a(s s1, String s2, String s3)
    {
        n n1 = new n(this, s1);
        Object obj = null;
        if(s3 == null)
        {
            String s4 = _mthif(n1.hashCode());
            n1.l = s4;
            n1.o = _mthif(n1.hashCode() + 1);
            n1._fldgoto = s2 + _fldint + ":" + _fldgoto._mthfor() + "z9hG4bK" + s4;
        } else
        {
            n1._fldgoto = s2 + _fldint + ":" + _fldgoto._mthfor() + s3;
        }
        Protocol._mthif("TxHandler: new client transaction: " + n1._fldgoto);
        d.put(n1._fldgoto, n1);
        return n1;
    }

    protected f a(i j)
    {
        f f1 = null;
        int l = 0;
        l = j.getStatusCode();
        if(l == 0)
        {
            Protocol._mthif("TxHandler: internal error getting status code");
            return null;
        }
        if(l < 101 || l > 299)
            return null;
        try
        {
            f1 = new f(j);
        }
        catch(Exception exception)
        {
            Protocol._mthif("TxHandler: ServerConnection no dialog for '" + j.getMethod() + "'");
            return null;
        }
        f1.a(j.y);
        f f2 = (f)_fldtry.get(f1.getDialogID());
        if(f2 != null)
        {
            Protocol._mthif("TxHandler: using existing dialog with ID: " + f2.getDialogID());
            return f2;
        } else
        {
            Protocol._mthif("TxHandler: created new dialog with ID: " + f1.getDialogID());
            _fldtry.put(f1.getDialogID(), f1);
            return f1;
        }
    }

    protected synchronized f a(s s1)
    {
        f f1 = null;
        int j = 0;
        j = s1.getStatusCode();
        if(j == 0)
        {
            Protocol._mthif("TxHandler: internal error getting status code");
            return null;
        }
        if(j < 101 || j > 299)
            return null;
        try
        {
            f1 = new f(s1);
        }
        catch(Exception exception)
        {
            Protocol._mthif("TxHandler: ClientConnection no dialog for '" + s1.getMethod() + "'");
            return null;
        }
        Protocol._mthif("TxHandler: created new dialog with ID: " + f1.getDialogID());
        _fldtry.put(f1.getDialogID(), f1);
        return f1;
    }

    protected synchronized f _mthif(i j)
    {
        Object obj = null;
        String s1 = j.getHeader("Call-ID");
        for(Enumeration enumeration = d.elements(); enumeration.hasMoreElements();)
        {
            Object obj1 = enumeration.nextElement();
            if(obj1 instanceof n)
            {
                s s2 = ((n)obj1).r;
                SipHeader sipheader = new SipHeader("To", j.getHeader("To"));
                SipHeader sipheader1 = new SipHeader("From", s2.getHeader("From"));
                String s3 = sipheader.getParameter("tag");
                String s4 = sipheader1.getParameter("tag");
                String s5 = s2._fldint.a("Event");
                String s6 = j.getHeader("Event");
                if(s5 != null && s6 != null && s3 != null && s4 != null)
                {
                    SipHeader sipheader2 = new SipHeader("Event", s5);
                    SipHeader sipheader3 = new SipHeader("Event", s6);
                    String s7 = sipheader2.getParameter("id");
                    String s8 = sipheader3.getParameter("id");
                    boolean flag = false;
                    if(s7 == null && s8 == null)
                        flag = true;
                    else
                    if(s7 != null && s8 != null && s7.equals(s8))
                        flag = true;
                    if(s2._fldint.a("Call-ID").equals(s1) && s2.getMethod().equals("SUBSCRIBE") && sipheader2.getValue().equals(sipheader3.getValue()) && flag && s3.equals(s4))
                    {
                        f f1;
                        try
                        {
                            f1 = new f(j, s2);
                        }
                        catch(Exception exception)
                        {
                            Protocol._mthif("TxHandler: ServerConnection no dialog for '" + j.getMethod() + "'");
                            return null;
                        }
                        f f2 = (f)_fldtry.get(f1.getDialogID());
                        if(f2 != null)
                        {
                            Protocol._mthif("TxHandler: using existing dialog with ID: " + f2.getDialogID());
                            return f2;
                        } else
                        {
                            s2.q = f1;
                            _fldtry.put(f1.getDialogID(), f1);
                            Protocol._mthif("TxHandler: createDialogFromNotify: created new dialog with ID: " + f1.getDialogID());
                            return f1;
                        }
                    }
                }
            }
        }

        return null;
    }

    protected f _mthif(a a1)
    {
        String s1 = f.a(a1);
        f f1 = (f)_fldtry.get(s1);
        return f1;
    }

    protected void a(String s1)
    {
        f f1 = (f)_fldtry.remove(s1);
        if(f1 != null)
            Protocol._mthif("TxHandler: removed dialog with ID: " + f1.getDialogID());
        else
            Protocol._mthif("TxHandler: can not remove dialog with ID: " + f1.getDialogID());
    }

    public void a(n n1, u u1)
    {
        if(u1._mthchar().equals("ACK"))
        {
            Protocol._mthif("TxHandler.sendClientTx: passing ACK directly to transport");
            n _tmp = n1;
            n.a(u1);
            a(((a) (u1)), false);
            return;
        } else
        {
            com.nokia.phone.ri.sip.b._mthif(n1, u1).run();
            return;
        }
    }

    public void a(k k1, c c1)
    {
        com.nokia.phone.ri.sip.b._mthif(k1, c1).run();
    }

    public void a(a a1, boolean flag)
    {
        try
        {
            if(flag)
            {
                Protocol._mthif("Resending to: " + a1._fldfor + ":" + a1._fldnew);
            } else
            {
                Protocol._mthif("Sending to: " + a1._fldfor + ":" + a1._fldnew);
                Protocol._mthif("\n->->->-----------------------------------\n" + a1.toString() + "\n-----------------------------------------");
            }
            _fldgoto._mthif().a(a1._fldfor, a1._fldnew, a1.toString());
        }
        catch(Exception exception)
        {
            exception.printStackTrace();
        }
    }

    private String a(String s1, String s2)
    {
        s1 = s1.trim();
        SipHeader sipheader = new SipHeader("Via", s2);
        String s3 = sipheader.getValue();
        String s4 = s3.substring(s3.indexOf(' ') + 1);
        String s5 = sipheader.getParameter("branch");
        s3 = s1 + s4 + s5;
        return s3;
    }

    public void a(a a1)
    {
        Object obj = null;
        String s1 = null;
        if(a1._mthif())
            s1 = a(((c)a1)._mthint(), a1.a("Via"));
        if(a1._mthdo())
        {
            String s2 = a1.a("Via");
            if(s2 != null)
            {
                String s4 = ((u)a1)._mthchar();
                if(s4.equals("ACK"))
                    s4 = "INVITE";
                s1 = a(s4, a1.a("Via"));
            }
        }
        if(a1._mthdo())
        {
            String s3 = ((u)a1)._mthchar();
            w w1 = (w)_fldbyte.get(new Integer(a1._flddo));
            String s5 = f.a(a1);
            Protocol._mthif("TxHandler: Dialog ID=" + s5);
            f f1 = (f)_fldtry.get(s5);
            m m1 = null;
            if(f1 != null)
            {
                Protocol._mthif("TxHandler: Associate the Notifier to the Dialog");
                m1 = f1._mthif();
                if(m1 != null)
                    Protocol._mthif("TxHandler: Notifier found from Dialog " + s5);
            }
            if(m1 == null)
            {
                Protocol._mthif("TxHandler: Associate the Notifier to the ListeningPoint (e.g. Accept-Contact)");
                m1 = w1.a(a1);
                if(m1 != null)
                    Protocol._mthif("TxHandler: Notifier found from ListeningPoint port:" + w1._mthfor());
            }
            if(s3.equals("ACK"))
            {
                obj = (d)_fldnew.get(s1);
                if(obj != null)
                {
                    Protocol._mthif("TxHandler.dispatch: trans id " + s1 + " found for " + s3);
                    com.nokia.phone.ri.sip.b.a(((d) (obj)), a1).run();
                    return;
                }
                if(m1 != null)
                {
                    Protocol._mthif("TxHandler.dispatch: passing ACK up without new transaction object, id is: " + s1);
                    m1.a(null, (u)a1);
                    return;
                } else
                {
                    Protocol._mthif("TxHandler.dispatch: ACK without Dialog and Notifier, just dropped");
                    return;
                }
            }
            if(w1 != null && m1 != null)
            {
                Protocol._mthif("TxHandler.dispatch: new server transaction: " + s1);
                obj = new k(this, m1);
                obj._fldgoto = s1;
                _fldnew.put(s1, obj);
                if(obj != null)
                    com.nokia.phone.ri.sip.b.a(((d) (obj)), a1).run();
            } else
            {
                if(w1 == null)
                    Protocol._mthif("TxHandler: no listening point on " + a1._flddo);
                else
                    Protocol._mthif("TxHandler: no associated listener on " + a1._flddo);
                c c1 = ((u)a1)._mthcase();
                c1.a(405);
                com.nokia.phone.ri.sip.d.a(c1, a1);
                b.a(((a) (c1)), false);
            }
            return;
        }
        Protocol._mthif("TxHandler.dispatch: response in " + s1);
        obj = (d)d.get(s1);
        if(obj == null)
            Protocol._mthif("TxHandler.dispatch: unable to find transcation " + s1);
        else
            com.nokia.phone.ri.sip.b.a(((d) (obj)), a1).run();
    }

    public void a(int j)
    {
        w w1 = (w)_fldbyte.remove(new Integer(j));
        if(w1 != null)
            Protocol._mthif("TxHandler: removed listening point " + w1._mthfor());
        if(_fldbyte.isEmpty())
            _fldfor = false;
    }

    public static final String _fldvoid = "SIP/2.0";
    public static final String _fldelse = "UDP";
    public static final String _fldif = "z9hG4bK";
    private static x b;
    private String _fldint;
    private boolean _fldfor;
    private boolean _fldnull;
    private Hashtable d;
    private Hashtable _fldnew;
    private Hashtable _fldtry;
    private Hashtable _fldbyte;
    private w _fldgoto;
    private String _fldlong;
    private String _fldchar;
    private String _flddo;
    private String c;
    private SipAddress _fldcase;
    private int e;
    private static Random a = new Random(System.currentTimeMillis());

}
