// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import java.util.Timer;

// Referenced classes of package com.nokia.phone.ri.sip:
//            d, a, Protocol, u, 
//            m, b, c, x, 
//            i

public class k extends d
{

    public k(x x1, m m1)
    {
        h = x1;
        q = m1;
        _fldint = 1;
    }

    public int a(u u1)
    {
        Object obj = n;
        JVM INSTR monitorenter ;
        j = u1;
        if(a == null)
        {
            a = u1;
            break MISSING_BLOCK_LABEL_53;
        }
        if(d == null || !d.a(u1))
            break MISSING_BLOCK_LABEL_53;
        Protocol._mthif("---- ServerTransaction.receiveRequest(): Request Already Received: Dropped ----");
        return 0;
        String s;
        d = u1;
        s = u1._mthchar();
        _fldint;
        JVM INSTR tableswitch 1 5: default 238
    //                   1 100
    //                   2 238
    //                   3 238
    //                   4 139
    //                   5 147;
           goto _L1 _L2 _L1 _L1 _L3 _L4
_L2:
        if(s.equals("INVITE"))
            a((short)4);
        else
            a((short)3);
        q.a(this, u1);
        1;
        obj;
        JVM INSTR monitorexit ;
        return;
_L3:
        _mthfor();
        1;
        obj;
        JVM INSTR monitorexit ;
        return;
_L4:
        Protocol._mthif("ServerTransaction: S_COMPLETED received " + s);
        if(!s.equals("ACK")) goto _L6; else goto _L5
_L5:
        a((short)6);
        _fldcase.schedule(b._mthdo(this), 10000L);
          goto _L7
_L6:
        Protocol._mthif("ServerTransaction.receiveRequest(): should we resend last response ?");
        a((short)7);
        _fldcase.schedule(b._mthdo(this), 10000L);
        -1;
        obj;
        JVM INSTR monitorexit ;
        return;
_L7:
        1;
        obj;
        JVM INSTR monitorexit ;
        return;
_L1:
        a((short)7);
        _fldcase.schedule(b._mthdo(this), 10000L);
        -1;
        obj;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        throw exception;
    }

    public int a(c c1)
    {
        Object obj = n;
        JVM INSTR monitorenter ;
        int i = 0;
        _fldint;
        JVM INSTR lookupswitch 2: default 403
    //                   3: 40
    //                   4: 40;
           goto _L1 _L2 _L2
_L2:
        int j = c1._mthnew();
        if(99 >= j || j >= 200) goto _L4; else goto _L3
_L3:
        a((short)4);
        i = a(((a) (c1)), ((a) (a)));
        if(i == 1)
        {
            _flddo = c1;
            h.a(c1, false);
        } else
        {
            Protocol._mthif("Problem routing the response! " + j);
        }
        return i;
_L4:
        if(199 >= j || j >= 300) goto _L6; else goto _L5
_L5:
        i = a(((a) (c1)), ((a) (a)));
        if(i == 1)
        {
            _flddo = c1;
            h.a(c1, false);
            if(this.j._mthchar().equals("INVITE"))
            {
                a((short)7);
                _fldcase.schedule(b._mthdo(this), 5000L);
            } else
            {
                a((short)5);
                _fldcase.schedule(b._mthdo(this), 5000L);
            }
        } else
        {
            Protocol._mthif("Problem routing the response! " + j);
        }
        i;
        obj;
        JVM INSTR monitorexit ;
        return;
_L6:
        if(299 >= j || j >= 700) goto _L8; else goto _L7
_L7:
        a((short)5);
        Protocol._mthif("ServerTransaction: Sending 3xx-699 response -> S_COMPLETED");
        i = a(((a) (c1)), ((a) (a)));
        if(i != 1) goto _L10; else goto _L9
_L9:
        _flddo = c1;
        h.a(c1, false);
        if(this.j._mthchar().equals("INVITE"))
            _fldcase.schedule(b._mthif(this), 500L);
        else
            _fldcase.schedule(b._mthdo(this), 5000L);
        1;
        obj;
        JVM INSTR monitorexit ;
        return;
_L10:
        Protocol._mthif("Problem routing the response! " + j);
        i;
        obj;
        JVM INSTR monitorexit ;
        return;
_L8:
        -1;
        obj;
        JVM INSTR monitorexit ;
        return;
_L1:
        -1;
        obj;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        throw exception;
    }

    private void _mthfor()
    {
        Protocol._mthif("ServerTransaction.resendLastResponse()");
        h.a(_flddo, true);
    }

    public int _mthdo()
    {
        Object obj = n;
        JVM INSTR monitorenter ;
        _fldint;
        JVM INSTR lookupswitch 1: default 69
    //                   5: 28;
           goto _L1 _L2
_L2:
        Protocol._mthif("ServerTranasction.ackTimer() in COMPLETED state");
        _mthfor();
        a((short)6);
        _fldcase.schedule(b._mthdo(this), 64 * 500);
        return 1;
_L1:
        Protocol._mthif("ServerTranasction.ackTimer() in state " + _fldint);
        a((short)7);
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
            a((short)7);
            _fldcase.schedule(b._mthdo(this), 5000L);
            // fall through

        case 6: // '\006'
        case 7: // '\007'
            _fldcase.cancel();
            h.a(this);
            a((short)8);
            return;
        }
        Protocol._mthif("ServerTransaction.transactionTimer():transaction timer fired in state: " + _fldint);
        break MISSING_BLOCK_LABEL_120;
        Exception exception;
        exception;
        throw exception;
    }

    public m q;
    public i p;
}
