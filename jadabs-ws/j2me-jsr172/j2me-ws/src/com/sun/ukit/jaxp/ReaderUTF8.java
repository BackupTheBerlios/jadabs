// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ReaderUTF8.java

package com.sun.ukit.jaxp;

import java.io.*;

public class ReaderUTF8 extends Reader
{

    public ReaderUTF8(InputStream is)
    {
        this.is = is;
    }

    public int read(char cbuf[], int off, int len)
        throws IOException
    {
        int num;
        for(num = 0; num < len; num++)
        {
            int val;
            if((val = is.read()) < 0)
                return num == 0 ? -1 : num;
            switch(val & 0xf0)
            {
            case 192: 
            case 208: 
                cbuf[off++] = (char)((val & 0x1f) << 6 | is.read() & 0x3f);
                break;

            case 224: 
                cbuf[off++] = (char)((val & 0xf) << 12 | (is.read() & 0x3f) << 6 | is.read() & 0x3f);
                break;

            case 240: 
                throw new UnsupportedEncodingException();

            default:
                cbuf[off++] = (char)val;
                break;
            }
        }

        return num;
    }

    public int read()
        throws IOException
    {
        int val;
        if((val = is.read()) < 0)
            return -1;
        switch(val & 0xf0)
        {
        case 192: 
        case 208: 
            val = (val & 0x1f) << 6 | is.read() & 0x3f;
            break;

        case 224: 
            val = (val & 0xf) << 12 | (is.read() & 0x3f) << 6 | is.read() & 0x3f;
            break;

        case 240: 
            throw new UnsupportedEncodingException();
        }
        return val;
    }

    public void close()
        throws IOException
    {
        is.close();
    }

    private InputStream is;
}
