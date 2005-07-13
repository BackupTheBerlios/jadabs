package ch.ethz.jadabs.im.sip.servertest;
/*
 * Created on 10 Nov, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

import java.util.Properties;

import javax.sip.ListeningPoint;
import javax.sip.SipFactory;
import javax.sip.SipProvider;
import javax.sip.SipStack;


/**
 * @author sky
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ServerMain {
	
	public static void main(String [] args) {
		try {
			SipFactory sipFactory = SipFactory.getInstance();
			sipFactory.setPathName("gov.nist");
			
			Properties props = new Properties();

			props.setProperty("javax.sip.IP_ADDRESS","127.0.0.1");
//			props.setProperty("javax.sip.IP_ADDRESS","129.132.210.67");
//			props.setProperty("javax.sip.IP_ADDRESS","172.30.57.44");
			props.setProperty("javax.sip.STACK_NAME","NISTv1.1");
			props.setProperty(
					"gov.nist.javax.sip.DEBUG_LOG",
					"shootistdebug.txt");
			props.setProperty(
					"gov.nist.javax.sip.SERVER_LOG",
					"shootistlog.txt");
			
			SipStack sipStack = sipFactory.createSipStack(props);
			ListeningPoint listeningPoint = sipStack.createListeningPoint(ListeningPoint.PORT_5060, ListeningPoint.TCP);
			SipProvider sipProvider = sipStack.createSipProvider(listeningPoint);
			ServerSipListener serverSipListener = new ServerSipListener();
			sipProvider.addSipListener(serverSipListener);
			
			listeningPoint = sipStack.createListeningPoint(ListeningPoint.PORT_5060, ListeningPoint.UDP);
			sipProvider = sipStack.createSipProvider(listeningPoint);
			sipProvider.addSipListener(serverSipListener);
			
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
	}

}
