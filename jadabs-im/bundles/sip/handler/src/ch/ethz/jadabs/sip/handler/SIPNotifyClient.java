/*
 * IMNotifyProcessing.java
 *
 * Created on September 26, 2002, 12:14 AM
 */

package ch.ethz.jadabs.sip.handler;

import gov.nist.javax.sip.Utils;

import java.util.Vector;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipProvider;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.sip.handler.cpimparser.NoteTag;
import ch.ethz.jadabs.sip.handler.cpimparser.TupleTag;
import ch.ethz.jadabs.sip.handler.cpimparser.XMLcpimParser;
import ch.ethz.jadabs.sip.handler.pidfparser.AddressTag;
import ch.ethz.jadabs.sip.handler.pidfparser.AtomTag;
import ch.ethz.jadabs.sip.handler.pidfparser.MSNSubStatusTag;
import ch.ethz.jadabs.sip.handler.pidfparser.PresenceTag;
import ch.ethz.jadabs.sip.handler.pidfparser.XMLpidfParser;

/**
 * 
 * @author olivier
 * @version 1.0
 */
public class SIPNotifyClient
{
    private static Logger logger = Logger.getLogger(SIPNotifyClient.class.getName());
    
    private XMLpidfParser xmlPidfParser;
    
    private XMLcpimParser xmlCpimParser;
    
    private IMUserAgent sipUAClient;

    private IMReceiveProcessor imRProc;
    
    private int cseq = 0;

    /** Creates new IMNotifyProcessing */
    public SIPNotifyClient(IMUserAgent sipUAClient, IMReceiveProcessor  imRProc)
    {
        this.sipUAClient = sipUAClient;
        this.imRProc = imRProc;
        xmlPidfParser = new XMLpidfParser();
        xmlCpimParser = new XMLcpimParser();
    }

    public void processOk(Response response, ClientTransaction clientTransaction)
    {
//        DebugIM.println("Processing OK received for a NOTIFY ");
//
//        // We have to particular processing to do with the OK..
//        DebugIM.println("OK processed!!!");
    }

    public void processRequest(RequestEvent requestEvent) {
        try {
            Request request = requestEvent.getRequest();

            MessageFactory messageFactory = sipUAClient.getMessageFactory();
            ServerTransaction serverTransaction = requestEvent
                    .getServerTransaction();

            FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
            String fromAddress = fromHeader.getAddress().getURI().toString();
            
            if (serverTransaction == null) {
            	serverTransaction = sipUAClient.getSipProvider().getNewServerTransaction(request);
            }
            
            EventHeader eHeader = (EventHeader) request.getHeader(EventHeader.NAME);
            if (eHeader.getEventType().equalsIgnoreCase("presence")) {
                SubscriptionStateHeader subStateHeader = (SubscriptionStateHeader)request.getHeader(SubscriptionStateHeader.NAME);
                if (subStateHeader.getState().equalsIgnoreCase(SubscriptionStateHeader.TERMINATED)) {
                    Response response = messageFactory.createResponse(Response.OK, request);
                    serverTransaction.sendResponse(response);
//                    imRProc.processNotifyRequest(fromAddress, null, true);
                }
                else {
                    Object content = request.getContent();
                    String text = null;
                    if (content instanceof String) {
                        text = (String) content;
                    } else if (content instanceof byte[]) {
                        text = new String((byte[]) content);
                    } else {
                                        logger.debug(" Error, the body of the request is unknown!!");
                        //                logger
                        //                        .debug("ERROR, IMNotifyProcessing, process(): "
                        //                                + " pb with the xml body, 488 Not Acceptable Here replied");
                        Response response = messageFactory.createResponse(
                                Response.NOT_ACCEPTABLE, request);
                        serverTransaction.sendResponse(response);
                        return;
                    }
                    
                    if (text != null && !text.trim().equals("")) {
                        // we have to parse the XML body!!!!
                        //                try {
                        ContentTypeHeader contentTypeHeader = (ContentTypeHeader) request
                        .getHeader(ContentTypeHeader.NAME);
                        String xmlType = contentTypeHeader.getContentSubType();
                        
                        //                    logger
                        //                            .debug("DEBUG, IMNotifyProcessing, process(), the XML body format"
                        //                                    + " is: " + xmlType);
                        String status = null;
                        if (xmlType.equals("xpidf+xml")) {
                            xmlPidfParser.parsePidfString(text);
                            PresenceTag presenceTag = xmlPidfParser
                            .getPresenceTag();
                            //                        if (presenceTag == null)
                            ////                            logger.debug("ERROR: The presence Tag is null!!!");
                            //                        else
                            ////                            logger.debug("the parsed body:"
                            ////                                    + presenceTag.toString());
                            
                            Vector atomTagList = presenceTag.getAtomTagList();
                            AtomTag atomTag = (AtomTag) atomTagList.firstElement();
                            AddressTag addressTag = atomTag.getAddressTag();
                            MSNSubStatusTag msnSubStatusTag = addressTag
                            .getMSNSubStatusTag();
                            status = msnSubStatusTag.getMSNSubStatus();
                        } else if (xmlType.equals("pidf+xml")) {
                            
                            xmlCpimParser.parseCPIMString(text.trim());
                            ch.ethz.jadabs.sip.handler.cpimparser.PresenceTag presenceTag = xmlCpimParser
                            .getPresenceTag();
                            
                            //                        if (presenceTag == null)
                            ////                            logger.debug("ERROR: The presence Tag is null!!!");
                            //                        else
                            ////                            logger.debug("the parsed body:"
                            ////                                    + presenceTag.toString());
                            
                            Vector tupleTagList = presenceTag.getTupleTagList();
                            NoteTag noteTag = null;
                            if (tupleTagList.size() > 0) {
                                TupleTag tupleTag = (TupleTag) tupleTagList
                                .firstElement();
                                noteTag = tupleTag.getNoteTag();
                            }
                            if (noteTag != null)
                                status = noteTag.getNote();
                            else
                                status = "offline";
                            //                        logger.debug("status:" + status);
                        }
                        
                        logger.debug("Status was: "+status);
                        // Send an OK
                        Response response = messageFactory.createResponse(
                                Response.OK, request);
                        serverTransaction.sendResponse(response);
                        logger.debug("OK replied to the NOTIFY");
                        
                        //                // We have to update the buddy list!!!
                        //                BuddyList buddyList = imGUI.getBuddyList();
                        //                buddyList.changeBuddyStatus(fromURL, status);
                        
                        //                // We can update the information field for the
                        //                // ChatFrame:
                        //                ChatSessionManager chatSessionManager = listenerIM
                        //                        .getChatSessionManager();
                        //                ChatSession chatSession = chatSessionManager
                        //                        .getChatSession(fromURL);
                        //                if (chatSession == null) {
                        //                    DebugIM
                        //                            .println("DEBUG, IMNotifyProcessing, processNotify(), "
                        //                                    + " the chat session does not exist, no need to update
                        // the
                        // chatFrame!!!");
                        //                } else {
                        //                    DebugIM
                        //                            .println("DEBUG, IMNotifyProcessing, processNotify(), "
                        //                                    + " the chat session does exist, need to update the
                        // chatFrame!!!");
                        //                    chatSession.setInfo("The contact is " + status);
                        //
                        //                }
                        
                        // WE have to update the presentity list: the status has
                        // changed!!!

                        imRProc.processNotifyRequest(fromAddress, status, false);
                        
                        //                } 
                        //                catch (Exception e) {
                        //                    //e.printStackTrace();
                        //                    logger
                        //                            .debug("ERROR, IMNotifyProcessing, process(): "
                        //                                    + " pb with the xml body, 488 Not Acceptable Here replied");
                        //                    Response response = messageFactory.createResponse(
                        //                            Response.NOT_ACCEPTABLE_HERE, request);
                        //                    serverTransaction.sendResponse(response);
                        //
                        //                    e.printStackTrace();
                        //                }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendNotifyToAllSubscribers(String status, String subStatus)
    {
//        try
//        {
//            // We have to get all our subscribers and send them a NOTIFY!
//            PresenceManager presenceManager = imUA.getPresenceManager();
//            Vector subscribersList = presenceManager.getAllSubscribers();
//            DebugIM.println("DEBUG, IMNotifyProcessing, sendNotifyToAllSuscribers(),"
//                    + " we have to notify our SUBSCRIBERS: let's send a NOTIFY for each one "
//                    + "of them (subscribersList: " + subscribersList.size() + ")!!!");
//            for (int i = 0; i < subscribersList.size(); i++)
//            {
//                Subscriber subscriber = (Subscriber) subscribersList.elementAt(i);
//
//                Response okSent = subscriber.getOkSent();
//                String subscriberName = subscriber.getSubscriberName();
//
//                String contactAddress = imUA.getIMAddress() + ":" + imUA.getIMPort();
//                String xmlBody = null;
//                //if (!status.equals("closed") )
//                xmlBody = xmlPidfParser.createXMLBody(status, subStatus, subscriberName, contactAddress);
//
//                Dialog dialog = subscriber.getDialog();
//                if (dialog == null)
//                {
//                    DebugIM.println("ERROR, sendNotifyToAllSubscribers(), PB to "
//                            + "retrieve the dialog, NOTIFY not sent!");
//                } else
//                    sendNotify(okSent, xmlBody, dialog);
//            }
//
//            //Send a PUBLISH request to our PA
//            IMRegisterProcessing imRegisterProcessing = imUA.getIMRegisterProcessing();
//            if (imRegisterProcessing.isRegistered())
//            {
//                //Fetching the sip-uri from gui. Isn't that a bit odd?
//                IMPublishProcessing imPublishProcessing = imUA.getIMPublishProcessing();
//                javax.swing.JTextField guiSipURI = imUA.getInstantMessagingGUI().getLocalSipURLTextField();
//                String localSipURI = guiSipURI.getText();
//                int colonIndex = localSipURI.indexOf(':');
//                String localURI = localSipURI.substring(colonIndex + 1); //strip
//                                                                         // off
//                                                                         // "sip:"
//                imPublishProcessing.sendPublish(localURI, subStatus); //"fosfor@nitrogen.epact.se"
//            }
//
//        } catch (Exception ex)
//        {
//            ex.printStackTrace();
//        }
    }

    public void sendNotify (Dialog dialog, String strStatus)
    {
        try
        {
            
            // we create the Request-URI: the one of the proxy
            HeaderFactory headerFactory = sipUAClient.getHeaderFactory();
            AddressFactory addressFactory = sipUAClient.getAddressFactory();
            MessageFactory messageFactory = sipUAClient.getMessageFactory();
            SipProvider sipProvider = sipUAClient.getSipProvider();

            cseq++;
        	String branchID = Utils.generateBranchId();
        	
//          Where to send the request, proxy or not ??
//			if (sipUAClient.usesProxy()) {
				Address requestAddress = addressFactory.createAddress("sip:"+sipUAClient.getRegistrar());
//			}
//			else {
//				Address requestAddress = addressFactory.createAddress(sipToAddress);
//			}

			// CallId Header
			CallIdHeader callIdHeader = dialog.getCallId();

			// CSeq header
			CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(cseq, Request.NOTIFY);

			// To Header
			ToHeader toHeader = headerFactory.createToHeader(dialog.getRemoteParty(), dialog.getRemoteTag());

			//From Header
			FromHeader fromHeader = headerFactory.createFromHeader(dialog.getLocalParty(), dialog.getLocalTag());
			
			// Via header
            String branchId = Utils.generateBranchId();
            ViaHeader viaHeader = headerFactory.createViaHeader(sipUAClient.getIpAddress(), sipUAClient.getPort(), sipUAClient.getLocalProtocol(), branchID);
            Vector viaList = new Vector();
            viaList.addElement(viaHeader);

            // MaxForwards header:
            MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);

//          Content-Type:
            ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "xpidf+xml");

            String xmlBody;
            if (strStatus.equals("offline")) {
                xmlBody = xmlPidfParser.createXMLBody("closed", strStatus, dialog.getRemoteParty().getURI().toString(), sipUAClient.getIpAddress()+":"+sipUAClient.getPort());
            }
            else {
                xmlBody = xmlPidfParser.createXMLBody("open", strStatus, dialog.getRemoteParty().getURI().toString(), sipUAClient.getIpAddress()+":"+sipUAClient.getPort());
            }
                
            Request request = messageFactory.createRequest(requestAddress.getURI(), "NOTIFY", callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaList, maxForwardsHeader, contentTypeHeader, xmlBody);
            
            // Event header:
            Header header = headerFactory.createHeader("Event", "presence");
            request.setHeader(header);
            
            // WE have to add a new Header: "Subscription-State"
            // Modified by Henrik Leion
            String subscriptionState;
            subscriptionState = "active";
            
            header = headerFactory.createHeader("Subscription-State", subscriptionState);
            request.setHeader(header);
            
            ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(request);

	        logger.debug("SENDING: NOTIFY");
	        logger.debug(request);
            
	        dialog.sendRequest(clientTransaction);
        } 
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}