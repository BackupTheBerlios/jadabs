/*
 * Created on Aug 4, 2004
 * $Id: SMSService.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package ch.ethz.jadabs.mservices.smsservice;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;


/**
 * SMS Service implemented based on the Wireless Messaging API.
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class SMSService implements Listener, Runnable
{
    /** Apache Log4J logger to be used in the SMSService */
    private static Logger LOG = Logger.getLogger("SMSService");
    
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
    
    
    /**
     * Create new SMSService instance. 
     */
    public SMSService()
    {          
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
     * @param msg message to send
     */
    public void sendSM(Message msg)
    {
        LOG.debug("SMSService.handleMessage()");
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
            
            // to
            String to = new String(msg.getElement(SMS_TO_TAG).getData());           
            
            // body
            String body = new String(msg.getElement(SMS_BODY_TAG).getData());
            
            LOG.debug(" sending SM to "+to);
            LOG.debug(" message '"+body+"'");
            
            // now comes the WMApi part
            MessageConnection smsconn = null;
            try {
                String address =  "sms://"+to;
         	    	smsconn = (MessageConnection) Connector.open(address);
         	    	TextMessage txtmessage = 
         	        	(TextMessage)smsconn.newMessage(MessageConnection.TEXT_MESSAGE);
         	    	txtmessage.setPayloadText(body);
         	    	smsconn.send(txtmessage);
            } catch (IOException e) {
         		    LOG.error("cannot send short message: "+e.getMessage());
            } finally {
                 if (smsconn != null) {
                     try {
                         smsconn.close();
                     } catch (IOException ioe) { 
                         /* forget exception here */ 
                     }
                 }
            }
        }
    }
}
