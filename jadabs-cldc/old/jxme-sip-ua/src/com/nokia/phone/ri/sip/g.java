// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.sip.SipException;

// Referenced classes of package com.nokia.phone.ri.sip:
//            l, o, t, e

public class g
{

    public g()
    {
    }

    public static synchronized l a(String s, short word0)
        throws SipException
    {
        l l1 = new l();
        Vector vector = e.a(s.trim());
        short word1 = 0;
        boolean flag = false;
        boolean flag1 = false;
        boolean flag2 = false;
        StringBuffer stringbuffer = new StringBuffer();
        Object obj = null;
        Object obj1 = null;
        short word2 = word0;
        for(short word3 = 0; word3 < vector.size(); word3++)
        {
            o o1 = (o)vector.elementAt(word3);
label0:
            switch(word2)
            {
            case 7: // '\007'
            case 8: // '\b'
            default:
                break;

            case 0: // '\0'
                switch(o1._fldcase)
                {
                case 7: // '\007'
                    l1._fldtry = o1._fldfor;
                    break;

                case 1: // '\001'
                case 5: // '\005'
                    if(o1._fldfor.equals("*"))
                    {
                        l1._fldint = "*";
                        return l1;
                    }
                    o o2 = (o)vector.elementAt(word3 + 1);
                    if(o2._fldint == ':')
                    {
                        a(o1);
                        l1.a = o1._fldfor;
                        if(stringbuffer.length() > 0)
                        {
                            l1._fldtry = stringbuffer.toString();
                            stringbuffer.setLength(0);
                        }
                        word3++;
                        word1 = word3;
                        word2 = 1;
                    } else
                    {
                        if(stringbuffer.length() > 0)
                            stringbuffer.append(" ");
                        stringbuffer.append(o1._fldfor);
                    }
                    break;

                case 2: // '\002'
                    if(o1._fldint == '<')
                        flag1 = true;
                    else
                        a("Unknown scheme '" + stringbuffer + "'");
                    break;

                case 3: // '\003'
                case 4: // '\004'
                case 6: // '\006'
                default:
                    a("Unknown scheme '" + o1._fldfor + "'");
                    break;
                }
                break;

            case 1: // '\001'
                switch(o1._fldcase)
                {
                case 1: // '\001'
                case 3: // '\003'
                case 5: // '\005'
                    stringbuffer.append(o1._fldfor);
                    break label0;

                case 2: // '\002'
                    switch(o1._fldint)
                    {
                    case 64: // '@'
                        l1._fldnew = stringbuffer.toString();
                        stringbuffer.setLength(0);
                        if(l1._fldnew.length() == 0)
                            a("Empty user name");
                        word2 = 3;
                        break label0;

                    case 58: // ':'
                        word2 = 2;
                        break label0;

                    case 62: // '>'
                        flag2 = true;
                        if(flag)
                        {
                            word3 = word1;
                            word1 = 0;
                            word2 = 3;
                            break label0;
                        }
                        o o5 = (o)vector.elementAt(word3 - 1);
                        if(o5._fldcase == 1 || o5._fldcase == 5)
                            l1._fldcase = o5._fldfor;
                        else
                            a("Illegal host '" + o5._fldfor + "'");
                        stringbuffer.setLength(0);
                        word2 = 8;
                        break;

                    case 36: // '$'
                    case 38: // '&'
                    case 43: // '+'
                    case 44: // ','
                    case 47: // '/'
                    case 59: // ';'
                    case 61: // '='
                    case 63: // '?'
                        flag = true;
                        stringbuffer.append(o1._fldint);
                        break;

                    case 37: // '%'
                    case 39: // '\''
                    case 40: // '('
                    case 41: // ')'
                    case 42: // '*'
                    case 45: // '-'
                    case 46: // '.'
                    case 48: // '0'
                    case 49: // '1'
                    case 50: // '2'
                    case 51: // '3'
                    case 52: // '4'
                    case 53: // '5'
                    case 54: // '6'
                    case 55: // '7'
                    case 56: // '8'
                    case 57: // '9'
                    case 60: // '<'
                    default:
                        a("Illegal character in user info part '" + o1._fldint + "'");
                        break;
                    }
                    break label0;

                case 6: // '\006'
                    l1._fldcase = o1._fldfor;
                    word2 = 4;
                    break label0;

                case -1: 
                    if(flag)
                    {
                        word3 = word1;
                        word1 = 0;
                        word2 = 3;
                        break label0;
                    }
                    o o6 = (o)vector.elementAt(word3 - 1);
                    if(o6._fldcase == 1 || o6._fldcase == 5)
                        l1._fldcase = o6._fldfor;
                    else
                        a("Illegal host '" + o6._fldfor + "'");
                    stringbuffer.setLength(0);
                    word2 = 8;
                    break;

                case 0: // '\0'
                case 4: // '\004'
                default:
                    a("Invalid user name part '" + o1._fldfor + "'");
                    break;
                }
                break;

            case 2: // '\002'
                switch(o1._fldcase)
                {
                case 1: // '\001'
                case 3: // '\003'
                case 5: // '\005'
                    o o3 = (o)vector.elementAt(word3 + 1);
                    o o7 = (o)vector.elementAt(word3 - 1);
                    if(o3._fldint == '@')
                    {
                        if(o7._fldint == ':')
                        {
                            l1._fldif = o1._fldfor;
                            stringbuffer.append(":");
                            stringbuffer.append(l1._fldif);
                            l1._fldnew = stringbuffer.toString();
                            stringbuffer.setLength(0);
                            if(l1._fldnew.length() == 0)
                                a("Empty user name");
                        } else
                        {
                            a("Wrong user info part '" + o1._fldfor + "'");
                        }
                        word3++;
                        word2 = 3;
                        break label0;
                    }
                    o o8 = (o)vector.elementAt(word3 - 2);
                    if(o8._fldcase == 1 || o8._fldcase == 5)
                        l1._fldcase = o8._fldfor;
                    else
                        a("Illegal host '" + o8._fldfor + "'");
                    stringbuffer.setLength(0);
                    word3--;
                    word2 = 4;
                    break;

                case -1: 
                case 2: // '\002'
                    a("Is this useless case in AFTER_NAME_STATE");
                    break;

                case 0: // '\0'
                case 4: // '\004'
                default:
                    a("Problem in user info part '" + o1._fldfor + "'");
                    break;
                }
                break;

            case 3: // '\003'
                switch(o1._fldcase)
                {
                case 1: // '\001'
                case 5: // '\005'
                case 6: // '\006'
                    l1._fldcase = o1._fldfor;
                    word2 = 4;
                    break;

                default:
                    a("Host missing, or illegal '" + o1._fldfor + "'");
                    break;
                }
                break;

            case 4: // '\004'
                switch(o1._fldcase)
                {
                case 3: // '\003'
                case 4: // '\004'
                default:
                    break;

                case 5: // '\005'
                    a("Invalid port or address: " + o1._fldfor);
                    break label0;

                case 1: // '\001'
                    l1._fldfor = o1._fldfor;
                    try
                    {
                        Integer.parseInt(l1._fldfor);
                    }
                    catch(Exception exception)
                    {
                        a("Invalid port number; " + exception.getMessage());
                    }
                    break label0;

                case 2: // '\002'
                    if(o1._fldint == ':')
                        break label0;
                    if(o1._fldint == ';')
                    {
                        word2 = 5;
                        break label0;
                    }
                    if(o1._fldint == '>')
                    {
                        flag2 = true;
                        word2 = 8;
                        break label0;
                    }
                    if(o1._fldint == '?')
                        word2 = 7;
                    else
                        a("Invalid host:port '" + o1._fldint + "'");
                    break;
                }
                break;

            case 5: // '\005'
                switch(o1._fldcase)
                {
                case 4: // '\004'
                default:
                    break;

                case 1: // '\001'
                case 3: // '\003'
                case 5: // '\005'
                    o o4 = (o)vector.elementAt(word3 + 1);
                    if(o4._fldint == '=')
                    {
                        word2 = 6;
                        word3++;
                        break label0;
                    }
                    if(o4._fldint == '>')
                    {
                        flag2 = true;
                        a(o1._fldfor, "", l1._fldbyte);
                        break label0;
                    }
                    if(o4._fldint == ';')
                    {
                        a(o1._fldfor, "", l1._fldbyte);
                        break label0;
                    }
                    if(o4._fldcase == -1)
                    {
                        a(o1._fldfor, "", l1._fldbyte);
                        break label0;
                    }
                    if(o4._fldint == '?')
                    {
                        a(o1._fldfor, "", l1._fldbyte);
                        word2 = 7;
                    } else
                    {
                        a("Invalid parameter name '" + o1._fldfor + "'");
                    }
                    break label0;

                case 2: // '\002'
                    if(o1._fldint == ';')
                        break label0;
                    if(o1._fldint == '>')
                    {
                        flag2 = true;
                        word2 = 8;
                    } else
                    {
                        a("Invalid parameter '" + o1._fldint + "'");
                    }
                    break;
                }
                break;

            case 6: // '\006'
                if(o1._fldcase == 1 || o1._fldcase == 5 || o1._fldcase == 3)
                {
                    String s1 = ((o)vector.elementAt(word3 - 2))._fldfor;
                    a(s1, o1._fldfor, l1._fldbyte);
                    word2 = 5;
                } else
                {
                    a("Invalid parameter value '" + o1._fldfor + "'");
                }
                break;
            }
        }

        if(l1._fldcase != null)
            _mthif(l1._fldcase);
        if(flag1 && !flag2)
            a("Missing closing '>' quote");
        if(flag2 && !flag1)
            a("Unexpected right '>' quote");
        l1._flddo = a(l1.a, l1._fldnew, l1._fldcase, l1._fldfor);
        return l1;
    }

    public static void a(o o1)
        throws SipException
    {
        char c = o1._fldfor.charAt(0);
        if(c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')
        {
            if(o1._fldcase == 1)
                return;
            int i = 1;
            do
            {
                if(i >= o1._fldfor.length())
                    break;
                char c1 = o1._fldfor.charAt(i);
                switch(c1)
                {
                case 33: // '!'
                case 37: // '%'
                case 39: // '\''
                case 42: // '*'
                case 95: // '_'
                    a("Illegal characters in scheme '" + o1._fldfor + "'");
                    break;
                }
                i++;
            } while(true);
        } else
        {
            a("Illegal first character in scheme '" + o1._fldfor + "'");
        }
    }

    public static void _mthif(String s)
        throws SipException
    {
        if(s.charAt(0) == '[')
            return;
        int i = 0;
        int j = 0;
        for(int k = 0; k < s.length(); k++)
        {
            char c = s.charAt(k);
            if(c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c >= 'a' && c <= 'z')
                continue;
            if(c == '.')
            {
                if(i + 1 == k || k == 0)
                    a("Invalid host address or name '" + s + "'");
                i = k;
                if(++j > 3)
                    a("Invalid host address or name '" + s + "'");
            } else
            {
                a("Invalid host address or name '" + s + "'");
            }
        }

    }

    private static void a(String s)
        throws SipException
    {
        throw new SipException(s, (byte)0);
    }

    private static void a(String s, String s1, Vector vector)
        throws SipException
    {
        s = s.toLowerCase();
        for(Enumeration enumeration = vector.elements(); enumeration.hasMoreElements();)
        {
            t t1 = (t)enumeration.nextElement();
            if(s.equals(t1._mthfor()))
                throw new SipException("Duplicate parameter '" + s + "'", (byte)0);
        }

        vector.addElement(new t(s, s1));
    }

    public static synchronized String a(String s, String s1, String s2, String s3)
    {
        StringBuffer stringbuffer = new StringBuffer();
        if(s != null && s.length() > 0)
        {
            stringbuffer.append(s);
            stringbuffer.append(":");
        }
        if(s1 != null)
        {
            stringbuffer.append(s1);
            stringbuffer.append("@");
        }
        if(s2 != null)
            stringbuffer.append(s2);
        if(s3 != null && s3.length() > 0)
        {
            Integer.parseInt(s3);
            stringbuffer.append(":");
            stringbuffer.append(s3);
        }
        return stringbuffer.toString();
    }

    public static final short _fldfor = 0;
    public static final short _fldnew = 1;
    public static final short _fldcase = 2;
    public static final short _fldif = 3;
    public static final short _fldtry = 4;
    public static final short a = 5;
    public static final short _fldint = 6;
    public static final short _fldbyte = 7;
    public static final short _flddo = 8;
}
