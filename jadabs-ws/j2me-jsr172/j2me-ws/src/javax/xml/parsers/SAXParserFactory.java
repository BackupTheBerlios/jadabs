// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SAXParserFactory.java

package javax.xml.parsers;

import com.sun.ukit.jaxp.ParserFactory;
import org.xml.sax.*;

// Referenced classes of package javax.xml.parsers:
//            FactoryConfigurationError, ParserConfigurationException, SAXParser

public abstract class SAXParserFactory
{

    protected SAXParserFactory()
    {
        namespaceAware = false;
        validating = false;
    }

    public static SAXParserFactory newInstance()
        throws FactoryConfigurationError
    {
        return new ParserFactory();
    }

    public abstract SAXParser newSAXParser()
        throws ParserConfigurationException, SAXException;

    public void setNamespaceAware(boolean awareness)
    {
        namespaceAware = awareness;
    }

    public boolean isNamespaceAware()
    {
        return namespaceAware;
    }

    public void setValidating(boolean validating)
    {
        validating = false;
    }

    public boolean isValidating()
    {
        return validating;
    }

    public abstract void setFeature(String s, boolean flag)
        throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException;

    public abstract boolean getFeature(String s)
        throws ParserConfigurationException, SAXNotRecognizedException, SAXNotSupportedException;

    private boolean namespaceAware;
    private boolean validating;
}
