// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SAXParser.java

package javax.xml.parsers;

import java.io.IOException;
import java.io.InputStream;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public abstract class SAXParser
{

    protected SAXParser()
    {
    }

    public abstract void parse(InputStream inputstream, DefaultHandler defaulthandler)
        throws SAXException, IOException;

    public abstract void parse(InputSource inputsource, DefaultHandler defaulthandler)
        throws SAXException, IOException;

    public abstract boolean isNamespaceAware();

    public abstract boolean isValidating();
}
