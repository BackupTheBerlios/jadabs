// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.sip.SipAddress;
import javax.microedition.sip.SipException;

// Referenced classes of package com.nokia.phone.ri.sip:
//            t, c, u, j, 
//            a

public class h
{

    public h()
    {
        _fldcase = new byte[120];
        _flddo = 32;
        f = null;
        _fldlong = true;
        _fldint = null;
    }

    public byte a(byte byte0)
    {
        byte byte1 = _flddo;
        _flddo = byte0;
        return byte1;
    }

    public a a(InputStream inputstream)
        throws IOException, SipException
    {
        Vector vector = new Vector();
        Vector vector1 = new Vector();
        _mthif(inputstream, vector);
        if(vector.size() > 3)
        {
            _fldlong = false;
            _fldint = "1 Line is not correct";
        }
        a(inputstream, vector1);
        t t1 = _mthdo(vector1, "Content-Type");
        byte abyte0[] = a(inputstream, vector1, t1);
        return a(vector, vector1, abyte0, t1);
    }

    private void _mthif(InputStream inputstream, Vector vector)
        throws IOException, SipException
    {
        boolean flag = false;
        int k = 0;
        int l = 0;
        char ac[] = new char[2];
        boolean flag1 = false;
        do
        {
            int i;
            if((i = inputstream.read()) == -1)
                break;
            if(i == 37)
            {
                ac[0] = (char)inputstream.read();
                ac[1] = (char)inputstream.read();
                try
                {
                    i = Integer.parseInt(new String(ac), 16);
                }
                catch(Exception exception)
                {
                    _fldlong = false;
                    _fldint = "Problem with % in 1Line";
                    if((i = inputstream.read()) != -1)
                        break;
                }
            }
            switch(l)
            {
            case 0: // '\0'
                switch(i)
                {
                default:
                    k = _mthif(i, k);
                    l = 1;
                    break;

                case 9: // '\t'
                case 10: // '\n'
                case 13: // '\r'
                case 32: // ' '
                    break;
                }
                break;

            case 1: // '\001'
                switch(i)
                {
                case 32: // ' '
                    l = 2;
                    k = a(vector, k);
                    break;

                default:
                    k = _mthif(i, k);
                    break;
                }
                break;

            case 2: // '\002'
                switch(i)
                {
                case 32: // ' '
                    k = a(vector, k);
                    l = 3;
                    break;

                case 13: // '\r'
                    i = inputstream.read();
                    if(i == 10)
                    {
                        flag1 = true;
                    } else
                    {
                        _fldlong = false;
                        flag1 = true;
                        _fldint = "CR without LF in 1Line";
                    }
                    break;

                case 10: // '\n'
                    flag1 = true;
                    break;

                default:
                    k = _mthif(i, k);
                    break;
                }
                break;

            case 3: // '\003'
                switch(i)
                {
                case 13: // '\r'
                    i = inputstream.read();
                    if(i == 10)
                    {
                        flag1 = true;
                    } else
                    {
                        _fldlong = false;
                        flag1 = true;
                        _fldint = "CR without LF in 1Line";
                    }
                    break;

                case 10: // '\n'
                    flag1 = true;
                    break;

                default:
                    k = _mthif(i, k);
                    break;
                }
                break;
            }
        } while(!flag1);
        if(k != 0)
            k = a(vector, k);
    }

    private void a(InputStream inputstream, Vector vector)
        throws IOException, SipException
    {
        boolean flag = false;
        int k = 0;
        boolean flag1 = false;
        boolean flag2 = false;
        char ac[] = new char[2];
        byte byte0 = 4;
        byte byte1 = 4;
        do
        {
            int i;
            if((i = inputstream.read()) == -1)
                break;
            if(i == 37)
            {
                ac[0] = (char)inputstream.read();
                ac[1] = (char)inputstream.read();
                try
                {
                    i = Integer.parseInt(new String(ac), 16);
                }
                catch(Exception exception)
                {
                    _fldlong = false;
                    _fldint = "Problem with % in Header";
                    if((i = inputstream.read()) != -1)
                        break;
                }
            }
            switch(byte0)
            {
            case 4: // '\004'
                switch(i)
                {
                case 13: // '\r'
                    byte0 = 8;
                    byte1 = 4;
                    break;

                case 10: // '\n'
                    byte0 = 9;
                    byte1 = 4;
                    break;

                case 58: // ':'
                    if(k > 0)
                    {
                        k = a(k);
                        byte0 = 6;
                        byte1 = 7;
                    } else
                    {
                        throw new SipException("Header without value", (byte)0);
                    }
                    break;

                default:
                    k = _mthif(i, k);
                    break;

                case 9: // '\t'
                case 32: // ' '
                    break;
                }
                break;

            case 6: // '\006'
                switch(i)
                {
                case 13: // '\r'
                    byte0 = 8;
                    break;

                case 10: // '\n'
                    byte0 = 9;
                    break;

                case 9: // '\t'
                case 32: // ' '
                    byte0 = 6;
                    break;

                case 34: // '"'
                    k = _mthif(i, k);
                    byte0 = 10;
                    break;

                case 59: // ';'
                    _fldlong = false;
                    k = _mthif(i, k);
                    _fldint = "; in header start";
                    break;

                default:
                    k = _mthif(i, k);
                    byte0 = 7;
                    break;
                }
                break;

            case 7: // '\007'
                switch(i)
                {
                case 13: // '\r'
                    byte0 = 8;
                    break;

                case 10: // '\n'
                    byte0 = 9;
                    break;

                case 34: // '"'
                    k = _mthif(i, k);
                    byte0 = 10;
                    break;

                case 9: // '\t'
                case 32: // ' '
                    k = _mthif(_flddo, k);
                    break;

                case 44: // ','
                    if(f != null && f.equals("Expires"))
                    {
                        k = _mthif(i, k);
                    } else
                    {
                        k = _mthif(vector, k);
                        byte0 = 6;
                    }
                    break;

                default:
                    k = _mthif(i, k);
                    byte0 = 7;
                    break;
                }
                break;

            case 8: // '\b'
                switch(i)
                {
                case 13: // '\r'
                    k = _mthif(vector, k);
                    byte0 = 4;
                    break;

                case 10: // '\n'
                    byte0 = 9;
                    break;

                case 9: // '\t'
                case 32: // ' '
                    k = _mthif(_flddo, k);
                    byte0 = byte1;
                    break;

                default:
                    k = _mthif(vector, k);
                    byte0 = 4;
                    k = _mthif(i, k);
                    if(byte1 == 10)
                        _fldlong = false;
                    _fldint = "End of line with hyphen but not continue";
                    break;
                }
                break;

            case 9: // '\t'
                switch(i)
                {
                case 10: // '\n'
                    flag2 = true;
                    break;

                case 9: // '\t'
                case 32: // ' '
                    k = _mthif(_flddo, k);
                    byte0 = byte1;
                    break;

                default:
                    k = _mthif(vector, k);
                    k = _mthif(i, k);
                    byte0 = 4;
                    if(byte1 == 10)
                        _fldlong = false;
                    _fldint = "End of line in Hyphen but line not continue";
                    break;

                case 13: // '\r'
                    break;
                }
                break;

            case 10: // '\n'
                switch(i)
                {
                case 34: // '"'
                    k = _mthif(i, k);
                    byte0 = byte1;
                    break;

                case 92: // '\\'
                    i = inputstream.read();
                    k = _mthif(i, k);
                    break;

                case 13: // '\r'
                    byte1 = 10;
                    byte0 = 8;
                    break;

                case 10: // '\n'
                    byte1 = 10;
                    byte0 = 9;
                    break;

                default:
                    k = _mthif(i, k);
                    break;
                }
                break;
            }
        } while(!flag2);
        if(k != 0)
            k = _mthif(vector, k);
    }

    public byte[] a(InputStream inputstream, Vector vector, t t1)
        throws IOException, SipException
    {
        t t2 = _mthdo(vector, "Content-Length");
        if(t2 == null)
            t2 = new t("Content-Length", "0");
        byte abyte0[] = _mthif(inputstream);
        int i = 0;
        if(abyte0 == null)
            i = 0;
        else
            i = abyte0.length;
        int k = Integer.parseInt(t2.a());
        if(k >= 0 && k <= i)
        {
            if(abyte0 != null && k != 0 && k < i)
            {
                byte abyte1[] = new byte[k];
                System.arraycopy(abyte0, 0, abyte1, 0, k);
                abyte0 = abyte1;
            } else
            if(t1 != null && t1.a().equals("text/lpidf"))
                t2.a("0");
            else
            if(t1 != null && t2.a().equals("0"))
                t2.a(Integer.toString(i));
        } else
        {
            _fldlong = false;
            _fldint = "Size of the payload is wrong Content-Length=" + k + " buffer=" + inputstream.available();
            t2.a("0");
        }
        a(vector, t2._mthfor(), t2.a());
        if(t2.a().equals("0"))
            abyte0 = null;
        return abyte0;
    }

    private byte[] _mthif(InputStream inputstream)
        throws IOException
    {
        byte abyte0[] = new byte[inputstream.available()];
        int i = 0;
        byte byte0 = 11;
        boolean flag = false;
        do
        {
            int k;
            if((k = inputstream.read()) == -1)
                break;
            switch(byte0)
            {
            default:
                break;

            case 11: // '\013'
                switch(k)
                {
                case 13: // '\r'
                    byte0 = 12;
                    break;

                case 10: // '\n'
                    byte0 = 13;
                    break;
                }
                break;

            case 12: // '\f'
                switch(k)
                {
                case 10: // '\n'
                    byte0 = 13;
                    break;

                default:
                    byte0 = 11;
                    break;

                case 13: // '\r'
                    break;
                }
                break;

            case 13: // '\r'
                switch(k)
                {
                case 10: // '\n'
                    flag = true;
                    break;

                default:
                    byte0 = 11;
                    break;

                case 13: // '\r'
                    break;
                }
                break;
            }
            abyte0[i++] = (byte)k;
        } while(!flag);
        if(i != 0)
        {
            byte abyte1[] = new byte[i];
            System.arraycopy(abyte0, 0, abyte1, 0, i);
            return abyte1;
        } else
        {
            return null;
        }
    }

    private a a(Vector vector, Vector vector1, byte abyte0[], t t1)
        throws IOException, SipException
    {
        String s = ((String)vector.elementAt(0)).trim();
        t t2 = _mthfor(vector1, "From");
        t t3 = _mthfor(vector1, "To");
        t t4 = _mthfor(vector1, "CSeq");
        t t5 = _mthfor(vector1, "Call-ID");
        Vector vector2 = a(vector1, "Via");
        if(t5 == null || t4 == null || t2 == null || t3 == null || vector2.isEmpty())
        {
            _fldlong = false;
            _fldint = "From = " + t2 + " To = " + t3 + " CallID = " + t5 + " CSeq = " + t4 + " Via = " + vector2 + " headers not allowed to be null";
            throw new SipException(_fldint, (byte)0);
        }
        if(s.startsWith("SIP"))
        {
            int i = Integer.parseInt((String)vector.elementAt(1));
            if(t1 != null)
                vector1.insertElementAt(t1, 0);
            if(vector2 != null)
            {
                for(int k = vector2.size() - 1; k >= 0; k--)
                    vector1.insertElementAt(vector2.elementAt(k), 0);

            }
            if(t5 != null)
                vector1.insertElementAt(t5, 0);
            if(t4 != null)
                vector1.insertElementAt(t4, 0);
            if(t2 != null)
                vector1.insertElementAt(t2, 0);
            if(t3 != null)
                vector1.insertElementAt(t3, 0);
            c c1 = new c(vector, vector1, abyte0);
            ((c)c1).a(i);
            return c1;
        }
        s = ((String)vector.elementAt(2)).trim();
        if(s.startsWith("SIP"))
        {
            String s1 = (String)vector.elementAt(0);
            Object obj = null;
            Object obj1 = null;
            u u1;
            try
            {
                SipAddress sipaddress = new SipAddress((String)vector.elementAt(1));
                if(t1 != null)
                    vector1.insertElementAt(t1, 0);
                if(vector2 != null)
                {
                    for(int l = vector2.size() - 1; l >= 0; l--)
                        vector1.insertElementAt(vector2.elementAt(l), 0);

                }
                if(t5 != null)
                    vector1.insertElementAt(t5, 0);
                if(t4 != null)
                    vector1.insertElementAt(t4, 0);
                if(t2 != null)
                    vector1.insertElementAt(t2, 0);
                if(t3 != null)
                    vector1.insertElementAt(t3, 0);
                u1 = new u(vector, vector1, abyte0);
            }
            catch(Exception exception)
            {
                _fldlong = false;
                _fldint = "SIP Request URI is not valid";
                SipAddress sipaddress1 = new SipAddress((String)vector.elementAt(1));
                u1 = new u(vector, vector1, abyte0);
            }
            return u1;
        } else
        {
            throw new SipException("Not Request and not Response: Request URI or SIP version is illegal ", (byte)0);
        }
    }

    private boolean a(u u1, String s)
    {
        if(!s.equals("SIP/2.0"))
        {
            _fldlong = false;
            _fldint = "SIP Version should be SIP/2.0 and not " + s;
            return false;
        }
        String s1 = u1.a("CSeq");
        if(s1.indexOf(u1._mthchar()) < 0)
        {
            _fldlong = false;
            _fldint = "Request Method and Cseq Method should match";
        }
        return true;
    }

    private int _mthif(int i, int k)
    {
        if(k >= _fldcase.length)
            _fldcase = a();
        _fldcase[k++] = (byte)i;
        return k;
    }

    private byte[] a()
    {
        byte abyte0[] = new byte[_fldcase.length * 2];
        System.arraycopy(_fldcase, 0, abyte0, 0, _fldcase.length);
        return abyte0;
    }

    private int a(Vector vector, int i)
    {
        byte abyte0[] = new byte[i];
        System.arraycopy(_fldcase, 0, abyte0, 0, i);
        String s = new String(abyte0);
        vector.addElement(s);
        _fldcase = new byte[120];
        return 0;
    }

    private int a(int i)
    {
        byte abyte0[] = new byte[i];
        System.arraycopy(_fldcase, 0, abyte0, 0, i);
        f = new String(abyte0);
        f = f.trim();
        _fldcase = new byte[120];
        return 0;
    }

    private int _mthif(Vector vector, int i)
    {
        byte abyte0[] = new byte[i];
        System.arraycopy(_fldcase, 0, abyte0, 0, i);
        String s = (new String(abyte0)).trim();
        a(vector, f, s);
        _fldcase = new byte[120];
        return 0;
    }

    private void a(Vector vector, String s, String s1)
    {
        try
        {
            vector.addElement(new t(s, s1));
        }
        catch(Exception exception)
        {
            _fldlong = false;
            _fldint = exception.getMessage();
        }
    }

    public int a(int i, int k)
    {
        k -= 48;
        i -= 48;
        if(9 < k || k < 0)
            return -1;
        if(9 < i || i < 0)
            return -1;
        else
            return k + i * 16;
    }

    private t _mthif(Vector vector, String s)
    {
        Enumeration enumeration = vector.elements();
        Object obj = null;
        while(enumeration.hasMoreElements()) 
        {
            t t1 = (t)enumeration.nextElement();
            if(t1._mthfor().equals(s) || t1._mthfor().equals(j.a(s)))
                return t1;
        }
        return null;
    }

    private void a(Vector vector, t t1)
    {
        if(t1 != null)
            vector.removeElement(t1);
    }

    private t _mthdo(Vector vector, String s)
    {
        t t1 = _mthif(vector, s);
        a(vector, t1);
        return t1;
    }

    private Vector a(Vector vector, String s)
    {
        boolean flag = true;
        Vector vector1 = new Vector();
        Object obj = null;
        while(flag) 
        {
            t t1 = _mthif(vector, s);
            if(t1 != null)
            {
                vector1.addElement(t1);
                a(vector, t1);
            } else
            {
                flag = false;
            }
        }
        return vector1;
    }

    private t _mthfor(Vector vector, String s)
    {
        Vector vector1 = a(vector, s);
        if(vector1.isEmpty())
        {
            _fldlong = false;
            _fldint = "Header " + s + "in missing";
            return null;
        }
        if(vector1.size() > 1)
        {
            _fldlong = false;
            _fldint = "More than 1 header " + s + "in the message";
        }
        return (t)vector1.elementAt(0);
    }

    public static final int _fldbyte = 0;
    public static final int e = 1;
    public static final int d = 2;
    public static final int c = 3;
    public static final int _fldchar = 4;
    public static final int _fldelse = 5;
    public static final int _fldgoto = 6;
    public static final int h = 7;
    public static final int g = 8;
    public static final int _fldnew = 9;
    public static final int b = 10;
    public static final int _fldfor = 11;
    public static final int _fldvoid = 12;
    public static final int a = 13;
    public static final int _fldif = 13;
    public static final int _fldtry = 10;
    public static final int _fldnull = 92;
    private byte _fldcase[];
    private byte _flddo;
    private String f;
    private boolean _fldlong;
    private String _fldint;
}
