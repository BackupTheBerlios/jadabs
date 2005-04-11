// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ReaderUTF16.java

package com.sun.ukit.jaxp;

import java.io.*;

public class ReaderUTF16 extends Reader
{

    public ReaderUTF16(InputStream is, char bo)
    {
        switch(bo)
        {
        default:
            throw new IllegalArgumentException("");

        case 98: // 'b'
        case 108: // 'l'
            this.bo = bo;
            break;
        }
        this.is = is;
    }

    public int read(char cbuf[], int off, int len)
        throws IOException
    {
        int num = 0;
        if(bo == 'b')
            for(; num < len; num++)
            {
                int val;
                if((val = is.read()) < 0)
                    return num == 0 ? -1 : num;
                cbuf[off++] = (char)(val << 8 | is.read() & 0xff);
            }

        else
            for(; num < len; num++)
            {
                int val;
                if((val = is.read()) < 0)
                    return num == 0 ? -1 : num;
                cbuf[off++] = (char)(is.read() << 8 | val & 0xff);
            }

        return num;
    }

    public int read()
        throws IOException
    {
        int val;
        if((val = is.read()) < 0)
            return -1;
        if(bo == 'b')
            val = (char)(val << 8 | is.read() & 0xff);
        else
            val = (char)(is.read() << 8 | val & 0xff);
        return val;
    }

    public void close()
        throws IOException
    {
        is.close();
    }

    private InputStream is;
    private char bo;
}
