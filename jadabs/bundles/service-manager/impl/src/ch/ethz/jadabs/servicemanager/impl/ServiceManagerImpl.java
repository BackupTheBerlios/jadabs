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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.bundleloader.BundleInformation;
import ch.ethz.jadabs.bundleloader.ServiceAdvertisement;
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
    
    /** Service Types */
    public static String SERVICE_REQ = "svcreq";
    public static String SERVICE_ACK = "svcack";
    
    /** Service Information */
    public static String SERVICE_ADV = "svcadv";
    public static String SERVICE_ID = "svcid";
    
    public static String SERVICE_FILTER = "svcfil";
    
    /** Service Running or Providing type */
    public static String SERVICE_RP_TYPE = "rptype";
    
    /** Service holder */
    public static String SERVICE_PEER = "peer";
    
    /** default Filter */
    private static String FILTER_DEFAULT = "|OPD,OBR,A";
    
    private static String SERVICE_OPD = "OPD";
    private static String SERVICE_OBR = "OBR";
    
    //  [(String filter,Set sync(HashSet(ServiceListener))]
    private Hashtable serviceListeners = new Hashtable(); 
        
    /** Local repository cache */
    private String repoCacheDirDefault = "./repocache/";
    private File repoCacheDir;
    
    /** providing OPDs */
    private Vector providingPlugins = new Vector();
    
    /** providing OBRS */
    private Vector providingBundles = new Vector();
    
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
    
    public void removeListener(ServiceListener serviceListener)
    {       
        for(Iterator it = serviceListeners.values().iterator();it.hasNext();)
        {
            Set s = (Set)it.next();
            
            for (Iterator sit = s.iterator(); sit.hasNext();)
            {
                ServiceListener slistener = (ServiceListener)sit.next();
                if (slistener.equals(serviceListener))
                    s.remove(slistener);
            }
            
            if (s.isEmpty())
                serviceListeners.remove(s);
        }
        
    }
    
    
    // add filter, listener: could be duplicate
    private void addListener(String filter, ServiceListener serviceListener)
    {
        if (serviceListeners.contains(filter))
        {
            Set set = (Set)serviceListeners.get(filter);
            
            set.add(serviceListener);
        }
        else
        {
            Set s = Collections.synchronizedSet(new HashSet());
            s.add(serviceListener);
            serviceListeners.put(filter, s);
        }
    }
    
    private void notifyListeners(String filter, ServiceReference sref)
    {
        Set s = (Set)serviceListeners.get(filter);
        
        for (Iterator it = s.iterator(); it.hasNext(); )
        {
            ServiceListener slistener = (ServiceListener)it.next();
            
            slistener.foundService(sref);
        }
    }
    
    /*
     * Filter is defined in following EBNF:
     * (ServiceAdvFilter ",")* "|" ("OPD" ",")? ("OBR" ",")? ["R"|"P"|"A"]
     * 
     * In case filter is null default is: "|OPD,OBR,A"
     */
    public boolean getServices(String filter, ServiceListener serviceListener)
    {
        if (filter == null)
            filter = FILTER_DEFAULT;
        
        addListener(filter, serviceListener);
        
        Element[] elm = new Element[2];
        
        elm[0] = new Element(SERVICE_TYPE, SERVICE_REQ, Message.JXTA_NAME_SPACE);
        elm[1] = new Element(SERVICE_FILTER, filter, Message.JXTA_NAME_SPACE);
        
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
        
        	// Plugin-Request
        //TODO include a callback function to the pluginloader
        // to match the ExtensionPoint filter.
        if (type.equals(SERVICE_REQ))
        {         
            	// match agains the provided filter
            String filter = new String(msg.getElement(SERVICE_FILTER).getData());
            
            	// service manager filter
            String smfilter = filter.substring(filter.lastIndexOf("|")+1);
                        
            	// send running OPDs
            if ((smfilter.indexOf("OPD".toString()) > -1) && 
                    ( ( (smfilter.indexOf(ServiceManager.RUNNING_SERVICES.toString()) > -1)) ||
                    (smfilter.indexOf(ServiceManager.ALL_SERVICES.toString()) > -1) ))
            {
                	//  enum over installed plugins
                Enumeration en = ServiceManagerActivator.pluginLoader.getOSGiPlugins();

                matchAndSendServiceAdvertisement(en, ServiceManager.RUNNING_SERVICES, filter, SERVICE_OPD);
	            
            }

// TODO buggy
            	//send running OBRs
//            if ((smfilter.indexOf("OBR".toString()) > -1) && 
//                    ( ( (smfilter.indexOf(ServiceManager.RUNNING_SERVICES.toString()) > -1)) ||
//                    (smfilter.indexOf(ServiceManager.ALL_SERVICES.toString()) > -1) ))
//            {
//                	//  enum over installed plugins
//                Enumeration en = ServiceManagerActivator.bundleLoader.getBundleAdvertisements();
//
//                matchAndSendServiceAdvertisement(en, ServiceManager.RUNNING_SERVICES, filter, SERVICE_OBR);
//	            
//            }
            
            	// send providing OPDs
            if ((smfilter.indexOf("OPD".toString()) > -1) && 
                    ( ( (smfilter.indexOf(ServiceManager.PROVIDING_SERVICES.toString()) > -1)) ||
                    (smfilter.indexOf(ServiceManager.ALL_SERVICES.toString()) > -1) ))
            {
                	//  enum over providing plugins
                Enumeration en = providingPlugins.elements();
                
                matchAndSendServiceAdvertisement(en, ServiceManager.PROVIDING_SERVICES, filter, SERVICE_OPD);
            }

// TODO buggy               
            	//send providing OBRs
//            if ((smfilter.indexOf("OBR".toString()) > -1) && 
//                    ( ( (smfilter.indexOf(ServiceManager.PROVIDING_SERVICES.toString()) > -1)) ||
//                    (smfilter.indexOf(ServiceManager.ALL_SERVICES.toString()) > -1) ))
//            {
//                	//  enum over providing bundles
//                Enumeration en = providingBundles.elements();
//                
//                matchAndSendServiceAdvertisement(en, ServiceManager.PROVIDING_SERVICES, filter, SERVICE_OBR);
//            }
        }
        	// Plugin-Ack
        else if (type.equals(SERVICE_ACK))
        {            
            String peer = new String(msg.getElement(SERVICE_PEER).getData());
            String adv = new String(msg.getElement(SERVICE_ADV).getData());
            
            String rptype = new String(msg.getElement(SERVICE_RP_TYPE).getData());
            
            String id = new String(msg.getElement(SERVICE_ID).getData());
            
            String idsuffix = id.substring(id.lastIndexOf(":") + 1);
            
            ServiceAdvertisement svcAdv = ServiceAdvertisement.initAdvertisement(adv);
 
            if (idsuffix.equals("opd"))
                svcAdv = OSGiPlugin.initAdvertisement(adv);
            else if (idsuffix.equals("obr"))
                svcAdv = BundleInformation.initAdvertisement(adv);
            else
                LOG.error("unknown id type:"+id);
            
//            svcAdv = ServiceManagerActivator.pluginLoader.parsePluginAdvertisement(adv);
//            plugin.setAdvertisement(adv);
            
            // per default save the opd in the repocache
            saveServiceAdvInCache(svcAdv);
            
            ServiceReference sref = new ServiceReferenceImpl(svcAdv, peer, rptype);

            String filter = new String(msg.getElement(SERVICE_FILTER).getData());
            
            // call listeners
            notifyListeners(filter, sref);
            
        }
        
    }

    private void matchAndSendServiceAdvertisement(Enumeration en, String rptype, String filter, String service)
    {
        
        // send a message for each running service, 
        // with opd adv
        for(;en.hasMoreElements(); )
        {
            ServiceAdvertisement svcAdv = (ServiceAdvertisement)en.nextElement();
        
            // match serviceAdvertisement against the service filter
        	// extension point filter
            String svcfilter = filter.substring(0,filter.lastIndexOf("|"));
            
            // if match then send
            if (svcAdv.matches(svcfilter))
            {
		        String adv = svcAdv.getAdvertisement();
		                     
		        String id = svcAdv.getID();
		        
		        Element[] elm = new Element[6];
		        
		        elm[0] = new Element(SERVICE_TYPE, SERVICE_ACK, Message.JXTA_NAME_SPACE);
		        elm[1] = new Element(SERVICE_ADV, adv, Message.JXTA_NAME_SPACE);
		        elm[2] = new Element(SERVICE_RP_TYPE, rptype, Message.JXTA_NAME_SPACE);
		        elm[3] = new Element(SERVICE_FILTER, filter, Message.JXTA_NAME_SPACE);
		        elm[4] = new Element(SERVICE_PEER, ServiceManagerActivator.peername, Message.JXTA_NAME_SPACE);
		        elm[5] = new Element(SERVICE_ID, id, Message.JXTA_NAME_SPACE);
		        
		        try
		        {                    
		            ServiceManagerActivator.groupService.send(ServiceManagerActivator.groupPipe, new Message(elm));
		        } catch (IOException e)
		        {
		            LOG.debug("error in sending message");
		        }
            }
        
        }
    }
    
    private void saveServiceAdvInCache(ServiceAdvertisement sref)
    {
        String group = sref.getGroup();
        String name = sref.getName();
        String version = sref.getVersion();
        
        String adv = sref.getAdvertisement();
        
        // save adv in repocache
        StringBuffer sb = new StringBuffer();
        sb.append(repoCacheDir.getAbsolutePath() + 
                File.separatorChar +group);
        
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
