// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   SaxXML.java

package Intel.UPnP;

import java.io.ByteArrayInputStream;
import java.util.Hashtable;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

// Referenced classes of package Intel.UPnP:
//            ManualResetEvent

public class SaxXML extends DefaultHandler
    implements Runnable
{

    public SaxXML(String XMLString)
    {
        TagTable = new Hashtable();
        INIT = false;
        OpTag = "";
        InnerXMLTag = "";
        LastTagWasClosed = false;
        parser = new ManualResetEvent(false);
        handler = new ManualResetEvent(false);
        CurrentElement = "";
        _str_ = XMLString;
        TheSource = new InputSource(new ByteArrayInputStream(XMLString.getBytes()));
        SAXParserFactory SPF = SAXParserFactory.newInstance();
        CurrentElement = "";
        EOF = false;
        String s;
        try
        {
            SAX = SPF.newSAXParser();
        }
        catch(Exception e)
        {
            s = e.toString();
        }
    }

    public void run()
    {
        try
        {
            SAX.parse(TheSource, this);
        }
        catch(Exception e)
        {
            EOF = true;
            return;
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes)
    {
        if(!TagTable.containsKey(localName))
        {
            TagTable.put(localName, new Integer(1));
        } else
        {
            Integer i = (Integer)TagTable.get(localName);
            i = new Integer(i.intValue() + 1);
            TagTable.put(localName, i);
        }
        EndElement = false;
        if(INIT)
        {
            INIT = false;
            parser.ResetEvent();
            handler.SetEvent();
            parser.WaitForEvent();
            OpTag = localName;
            ElementData = "";
            Nodes = "";
        } else
        {
            if(OPS == READ)
            {
                if(!LastTagWasClosed)
                {
                    parser.ResetEvent();
                    handler.SetEvent();
                    parser.WaitForEvent();
                }
                if(OPS != INNER)
                    OpTag = localName;
                ElementData = "";
                Nodes = "";
            }
            if(OPS == INNER)
                Nodes = String.valueOf(Nodes) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("<")).append(localName).append(">"))));
        }
        LastTagWasClosed = false;
    }

    public void endElement(String uri, String localName, String qName)
    {
        int i = ((Integer)TagTable.get(localName)).intValue() - 1;
        TagTable.put(localName, new Integer(i));
        EndElement = true;
        if(OPS == INNER)
            if(OpTag.compareTo(localName) == 0 && i == 0)
            {
                parser.ResetEvent();
                handler.SetEvent();
                parser.WaitForEvent();
                ElementData = "";
                Nodes = "";
                LastTagWasClosed = true;
                return;
            } else
            {
                Nodes = String.valueOf(Nodes) + String.valueOf(String.valueOf(String.valueOf((new StringBuffer("</")).append(localName).append(">"))));
                LastTagWasClosed = true;
                return;
            }
        if(OPS == READ)
        {
            if(LastTagWasClosed)
                OpTag = localName;
            parser.ResetEvent();
            handler.SetEvent();
            parser.WaitForEvent();
            ElementData = "";
            Nodes = "";
            LastTagWasClosed = true;
            return;
        } else
        {
            return;
        }
    }

    public void characters(char ch[], int start, int length)
    {
        String data = new String(ch, start, length);
        ElementData = String.valueOf(ElementData) + String.valueOf(data);
        Nodes = String.valueOf(Nodes) + String.valueOf(data);
    }

    public void startDocument()
    {
        INIT = true;
        ElementData = "";
        OPS = NOP;
    }

    public void endDocument()
    {
        EOF = true;
    }

    public String getLocalName()
    {
        return OpTag;
    }

    public String getElementString()
    {
        return ElementData;
    }

    public String getInnerXML()
    {
        return Nodes;
    }

    public boolean IsEOF()
    {
        return EOF;
    }

    protected String _str_;
    protected Hashtable TagTable;
    public static int NOP = -1;
    public static int READ = 0;
    public static int INNER = 1;
    public boolean INIT;
    protected String OpTag;
    protected String InnerXMLTag;
    protected boolean LastTagWasClosed;
    public ManualResetEvent parser;
    public ManualResetEvent handler;
    protected String ElementData;
    protected boolean FetchElement;
    protected String Nodes;
    protected boolean SaveInfo;
    public int OPS;
    protected String LocalName;
    protected boolean EOF;
    public ManualResetEvent WaitObject;
    public ManualResetEvent WaitObject2;
    protected InputSource TheSource;
    protected SAXParser SAX;
    protected boolean ReadChars;
    public boolean EndElement;
    protected String CurrentElement;

}
