package ch.ethz.jadabs.im.common;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.api.IOProperty;
import ch.ethz.jadabs.api.MessageCons;
import ch.ethz.jadabs.im.api.IMContact;

/*
 * Created on Nov 29, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

/**
 * @author franz
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class UserList {
    private static Logger logger = Logger.getLogger("ch.ethz.jadabs.im.common.UserList");
	private Hashtable users;
	private IOProperty p;
	
	public UserList(IOProperty p) {
		users = new Hashtable();
		this.p = p;
		
		load();
	}
	
	private void load() {
		p.load();
		
        int n = Integer.parseInt(p.getProperty("number_of_buddies", "0"));
        for (int i=0; i < n; i++) {
            IMContact bud = new IMContact(p.getProperty("buddy"+i, ""), MessageCons.IM_STATUS_UNKNOWN);
            users.put(bud.getUsername(), bud);
        }	
	}
	
	public void addUser (IMContact user) {
	    logger.debug("Added " + user.getUsername() +" to user list");
		users.put(user.getUsername(), user);
		save();
	}
	
	public IMContact[] getUsers(){
	    IMContact[] ntuples = new IMContact[users.size()];
        
        int i = 0;
        for (Enumeration en = users.elements(); en.hasMoreElements();)
        {
            ntuples[i++] = (IMContact)en.nextElement();
        }
        
        return ntuples;
	}

	private void save() {
	    int n = users.size();
	    p.clear();
	    p.setProperty("number_of_buddies", n+"");
	    int i=0;
	    for (Enumeration en = users.elements(); en.hasMoreElements();i++)
        {
            p.setProperty("buddy"+i, ((IMContact)en.nextElement()).getUsername());
        }	 
	    p.save("Buddy list");
	}
	
    /**
     * @param buddy
     */
    public void removeUser(String buddy) {
        logger.debug("Removing "+buddy+" from user list");
        if (users.remove(buddy) == null) {
            logger.debug("Nothing removed");
        }
        else {
            logger.debug(buddy + " removed");
        }
        save();
    }

    /**
     * @param fromUri
     */
    public IMContact getUser(String buddy) {
        return (IMContact)users.get(buddy);
    }
	
//	public User getUserbyName(String name) {
//		Iterator i = users.iterator();
//		while (i.hasNext()) {
//			User u = (User)i.next();
//			if (u.getName().equalsIgnoreCase(name)) {
//				return u;
//			}
//		}
//		return null;
//	}
	
//	public void getFromFile(String fileName) {
//		XMLBuddyParser parser = new XMLBuddyParser(fileName);
//		parser.parse();
//		users = parser.getBuddies();
//	}
	
//	public String getXMLTag() {
//		StringBuffer sb = new StringBuffer();
////		sb.append("<?xml version='1.0' encoding='us-ascii'?> ");
//		sb.append("<BUDDIES>\n");
//		Iterator i = users.iterator();
//		while (i.hasNext()) {
//			sb.append(((User)i.next()).getXMLTag());
//		}
//		sb.append("</BUDDIES>");
////		IMUtilities.writeFile(sb.toString(), fileName);
//		return sb.toString();
//	}
	
//	public String toString() {
//		return getXMLTag();
//	}
}