/*
 * Created on Jun 3, 2003
 *
 * $Id: InQueue.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.jadabs.eventsystem.impl;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.eventsystem.Event;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;

/**
 * InQueue is run as a Thread and contains all incomming Messages. After a
 * 
 * @Message arrives in the InQueue the inqueue Thread will process the entry and
 *          read first the type information. An instance of the event type will
 *          be created and initialized with the content of the message.
 * 
 * @author andfrei
 */
public class InQueue extends Queue implements Listener
{

    private static Logger LOG = Logger.getLogger(InQueue.class.getName());

    EventServiceImpl m_eventservice;

    public InQueue(EventServiceImpl eventservice)
    {
        m_eventservice = eventservice;
    }

    /**
     * Process the incomming Message from the PeerNetwork, implements
     * MessageListener.
     */
    public void handleMessage(Message message, String listenerId)
    {
        put(message);
    }

    /**
     * processEntry dispatches the received message from the InQueue
     */
    public void processEntry(Object obj)
    {
        Message message = (Message) obj;

        // here we assume each message is an event type should be later extended
        // to test if message is an event
        String type = null;

        EventImpl event = new EventImpl();
        event.unmarshal(message.getElements());
                    
        m_eventservice.processEvent(event);
            
          // reliable Events
          
//            if (aevent.reliability.equals(AEvent.REL_REQ))
//            {
//
//                aevent.reliability = AEvent.REL_NONE;
//
////                ReliableEventAck eack = new ReliableEventAck(event);
//
//                //				LOG.info("send aevent ack: "+ eack.getSlavePeerName()+" uuid:
//                // "+eack.getID());
//                m_eventservice.publish(eack);
//
//                m_eventservice.processEvent(event);
//            } else if (event instanceof ReliableEventAck)
//            {
//                LOG.info("will now remove reliable event from list: " + event.getMasterPeerName() + " uuid: "
//                        + event.getID());
//                m_eventservice.reliableESvc.remove(event.getMasterPeerName(), event.getID());
//            } else

    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.jadabs.jxme.Listener#handleSearchResponse(ch.ethz.jadabs.jxme.NamedResource)
     */
    public void handleSearchResponse(NamedResource namedResource)
    {
    }

}