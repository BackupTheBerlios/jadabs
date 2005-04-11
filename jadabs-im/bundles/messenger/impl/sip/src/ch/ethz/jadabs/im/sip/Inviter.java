/*
 * Created on 23 janv. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ch.ethz.jadabs.im.sip;

import java.util.Vector;

import javax.sip.Dialog;

/**
 * @author Franz Terrier
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Inviter {
	private String username;
	private Dialog dialog;
	private boolean byeReceived = false;
	
	private Vector messages;
	
	public Inviter(String username, Dialog dialog) {
		this.username = username;
		this.dialog = dialog;
		messages = new Vector();
	}
	
	public boolean byeReceived() {
		return byeReceived;
	}
	
	public void setByeReceived(boolean br) {
		this.byeReceived = br;
	}
	
	public Inviter(String username) {
		this.username = username;
		this.dialog = null;
		messages = new Vector();
	}
	
	public boolean isConfirmed() {
		return dialog != null;
	}
	
	public Dialog getDialog() {
		return dialog;
	}
	
	public void setDialog(Dialog dialog) {
		this.dialog = dialog;
	}
	
	public String getUsername () {
		return username;
	}
	
	public void addPendingMessage(String message) {
		messages.add(message);
	}
	
	public String getNextMessage() {
		return (String)messages.remove(0);
	}
	
	public boolean hasPendingMessage() {
		return !messages.isEmpty();
	}
}
