/*
 * Created on Nov 12, 2004
 *
 */
package ch.ethz.jadabs.jxme.sip;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;


/**
 * @author andfrei
 * 
 */
public class SipGatewayActivator implements BundleActivator
{

    static BundleContext bc;
    
    SipGatewayImpl sipgw;
    
    /* 
     */
    public void start(BundleContext bc) throws Exception
    {
        SipGatewayActivator.bc = bc;
        
        sipgw = new SipGatewayImpl();
        
        sipgw.start();
        
        bc.registerService(SipGateway.class.getName(), sipgw, null);
        
    }

    /*
     */
    public void stop(BundleContext bc) throws Exception
    {

    }

}
