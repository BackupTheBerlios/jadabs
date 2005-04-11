package ch.ethz.jadabs.sip.handler;

import gov.nist.javax.sip.SIPConstants;
import gov.nist.javax.sip.Utils;

import java.util.ArrayList;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
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
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

/*
 * Created on Nov 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

/**
 * @author franz
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SIPMessageClient {
	private static Logger logger = Logger.getLogger(SIPMessageClient.class);
	
	private IMUserAgent sipUAClient;
	private int cseq = 0;
	private String SIPRemoteAddress;
	private IMReceiveProcessor imRProc;
	
	public SIPMessageClient(IMUserAgent sipUAClient, IMReceiveProcessor imRProc) {
		this.sipUAClient = sipUAClient;
		this.imRProc = imRProc;
	} 
	
	public void sendMessage(String sipFromAddress, String sipToAddress, String text) throws Exception{
	    /* when registering, the MESSAGE request should be sent to the domain, 
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
	    //				Address requestAddress = addressFactory.createAddress(SIPRemoteAddress);
	    //			}
	    
	    // CallId Header
	    CallIdHeader callIdHeader = sipProvider.getNewCallId();
	    
	    // CSeq header
	    CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(cseq, Request.MESSAGE);
	    
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
	            Request.MESSAGE,
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
	    
	    // Content
	    ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("text", "plain");
	    contentTypeHeader.setParameter("charset", "UTF-8");
	    request.setContent(text, contentTypeHeader);
	    
	    logger.debug("SENDING: MESSAGE");
	    logger.debug(request);
	    
	    ClientTransaction clientTransaction = sipUAClient.getSipProvider().getNewClientTransaction(request);
	    clientTransaction.sendRequest();
	}

	public void sendMessage(String sipFromAddress, String sipToAddress, String text, Dialog dialog) throws Exception{
	    /* when registering, the MESSAGE request should be sent to the domain, 
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
	    //				Address requestAddress = addressFactory.createAddress(SIPRemoteAddress);
	    //			}
	    
	    // CallId Header
	    CallIdHeader callIdHeader = dialog.getCallId();
	    
	    // CSeq header
	    CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(dialog.getLocalSequenceNumber()+1, Request.MESSAGE);
	    
	    // To Header
	    Address address = dialog.getRemoteParty();
	    ToHeader toHeader = headerFactory.createToHeader(address, dialog.getRemoteTag());

	    //From Header
	    address =  dialog.getLocalParty();
	    FromHeader fromHeader = headerFactory.createFromHeader(address, dialog.getLocalTag());
	    fromHeader.setParameter("epid", sipUAClient.getEpid());
		
	    // Via Headers
	    ArrayList viaHeaders = new ArrayList();
	    ViaHeader viaHeader = headerFactory.createViaHeader(
	            sipUAClient.getIpAddress(), sipUAClient.getPort(), sipUAClient.getLocalProtocol(), branchID);
	    viaHeaders.add(viaHeader);
	    
	    // MaxForward Headers
	    MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(16);
	    
	    Request request = messageFactory.createRequest(
	            requestAddress.getURI(),
	            Request.MESSAGE,
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
	    
//        List list = new ArrayList();
//        list.add("RTC/1.3");
//        UserAgentHeader uaHeader = headerFactory.createUserAgentHeader(list);
//        request.setHeader(uaHeader);
	    
	    // Content
	    ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("text", "plain");
	    contentTypeHeader.setParameter("charset", "UTF-8");
	    request.setContent(text, contentTypeHeader);
	    
	    logger.debug("SENDING: MESSAGE");
	    logger.debug(request);
	    
	    ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(request);
	    dialog.sendRequest(clientTransaction);
	}
	
	/**
	 * @param requestEvent
	 */
	public void processRequest(RequestEvent requestEvent) {
		try {
			
			// We've just received a MESSAGE Request, answer with an OK.
			// create a dialog.
			Request request = requestEvent.getRequest();
			ServerTransaction serverTransaction = requestEvent.getServerTransaction();

			// Let's test if we have already received the message
			// I think that's an ugly hack, but I always receive the same message twice ...
			// Don't wanna look at nist-sip in details...
			CallIdHeader callIdHeader = (CallIdHeader)request.getHeader(SIPConstants.CALL_ID);
			callIdHeader.getCallId();
			
			// Get different factories
			MessageFactory messageFactory = sipUAClient.getMessageFactory();
			HeaderFactory headerFactory = sipUAClient.getHeaderFactory();
			AddressFactory addressFactory = sipUAClient.getAddressFactory();
			SipProvider sipProvider = sipUAClient.getSipProvider();
			
			// BEGIN OF LISTENER
	        Object content = request.getContent();
			String text = null;
			if (content instanceof String) {
				text = (String) content;
			}
			else if (content instanceof byte[]) {
				text = new String((byte[]) content);
			} 
			imRProc.processMessageRequest(((FromHeader) request.getHeader(FromHeader.NAME)).getAddress().getURI().toString(),
					((ToHeader) request.getHeader(ToHeader.NAME)).getAddress().getURI().toString(),
					text);
			// END OF LISTENER TRANSMISSION

			// TODO look transaction management
//			if (serverTransaction != null) {
//				logger.debug("processRequest MESSAGE\ntransaction already exists");
//				//throw new IMException("Receiving message");
//			} 
			if (serverTransaction == null) {
				// get new Transaction
				serverTransaction = sipProvider.getNewServerTransaction(request);
			}
			// create Response
			Response response = messageFactory.createResponse(Response.OK,request);
			
			// Contact header
			SipURI sipURI = addressFactory.createSipURI(null,sipUAClient.getIpAddress());
			sipURI.setPort(sipUAClient.getPort());
			sipURI.setTransportParam(sipUAClient.getLocalProtocol());
			Address contactAddress = addressFactory.createAddress(sipURI);
			ContactHeader contactHeader=headerFactory.createContactHeader(contactAddress);
			request.setHeader(contactHeader);
			
			// From header tag
			ToHeader toHeader = (ToHeader)response.getHeader(SIPConstants.TO);
			toHeader.setTag(Utils.generateTag());
			
			// send response
			serverTransaction.sendResponse(response);
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param responseEvent
	 */
	public void processResponse(ResponseEvent response) {
		switch (response.getResponse().getStatusCode()) {
		case (Response.ACCEPTED):
		case (Response.OK) :
			// WE GOT A 2XX RESPONSE !
			break;
		case (Response.TRYING):
		case (Response.RINGING):
		case (Response.CALL_IS_BEING_FORWARDED):
		case (Response.QUEUED):
		case (Response.SESSION_PROGRESS):
			// WE GOT A 1XX RESPONSE !
			break;
		}
		if ((400<response.getResponse().getStatusCode()) && (response.getResponse().getStatusCode()<500)) {
		    imRProc.processMessageResponse(false);
		}
		
	}
}
