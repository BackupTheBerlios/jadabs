// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Pair.java

package com.sun.ukit.jaxp;


class Pair
{

    Pair()
    {
    }

    public String qname()
    {
        return new String(chars, 1, chars.length - 1);
    }

    public String local()
    {
        if(chars[0] != 0)
            return new String(chars, chars[0] + 1, chars.length - chars[0] - 1);
        else
            return new String(chars, 1, chars.length - 1);
    }

    public String pref()
    {
        if(chars[0] != 0)
            return new String(chars, 1, chars[0] - 1);
        else
            return "";
    }

    public boolean eqpref(char qname[])
    {
        if(chars[0] == qname[0])
        {
            char len = chars[0];
            for(char i = '\001'; i < len; i++)
                if(chars[i] != qname[i])
                    return false;

            return true;
        } else
        {
            return false;
        }
    }

    public boolean eqname(char qname[])
    {
        char len = (char)chars.length;
        if(len == qname.length)
        {
            for(char i = '\0'; i < len; i++)
                if(chars[i] != qname[i])
                    return false;

            return true;
        } else
        {
            return false;
        }
    }

    public String name;
    public String value;
    public char chars[];
    public char id;
    public Pair list;
    public Pair next;
}
