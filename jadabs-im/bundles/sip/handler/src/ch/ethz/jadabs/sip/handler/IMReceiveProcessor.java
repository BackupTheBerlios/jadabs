/*
 * Created on 11-ene-2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.ethz.jadabs.sip.handler;

import javax.sip.ClientTransaction;
import javax.sip.ServerTransaction;
import javax.sip.message.Response;

/**
 * @author franz
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface IMReceiveProcessor {
	/**
	 * @param string
	 * @param text
	 */
	public void processMessageRequest(String fromSipUri, String toSipUri, String text);

	/**
	 * @param im_status_offline
	 */
	public void processRegister(boolean registered);

	/**
	 * @param request
	 */
	public void processSubscribeRequest(Response response, ServerTransaction transaction);

    /**
     * @param response
     * @param transaction
     */
    public void processSubscribeResponse(Response response, ClientTransaction transaction, boolean expired);

    /**
     * @param request
     * @param status
     */
    public void processNotifyRequest(String fromUri, String status, boolean expired);

    /**
     * @param b
     */
    public void processMessageResponse(boolean b);

	/**
	 * @param response
	 */
	public void processInviteRequest(String sipURI, ServerTransaction transaction);
	
	/**
	 * @param response
	 */
	public void processInviteResponse(String sipURI, ClientTransaction transaction, boolean isOK);
	
	public void processBye(String sipURI);
}
