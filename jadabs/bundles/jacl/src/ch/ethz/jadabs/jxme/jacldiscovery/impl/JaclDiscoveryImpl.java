package ch.ethz.jadabs.jxme.jacldiscovery.impl;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.jacldiscovery.JaclDiscovery;
import ch.ethz.jadabs.jxme.jacldiscovery.JaclListener;

/**
 * adds JACL discovery features and leases to the Jadabs Eventsystem.
 * 
 * @author rjan
 */
public class JaclDiscoveryImpl implements Listener, JaclDiscovery
{

    private static String OFFSET = "offset";
    
    private static Logger LOG = Logger.getLogger(JaclDiscoveryImpl.class.getName());

    private long discoveryTimeEnds;

    private Vector leases = new Vector();

    private Vector listeners = new Vector();

    /**
     * Backoffnumber, the number of times a peer may send a SAE with its
     * wakeuptime.
     */
    protected int backoffno = 3;

    /**
     * Wakeuptime for the chronDeamon to send out SAEs
     */
    protected int wakeuptime = 5 * 1000;

    /**
     * constructor for the EventDiscoveryImpl.
     */
    public JaclDiscoveryImpl()
    {
        Thread chronJob = new chronThread(this);
        chronJob.start();
    }

    /**
     * subscribe an Eventsystem filter that is valid for a certain time period.
     * 
     * @param IFilter
     *            a Eventsystem filter.
     * @param IEventListener
     *            a Eventsystem EventListener.
     * @param long
     *            the time period in seconds.
     * 
     * @see ch.ethz.iks.eventsystem.jacldiscovery.JaclDiscovery#subscribe(ch.ethz.iks.eventsystem.IFilter,
     *      ch.ethz.iks.eventsystem.IEventListener, long)
     */
    public void subscribe(Filter filter, EventListener listener, long timeout)
    {
        if (timeout > 0)
        {
            leases.add(new Lease(System.currentTimeMillis() + 1000 * timeout, filter));
        }
        ActivatorJaclActivator.subscribe(filter, listener);
    }

    /**
     * set the amount of seconds, the JACL Discovery performs the discovering of
     * other peers.
     * 
     * @param int
     *            number of seconds, if 0, JACL will discover forever.
     * @see ch.ethz.iks.eventsystem.jacldiscovery.JaclDiscovery#setDiscoveryTime(int)
     */
    public void setDiscoveryTime(int time)
    {
        if (time > 0)
        {
            discoveryTimeEnds = System.currentTimeMillis() + 1000 * time;
        } else
        {
            discoveryTimeEnds = 0;
        }
    }

    /**
     * get the peers.
     * 
     * @return an enumeration of peers currently active and reachable.
     * @see ch.ethz.iks.eventsystem.jacldiscovery.JaclDiscovery#getPeers()
     */
    public Enumeration getPeers()
    {
        return JaclActivator.peerList.keys();
    }

    public void addListener(JaclListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(JaclListener listener)
    {
        listeners.remove(listener);
    }

    private void fireJaclGotPeerEvent(String newPeer)
    {
        for (Enumeration en = listeners.elements(); en.hasMoreElements();)
        {
            ((JaclListener) en.nextElement()).JaclGotPeerEvent(newPeer);
        }
    }

    private void fireJaclLostPeerEvent(String lostPeer)
    {
        for (Enumeration en = listeners.elements(); en.hasMoreElements();)
        {
            ((JaclListener) en.nextElement()).JaclLostPeerEvent(lostPeer);
        }
    }

    /* thread, that does all periodical functions */
    class chronThread extends Thread
    {

        private JaclDiscovery parent;

        public chronThread(JaclDiscovery parent)
        {
            this.parent = parent;
        }

        public void run()
        {
            
            Element elm = new Element(OFFSET, Long.toString(wakeuptime).getBytes(), 
                    null, Element.TEXTUTF8_MIME_TYPE);
            
            event.setMasterPeerName(JaclActivator.peerName);

            while (JaclActivator.chronThreadRunning)
            {
                ActivatorJaclActivator.publish(event);

                /*
                 * check the peer list for peers, that haven't responded for
                 * three cycles
                 */
                for (Enumeration keys = JaclActivator.peerList.keys(); keys.hasMoreElements();)
                {
                    String key = ((String) keys.nextElement());
                    Peer peer = ((Peer) JaclActivator.peerList.get(key));
                    if (peer.lastAlive + backoffno * peer.offset < System.currentTimeMillis())
                    {
                        JaclActivator.peerList.remove(key);
                        fireJaclLostPeerEvent(key);
                        // setDiscoveryTime(122);
                    }
                }

                /* check, if leases have expired */
                for (Enumeration elems = leases.elements(); elems.hasMoreElements();)
                {
                    Lease currLease = (Lease) elems.nextElement();

                    if (currLease.expires > System.currentTimeMillis())
                    {
                        ActivatorJaclActivator.unsubscribe(currLease.filter);
                        leases.remove(currLease);
                    }
                }

                /* check, if the discovery period is over */
                if (discoveryTimeEnds != 0 && System.currentTimeMillis() > discoveryTimeEnds)
                {
                    ActivatorJaclActivator.unsubscribe(stillAliveFilter);
                    JaclActivator.chronThreadRunning = false;
                }
                try
                {
                    Thread.sleep(wakeuptime);
                } catch (InterruptedException ie)
                {
                    LOG.error("JaclDiscovery:chronThread interrupted", ie);
                    ie.printStackTrace();
                }
            }
        }
    }

    /*
     */
    public void handleMessage(Message msg, String listenerId)
    {

        long offset = Long.parseLong(new String(msg.getElement(OFFSET).getData()));
        
        String sender = event.getMasterPeerName();
        Peer peer = (Peer) JaclActivator.peerList.get(sender);
        if (peer != null)
        {
            peer.lastAlive = System.currentTimeMillis();
            peer.offset = offset;
        } else
        {
            peer = new Peer();
            peer.name = sender;
            //peer.lastAlive = ((StillAliveEvent) event).getTimestamp();
            peer.lastAlive = System.currentTimeMillis();
            peer.offset = offset;
            JaclActivator.peerList.put(sender, peer);

            fireJaclGotPeerEvent(sender);
        }
        
    }

    /*
     */
    public void handleSearchResponse(NamedResource namedResource)
    {
        
    }
}