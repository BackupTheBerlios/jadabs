/*
 * Created on Jul 31, 2003
 *
 * $Id: EventService.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.jadabs.eventsystem;

import java.io.Serializable;

/**
 * EventService is the interface to register, unregister a EventListener.
 * Whoever is interested in an event, may write an EventListener and register it
 * on the EventService.
 * 
 * @author andfrei
 *  
 */
public interface EventService
{

    /**
     * Subscribe follows the publish/subscribe principle to subscribe for a
     * template. The template will be checked for the same type and content.
     * 
     * @param template
     * @param listener
     */
    public void subscribe(Filter filter, EventListener listener);

    /**
     * Unsubscribe removes the template and its listener from the listening
     * templates.
     * 
     * @param template
     */
    public void unsubscribe(Filter filter);

    /**
     * fireEvent will be called by an event source, which then sends an event to
     * all listeners.
     * 
     * @param event
     */
    public void publish(Event event);

    /**
     * Publish may additional contain an
     * 
     * @Advertisement which allows to narrow the subscribers.
     * 
     * @param event
     * @param adv
     */
//    public void publish(Serializable event, Advertisement adv);

    /**
     * Publish an event with a given reliable event policy, look for policy in
     * IReliableEventPolicy type.
     * 
     * @param event
     * @param policy
     */
//    public void publish(ReliableEventPolicy policy);

    /**
     * Publish may additional contain an
     * 
     * @Advertisement which allows to narrow the subscribers.
     * 
     * @param event
     * @param adv
     */

    /**
     * TODO: Only for testing
     * 
     * create concrete instances of events by a factory method to be compliant
     * to the coding convention used for the dynamic proxy
     */
    //public IEvent createEvent(String tag);
    /**
     * TODO: Only for testing
     * 
     * @param id
     * @param tag
     * @param attr
     */
    //public void publish(String id, String tag, Hashtable attr);
}