/*
 * Created on 19-ene-2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.ethz.jadabs.im.common;

import ch.ethz.jadabs.im.ioapi.IOProperty;
import ch.ethz.jadabs.im.ioapi.CommonSettings;

/**
 * @author franz
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class CommonIMSettings extends CommonSettings {
    
    public CommonIMSettings(IOProperty p, String comment) {
    	super(p, comment);
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMAuthentication#setUserName(java.lang.String)
     */
    public void setUserName(String userName) {
        p.setProperty("ch.ethz.jadabs.im.username", userName);
        p.save(comment);
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMAuthentication#setPassword(java.lang.String)
     */
    public void setPassword(String password) {
        p.setProperty("ch.ethz.jadabs.im.password", password);
        p.save(comment);
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMAuthentication#setRegistrar(java.lang.String)
     */
    public void setRegistrar(String proxy) {
        p.setProperty("ch.ethz.jadabs.im.registrar", proxy);
        p.save(comment);
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMSettings#setIpPort(java.lang.String)
     */
    public void setIpPort(String ipPort) {
        p.setProperty("ch.ethz.jadabs.im.ipport", ipPort);
        p.save(comment);
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMSettings#getBuddyListPath()
     */
    public String getBuddyListPath() {
        return p.getProperty("ch.ethz.jadabs.im.buddylist", "./buddy.list");
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMSettings#setBuddyListPath()
     */
    public void setBuddyListPath(String path) {
        p.getProperty("ch.ethz.jadabs.im.buddylist", path);
        p.save(comment);
    }

}
