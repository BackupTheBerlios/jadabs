package ch.ethz.jadabs.sip.handler;

import gov.nist.javax.sip.Utils;

import java.util.ArrayList;

import javax.sip.ClientTransaction;
import javax.sip.ResponseEvent;
import javax.sip.SipProvider;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ExpiresHeader;
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
 * Created on Nov 22, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

/**
 * @author franz
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class SIPRegistrationClient {
	private IMUserAgent sipUAClient;
	private IMReceiveProcessor imRProc;
	private int cseq = 0;
	
	static Logger logger = Logger.getLogger(SIPRegistrationClient.class);
	
	public SIPRegistrationClient(IMUserAgent sipUAClient, IMReceiveProcessor  imRProc) {
		this.sipUAClient = sipUAClient;
		this.imRProc = imRProc;
	}
	
	public void register(String sipLocalAddress, boolean unregister) throws Exception {
	    /* when registering, the REGISTER request should be sent to the registrar, 
	     * which is what is after the @ in the user SIP address 
	     * (eg bob@alice.com -> alice.com).
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
	    
	    // Where to send the request
	    Address requestAddress = addressFactory.createAddress("sip:"+sipUAClient.getRegistrar());
	    
	    // CallId Header
	    CallIdHeader callIdHeader = sipProvider.getNewCallId();
	    
	    // CSeq header
	    CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(cseq, Request.REGISTER);
	    
	    // To Header
	    Address address = addressFactory.createAddress(sipLocalAddress);
	    ToHeader toHeader = headerFactory.createToHeader(address, null);
	    
	    //From Header
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
	            Request.REGISTER,
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
	    
	    if (unregister) {
	        // Expires Header
	        ExpiresHeader expiresHeader = headerFactory.createExpiresHeader(0);
	        request.setHeader(expiresHeader);
	    }
	    
	    logger.debug("SENDINGs: "+request);
	    
	    ClientTransaction clientTransaction = sipUAClient.getSipProvider().getNewClientTransaction(request);
	    clientTransaction.sendRequest();
	}
	
	/**
	 * @param response
	 * @param clientTransaction
	 */
	public void processResponse(/*Response response, ClientTransaction clientTransaction*/ResponseEvent responseEvent) {
		// TODO see types
		Response response = responseEvent.getResponse();
		ClientTransaction clientTransaction = responseEvent.getClientTransaction();
		
		switch (response.getStatusCode()) {
		case (Response.ACCEPTED):
		case (Response.OK):
			// WE GOT A 2XX RESPONSE !
			ExpiresHeader expiresHeader = (ExpiresHeader) response
					.getHeader(ExpiresHeader.NAME);
			if (expiresHeader == null || expiresHeader.getExpires() != 0) {
				// Response to REGISTER
				imRProc.processRegister(true);
			}
			else {
				// Response to UNREGISTER
				imRProc.processRegister(false);
			}
			break;
		case (Response.TRYING):
		case (Response.RINGING):
		case (Response.CALL_IS_BEING_FORWARDED):
		case (Response.QUEUED):
		case (Response.SESSION_PROGRESS):
			// WE GOT A 1XX RESPONSE !
			break;
		case (Response.PROXY_AUTHENTICATION_REQUIRED):
		case (Response.UNAUTHORIZED):
			// Already treated by IMUserAgent
		}
	}
	
	
}