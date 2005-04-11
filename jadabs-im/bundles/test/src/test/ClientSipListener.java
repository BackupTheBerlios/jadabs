/*
 * Created on Nov 16, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package test;
import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.TimeoutEvent;
import javax.sip.message.Response;

/**
 * @author franz
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ClientSipListener implements SipListener {

	/* (non-Javadoc)
	 * @see javax.sip.SipListener#processRequest(javax.sip.RequestEvent)
	 */
	public void processRequest(RequestEvent requestEvent) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.sip.SipListener#processResponse(javax.sip.ResponseEvent)
	 */
	public void processResponse(ResponseEvent responseEvent) {
		System.out.println("\n\n\n***************************************\nSipListener.processResponse\n");
		System.out.println("------\nRESPONSE\n------\n" + responseEvent.getResponse().toString());
		System.out.println("------------\nRESPONSE EVENT\n------------\n" + responseEvent.toString());
		System.out.println("***************************************\n\n\n\n\n");
		try {	
		
			SipProvider sipProvider = (SipProvider)responseEvent.getSource();
			
			
			if (responseEvent.getResponse().getStatusCode() == Response.TRYING) {
				System.out.println("   ****   TRYING Received   ****   ");
				ClientTransaction clientTransaction = responseEvent.getClientTransaction();
				if (clientTransaction == null) {
					System.out.println("Error ");
					System.exit(0);
				}
				System.out.println("***************************************\n");
				System.out.println(clientTransaction.getState().toString()+"\n");	
				System.out.println("***************************************\n\n\n\n\n");
				
				//Request request = clientTransaction.createAck();
				System.out.println("\n\n\n***************************************\nSipListener.processResponse\n");
				System.out.println("------\nRESPONSE\n------\n" + responseEvent.getResponse().toString());
				//System.out.println("\n-----------\nSENDING REQUEST\n------------\n"+ request.toString());
				System.out.println("------------\nREQUEST EVENT\n------------\n" + responseEvent.toString());
				System.out.println("***************************************\n\n\n\n\n");
				//	clientTransaction.
			}
			if (responseEvent.getResponse().getStatusCode() == Response.OK) {
				System.out.println("   ****   OK Received   ****   ");
				ClientTransaction clientTransaction = responseEvent.getClientTransaction();
				if (clientTransaction == null) {
					System.out.println("Error ");
					System.exit(0);
				}
				System.out.println("***************************************\n");
				System.out.println(clientTransaction.getState().toString()+"\n");	
				System.out.println("***************************************\n\n\n\n\n");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see javax.sip.SipListener#processTimeout(javax.sip.TimeoutEvent)
	 */
	public void processTimeout(TimeoutEvent timeoutEvent) {
		System.out.println("***************************************\n");
		System.out.println("TIMEOUT\n"+timeoutEvent.getTimeout().toString());	
		System.out.println("***************************************\n\n\n\n\n");
	}

}
