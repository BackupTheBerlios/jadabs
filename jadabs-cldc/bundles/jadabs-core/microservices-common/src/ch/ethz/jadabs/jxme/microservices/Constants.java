/*
 * Created on Jan 16, 2005
 *
 * $Id: Constants.java,v 1.1 2005/01/16 22:43:28 printcap Exp $
 */
package ch.ethz.jadabs.jxme.microservices;


/**
 * This interface contains the message constants the are used
 * in the wiring layer between the MicroGroupServiceBundle and 
 * the MicroGroupServiceCore.
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public interface Constants
{
    /** return value to ACK when method does not have a return value */
    public static final short RETURN_ACK = 0;
    
    /** message code for publish */
    public static final short PUBLISH = 64;
    
    /** message code for publish remote */ 
    public static final short PUBLISH_REMOTE = 65;
    
    /** message code for local search */
    public static final short LOCAL_SEARCH = 66;
    
}
