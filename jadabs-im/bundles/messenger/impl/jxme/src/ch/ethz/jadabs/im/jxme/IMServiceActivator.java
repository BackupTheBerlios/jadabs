/*
 * Created on Dec 15, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.ethz.jadabs.im.jxme;


import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.im.ioapi.IOProperty;
import ch.ethz.jadabs.im.api.IMService;
import ch.ethz.jadabs.im.api.IMSettings;
import ch.ethz.jadabs.jxme.Pipe;
import ch.ethz.jadabs.jxme.services.GroupService;
import ch.ethz.jadabs.osgiaop.AOPContext;
import ch.ethz.jadabs.osgiaop.AOPServiceRegistration;

/**
 */
public class IMServiceActivator implements BundleActivator {
	
	static BundleContext bc;
    
    IMServiceImpl jxmeUAClient;
    
	/*
	 */
	public void start(BundleContext bc) throws Exception 
	{
		IMServiceActivator.bc = bc;
		
//		 get GroupService
        ServiceReference sref = bc.getServiceReference("ch.ethz.jadabs.jxme.services.GroupService");
        if (sref == null)
            throw new Exception("could not properly initialize IM, GroupService is missing");
        GroupService groupsvc = (GroupService)bc.getService(sref);
        
        String gmpipeName = bc.getProperty("ch.ethz.jadabs.jxme.services.gmpipe.name");
        long gmpipeID = Long.parseLong(bc.getProperty("ch.ethz.jadabs.jxme.services.gmpipe.id"));
        Pipe groupPipe = groupsvc.createGroupPipe(gmpipeName, gmpipeID); 

        ServiceReference[] sr = bc.getServiceReferences(IOProperty.class.getName(), "(buddy=false)");
        if (sref == null)
            throw new Exception("could not properly initialize IM, IOProperty 1 is missing");
        IOProperty prop = (IOProperty)bc.getService(sr[0]);
        
        sr = bc.getServiceReferences(IOProperty.class.getName(), "(buddy=true)");
        if (sref == null)
            throw new Exception("could not properly initialize IM, IOProperty 2 is missing");
        IOProperty buddyprop = (IOProperty)bc.getService(sr[0]);
        
		jxmeUAClient = new IMServiceImpl(groupsvc, groupPipe, prop, buddyprop);
		
        // set listener
//        groupsvc.listen(groupPipe, jxmeUAClient);

		// register IMService independet of AOP
//		bc.registerService("ch.ethz.jadabs.im.api.IMService", (IMService)jxmeUAClient, null);

		bc.registerService(IMSettings.class.getName(), jxmeUAClient, null);
		
//		 register the service with the AOPContext.registerAOPService(..)
		Hashtable dict = new Hashtable();
		dict.put("impl","jxme");
		AOPServiceRegistration imsvcreg = 
        	((AOPContext) bc).registerAOPService(
				IMService.class,
				jxmeUAClient, dict);
        
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
