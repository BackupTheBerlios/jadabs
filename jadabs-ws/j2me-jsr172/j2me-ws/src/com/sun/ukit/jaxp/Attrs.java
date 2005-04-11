// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Attrs.java

package com.sun.ukit.jaxp;

import org.xml.sax.Attributes;

class Attrs
    implements Attributes
{

    Attrs()
    {
        mItems = new String[64];
    }

    void setLength(char length)
    {
        if(length > (char)(mItems.length >> 3))
            mItems = new String[length << 3];
        mLength = length;
    }

    public int getLength()
    {
        return mLength;
    }

    public String getURI(int index)
    {
        return index < 0 || index >= mLength ? null : mItems[index << 3];
    }

    public String getLocalName(int index)
    {
        return index < 0 || index >= mLength ? null : mItems[(index << 3) + 2];
    }

    public String getQName(int index)
    {
        if(index < 0 || index >= mLength)
            return null;
        else
            return mItems[(index << 3) + 1];
    }

    public String getType(int index)
    {
        return index >= mItems.length >> 3 ? null : mItems[(index << 3) + 4];
    }

    public String getValue(int index)
    {
        return index < 0 || index >= mLength ? null : mItems[(index << 3) + 3];
    }

    public int getIndex(String uri, String localName)
    {
        char len = mLength;
        for(char idx = '\0'; idx < len; idx++)
            if(mItems[idx << 3].equals(uri) && mItems[(idx << 3) + 2].equals(localName))
                return idx;

        return -1;
    }

    public int getIndex(String qName)
    {
        char len = mLength;
        for(char idx = '\0'; idx < len; idx++)
            if(getQName(idx).equals(qName))
                return idx;

        return -1;
    }

    public String getType(String uri, String localName)
    {
        int idx = getIndex(uri, localName);
        return idx < 0 ? null : mItems[(idx << 3) + 4];
    }

    public String getType(String qName)
    {
        int idx = getIndex(qName);
        return idx < 0 ? null : mItems[(idx << 3) + 4];
    }

    public String getValue(String uri, String localName)
    {
        int idx = getIndex(uri, localName);
        return idx < 0 ? null : mItems[(idx << 3) + 3];
    }

    public String getValue(String qName)
    {
        int idx = getIndex(qName);
        return idx < 0 ? null : mItems[(idx << 3) + 3];
    }

    String mItems[];
    private char mLength;
}
