/*
 * Created on May 5, 2004
 *
 */
package ch.ethz.iks.eventsystem.test;

import ch.ethz.iks.eventsystem.IEvent;
import ch.ethz.iks.eventsystem.IEventListener;
import ch.ethz.iks.eventsystem.EventService;


/**
 * @author andfrei
 * 
 */
public class EchoEventListener implements EventListener
{
    
    EventService eventservice;
    
    public EchoEventListener(EventService eventservice)
    {
        this.eventservice = eventservice;
    }

    /*
     */
    public void processEvent(Event event)
    {
//        System.out.println(event.toXMLString());
//        event.addAttribute("test", "by peer2");
//        
//        StringEvent echoevent = new StringEvent();
//        echoevent.setMasterPeerName("peer2");
//        echoevent.addAttribute("starttime", event.getAttributeValue("starttime"));

        eventservice.publish(event);
    }

}
