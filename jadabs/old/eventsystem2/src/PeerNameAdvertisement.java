/*
 * Created on Jul 2, 2003
 *
 * $Id: PeerNameAdvertisement.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.jadabs.eventsystem.impl;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.eventsystem.EventService;
import ch.ethz.jadabs.eventsystem.Advertisement;
import ch.ethz.jadabs.eventsystem.Event;

/**
 * PeerNameAdvertisment narrows the publishing events to a list of given
 * peernames.
 * 
 * @author andfrei
 */
public class PeerNameAdvertisement implements Advertisement
{

    private static Logger LOG = Logger.getLogger(PeerNameAdvertisement.class.getName());

    private Vector peerlist;

    /**
     *  
     */
    public PeerNameAdvertisement(Vector peerlist)
    {
        this.peerlist = peerlist;
    }

    /*
     * @see ch.ethz.iks.eventsystem.Advertisement#narrow(ch.ethz.iks.eventsystem.EventService,
     *      ch.ethz.iks.eventsystem.Event)
     */
    public void narrow(EventService eventservice, Event event)
    {

        for (Enumeration en = peerlist.elements(); en.hasMoreElements();)
        {
            try
            {
                Event slaveEvent = (Event) ((AEvent) event).clone();

                String remotePeerName = (String) en.nextElement();

                slaveEvent.setSlavePeerName(remotePeerName);

                // publish event
                eventservice.publish(slaveEvent);
            } catch (CloneNotSupportedException cnse)
            {
                LOG.error("Could not clone Event", cnse);
            }
        }
    }

}