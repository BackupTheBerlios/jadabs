/*
 * Created on Jan 31, 2005
 *
 */
package ch.ethz.jadabs.servicemanager.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.bundleloader.BundleLoader;
import ch.ethz.jadabs.jxme.Pipe;
import ch.ethz.jadabs.jxme.services.GroupService;
import ch.ethz.jadabs.pluginloader.PluginLoader;



/**
 * @author andfrei
 * 
 */
public class ServiceManagerActivator implements BundleActivator
{

    static BundleContext bc;

    static GroupService groupService;
    static Pipe groupPipe;
    
    static PluginLoader pluginLoader;
    
    static BundleLoader bundleLoader;
    
    private ServiceManagerImpl serviceManager;
    
    static String peername;
    
    /*
     */
    public void start(BundleContext bc) throws Exception
    {
        ServiceManagerActivator.bc = bc;
        
        peername = bc.getProperty("ch.ethz.jadabs.jxme.peeralias");
                
        // GroupService
        ServiceReference sref = bc.getServiceReference(
                "ch.ethz.jadabs.jxme.services.GroupService");
        groupService = (GroupService)bc.getService(sref);
        
        //  create Pipe
        String gmpipeName = bc.getProperty("ch.ethz.jadabs.servicemanager.gmpipe.name");
        long gmpipeID = Long.parseLong(bc.getProperty("ch.ethz.jadabs.servicemanager.gmpipe.id"));
        
        groupPipe = groupService.createGroupPipe(gmpipeName, gmpipeID);
        
        
        // create and publish ServiceManager
        serviceManager = new ServiceManagerImpl();
        serviceManager.initRepoCache();
        
        	// PluginLoader
        sref = bc.getServiceReference(PluginLoader.class.getName());
        pluginLoader = (PluginLoader)bc.getService(sref);
        
    		// BundleLoader
        sref = bc.getServiceReference(BundleLoader.class.getName());
        bundleLoader = (BundleLoader)bc.getService(sref);
        
        // register servicemanager
        bc.registerService("ch.ethz.jadabs.servicemanager.ServiceManager", serviceManager, null);
        
        // set listener
        groupService.listen(groupPipe, serviceManager);
        
    }
    
    

    /*
     */
    public void stop(BundleContext bc) throws Exception
    {
//        bundleLoader.removeListener(serviceManager);
    }
    
}
