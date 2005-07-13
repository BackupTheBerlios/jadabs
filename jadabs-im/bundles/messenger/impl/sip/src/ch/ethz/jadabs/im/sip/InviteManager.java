/*
 * Created on 23 janv. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ch.ethz.jadabs.im.sip;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.sip.Dialog;

import org.apache.log4j.Logger;

/**
 * @author Franz Terrier
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class InviteManager {
	private Logger logger = Logger.getLogger(InviteManager.class.getName());
	
	private Hashtable invites;
	
	public InviteManager () {
		invites = new Hashtable();
	}
	
	public void addInvite(String username, Dialog dialog) {
		if (invites.get(username) == null) {
			logger.debug("Adding unknown user: "+username);
			Inviter inv = new Inviter(username, dialog);
			invites.put(username, inv);
//		}
//		else {
//			logger.debug("Found already invited user :"+username);
//			Inviter user = (Inviter)invites.get(username);
//			if (user.byeReceived()) {
//				removeInvite(user.getUsername());
//			}
//			else {
//				user.setDialog(dialog);
//			}
		}
	}
	
	public Inviter getInvite(String username) {
		return (Inviter)invites.get(username);
	}
	
	public boolean hasInvite(String username) {
		return invites.containsKey(username);
	}

	public boolean hasInviteReceived(String tousername) {
		Inviter user = (Inviter)invites.get(tousername);
		if (user == null) {
			return false;
		}
		else {
			return user.isConfirmed();
		}
	}
	
	public void addPendingMessage(String username, String message) {
		if (invites.get(username) == null) {
			Inviter inv = new Inviter(username);
			inv.addPendingMessage(message);
			invites.put(username, inv);
		}
		else {
			Inviter user = (Inviter)invites.get(username);
			user.setDialog(null);
			user.addPendingMessage(message);
		}
	}
	
	public void removeInvite(String username) {
		if (invites.get(username) == null){
			Inviter inv = new Inviter(username);
			inv.setByeReceived(true);
		}
		else {
			invites.remove(username);
		}
	}
	
	public void purge() {
		invites.clear();
	}
	
	public Inviter[] getAllConfirmedInvites() {
        Vector vector = new Vector();
	    
        for (Enumeration en = invites.elements(); en.hasMoreElements();)
        {
            Inviter temp = (Inviter)en.nextElement();
            if (temp.isConfirmed()) {
            	vector.add(temp);
            }
        }
       
        return (Inviter[])vector.toArray(new Inviter[vector.size()]);
	}

	public void updateInvite(String username, Dialog dialog) {
		Inviter inv = (Inviter)invites.get(username);
		if (inv != null) {
			if (inv.byeReceived()) {
				invites.remove(username);
			}
			else {
				inv.setDialog(dialog);
			}
		}
	}
}
