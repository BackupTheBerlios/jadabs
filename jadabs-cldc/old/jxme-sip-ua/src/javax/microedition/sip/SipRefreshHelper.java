// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package javax.microedition.sip;

import com.nokia.phone.ri.sip.p;
import java.io.OutputStream;

public class SipRefreshHelper
{

    private SipRefreshHelper()
    {
    }

    public static SipRefreshHelper getInstance()
    {
        if(_fldif == null)
        {
            a = p.a();
            _fldif = new SipRefreshHelper();
        }
        return _fldif;
    }

    public void stop(int i)
    {
        a.a(i);
    }

    public OutputStream update(int i, String as[], String s, int j, int k)
    {
        return a.a(i, as, s, j, k);
    }

    private static SipRefreshHelper _fldif;
    private static p a;
}
