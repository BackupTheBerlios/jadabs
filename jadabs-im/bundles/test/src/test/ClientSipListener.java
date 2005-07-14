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

import org.apache.log4j.Logger;

/**
 * @author franz
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ClientSipListener implements SipListener 
{

    private static Logger LOG = Logger.getLogger("test.ClientSipListener");
	
    
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
		LOG.debug("\n\n\n***************************************\nSipListener.processResponse\n");
		LOG.debug("------\nRESPONSE\n------\n" + responseEvent.getResponse().toString());
		LOG.debug("------------\nRESPONSE EVENT\n------------\n" + responseEvent.toString());
		LOG.debug("***************************************\n\n\n\n\n");
		try {	
		
			SipProvider sipProvider = (SipProvider)responseEvent.getSource();
			
			
			if (responseEvent.getResponse().getStatusCode() == Response.TRYING) {
				LOG.debug("   ****   TRYING Received   ****   ");
				ClientTransaction clientTransaction = responseEvent.getClientTransaction();
				if (clientTransaction == null) {
					LOG.debug("Error ");
					System.exit(0);
				}
				LOG.debug("***************************************\n");
				LOG.debug(clientTransaction.getState().toString()+"\n");	
				LOG.debug("***************************************\n\n\n\n\n");
				
				//Request request = clientTransaction.createAck();
				LOG.debug("\n\n\n***************************************\nSipListener.processResponse\n");
				LOG.debug("------\nRESPONSE\n------\n" + responseEvent.getResponse().toString());
				//LOG.debug("\n-----------\nSENDING REQUEST\n------------\n"+ request.toString());
				LOG.debug("------------\nREQUEST EVENT\n------------\n" + responseEvent.toString());
				LOG.debug("***************************************\n\n\n\n\n");
				//	clientTransaction.
			}
			if (responseEvent.getResponse().getStatusCode() == Response.OK) {
				LOG.debug("   ****   OK Received   ****   ");
				ClientTransaction clientTransaction = responseEvent.getClientTransaction();
				if (clientTransaction == null) {
					LOG.debug("Error ");
					System.exit(0);
				}
				LOG.debug("***************************************\n");
				LOG.debug(clientTransaction.getState().toString()+"\n");	
				LOG.debug("***************************************\n\n\n\n\n");
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
		LOG.debug("***************************************\n");
		LOG.debug("TIMEOUT\n"+timeoutEvent.getTimeout().toString());	
		LOG.debug("***************************************\n\n\n\n\n");
	}

}
