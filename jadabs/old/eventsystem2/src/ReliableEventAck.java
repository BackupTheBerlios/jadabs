/*
 * Created on Jul 30, 2003
 *
 */
package ch.ethz.jadabs.eventsystem.impl;

import ch.ethz.jadabs.eventsystem.Event;

/**
 * @author andfrei
 *  
 */
public class ReliableEventAck extends AEvent
{

    public ReliableEventAck()
    {

    }

    public ReliableEventAck(Event event)
    {
        uuid = event.getID();
        slavePeerName = event.getMasterPeerName();
    }

    /**
     * Create a Message Type from the content.
     * 
     * @return Message
     */
    public IMessage toMessage(Class clas)
    {

        IMessage msg = null;
        if (clas != null)
            msg = super.toMessage(clas);
        else
            msg = super.toMessage(ReliableEventAck.class);

        return (msg);

    }
}