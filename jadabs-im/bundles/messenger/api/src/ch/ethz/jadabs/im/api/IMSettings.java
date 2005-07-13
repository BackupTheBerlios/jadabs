/*
 * Created on Dec 15, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.ethz.jadabs.im.api;

import ch.ethz.jadabs.im.ioapi.Settings;

/**
 * @author franz
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface IMSettings extends Settings {
	void newSettings(String usernameAtRealm, String password, String registrar, String ipPort);
	//void newSettings(String usernameATrealm, String password, String registrar, String ipPort);
    
	String getBuddyListPath();
	
	String getIpPort();
	
	String getRegistrar();
}