/*
 * Created on Dec 15, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.ethz.jadabs.im.gui.handygui;

import org.apache.log4j.Logger;
import ch.ethz.jadabs.im.api.IMListener;

/**
 * The IMlistener has to be implemented by an Instant Messenger
 * User Agent to get notified about other Instant Messenger in its
 * neighbourhoud. 
 */
public class Listener implements IMListener
{   
    private static Logger LOG;
    
    /** reference to the main MIDlet */
    private HandyguiMIDlet midlet;
	
	public Listener(HandyguiMIDlet midlet) {
		this.midlet = midlet;
		
		LOG = Logger.getLogger("Listener"); 		
	}
	
	/**
	 * If IMService.register is called, used to notify app about 
	 * status of registration process
	 */
	public void connectOK() {
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke connectOK()");
        }
		midlet.connectOk();
	}

	/**
	 * If IMService.unregister is called, used to notify app about 
	 * status of registration process
	 */
	public void disconnectOK() {
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke disconnectOK()");
        }
		midlet.disconnectOk();
	}
	
	/**
	 * If an instant message is received from the sender this method
	 * is called.
	 * 
	 * @param sipaddress from address, who sent the message
	 * @param msg instant message
	 */
	public void incomingMessage(String sipaddress, String message) 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke incomingMessage()");
        }
		final String sipaddr = sipaddress;
		final String msg = message;
		midlet.incomingMessage(sipaddr, msg);          	
	}
	
    /**
     * Once a gateway comes in the transmission field of our agent, this
     * method gets called... 
     * 
     * @param presence
     */
	public void gatewayEvent(boolean presence) 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke gatewayEvent()");
        }
	    final boolean b = presence;
		midlet.gatewayEvent(b);       
	}

    /**
     * If an operation times out, for example, a message confirmation is not received.
     * 
     * @param message the timeout error message
     */
	public void operationFailed(int type, String message) 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke operationFailed()");
        }
	    final String msg = message;
		midlet.operationFailed(type,message);
	}


	/**
	 * Used to notify app about a status change of a buddy
	 */
	public void buddyStatusChanged() 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke buddyStatusChanged()");
        }
		midlet.buddyStatusChanged();
	}


	/**
	 * Used to notify app about a status change of a neighbour
	 */
	public void neighbourListChanged() 
	{
	    if (LOG.isDebugEnabled()) {
            LOG.debug("invoke neighbourListChanged()");
        }
		midlet.neighbourListChanged();
	}
}
