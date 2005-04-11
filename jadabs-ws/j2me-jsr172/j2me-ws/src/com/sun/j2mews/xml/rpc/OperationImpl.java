// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   OperationImpl.java

package com.sun.j2mews.xml.rpc;

import com.sun.midp.io.Base64;
import java.io.*;
import java.rmi.MarshalException;
import java.rmi.ServerException;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.xml.rpc.*;
import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;

// Referenced classes of package com.sun.j2mews.xml.rpc:
//            SOAPEncoder, SOAPDecoder

public class OperationImpl extends Operation
{

    public OperationImpl(QName name, Element input, Element output)
        throws IllegalArgumentException
    {
        this.name = name;
        inputType = input;
        returnType = output;
        encoder = new SOAPEncoder();
        decoder = new SOAPDecoder();
    }

    public OperationImpl(QName name, Element input, Element output, FaultDetailHandler faultDetailHandler)
        throws IllegalArgumentException
    {
        this.name = name;
        inputType = input;
        returnType = output;
        faultHandler = faultDetailHandler;
        encoder = new SOAPEncoder();
        decoder = new SOAPDecoder();
    }

    public void setProperty(String name, String value)
        throws IllegalArgumentException
    {
        if(name == null || value == null)
            throw new IllegalArgumentException();
        if(!name.equals("javax.xml.rpc.service.endpoint.address") && !name.equals("javax.xml.rpc.security.auth.password") && !name.equals("javax.xml.rpc.security.auth.username") && !name.equals("javax.xml.rpc.session.maintain") && !name.equals("javax.xml.rpc.soap.http.soapaction.uri"))
            throw new IllegalArgumentException();
        if(properties != null)
        {
            for(int i = 0; i < propertyIndex; i += 2)
                if(properties[i].equals(name))
                {
                    properties[i + 1] = value;
                    return;
                }

        }
        if(properties == null)
            properties = new String[10];
        else
        if(propertyIndex == properties.length)
        {
            String newProps[] = new String[properties.length + 10];
            System.arraycopy(properties, 0, newProps, 0, properties.length);
            properties = null;
            properties = newProps;
        }
        properties[propertyIndex++] = name;
        properties[propertyIndex++] = value;
    }

    public Object invoke(Object params)
        throws JAXRPCException
    {
        HttpConnection http = null;
        OutputStream ostream = null;
        InputStream istream = null;
        try
        {
            http = (HttpConnection)Connector.open(getProperty("javax.xml.rpc.service.endpoint.address"));
            ostream = setupReqStream(http);
            encoder.encode(params, inputType, ostream, null);
            if(ostream != null)
                ostream.close();
            istream = setupResStream(http);
            Object result = null;
            if(returnType != null)
                result = decoder.decode(returnType, istream, http.getEncoding(), http.getLength());
            if(http != null)
                http.close();
            if(istream != null)
                istream.close();
            return result;
        }
        catch(Throwable t)
        {
            if(ostream != null)
                try
                {
                    ostream.close();
                }
                catch(Throwable t2) { }
            if(istream != null)
                try
                {
                    istream.close();
                }
                catch(Throwable t3) { }
            if(http != null)
                try
                {
                    http.close();
                }
                catch(Throwable t1) { }
            if(t instanceof JAXRPCException)
                throw (JAXRPCException)t;
            if((t instanceof MarshalException) || (t instanceof ServerException) || (t instanceof FaultDetailException))
                throw new JAXRPCException(t);
            else
                throw new JAXRPCException(t.toString());
        }
    }

    protected OutputStream setupReqStream(HttpConnection http)
        throws IOException
    {
        http.setRequestMethod("POST");
        http.setRequestProperty("User-Agent", "Profile/MIDP-1.0 Configuration/CLDC-1.0");
        http.setRequestProperty("Content-Language", "en-US");
        http.setRequestProperty("Content-Type", "text/xml");
        String soapAction = getProperty("javax.xml.rpc.soap.http.soapaction.uri");
        if(soapAction == null)
            soapAction = "\"\"";
        if(!soapAction.startsWith("\""))
            soapAction = "\"" + soapAction;
        if(!soapAction.endsWith("\""))
            soapAction = soapAction + "\"";
        http.setRequestProperty("SOAPAction", soapAction);
        String useSession = getProperty("javax.xml.rpc.session.maintain");
        if(useSession != null && useSession.toLowerCase().equals("true"))
        {
            String cookie = getSessionCookie(getProperty("javax.xml.rpc.service.endpoint.address"));
            if(cookie != null)
                http.setRequestProperty("Cookie", cookie);
        }
        String s1 = getProperty("javax.xml.rpc.security.auth.username");
        String s2 = getProperty("javax.xml.rpc.security.auth.password");
        if(s1 != null && s2 != null)
        {
            byte encodeData[] = (s1 + ":" + s2).getBytes();
            http.setRequestProperty("Authorization", "Basic " + Base64.encode(encodeData, 0, encodeData.length));
        }
        return http.openOutputStream();
    }

    protected InputStream setupResStream(HttpConnection http)
        throws IOException, ServerException
    {
        InputStream input = http.openInputStream();
        int response = http.getResponseCode();
        if(response == 200)
        {
            String useSession = getProperty("javax.xml.rpc.session.maintain");
            if(useSession != null && useSession.toLowerCase().equals("true"))
            {
                String cookie = http.getHeaderField("Set-Cookie");
                if(cookie != null)
                    addSessionCookie(getProperty("javax.xml.rpc.service.endpoint.address"), cookie);
            }
            return input;
        }
        Object detail = decoder.decodeFault(faultHandler, input, http.getEncoding(), http.getLength());
        if(detail instanceof String)
        {
            if(((String)detail).indexOf("DataEncodingUnknown") != -1)
                throw new MarshalException((String)detail);
            else
                throw new ServerException((String)detail);
        } else
        {
            Object wrapper[] = (Object[])detail;
            String message = (String)wrapper[0];
            QName name = (QName)wrapper[1];
            detail = wrapper[2];
            throw new JAXRPCException(message, new FaultDetailException(name, detail));
        }
    }

    private String getProperty(String key)
    {
        if(properties != null)
        {
            for(int i = 0; i < properties.length - 2; i += 2)
            {
                if(properties[i] == null)
                    return null;
                if(properties[i].equals(key))
                    return properties[i + 1];
            }

        }
        return null;
    }

    private static synchronized void addSessionCookie(String endpoint, String cookie)
    {
        if(endpoint == null || cookie == null)
            return;
        int i = cookie.indexOf(";");
        if(i > 0)
            cookie = cookie.substring(0, i);
        if(cookies != null)
            for(i = 0; i < cookieIndex; i += 2)
                if(cookies[i].equals(endpoint))
                {
                    cookies[i + 1] = cookie;
                    return;
                }

        if(cookies == null)
            cookies = new String[10];
        else
        if(cookieIndex == cookies.length)
        {
            String newCookies[] = new String[cookies.length + 10];
            System.arraycopy(cookies, 0, newCookies, 0, cookies.length);
            cookies = null;
            cookies = newCookies;
        }
        cookies[cookieIndex++] = endpoint;
        cookies[cookieIndex++] = cookie;
    }

    private static synchronized String getSessionCookie(String endpoint)
    {
        if(cookies != null)
        {
            for(int i = 0; i < cookies.length - 2; i += 2)
            {
                if(cookies[i] == null)
                    return null;
                if(cookies[i].equals(endpoint))
                    return cookies[i + 1];
            }

        }
        return null;
    }

    private static String cookies[];
    private static int cookieIndex;
    private String properties[];
    private int propertyIndex;
    private SOAPEncoder encoder;
    private SOAPDecoder decoder;
    private QName name;
    private Element inputType;
    private Element returnType;
    private FaultDetailHandler faultHandler;
}
