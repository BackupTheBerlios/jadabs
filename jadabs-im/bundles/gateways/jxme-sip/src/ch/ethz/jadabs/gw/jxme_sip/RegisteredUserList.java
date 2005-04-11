/*
 * Created on 01-feb-2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.ethz.jadabs.gw.jxme_sip;
import org.apache.log4j.Logger;
import java.util.Hashtable;

/**
 * @author franz
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class RegisteredUserList {
    private Logger logger = Logger.getLogger(RegisteredUserList.class.getName());
    
    private Hashtable users;
    
    public RegisteredUserList() {
        this.users = new Hashtable();
    }
    
    public void addUser(RegisteredUser user) {
        logger.debug("Adding user to registeres user list: "+user.getUserAtRealm());
        users.put(user.getUserAtRealm(), user);
    }
    
    public RegisteredUser getUser(String useratrealm) {
        return (RegisteredUser)users.get(useratrealm);
    }
    
    public boolean hasUser(String useratrealm) {
        if (users.get(useratrealm)==null) {
            return false;
        }
        else {
            return true;
        }
    }
}
