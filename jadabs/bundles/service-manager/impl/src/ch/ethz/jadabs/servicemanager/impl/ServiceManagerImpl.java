/*
 * Created on Jan 31, 2005
 *
 */
package ch.ethz.jadabs.servicemanager.impl;

import java.io.IOException;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.bundleloader.BundleInformation;
import ch.ethz.jadabs.bundleloader.BundleLoaderListener;
import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.Pipe;
import ch.ethz.jadabs.servicemanager.ServiceListener;
import ch.ethz.jadabs.servicemanager.ServiceManager;
import ch.ethz.jadabs.servicemanager.ServiceReference;


/**
 * @author andfrei
 * 
 */
public class ServiceManagerImpl implements ServiceManager, Listener, BundleLoaderListener
{

    private static Logger LOG = Logger.getLogger(ServiceManagerImpl.class);
    
    public static String SERVICE_TYPE = "type";
    public static String SERVICE_REQ = "svcreq";
    public static String SERVICE_ACK = "svcack";
    
    public static String SERVICE_FILTER = "svcfil";
    
    public ServiceManagerImpl()
    {
        
    }
    
    /*
     */
    public boolean getServices(Pipe pipe, String filter, ServiceListener serviceListener)
    {
        
        Element[] elm = new Element[2];
        
        elm[0] = new Element("type", SERVICE_REQ, Element.TEXTUTF8_MIME_TYPE);
        elm[1] = new Element(SERVICE_FILTER, filter, Element.TEXTUTF8_MIME_TYPE);
        
        try
        {
            ServiceManagerActivator.groupService.send(pipe, new Message(elm));
        } catch (IOException e)
        {
            LOG.debug("error in sending message");
            return false;
        }
        
        return true;
    }

    /*
     */
    public boolean getService(Pipe pipe, String fromPeer, ServiceReference sref)
    {
        return false;
    }

    /*
     */
    public boolean istartService(Pipe pipe, String toPeer, ServiceReference sref)
    {
        return false;
    }

    /*
     */
    public void addProvidingService(Pipe pipe, ServiceReference sref)
    {
        
    }

    /*
     */
    public void removeProvidingService(Pipe pipe, ServiceReference sref)
    {
        
    }
    
    //---------------------------------------------------
    // Implements Listener Interface
    //---------------------------------------------------
    
    /*
     */
    public void handleMessage(Message msg, String listenerId)
    {
        
        Element typeElement = msg.getElement(SERVICE_TYPE);
        if (typeElement.getName().equals(SERVICE_REQ))
        {
            // got a service req, put together a response
            LOG.debug("got service req");
        }
        else if (typeElement.getName().equals(SERVICE_ACK))
        {
            // got a response, call the listeners
            LOG.debug("got service ack");
        }
        
    }

    /*
     */
    public void handleSearchResponse(NamedResource namedResource)
    {
        
    }

    //---------------------------------------------------
    // implements BundleLoaderListener
    //---------------------------------------------------
    /*
     */
    public void bundleChanged(BundleInformation binfo, int type)
    {
        // create message out of this event and send it to remote peers.
        
    }


}
