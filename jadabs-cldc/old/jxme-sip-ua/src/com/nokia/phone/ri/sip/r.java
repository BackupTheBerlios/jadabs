// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;

import java.io.*;
import javax.microedition.io.*;
import javax.microedition.sip.SipException;

// Referenced classes of package com.nokia.phone.ri.sip:
//            h, Protocol, x, a

public class r
{
    private class a extends Thread
    {

        public void run()
        {
            while(_fldnew) 
                try
                {
                    Datagram datagram = _fldif.newDatagram(1500);
                    _fldif.receive(datagram);
                    String s = (new String(datagram.getData())).substring(0, datagram.getLength());
                    Protocol._mthif("\nDatagram received from: " + datagram.getAddress());
                    if(datagram.getLength() > 0)
                    {
                        Protocol._mthif("\n<-<-<-----------------------------------\n" + s);
                        Protocol._mthif("----------------------------------------");
                        ByteArrayInputStream bytearrayinputstream = new ByteArrayInputStream(s.getBytes());
                        com.nokia.phone.ri.sip.a a1 = _fldbyte.a(bytearrayinputstream);
                        if(a1 == null)
                            throw new SipException("Unable to parse received message", (byte)0);
                        a1._flddo = r.this.a;
                        _fldtry.a(a1);
                    } else
                    {
                        Protocol._mthif("DatagramController: Datagram empty");
                    }
                    System.out.flush();
                }
                catch(InterruptedIOException interruptedioexception) { }
                catch(IOException ioexception)
                {
                    Protocol._mthif("\nDatagramController: receiver failure... " + ioexception);
                }
                catch(Exception exception)
                {
                    Protocol._mthif("\nDatagramController: Internal failure... " + exception);
                }
        }

        private a()
        {
        }

    }


    public r(x x1, int i)
        throws IOException
    {
        _fldbyte = null;
        _fldnew = true;
        _fldtry = x1;
        _fldbyte = new h();
        if(i == 0)
        {
            Protocol._mthif("DatagramController: init with datagram://");
            _fldif = (UDPDatagramConnection)Connector.open("datagram://", 3);
        } else
        {
            Protocol._mthif("DatagramController: init with datagram://:" + i);
            _fldif = (UDPDatagramConnection)Connector.open("datagram://:" + i, 3);
        }
        _flddo = _fldif.getLocalAddress();
        a = _fldif.getLocalPort();
        _fldfor = new a();
        _fldfor.start();
        Protocol._mthif("DatagramListener: started...on port: " + a);
    }

    public void _mthif()
    {
        _fldnew = false;
        try
        {
            _fldif.close();
        }
        catch(IOException ioexception) { }
    }

    public String _mthdo()
    {
        return _flddo;
    }

    public int a()
    {
        return a;
    }

    public void a(String s, int i, String s1)
        throws IOException
    {
        byte abyte0[] = s1.getBytes();
        Datagram datagram = _fldif.newDatagram(abyte0, abyte0.length);
        datagram.setAddress("datagram://" + s + ":" + i);
        _fldif.send(datagram);
    }

    private static final short _fldint = 1500;
    private x _fldtry;
    private h _fldbyte;
    private a _fldfor;
    private UDPDatagramConnection _fldif;
    private String _flddo;
    private int a;
    private boolean _fldnew;





}
