/*
 * Created on Jul 28, 2004
 *
 */
package ch.ethz.jadabs.jxme.ws;

import ch.ethz.jadabs.eventsystem.EventService;
import ch.ethz.jadabs.jxme.EndpointService;


/**
 * @author andfrei
 *
 */
public class Service
{

    static EventService eventsvc;
    static EndpointService endptsvc;
    
    public Service()
    {
        
    }
    
    public Call createCall()
    {
        return new Call();
    }
}
