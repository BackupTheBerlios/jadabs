/*
 * Created on Aug 5, 2004
 * $Id: SMSGatewayActivator.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package ch.ethz.jadabs.mservices.smsgateway;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.EndpointService;

/**
 * This is the activator for the SMS Gateway Service.
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class SMSGatewayActivator implements BundleActivator
{
    /** Apache Log4J logger to be used in the SMSService */
    private static Logger LOG = Logger.getLogger("SMSGatewayActivator");
    
    /** reference to the context of this bundle */
    static BundleContext bc;
    
    /** the SMS Gatway service */
    SMSGatewayService smsgateway;
    
    /** the SMTP discovery service */
    SMTPServiceDiscovery smtpServiceDiscovery;

    /**
     * Called when the bundle has to be started
     * @param bc reference to the bundle's context
     */
    public void start(BundleContext bc) throws Exception
    {
        SMSGatewayActivator.bc = bc;               
        
        // register SMTPGateway in EndpointService
        ServiceReference sref = bc.getServiceReference("ch.ethz.jadabs.jxme.EndpointService");
        EndpointService endptsvc = (EndpointService)bc.getService(sref);
        
        // register SMSService
        String emailSuffix = bc.getProperty("ch.ethz.jadabs.mservices.smsgateway.emailsuffix");
        String senderEmailAddress = bc.getProperty("ch.ethz.jadabs.mservices.smsgateway.senderaddress");
        if ((emailSuffix == null) || (senderEmailAddress == null)) {
            LOG.fatal("SMS-Gateway: emailsuffix or senderaddress not specified.");
            throw new Exception("SMS-Gateway: emailsuffix or senderaddress not specified.");
        }
        
        smtpServiceDiscovery = new SMTPServiceDiscovery(endptsvc);
        smsgateway = new SMSGatewayService(smtpServiceDiscovery, endptsvc, emailSuffix,
                senderEmailAddress);
        SMSGatewayActivator.bc.registerService(
                "ch.ethz.jadabs.mservices.smsgateway.SMSGateway", smsgateway, null);
        SMSGatewayActivator.bc.registerService(
                "ch.ethz.jadabs.mservices.smsgateway.SMTPServiceDiscovery", 
                smtpServiceDiscovery, null);                   
        endptsvc.addListener("smsgateway", smsgateway);
        endptsvc.addListener("smtpdiscovery", smtpServiceDiscovery);
    }

    /**
     * Called when the bundle has to be stopped 
     * @param bc reference to the bundle's context
     */
    public void stop(BundleContext bc) throws Exception
    {
       smsgateway.abort(); 
    }
}