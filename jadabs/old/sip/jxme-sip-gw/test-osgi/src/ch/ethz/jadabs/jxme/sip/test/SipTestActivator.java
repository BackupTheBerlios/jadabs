/*
 * Created on Nov 15, 2004
 *
 */
package ch.ethz.jadabs.jxme.sip.test;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.sip.SipGateway;


/**
 * @author andfrei
 * 
 */
public class SipTestActivator implements BundleActivator
{

    /*
     */
    public void start(BundleContext bc) throws Exception
    {
        
        ServiceReference sref = bc.getServiceReference(SipGateway.class.getName());
        
        SipGateway sipgw = (SipGateway)bc.getService(sref);
        
        // sigin
        sipgw.signIn("sip:frei@ethz.ch");
        
        
        // signout
        Thread.sleep(5000);
        sipgw.signOut("sip:frei@ethz.ch");

    }

    /*
     */
    public void stop(BundleContext bc) throws Exception
    {

    }

}
