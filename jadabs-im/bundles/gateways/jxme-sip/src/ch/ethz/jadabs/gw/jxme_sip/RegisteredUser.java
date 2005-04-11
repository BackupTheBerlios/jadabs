/*
 * Created on 01-feb-2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.ethz.jadabs.gw.jxme_sip;

import ch.ethz.jadabs.sip.handler.authentication.AuthenticationProcess;

/**
 * @author franz
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class RegisteredUser {
    private String username;
    private String password;
    private String realm;
    
    private AuthenticationProcess authenticationProcess;
    
    public RegisteredUser (String username, String realm, String password, AuthenticationProcess authenticationProcess) {
        this.password = password;
        this.username = username;
        this.realm = realm;
        this.authenticationProcess = authenticationProcess;
    }
    
    public String getUserAtRealm() {
        return getUserName()+"@"+getRealm();
    }
    
    public String getUserName() {
        return username;
    }
    
    public String getRealm() {
        return realm;
    }
    
    public String getPassword() {
        return password;
    }
    
    public AuthenticationProcess getAuthProcess() {
        return authenticationProcess;
    }
}
