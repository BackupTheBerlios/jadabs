/*
 * Created on 10 Nov, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package test;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.TimeoutEvent;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

/**
 * @author sky
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ServerSipListener implements SipListener {
	public void processRequest(RequestEvent requestEvent) {
		System.out.println("\n\n\n***************************************\nSipListener.processRequest\n");
		System.out.println("------\nREQUEST\n------\n" + requestEvent.getRequest().toString());
		System.out.println("------------\nREQUEST EVENT\n------------\n" + requestEvent.toString());
		System.out.println("***************************************\n\n\n\n\n");
		try {	
		
			SipProvider sipProvider = (SipProvider)requestEvent.getSource();
			SipFactory factory = SipFactory.getInstance();
			MessageFactory messFactory = factory.createMessageFactory();
			
			if (requestEvent.getRequest().getMethod().equals(Request.REGISTER)) {
				System.out.println("   ****   REGISTER Received   ****   ");
				ServerTransaction serverTransaction = requestEvent.getServerTransaction();
				if (serverTransaction == null) {
					serverTransaction = sipProvider.getNewServerTransaction(requestEvent.getRequest());
				}
				else {
					System.out.println("Already received Transaction\n");
				}
				System.out.println("***************************************\n");
				System.out.println(serverTransaction.getState().toString()+"\n");
				System.out.println("***************************************\n\n\n\n\n");
					
				Response response = messFactory.createResponse(100, requestEvent.getRequest());
				System.out.println("\n\n\n***************************************\nSipListener.processRequest\n");
				System.out.println("------\nREQUEST\n------\n" + requestEvent.getRequest().toString());
				System.out.println("\n-----------\nSENDING RESPONSE\n------------\n"+ response.toString());
				System.out.println("------------\nREQUEST EVENT\n------------\n" + requestEvent.toString());
				System.out.println("***************************************\n\n\n\n\n");
				serverTransaction.sendResponse(response);
				System.out.println("***************************************\n");
				System.out.println(serverTransaction.getState().toString()+"\n");
				System.out.println("***************************************\n\n\n\n\n");
				
				response = messFactory.createResponse(200, requestEvent.getRequest());
				serverTransaction.sendResponse(response);
				System.out.println("***************************************\n");
				System.out.println(serverTransaction.getState().toString()+"\n");
				System.out.println("***************************************\n\n\n\n\n");
			}
//			if (requestEvent.getRequest().getMethod().equals(requestEvent.getRequest().ACK)) {
//				
//			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}	
	public void processResponse(ResponseEvent responseEvent) {
		//		System.out.println("SipListener.processResponse\n" + responseEvent.toString());
	}

	public void processTimeout(TimeoutEvent timeoutEvent) {
		System.out.println("***************************************\n");
		System.out.println("TIMEOUT\n"+timeoutEvent.getTimeout().toString());
		System.out.println("***************************************\n\n\n\n\n");
	}

}