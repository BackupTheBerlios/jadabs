/*
 * Created on 27-ene-2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.ethz.jadabs.gw.sip_smtp.test;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.gw.api.Gateway;

/**
 * @author franz
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Activator  implements BundleActivator
{

    /*
     */
    public void start(BundleContext bc) throws Exception
    {
        
        ServiceReference sref = bc.getServiceReference(Gateway.class.getName());
        
        Gateway sipgw = (Gateway)bc.getService(sref);

        
        // sigin
        sipgw.signIn();
        
        
        // signout
//        Thread.sleep(5000);
//        sipgw.signOut("sip:gw@localhost");

    }

    /*
     */
    public void stop(BundleContext bc) throws Exception
    {

    }

}
