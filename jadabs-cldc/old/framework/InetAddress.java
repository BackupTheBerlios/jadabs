/* 
 * Created on Jul 30, 2004
 * $Id: InetAddress.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package java.net;

import ch.ethz.jadabs.osgi.j2me.OSGiContainer;


/**
 * J2ME does not contain InetAddress, however due to compatibility
 * reasons to the J2SE implementation of JXME we provide an explicit
 * implementation of <code>InetAdddress</code>. 
 * 
 * But since the J2ME-Preverifier rejects any class that references
 * a class in the <code>java.net</code> package (since there is no
 * such package in the MIDP API) because of the lack of any imagination 
 * the package prefix <code>foo</code> was chosen. 
 * 
 * The InetAddress functionality is not used at all in CLDC therefore
 * we provide only an empty implementation here.    
 * 
 * @author andfrei
 * @author Ren&eacute; M&uuml;ller 
 */
public class InetAddress
{
    /** the single(ton) instance of this class  */
    private static InetAddress inetaddr = new InetAddress();
    
    /**
     * Get the address of the local host (which is the singleton instance) 
     * @return reference the one and only instance of this class
     */
    public static InetAddress getLocalHost()
    {
        return inetaddr;
    }
    
    /**
     * Return the name of the host specified by this address.
     * This implementation returns the String stored in the OSGiContainer
     * property <code>ch.ethz.jadabs.jxme.hostname</code>. 
     * 
     * @return value of <code>ch.ethz.ch.jxme.hostname</code>. 
     */
    public String getHostName()
    {
        return OSGiContainer.Instance().getProperty("ch.ethz.jadabs.jxme.hostname");
    }
    
    /**
     * Perform address look-up by name. This is an <b>empty</b> implementation.  
     * @param host argument is completely ignored
     * @return <b>always</b> <code>NULL</code>
     */
    public static InetAddress getByName(String host)
    {
        return null;
    }
    
    /**
     * Return host address string. This method implementation is empty.  
     * @return <b>always</b> <code>NULL</code>
     */
    public static String getHostAddress()
    {
        return null;
    }
}
