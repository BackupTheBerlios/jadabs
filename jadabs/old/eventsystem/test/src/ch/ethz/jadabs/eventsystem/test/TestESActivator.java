/*
 * Created on Jul 27, 2004
 *
 */
package ch.ethz.jadabs.eventsystem.test;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.eventsystem.Event;
import ch.ethz.jadabs.eventsystem.EventListener;
import ch.ethz.jadabs.eventsystem.EventService;
import ch.ethz.jadabs.eventsystem.Filter;
import ch.ethz.jadabs.eventsystem.impl.EventImpl;
import ch.ethz.jadabs.eventsystem.impl.FilterImpl;


/**
 * @author andfrei
 *
 */
public class TestESActivator implements BundleActivator, EventListener
{
    private static Logger LOG = Logger.getLogger(TestESActivator.class.getName());
    
    
    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception
    {
        ServiceReference sref = context.getServiceReference(EventService.class.getName());
        EventService eventsvc = (EventService)context.getService(sref);
        
        MyClass myclass = new MyClass();
        
        Event event = new EventImpl(myclass);
        
        Filter filter = new FilterImpl(new EventImpl());
        
        if (context.getProperty("ch.ethz.jadabs.jxme.peeralias").equals("peer1"))
            eventsvc.publish(event);
        else
            eventsvc.subscribe(filter, this);
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception
    {
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.eventsystem.EventListener#processEvent(ch.ethz.jadabs.eventsystem.Event)
     */
    public void processEvent(Event event)
    {
        LOG.debug("got event: "+event);
    }

}
