/*
 * Created on Jul 20, 2004
 *
 */
package ch.ethz.jadabs.jxme.tcp.test;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.EndpointService;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;

import junit.framework.TestCase;


/**
 * @author andfrei
 *
 */
public class TCPTransportTest extends TestCase 
	implements BundleActivator, Listener
{

    private static final Logger LOG = Logger.getLogger(TCPTransportTest.class.getName());
    
    EndpointService endptsvc;
    
    /*
     */
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    /*
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    /*
     */
    public void start(BundleContext bc) throws Exception
    {
        ServiceReference sref = bc.getServiceReference(EndpointService.class.getName());
        endptsvc = (EndpointService)bc.getService(sref);
        
        endptsvc.addListener("testlistener",this);
        
        testSend();
    }

    /*
     */
    public void stop(BundleContext bc) throws Exception
    {
    }
    
    public void testSend()
    {
        Element[] elm = new Element[3];
        elm[0] = new Element("tag1", "hello", Message.JXTA_NAME_SPACE);
        elm[1] = new Element("tag2", "world", Message.JXTA_NAME_SPACE);
        elm[2] = new Element("tag3", "!", Message.JXTA_NAME_SPACE);
        Message msg = new Message(elm);
        
        LOG.debug("call now propagate message: " + msg);
                
        try
        {
            // wilab9
//            EndpointAddress endptlistener = new EndpointAddress(
//                    "tcp","129.132.177.109", 9001, "testlistener",null);
            
            // saentis
            EndpointAddress endptlistener = new EndpointAddress(
                    "tcp","129.132.130.142", 9001, "testlistener",null);
            
            // multicast
            //endptsvc.propagate(msg, endptlistener);
            
            // unicast
            endptsvc.send(elm, new EndpointAddress[] {endptlistener});
            
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /* 
     */
    public void handleMessage(Message message, String listenerId)
    {
        LOG.debug("udptransporttest got message: "+message.toXMLString());
        LOG.debug("listener params: "+listenerId);
    }

    /* 
     */
    public void handleSearchResponse(NamedResource namedResource)
    {
        LOG.debug("called handleSearchResponse, not implemented");
    }

}
