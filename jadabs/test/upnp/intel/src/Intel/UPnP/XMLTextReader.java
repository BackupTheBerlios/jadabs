// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   XMLTextReader.java

package Intel.UPnP;


// Referenced classes of package Intel.UPnP:
//            SaxXML, ManualResetEvent

public class XMLTextReader
{

    public void dispose()
    {
        XParser.handler.dispose();
        XParser.parser.dispose();
        WorkerThread.interrupt();
    }

    public XMLTextReader(String XML)
    {
        XParser = new SaxXML(XML);
        WorkerThread = new Thread(XParser);
        WorkerThread.start();
        XParser.handler.WaitForEvent();
    }

    public boolean Read()
    {
        if(XParser.IsEOF())
        {
            return false;
        } else
        {
            XParser.OPS = SaxXML.READ;
            XParser.handler.ResetEvent();
            XParser.parser.SetEvent();
            XParser.handler.WaitForEvent();
            return true;
        }
    }

    public String ReadString()
    {
        return XParser.getElementString();
    }

    public String getLocalName()
    {
        return XParser.getLocalName();
    }

    public void Skip()
    {
        ReadInnerXML();
    }

    public String ReadInnerXML()
    {
        if(XParser.EndElement)
        {
            return XParser.getElementString();
        } else
        {
            XParser.OPS = SaxXML.INNER;
            XParser.handler.ResetEvent();
            XParser.parser.SetEvent();
            XParser.handler.WaitForEvent();
            return XParser.getInnerXML();
        }
    }

    public boolean EOF()
    {
        return XParser.IsEOF();
    }

    public boolean IsEndElement()
    {
        return XParser.EndElement;
    }

    protected SaxXML XParser;
    protected Thread WorkerThread;
}
