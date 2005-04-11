 /*
 * IMSubscribeProcessing.java
 *
 * Created on September 26, 2002, 12:13 AM
 */

package ch.ethz.jadabs.sip.handler;

import gov.nist.javax.sip.SIPConstants;
import gov.nist.javax.sip.Utils;

import java.util.ArrayList;

import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipProvider;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;


/**
 * 
 * @author franz
 * @version 1.0
 */
public class SIPSubscribeClient
{
	static Logger logger = Logger.getLogger(SIPSubscribeClient.class);
	
    private IMUserAgent sipUAClient;
	private IMReceiveProcessor imRProc;
	
    private int cseq = 0;

    /** Creates new IMSubscribeProcessing */
    public SIPSubscribeClient(IMUserAgent sipUAClient, IMReceiveProcessor  imRProc)
    {
        this.sipUAClient = sipUAClient;
        this.imRProc = imRProc;
    }

    public void processResponse(ResponseEvent responseEvent) {
		Response response = responseEvent.getResponse();
		ClientTransaction clientTransaction = responseEvent.getClientTransaction();
		if (clientTransaction == null) {
		    logger.info("No client transaction for SUBSCRIBE response");
		    switch (responseEvent.getResponse().getStatusCode()) {
		    case (Response.ACCEPTED):
		    case (Response.OK):
		        // WE GOT A 2XX RESPONSE !
		        ExpiresHeader expiresHeader = (ExpiresHeader) response.getHeader(ExpiresHeader.NAME);
		    if (expiresHeader != null && expiresHeader.getExpires() == 0) {
		        logger.info("We got the OK for the unsubscribe... we are unsubscribed to "
		                +((ToHeader) response.getHeader(SIPConstants.TO)).getAddress().toString());
		        imRProc.processSubscribeResponse(response, responseEvent.getClientTransaction(), true);
		    }
		    else {
		        logger.info("We got the OK for the subscribe... we are subscribed to "
		                +((ToHeader) response.getHeader(SIPConstants.TO)).getAddress().toString());
		        //		((ToHeader) response.getHeader(SIPConstants.TO)).getAddress().toString());
		        imRProc.processSubscribeResponse(response, responseEvent.getClientTransaction(), false);
		    }
		    break;
		    case (Response.TRYING):
		    case (Response.RINGING):
		    case (Response.CALL_IS_BEING_FORWARDED):
		    case (Response.QUEUED):
		    case (Response.SESSION_PROGRESS):
		        // WE GOT A 1XX RESPONSE !
		        break;
		    case (Response.NOT_FOUND):
		    case (Response.TEMPORARILY_UNAVAILABLE):
		        // Buddy is not registered !
		        
		        break;
		    }
		}
		
//            if (cseqHeader.getMethod().equals("SUBSCRIBE"))
//            {
//                new AlertInstantMessaging("The presence server is not aware " + "of the buddy you want to add.");
//            } else
//            {
//                ListenerInstantMessaging listenerInstantMessaging = imGUI.getListenerInstantMessaging();
//                ChatSessionManager chatSessionManager = listenerInstantMessaging.getChatSessionManager();
//                ChatSession chatSession = null;
//                String toURL = IMUtilities.getKey(response, "To");
//                if (chatSessionManager.hasAlreadyChatSession(toURL))
//                {
//                    chatSession = chatSessionManager.getChatSession(toURL);
//                    chatSession.setExitedSession(true, "Contact not found");
//                }
//                new AlertInstantMessaging("Your instant message could not be delivered..."
//                        + " The contact is not available!!!");
                
		
//            DebugIM.println("Processing OK for SUBSCRIBE in progress...");
//
//            ExpiresHeader expiresHeader = (ExpiresHeader)
// responseCloned.getHeader(ExpiresHeader.NAME);
//            if (expiresHeader != null && expiresHeader.getExpires() == 0)
//            {
//                DebugIM.println("DEBUG, IMSubscribeProcessing, processOK(), we got" + " the OK for the unsubscribe...");
//            } else
//            {
//
//                // We have to create or update the presentity!
//                PresenceManager presenceManager = imUA.getPresenceManager();
//                String presentityURL = IMUtilities.getKey(responseCloned, "To");
//
//                Dialog dialog = clientTransaction.getDialog();
//                if (dialog != null)
//                    presenceManager.addPresentity(presentityURL, responseCloned, dialog);
//                else
//                {
//                    DebugIM.println("ERROR, IMSubscribeProcessing, processOK(), the"
//                            + " dialog for the SUBSCRIBE we sent is null!!!" + " No presentity added....");
//
//                }
//
//                // WE have to create a new Buddy in the GUI!!!
//                InstantMessagingGUI imGUI = imUA.getInstantMessagingGUI();
//                BuddyList buddyList = imGUI.getBuddyList();
//                if (!buddyList.hasBuddy(presentityURL))
//                {
//                    buddyList.addBuddy(presentityURL, "offline");
//                } else
//                {
//                    DebugIM.println("The buddy is already in the Buddy list...");
//                }
//            }
//            DebugIM.println("Processing OK for SUBSCRIBE completed...");
//        } catch (Exception ex)
//        {
//            ex.printStackTrace();
//        }
    }

    public void processRequest(RequestEvent requestEvent)
    {
    	try {
    		Request request = (Request)requestEvent.getRequest();
	    	ServerTransaction serverTransaction = requestEvent.getServerTransaction();
	    	// ???
	    	if (serverTransaction == null) {
	    	    logger.debug("!! creating new server transaction for subscribe !!");
	    	    serverTransaction = sipUAClient.getSipProvider().getNewServerTransaction(request);
	    	}
	    	
	    	MessageFactory messageFactory = sipUAClient.getMessageFactory();
	        HeaderFactory headerFactory = sipUAClient.getHeaderFactory();
	        AddressFactory addressFactory = sipUAClient.getAddressFactory();
	        
	        ExpiresHeader expiresHeader = (ExpiresHeader)request.getHeader(ExpiresHeader.NAME);
	        
	        if (expiresHeader != null && expiresHeader.getExpires()==0) {
	        	// Terminating an existing subscription
	        	Response response = messageFactory.createResponse(Response.OK, request);
	        	// To tag:
	        	ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
	            toHeader.setTag(Utils.generateTag());
	            // Expire header
	            expiresHeader = headerFactory.createExpiresHeader(0);
	            response.setHeader(expiresHeader);
	            
	        	serverTransaction.sendResponse(response);	        	
	        }
	        else {
	        	Response response = messageFactory.createResponse(Response.ACCEPTED, request);
	        	// Tag:
	        	ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
	            toHeader.setTag(Utils.generateTag());
	            
//	            Dialog dialog = serverTransaction.getDialog();
//	            FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
//	            String fromAddress = fromHeader.getAddress().getDisplayName();
//	            // let's add it to our subscriber list
//	            sipUAClient.getPresenceManager().addSubscriber(fromAddress, response, dialog);
	            
	        	serverTransaction.sendResponse(response);
	        	
	        	imRProc.processSubscribeRequest(response, serverTransaction);
	        }
    	}
	    catch (Exception e) {
	    	e.printStackTrace();
	    }
//    	try
//        {
//    		Request request = requestEvent.getRequest();
//    		ServerTransaction serverTransaction = requestEvent.getServerTransaction();
//    		
//            MessageFactory messageFactory = sipUAClient.getMessageFactory();
//            HeaderFactory headerFactory = sipUAClient.getHeaderFactory();
//            AddressFactory addressFactory = sipUAClient.getAddressFactory();
//
//            //********** Terminating subscriptions **********
//            ExpiresHeader expiresHeader = (ExpiresHeader) request.getHeader(ExpiresHeader.NAME);
//            if (expiresHeader != null && expiresHeader.getExpires() == 0)
//            {
//                if (dialog != null)
//                {
//                    //Terminating an existing subscription
//                    Response response = messageFactory.createResponse(Response.OK, request);
//                    serverTransaction.sendResponse(response);
//                    IMNotifyProcessing imNotifyProcessing = imUA.getIMNotifyProcessing();
//                    imNotifyProcessing.sendNotify(response, null, dialog);
//                    return;
//                } else
//                {
//                    //Terminating an non existing subscription
//                    Response response = messageFactory.createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST,
//                            request);
//                    serverTransaction.sendResponse(response);
//                    return;
//                }
//            }
//
//            //********** Non-terminating subscriptions ************
//
//            //send a 202 Accepted while waiting for authorization from user
//            Response response = messageFactory.createResponse(Response.ACCEPTED, request);
//            // Tag:
//            ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
//            if (toHeader.getTag() == null)
//                toHeader.setTag(new Integer((int) (Math.random() * 10000)).toString());
//            serverTransaction.sendResponse(response);
//            DebugIM.println(response.toString());
//
//            // We have to ask the user to authorize the guy to be in his buddy
//            // list
//            String presentityURL = IMUtilities.getKey(request, "From");
//            SipProvider sipProvider = imUA.getSipProvider();
//            InstantMessagingGUI imGUI = imUA.getInstantMessagingGUI();
//            boolean authorization = imGUI.getAuthorizationForBuddy(presentityURL);
//            if (authorization)
//            {
//                DebugIM.println("DEBUG: SubscribeProcessing, processSubscribe(), " + " Response 202 Accepted sent.");
//
//                // We have to create or update the subscriber!
//                PresenceManager presenceManager = imUA.getPresenceManager();
//                String subscriberURL = IMUtilities.getKey(request, "From");
//
//                if (dialog != null)
//                    presenceManager.addSubscriber(subscriberURL, response, dialog);
//                else
//                {
//                    DebugIM.println("ERROR, IMSubscribeProcessing, processSubscribe(), the"
//                            + " dialog for the SUBSCRIBE we received is null!!! No subscriber added....");
//                    return;
//                }
//
//                // Let's see if this buddy is in our buddy list
//                // if not let's ask to add him!
//                BuddyList buddyList = imGUI.getBuddyList();
//                ListenerInstantMessaging listenerIM = imGUI.getListenerInstantMessaging();
//                if (!buddyList.hasBuddy(subscriberURL))
//                {
//                    // Let's ask:
//                    listenerIM.addContact(subscriberURL);
//                }
//
//                /** ********************** send NOTIFY ************************* */
//                // We send a NOTIFY for any of our status but offline
//                String localStatus = listenerIM.getLocalStatus();
//                if (!localStatus.equals("offline"))
//                {
//                    IMNotifyProcessing imNotifyProcessing = imUA.getIMNotifyProcessing();
//                    Subscriber subscriber = presenceManager.getSubscriber(subscriberURL);
//                    //Response okSent=subscriber.getOkSent();
//
//                    subscriberURL = subscriber.getSubscriberName();
//
//                    String contactAddress = imUA.getIMAddress() + ":" + imUA.getIMPort();
//
//                    String subStatus = listenerIM.getLocalStatus();
//                    String status = null;
//                    if (subStatus.equals("offline"))
//                        status = "closed";
//                    else
//                        status = "open";
//                    String xmlBody = imNotifyProcessing.xmlPidfParser.createXMLBody(status, subStatus, subscriberURL,
//                            contactAddress);
//                    imNotifyProcessing.sendNotify(response, xmlBody, dialog);
//
//                }
//            } else
//            {
//                //User did not authorize subscription. Terminate it!
//                DebugIM.println("DEBUG, IMSubsribeProcessing, processSubscribe(), " + " Subscription declined!");
//                DebugIM.println("DEBUG, IMSubsribeProcessing, processSubscribe(), "
//                        + " Sending a Notify with Subscribe-state=terminated");
//
//                IMNotifyProcessing imNotifyProcessing = imUA.getIMNotifyProcessing();
//                if (dialog != null)
//                {
//                    imNotifyProcessing.sendNotify(response, null, dialog);
//                    DebugIM.println("DEBUG, IMSubsribeProcessing, processSubscribe(), "
//                            + " Sending a Notify with Subscribe-state=terminated");
//                } else
//                {
//                    DebugIM.println("ERROR, IMSubscribeProcessing, processSubscribe(), the"
//                            + " dialog for the SUBSCRIBE we received is null!!! \n" + "   No terminating Notify sent");
//
//                }
//                imNotifyProcessing.sendNotify(response, null, dialog);
//
//            }
//        } catch (Exception ex)
//        {
//            ex.printStackTrace();
//        }
    }

//    public void sendSubscribeToAllPresentities(Vector buddies, int expireTime)
//    {
//        try
//        {
//            DebugIM.println("DebugIM, IMSubscribeProcessing, sendSubscribeToAllPresentities(),"
//                    + " we have to subscribe to our buddies: let's send a SUBSCRIBE for each ones.");
//            for (int i = 0; i < buddies.size(); i++)
//            {
//                BuddyTag buddyTag = (BuddyTag) buddies.elementAt(i);
//
//                String buddyURI = buddyTag.getURI();
//
//                InstantMessagingGUI imGUI = imUA.getInstantMessagingGUI();
//                ListenerInstantMessaging listenerIM = imGUI.getListenerInstantMessaging();
//                String localURL = listenerIM.getLocalSipURL();
//                sendSubscribe(localURL, buddyURI, EXPIRED);
//            }
//        } catch (Exception ex)
//        {
//            ex.printStackTrace();
//        }
//    }

    public void sendSubscribe(String sipFromAddress, String sipToAddress, int expireTime)
    {
    	try {
			/* when registering, the SUBSCRIBE request should be sent to the domain, 
			 * which is what is after the @ in the remote user SIP address 
			 * (eg sip:bob@alice.com -> alice.com).
			 * To and From Headers are the local URI (bob@alice.com).
			 * Contact Header identifies the UserAgent. It is his real address (bob@"IPAddress").
			 * Via Header contains IP/port/protocol of the local useragent
			 */
			MessageFactory messageFactory = sipUAClient.getMessageFactory();
			HeaderFactory headerFactory = sipUAClient.getHeaderFactory();
			AddressFactory addressFactory = sipUAClient.getAddressFactory();
			SipProvider sipProvider = sipUAClient.getSipProvider();
	
			cseq++;
			String branchID = Utils.generateBranchId();
			
			// Where to send the request, proxy or not ??
//			if (sipUAClient.usesProxy()) {
				Address requestAddress = addressFactory.createAddress("sip:"+sipUAClient.getRegistrar());
//			}
//			else {
//				Address requestAddress = addressFactory.createAddress(sipToAddress);
//			}
			
			// CallId Header
			CallIdHeader callIdHeader = sipProvider.getNewCallId();
			
			// CSeq header
			CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(cseq, Request.SUBSCRIBE);
			
			// To Header
			Address address = addressFactory.createAddress(sipToAddress);
			ToHeader toHeader = headerFactory.createToHeader(address, null);
			
			//From Header
			address = addressFactory.createAddress(sipFromAddress);
			FromHeader fromHeader = headerFactory.createFromHeader(address, null);
			fromHeader.setTag(Utils.generateTag());
			
			// Via Headers
			ArrayList viaHeaders = new ArrayList();
			ViaHeader viaHeader = headerFactory.createViaHeader(
					sipUAClient.getIpAddress(), sipUAClient.getPort(), sipUAClient.getLocalProtocol(), branchID);
			viaHeaders.add(viaHeader);
			
			// MaxForward Headers
			MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);
			
			Request request = messageFactory.createRequest(
					requestAddress.getURI(),
					Request.SUBSCRIBE,
					callIdHeader,
					cSeqHeader,
					fromHeader,
					toHeader,
					viaHeaders,
					maxForwardsHeader);
			
			// Contact header
	        SipURI sipURI = addressFactory.createSipURI(null,sipUAClient.getIpAddress());
	        sipURI.setPort(sipUAClient.getPort());
	        sipURI.setTransportParam(sipUAClient.getLocalProtocol());
	        Address contactAddress = addressFactory.createAddress(sipURI);
	        ContactHeader contactHeader=headerFactory.createContactHeader(contactAddress);
	        request.setHeader(contactHeader);
	        
	        // Expires Header
	        ExpiresHeader expiresHeader = null;
            expiresHeader = headerFactory.createExpiresHeader(expireTime);
            request.setHeader(expiresHeader);
	
            // Event Header
            Header eventHeader = headerFactory.createHeader("Event", "presence");
            request.setHeader(eventHeader);
            
//          // Add Acceptw Header
            Header acceptHeader = headerFactory.createHeader("Accept", "application/pidf+xml");
            request.setHeader(acceptHeader);
            
	        logger.debug("SENDING: SUBSCRIBE");
	        logger.debug(request);
	        
			ClientTransaction clientTransaction = sipUAClient.getSipProvider().getNewClientTransaction(request);
			clientTransaction.sendRequest();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
    }

}