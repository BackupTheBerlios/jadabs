/*
 * Created on Dec 22, 2004
 * 
 * $Id: ConnectionNotifee.java,v 1.1 2004/12/27 15:25:03 printcap Exp $
 */
package ch.ethz.jadabs.core.wiring;


/**
 * Interface of listener that gets notified when a wakeup
 * connection is established.  
 *  
 * @author Ren&eacute; M&uuml;ller
 * @version 1.0
 */
public interface ConnectionNotifee
{
    /**
     * Invoked when a TCP connection was accepted.
     * @param connection LocalWiringConnection from this new connection
     */
    public void connectionEstablished(LocalWiringConnection connection);
}
