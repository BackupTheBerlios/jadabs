/*
 * Created on May 13, 2004
 *
 */
package ch.ethz.jadabs.jxme.jacldiscovery;

import java.util.Enumeration;

/**
 * The EventServiceDiscovery extends the EventService to allow
 * a regular discovery or publishing for the subscribed filters
 * and sending events.
 * 
 * @author andfrei
 * 
 */
public interface JaclDiscovery
{
    
    /**
     * Subscribe a filter with a predefined leasetime. After the leasetime
     * is over the subscribed listener has to be deleted.
     * 
     * The timeout of 0 means to cache and do a regular discovery of
     * the subscribed filter.
     * 
     * @param filter
     * @param listener
     * @param leasetime
     */
//    void subscribe(Filter filter, EventListener listener, long timeout);
    
    
    /**
     * Publish an event until the given timeout is over.
     * 
     * @param event
     * @param timeout
     */
    // void publish(IEvent event, long timeout);
    
    /**
     * Set the discovery time to send out the published events and subscribed
     * filters.
     * 
     * @param time
     */
    void setDiscoveryTime(int time);
    
    public Enumeration getPeers();
    
    public void addListener(JaclListener listener);
    public void removeListener(JaclListener listener);
    
}
