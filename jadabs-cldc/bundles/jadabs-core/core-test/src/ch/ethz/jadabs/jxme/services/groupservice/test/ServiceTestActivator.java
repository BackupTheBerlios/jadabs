/* $Id: ServiceTestActivator.java,v 1.1 2005/02/18 21:12:30 printcap Exp $
 * Created on Feb 18, 2005
 *
 */
package ch.ethz.jadabs.jxme.services.groupservice.test;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.DiscoveryListener;
import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.Pipe;
import ch.ethz.jadabs.jxme.services.GroupService;


/**
 * Test for the group service.
 * 
 * @author Ren&eacute; M&uml;ller
 */
public class ServiceTestActivator implements BundleActivator, Listener
{
    /** logger used in this activator */
    static Logger LOG = Logger.getLogger(ServiceTestActivator.class.getName());

    /** the group service instance */
    GroupService groupService;
    
    /** reference to the pipe being accessed */
    Pipe pipe;
    
    /**
     * Called to start bundle 
     * @param bc context of this bundle 
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bc) throws Exception
    {

        // obtain a service reference for the group service 
        ServiceReference sref = bc.getServiceReference("ch.ethz.jadabs.jxme.services.GroupService");
        groupService = (GroupService)bc.getService(sref);
        
        // obtain reference to pipe
        String pipeName = bc.getProperty("ch.ethz.jadabs.jxme.services.groupservice.test.pipe");
                
        groupService.remoteSearch(NamedResource.PIPE, "Name", "", 1, new DiscoveryListener() {
            public void handleSearchResponse(NamedResource namedResource)
            {
                LOG.debug("Named Resource found: "+namedResource);                
            }

            public void handleNamedResourceLoss(NamedResource namedResource)
            {
                // TODO Auto-generated method stub
                
            }
            
            
        });
                
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bc) throws Exception
    {

    }
    
    //---------------------------------------------------
    //implements Listener
    //---------------------------------------------------
    
    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.Listener#handleMessage(ch.ethz.jadabs.jxme.Message, java.lang.String)
     */
    public void handleMessage(Message message, String listenerId)
    {
        System.out.println("got message: "+ message.toXMLString());
        
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.Listener#handleSearchResponse(ch.ethz.jadabs.jxme.NamedResource)
     */
    public void handleSearchResponse(NamedResource namedResource)
    {
        // TODO Auto-generated method stub
        
    }
}
