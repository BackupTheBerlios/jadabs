/*
 * Created on Jan 31, 2005
 *
 */
package ch.ethz.jadabs.servicemanager.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.pluginloader.OSGiPlugin;
import ch.ethz.jadabs.servicemanager.ServiceListener;
import ch.ethz.jadabs.servicemanager.ServiceManager;
import ch.ethz.jadabs.servicemanager.ServiceReference;


/**
 * @author andfrei
 * 
 */
public class ServiceManagerImpl implements ServiceManager, Listener
{

    private static Logger LOG = Logger.getLogger(ServiceManagerImpl.class);
    
    public static String SERVICE_TYPE = "type";
    public static String SERVICE_REQ = "svcreq";
    public static String SERVICE_ACK = "svcack";
    public static String SERVICE_ADV = "svcack";
    public static String SERVICE_ID = "svcid";
    
    public static String SERVICE_FILTER = "svcfil";
    
    public static String SERVICE_RP_TYPE = "rptype";
    
    public static String SERVICE_PEER = "peer";
    
    private Vector listener = new Vector();
    
    private String repoCacheDirDefault = "./repocache/";
    private File repoCacheDir;
    
    
    public ServiceManagerImpl()
    {
        
    }
    
    public void initRepoCache()
    {
        
        // creat new repocache if not already exists and not stated
        // otherwise
        
        repoCacheDir = new File(repoCacheDirDefault);
        
        if (!repoCacheDir.exists())
            repoCacheDir.mkdir();
                

    }
    
    /*
     */
    public boolean getServices(ServiceListener serviceListener, String rptype)
    {
        listener.add(serviceListener);
        
        Element[] elm = new Element[2];
        
        elm[0] = new Element(SERVICE_TYPE, SERVICE_REQ, Message.JXTA_NAME_SPACE);
        elm[1] = new Element(SERVICE_RP_TYPE, rptype, Message.JXTA_NAME_SPACE);
        
        try
        {
            LOG.debug("send servicemanager message");
            
            ServiceManagerActivator.groupService.send(
                    ServiceManagerActivator.groupPipe, 
                    new Message(elm));
            
        } catch (IOException e)
        {
            LOG.debug("error in sending message");
            return false;
        }
        
        return true;
    }

    /*
     */
    public boolean getService(String fromPeer, ServiceReference sref)
    {
        return false;
    }

    /*
     */
    public boolean istartService(String toPeer, ServiceReference sref)
    {
        return false;
    }

    /*
     */
    public void addProvidingService(ServiceReference sref)
    {
        
    }

    /*
     */
    public void removeProvidingService(ServiceReference sref)
    {
        
    }
    
    //---------------------------------------------------
    // Implements Listener Interface
    //---------------------------------------------------
    
    /*
     */
    public void handleMessage(Message msg, String listenerId)
    {
        LOG.debug("handle message: service manager");
        
        String type = new String(msg.getElement(SERVICE_TYPE).getData());
        if (type.equals(SERVICE_REQ))
        {            
            // return one opd to test
            Enumeration en = ServiceManagerActivator.pluginLoader.getOSGiPlugins();
            
            for(;en.hasMoreElements(); )
            {
                OSGiPlugin plugin = (OSGiPlugin)en.nextElement();
                
                String adv = plugin.getAdvertisement();
                             
                String id = plugin.getID();
                
                Element[] elm = new Element[5];
                
                elm[0] = new Element(SERVICE_TYPE, SERVICE_ACK, Message.JXTA_NAME_SPACE);
                elm[1] = new Element(SERVICE_ADV, adv, Message.JXTA_NAME_SPACE);
                elm[2] = new Element(SERVICE_RP_TYPE, ServiceManager.RUNNING_SERVICE, Message.JXTA_NAME_SPACE);
                elm[3] = new Element(SERVICE_PEER, ServiceManagerActivator.peername, Message.JXTA_NAME_SPACE);
                elm[4] = new Element(SERVICE_ID, id, Message.JXTA_NAME_SPACE);
                try
                {                    
                    ServiceManagerActivator.groupService.send(ServiceManagerActivator.groupPipe, new Message(elm));
                } catch (IOException e)
                {
                    LOG.debug("error in sending message");
                }
            
            }
                
        }
        else if (type.equals(SERVICE_ACK))
        {            
            String peer = new String(msg.getElement(SERVICE_PEER).getData());
            String adv = new String(msg.getElement(SERVICE_ADV).getData());
            
            OSGiPlugin plugin = ServiceManagerActivator.pluginLoader.parsePluginAdvertisement(adv);
            
            plugin.setAdvertisement(adv);
            
            // per default save the opd in the repocache
            saveOSGiPluginInCache(plugin, adv);
            
            ServiceReference sref = new ServiceReferenceImpl(plugin, peer);

            
            // call listeners
            for (Enumeration en = listener.elements(); en.hasMoreElements();)
            {
                ServiceListener svcl = (ServiceListener)en.nextElement();
                
                svcl.foundService(sref, peer);
            }
        }
        
    }

    private void saveOSGiPluginInCache(OSGiPlugin plugin, String adv)
    {
        String group = plugin.getGroup();
        String name = plugin.getName();
        String version = plugin.getVersion();
        
        // save adv in repocache
        StringBuffer sb = new StringBuffer();
        sb.append(repoCacheDir.getAbsolutePath() + 
                File.separatorChar +group);
        System.out.println("abspath: "+sb.toString());
        
        File groupdir = new File(sb.toString());
        groupdir.mkdir();
        
        sb.append(File.separatorChar + "opds");
        File opddir = new File(sb.toString());
        opddir.mkdir();
        
        sb.append(File.separatorChar + name+"-"+version+".opd");
        File pluginfile = new File(sb.toString());
        
        FileOutputStream out;
        try
        {

            pluginfile.createNewFile();
            
            out = new FileOutputStream(sb.toString());
            
            // Connect print stream to the output stream
            PrintStream p = new PrintStream( out );

            p.println(adv);
            p.close();
            
        } catch (FileNotFoundException e)
        {
            LOG.error("error in writing file");
        } catch (IOException e)
        {
            LOG.error("error in writing file");
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
//    public void bundleChanged(BundleInformation binfo, int type)
//    {
//        // create message out of this event and send it to remote peers.
//        
//    }


}
