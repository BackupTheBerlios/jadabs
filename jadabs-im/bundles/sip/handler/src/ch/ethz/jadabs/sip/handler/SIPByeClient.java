/*
 * IMByeProcessing.java
 *
 * Created on September 25, 2002, 11:29 PM
 */

package ch.ethz.jadabs.sip.handler;

import gov.nist.javax.sip.Utils;

import java.util.ArrayList;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.RequestEvent;
import javax.sip.SipProvider;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
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
 * @author  olivier
 * @version 1.0
 */
public class SIPByeClient {
    private Logger logger = Logger.getLogger(SIPByeClient.class);
	
    private IMUserAgent sipUAClient;
    private IMReceiveProcessor imRProc;
    private int cseq = 0;

    /** Creates new IMByeProcessing */
    public SIPByeClient(IMUserAgent sipUAClient, IMReceiveProcessor imRPRoc) {
        this.sipUAClient=sipUAClient;
        this.imRProc = imRPRoc;
    }
    
    public void processRequest(RequestEvent requestEvent) {
        try{
            logger.debug("DEBUG: IMByeProcessing, Processing BYE in progress...");
	    
            Request request = requestEvent.getRequest();
//            ServerTransaction serverTransaction = requestEvent.getServerTransaction();
            
            MessageFactory messageFactory = sipUAClient.getMessageFactory();
            SipProvider sipProvider = sipUAClient.getSipProvider();
            
            
//            if (serverTransaction == null) {
//            	serverTransaction = sipProvider.getNewServerTransaction(request);
//            }
            
//            InstantMessagingGUI instantMessagingGUI=imUA.getInstantMessagingGUI();
//            ListenerInstantMessaging listenerInstantMessaging=
//            instantMessagingGUI.getListenerInstantMessaging();
//            ChatSessionManager chatSessionManager=listenerInstantMessaging.getChatSessionManager();
//            ChatSession chatSession=null;
//            String buddy=IMUtilities.getKey(request,"From");
//            if (chatSessionManager.hasAlreadyChatSession(buddy)) {
//                chatSession=chatSessionManager.getChatSession(buddy);
//                chatSessionManager.removeChatSession(buddy);
//                //chatSession.setExitedSession(true,"Your contact has exited the session");
//            }
//            else {
//                DebugIM.println("DEBUG: IMByeProcessing, processBye(), no active chatSession");
//            }

			// Send an OK
            Response response= messageFactory.createResponse(Response.OK, request);
//            serverTransaction.sendResponse(response);
            sipProvider.sendResponse(response);
            
            FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
	        String address = fromHeader.getAddress().getURI().toString();
            imRProc.processBye(address);
            
            logger.debug("SENDING RESPONSE: ");
            logger.debug(response);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
    public void processResponse(Response responseCloned,ClientTransaction clientTransaction ) {
    }
    
    
    public void sendBye(String localURI, String remoteURI, Dialog dialog) {
        // Send a Bye only if there were exchanged messages!!!    
    	try {
    		SipProvider sipProvider=sipUAClient.getSipProvider();
    		MessageFactory messageFactory=sipUAClient.getMessageFactory();
    		HeaderFactory headerFactory=sipUAClient.getHeaderFactory();
    		AddressFactory addressFactory=sipUAClient.getAddressFactory();
    		
    		// Request-URI:
    		Address requestAddress=addressFactory.createAddress(remoteURI);
    		
    		// Call-ID:
    		CallIdHeader callIdHeader=dialog.getCallId();
    		
    		// CSeq:
    		cseq++;
			String branchID = Utils.generateBranchId();
    		CSeqHeader cseqHeader=headerFactory.createCSeqHeader(cseq,"BYE");
    		
    		// To header:
    		Address toAddress=dialog.getRemoteParty();
    		ToHeader toHeader=headerFactory.createToHeader(toAddress, dialog.getRemoteTag());
    		
    		// From Header:
    		Address fromAddress=dialog.getLocalParty();
    		FromHeader fromHeader=headerFactory.createFromHeader(fromAddress, dialog.getLocalTag());
    		
    		//  Via header
			ArrayList viaHeaders = new ArrayList();
			ViaHeader viaHeader = headerFactory.createViaHeader(
					sipUAClient.getIpAddress(), sipUAClient.getPort(), sipUAClient.getLocalProtocol(), branchID);
			viaHeaders.add(viaHeader);
    		
    		// MaxForwards header:
    		MaxForwardsHeader maxForwardsHeader=headerFactory.createMaxForwardsHeader(10);
    		
    		Request request=messageFactory.createRequest(requestAddress.getURI(),"BYE",
    				callIdHeader,cseqHeader,fromHeader,toHeader,viaHeaders,maxForwardsHeader);
    		
			// Contact header
			SipURI sipURI = addressFactory.createSipURI(null,sipUAClient.getIpAddress());
	        sipURI.setPort(sipUAClient.getPort());
	        sipURI.setTransportParam(sipUAClient.getLocalProtocol());
	        Address contactAddress = addressFactory.createAddress(sipURI);
	        ContactHeader contactHeader=headerFactory.createContactHeader(contactAddress);
	        request.setHeader(contactHeader);
    		
    		ClientTransaction clientTransaction=sipProvider.getNewClientTransaction(request);
    		
    		clientTransaction.sendRequest();
    		logger.debug("SENDING BYE:");
    		logger.debug(request);
    		
    	}
    	catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
    
}
