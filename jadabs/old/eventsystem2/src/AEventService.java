/*
 * Created on Apr 15, 2003
 *
 * $Id: AEventService.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.jadabs.eventsystem.impl;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.eventsystem.EventService;
import ch.ethz.jadabs.eventsystem.Event;
import ch.ethz.jadabs.eventsystem.EventListener;
import ch.ethz.jadabs.eventsystem.Filter;

/**
 * AEventService is an abstract class for any implementing EventService. The
 * abstract class combines the GroupEventService and SpecializedEventService
 * implementation. Basically it administrates the subscribed EventListener with
 * an EventTemplate.
 * 
 * @author andfrei
 */
public abstract class AEventService implements EventService, EventListener
{

    private static Logger LOG = Logger.getLogger(AEventService.class.getName());

    private Hashtable eventListeners = new Hashtable();

    /**
     * Subscribe a MidasEventListener for a MidasTemplate.
     *  
     */
    public void subscribe(Filter filter, EventListener listener)
    {
        eventListeners.put(filter, listener);
    }

    /**
     * 
     * @see ch.ethz.iks.eventsystem.test.EventService#unsubscribe(ch.ethz.iks.eventsystem.Filter)
     */
    public void unsubscribe(Filter filter)
    {

        Object listener = eventListeners.remove(filter);

        if (LOG.isDebugEnabled())
        {
            if (listener == null)
                LOG.debug("unsubscribe Filter couldn't find a listener");
            else
                LOG.debug("unsubscribed Filter with listener");
        }
    }

    /**
     * 
     * @see ch.ethz.iks.eventsystem.test.EventListener#processEvent(ch.ethz.iks.eventsystem.Event)
     */
    public void processEvent(Event event)
    {

        for (Enumeration en = eventListeners.keys(); en.hasMoreElements();)
        {

            Filter filter = (Filter) en.nextElement();

            if (filter.matches(event))
            {
                EventListener el = (EventListener) eventListeners.get(filter);

                if (LOG.isDebugEnabled())
                    LOG.debug("found eventlistener for event");

                el.processEvent(event);
            }

        }

    }

}