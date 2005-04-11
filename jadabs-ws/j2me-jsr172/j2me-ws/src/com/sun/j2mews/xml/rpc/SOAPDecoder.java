// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SOAPDecoder.java

package com.sun.j2mews.xml.rpc;

import java.io.InputStream;
import java.rmi.MarshalException;
import java.util.Stack;
import javax.microedition.xml.rpc.*;
import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.rpc.JAXRPCException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

// Referenced classes of package com.sun.j2mews.xml.rpc:
//            TypedVector

public class SOAPDecoder extends DefaultHandler
{

    public SOAPDecoder()
    {
        try
        {
            SAXParserFactory pf = SAXParserFactory.newInstance();
            pf.setNamespaceAware(true);
            pf.setValidating(false);
            parser = pf.newSAXParser();
        }
        catch(Exception e)
        {
            throw new RuntimeException("Could not instantiate parser");
        }
        token = new StringBuffer();
        valueStack = new Stack();
        typeStack = new Stack();
        stateStack = new Stack();
    }

    public synchronized Object decode(Type type, InputStream stream, String encoding, long length)
        throws JAXRPCException
    {
        handler = null;
        handlerDetail = null;
        faultMode = false;
        detailName = null;
        isNill = false;
        bodyNEnvelope = 0;
        processingHeader = false;
        clearStacks();
        if(!(type instanceof Element))
            throw new JAXRPCException(new MarshalException("Type parameter not instanceof Element"));
        typeStack.push(type);
        try
        {
            parser.parse(stream, this);
        }
        catch(RuntimeException re) { }
        catch(SAXParseException spe)
        {
            stateStack.push(new Integer(4));
            errString = "SAXParseException in response from server";
        }
        catch(Throwable t)
        {
            stateStack.push(new Integer(4));
            errString = "Unexpected Exception : " + t.getMessage();
        }
        if(!stateStack.empty())
            state = ((Integer)stateStack.pop()).intValue();
        else
        if(bodyNEnvelope != 0)
        {
            state = 4;
            errString = "(1)Missing end tag for Body or Envelope";
        } else
        {
            state = 0;
        }
        if(state == 4)
            throw new JAXRPCException(new MarshalException(errString));
        if(valueStack.isEmpty())
        {
            Element e = (Element)type;
            if(e.isNillable || e.isOptional)
                return null;
            else
                throw new JAXRPCException(new MarshalException("Missing return data in response from server"));
        } else
        {
            return vectorToArray(valueStack.pop());
        }
    }

    public synchronized Object decodeFault(FaultDetailHandler handler, InputStream stream, String encoding, long length)
        throws JAXRPCException
    {
        this.handler = handler;
        handlerDetail = null;
        faultMode = true;
        detailName = null;
        isNill = false;
        bodyNEnvelope = 0;
        processingHeader = false;
        clearStacks();
        faultCT = new ComplexType();
        faultCT.elements = new Element[4];
        faultCT.elements[0] = new Element(new QName("", "faultcode"), Type.STRING);
        faultCT.elements[1] = new Element(new QName("", "faultstring"), Type.STRING);
        faultCT.elements[2] = new Element(new QName("", "faultactor"), Type.STRING, 0, 1, false);
        faultCT.elements[3] = new Element(new QName("", "detail"), Type.STRING, 0, 1, false);
        Element faultType = new Element(new QName("http://schemas.xmlsoap.org/soap/envelope/", "Fault"), faultCT);
        typeStack.push(faultType);
        try
        {
            parser.parse(stream, this);
        }
        catch(RuntimeException re) { }
        catch(SAXParseException spe)
        {
            stateStack.push(new Integer(4));
            errString = "SAXParseException in response from server";
        }
        catch(Throwable t)
        {
            t.printStackTrace();
        }
        if(!stateStack.empty())
            state = ((Integer)stateStack.pop()).intValue();
        else
        if(bodyNEnvelope != 0)
        {
            state = 4;
            errString = "(2)Missing end tag for Body or Envelope";
        } else
        {
            state = 0;
        }
        if(state == 4)
            throw new JAXRPCException(new MarshalException(errString));
        Object fault[] = (Object[])valueStack.pop();
        token.delete(0, token.length());
        if(fault[0] != null)
            token.append("\n[Code:   " + (String)fault[0] + "] ");
        if(fault[1] != null)
            token.append("\n[String: " + (String)fault[1] + "] ");
        if(fault[2] != null)
            token.append("\n[Actor:  " + (String)fault[2] + "] ");
        if(detailName == null)
        {
            if(fault[3] != null)
                token.append("\n[Detail: " + (String)fault[3] + "] ");
            return token.toString();
        } else
        {
            return ((Object) (new Object[] {
                token.toString(), detailName, vectorToArray(fault[3])
            }));
        }
    }

    private void clearStacks()
    {
        stateStack.removeAllElements();
        typeStack.removeAllElements();
        valueStack.removeAllElements();
    }

    private Object graph(ComplexType ct)
        throws JAXRPCException
    {
        try
        {
            Object o[] = new Object[ct.elements.length];
            for(int i = 0; i < ct.elements.length; i++)
                if(ct.elements[i].isArray)
                    o[i] = new TypedVector(ct.elements[i].contentType.value, ct.elements[i].isNillable);

            return ((Object) (o));
        }
        catch(Throwable t)
        {
            throw new JAXRPCException("Invalid Type for Output");
        }
    }

    private void startState(String uri, String name)
    {
        Type top = (Type)typeStack.peek();
        if(top instanceof Element)
        {
            Element e = (Element)top;
            validate(e, uri, name);
            if(e.contentType.value < 8)
                stateStack.push(new Integer(1));
            else
            if(e.contentType.value == 8)
            {
                typeStack.push(e.contentType);
                if(!isNill)
                    valueStack.push(graph((ComplexType)e.contentType));
                stateStack.push(new Integer(2));
            }
        } else
        if(top instanceof ComplexType)
        {
            ComplexType ct = (ComplexType)top;
            int index = -1;
            for(int i = 0; i < ct.elements.length; i++)
            {
                if(!matchType(ct.elements[i].name, uri, name))
                    continue;
                index = i;
                break;
            }

            if(index == -1)
            {
                if(faultMode && handler != null && detailName == null && ct == faultCT)
                {
                    detailName = new QName(uri, name);
                    Element e = handler.handleFault(detailName);
                    if(e != null)
                    {
                        handlerDetail = e;
                        if(handlerDetail.contentType.value == 8)
                        {
                            typeStack.push(handlerDetail.contentType);
                            if(!isNill)
                                valueStack.push(graph((ComplexType)handlerDetail.contentType));
                            stateStack.push(new Integer(2));
                        } else
                        {
                            typeStack.push(handlerDetail);
                            stateStack.push(new Integer(1));
                        }
                        return;
                    }
                }
                if(handlerDetail == null)
                {
                    stateStack.push(new Integer(4));
                    errString = "Invalid Element in Response: " + name;
                    throw new RuntimeException();
                }
            }
            validate(ct.elements[index], uri, name);
            if(ct.elements[index].contentType.value == 8)
            {
                typeStack.push(ct.elements[index].contentType);
                if(!isNill)
                    valueStack.push(graph((ComplexType)ct.elements[index].contentType));
                stateStack.push(new Integer(2));
            }
        }
    }

    private static boolean matchType(QName qname, String uri, String name)
    {
        return qname.getNamespaceURI().equals(uri) && qname.getLocalPart().equals(name);
    }

    private void validate(Element e, String uri, String name)
    {
        if(!name.equals(e.name.getLocalPart()))
        {
            errString = "Invalid Element Name From Server: " + name + ", " + "expected: " + e.name.getLocalPart();
            stateStack.push(new Integer(4));
            throw new RuntimeException();
        }
        if(!uri.equals(e.name.getNamespaceURI()))
        {
            errString = "Invalid Namespace URI From Server: " + uri + ", " + "expected: " + e.name.getNamespaceURI() + " for element: " + name;
            stateStack.push(new Integer(4));
            throw new RuntimeException();
        }
        if(isNill && !e.isNillable)
        {
            errString = "Nillable mismatch from server for: " + name;
            stateStack.push(new Integer(4));
            throw new RuntimeException();
        } else
        {
            return;
        }
    }

    private void endState(String uri, String name)
    {
        Type top = (Type)typeStack.pop();
        state = ((Integer)stateStack.peek()).intValue();
        switch(state)
        {
        case 1: // '\001'
            Element e = (Element)top;
            if(detailName != null && matchType(detailName, uri, name) && handlerDetail != null)
            {
                stateStack.pop();
                if(!isNill)
                {
                    Object fd = tokenToObject(e.contentType, token.toString());
                    Object f[] = (Object[])valueStack.peek();
                    f[3] = fd;
                }
                return;
            }
            validate(e, uri, name);
            if(!isNill)
                valueStack.push(tokenToObject(e.contentType, token.toString()));
            stateStack.pop();
            return;

        case 2: // '\002'
            ComplexType ct = (ComplexType)top;
            int index = -1;
            for(int i = 0; i < ct.elements.length; i++)
            {
                if(!matchType(ct.elements[i].name, uri, name))
                    continue;
                index = i;
                break;
            }

            Object ctVal = null;
            if(index == -1)
            {
                stateStack.pop();
                if(!stateStack.isEmpty())
                {
                    state = ((Integer)stateStack.peek()).intValue();
                    if(state != 2)
                    {
                        stateStack.push(new Integer(4));
                        errString = "(1):Mismatch between server response and type map";
                        throw new RuntimeException();
                    }
                    top = (Type)typeStack.pop();
                    ct = (ComplexType)top;
                    index = -1;
                    for(int i = 0; i < ct.elements.length; i++)
                    {
                        if(!matchType(ct.elements[i].name, uri, name))
                            continue;
                        index = i;
                        break;
                    }

                    if(index == -1)
                        if(detailName != null && matchType(detailName, uri, name) && handlerDetail != null)
                        {
                            Object fd = valueStack.pop();
                            Object f[] = (Object[])valueStack.peek();
                            f[3] = fd;
                            typeStack.push(top);
                            return;
                        } else
                        {
                            stateStack.push(new Integer(4));
                            errString = "(2):Mismatch between server response and type map";
                            throw new RuntimeException();
                        }
                    if(!isNill)
                        ctVal = valueStack.pop();
                } else
                {
                    top = (Type)typeStack.pop();
                    if((top instanceof Element) && matchType(((Element)top).name, uri, name))
                    {
                        return;
                    } else
                    {
                        stateStack.push(new Integer(4));
                        errString = "(3):Mismatch between server response and type map";
                        throw new RuntimeException();
                    }
                }
            }
            Object els[] = (Object[])valueStack.peek();
            validate(ct.elements[index], uri, name);
            if(faultCT != null && faultCT.elements == ct.elements && index == 3 && handlerDetail != null)
                ctVal = els[3];
            else
            if(ctVal == null && !isNill)
                ctVal = tokenToObject(ct.elements[index].contentType, token.toString());
            if(ct.elements[index].isArray)
            {
                TypedVector v = null;
                if(els[index] == null)
                {
                    v = new TypedVector(ct.elements[index].contentType.value, ct.elements[index].isNillable);
                    els[index] = v;
                } else
                {
                    v = (TypedVector)els[index];
                }
                if(ctVal == null)
                    v.addElement(NIL);
                else
                    v.addElement(ctVal);
            } else
            {
                els[index] = ctVal;
            }
            typeStack.push(top);
            return;
        }
    }

    private static Object vectorToArray(Object o)
    {
        if(o instanceof Object[])
        {
            Object set[] = (Object[])o;
            for(int i = 0; i < set.length; i++)
                if(set[i] instanceof TypedVector)
                {
                    TypedVector v = (TypedVector)set[i];
                    switch(v.type)
                    {
                    case 0: // '\0'
                    {
                        boolean tmp[];
                        if(v.nillable)
                        {
                            tmp = new Boolean[v.size()];
                            for(int j = 0; j < tmp.length; j++)
                            {
                                Object arrayEl = v.elementAt(j);
                                if(arrayEl != NIL)
                                    tmp[j] = (Boolean)arrayEl;
                            }

                            set[i] = tmp;
                            break;
                        }
                        tmp = new boolean[v.size()];
                        for(int k = 0; k < tmp.length; k++)
                            tmp[k] = ((Boolean)v.elementAt(k)).booleanValue();

                        set[i] = tmp;
                        break;
                    }

                    case 1: // '\001'
                    {
                        byte tmp[];
                        if(v.nillable)
                        {
                            tmp = new Byte[v.size()];
                            for(int j = 0; j < tmp.length; j++)
                            {
                                Object arrayEl = v.elementAt(j);
                                if(arrayEl != NIL)
                                    tmp[j] = (Byte)arrayEl;
                            }

                            set[i] = tmp;
                            break;
                        }
                        tmp = new byte[v.size()];
                        for(int k = 0; k < tmp.length; k++)
                            tmp[k] = ((Byte)v.elementAt(k)).byteValue();

                        set[i] = tmp;
                        break;
                    }

                    case 2: // '\002'
                    {
                        short tmp[];
                        if(v.nillable)
                        {
                            tmp = new Short[v.size()];
                            for(int j = 0; j < tmp.length; j++)
                            {
                                Object arrayEl = v.elementAt(j);
                                if(arrayEl != NIL)
                                    tmp[j] = (Short)arrayEl;
                            }

                            set[i] = tmp;
                            break;
                        }
                        tmp = new short[v.size()];
                        for(int k = 0; k < tmp.length; k++)
                            tmp[k] = ((Short)v.elementAt(k)).shortValue();

                        set[i] = tmp;
                        break;
                    }

                    case 3: // '\003'
                    {
                        int tmp[];
                        if(v.nillable)
                        {
                            tmp = new Integer[v.size()];
                            for(int j = 0; j < tmp.length; j++)
                            {
                                Object arrayEl = v.elementAt(j);
                                if(arrayEl != NIL)
                                    tmp[j] = (Integer)arrayEl;
                            }

                            set[i] = tmp;
                            break;
                        }
                        tmp = new int[v.size()];
                        for(int k = 0; k < tmp.length; k++)
                            tmp[k] = ((Integer)v.elementAt(k)).intValue();

                        set[i] = tmp;
                        break;
                    }

                    case 4: // '\004'
                    {
                        long tmp[];
                        if(v.nillable)
                        {
                            tmp = new Long[v.size()];
                            for(int j = 0; j < tmp.length; j++)
                            {
                                Object arrayEl = v.elementAt(j);
                                if(arrayEl != NIL)
                                    tmp[j] = (Long)arrayEl;
                            }

                            set[i] = tmp;
                            break;
                        }
                        tmp = new long[v.size()];
                        for(int k = 0; k < tmp.length; k++)
                            tmp[k] = ((Long)v.elementAt(k)).longValue();

                        set[i] = tmp;
                        break;
                    }

                    case 7: // '\007'
                    {
                        String tmp[] = new String[v.size()];
                        for(int j = 0; j < tmp.length; j++)
                        {
                            Object arrayEl = v.elementAt(j);
                            if(arrayEl != NIL)
                                tmp[j] = (String)arrayEl;
                        }

                        set[i] = tmp;
                        break;
                    }

                    case 5: // '\005'
                    case 6: // '\006'
                    default:
                    {
                        Object l[] = new Object[v.size()];
                        v.copyInto(l);
                        set[i] = vectorToArray(((Object) (l)));
                        break;
                    }
                    }
                    v = null;
                } else
                if(set[i] == NIL)
                    set[i] = null;
                else
                if(set[i] instanceof Object[])
                    set[i] = vectorToArray(set[i]);

        }
        return o;
    }

    private Object tokenToObject(Type type, String token)
    {
        if(token == null)
            return null;
        switch(type.value)
        {
        case 0: // '\0'
            token = token.toLowerCase();
            if(token.equals("true") || token.equals("1"))
                return new Boolean(true);
            if(token.equals("false") || token.equals("0"))
            {
                return new Boolean(false);
            } else
            {
                stateStack.push(new Integer(4));
                errString = "Expected Boolean, received: " + token;
                throw new RuntimeException();
            }

        case 1: // '\001'
            try
            {
                if(token.startsWith("+"))
                    return new Byte(Byte.parseByte(token.substring(1)));
                else
                    return new Byte(Byte.parseByte(token));
            }
            catch(NumberFormatException nfe)
            {
                stateStack.push(new Integer(4));
            }
            errString = "Expected Byte, received: " + token;
            throw new RuntimeException();

        case 2: // '\002'
            try
            {
                if(token.startsWith("+"))
                    return new Short(Short.parseShort(token.substring(1)));
                else
                    return new Short(Short.parseShort(token));
            }
            catch(NumberFormatException nfe)
            {
                stateStack.push(new Integer(4));
            }
            errString = "Expected Short, received: " + token;
            throw new RuntimeException();

        case 3: // '\003'
            try
            {
                if(token.startsWith("+"))
                    return new Integer(Integer.parseInt(token.substring(1)));
                else
                    return new Integer(Integer.parseInt(token));
            }
            catch(NumberFormatException nfe)
            {
                stateStack.push(new Integer(4));
            }
            errString = "Expected Integer, received: " + token;
            throw new RuntimeException();

        case 4: // '\004'
            try
            {
                if(token.startsWith("+"))
                    return new Long(Long.parseLong(token.substring(1)));
                else
                    return new Long(Long.parseLong(token));
            }
            catch(NumberFormatException nfe)
            {
                stateStack.push(new Integer(4));
            }
            errString = "Expected Long, received: " + token;
            throw new RuntimeException();

        case 7: // '\007'
            return token;

        case 5: // '\005'
        case 6: // '\006'
        default:
            stateStack.push(new Integer(4));
            errString = "Unable to decode type: " + type.value + ", for token: " + token;
            throw new RuntimeException();
        }
    }

    public void startElement(String uri, String localName, String name, Attributes attrs)
    {
        eName = localName;
        if(eName == null || eName.length() == 0)
            eName = name;
        if(eName.toLowerCase().equals("envelope") || eName.toLowerCase().equals("body"))
            if(!uri.equals("http://schemas.xmlsoap.org/soap/envelope/"))
            {
                errString = "Invalid URI From Server: " + uri + ", " + "expected: " + "http://schemas.xmlsoap.org/soap/envelope/";
                stateStack.push(new Integer(4));
                throw new RuntimeException();
            } else
            {
                bodyNEnvelope++;
                return;
            }
        if(bodyNEnvelope == 1 && uri.equals("http://schemas.xmlsoap.org/soap/envelope/") && eName.toLowerCase().equals("header"))
        {
            processingHeader = true;
            return;
        }
        if(processingHeader = true)
        {
            String mustUnderstand = attrs.getValue("http://schemas.xmlsoap.org/soap/envelope/", "mustUnderstand");
            if(mustUnderstand != null && mustUnderstand.equals("1"))
            {
                errString = "Unsupported header element with mustUnderstand";
                stateStack.push(new Integer(4));
                throw new RuntimeException();
            }
        }
        if(bodyNEnvelope != 2)
        {
            errString = "Missing SOAP Body or Envelope";
            stateStack.push(new Integer(4));
            throw new RuntimeException();
        }
        if(isNill)
        {
            errString = "Nillable element contains value: " + eName;
            stateStack.push(new Integer(4));
            throw new RuntimeException();
        } else
        {
            String attr = attrs.getValue("http://www.w3.org/2001/XMLSchema-instance", "nil");
            isNill = attr != null && (attr.toLowerCase().equals("true") || attr.equals("1"));
            token.delete(0, token.length());
            startState(uri, eName);
            return;
        }
    }

    public void endElement(String uri, String localName, String name)
    {
        eName = localName;
        if(eName == null || eName.length() == 0)
            eName = name;
        if(eName.toLowerCase().equals("envelope") || eName.toLowerCase().equals("body"))
            if(!uri.equals("http://schemas.xmlsoap.org/soap/envelope/"))
            {
                errString = "Invalid URI From Server: " + uri + ", " + "expected: " + "http://schemas.xmlsoap.org/soap/envelope/";
                stateStack.push(new Integer(4));
                throw new RuntimeException();
            } else
            {
                bodyNEnvelope--;
                return;
            }
        endState(uri, eName);
        isNill = false;
        if(bodyNEnvelope == 1 && uri.equals("http://schemas.xmlsoap.org/soap/envelope/") && eName.toLowerCase().equals("header"))
        {
            processingHeader = false;
            return;
        } else
        {
            return;
        }
    }

    public void characters(char chars[], int start, int len)
    {
        token.append(chars, start, len);
    }

    private static final Object NIL = new Object();
    private SAXParser parser;
    private StringBuffer token;
    private int state;
    private int bodyNEnvelope;
    private boolean processingHeader;
    private boolean isNill;
    private String eName;
    private boolean faultMode;
    private ComplexType faultCT;
    private FaultDetailHandler handler;
    private QName detailName;
    private Element handlerDetail;
    private Stack valueStack;
    private Stack typeStack;
    private Stack stateStack;
    private String errString;
    private static final String SOAP_URI = "http://schemas.xmlsoap.org/soap/envelope/";

    static 
    {
        SOAP_URI = "http://schemas.xmlsoap.org/soap/envelope/";
    }
}
