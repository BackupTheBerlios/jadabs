/*
 * Created on Jul 29, 2003
 *
 * $Id: IReliableEventPolicy.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.jadabs.eventsystem;

/**
 * Reliable Event takes care about the delivery of events. A reliable event may
 * have different sematics on the reliability. 1. Basically the event should be
 * sent to any node which matches the filter. 2. What is a reliable multicast
 * event?
 * 
 * @author andfrei
 *  
 */
public interface IReliableEventPolicy
{

    /**
     * Time to live can be used to resend the event when there was no
     * connection, or the ack has not been received.
     * 
     * @param ttl
     */
    public void setTTL(long ttl);

    /**
     * Returns the time to live amount.
     * 
     * @return
     */
    public long getTTL();

    /**
     * Set the DeliveryExceptionListener which handles the event if the event
     * could not be delivered as expected. Only one listener specified, replace
     * the previouse one.
     * 
     * @param listener
     */
    public void setDeliveryException(DeliveryExceptionListener listener);

    /**
     * Returns the DeliveryExceptionListener.
     * 
     * @return
     */
    public DeliveryExceptionListener getDeliveryException();

    /**
     * The event is enclosed.
     * 
     * @return
     */
    public Event getEvent();

}