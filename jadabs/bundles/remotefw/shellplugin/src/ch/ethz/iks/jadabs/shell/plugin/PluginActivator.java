/*
 * Created on 09.12.2004
 */
package ch.ethz.iks.jadabs.shell.plugin;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

import ch.ethz.iks.jadabs.shell.svc.IShellPluginService;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class PluginActivator implements BundleActivator {
	
	protected static BundleContext b_context;
	protected static Logger LOG = Logger.getLogger(PluginActivator.class);
	protected static IShellPluginService shell;
	protected static RemotefwPlugin remotefwPlugin;
	
	/**
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bc) throws Exception {
        PluginActivator.b_context = bc;
        ServiceReference sref;
        
        System.out.println("registering RemoteFW Plugin ...");
        System.out.print("jadabs>");
        
        // get RemoteFramework
        sref = bc.getServiceReference(IShellPluginService.class.getName());
        if (sref != null)
        {
            LOG.debug("Connected to Jadabs Shell ");
        } else
        {
            LOG.debug("Can't start ARA Plugin, Jadabs Shell not running !");
            throw new BundleException("Can't start ARA Plugin, Jadabs Shell not running !");
        }
        PluginActivator.shell = (IShellPluginService) bc.getService(sref);                
        
        
        PluginActivator.remotefwPlugin = new RemotefwPlugin();
        
        shell.registerPlugin(remotefwPlugin);
        System.out.println("RemoteFW Plugin registered");
        System.out.print("jadabs> ");

	}

	/**
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bc) throws Exception {
	       shell.unregisterPlugin(PluginActivator.remotefwPlugin);
	       shell = null;
	}

}
