/*
 * Created on Jun 3, 2003
 * 
 * $Id: EventServiceImpl.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.jadabs.eventsystem.impl;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.eventsystem.Event;
import ch.ethz.jadabs.eventsystem.EventService;
import ch.ethz.jadabs.eventsystem.EventListener;
import ch.ethz.jadabs.eventsystem.Filter;
import ch.ethz.jadabs.jxme.ID;
import ch.ethz.jadabs.jxme.Peer;
import ch.ethz.jadabs.jxme.PeerGroup;
import ch.ethz.jadabs.jxme.Service;

/**
 * GroupEventService passes the events generated in the group to all peers in
 * this group.
 * 
 * @author andfrei
 *  
 */
public class EventServiceImpl extends Service implements EventService, EventListener
{

    static Logger LOG = Logger.getLogger(EventServiceImpl.class.getName());

    static final String EVENTSERVICE_NAME = "EventService";

    public static final String EVENTSERVICE = "10";
    
    public static String peeralias;

    private PeerGroup group;
    
    // registered EventListener
    private Hashtable eventListeners = new Hashtable();

    // output queue
    protected OutQueue outq;

    protected InQueue inq;

    static long resourceid = 0;
    
    // check for not acked reliable events
//    public ReliableEventService reliableESvc;

    public EventServiceImpl(Peer peer)
    {
        super(peer, EVENTSERVICE_NAME);

//        this.group = group;
                
        outq = new OutQueue();
        inq = new InQueue(this);

        EventsystemActivator.endptsvc.addListener(EVENTSERVICE_NAME, inq);
    }

    /**
     * Convention for instantiation of main classes of (service) components. The
     * (default) constructor should be declared private to allow singleton
     * components.
     * 
     * @see ch.ethz.iks.jadabs.ComponentResource#initServiceComponent
     * @return an instance of this class
     */

    /**
     * Subscribe a MidasEventListener for a MidasTemplate.
     *  
     */
    public void subscribe(Filter filter, EventListener listener)
    {
        eventListeners.put(filter, listener);
    }

    /**
     * @see ch.ethz.iks.eventsystem.test.EventService#unsubscribe(ch.ethz.iks.eventsystem.Filter)
     */
    public void unsubscribe(Filter filter)
    {

        if (filter == null)
            return;

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

    /**
     * Send now the Event to remote GroupEventServices. The event will come into
     * the outputQueue, from where it will be sent ASAP.
     * 
     * @see midas.eventsystem.EventService#publish(midas.eventsystem.Event)
     */
    public void publish(Event event)
    {
        outq.put(event);
    }

    public ID nextEventID()
    {
        return new ID(peer.getID(), resourceid++, EVENTSERVICE);
    }
    
    /**
     * Publish Event in the given peerlist.
     * 
     * @param event
     * @param peerlist
     */
//    public void publish(Event event, Advertisement adv)
//    {
//
//        if (adv != null)
//            adv.narrow(this, event);
//        else
//            publish(event);
//    }

//    public void publish(ReliableEventPolicy eventpolicy)
//    {
//
//        //		// add event to reliableEventSvc if of that type
//        //		IReliableEventPolicy relEvent = (IReliableEventPolicy)event;
//        //		
//        //		((AEvent)event).reliability = AEvent.REL_REQ;
//        //		
//        //		ReliableEventPolicy eventpolicy = (ReliableEventPolicy)policy;
//        //		eventpolicy.event = event;
//        //		reliableESvc.add(event, eventpolicy);
//        //		
//        reliableESvc.add(eventpolicy);
//
//        publish(eventpolicy.getEvent());
//    }


    public void start()
    {
        // start finally
        inq.start();
        outq.start();
    }

    public void stop()
    {
        inq.stopThread();
        outq.stopThread();
    }

//    /***************************************************************************
//     * 
//     * code below is for testing only (evolution)
//     *  
//     **************************************************************************/
//
//    public Object[] exportFilters()
//    {
//        int i = 0;
//        String[] serializedFilters = new String[this.eventListeners.entrySet().size()];
//        //LOG.info("loaded IFilter from"+IFilter.class.getClassLoader());
//        //LOG.info("loaded FilterImpl from
//        // "+FilterImpl.class.getClassLoader());
//        LOG.info("exporting " + serializedFilters.length + " filter(s) ");
//        Iterator filters = this.eventListeners.entrySet().iterator();
//        while (filters.hasNext())
//        {
//            java.util.Map.Entry element = (java.util.Map.Entry) filters.next();
//            Filter filter = (Filter) element.getKey();
//            EventListener subscriber = (EventListener) element.getValue();
//            Event e = filter.getEvent();
//            serializedFilters[i] = e.toXMLString();
//            try
//            {
//                // hack to pretty print proxies
//                Method dumpProxy = subscriber.getClass().getMethod("dump", null);
//                serializedFilters[i] += dumpProxy.invoke(subscriber, null);
//            } catch (Exception ex)
//            {
//                serializedFilters[i] += subscriber.toString();
//            }
//        }
//        return serializedFilters;
//    }
//
//    public Event createEvent(String tag)
//    {
//        StringEvent evt = new StringEvent();
//        evt.setTag(tag);
//        return evt;
//    }

//    public void publish(String id, String tag, Hashtable attr)
//    {
//        Event evt = new StringEvent(tag, id);
//        Hashtable attrs = evt.getAttributes();
//        attrs.putAll(attr);
//        publish(evt);
//    }

}

