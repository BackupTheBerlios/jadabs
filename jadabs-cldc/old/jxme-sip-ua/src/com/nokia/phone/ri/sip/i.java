// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import javax.microedition.sip.*;

// Referenced classes of package com.nokia.phone.ri.sip:
//            Protocol, u, c, x, 
//            f, a, w, p, 
//            d, m

public class i extends Protocol
    implements SipServerConnection
{

    protected i(d d1, u u1, w w1, m m1)
    {
        e = d1;
        _fldelse = com.nokia.phone.ri.sip.x._mthdo();
        _fldint = u1;
        l = u1;
        x = w1;
        y = m1;
        i = 5;
        q = _fldelse._mthif(u1);
        if(q != null)
            Protocol._mthif("ServerConnection: associating " + getMethod() + " with existing dialog: " + q.getDialogID());
    }

    public void initResponse(int j)
        throws IllegalArgumentException, SipException
    {
        if(i != 5)
            throw new SipException("Can not init response in this state", (byte)5);
        if(j < 100 || j > 699)
            throw new IllegalArgumentException("Status code out-of-range");
        c c1 = ((u)_fldint)._mthcase();
        c1.a(j);
        String s = _fldint.a("To");
        try
        {
            SipHeader sipheader = new SipHeader("To", s);
            if(sipheader.getParameter("tag") == null)
                sipheader.setParameter("tag", Integer.toString(x._mthdo().toString().hashCode()));
            c1.a("To", sipheader.getHeaderValue(), true);
        }
        catch(Exception exception)
        {
            throw new SipException(exception.getMessage(), (byte)0);
        }
        String s1 = ((u)_fldint)._mthchar();
        if(s1.equals("INVITE") || s1.equals("SUBSCRIBE") || s1.equals("REFER"))
            c1.a("Contact", x._mthdo().toString());
        l = c1;
        i = 1;
    }

    public void setReasonPhrase(String s)
        throws IllegalArgumentException, SipException
    {
        if(i != 1)
            throw new SipException("Can not set reason phrase in this state", (byte)5);
        if(s == null)
        {
            throw new IllegalArgumentException("Phrase is null");
        } else
        {
            ((c)l)._mthtry(s);
            return;
        }
    }

    public void close()
    {
        i = -1;
        super.close();
    }

    protected void _mthdo()
    {
        String s = getMethod();
        if(s.equals("NOTIFY"))
        {
            if(q == null)
                q = _fldelse._mthif(this);
            if(q == null)
            {
                Protocol._mthif("Internal error: dialog == null");
                return;
            }
            try
            {
                q.a((u)l);
            }
            catch(Exception exception)
            {
                Protocol._mthif("ServerConnection: Error updating the dialog info; " + exception.getMessage());
                Protocol._mthif("ServerConnection: Terminate the dialog state");
                q.a((byte)0);
            }
            p p1 = p.a();
            p1.a((u)l);
        }
    }

    protected w x;
    protected m y;
}
