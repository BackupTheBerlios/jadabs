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

import org.apache.log4j.Logger;

/**
 * @author sky
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ServerSipListener implements SipListener 
{

    private static Logger LOG = Logger.getLogger("test.ServerSipListener");
	
    
    public void processRequest(RequestEvent requestEvent) {
		LOG.debug("\n\n\n***************************************\nSipListener.processRequest\n");
		LOG.debug("------\nREQUEST\n------\n" + requestEvent.getRequest().toString());
		LOG.debug("------------\nREQUEST EVENT\n------------\n" + requestEvent.toString());
		LOG.debug("***************************************\n\n\n\n\n");
		try {	
		
			SipProvider sipProvider = (SipProvider)requestEvent.getSource();
			SipFactory factory = SipFactory.getInstance();
			MessageFactory messFactory = factory.createMessageFactory();
			
			if (requestEvent.getRequest().getMethod().equals(Request.REGISTER)) {
				LOG.debug("   ****   REGISTER Received   ****   ");
				ServerTransaction serverTransaction = requestEvent.getServerTransaction();
				if (serverTransaction == null) {
					serverTransaction = sipProvider.getNewServerTransaction(requestEvent.getRequest());
				}
				else {
					LOG.debug("Already received Transaction\n");
				}
				LOG.debug("***************************************\n");
				LOG.debug(serverTransaction.getState().toString()+"\n");
				LOG.debug("***************************************\n\n\n\n\n");
					
				Response response = messFactory.createResponse(100, requestEvent.getRequest());
				LOG.debug("\n\n\n***************************************\nSipListener.processRequest\n");
				LOG.debug("------\nREQUEST\n------\n" + requestEvent.getRequest().toString());
				LOG.debug("\n-----------\nSENDING RESPONSE\n------------\n"+ response.toString());
				LOG.debug("------------\nREQUEST EVENT\n------------\n" + requestEvent.toString());
				LOG.debug("***************************************\n\n\n\n\n");
				serverTransaction.sendResponse(response);
				LOG.debug("***************************************\n");
				LOG.debug(serverTransaction.getState().toString()+"\n");
				LOG.debug("***************************************\n\n\n\n\n");
				
				response = messFactory.createResponse(200, requestEvent.getRequest());
				serverTransaction.sendResponse(response);
				LOG.debug("***************************************\n");
				LOG.debug(serverTransaction.getState().toString()+"\n");
				LOG.debug("***************************************\n\n\n\n\n");
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
		//		LOG.debug("SipListener.processResponse\n" + responseEvent.toString());
	}

	public void processTimeout(TimeoutEvent timeoutEvent) {
		LOG.debug("***************************************\n");
		LOG.debug("TIMEOUT\n"+timeoutEvent.getTimeout().toString());
		LOG.debug("***************************************\n\n\n\n\n");
	}

}