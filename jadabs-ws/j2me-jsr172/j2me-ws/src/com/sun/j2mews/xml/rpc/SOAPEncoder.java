// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SOAPEncoder.java

package com.sun.j2mews.xml.rpc;

import java.io.OutputStream;
import javax.microedition.xml.rpc.*;
import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;

public class SOAPEncoder
{

    public SOAPEncoder()
    {
    }

    public synchronized void encode(Object value, Type type, OutputStream stream, String encoding)
        throws JAXRPCException
    {
        buffer = new StringBuffer();
        errString = null;
        buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        buffer.append("<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n\txmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n\txmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"");
        try
        {
            if(type != null)
            {
                Element op = (Element)type;
                defaultNS = op.name.getNamespaceURI();
                buffer.append("\n\txmlns:tns=\"" + defaultNS + "\">\n");
                buffer.append("<soap:Body>\n");
                encode((Element)type, value);
            } else
            {
                buffer.append(">\n<soap:Body/>\n");
            }
        }
        catch(RuntimeException re) { }
        catch(Throwable t)
        {
            throw new JAXRPCException("Could not encode request");
        }
        if(errString != null)
            throw new JAXRPCException(errString);
        if(type != null)
            buffer.append("</soap:Body>\n");
        buffer.append("</soap:Envelope>\n");
        try
        {
            if(encoding != null)
                stream.write(buffer.toString().getBytes(encoding));
            else
                stream.write(buffer.toString().getBytes());
        }
        catch(Exception e)
        {
            buffer = null;
            throw new JAXRPCException(e.getMessage());
        }
        buffer = null;
    }

    private void encode(Element parent, Object value)
    {
        String id;
        if(parent.name.getNamespaceURI().equals(defaultNS))
            id = "tns:" + parent.name.getLocalPart();
        else
            id = parent.name.getLocalPart() + " xmlns=\"" + parent.name.getNamespaceURI() + "\"";
        if(value == null)
        {
            if(parent.isNillable && !parent.isArray)
                buffer.append("<" + id + " xsi:nil=\"true\"/>\n");
            else
            if(!parent.isOptional)
            {
                errString = "Null value for non-nillable/optional element: " + parent.name.getLocalPart();
                throw new RuntimeException();
            }
            return;
        }
        int count = 1;
        Object values[] = {
            value
        };
        if(parent.isArray)
        {
            if(value instanceof Object[])
            {
                count = ((Object[])value).length;
                values = (Object[])value;
            } else
            if(isPrimitiveArray(parent.contentType, value))
            {
                encodePrimitiveArray(id, parent, value);
                return;
            } else
            {
                errString = "Type mismatch: elements of an array must be an array.";
                throw new RuntimeException();
            }
            checkArraySize(parent, values.length);
        }
        for(int i = 0; i < count; i++)
            if(values[i] == null)
            {
                if(!parent.isNillable)
                {
                    errString = "Null value for non-nillable/optional element: " + parent.name.getLocalPart();
                    throw new RuntimeException();
                }
                buffer.append("<" + id + " xsi:nil=\"true\"/>\n");
            } else
            {
                buffer.append("<" + id + ">");
                if(parent.contentType.value < 8)
                    encodeSimpleType(parent.contentType, values[i]);
                else
                if(parent.contentType.value == 8)
                {
                    if(values[i] instanceof Object[])
                    {
                        buffer.append("\n");
                        encodeComplexType((ComplexType)parent.contentType, (Object[])values[i]);
                    } else
                    {
                        errString = "Type mismatch: element of ComplexType must be an array.";
                        throw new RuntimeException();
                    }
                } else
                if(parent.contentType.value == 9)
                {
                    errString = "Encoding error - unable to encode indirected Elements of Elements";
                    throw new RuntimeException();
                }
                if(parent.name.getNamespaceURI().equals(defaultNS))
                    buffer.append("</" + id + ">\n");
                else
                    buffer.append("</" + parent.name.getLocalPart() + ">\n");
            }

    }

    private void encodeSimpleType(Type contentType, Object value)
    {
        if(!checkSimpleType(contentType, value))
        {
            errString = "Simple Type Mismatch";
            throw new RuntimeException();
        }
        if(contentType.value < 7)
            buffer.append(value.toString());
        else
            xmlIzeString(value.toString());
    }

    private void encodeComplexType(ComplexType type, Object values[])
    {
        Element elements[] = type.elements;
        if(elements.length != values.length)
        {
            errString = "Wrong number of values passed for complex type";
            throw new RuntimeException();
        }
        for(int i = 0; i < elements.length; i++)
            encode(elements[i], values[i]);

    }

    private void encodePrimitiveArray(String id, Element parent, Object value)
    {
        switch(parent.contentType.value)
        {
        case 5: // '\005'
        case 6: // '\006'
        default:
            break;

        case 0: // '\0'
        {
            if(value instanceof boolean[])
            {
                boolean values[] = (boolean[])value;
                checkArraySize(parent, values.length);
                for(int i = 0; i < values.length; i++)
                    buffer.append("<" + id + ">" + values[i] + "</" + id + ">\n");

                return;
            }
            break;
        }

        case 1: // '\001'
        {
            if(!(value instanceof byte[]))
                break;
            byte values[] = (byte[])value;
            checkArraySize(parent, values.length);
            for(int i = 0; i < values.length; i++)
                buffer.append("<" + id + ">" + values[i] + "</" + id + ">\n");

            return;
        }

        case 2: // '\002'
        {
            if(!(value instanceof short[]))
                break;
            short values[] = (short[])value;
            checkArraySize(parent, values.length);
            for(int i = 0; i < values.length; i++)
                buffer.append("<" + id + ">" + values[i] + "</" + id + ">\n");

            return;
        }

        case 3: // '\003'
        {
            if(!(value instanceof int[]))
                break;
            int values[] = (int[])value;
            checkArraySize(parent, values.length);
            for(int i = 0; i < values.length; i++)
                buffer.append("<" + id + ">" + values[i] + "</" + id + ">\n");

            return;
        }

        case 4: // '\004'
        {
            if(!(value instanceof long[]))
                break;
            long values[] = (long[])value;
            checkArraySize(parent, values.length);
            for(int i = 0; i < values.length; i++)
                buffer.append("<" + id + ">" + values[i] + "</" + id + ">\n");

            return;
        }
        }
        errString = "Invalid values for primitive array for " + parent.name.getLocalPart();
        throw new RuntimeException();
    }

    private void checkArraySize(Element parent, int len)
    {
        if(len < parent.minOccurs)
        {
            errString = "Not enough array elements for: " + parent.name.getLocalPart();
            throw new RuntimeException();
        }
        if(parent.maxOccurs > 0 && len > parent.maxOccurs)
        {
            errString = "Too many array elements for: " + parent.name.getLocalPart();
            throw new RuntimeException();
        } else
        {
            return;
        }
    }

    private void xmlIzeString(String input)
    {
        char chars[] = input.toCharArray();
        for(int i = 0; i < chars.length; i++)
            if(chars[i] == '<')
                buffer.append("&lt;");
            else
            if(chars[i] == '>')
                buffer.append("&gt;");
            else
            if(chars[i] == '&')
                buffer.append("&amp;");
            else
            if(chars[i] == '\'')
                buffer.append("@apos;");
            else
            if(chars[i] == '"')
                buffer.append("&quot;");
            else
                buffer.append(chars[i]);

    }

    private boolean checkSimpleType(Type contentType, Object value)
    {
        switch(contentType.value)
        {
        case 0: // '\0'
            return value instanceof Boolean;

        case 1: // '\001'
            return value instanceof Byte;

        case 2: // '\002'
            return value instanceof Short;

        case 3: // '\003'
            return value instanceof Integer;

        case 4: // '\004'
            return value instanceof Long;

        case 7: // '\007'
            return value instanceof String;

        case 5: // '\005'
        case 6: // '\006'
        default:
            return false;
        }
    }

    private boolean isPrimitiveArray(Type contentType, Object value)
    {
        switch(contentType.value)
        {
        case 0: // '\0'
            return value instanceof boolean[];

        case 1: // '\001'
            return value instanceof byte[];

        case 2: // '\002'
            return value instanceof short[];

        case 3: // '\003'
            return value instanceof int[];

        case 4: // '\004'
            return value instanceof long[];
        }
        return false;
    }

    StringBuffer buffer;
    String defaultNS;
    String errString;
}
