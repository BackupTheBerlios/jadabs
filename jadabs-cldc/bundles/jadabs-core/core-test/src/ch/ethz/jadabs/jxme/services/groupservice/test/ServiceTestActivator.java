/* $Id: ServiceTestActivator.java,v 1.2 2005/04/03 16:42:21 printcap Exp $
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
import ch.ethz.jadabs.jxme.Peer;
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
        LOG.debug("pipeName: "+pipeName);
        
        groupService.remoteSearch(NamedResource.PIPE, "Name", "", 1, new DiscoveryListener() {
            public void handleSearchResponse(NamedResource namedResource)
            {
                LOG.debug("Named Resource found: "+namedResource);
                if (namedResource instanceof Pipe) {
                    LOG.debug("pipe resource found.");
                    Pipe pipe = (Pipe)namedResource;
                    try {
                        groupService.resolve(pipe, 100000);
                        Element[] elems = new Element[] {
                                new Element("message","Hello World!",Message.JXTA_NAME_SPACE)
                        };
                        LOG.debug("sending 'Hello World!' message");
                        groupService.send(pipe, new Message(elems));                        
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                } else if (namedResource instanceof Peer) {
                    LOG.debug("peer resource found.");
                }
            }

            public void handleNamedResourceLoss(NamedResource namedResource)
            {
                LOG.debug("handle named resource loss called.");                
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
