// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import java.util.Vector;
import javax.microedition.sip.SipException;

// Referenced classes of package com.nokia.phone.ri.sip:
//            o

public class e
{

    public e()
    {
    }

    public static synchronized Vector a(String s)
        throws SipException
    {
        Vector vector = new Vector(10);
        StringBuffer stringbuffer = new StringBuffer();
        o o1 = null;
        boolean flag = false;
        boolean flag1 = false;
        for(short word0 = 0; word0 < s.length(); word0++)
        {
            char c = s.charAt(word0);
            if(c >= 'A' && c <= 'Z' || c >= '0' && c <= '9' || c >= 'a' && c <= 'z')
            {
                if(flag)
                {
                    stringbuffer.append(c);
                    continue;
                }
                if(flag1)
                {
                    if(c >= '0' && c <= '9' || c >= 'A' && c <= 'F')
                        stringbuffer.append(c);
                    else
                        throw new SipException("Illegal IPv6 address '" + stringbuffer + c + "'", (byte)0);
                    continue;
                }
                if(o1 != null && (o1._fldcase == 1 || o1._fldcase == 5 || o1._fldcase == 3))
                {
                    stringbuffer.append(c);
                } else
                {
                    o1 = new o((byte)1);
                    stringbuffer.append(c);
                }
                continue;
            }
            switch(c)
            {
            case 9: // '\t'
            case 32: // ' '
                if(flag)
                {
                    stringbuffer.append(c);
                    break;
                }
                if(o1 != null)
                {
                    a(vector, o1, stringbuffer);
                    o1 = null;
                }
                break;

            case 33: // '!'
            case 37: // '%'
            case 39: // '\''
            case 42: // '*'
            case 43: // '+'
            case 45: // '-'
            case 46: // '.'
            case 95: // '_'
            case 126: // '~'
                if(flag)
                {
                    stringbuffer.append(c);
                    break;
                }
                stringbuffer.append(c);
                if(o1 != null && o1._fldcase == 1)
                {
                    o1._fldcase = 5;
                    break;
                }
                if(o1 == null)
                    o1 = new o((byte)5);
                break;

            case 96: // '`'
                if(flag)
                {
                    stringbuffer.append(c);
                    break;
                }
                stringbuffer.append(c);
                if(o1 != null)
                {
                    o1._fldcase = 5;
                    break;
                }
                if(o1 == null)
                    o1 = new o((byte)5);
                break;

            case 40: // '('
            case 41: // ')'
                if(flag)
                {
                    stringbuffer.append(c);
                    break;
                }
                stringbuffer.append(c);
                if(o1 != null)
                {
                    o1._fldcase = 3;
                    break;
                }
                if(o1 == null)
                    o1 = new o((byte)3);
                break;

            case 36: // '$'
            case 38: // '&'
            case 44: // ','
            case 47: // '/'
            case 58: // ':'
            case 59: // ';'
            case 60: // '<'
            case 61: // '='
            case 62: // '>'
            case 63: // '?'
            case 64: // '@'
                if(flag)
                {
                    stringbuffer.append(c);
                    break;
                }
                if(flag1 && c == ':')
                {
                    stringbuffer.append(c);
                    break;
                }
                if(o1 != null)
                    a(vector, o1, stringbuffer);
                stringbuffer.append(c);
                o1 = new o((byte)2);
                a(vector, o1, stringbuffer);
                o1 = null;
                break;

            case 91: // '['
                if(flag)
                {
                    stringbuffer.append(c);
                    break;
                }
                flag1 = true;
                if(o1 != null)
                    a(vector, o1, stringbuffer);
                stringbuffer.append(c);
                o1 = new o((byte)6);
                break;

            case 93: // ']'
                if(flag)
                {
                    stringbuffer.append(c);
                    break;
                }
                if(flag1)
                {
                    flag1 = false;
                    stringbuffer.append(c);
                    a(vector, o1, stringbuffer);
                    o1 = null;
                    break;
                }
                if(o1 != null)
                    a(vector, o1, stringbuffer);
                stringbuffer.append(c);
                o1 = new o((byte)2);
                a(vector, o1, stringbuffer);
                o1 = null;
                break;

            case 34: // '"'
                if(flag)
                {
                    flag = false;
                    a(vector, o1, stringbuffer);
                    o1 = null;
                    break;
                }
                flag = true;
                if(o1 != null)
                    a(vector, o1, stringbuffer);
                o1 = new o((byte)7);
                break;

            case 10: // '\n'
            case 11: // '\013'
            case 12: // '\f'
            case 13: // '\r'
            case 14: // '\016'
            case 15: // '\017'
            case 16: // '\020'
            case 17: // '\021'
            case 18: // '\022'
            case 19: // '\023'
            case 20: // '\024'
            case 21: // '\025'
            case 22: // '\026'
            case 23: // '\027'
            case 24: // '\030'
            case 25: // '\031'
            case 26: // '\032'
            case 27: // '\033'
            case 28: // '\034'
            case 29: // '\035'
            case 30: // '\036'
            case 31: // '\037'
            case 35: // '#'
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
            case 65: // 'A'
            case 66: // 'B'
            case 67: // 'C'
            case 68: // 'D'
            case 69: // 'E'
            case 70: // 'F'
            case 71: // 'G'
            case 72: // 'H'
            case 73: // 'I'
            case 74: // 'J'
            case 75: // 'K'
            case 76: // 'L'
            case 77: // 'M'
            case 78: // 'N'
            case 79: // 'O'
            case 80: // 'P'
            case 81: // 'Q'
            case 82: // 'R'
            case 83: // 'S'
            case 84: // 'T'
            case 85: // 'U'
            case 86: // 'V'
            case 87: // 'W'
            case 88: // 'X'
            case 89: // 'Y'
            case 90: // 'Z'
            case 92: // '\\'
            case 94: // '^'
            case 97: // 'a'
            case 98: // 'b'
            case 99: // 'c'
            case 100: // 'd'
            case 101: // 'e'
            case 102: // 'f'
            case 103: // 'g'
            case 104: // 'h'
            case 105: // 'i'
            case 106: // 'j'
            case 107: // 'k'
            case 108: // 'l'
            case 109: // 'm'
            case 110: // 'n'
            case 111: // 'o'
            case 112: // 'p'
            case 113: // 'q'
            case 114: // 'r'
            case 115: // 's'
            case 116: // 't'
            case 117: // 'u'
            case 118: // 'v'
            case 119: // 'w'
            case 120: // 'x'
            case 121: // 'y'
            case 122: // 'z'
            case 123: // '{'
            case 124: // '|'
            case 125: // '}'
            default:
                throw new SipException("Illegal character '" + c + "'", (byte)0);
            }
        }

        if(flag1)
            throw new SipException("Unclosed IPv6 address '" + o1._fldfor + "'", (byte)0);
        if(o1 != null)
            a(vector, o1, stringbuffer);
        o1 = new o((byte)-1);
        vector.addElement(o1);
        return vector;
    }

    private static void a(Vector vector, o o1, StringBuffer stringbuffer)
    {
        o1._fldfor = stringbuffer.toString().trim();
        if(o1._fldcase == 2)
            o1._fldint = o1._fldfor.charAt(0);
        stringbuffer.setLength(0);
        vector.addElement(o1);
    }
}
