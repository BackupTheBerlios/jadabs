package ch.ethz.iks.jadabs.shell.svc;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;
import ch.ethz.iks.remotefw.FrameworkManager;


/**
 * @author rjan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class Activator implements BundleActivator
{

    protected static Logger LOG = Logger.getLogger(Activator.class.getName());    
    protected static BundleContext b_context;
    protected static String peerName;
    protected static FrameworkManager remotefw;
    protected static boolean running = true;
    private Shell shell;

    /**
     * start the bundle, this method is called by the OSGi implementation.
     * 
     * @param bc the bundle context of the OSGi framework.
     * @throws Exception
     */
    public void start(BundleContext bc) throws Exception
    {
        Activator.b_context = bc;
        ServiceReference sref;
        
        if (LOG.isDebugEnabled())
            LOG.debug("starting Jadabs Shell ... ");

        // get RemoteFramework
        sref = bc.getServiceReference(FrameworkManager.class.getName());
        if (sref != null)
        {
            LOG.debug("Connected to RemoteFramework ");
        } else
        {
            LOG.debug("Can't start Jadabs Shell, RemoteFramework not running !");
            throw new BundleException("Can't start Jadabs Shell, RemoteFramework not running !");
        }
        Activator.remotefw = (FrameworkManager) bc.getService(sref);

        Activator.peerName = bc.getProperty("jxme.peername");

        if (LOG.isDebugEnabled())
            LOG.debug("peername is " + peerName);

        shell = Shell.getInstance();
        shell.start();
        
        b_context.registerService(IShellPluginService.class.getName(),shell,null);

    }
    
    /**
     * stops the bundle, this method is called by the OSGi implementation.
     * 
     * @param bc
     *            the bundle context of the OSGi framework.
     * @throws Exception
     */
    public void stop(BundleContext bc) throws Exception
    {
       running = false;
       shell = null;
    }
}
