/*
 * MessageListener.java
 * 
 * Created on Mar 2, 2004
 *
 */
package examples.messaging;

/**
 * @author Jean Deruelle <jean.deruelle@nist.gov>
 *
 * <a href="{@docRoot}/uncopyright.html">This code is in the public domain.</a>
 */
public interface SipStateListener {
	public static final int NOT_REGISTERED=0;
	public static final int REGISTERED=1;
	public static final int CALLING=2;	
	public static final int IN_A_CALL=3;
	public static final int INCOMING_CALL=4;
	public static final int IDLE=5;
	/**
	 * Notify the implementing class of a change in a sip state of the application 
	 * @param state - the new sip state
	 */
	public void sipStateChanged(int state);
	
	/**
	 * Notify the implementing class of a change in a presence state of 
	 * one of the buddies
	 * @param presenceState - the new presence State
	 * @param buddy - the buddy whose the presence state has changed
	 */
	public void sipBuddyPresenceChanged(String presenceState, String buddyURI);
}
