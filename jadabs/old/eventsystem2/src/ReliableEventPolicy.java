/*
 * Created on Jul 30, 2003
 *
 */
package ch.ethz.jadabs.eventsystem.impl;

import ch.ethz.jadabs.eventsystem.DeliveryExceptionListener;
import ch.ethz.jadabs.eventsystem.Event;
import ch.ethz.jadabs.eventsystem.ReliableEventPolicy;

/**
 * @author andfrei
 *  
 */
public class ReliableEventPolicy implements ReliableEventPolicy
{

    protected long ttl = 30 * 1000;

    protected DeliveryExceptionListener listener;

    protected Event event;

    public ReliableEventPolicy(Event event)
    {
        this.event = event;
        ((AEvent) event).reliability = AEvent.REL_REQ;
    }

    /**
     * @see ch.ethz.iks.eventsystem.test.IPolicy#setTTL(long)
     */
    public void setTTL(long ttl)
    {
        this.ttl = ttl;
    }

    /**
     * @see ch.ethz.iks.eventsystem.test.IPolicy#getTTL()
     */
    public long getTTL()
    {
        return ttl;
    }

    /**
     * @see ch.ethz.iks.eventsystem.test.IPolicy#setDeliveryException(ch.ethz.iks.eventsystem.DeliveryExceptionListener)
     */
    public void setDeliveryException(DeliveryExceptionListener listener)
    {
        this.listener = listener;
    }

    /**
     * @see ch.ethz.iks.eventsystem.test.IPolicy#getDeliveryException()
     */
    public DeliveryExceptionListener getDeliveryException()
    {
        return listener;
    }

    public Event getEvent()
    {
        return event;
    }

}