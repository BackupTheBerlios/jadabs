/*
 * Created on Nov 12, 2004
 *
 */
package ch.ethz.jadabs.gw.jxme_sip;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.im.ioapi.IOProperty;
import ch.ethz.jadabs.gw.api.Gateway;
import ch.ethz.jadabs.jxme.Pipe;
import ch.ethz.jadabs.jxme.services.GroupService;


/**
 * @author andfrei
 * 
 */
public class SipGatewayActivator implements BundleActivator
{

    static BundleContext bc;
    
    SipGatewayImpl sipGatewayImpl;
    
    static GroupService groupsvc;
    /* 
     */
    public void start(BundleContext bc) throws Exception
    {
        SipGatewayActivator.bc = bc;
                
        // get GroupService
        ServiceReference sref = bc.getServiceReference("ch.ethz.jadabs.jxme.services.GroupService");
        if (sref == null)
            throw new Exception("could not properly initialize IM, GroupService is missing");
        groupsvc = (GroupService)bc.getService(sref);
        
        sref = bc.getServiceReference(IOProperty.class.getName());
        if (sref == null)
            throw new Exception("could not properly initialize IM, IOProperty is missing");
        IOProperty prop = (IOProperty)bc.getService(sref);
        
        String gmpipeName = bc.getProperty("ch.ethz.jadabs.jxme.services.gmpipe.name");
        long gmpipeID = Long.parseLong(bc.getProperty("ch.ethz.jadabs.jxme.services.gmpipe.id"));
        Pipe groupPipe = groupsvc.createGroupPipe(gmpipeName, gmpipeID); 
        
        sipGatewayImpl = new SipGatewayImpl(groupsvc, groupPipe, prop);
		
		System.out.println("Starting gateway !");
		sipGatewayImpl.start();
		
		// TEST
//		sipGatewayImpl.signIn();
//        
        bc.registerService(Gateway.class.getName(), sipGatewayImpl, null);
        
        
        
    }

    /*
     */
    public void stop(BundleContext bc) throws Exception
    {

    }

}
