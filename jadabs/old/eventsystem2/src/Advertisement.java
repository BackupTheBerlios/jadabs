/*
 * Created on Jul 2, 2003
 *
 * $Id: Advertisement.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.jadabs.eventsystem;

/**
 * @author andfrei
 */
public interface Advertisement
{

    public void narrow(EventService eventservice, Event event);

}