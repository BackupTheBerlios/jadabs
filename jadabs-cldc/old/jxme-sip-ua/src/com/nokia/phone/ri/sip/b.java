// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import java.util.TimerTask;

// Referenced classes of package com.nokia.phone.ri.sip:
//            n, u, k, c, 
//            a, d

public class b extends TimerTask
{

    private b(d d1, int i)
    {
        _flddo = i;
        _fldfor = d1;
    }

    private b(d d1, int i, a a1)
    {
        _flddo = i;
        _fldfor = d1;
        _fldtry = a1;
    }

    public void run()
    {
        switch(_flddo)
        {
        case 2: // '\002'
        default:
            break;

        case 0: // '\0'
            if(_fldtry._mthdo())
            {
                ((n)_fldfor)._mthif((u)_fldtry);
                break;
            }
            if(_fldtry._mthif())
                ((k)_fldfor).a((c)_fldtry);
            break;

        case 1: // '\001'
            if(_fldtry._mthdo())
            {
                ((k)_fldfor).a((u)_fldtry);
                break;
            }
            if(_fldtry._mthif())
                ((n)_fldfor)._mthif((c)_fldtry);
            break;

        case 3: // '\003'
            _fldfor.a();
            break;

        case 4: // '\004'
            ((n)_fldfor)._mthdo((u)_fldtry);
            break;

        case 5: // '\005'
            _fldfor._mthdo();
            break;
        }
    }

    public static b _mthif(d d1, a a1)
    {
        return new b(d1, 0, a1);
    }

    public static b a(d d1, a a1)
    {
        return new b(d1, 1, a1);
    }

    public static b a(d d1)
    {
        return new b(d1, 2);
    }

    public static b _mthdo(d d1)
    {
        return new b(d1, 3);
    }

    public static b _mthdo(d d1, a a1)
    {
        return new b(d1, 4, a1);
    }

    public static b _mthif(d d1)
    {
        return new b(d1, 5);
    }

    public boolean _mthint()
    {
        return _flddo == 0;
    }

    public boolean _mthif()
    {
        return _flddo == 1;
    }

    public boolean _mthtry()
    {
        return _flddo == 2;
    }

    public boolean a()
    {
        return _flddo == 3;
    }

    public boolean _mthnew()
    {
        return _flddo == 4;
    }

    public boolean _mthfor()
    {
        return _flddo == 5;
    }

    public a _mthdo()
    {
        return _fldtry;
    }

    private static final int _fldcase = 0;
    private static final int _fldint = 1;
    private static final int _fldif = 2;
    private static final int a = 3;
    private static final int _fldnew = 4;
    private static final int _fldbyte = 5;
    private int _flddo;
    private d _fldfor;
    private a _fldtry;
}
