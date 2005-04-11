/*
 * Created on 27-ene-2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.ethz.jadabs.gw.sip_smtp;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.api.IOProperty;
import ch.ethz.jadabs.gw.api.Gateway;

/**
 * @author franz
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Activator implements BundleActivator{

    static BundleContext bc;

    TemplateJL tjl;

    public void start(BundleContext bc) throws Exception
    {
        Activator.bc = bc;
        
        BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
        
        ServiceReference sref = bc.getServiceReference(IOProperty.class.getName());
        if (sref == null)
            throw new Exception("could not properly initialize IM, IOProperty is missing");
        IOProperty prop = (IOProperty)bc.getService(sref);
        
        
        tjl = new TemplateJL(prop, System.getProperty("ch.ethz.jadabs.im.cayenne_config_file"));
        		
		System.out.println("Starting gateway !");
		tjl.start();
		
        bc.registerService(Gateway.class.getName(), tjl, null);
    }

    /*
     */
    public void stop(BundleContext bc) throws Exception
    {

    }

}
