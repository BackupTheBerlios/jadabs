/*
 * Created on 11 Nov, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package test;
import java.util.ArrayList;
import java.util.Properties;

import javax.sip.ClientTransaction;
import javax.sip.ListeningPoint;
import javax.sip.SipFactory;
import javax.sip.SipProvider;
import javax.sip.SipStack;
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

/**
 * @author sky
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ClientMain {
	public static void main(String [] args) {
		try {
			SipFactory sipFactory = SipFactory.getInstance();
			sipFactory.setPathName("gov.nist");
			
			Properties props = new Properties();
			props.setProperty("javax.sip.IP_ADDRESS","172.30.57.44");
			props.setProperty("javax.sip.STACK_NAME","NISTv1.1");
			props.setProperty("javax.sip.OUTBOUND_PROXY", "172.30.57.45:5060/tcp");
			props.setProperty(
					"gov.nist.javax.sip.DEBUG_LOG",
					"shootistdebug.txt");
			props.setProperty(
					"gov.nist.javax.sip.SERVER_LOG",
					"shootistlog.txt");
			
			
			ClientSipListener clientSipListener = new ClientSipListener ();
			SipStack sipStack = sipFactory.createSipStack(props);
			ListeningPoint listeningPoint = sipStack.createListeningPoint(5059, ListeningPoint.TCP);
			SipProvider sipProvider = sipStack.createSipProvider(listeningPoint);
			sipProvider.addSipListener(clientSipListener);
			listeningPoint = sipStack.createListeningPoint(5059, ListeningPoint.UDP);
			sipProvider = sipStack.createSipProvider(listeningPoint);
			sipProvider.addSipListener(clientSipListener);
					
			
			MessageFactory messageFactory = sipFactory.createMessageFactory();
			AddressFactory addressFactory = sipFactory.createAddressFactory();
			HeaderFactory headerFactory = sipFactory.createHeaderFactory();
			
			Address address = addressFactory.createAddress("Jean-Luc Geering", addressFactory.createSipURI("jlg", "172.30.57.44"));
			
			CallIdHeader callIdHeader = sipProvider.getNewCallId();
			CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1, Request.REGISTER);
			ToHeader toHeader = headerFactory.createToHeader(address, null);
			FromHeader fromHeader = headerFactory.createFromHeader(address, null);
			ArrayList viaHeaders = new ArrayList();
			ViaHeader viaHeader = headerFactory.createViaHeader("192.168.1.101", 5059, "UDP", "groscaca");
			viaHeaders.add(viaHeader);
			MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);
			
			System.out.println(address);	
			System.out.println(address.getURI());
			System.out.println("");
			
			System.out.print(callIdHeader);
			System.out.print(cSeqHeader);
			System.out.print(fromHeader);
			System.out.print(toHeader);
			System.out.print(viaHeader);
			System.out.print(maxForwardsHeader);
			System.out.println("");
			
			Request request = messageFactory.createRequest(
					address.getURI(),
					Request.REGISTER,
					callIdHeader,
					cSeqHeader,
					fromHeader,
					toHeader,
					viaHeaders,
					maxForwardsHeader);
			
			System.out.println(request);
			System.out.println("");
//	        sipProvider.sendRequest(request);
	        ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(request);
	        clientTransaction.sendRequest();
	        System.out.println("***************************************\n");
	        System.out.println(clientTransaction.getState());
	        System.out.println("***************************************\n\n\n\n\n");
	        
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
	}

}
