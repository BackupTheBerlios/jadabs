/*
 * Created on Dec 5, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.ethz.jadabs.im.sip;


import org.apache.log4j.Logger;

import ch.ethz.jadabs.im.api.IMListener;

/**
 * @author franz
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SIPIMListener implements IMListener {

		private static Logger logger = Logger.getLogger(SIPIMListener.class);
	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.api.IMListener#imRegistered(java.lang.String, int)
	 */
	public void statusChanged(String sipaddress, int status, String imType) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.api.IMListener#process(java.lang.String, java.lang.String)
	 */
	public void incomingMessage(String sipaddress, String msg) {
		logger.info ("Message received from: "+sipaddress+"\n"+msg) ;
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.api.IMListener#statusChanged(int)
	 */
	public void connectOK() {
		logger.debug("**** STATUS: REGISTERED ****");
	}
	
	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.api.IMListener#statusChanged(int)
	 */
	public void disconnectOK() {
		logger.debug("**** STATUS: UNREGISTERED ****");
	}

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.api.IMListener#gatewayEvent(boolean)
	 */
	public void gatewayEvent(boolean presence) {
		// TODO Auto-generated method stub
		
	}

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMListener#operationTimeout(java.lang.String)
     */
    public void operationTimeout(String Message) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMListener#operationFailed(java.lang.String)
     */
    public void operationFailed(int type, String Message) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMListener#buddyStatusChanged()
     */
    public void buddyStatusChanged() {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMListener#neighbourListChanged()
     */
    public void neighbourListChanged() {
        // TODO Auto-generated method stub
        
    }

	
}
