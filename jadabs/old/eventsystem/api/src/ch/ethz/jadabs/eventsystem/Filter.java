/*
 * Created on Jul 1, 2003
 * 
 * $Id: Filter.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.jadabs.eventsystem;

import ch.ethz.jadabs.eventsystem.Event;

/**
 * A Filter allows to filter incomming events for type and content.
 * 
 * @author andfrei
 */
public interface Filter
{

    /**
     * Filter any of this type
     */
    public class ANY
    {

    }

    /**
     * Check if the event machtes with this filter.
     * 
     * @param e
     * @return
     */
    public boolean matches(Event e);

    /**
     * Return event which is the template to filter events.
     * 
     * @return
     */
    public Event getEvent();

}