// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SAXParseException.java

package org.xml.sax;


// Referenced classes of package org.xml.sax:
//            SAXException, Locator

public class SAXParseException extends SAXException
{

    public SAXParseException(String message, Locator locator)
    {
        super(message);
        if(locator != null)
        {
            publicId = locator.getPublicId();
            systemId = locator.getSystemId();
            lineNumber = locator.getLineNumber();
            columnNumber = locator.getColumnNumber();
        } else
        {
            lineNumber = -1;
            columnNumber = -1;
        }
    }

    public String getPublicId()
    {
        return publicId;
    }

    public String getSystemId()
    {
        return systemId;
    }

    public int getLineNumber()
    {
        return lineNumber;
    }

    public int getColumnNumber()
    {
        return columnNumber;
    }

    private String publicId;
    private String systemId;
    private int lineNumber;
    private int columnNumber;
}
