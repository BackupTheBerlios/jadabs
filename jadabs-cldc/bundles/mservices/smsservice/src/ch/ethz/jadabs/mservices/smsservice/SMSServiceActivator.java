/*
 * Created on Aug 4, 2004
 * $Id: SMSServiceActivator.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package ch.ethz.jadabs.mservices.smsservice;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.EndpointService;

/**
 * This is the activator for the SMS Service.
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class SMSServiceActivator implements BundleActivator
{
    /** reference to the context of this bundle */
    static BundleContext bc;
    
    /** the SMS service */
    SMSService smsservice;

    /**
     * Called when the bundle has to be started
     * @param bc reference to the bundle's context
     */
    public void start(BundleContext bc) throws Exception
    {
        SMSServiceActivator.bc = bc;               
        
        // register SMSService
        smsservice = new SMSService();
        SMSServiceActivator.bc.registerService("ch.ethz.jadabs.mservices.smsservice.SMSService", smsservice, null);

        // register SMTPGateway in EndpointService
        ServiceReference sref = bc.getServiceReference("ch.ethz.jadabs.jxme.EndpointService");
        EndpointService endptsvc = (EndpointService)bc.getService(sref);
        
        endptsvc.addListener("smsservice", smsservice);
    }

    /**
     * Called when the bundle has to be stopped 
     * @param bc reference to the bundle's context
     */
    public void stop(BundleContext bc) throws Exception
    {
       smsservice.abort(); 
    }
}