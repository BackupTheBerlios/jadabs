// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.EmptyStackException;
import java.util.Vector;
import javax.microedition.sip.*;

// Referenced classes of package com.nokia.phone.ri.sip:
//            i, x, w, Protocol, 
//            d, u

public class m
    implements SipConnectionNotifier
{

    public m(int j, int k, boolean flag, String s)
        throws IOException
    {
        _fldtry = null;
        _flddo = null;
        _fldfor = null;
        _fldint = true;
        _fldnew = null;
        a = new Vector();
        _fldnew = s;
        _fldtry = x._mthdo();
        _fldif = _fldtry.a(j, this);
        _flddo = _fldtry._mthnew();
    }

    private void _mthdo()
        throws IOException
    {
        if(!_fldint)
            throw new IOException("SipConnectionNotifier closed");
        else
            return;
    }

    public SipServerConnection acceptAndOpen()
        throws IOException, InterruptedIOException
    {
        _mthdo();
        m m1 = this;
        JVM INSTR monitorenter ;
        i j;
        while(a.isEmpty()) 
        {
            Protocol._mthif("Notifier.acceptAndOpen(): port: " + _fldif._mthfor() + " waiting for request...");
            wait();
            if(!_fldint)
                throw new IOException("SipConnectionNotifier closed");
            Protocol._mthif("Notifier.acceptAndOpen(): port: " + _fldif._mthfor() + " running...");
            if(a.isEmpty())
                throw new InterruptedIOException("Error opening SIP server connection, empty receive buffer");
        }
        j = (i)a.firstElement();
        a.removeElement(j);
        j._mthdo();
        return j;
        Object obj;
        obj;
        throw new IOException(((EmptyStackException) (obj)).getMessage());
        obj;
        throw new IOException(((InterruptedException) (obj)).getMessage());
        Exception exception;
        exception;
        throw exception;
    }

    protected void a(d d1, u u1)
    {
        synchronized(this)
        {
            i j = new i(d1, u1, _fldif, this);
            a.addElement(j);
            notify();
        }
        try
        {
            if(_fldfor != null)
                _fldfor.notifyRequest(this);
        }
        catch(Exception exception) { }
    }

    public void close()
    {
        synchronized(this)
        {
            if(_fldint)
            {
                _fldint = false;
                notify();
                _fldif.a(this);
            }
        }
    }

    public void setListener(SipServerConnectionListener sipserverconnectionlistener)
        throws IOException
    {
        _mthdo();
        _fldfor = sipserverconnectionlistener;
    }

    public String getLocalAddress()
        throws IOException
    {
        _mthdo();
        return _flddo;
    }

    public int getLocalPort()
        throws IOException
    {
        _mthdo();
        return _fldif._mthfor();
    }

    protected SipAddress _mthif()
    {
        return _fldif._mthdo();
    }

    public String a()
    {
        return _fldnew;
    }

    private x _fldtry;
    private String _flddo;
    private SipServerConnectionListener _fldfor;
    private boolean _fldint;
    private w _fldif;
    private String _fldnew;
    private Vector a;
}
