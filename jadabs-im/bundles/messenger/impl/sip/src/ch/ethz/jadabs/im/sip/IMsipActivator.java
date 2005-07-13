/*
 * Created on Dec 15, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.ethz.jadabs.im.sip;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.im.ioapi.IOProperty;
import ch.ethz.jadabs.im.api.IMService;

/**
 * @author franz
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class IMsipActivator implements BundleActivator {
	
	static BundleContext bc;
    
    SIPUserAgentClient sipUAClient;
        
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bc) throws Exception {
		IMsipActivator.bc = bc;
		
        ServiceReference sref = bc.getServiceReference(IOProperty.class.getName());
        if (sref == null)
            throw new Exception("could not properly initialize IM, IOProperty is missing");
        IOProperty prop = (IOProperty)bc.getService(sref);
		
        sipUAClient = new SIPUserAgentClient(prop);
        
//      sipUAClient.start();
            
//      register the imservice implementation
//		ServiceReference impref = bc.getServiceReference(IMService.class.getName());
		
		Hashtable dict = new Hashtable();
		dict.put("impl","sip");
		bc.registerService(IMService.class.getName(), sipUAClient, dict);
        
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext arg0) throws Exception {
		
		
	}

}
