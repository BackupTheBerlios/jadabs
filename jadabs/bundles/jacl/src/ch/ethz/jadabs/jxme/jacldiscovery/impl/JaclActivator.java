package ch.ethz.jadabs.jxme.jacldiscovery.impl;

import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.jacldiscovery.JaclDiscovery;

/**
 * OSGi activator for the JACL Discovery Service, that extends the Jadabs
 * Eventsystem.
 * 
 * Should actually implement the BundleActivator, as long as it is in
 * eventsystem bundle the Activator of the EventSystem will call this
 * Activator.
 * 
 * @author rjan
 */
public class JaclActivator 
{

    protected static BundleContext b_context;

    protected static String peerName;

    protected static boolean chronThreadRunning = true;

    protected static Hashtable peerList = new Hashtable();

    private JaclDiscoveryImpl discovery;

    private ServiceReference sref;

    static Logger LOG = Logger.getLogger(JaclActivator.class.getName());

    /**
     * start the bundle, this method is called by the OSGi implementation.
     * 
     * @param bc
     *            the bundle context of the OSGi framework.
     * @throws Exception
     */
    public void start(BundleContext bc) throws Exception
    {
        // TODO: Get the AOP Proxy for the IEventSystem and
        // add the EventDiscovery as a mixin to the proxy.
        JaclActivator.b_context = bc;
        
        LOG.info("starting JACL - Discovery");
        
        // get EventService
//        sref = bc.getServiceReference(EventService.class.getName());
//        if (sref != null)
//        {
//            LOG.debug("Connected to EventService ...");
//        } else
//        {
//            LOG.debug("Can't start JACL - Discovery, evenservice not running !");
//            
//            bc.ungetService(sref);
//            throw new BundleException("Can't start JACL - Discovery, evenservice not running !");
//        }
//        
//        eventsvc = (EventService) bc.getService(sref);
        
        
        peerName = bc.getProperty("ch.ethz.jadabs.jxme.peeralias");

        discovery = new JaclDiscoveryImpl();
        // register Jacl as discovery service
        bc.registerService(JaclDiscovery.class.getName(), discovery, null);
    }

    /**
     * stops the bundle, this method is called by the OSGi implementation.
     * 
     * @param bc
     *            the bundle context of the OSGi framework.
     * @throws Exception
     */
    public void stop(BundleContext bc) throws Exception
    {
        JaclActivator.chronThreadRunning = false;
        bc.ungetService(sref);
        discovery = null;
    }
}