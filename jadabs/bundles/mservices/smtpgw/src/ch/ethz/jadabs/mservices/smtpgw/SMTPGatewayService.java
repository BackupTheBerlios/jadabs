/*
 * Copyright (c) 2003-2004, Jadabs project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * - Neither the name of the Jadabs project nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Created on Jul 21, 2004
 * 
 * $Id :$
 */
package ch.ethz.jadabs.mservices.smtpgw;

import java.io.IOException;
import java.util.Date;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.EndpointService;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.MalformedURIException;

import java.util.Properties;


/**
 * Mail-Gateway-Service for Jadabs using the Java-Mail-API
 * 
 * @author andfrei
 * @author Ren&eacute; M&uuml;ller
 */
public class SMTPGatewayService implements Listener
{
    /** Apache Log4J logger to be used in the SMTPGatewayService */
    private static Logger LOG = Logger.getLogger(SMTPGatewayService.class);
    
    /** name of message tag 'to' */
    public static final String EMAIL_TO_TAG = "to";
    
    /** name of message tag 'from' */
    public static final String EMAIL_FROM_TAG = "from";
    
    /** name of message tag 'subject' */
    public static final String EMAIL_SUBJECT_TAG = "subject";
    
    /** name of message tag 'body' */
    public static final String EMAIL_BODY_TAG = "body";
    
    /** ETH INF Mail server as a default server (for ETH users only) */
    public static final String SMTPServer_Default = "smtp.inf.ethz.ch";    
    
    /** name of SMTP server to be used */
    public String smtpHost;
    
    
    /**
     * Create new SMTPGateway. The associtated SMTP-Server is 
     * specified in the BundleContext-Property 
     * "ch.ethz.jadabs.mservices.smtpgw.smtphost".
     */
    public SMTPGatewayService()
    {
        this.smtpHost = 
            SMTPGatewayActivator.bc.getProperty("ch.ethz.jadabs.mservices.smtpgw.smtphost");        
    }
    
    /**
     * Send JXME message to the SMTP Gateway. Message must contain 
     * the following manatory elements "to", "from", "subject" and "body"
     * (all have MIME-Format Element.TEXTUTF8_MIME_TYPE).
     * @param message message to send
     */
    public void sendMailMessage(ch.ethz.jadabs.jxme.Message message)
    {
        boolean debug = true;
        
        // create some properties and get the default Session
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        
        
        // Get (create) session instance
        Session session = Session.getInstance(props, null);
        session.setDebug(debug);
        
        try
        {           
            // create a mail message
            Message msg = new MimeMessage(session);
            
            // from
            String from = new String(message.getElement(EMAIL_FROM_TAG).getData());
            msg.setFrom(new InternetAddress(from));
            // to
            String to = new String(message.getElement(EMAIL_TO_TAG).getData());
            InternetAddress[] address = { new InternetAddress(to)};
            msg.setRecipients(Message.RecipientType.TO, address);
            // subject
            String subject = new String(message.getElement(EMAIL_SUBJECT_TAG).getData());
            
            msg.setSubject(subject);
            // date
            msg.setSentDate(new Date());
            // body
            String body = new String(message.getElement(EMAIL_BODY_TAG).getData());

            msg.setText(body);            
            
            Transport.send(msg);
            
        } catch (MessagingException mex)
        {
            System.out.println("\n--Exception handling in msgsendsample.java");

            mex.printStackTrace();
            System.out.println();
            Exception ex = mex;
            do
            {
                if (ex instanceof SendFailedException)
                {
                    SendFailedException sfex = (SendFailedException) ex;
                    Address[] invalid = sfex.getInvalidAddresses();
                    if (invalid != null)
                    {
                        System.out.println("    ** Invalid Addresses");
                        if (invalid != null)
                        {
                            for (int i = 0; i < invalid.length; i++)
                                System.out.println("         " + invalid[i]);
                        }
                    }
                    Address[] validUnsent = sfex.getValidUnsentAddresses();
                    if (validUnsent != null)
                    {
                        System.out.println("    ** ValidUnsent Addresses");
                        if (validUnsent != null)
                        {
                            for (int i = 0; i < validUnsent.length; i++)
                                System.out.println("         " + validUnsent[i]);
                        }
                    }
                    Address[] validSent = sfex.getValidSentAddresses();
                    if (validSent != null)
                    {
                        System.out.println("    ** ValidSent Addresses");
                        if (validSent != null)
                        {
                            for (int i = 0; i < validSent.length; i++)
                                System.out.println("         " + validSent[i]);
                        }
                    }
                }
                System.out.println();
                if (ex instanceof MessagingException)
                    ex = ((MessagingException) ex).getNextException();
                else
                    ex = null;
            } while (ex != null);
        }
    }

    /**
     * Listener-Method is called by the JXME EndpointService when a 
     * message has to be sent over the SMTPGateway. 
     *  
     * @param msg The JXME message to be send
     * @param args additional arguments (required by the listener interface but are 
     *             ignored in this case)
     */
    public void handleMessage(ch.ethz.jadabs.jxme.Message msg, String args)
    {
        if (msg.getElementCount() < 1) {
            LOG.error("invalid message received, must contain at least one element");
            return;
        }
        
        // check for type element
        Element typeElement = msg.getElement(0);
        if (typeElement.getName().equals("type")) {
            String typeValue = new String(typeElement.getData());
            if (typeValue.equals("DISCOVERY_MESSAGE")) {
                if (msg.getElementCount() < 2) {
                    LOG.error("invalid DISCOVERY_MESSAGE received, two elements required.");
                    return;
                }
                Element replyElement = msg.getElement(1);
                if (!replyElement.getName().equals("replyto")) {
                    LOG.error("invalid DISCOVERY_MESSAGE received, element[1] must be 'replyto'.");
                    return;
                }
                String replyto = new String(replyElement.getData());
                sendSMTPDiscoveryReply(replyto);                
            } else {
                LOG.error("Received message has invalid 'type' element.");
                return;
            }
        } else {
            // type element is not present therefore it is a mail message
            sendMailMessage(msg);
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
     * Send reply to DISCOVERY_MESSAGE 
     * @param replyto URI to send reply message to
     */
    public void sendSMTPDiscoveryReply(String replyto)
    {
        ServiceReference sref = SMTPGatewayActivator.bc.getServiceReference("ch.ethz.jadabs.jxme.EndpointService");
        EndpointService endptsvc = (EndpointService)SMTPGatewayActivator.bc.getService(sref);
        EndpointAddress endptlistener;
        try {
            endptlistener = new EndpointAddress(replyto);
        } catch (MalformedURIException e) {
            LOG.error("invalid reply URI in DISCOVERY_MESSAGE");
            return;
        }
        LOG.debug("sendSMTPDiscoveryReply to "+replyto);
        Element[] elms = new Element[1];
        elms[0] = new Element("type", "DISCOVERY_REPLY_MESSAGE".getBytes(), null, Element.TEXTUTF8_MIME_TYPE);        
        try {
            endptsvc.propagate(elms, endptlistener);
        } catch(IOException e) {
            LOG.debug("cannot send DISCOVERY_MESSAGE_REPLY: "+e.getMessage());
        }
    }
}
