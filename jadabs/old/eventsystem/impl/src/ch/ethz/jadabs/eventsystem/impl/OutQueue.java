/*
 * Created on Jun 3, 2003
 * 
 * $Id: OutQueue.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.jadabs.eventsystem.impl;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.eventsystem.Event;
import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.Peer;
import ch.ethz.jadabs.jxme.PeerGroup;
import ch.ethz.jadabs.jxme.Pipe;

/**
 * OutQueue receives events from the
 * 
 * @EventService and puts them into the outqueue. The Thread will then take the
 *               event from the queue, create a
 * @Message type out of it and send it to the network.
 * 
 * @author andfrei
 */
public class OutQueue extends Queue
{

    private static Logger LOG = Logger.getLogger(OutQueue.class.getName());

    public OutQueue()
    {
    }

    /**
     * processEntry sends the message through the outputPipe.
     */
    public void processEntry(Object obj)
    {
        Element[] elm;
        EventImpl event = null;
        String slavePeerName = null;

        if (obj instanceof EventImpl)
        {
            event = (EventImpl) obj;
            elm = event.marshal();
        }
        else
            return;

        try
        {
            // send event to specified namedresouce in the event
            for(Enumeration en = event.getNamedResources(); en.hasMoreElements();)
            {
                NamedResource namedr = (NamedResource)en.nextElement();
                
              if(namedr instanceof Peer)
              {
                  Peer peer = (Peer)namedr;
                  
                  EndpointAddress[] addrs = peer.getURIList();
                  
                  for(int i = 0; i < addrs.length; i++)
                  {
                      EventsystemActivator.endptsvc.send( event.marshal(), 
                              new EndpointAddress[] {addrs[i]});
                      break; // event is sent, otherwise an IOException would be thrown
                  }
                  
              } else if (namedr instanceof Pipe)
              {
                  //Pipe pipe = (Pipe)namedr;
                  // not yet supported, send through the pipeservice if available
              }else // (namedr instanceof PeerGroup)
              {
                  EndpointAddress endptlistener = new EndpointAddress(
                          null, EventServiceImpl.EVENTSERVICE_NAME,null);
                  //PeerGroup group = (PeerGroup)namedr;
                  // not yet supported fully, would need to propagate to the group 
                  // not to the own group
                  EventsystemActivator.endptsvc.propagate(elm, endptlistener);
              }
            }
            
            
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

}