/*
 * Created on 09.12.2004
 */
package ch.ethz.jadabs.shell.plugin;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import ch.ethz.jadabs.remotefw.FrameworkManager;
import ch.ethz.jadabs.shell.IShellPluginService;


/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class PluginActivator implements BundleActivator {
	
	protected static BundleContext b_context;
	protected static Logger LOG = Logger.getLogger(PluginActivator.class);
	protected static IShellPluginService shell;
	protected static RemotefwPlugin remotefwPlugin;
	protected static FrameworkManager remotefw;
	
	/**
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bc) throws Exception {
        PluginActivator.b_context = bc;
        ServiceReference sref;
      
        // get RemoteFW
        sref = bc.getServiceReference(FrameworkManager.class.getName());
        if (sref != null)
        {
            LOG.debug("Connected to Jadabs Shell ");
        } else
        {
            LOG.debug("Can't start RemoteFWPlugin, RemoteFW not running !");
            throw new BundleException("Can't start RemoteFWPlugin, RemoteFW not running !");
        }
        PluginActivator.remotefw = (FrameworkManager) bc.getService(sref);                

        System.out.println("registering RemoteFW Plugin ...");
        System.out.print("jadabs>");
        
        // get JadabsShell
        sref = bc.getServiceReference(IShellPluginService.class.getName());
        if (sref != null)
        {
            LOG.debug("Connected to Jadabs Shell ");
        } else
        {
            LOG.debug("Can't start RemoteFWPlugin, Jadabs Shell not running !");
            throw new BundleException("Can't start RemoteFWPlugin, Jadabs Shell not running !");
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
