/*
 * SubscriberList.java
 *
 * Created on October 3, 2002, 6:48 PM
 */

package ch.ethz.jadabs.jxme.sip;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * 
 * @author deruelle
 * @version 1.0
 */
public class SubscriberList
{
    private Logger LOG = Logger.getLogger(SubscriberList.class);
    
    private Hashtable subscriberList;

    /** Creates new SubscriberController */
    public SubscriberList()
    {
        subscriberList = new Hashtable();
    }

    public Vector getAllSubscribers()
    {
        if (subscriberList != null)
        {
            Collection collection = subscriberList.values();
            return new Vector(collection);
        }
        return new Vector();
    }

    public boolean hasSubscriber(String subscriberName)
    {
        Subscriber subscriber = (Subscriber) subscriberList.get(subscriberName);
        if (subscriber == null)
            return false;
        else
            return true;
    }

    public Subscriber getSubscriber(String subscriberName)
    {
        return (Subscriber) subscriberList.get(subscriberName);
    }

    public void addSubscriber(Subscriber subscriber)
    {
        String subscriberName = subscriber.getSubscriberName();
        if (hasSubscriber(subscriberName))
        {
            LOG.debug("DEBUG, subscriberList, addSubscriber(), " + "We add a new subscriber: " + subscriberName);
            subscriberList.put(subscriberName, subscriber);
        } else
        {
            LOG.debug("DEBUG, subscriberList, addSubscriber(), " + "We update the subscriber: " + subscriberName);
            subscriberList.put(subscriberName, subscriber);
        }
        printSubscriberList();
    }

    public void removeSubscriber(String subscriberName)
    {
        Subscriber subscriber = (Subscriber) subscriberList.get(subscriberName);
        if (subscriber != null)
        {
            LOG.debug("DEBUG: subscriberList, removeSubscriber(), " + " the subscriber " + subscriberName
                    + " has been removed");
            subscriberList.remove(subscriberName);
        } else
            LOG.debug("DEBUG: subscriberList, removeSubscriber(), " + " the subscriber: " + subscriberName
                    + " was not found...");
        printSubscriberList();
    }

    public void printSubscriberList()
    {
        Collection collection = subscriberList.values();
        Vector subscribers = new Vector(collection);

        StringBuffer sb = new StringBuffer();
        
        sb.append("************* DEBUG subscriberList    ************************************");
        sb.append("************* Subscribers  record:    ************************************");
        sb.append("\n");
        for (int i = 0; i < subscribers.size(); i++)
        {
            Subscriber subscriber = (Subscriber) subscribers.elementAt(i);
            sb.append("subscriber URL : " + subscriber.getSubscriberName());
            sb.append("\n");
        }
        sb.append("**************************************************************************");

        LOG.debug(sb.toString());
    }

}