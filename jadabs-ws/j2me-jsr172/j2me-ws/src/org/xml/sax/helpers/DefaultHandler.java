// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DefaultHandler.java

package org.xml.sax.helpers;

import org.xml.sax.*;

public class DefaultHandler
{

    public DefaultHandler()
    {
    }

    public InputSource resolveEntity(String publicId, String systemId)
        throws SAXException
    {
        return null;
    }

    public void notationDecl(String s, String s1, String s2)
        throws SAXException
    {
    }

    public void unparsedEntityDecl(String s, String s1, String s2, String s3)
        throws SAXException
    {
    }

    public void setDocumentLocator(Locator locator1)
    {
    }

    public void startDocument()
        throws SAXException
    {
    }

    public void endDocument()
        throws SAXException
    {
    }

    public void startPrefixMapping(String s, String s1)
        throws SAXException
    {
    }

    public void endPrefixMapping(String s)
        throws SAXException
    {
    }

    public void startElement(String s, String s1, String s2, Attributes attributes1)
        throws SAXException
    {
    }

    public void endElement(String s, String s1, String s2)
        throws SAXException
    {
    }

    public void characters(char ac[], int i, int j)
        throws SAXException
    {
    }

    public void ignorableWhitespace(char ac[], int i, int j)
        throws SAXException
    {
    }

    public void processingInstruction(String s, String s1)
        throws SAXException
    {
    }

    public void skippedEntity(String s)
        throws SAXException
    {
    }

    public void warning(SAXParseException saxparseexception)
        throws SAXException
    {
    }

    public void error(SAXParseException saxparseexception)
        throws SAXException
    {
    }

    public void fatalError(SAXParseException e)
        throws SAXException
    {
        throw e;
    }
}
