// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import com.sun.cldc.io.ConnectionBaseInterface;
import com.sun.midp.main.Configuration;
import java.io.*;
import java.util.Vector;
import javax.microedition.io.Connection;
import javax.microedition.sip.*;

// Referenced classes of package com.nokia.phone.ri.sip:
//            m, s, n, u, 
//            k, c, i, t, 
//            x, a, f, d

public class Protocol
    implements SipConnection, ConnectionBaseInterface
{
    protected class b extends InputStream
    {

        public int read()
            throws IOException
        {
            if(a >= _fldif.length)
                return _flddo;
            else
                return _fldif[a++];
        }

        private byte _fldif[];
        private int a;
        private int _flddo;

        public b(byte abyte0[])
        {
            a = 0;
            _flddo = -1;
            _fldif = abyte0;
        }
    }

    protected class a extends OutputStream
    {

        public void write(int i1)
            throws IOException
        {
            if(!a)
                throw new IOException("stream closed");
            if(_fldif >= _flddo.length)
            {
                throw new IOException("exceeding content output buffer");
            } else
            {
                _flddo[_fldif] = (byte)i1;
                _fldif++;
                return;
            }
        }

        public void close()
            throws IOException
        {
            if(!a)
                return;
            a = false;
            if(i != 10)
                send();
        }

        public boolean a()
        {
            return a;
        }

        private byte _flddo[];
        private int _fldif;
        private boolean a;

        public a(byte abyte0[])
        {
            _fldif = 0;
            a = true;
            _flddo = abyte0;
        }
    }


    public Protocol()
    {
        w = 0;
        i = 0;
        d = false;
        _fldint = null;
        l = null;
        o = null;
        _fldelse = x._mthdo();
    }

    public Connection openPrim(String s1, int i1, boolean flag)
        throws IOException
    {
        x x1 = x._mthdo();
        JVM INSTR monitorenter ;
        _fldlong = Configuration.getProperty("SIPA_DEBUG");
        if(s1.trim().length() == 0)
            return new m(0, i1, flag, null);
        String s2;
        int j1;
        int k1;
        s2 = null;
        j1 = -1;
        k1 = s1.indexOf(";type=");
        if(k1 == -1) goto _L2; else goto _L1
_L1:
        s2 = s1.substring(k1 + 6);
        _mthif("Protocol: Initialize Notifier with application MIME-Type: " + s2 + " and share system SIP port");
        j1 = Integer.parseInt(s1.substring(0, k1));
          goto _L3
        NumberFormatException numberformatexception;
        numberformatexception;
_L3:
        new m(j1, i1, flag, s2);
        x1;
        JVM INSTR monitorexit ;
        return;
_L2:
        try
        {
            j1 = Integer.parseInt(s1);
            break MISSING_BLOCK_LABEL_144;
        }
        catch(NumberFormatException numberformatexception1) { }
        if(j1 == -1) goto _L5; else goto _L4
_L4:
        new m(j1, i1, flag, s2);
        x1;
        JVM INSTR monitorexit ;
        return;
_L5:
        new s(s1, i1, flag);
        x1;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        throw exception;
    }

    public void close()
    {
        b = null;
        _fldcase = null;
        q = null;
        i = -1;
    }

    protected void _mthif()
        throws IOException
    {
        if(i == -1)
            throw new IOException("SIP connection closed");
        else
            return;
    }

    private void a(String s1)
        throws SipException
    {
        if(i != 1)
            throw new SipException("Can not edit " + s1 + " in this sate", (byte)5);
        else
            return;
    }

    protected boolean a()
    {
        return i == 1 || i == 5 || i == 3 || i == 4 || i == 2;
    }

    public void send()
        throws IOException, IllegalArgumentException, InterruptedIOException, SecurityException, SipException
    {
        if(i != 1 && i != 2)
            throw new SipException("Can not send in this state", (byte)5);
        if(b != null && i == 2)
        {
            i = 10;
            if(b.a())
                b.close();
            if(u != null)
                l.a(u);
            u = null;
        }
        i = 10;
        if(w == 10)
        {
            _fldelse.a((n)e, (u)l);
            String s1 = ((u)l)._mthchar();
            if(s1.equals("ACK"))
                i = 4;
            else
                i = 3;
        } else
        {
            _fldelse.a((k)e, (c)l);
            int i1 = ((c)l)._mthnew();
            if(i1 < 200)
                i = 5;
            else
                i = 4;
            if(q == null)
                q = _fldelse.a((i)this);
            else
            if(i1 < 200)
                q.a((byte)1);
            else
            if(i1 < 300)
                q.a((byte)2);
            else
                q.a((byte)0);
            String s2 = ((c)l)._mthint();
            if(q != null && s2.equals("BYE") && i1 >= 200 && i1 < 300)
                q.a((byte)0);
        }
    }

    public void setHeader(String s1, String s2)
        throws SipException
    {
        a("headers");
        l.a(s1, s2, true);
    }

    public void addHeader(String s1, String s2)
        throws SipException
    {
        a("headers");
        l.a(s1, s2);
    }

    public void removeHeader(String s1)
        throws SipException
    {
        a("headers");
        l._mthfor(s1);
    }

    public String[] getHeaders(String s1)
    {
        try
        {
            _mthif();
        }
        catch(IOException ioexception)
        {
            return null;
        }
        if(!a())
            return null;
        Vector vector = l._mthif(s1);
        String as[] = null;
        if(vector != null)
        {
            as = new String[vector.size()];
            for(short word0 = 0; word0 < vector.size(); word0++)
            {
                t t1 = (t)vector.elementAt(word0);
                as[word0] = t1.a();
            }

        }
        return as;
    }

    public String getHeader(String s1)
    {
        try
        {
            _mthif();
        }
        catch(IOException ioexception)
        {
            return null;
        }
        if(!a())
            return null;
        else
            return l.a(s1);
    }

    public String getMethod()
    {
        try
        {
            _mthif();
        }
        catch(IOException ioexception)
        {
            return null;
        }
        if(!a())
            return null;
        if(l._mthdo())
            return ((u)l)._mthchar();
        else
            return ((c)l)._mthint();
    }

    public String getRequestURI()
    {
        try
        {
            _mthif();
        }
        catch(IOException ioexception)
        {
            return null;
        }
        if(!a())
            return null;
        if(l != null && l._mthdo())
            return ((u)l)._mthbyte().toString();
        else
            return null;
    }

    public int getStatusCode()
    {
        try
        {
            _mthif();
        }
        catch(IOException ioexception)
        {
            return 0;
        }
        if(!a())
            return 0;
        if(l._mthif())
            return ((c)l)._mthnew();
        else
            return 0;
    }

    public String getReasonPhrase()
    {
        try
        {
            _mthif();
        }
        catch(IOException ioexception)
        {
            return null;
        }
        if(!a())
            return null;
        if(l._mthif())
            return ((c)l)._mthfor();
        else
            return null;
    }

    public SipDialog getDialog()
    {
        try
        {
            _mthif();
        }
        catch(IOException ioexception)
        {
            return null;
        }
        if(!a())
            return null;
        else
            return q;
    }

    public InputStream openContentInputStream()
        throws IOException, SipException
    {
        if(w == 10)
        {
            if(i != 3 && i != 4)
                throw new SipException("Can not open InputStream in this state", (byte)5);
        } else
        if(i != 5)
            throw new SipException("Can not open InputStream in this state", (byte)5);
        c = l.a();
        if(c == null)
            throw new IOException("no input available");
        else
            return _fldcase = new b(c);
    }

    public OutputStream openContentOutputStream()
        throws SipException
    {
        if(i != 1)
            throw new SipException("Can not open OutputStream in this state", (byte)5);
        int i1 = 0;
        String s1 = getHeader("Content-Length");
        if(s1 == null)
            throw new SipException("Content-Length not set", (byte)4);
        if(s1 != null)
            try
            {
                i1 = Integer.parseInt(s1);
            }
            catch(Exception exception)
            {
                throw new SipException("Illegal Content-Length", (byte)4);
            }
        if(getHeader("Content-Type") == null)
        {
            throw new SipException("Content-Type not set", (byte)3);
        } else
        {
            u = new byte[i1];
            i = 2;
            return b = new a(u);
        }
    }

    protected static void _mthif(String s1)
    {
        if(_fldlong != null)
            System.out.println(s1);
    }

    public static final String _fldbyte = "REGISTER";
    public static final String a = "INVITE";
    public static final String p = "ACK";
    public static final String k = "CANCEL";
    public static final String g = "BYE";
    public static final String t = "OPTIONS";
    public static final String m = "MESSAGE";
    public static final String h = "SUBSCRIBE";
    public static final String r = "NOTIFY";
    public static final String s = "PUBLISH";
    public static final String f = "PRACK";
    public static final String _fldnull = "REFER";
    protected static final byte j = -1;
    protected static final byte _fldnew = 0;
    protected static final byte _fldgoto = 1;
    protected static final byte _fldfor = 2;
    protected static final byte _flddo = 3;
    protected static final byte n = 4;
    protected static final byte _fldchar = 5;
    protected static final byte _fldvoid = 10;
    protected static final byte _fldtry = 10;
    protected static final byte v = 11;
    protected byte w;
    protected byte i;
    protected boolean d;
    protected x _fldelse;
    protected SipAddress _fldif;
    protected d e;
    protected f q;
    protected com.nokia.phone.ri.sip.a _fldint;
    protected com.nokia.phone.ri.sip.a l;
    protected com.nokia.phone.ri.sip.a o;
    protected byte u[];
    protected byte c[];
    protected a b;
    protected b _fldcase;
    private static String _fldlong = null;

}
