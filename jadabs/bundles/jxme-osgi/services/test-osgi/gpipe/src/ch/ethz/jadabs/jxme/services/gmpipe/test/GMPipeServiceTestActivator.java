/*
 * Created on Jan 26, 2005
 *
 */
package ch.ethz.jadabs.jxme.services.gmpipe.test;

import java.io.IOException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.Pipe;
import ch.ethz.jadabs.jxme.services.GroupService;


/**
 * @author andfrei
 * 
 */
public class GMPipeServiceTestActivator implements BundleActivator, Listener
{

    GroupService groupService;
    Pipe groupPipe;
    
    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext bc) throws Exception
    {

        ServiceReference sref = bc.getServiceReference("ch.ethz.jadabs.jxme.services.GroupService");
        groupService = (GroupService)bc.getService(sref);
        
        // create Pipe
        String gmpipeName = bc.getProperty("ch.ethz.jadabs.jxme.services.gmpipe.name");
        long gmpipeID = Long.parseLong(bc.getProperty("ch.ethz.jadabs.jxme.services.gmpipe.id"));
        
        groupPipe = groupService.createGroupPipe(gmpipeName, gmpipeID);
        
        
        // set listener
        groupService.listen(groupPipe, this);
        
                
        if (bc.getProperty("ch.ethz.jadabs.jxme.peeralias").equals("peer1"))
        {
            sendTestString("hallo");
        }
        
    }

    /* (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bc) throws Exception
    {

    }

    public void sendTestString(String string)
    {
        
        Element[] elm = new Element[1];
        
        elm[0] = new Element("obrstr", string, Message.JXTA_NAME_SPACE);
        
        Message msg = new Message(elm);
        
        try {
            groupService.send(groupPipe, msg);
        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
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
