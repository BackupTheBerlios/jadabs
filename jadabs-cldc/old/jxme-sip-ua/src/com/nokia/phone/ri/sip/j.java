// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.nokia.phone.ri.sip;


public class j
{

    public j()
    {
    }

    public static String _mthif(String s)
    {
        if(s.equals("i"))
            return "Call-ID";
        if(s.equals("m"))
            return "Contact";
        if(s.equals("e"))
            return "Content-Encoding";
        if(s.equals("l"))
            return "Content-Length";
        if(s.equals("c"))
            return "Content-Type";
        if(s.equals("f"))
            return "From";
        if(s.equals("s"))
            return "Subject";
        if(s.equals("k"))
            return "Supported";
        if(s.equals("t"))
            return "To";
        if(s.equals("v"))
            return "Via";
        if(s.equals("a"))
            return "Accept-Contact";
        if(s.equals("j"))
            return "Reject-Contact";
        else
            return s;
    }

    public static String a(String s)
    {
        if(s.equals("Call-ID"))
            return "i";
        if(s.equals("Contact"))
            return "m";
        if(s.equals("Content-Encoding"))
            return "e";
        if(s.equals("Content-Length"))
            return "l";
        if(s.equals("Content-Type"))
            return "c";
        if(s.equals("From"))
            return "f";
        if(s.equals("Subject"))
            return "s";
        if(s.equals("Supported"))
            return "k";
        if(s.equals("To"))
            return "t";
        if(s.equals("Via"))
            return "v";
        if(s.equals("Accept-Contact"))
            return "a";
        if(s.equals("Reject-Contact"))
            return "j";
        else
            return s;
    }

    public static String a(int i)
    {
        if(i == 100)
            return "Trying";
        if(i == 180)
            return "Ringing";
        if(i == 181)
            return "Call Is Being Forwarded";
        if(i == 182)
            return "Queued";
        if(i == 183)
            return "Session Progress";
        if(i == 200)
            return "OK";
        if(i == 202)
            return "Accepted";
        if(i == 300)
            return "Multiple Choices";
        if(i == 301)
            return "Moved Permanently";
        if(i == 302)
            return "Moved Temporarily";
        if(i == 305)
            return "Use Proxy";
        if(i == 380)
            return "Alternative Service";
        if(i == 400)
            return "Bad Request";
        if(i == 401)
            return "Unauthorized";
        if(i == 402)
            return "Payment Required";
        if(i == 403)
            return "Forbidden";
        if(i == 404)
            return "Not Found";
        if(i == 405)
            return "Method Not Allowed";
        if(i == 406)
            return "Not Acceptable";
        if(i == 407)
            return "Proxy Authentication Required";
        if(i == 408)
            return "Request Timeout";
        if(i == 409)
            return "Conflict";
        if(i == 410)
            return "Gone";
        if(i == 411)
            return "Length Required";
        if(i == 413)
            return "Request Entity To Large";
        if(i == 414)
            return "Request-URI To Large";
        if(i == 415)
            return "Unsupported Media Type";
        if(i == 420)
            return "Bad Extension";
        if(i == 421)
            return "Extension Required";
        if(i == 423)
            return "Interval Too Small";
        if(i == 480)
            return "Temporarily Unavailable";
        if(i == 481)
            return "Call/Transaction Does Not Exist";
        if(i == 482)
            return "Loop Detected";
        if(i == 483)
            return "Too Many Hops";
        if(i == 484)
            return "Address Incomlpete";
        if(i == 485)
            return "Ambiguous";
        if(i == 486)
            return "Buzy Here";
        if(i == 487)
            return "Request Terminated";
        if(i == 488)
            return "Not Acceptable Here";
        if(i == 489)
            return "Bad Event";
        if(i == 491)
            return "Request Pending";
        if(i == 491)
            return "Undecipherable";
        if(i == 500)
            return "Server Internal Error";
        if(i == 501)
            return "Not Implemented";
        if(i == 502)
            return "Bad Gateway";
        if(i == 503)
            return "Service Unavailable";
        if(i == 504)
            return "Server Time-out";
        if(i == 505)
            return "Version Not Supported";
        if(i == 513)
            return "Message Too Large";
        if(i == 600)
            return "Buzy Everywhere";
        if(i == 603)
            return "Decline";
        if(i == 604)
            return "Does Not Exist Anywhere";
        if(i == 606)
            return "Not Acceptable";
        else
            return null;
    }
}
