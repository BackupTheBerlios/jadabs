/*
 * Created on Aug 5, 2004
 * $Id: SMTPServiceDiscovery.java,v 1.2 2004/11/19 14:31:07 afrei Exp $
 */
package ch.ethz.jadabs.mservices.smsgateway;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.EndpointService;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.MalformedURIException;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;


/**
 * This class implements the discovery mechanism for the SMTP-Service
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class SMTPServiceDiscovery implements Listener, Runnable
{
    /** Log4j logger to use in the discovery */
    private static Logger LOG = Logger.getLogger("SMTPServiceDiscovery");
    
    /** 
     * polling interval for discovery thread sending discovery 
     * messages to the SMTPGateway
     */
    private static final int DISCOVERY_INTERVAL_MS = 10000;
    
    /** expriation time of smtp discovery lease in ms */
    private static final int EXPIRATION_TIME_MS = 600000;
    
    /** EndpointService we are going to send messages over */
    private EndpointService endptsvc;
    
    /** true if we are about to shutdown the server */
    private boolean aborting = false;
        
    /** time (UTC) since last discovery reply was received */
    private long lastDiscoveryReceived = 0;
    
    /**
     * Create new SMTPServiceDiscovery and start discovry 
     * thread that tries to send discovery message to the 
     * server. 
     * @param endptsvc EndpointService we are going to send messages over
     */
    public SMTPServiceDiscovery(EndpointService endptsvc)
    {
        this.endptsvc = endptsvc;
        Thread discoveryThread = new Thread(this);
        discoveryThread.start();		// start discovery thread         
    }


    /**
     * This method is called by the JXME system whenever message to the 
     * discovery service is sent. 
     * 
     * @param message message that was received
     * @param listenerId string of listener's ID
     * @see ch.ethz.jadabs.jxme.Listener#handleMessage(ch.ethz.jadabs.jxme.Message,
     *      java.lang.String)
     */
    public void handleMessage(Message message, String listenerId)
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("handleMessage(..)");
        }
        if (message.getElementCount() < 1) {
            LOG.error("DISCOVERY_REPLY_MESSAGE must have at least one element");
            return;
        }
        Element typeElement = message.getElement(0);
        if (!typeElement.getName().equals("type")) {
            LOG.error("DISCOVERY_REPLY_MESSAGE must have 'type' element");
            return;
        }
        String typeString = new String(typeElement.getData());
        if (!typeString.equals("DISCOVERY_REPLY_MESSAGE")) {
            LOG.error("DISCOVERY_REPLY_MESSAGE has invalid type.");
            return;
        }
        // SMTPGateway is available
        lastDiscoveryReceived = (new Date()).getTime();  
        if (LOG.isDebugEnabled()) {
            LOG.debug("DISCOVERY_REPLY_MESSAGE received, SMTPGateway is available.");
        }
    }
    
    /**
     * Return true if SMTP is avaible for the application 
     * @return true if SMTP Gateway can be used for the application
     */
    public boolean isSMTPGatewayAvailable()
    {
        return ((new Date()).getTime() <= (lastDiscoveryReceived+EXPIRATION_TIME_MS));
    }


    /**
     * Called as resonce to the a search request.
     * @param namedResource resource that was search for
     * @see ch.ethz.jadabs.jxme.Listener#handleSearchResponse(ch.ethz.jadabs.jxme.NamedResource)
     */
    public void handleSearchResponse(NamedResource namedResource)
    {
        // empty       
    }


    /**
     * This method contains the run-body of the discovery thread
     */
    public void run()
    {
        EndpointAddress endptlistener;
        try {
            endptlistener = new EndpointAddress("btspp", 
                   "andybody", -1, "smtpgateway");
        } catch (MalformedURIException e) {
            LOG.error("EndpointAddress malformed: "+e.getMessage());
            return;
        }
        
        while (!aborting) {
            
            // send discovery message 
            Element[] elms = new Element[2];
            elms[0] = new Element("type", "DISCOVERY_MESSAGE".getBytes(), null, Element.TEXTUTF8_MIME_TYPE);
            elms[1] = new Element("replyto", "btspp://anybody/smtpdiscovery/".getBytes(), null, Element.TEXTUTF8_MIME_TYPE);
            try {
                endptsvc.propagate(elms, endptlistener);
            } catch(IOException e) {
                LOG.debug("cannot send DISCOVERY_MESSAGE: "+e.getMessage());
            }
            
            // sleep until next discovery cycle
            try {
                Thread.sleep(DISCOVERY_INTERVAL_MS);
            } catch (InterruptedException e) {
            	// do nothing
            }
        }
    }

	
    /** 
     * Abort discovery thread.
     */
    public void abort() 
    {
        synchronized(this) {
            aborting = true;
            this.notifyAll();
        }
    }
}
