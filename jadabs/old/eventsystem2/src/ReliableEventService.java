/*
 * Created on Jul 29, 2003
 *
 * $Id: ReliableEventService.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.jadabs.eventsystem.impl;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.eventsystem.Event;
import ch.ethz.jadabs.eventsystem.ReliableEventPolicy;

/**
 * @author andfrei
 */
public class ReliableEventService extends Thread
{

    private static Logger LOG = Logger.getLogger(ReliableEventService.class.getName());

    private static long periodThread = 5 * 1000;

    private boolean running = true;

    private Hashtable peers = new Hashtable();

    private EventServiceImpl esvc;

    public ReliableEventService(EventServiceImpl esvc)
    {
        this.esvc = esvc;
    }

    public void run()
    {

        //		synchronized(awaitReliableEvents){

        while (running)
        {
            Vector rmlist = new Vector();

            for (Enumeration en = peers.elements(); en.hasMoreElements();)
            {

                Hashtable eventsPerPeer = (Hashtable) en.nextElement();

                for (Enumeration txnen = eventsPerPeer.elements(); txnen.hasMoreElements();)
                {
                    ReliableEventPolicy eventpolicy = (ReliableEventPolicy) txnen.nextElement();

                    esvc.outq.put(eventpolicy.event);

                    long newttl = eventpolicy.getTTL() - periodThread;
                    eventpolicy.setTTL(newttl);

                    if (newttl <= 0)
                        rmlist.add(eventpolicy.event.getID());
                }

                for (Enumeration erm = rmlist.elements(); erm.hasMoreElements();)
                {
                    eventsPerPeer.remove(erm.nextElement());
                }

            }

            // remove timedout events

            try
            {
                Thread.sleep(periodThread);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }

        }
    }

    public void stopThread()
    {
        running = false;
    }

    public void add(ReliableEventPolicy eventpolicy)
    {

        Event event = eventpolicy.getEvent();
        String slaveName = event.getSlavePeerName();

        // add reliable event only when slavename is set
        if (slaveName != null)
        {
            Hashtable eventsPerPeer = null;
            if (peers.containsKey(slaveName))
                eventsPerPeer = (Hashtable) peers.get(slaveName);
            else
            {
                eventsPerPeer = new Hashtable();
                peers.put(slaveName, eventsPerPeer);
            }

            eventsPerPeer.put(event.getID(), eventpolicy);
            LOG.debug("inserted reliable event: " + slaveName + " uuid:" + event.getID());
        }
    }

    public void remove(String peername, String uuid)
    {

        //		String peername = event.getSlavePeerName();
        //		String uuid = event.getID();

        if (peers.containsKey(peername))
        {

            Hashtable eventsPerPeer = (Hashtable) peers.get(peername);

            if (eventsPerPeer.containsKey(uuid))
            {
                eventsPerPeer.remove(uuid);
                LOG.debug("removed reliable event: " + peername + " uuid:" + uuid);
            }

            if (eventsPerPeer.size() == 0)
                peers.remove(peername);
        }

    }

}