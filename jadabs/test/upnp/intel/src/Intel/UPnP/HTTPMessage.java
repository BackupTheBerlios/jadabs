// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   HTTPMessage.java

package Intel.UPnP;

import java.io.*;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Hashtable;

public class HTTPMessage
{

    public HTTPMessage()
    {
        Headers = new Hashtable();
        StateObject = null;
        Headers = new Hashtable();
        Directive = "";
        DirectiveObj = "";
        StatusCode = -1;
        StatusData = "";
        Body = new byte[0];
    }

    public void AddTag(String TagName, String TagData)
    {
        Headers.put(TagName.toUpperCase(), TagData);
    }

    public String GetTag(String TagName)
    {
        String val = (String)Headers.get(TagName.toUpperCase());
        if(val == null)
            return "";
        else
            return val;
    }

    public String GetStringPacket()
        throws Exception
    {
        return new String(GetRawPacket());
    }

    public byte[] GetRawPacket()
    {
        Enumeration en = Headers.keys();
        ByteArrayOutputStream bostream = new ByteArrayOutputStream();
        byte abyte1[];
        try
        {
            if(StatusCode != -1)
                bostream.write(String.valueOf(String.valueOf((new StringBuffer("HTTP/1.1 ")).append((new Integer(StatusCode)).toString()).append(" ").append(StatusData).append("\r\n"))).getBytes());
            else
                bostream.write(String.valueOf(String.valueOf((new StringBuffer(String.valueOf(String.valueOf(Directive)))).append(" ").append(DirectiveObj).append(" HTTP/1.1\r\n"))).getBytes());
            String Tag;
            String TagData;
            for(; en.hasMoreElements(); bostream.write(String.valueOf(String.valueOf((new StringBuffer(String.valueOf(String.valueOf(Tag)))).append(":").append(TagData).append("\r\n"))).getBytes()))
            {
                Tag = (String)en.nextElement();
                TagData = (String)Headers.get(Tag);
            }

            bostream.write(String.valueOf(String.valueOf((new StringBuffer("Content-Length:")).append(Body.length).append("\r\n"))).getBytes());
            bostream.write("\r\n".getBytes());
            bostream.write(Body);
            byte buffer[] = bostream.toByteArray();
            bostream.close();
            byte abyte0[] = buffer;
            return abyte0;
        }
        catch(Exception e)
        {
            abyte1 = new byte[0];
        }
        return abyte1;
    }

    public byte[] GetBodyBuffer()
    {
        return Body;
    }

    public String GetStringBuffer()
    {
        if(Body.length == 0)
            return "";
        else
            return new String(Body);
    }

    public void SetBodyBuffer(byte buffer[])
    {
        Body = buffer;
    }

    public void SetStringBuffer(String buffer)
    {
        Body = buffer.getBytes();
    }

    public static HTTPMessage Parse(byte buffer[])
    {
        return Parse(buffer, 0, buffer.length);
    }

    public static HTTPMessage Parse(byte buffer[], int offset, int length)
    {
        String temp = new String(buffer, offset, length);
        HTTPMessage RetVal = new HTTPMessage();
        int ContentLength = 0;
        int BodyStart = temp.indexOf("\r\n\r\n") + 4;
        if(BodyStart == 3)
            System.out.println(String.valueOf(String.valueOf(temp)).concat("*"));
        temp = temp.substring(0, temp.indexOf("\r\n\r\n") + 2);
        int StartIDX = 0;
        int EndIDX = temp.indexOf("\r\n");
        do
        {
            String Line = temp.substring(StartIDX, EndIDX);
            if(StartIDX == 0)
            {
                if(Line.toUpperCase().startsWith("HTTP/"))
                {
                    String Tag = Line.substring(Line.indexOf(" ") + 1);
                    String TagData = Tag.substring(0, Tag.indexOf(" "));
                    RetVal.StatusCode = Integer.parseInt(TagData);
                    RetVal.StatusData = Tag.substring(Tag.indexOf(" ") + 1);
                } else
                {
                    RetVal.Directive = Line.substring(0, Line.indexOf(" ")).toUpperCase();
                    RetVal.DirectiveObj = URLDecoder.decode(Line.substring(Line.indexOf(" ") + 1, Line.indexOf(" ", Line.indexOf(" ") + 1)));
                }
            } else
            {
                String Tag = Line.substring(0, Line.indexOf(":")).toUpperCase().trim();
                String TagData;
                if(Tag.indexOf(":") == Line.length() - 1)
                    TagData = "";
                else
                    TagData = Line.substring(Line.indexOf(":") + 1).trim();
                if(Tag.compareTo("CONTENT-LENGTH") != 0)
                    RetVal.AddTag(Tag, TagData);
                else
                    ContentLength = Integer.parseInt(TagData);
            }
            StartIDX += Line.length() + 2;
            EndIDX = temp.indexOf("\r\n", StartIDX);
        } while(StartIDX < temp.length());
        if(ContentLength > 0)
        {
            RetVal.Body = new byte[ContentLength];
            System.arraycopy(buffer, BodyStart, RetVal.Body, 0, RetVal.Body.length);
        } else
        {
            RetVal.Body = new byte[0];
        }
        return RetVal;
    }

    public static int SizeToRead(byte buffer[])
    {
        return SizeToRead(buffer, 0, buffer.length);
    }

    public static int SizeToRead(byte buffer[], int start, int length)
    {
        String temp = new String(buffer, 0, length);
        int x = temp.indexOf("\r\n\r\n") + 4;
        temp = temp.substring(0, temp.indexOf("\r\n\r\n") + 2).toUpperCase();
        if(temp.indexOf("CONTENT-LENGTH") == -1)
        {
            return 0;
        } else
        {
            temp = temp.substring(temp.indexOf("CONTENT-LENGTH"));
            temp = temp.substring(0, temp.indexOf("\r\n"));
            temp = temp.substring(temp.indexOf(":") + 1).trim();
            x += Integer.parseInt(temp);
            return x;
        }
    }

    protected Hashtable Headers;
    protected byte Body[];
    public String Directive;
    public String DirectiveObj;
    public int StatusCode;
    public String StatusData;
    public Object StateObject;
}
