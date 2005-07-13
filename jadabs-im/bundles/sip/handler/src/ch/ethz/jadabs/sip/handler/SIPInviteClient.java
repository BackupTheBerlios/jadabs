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
public class SIPInviteClient {
	private static Logger logger = Logger.getLogger(SIPInviteClient.class.getName());
	
	private IMUserAgent sipUAClient;
	private int cseq = 0;
	private String SIPRemoteAddress;

	private IMReceiveProcessor sipRProc;
	
	public SIPInviteClient (IMUserAgent sipUAClient, IMReceiveProcessor sipRProc) {
		this.sipUAClient = sipUAClient;
		this.sipRProc = sipRProc;
	} 
	
	public void invite(String remoteURI) {
		try {
			/* when registering, the INVITE request should be sent to the domain, 
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
			
			// Where to send the request
			Address requestAddress = addressFactory.createAddress("sip:"+sipUAClient.getRegistrar());
	
			// CallId Header
			CallIdHeader callIdHeader = sipProvider.getNewCallId();
			
			// CSeq header
			CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(cseq, Request.INVITE);
			
			// To Header
			Address address = addressFactory.createAddress(remoteURI);
			ToHeader toHeader = headerFactory.createToHeader(address, null);
			
			//From Header
			address = addressFactory.createAddress(sipUAClient.getLocalURI());
			FromHeader fromHeader = headerFactory.createFromHeader(address, null);
			fromHeader.setTag(Utils.generateTag());
//			fromHeader.setParameter("epid", "37ab445b63"); // that's for fterrier@iptel.org
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
					Request.INVITE,
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
	            
//            SupportedHeader supHeader = headerFactory.createSupportedHeader("com.microsoft.rtc-multiparty");
//            request.setHeader(supHeader);
	        
//	        ExpiresHeader expiresHeader = headerFactory.createExpiresHeader(3600);
//	        request.setHeader(expiresHeader);
//	        
//            List list = new ArrayList();
//            list.add("RTC/1.3");
//            UserAgentHeader uaHeader = headerFactory.createUserAgentHeader(list);
//            request.setHeader(uaHeader);
            
            
            
            ContentTypeHeader header = headerFactory.createContentTypeHeader("application","sdp");
	        request.setContent("v=0\r\n" +
	        		"o=- 0 0 IN IP4 "+sipUAClient.getIpAddress()+"\r\n" +
	        		"s=session\r\n" + 
	        		"c=IN IP4 "+sipUAClient.getIpAddress()+"\r\n" +
	        		"t=0 0\r\n" +
	        		"m=x-ms-message "+sipUAClient.getPort()+" sip "+sipUAClient.getLocalURI()+"\r\n", header);
            
	        		
//	        ContentTypeHeader header = headerFactory.createContentTypeHeader("application","sdp");
//	        
//	        request.setContent("v=0\n" +
//	        		"o=linphone 123456 654321 IN IP4 127.0.0.1\n" +
//	        		"s=A conversation\n" +
//	        		"c=IN IP4 127.0.0.1\n" +
//	        		"t=0 0\n" +
//	        		"m=audio 7078 RTP/AVP 0 3 8 111 115 101\n" +
//	        		"b=AS:28\n" +
//	        		"a=rtpmap:0 PCMU/8000/1\n" +
//	        		"a=rtpmap:3 GSM/8000/1\n" +
//	        		"a=rtpmap:8 PCMA/8000/1\n" +
//	        		"a=rtpmap:111 speex/16000/1\n" +
//	        		"a=rtpmap:115 1015/8000/1\n" +
//	        		"a=rtpmap:101 telephone-event/8000\n" +
//	        		"a=fmtp:101 0-11", header);
	        
	     
	        // ****** SDP *********
//	        SdpFactory sdpFactory = SdpFactory.getInstance();
//	        Version version = sdpFactory.createVersion(0);
//	        Origin origin = sdpFactory.createOrigin("im-client", SdpFactory.getNtpTime(new Date()), 123, "IN", "IP4", sipUAClient.getLocalAddress());
//	        SessionName sessionName = sdpFactory.createSessionName("A chat");
//	        //Media media = sdpFactory.createMedia("application", sipUAClient.getLocalPort(), 1, "SIP", new Vector());
//	        Vector vv = new Vector();
//	        vv.addElement(""+0);
////	        vv.addElement(""+3);
////	        vv.addElement(""+8);
////	        vv.addElement(""+111);
////	        vv.addElement(""+115);
////	        vv.addElement(""+101);
//	        Media media = sdpFactory.createMedia("audio", sipUAClient.getLocalPort(), 1, "RTP/AVP", vv);
//	        Attribute attribute = sdpFactory.createAttribute("rtpmap", "0 PCMU/8000/1");
//	        
//	        SessionDescription sdp = sdpFactory.createSessionDescription();
//	        sdp.setVersion(version);
//	        sdp.setOrigin(origin);
//	        sdp.setSessionName(sessionName);
//	        Vector v = new Vector();
//			v.addElement(media);
//	        sdp.setMediaDescriptions(v);
//	        v= new Vector();
//	        v.addElement(attribute);
//	        sdp.setAttributes(v);
	        // *********************
	        
//	        ContentTypeHeadont(sdp, header);
	        
	        
			logger.debug("SENDING "+request);

			ClientTransaction clientTransaction = sipUAClient.getSipProvider().getNewClientTransaction(request);
			
			clientTransaction.sendRequest();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param responseEvent
	 */
	public void processResponse(ResponseEvent response) {
		logger.debug("Got Response !");
		ToHeader toHeader = (ToHeader) response.getResponse().getHeader(ToHeader.NAME);
		String address = toHeader.getAddress().getURI().toString();
//		logger.debug(response.getResponse());
		if ((response.getResponse().getStatusCode() >= 400) && (response.getResponse().getStatusCode()< 500)) {
			sipRProc.processInviteResponse(address, response.getClientTransaction(), false);
		}
		else {
			switch (response.getResponse().getStatusCode()) {
			case (Response.ACCEPTED):
			case (Response.OK):
				// WE GOT A 2XX RESPONSE !
				// Take the infos inside sdp payload, initiate chat communication with own protocol
				// Send to app ... ?
			logger.debug("It's a 2xx response !");
			ClientTransaction clientTransaction = response.getClientTransaction();
			if (clientTransaction == null) {
				logger.debug("OOPS, retransmission !");
			}
			else {
				sipRProc.processInviteResponse(address, response.getClientTransaction(), true);
			}
			break;
			case (Response.TRYING):
			case (Response.RINGING):
			case (Response.CALL_IS_BEING_FORWARDED):
			case (Response.QUEUED):
			case (Response.SESSION_PROGRESS):
				// WE GOT A 1XX RESPONSE !
				
				break;
			}
		}
	}

	/**
	 * @param requestEvent
	 */
	public void processRequest(RequestEvent requestEvent) {
		HeaderFactory headerFactory = sipUAClient.getHeaderFactory();
		AddressFactory addressFactory = sipUAClient.getAddressFactory();
		SipProvider sipProvider = sipUAClient.getSipProvider();
	    
		Request request = requestEvent.getRequest();
		ServerTransaction serverTransaction = requestEvent.getServerTransaction();
		try {
			if (serverTransaction == null) {
				logger.debug("*** New ServerTransaction ***");
				serverTransaction = sipUAClient.getSipProvider().getNewServerTransaction(request);
				Response response = sipUAClient.getMessageFactory().createResponse(200, request);
				
				SipURI sipURI = addressFactory.createSipURI(null,sipUAClient.getIpAddress());
	            sipURI.setPort(sipUAClient.getPort());
	            sipURI.setTransportParam(sipUAClient.getLocalProtocol());
	            Address contactAddress = addressFactory.createAddress(sipURI);
	            ContactHeader contactHeader=headerFactory.createContactHeader(contactAddress);
	            response.setHeader(contactHeader);
	            
	            ToHeader toHeader = (ToHeader)request.getHeader(SIPConstants.TO);
	            toHeader.setTag(Utils.generateTag());
	            response.setHeader(toHeader);
	            
//	            ViaHeader viaHeader = (ViaHeader)request.getHeader(SIPConstants.VIA);
//	            viaHeader.setBranch(Utils.generateBranchId());
//	            response.setHeader(viaHeader);
	            
//	            SupportedHeader supHeader = headerFactory.createSupportedHeader("com.microsoft.rtc-multiparty");
//	            response.setHeader(supHeader);
	            
	            ContentTypeHeader header = headerFactory.createContentTypeHeader("application","sdp");
		        
	            
	            // TODO ENORME
		        response.setContent("v=0\r\n" +
		        		"o=- 0 0 IN IP4 "+sipUAClient.getIpAddress()+"\r\n" +
		        		"s=session\r\n" + 
		        		"c=IN IP4 "+sipUAClient.getIpAddress()+"\r\n" +
		        		"t=0 0\r\n" +
		        		"m=message "+sipUAClient.getPort()+" sip "+sipUAClient.getLocalURI()+"\r\n", header);
	            
				logger.debug("sending response: ");
				logger.debug(response);
				
				Dialog dialog = serverTransaction.getDialog();
				serverTransaction.sendResponse(response);
				sipProvider.sendResponse(response);
				
				FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
		        String address = fromHeader.getAddress().getURI().toString();
	        	sipRProc.processInviteRequest(address, serverTransaction);
			}
			else {
				logger.debug("Transaction already exists !");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
