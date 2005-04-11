/*
 * Created on 23 janv. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ch.ethz.jadabs.sip.handler;

import gov.nist.javax.sip.Utils;

import java.util.ArrayList;

import javax.sip.Dialog;
import javax.sip.SipProvider;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;

import org.apache.log4j.Logger;

/**
 * @author Franz Terrier
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SIPAckClient {
	private Logger logger = Logger.getLogger(SIPAckClient.class);
	
	private IMUserAgent sipUAClient;
	private int cseq = 0;
	
	public SIPAckClient(IMUserAgent sipUAClient) {
		this.sipUAClient = sipUAClient;
	}
	
	public void sendAck(String toURI, Dialog dialog) {
		try {
			MessageFactory messageFactory = sipUAClient.getMessageFactory();
			HeaderFactory headerFactory = sipUAClient.getHeaderFactory();
			AddressFactory addressFactory = sipUAClient.getAddressFactory();
			SipProvider sipProvider = sipUAClient.getSipProvider();
			
			cseq++;
			String branchID = Utils.generateBranchId();
			
			// Where to send the request
			Address requestAddress = addressFactory.createAddress("sip:"+sipUAClient.getRegistrar());
			
			// CallId Header
			CallIdHeader callIdHeader = dialog.getCallId();
			
			// CSeq header
			CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(dialog.getLocalSequenceNumber(), Request.ACK);
			
			// To Header
			Address address = dialog.getRemoteParty();
			ToHeader toHeader = headerFactory.createToHeader(address, null);
			toHeader.setTag(dialog.getRemoteTag());
			
			//From Header
			address = dialog.getLocalParty();
			FromHeader fromHeader = headerFactory.createFromHeader(address, null);
			fromHeader.setTag(dialog.getLocalTag());
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
					Request.ACK,
					callIdHeader,
					cSeqHeader,
					fromHeader,
					toHeader,
					viaHeaders,
					maxForwardsHeader);
			
	        
			logger.debug("SENDING "+request);

			dialog.sendAck(request);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
