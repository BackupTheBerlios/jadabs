/*
 * Created on Jul 29, 2003
 *
 *	$Id: DeliveryExceptionListener.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.jadabs.eventsystem;

/**
 * @author andfrei
 *  
 */
public interface DeliveryExceptionListener
{

    public void notDelivered(DeliveryException de);

}