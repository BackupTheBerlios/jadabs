/*
 * Created on May 4, 2004
 *
 */
package ch.ethz.jadabs.eventsystem.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.eventsystem.EventService;
import ch.ethz.jadabs.jxme.EndpointService;
import ch.ethz.jadabs.jxme.PeerNetwork;
//import ch.ethz.jadabs.osgiaop.AOPContext;
//import ch.ethz.jadabs.osgiaop.AOPServiceRegistration;

/**
 * On activation lookup a PeerNetwork implementation and use it to send out
 * events. At least one PeerNetwork has to be available at startup.
 * 
 * Register the <code>IEventService</code> which can be used by another
 * service client. To get an instance of the EventServiceImpl get a reference by
 * <code>BundleContext.getServiceReference(IEventService.class.getName())</code>;
 * 
 * @author andfrei
 *  
 */
public class EventsystemActivator implements BundleActivator
{

    static BundleContext bc;

    static PeerNetwork peernetwork;

    static EndpointService endptsvc;

//    AOPServiceRegistration eventsvcreg;

    static EventServiceImpl eventsvc;

    //    ch.ethz.iks.eventsystem.jacldiscovery.impl.ESActivator jaclactivator;

    /*
     */
    public void start(BundleContext context) throws Exception
    {
        EventsystemActivator.bc = context;

        //init first, requires an IPeerNetwork instance
        //        URL logurl = bc.getBundle().getResource("log4j.properties");
        //        PropertyConfigurator.configure(logurl);

        // get PeerNetwork
        ServiceReference sref = context.getServiceReference(PeerNetwork.class.getName());
        peernetwork = (PeerNetwork) context.getService(sref);

        // get EndpointService
        sref = context.getServiceReference(EndpointService.class.getName());
        endptsvc = (EndpointService) context.getService(sref);

        // create the EventSerivce
        eventsvc = new EventServiceImpl(peernetwork.getPeer());

        // register ES as nanning proxy
        //        eventsvcreg = ((AOPContext) context).registerAOPService(
        //              EventService.class,
        //              eventserviceimpl, null);

        // register Eventsystem as a service
        context.registerService(EventService.class.getName(), eventsvc, null);

        // start the eventservice
        eventsvc.start();

        // start the JaclDiscoveryActivator
        //        jaclactivator = new
        // ch.ethz.iks.eventsystem.jacldiscovery.impl.ESActivator();
        //        jaclactivator.start(bc);

        // register fwevents.impl in jxme
        // get jxme bundle

    }

    /*
     */
    public void stop(BundleContext context) throws Exception
    {
        eventsvc.stop();

        // stop the JaclDisvcovery
        //        jaclactivator.stop(bc);
    }

}