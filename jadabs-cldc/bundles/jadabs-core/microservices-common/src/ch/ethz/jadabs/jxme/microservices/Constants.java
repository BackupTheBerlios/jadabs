/*
 * Created on Jan 16, 2005
 *
 * $Id: Constants.java,v 1.3 2005/02/18 21:12:30 printcap Exp $
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
    
    /** message code for remote search */
    public static final short REMOTE_SEARCH = 67;
    
    /** message code for remote search */
    public static final short CANCEL_SEARCH = 68;
    
    /** message code for create and publish */
    public static final short CREATE = 69;
    
    /** message code for join*/
    public static final short JOIN  = 70;
    
    /** message code for send */
    public static final short SEND  = 71;
    
    /** message code for listen */
    public static final short LISTEN = 72;
    
    /** message code for resolve */
    public static final short RESOLVE = 73; 
    
    /** message code for close */
    public static final short CLOSE = 74;
    
    /** message for SEARCH_RESPONSE ASYNC_MSG */
    public static final short SEARCH_RESPONSE = 92;
    
    /** message for NAME_RESOURCE_LOSS ASYNC_MSG */
    public static final short NAME_RESOURCE_LOSS = 93;
    
    /** message for MESSAGE ASYNC_MSG */
    public static final short MESSAGE = 94;
    
}
