/*
 * Created on 19-ene-2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.ethz.jadabs.config;

import ch.ethz.jadabs.api.IOProperty;


/**
 * @author franz
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CommonSettings {
    protected IOProperty p;
    protected String comment;
    
    public CommonSettings(IOProperty p, String comment) {
    	this.p = p;
    	this.comment = comment;
    	p.load();
    }

    public String getUserName() {
        String userRealm = p.getProperty("ch.ethz.jadabs.im.username", "gw@gw.com");
   		return userRealm.substring(0, userRealm.indexOf("@"));
    }
    
    public String getRealm() {
        String userRealm = p.getProperty("ch.ethz.jadabs.im.username", "gw@gw.com");
        return userRealm.substring(userRealm.indexOf("@")+1);
    }

    public String getPassword() {
        return p.getProperty("ch.ethz.jadabs.im.password", "123");
    }

    public String getRegistrar() {
        return p.getProperty("ch.ethz.jadabs.im.registrar", "127.0.0.1:5060");
    }
    
    public int getPort() {
        String ipPort =  p.getProperty("ch.ethz.jadabs.im.ipport", "127.0.0.1:5067");
	    int index = ipPort.indexOf(":");
	    if (index == -1) {
	        return 5060;
	    }
	    else {
	        return Integer.parseInt(ipPort.substring(index+1));
	    }
    }
    
    public String getIpAddress() {
	    String ipPort = p.getProperty("ch.ethz.jadabs.im.ipport", "127.0.0.1:5067");
	    int index = ipPort.indexOf(":");
	    if (index == -1) {
	        return ipPort;
	    }
	    else {
	        return ipPort.substring(0, index);
	    }
    }

}