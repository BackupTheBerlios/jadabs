/*
 * Created on Jul 31, 2003
 *
 * $Id: EventListener.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.jadabs.eventsystem;

import ch.ethz.jadabs.eventsystem.Event;

/**
 * EventListener Interface defines the method which will be called on an occured
 * event.
 * 
 * Register the EventListener in the EventService.
 * 
 * @author andfrei
 *  
 */
public interface EventListener
{

    /**
     * Implement this method to process an event.
     * 
     * @param event
     */
    public void processEvent(Event event);

}