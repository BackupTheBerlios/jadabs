/*
 * Created on Nov 15, 2004
 *
 */
package ch.ethz.jadabs.gw.jxme_sip.test;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.gw.api.Gateway;



/**
 * @author andfrei
 * 
 */
public class SipGwTestActivator implements BundleActivator
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
