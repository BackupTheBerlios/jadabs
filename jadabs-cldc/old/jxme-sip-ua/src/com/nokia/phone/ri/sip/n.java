// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import java.util.Timer;

// Referenced classes of package com.nokia.phone.ri.sip:
//            d, c, s, u, 
//            b, x, Protocol

public class n extends d
{

    public n(x x1, s s1)
    {
        h = x1;
        r = s1;
        _fldint = 1;
    }

    public int _mthif(c c1)
    {
        Object obj = n;
        JVM INSTR monitorenter ;
        int i = c1._mthnew();
        _fldint;
        JVM INSTR tableswitch 3 7: default 436
    //                   3 48
    //                   4 48
    //                   5 285
    //                   6 436
    //                   7 368;
           goto _L1 _L2 _L2 _L3 _L1 _L4
_L2:
        if(99 >= i || i >= 200) goto _L6; else goto _L5
_L5:
        _fldint = 4;
        r.a(c1);
        return 1;
_L6:
        if(199 >= i || i >= 300) goto _L8; else goto _L7
_L7:
        if(a._mthchar().equals("INVITE"))
            _fldint = 7;
        else
            _fldint = 5;
        _fldcase.schedule(b._mthdo(this), 10000L);
        r.a(c1);
        1;
        obj;
        JVM INSTR monitorexit ;
        return;
_L8:
        if(299 >= i || i >= 700) goto _L10; else goto _L9
_L9:
        _fldint = 5;
        if(a._mthchar().equals("INVITE"))
        {
            j = r.a("ACK", null);
            try
            {
                j.a("Via", a.a("Via"), true);
            }
            catch(Exception exception) { }
            a(j);
            h.a(j, false);
            _fldcase.schedule(b._mthif(this), 15000L);
        } else
        {
            _fldcase.schedule(b._mthdo(this), 5000L);
        }
        r.a(c1);
        1;
        obj;
        JVM INSTR monitorexit ;
        return;
_L10:
        -1;
        obj;
        JVM INSTR monitorexit ;
        return;
_L3:
        if(299 >= i || i >= 700 || !a._mthchar().equals("INVITE")) goto _L12; else goto _L11
_L11:
        a(j);
        h.a(j, true);
        1;
        obj;
        JVM INSTR monitorexit ;
        return;
_L12:
        Protocol._mthif("ClientTransaction.receiveResponse ignoring response " + i + " in S_COMPLETED state");
        1;
        obj;
        JVM INSTR monitorexit ;
        return;
_L4:
        if(199 < i && i < 299 && a._mthchar().equals("INVITE"))
            r.a(c1);
        Protocol._mthif("ClientTransaction.receiveResponse ignoring response " + i + " in S_TERMINATED state");
        1;
        obj;
        JVM INSTR monitorexit ;
        return;
_L1:
        throw new IllegalStateException("wrong state in ClientTransaction.receiveResponse()");
        Exception exception1;
        exception1;
        throw exception1;
    }

    public int _mthif(u u1)
    {
        Object obj = n;
        JVM INSTR monitorenter ;
        if(a == null)
            a = u1;
        _fldint;
        JVM INSTR tableswitch 1 5: default 117
    //                   1 56
    //                   2 117
    //                   3 56
    //                   4 117
    //                   5 107;
           goto _L1 _L2 _L1 _L2 _L1 _L3
_L2:
        _fldint = 3;
        a(u1);
        h.a(u1, false);
        b = 0;
        _fldnull = 500;
        _fldcase.schedule(b._mthdo(this, u1), _fldnull);
        return 1;
_L3:
        throw new IllegalStateException("ClientTransaction.sendRequest S_COMPLETED");
_L1:
        throw new IllegalStateException("ClientTransaction.sendRequest default:");
        Exception exception;
        exception;
        throw exception;
    }

    public int _mthdo(u u1)
    {
        Object obj = n;
        JVM INSTR monitorenter ;
        _fldint;
        JVM INSTR lookupswitch 2: default 167
    //                   3: 36
    //                   4: 163;
           goto _L1 _L2 _L3
_L2:
        if(b != 6) goto _L5; else goto _L4
_L4:
        Protocol._mthif("ClientTransaction.resend: max resend reached");
        _fldnull = 500;
        b = 0;
        r._mthfor();
        return 1;
_L5:
        Protocol._mthif("ClientTransaction.resendRequest: resend timer fired ");
        a(u1);
        h.a(u1, true);
        _fldnull = Math.min(_fldnull * 2, 4000);
        b++;
        Protocol._mthif("ClientTransaction.resend: new Timer E = " + _fldnull);
        _fldcase.schedule(b._mthdo(this, u1), _fldnull);
        1;
        obj;
        JVM INSTR monitorexit ;
        return;
_L3:
        1;
        obj;
        JVM INSTR monitorexit ;
        return;
_L1:
        1;
        obj;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        throw exception;
    }

    public int _mthdo()
    {
        Object obj = n;
        JVM INSTR monitorenter ;
        _fldint;
        JVM INSTR lookupswitch 1: default 47
    //                   5: 28;
           goto _L1 _L2
_L2:
        Protocol._mthif("ClientTransaction.ackTimer: S_COMPLETED");
        _fldint = 7;
        _mthint();
        return 1;
_L1:
        -1;
        obj;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        throw exception;
    }

    public void a()
    {
        Object obj = n;
        JVM INSTR monitorenter ;
        switch(_fldint)
        {
        case 5: // '\005'
        case 7: // '\007'
            _mthint();
            return;
        }
        break MISSING_BLOCK_LABEL_53;
        Exception exception;
        exception;
        throw exception;
    }

    private void _mthint()
    {
        _fldcase.cancel();
        h.a(this);
        _fldint = 8;
    }

    public s r;
}
