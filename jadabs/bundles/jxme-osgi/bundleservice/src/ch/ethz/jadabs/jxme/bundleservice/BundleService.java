/*
 * Created on Jan 26, 2005
 *
 */
package ch.ethz.jadabs.jxme.bundleservice;

import java.io.IOException;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.Peer;
import ch.ethz.jadabs.jxme.Service;
import ch.ethz.jadabs.jxme.services.GroupService;


/**
 * @author andfrei
 * 
 */
public class BundleService extends Service implements Listener
{

    private static Logger LOG = Logger.getLogger(BundleService.class);
    
    static final String BUNDLESERVICE_NAME = "BundleService";
    
    private GroupService groupService;
    
    public BundleService(Peer peer, GroupService groupService)
    {
        super(peer, BUNDLESERVICE_NAME);
     
        groupService.addCoreService(BUNDLESERVICE_NAME, this);

    }
    
    
    public void sendTestString(String string)
    {
        
        Element[] elm = new Element[1];
        
        elm[0] = new Element("obrstr", string, Message.JXTA_NAME_SPACE);
        
        Message msg = new Message(elm);
        
        try {
            LOG.debug("send string now: "+msg.toXMLString());
            BundleServiceActivator.groupService.send(msg, serviceName, "");
        } catch (IOException ioe)
        {
            LOG.error("could not send teststring");
        }
    }
    
    //---------------------------------------------------
    // Listener implementation
    //---------------------------------------------------
    
    /*
     */
    public void handleMessage(Message message, String listenerId)
    {
        LOG.debug("got message: "+message.toXMLString());
        
    }

    /*
     */
    public void handleSearchResponse(NamedResource namedResource)
    {

    }

    
}
