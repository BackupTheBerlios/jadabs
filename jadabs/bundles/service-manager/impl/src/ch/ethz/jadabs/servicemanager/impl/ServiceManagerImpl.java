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
import ch.ethz.jadabs.servicemanager.ServiceAdvertisementListener;
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
    
    /** Jar Service Types */
    public static String JAR_REQ = "jarreq";
    public static String JAR_ACK = "jarack";
    
    /** Service Information */
    public static String SERVICE_ADV = "svcadv";
    public static String SERVICE_ID = "svcid";
    
    public static String SERVICE_FILTER = "svcfil";
    
    public static String SERVICE_CODE = "svccode";
    
    /** Service Running or Providing type */
    public static String SERVICE_RP_TYPE = "rptype";
    
    /** Service holder */
    public static String SERVICE_PEER = "peer";
    
    /** default Filter */
    private static String FILTER_DEFAULT = "|OPD,OBR,A";
    
    private static String SERVICE_OPD = "OPD";
    private static String SERVICE_OBR = "OBR";
    
    //  [(String filter,Set sync(HashSet(ServiceListener))]
    private Hashtable serviceAdvListeners = new Hashtable(); 
        
    /** Service Listener */
    // [(String id, ServiceListener)]
    private Hashtable mapId2SvcListener = new Hashtable();
    private Hashtable mapId2SvcReference = new Hashtable();
  
    
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
    
    public void removeListener(ServiceAdvertisementListener serviceListener)
    {       
        for(Iterator it = serviceAdvListeners.values().iterator();it.hasNext();)
        {
            Set s = (Set)it.next();
            
            for (Iterator sit = s.iterator(); sit.hasNext();)
            {
                ServiceAdvertisementListener slistener = (ServiceAdvertisementListener)sit.next();
                if (slistener.equals(serviceListener))
                    s.remove(slistener);
            }
            
            if (s.isEmpty())
                serviceAdvListeners.remove(s);
        }
        
    }
    
    
    // add filter, listener: could be duplicate
    private void addListener(String filter, ServiceAdvertisementListener serviceListener)
    {
        if (serviceAdvListeners.contains(filter))
        {
            Set set = (Set)serviceAdvListeners.get(filter);
            
            set.add(serviceListener);
        }
        else
        {
            Set s = Collections.synchronizedSet(new HashSet());
            s.add(serviceListener);
            serviceAdvListeners.put(filter, s);
        }
    }
    
    private void notifyListeners(String filter, ServiceReference sref)
    {
        Set s = (Set)serviceAdvListeners.get(filter);
        
        for (Iterator it = s.iterator(); it.hasNext(); )
        {
            ServiceAdvertisementListener slistener = (ServiceAdvertisementListener)it.next();
            
            slistener.foundService(sref);
        }
    }
    
    /*
     * Filter is defined in following EBNF:
     * (ServiceAdvFilter ",")* "|" ("OPD" ",")? ("OBR" ",")? ["R"|"P"|"A"]
     * 
     * In case filter is null default is: "|OPD,OBR,A"
     */
    public boolean getServiceAdvertisements(String filter, ServiceAdvertisementListener serviceListener)
    {
        if (filter == null)
            filter = FILTER_DEFAULT;
        
        System.out.println("printed filters: "+filter);
        
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
    public boolean getService(String fromPeer, ServiceReference sref, ServiceListener listener)
    {
        // save servicelistener for the givcen sref id
        mapId2SvcListener.put(sref.getID(), listener);
        mapId2SvcReference.put(sref.getID(), sref);
        
        // send out request
        Element[] elm = new Element[2];
        if (fromPeer != null)
        {
            elm = new Element[3];
            elm[2] = new Element(SERVICE_PEER, fromPeer, Message.JXTA_NAME_SPACE);
        }
        
        elm[0] = new Element(SERVICE_TYPE, JAR_REQ, Message.JXTA_NAME_SPACE);
        elm[1] = new Element(SERVICE_ID, sref.getID(), Message.JXTA_NAME_SPACE);
                
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

            	//send running OBRs
            if ((smfilter.indexOf("OBR".toString()) > -1) && 
                    ( ( (smfilter.indexOf(ServiceManager.RUNNING_SERVICES.toString()) > -1)) ||
                    (smfilter.indexOf(ServiceManager.ALL_SERVICES.toString()) > -1) ))
            {
                	//  enum over installed plugins
                Enumeration en = ServiceManagerActivator.bundleLoader.getBundleAdvertisements();

                matchAndSendServiceAdvertisement(en, ServiceManager.RUNNING_SERVICES, filter, SERVICE_OBR);
	            
            }
            
            	// send providing OPDs
            if ((smfilter.indexOf("OPD".toString()) > -1) && 
                    ( ( (smfilter.indexOf(ServiceManager.PROVIDING_SERVICES.toString()) > -1)) ||
                    (smfilter.indexOf(ServiceManager.ALL_SERVICES.toString()) > -1) ))
            {
                	//  enum over providing plugins
                Enumeration en = providingPlugins.elements();
                
                matchAndSendServiceAdvertisement(en, ServiceManager.PROVIDING_SERVICES, filter, SERVICE_OPD);
            }
            
            	//send providing OBRs
            if ((smfilter.indexOf("OBR".toString()) > -1) && 
                    ( ( (smfilter.indexOf(ServiceManager.PROVIDING_SERVICES.toString()) > -1)) ||
                    (smfilter.indexOf(ServiceManager.ALL_SERVICES.toString()) > -1) ))
            {
                	//  enum over providing bundles
                Enumeration en = providingBundles.elements();
                
                matchAndSendServiceAdvertisement(en, ServiceManager.PROVIDING_SERVICES, filter, SERVICE_OBR);
            }
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
    		// JAR-Request
        else if (type.equals(JAR_REQ))
        {
            String peer = new String(msg.getElement(SERVICE_PEER).getData());
            
            if (peer.equals(ServiceManagerActivator.peername))
            {
                String id = new String(msg.getElement(SERVICE_ID).getData());
                
                BundleInformation binfo = ServiceManagerActivator.bundleLoader.getBundleInfo(id);
                
	            try
	            {
	                // append file data
	                //byte[] data = getBytesFromFile(file);
	                byte[] data = binfo.getBundleCode();
	                
	                LOG.debug("file data size:"+data.length);
	                
	                Element[] elms = new Element[3];
	                
	                elms[0] = new Element(SERVICE_TYPE, 
	                        JAR_ACK, 
	                        Message.JXTA_NAME_SPACE);
	                elms[1] = new Element(SERVICE_ID, 
	                        id, Message.JXTA_NAME_SPACE);
	                elms[2] = new Element(SERVICE_CODE, 
	                        data, Message.JXTA_NAME_SPACE, Element.TEXTUTF8_MIME_TYPE);
	
			        try
			        {                    
			            ServiceManagerActivator.groupService.send(ServiceManagerActivator.groupPipe, new Message(elms));
			        } catch (IOException e)
			        {
			            LOG.debug("error in sending message");
			        }
			        
	            } catch (IOException ioe)
	            {
	                LOG.error("couldn't append File to Event", ioe);
	            }
            }
        }
        	// JAR-Ack
        else if (type.equals(JAR_ACK))
        {
	        byte[] data = msg.getElement(SERVICE_CODE).getData();
	        String id = new String(msg.getElement(SERVICE_ID).getData());
	
	        BundleInformation binfo = ServiceManagerActivator.bundleLoader.getBundleInfo(id);
	        
	        saveJarInCache(data, id, binfo);
	        
	        // TODO call bundleloader for the received bundle
	        
	        ServiceListener svcListener = (ServiceListener)mapId2SvcListener.remove(id);
	        ServiceReference sref = (ServiceReference)mapId2SvcReference.remove(id);
	        
	        svcListener.receivedService(sref);
	        
//	        ByteArrayInputStream bin = new ByteArrayInputStream(data);
//	        bc.installBundle(filename, bin);
	
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
		        System.out.println("id: "+id);
		        
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
    
    private void saveJarInCache(byte[] data, String id, BundleInformation binfo)
    {
        
        String group = id.substring(0,id.indexOf(":"));
        id = id.substring(id.indexOf(":")+1);
        String name = id.substring(0,id.indexOf(":"));
        id = id.substring(id.indexOf(":")+1);
        String version = id.substring(0,id.indexOf(":"));
        
        String filename = name + "-" + version + ".jar";
        
        
        // init folder
        StringBuffer sb = new StringBuffer();
        sb.append(repoCacheDir.getAbsolutePath() + 
                File.separatorChar +group);
        
        File groupdir = new File(sb.toString());
        groupdir.mkdir();
        
        sb.append(File.separatorChar + "jars");
        File opddir = new File(sb.toString());
        opddir.mkdir();
        
        // save file
        sb.append(File.separatorChar + filename);
        String absfilepath = sb.toString();
        
        // set filepath in BundleInformation
        binfo.setBundleCacheLocation(absfilepath);
        
	    File file = new File(absfilepath);
	    FileOutputStream fo;
        try
        {
            fo = new FileOutputStream(file);
            
            fo.write(data);
    	    fo.close();
        } catch (FileNotFoundException e)
        {
            LOG.error("could not create file outputstream: ",e);
        } catch (IOException e)
        {
            LOG.error("could not write file:",e);
        }
	
//	    RandomAccessFile raf = new RandomAccessFile(filename, "rw");
//	    raf.write(data);

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
