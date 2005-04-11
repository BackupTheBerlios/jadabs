/*
 * Created on Dec 15, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.ethz.jadabs.im.gui.swtgui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import ch.ethz.jadabs.im.api.IMListener;

/**
 * @author franz
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Listener implements IMListener
{   
	private MainComposite maincomposite;
	
	public Listener(MainComposite maincomposite) {
		this.maincomposite = maincomposite;
	}
	
	/**
	 * If IMService.register is called, used to notify app about 
	 * status of registration process
	 */
	public void connectOK() { 	
		SwtGUI.manager.exec(new Runnable() {
			public void run()
			{
				maincomposite.connectOk();
			}
		}, false);
	}

	/**
	 * If IMService.unregister is called, used to notify app about 
	 * status of registration process
	 */
	public void disconnectOK() {	 	
		SwtGUI.manager.exec(new Runnable() {
			public void run()
			{
				maincomposite.disconnectOk();
			}
		}, false);
	}
	
	/**
	 * If an instant message is received from the sender this method
	 * is called.
	 * 
	 * @param sipaddress from address, who sent the message
	 * @param msg instant message
	 */
	public void incomingMessage(String sipaddress, String message) {
		final String sipaddr = sipaddress;
		final String msg = message;
		SwtGUI.manager.exec(new Runnable() {
			public void run()
			{
				maincomposite.incomingMessage(sipaddr, msg);
			}
		}, false);            	
	}
	
	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.api.IMListener#gatewayEvent(boolean)
	 */
	public void gatewayEvent(boolean presence) {
		final boolean b = presence;
		SwtGUI.manager.exec(new Runnable() {
			public void run()
			{
				maincomposite.gatewayEvent(b);
			}
		}, false);         
	}

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.api.IMListener#operationTimeout(java.lang.String)
	 */
	public void operationFailed(int type, String message) {
		final String msg = message;
		SwtGUI.manager.exec(new Runnable() {
			public void run()
			{
				MessageBox messageBox = new MessageBox(maincomposite.getShell(), SWT.OK | SWT.ICON_ERROR);
				messageBox.setMessage(msg);
				messageBox.open();
			}
		}, false);   
	}

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.api.IMListener#buddyStatusChanged()
	 */
	public void buddyStatusChanged() {
		SwtGUI.manager.exec(new Runnable() {
			public void run()
			{
				maincomposite.buddyStatusChanged();
			}
		}, false);   
	}

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.api.IMListener#neighbourListChanged()
	 */
	public void neighbourListChanged() {
		SwtGUI.manager.exec(new Runnable() {
			public void run()
			{
				maincomposite.neighbourListChanged();
			}
		}, false);   
	}
}
