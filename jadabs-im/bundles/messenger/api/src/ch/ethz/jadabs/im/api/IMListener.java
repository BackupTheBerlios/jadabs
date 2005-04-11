package ch.ethz.jadabs.im.api;
/*
 * Created on Nov 15, 2004
 *
 */
/**
 * The IMlistener has to be implemented by an Instant Messenger
 * User Agent to get notified about other Instant Messenger in its
 * neighbourhoud. 
 * 
 * @author andfrei
 * 
 */
public interface IMListener
{
	public static final int CONNECTION_FAILED = 1;

	public static final int CANNOT_DELIVER_MESSAGE = 2;
    /**
     * If IMService.register is called, used to notify app about 
     * status of registration process
     */
	void connectOK();
	
	/**
     * If IMService.unregister is called, used to notify app about 
     * status of unregistration process
     */
	void disconnectOK();
	
    /**
     * Once another new Instant Messenger has registered this IM gets
     * notified with the others sipaddres and its status.
     * 
     * @param sipaddress instant messenger who registered
     * @param status of the instant messenger
     */
    void buddyStatusChanged();
    
    void neighbourListChanged();
    /**
     * Once a gateway comes in the transmission field of our agent, this
     * method gets called... 
     * 
     * @param presence
     */
    void gatewayEvent(boolean presence);
    
    /**
     * If an instant message is received from the sender this method
     * is called.
     * 
     * @param sipaddress from address, who sent the message
     * @param msg instant message
     */
    void incomingMessage(String sipaddress, String message);
    
    /**
     * If an operation times out, for example, a message confirmation is not received.
     * 
     * @param message the timeout error message
     */
    void operationFailed(int type, String message);
}
