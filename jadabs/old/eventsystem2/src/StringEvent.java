/*
 * Created on Jun 2, 2003
 *
 * $Id: StringEvent.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.jadabs.eventsystem.impl;

import ch.ethz.jadabs.jxme.Message;

/**
 * Simple String Event.
 * 
 * @author andfrei
 *  
 */
public class StringEvent extends AEvent
{

    public StringEvent()
    {
        super();
    }

    public StringEvent(String tag, String str)
    {

        super(tag, str);
    }

    /**
     * Create a Message Type from the content.
     * 
     * This method is called to serialize the content of this instance.
     * Implement the method <code>init(Message)</code> to deserialize the
     * event.
     * 
     * @return Message
     */
    public IMessage toMessage(Class clas)
    {

        IMessage msg = null;
        if (clas != null)
            msg = super.toMessage(clas);
        else
            msg = super.toMessage(StringEvent.class);

        //		msg.setElement(new Element(AEvent.TYPE,
        // StringEvent.class.getName()));

        return (msg);

    }

}