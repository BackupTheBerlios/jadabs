package javax.microedition.sip;

import com.nokia.phone.ri.sip.v;

public class SipAddress
{

    public SipAddress(String s) throws IllegalArgumentException
    {
        a = new v(s);
    }

    public SipAddress(String s, String s1) throws IllegalArgumentException
    {
        a = new v(s, s1);
    }

    public String getDisplayName()
    {
        return a._mthtry();
    }

    public void setDisplayName(String s) throws IllegalArgumentException
    {
        a._mthfor(s);
    }

    public String getScheme()
    {
        return a._mthint();
    }

    public void setScheme(String s) throws IllegalArgumentException
    {
        a._mthtry(s);
    }

    public String getUser()
    {
        return a.a();
    }

    public void setUser(String s) throws IllegalArgumentException
    {
        a.a(s);
    }

    public String getURI()
    {
        return a._mthnew();
    }

    public void setURI(String s) throws IllegalArgumentException
    {
        a._mthif(s);
    }

    public String getHost()
    {
        return a._mthif();
    }

    public void setHost(String s) throws IllegalArgumentException
    {
        a._mthdo(s);
    }

    public int getPort()
    {
        return a._mthfor();
    }

    public void setPort(int i) throws IllegalArgumentException
    {
        a.a(i);
    }

    public String getParameter(String s)
    {
        return a._mthnew(s);
    }

    public void setParameter(String s, String s1) throws IllegalArgumentException
    {
        a._mthif(s, s1);
    }

    public void removeParameter(String s)
    {
        a._mthcase(s);
    }

    public String[] getParameterNames()
    {
        return a._mthdo();
    }

    public String toString()
    {
        return a.toString();
    }

    private v a;
}