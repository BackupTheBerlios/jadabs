/*
 * Created on Aug 5, 2004
 * 
 * $Id: SMSGatewayService.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package ch.ethz.jadabs.mservices.smsgateway;

import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.EndpointService;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.MalformedURIException;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.mservices.smsservice.SMSService;


/**
 * SMS Service implemented based on the Wireless Messaging API.
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class SMSGatewayService implements Listener, Runnable
{
    /** Apache Log4J logger to be used in the SMSGatewayService */
    private static Logger LOG = Logger.getLogger("SMSGatewayService");
    
    /** name of message tag 'to' */
    public static final String SMS_TO_TAG = "to";
    
    /** name of message tag 'body' */
    public static final String SMS_BODY_TAG = "body";
    
    /**
     * vector representing queue (of Message Instances) 
     * of messages to be sent 
     */
    private Vector sendQueue = new Vector();
    
    /** flag indicating that shutdown of sender thread has been requested */
    private boolean aborting = false;
    
    /** EndpointService that is used to send messages to */
    private EndpointService endptsvc;
    
    /** SMTP Gateway discovery service */
    private SMTPServiceDiscovery discovery;
    
    /** Refernence to the local SMSService (WMA) */
    private SMSService smsservice;
    
    /** email address suffix used to contact the SMTP-SMS Gateway */
    private String emailSuffix;
    
    /** sender email address to be used in emails sent to SMTP-SMS Gateway */
    private String senderEmailAddress;
    
    
    /**
     * Create new SMSService instance.
     * @param discovery reference SMTP discovery instance
     * @param endptsvc reference to EndpointService instance
     * @param emailSuffix email address suffix used to contact the SMTP-SMS Gateway
     * @param senderEmailAddress sender email address to be used in 
     *        emails sent to SMTP-SMS Gateway
     */
    public SMSGatewayService(SMTPServiceDiscovery discovery, 
                             EndpointService endptsvc, 
                             String emailSuffix, String senderEmailAddress)
    {         
        this.discovery = discovery;
        this.endptsvc = endptsvc;
        this.emailSuffix = emailSuffix;
        this.senderEmailAddress = senderEmailAddress;
        
        // get SMS service
        ServiceReference sref = SMSGatewayActivator.bc.getServiceReference(
                "ch.ethz.jadabs.mservices.smsservice.SMSService");
        smsservice = (SMSService)SMSGatewayActivator.bc.getService(sref);
        
        // create sender thread and start it
        Thread senderThread = new Thread(this);
        senderThread.start();       
    }
    

    /**
     * Listener-Method is called by the JXME EndpointService when a 
     * message has to be sent over the SMSService. 
     *  
     * @param msg The JXME message to be send
     * @param args additional arguments (required by the listener interface but are 
     *             ignored in this case)
     */
    public void handleMessage(Message msg, String args)
    {
        sendSM(msg);
    }

    /** 
     * Send short message. Add <b>reference</b> to message object
     * at it the end of the send queue.  
     * @param msg Message to send
     */
    public void sendSM(Message msg)
    {
        // add message to the queue
        synchronized(this) {
            sendQueue.addElement(msg);
            this.notifyAll();
        }
    }
    
    
    /** 
     * This method's implementation is empty. 
     * @see ch.ethz.jadabs.jxme.Listener#handleSearchResponse(ch.ethz.jadabs.jxme.NamedResource)
     */
    public void handleSearchResponse(NamedResource res)
    {
        // empty
    }
    
    /**
     * Abort sending thread 
     */
    public void abort() 
    {
        synchronized(this) {
            aborting = true;
            this.notifyAll();
        }
    }
    
    /**
     * run body of the thread processing the sender queue
     */
    public void run() 
    {
        Message msg;
        while (!aborting) {
            synchronized(this) {
               while (sendQueue.size() == 0) {
                   // wait until there are messages to be sent
                   try {
                       this.wait();
                   } catch(InterruptedException e) { }
               }
               msg = (Message)sendQueue.firstElement();
               sendQueue.removeElementAt(0);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("processing message from queue.");
            }
            
            // check message
            if (msg.getElementCount()<2) {
                LOG.error("message requires at least two elements");
                continue;                
            }
            
            // get reciepient's phone number and message body 
            Element toElement = msg.getElement(0);
            Element bodyElement = msg.getElement(1);
            if (!toElement.getName().equals("to") || 
                !bodyElement.getName().equals("body")) {
                LOG.error("message does not have correctly formatted elements.");
                continue;
            }
            String phoneNumber = new String(toElement.getData());
            String body = new String(bodyElement.getData());

            
            if (discovery.isSMTPGatewayAvailable()) {
                // send via SMTP Gateway
                if (LOG.isDebugEnabled()) {
                    LOG.debug("send SM via SMTP Gateway");
                }
                sendSMviaSMTP(phoneNumber, body);
            } else {
                // send via WMA Gateway
                if (LOG.isDebugEnabled()) {
                    LOG.debug("send SM using WMA");
                }
                sendSMviaWMA(phoneNumber, body);
            }          
        }
    }
    
    /**
     * Send the specified message over the SMTP Gateway
     * @param phoneNumber recipient's phone number
     * @param message message to send 
     */
    public void sendSMviaSMTP(String phoneNumber, String message) 
    {
        Element[] elms = new Element[4];
        elms[0] = new Element("to", (phoneNumber+"@"+emailSuffix).getBytes(), 
                               null, Element.TEXTUTF8_MIME_TYPE);
        elms[1] = new Element("from", senderEmailAddress.getBytes(), 
                               null, Element.TEXTUTF8_MIME_TYPE);
        elms[2] = new Element("subject", "".getBytes(), 
                				   null, Element.TEXTUTF8_MIME_TYPE);
        elms[3] = new Element("body", message.getBytes(), 
                               null, Element.TEXTUTF8_MIME_TYPE);
        Message msg = new Message(elms);
        
        EndpointAddress endptlistener;
        try {
            endptlistener = new EndpointAddress(
                "btspp","anybody", -1, "smtpgateway");           
        } catch (MalformedURIException e ) {
            LOG.error("Malformed EndpointAddress: "+e.getMessage());
            return;
        }
        try {
            endptsvc.propagate(elms, endptlistener);
        } catch(IOException e) {
            LOG.error("Cannot propagate message over EndpointService: "+e.getMessage());           
        }   
        LOG.debug("message handed over to EndpointService layer.");
    }
    
    /**
     * Send the specified message over the Wireless Messaging Interface 
     * @param phoneNumber recipient's phone number
     * @param message message to be sent
     */
    public void sendSMviaWMA(String phoneNumber, String message) 
    {
        Element[] elms = new Element[2];
        elms[0] = new Element("to", phoneNumber.getBytes(), 
                               null, Element.TEXTUTF8_MIME_TYPE);       
        elms[1] = new Element("body", message.getBytes(), 
                               null, Element.TEXTUTF8_MIME_TYPE);
        Message msg = new Message(elms);
        smsservice.sendSM(msg);
    }
}
