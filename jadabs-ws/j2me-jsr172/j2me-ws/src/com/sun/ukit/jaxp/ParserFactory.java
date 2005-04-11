// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ParserFactory.java

package com.sun.ukit.jaxp;

import javax.xml.parsers.*;
import org.xml.sax.*;

// Referenced classes of package com.sun.ukit.jaxp:
//            Parser

public class ParserFactory extends SAXParserFactory
{

    public ParserFactory()
    {
        namespaces = false;
        prefixes = true;
    }

    public SAXParser newSAXParser()
        throws ParserConfigurationException, SAXException
    {
        if(namespaces && !prefixes)
            return new Parser(true);
        if(!namespaces && prefixes)
            return new Parser(false);
        else
            throw new ParserConfigurationException("");
    }

    public void setNamespaceAware(boolean awareness)
    {
        super.setNamespaceAware(awareness);
        if(awareness)
        {
            namespaces = true;
            prefixes = false;
        } else
        {
            namespaces = false;
            prefixes = true;
        }
    }

    public void setFeature(String name, boolean value)
        throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException
    {
        if("http://xml.org/sax/features/namespaces".equals(name))
            namespaces = value;
        else
        if("http://xml.org/sax/features/namespace-prefixes".equals(name))
            prefixes = value;
        else
            throw new SAXNotRecognizedException(name);
    }

    public boolean getFeature(String name)
        throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException
    {
        if("http://xml.org/sax/features/namespaces".equals(name))
            return namespaces;
        if("http://xml.org/sax/features/namespace-prefixes".equals(name))
            return prefixes;
        else
            throw new SAXNotRecognizedException(name);
    }

    public static final String FEATURE_NS = "http://xml.org/sax/features/namespaces";
    public static final String FEATURE_PREF = "http://xml.org/sax/features/namespace-prefixes";
    private boolean namespaces;
    private boolean prefixes;

    static 
    {
        FEATURE_NS = "http://xml.org/sax/features/namespaces";
        FEATURE_PREF = "http://xml.org/sax/features/namespace-prefixes";
    }
}
