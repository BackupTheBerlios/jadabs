/*
 * Created on Jul 15, 2004
 *
 */
package ch.ethz.jadabs.jxme.test;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.EndpointService;
import ch.ethz.jadabs.jxme.JxmeActivator;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.tcp.TCPActivator;
import ch.ethz.jadabs.osgi.j2me.OSGiContainer;


/**
 * @author andfrei
 * 
 */
public class TestMain implements BundleActivator
{

    public TestMain()
    {
        
    }
    
    public static void main(String[] args)
    {
        
        OSGiContainer osgicontainer = OSGiContainer.Instance();
        
        osgicontainer.startBundle(new JxmeActivator());
        osgicontainer.startBundle(new TCPActivator());
        
        osgicontainer.startBundle(new TestMain());
    }

    /*
     */
    public void start(BundleContext bc) throws Exception
    {
        
        ServiceReference sref = bc.getServiceReference(EndpointService.class.getName());
        
        EndpointService endptsvc = (EndpointService)bc.getService(sref);
            
        // create message
        Element[] elm = new Element[3];
        elm[0] = new Element("tag1", "hello", Message.JXTA_NAME_SPACE);
        elm[1] = new Element("tag2", "world", Message.JXTA_NAME_SPACE);
        elm[2] = new Element("tag3", "!", Message.JXTA_NAME_SPACE);
        
        //Message msg = new Message(elm);
        
        // create endpoint for a remote peer, connect through tcpconnection
        //EndpointAddress endptAdr = new EndpointAddress("tcp", "peer2", 9002);

        // send message
        endptsvc.propagate(elm,null);
        
    }

    /*
     */
    public void stop(BundleContext bc) throws Exception
    {
        
    }
    
}
