/*
 * Created on Jan 31, 2005
 *
 */
package ch.ethz.jadabs.servicemanager.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.bundleLoader.BundleDescriptor;
import ch.ethz.jadabs.bundleLoader.api.Descriptor;
import ch.ethz.jadabs.bundleLoader.api.InformationSource;
import ch.ethz.jadabs.bundleLoader.api.PluginFilterMatcher;
import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.pluginLoader.PluginDescriptor;
import ch.ethz.jadabs.servicemanager.ServiceAdvertisementListener;
import ch.ethz.jadabs.servicemanager.ServiceManager;
import ch.ethz.jadabs.servicemanager.ServiceReference;


/**
 * @author andfrei
 * 
 */
public class ServiceManagerImpl extends PluginFilterMatcher 
	implements ServiceManager, InformationSource, Listener
{

    private static Logger LOG = Logger.getLogger(ServiceManagerImpl.class);
    
    
    /** default Filter */
    private static String FILTER_DEFAULT = "|RP";
    
    private static String SERVICE_OPD = "OPD";
    private static String SERVICE_OBR = "OBR";
    
    //  [(String filter,Set sync(HashSet(ServiceListener))]
    private Hashtable serviceAdvListeners = new Hashtable(); 
        
    /** Service Listener */
    // [(String id, ServiceListener)]
    private Hashtable mapId2SvcListener = new Hashtable();
    private Hashtable mapId2SvcReference = new Hashtable();
  
    private Vector serviceListeners = new Vector();
    
    /** Local repository cache */
    private String repoCacheDirDefault = "./repocache/";
    private File repoCacheDir;
    
    private Hashtable serviceRefs = new Hashtable();
    
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
    public boolean getServiceAdvertisements(String peername, String filter)
    {
        sendRequest(peername, FILTER_REQ, SERVICE_FILTER, filter);
        
        return true;
    }

    public void addServiceAdvertisementListener(ServiceAdvertisementListener svcListener)
    {
        serviceListeners.add(svcListener);
    }
    
    public void removeServiceAdvertisementListener(ServiceAdvertisementListener svcListener)
    {
        serviceListeners.remove(svcListener);
    }
    
    /*
     */
    public boolean getService(String toPeer, ServiceReference sref)
    {
        // save servicelistener for the givcen sref id
//        mapId2SvcListener.put(sref.getID(), listener);
//        mapId2SvcReference.put(sref.getID(), sref);
        
        // send out request
        Element[] elm = new Element[3];
        if (toPeer != null)
        {
            elm = new Element[4];
            elm[3] = new Element(SERVICE_TO_PEER, toPeer, Message.JXTA_NAME_SPACE);
        }
        
        elm[0] = new Element(SERVICE_TYPE, JAR_REQ, Message.JXTA_NAME_SPACE);
        elm[1] = new Element(SERVICE_ID, sref.getID(), Message.JXTA_NAME_SPACE);
        elm[2] = new Element(SERVICE_FROM_PEER, ServiceManagerActivator.peername, Message.JXTA_NAME_SPACE);
                
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
        
        String topeer = new String(msg.getElement(SERVICE_TO_PEER).getData());
        String frompeer = new String(msg.getElement(SERVICE_FROM_PEER).getData());
        
        if (topeer.equals(ServiceManagerActivator.peername) || topeer.equals(ANYPEER))
        {
        
            if (type.equals(SERVICE_REQ))
            {
                String filter = new String(msg.getElement(SERVICE_FILTER).getData());
	            
	            String smfilter = filter.substring(filter.lastIndexOf("|")+1);
	             
                System.out.println("got servicereq: "+ msg.toXMLString());
	            
            	// send matching plugins
	            if ((smfilter.indexOf("OPD".toString()) > -1)) 
	            {
      		    	sendTestServiceAdvertisement(SERVICE_ACK, frompeer);
	            }

                
            }
            else if (type.equals(SERVICE_ACK))
            {
                
            }
	        	// Plugin-Request
	        //TODO include a callback function to the pluginloader
	        // to match the ExtensionPoint filter.
	        else if (type.equals(FILTER_REQ))
	        {         
	            	// match agains the provided filter
	            String filter = new String(msg.getElement(SERVICE_FILTER).getData());
	            
	            	// service manager filter
//	            String smfilter = filter.substring(filter.lastIndexOf("|")+1);
	             
            	// send matching plugins
//	            if ((smfilter.indexOf("OPD".toString()) > -1) && 
//	                    ( ( (smfilter.indexOf(ServiceManager.RUNNING_SERVICES.toString()) > -1)) ||
//	                    (smfilter.indexOf(ServiceManager.ALL_SERVICES.toString()) > -1) ))

                	//  enum over installed plugins
//                Iterator it = ServiceManagerActivator.pluginLoader.getMatchingPlugins(filter, this);
//
//                for(;it.hasNext(); )
//                {
//                    Descriptor descriptor = (Descriptor)it.next();
//                  	String id = descriptor.toString();
//                  
//      		    	InputStream instr = ServiceManagerActivator.bundleLoader.fetchInformation(id, this);
//      		    
//      		    	sendServiceAdvertisement(FILTER_ACK, frompeer, 
//      		    	        id, ServiceManager.RUNNING_SERVICES, 
//      		    	        SERVICE_FILTER, filter);
//                }
	        }
        		// Plugin-Ack
	        else if (type.equals(FILTER_ACK))
	        {            
	            String peer = new String(msg.getElement(SERVICE_PEER).getData());
	            String adv = new String(msg.getElement(SERVICE_ADV).getData());
	            
	            String rptype = new String(msg.getElement(SERVICE_RP_TYPE).getData());
	            
	            String id = new String(msg.getElement(SERVICE_ID).getData());
	            
//	            String idsuffix = id.substring(id.lastIndexOf(":") + 1);
	            
	            // save the uuid together with the adv in a serviceref
//	            ServiceAdvertisement svcAdv = ServiceAdvertisement.initAdvertisement(adv);
	            
	            ServiceReferenceImpl svcRefImpl = new ServiceReferenceImpl(id, null, peer, rptype);
	            
	            serviceRefs.put(id, svcRefImpl);
	            	            	            
	            // per default save the opd in the repocache
//	            saveServiceAdvInCache(svcAdv);
	        
	            String filter = new String(msg.getElement(SERVICE_FILTER).getData());
	            
	            // call listeners
//	            notifyListeners(filter, svcRefImpl);
	            
	            System.out.println("got info_ack: "+adv);
	        }
	        	// UUID_INFORMATION
	        else if (type.equals(INFO_REQ))
	        {
	            String uuid = new String(msg.getElement(UUID).getData());
	            
	            sendServiceAdvertisement(INFO_ACK, frompeer, 
  		    	        uuid, ServiceManager.RUNNING_SERVICES, 
  		    	        UUID, uuid);
	            
	        }
        	// UUID_INFORMATION
	        else if (type.equals(INFO_ACK))
	        {
	            String uuid = new String(msg.getElement(UUID).getData());
	            String adv = new String(msg.getElement(SERVICE_ADV).getData());
	            
	            System.out.println("got info_ack: "+adv);
	            
	        }
	    		// JAR-Request
	        else if (type.equals(JAR_REQ))
	        {
//	            String peer = new String(msg.getElement(SERVICE_PEER).getData());
	            
//                String id = new String(msg.getElement(SERVICE_ID).getData());
//                
//                BundleInformation binfo = ServiceManagerActivator.bundleLoader.getBundleInfo(id);
//                
//	            try
//	            {
//	                // append file data
//	                //byte[] data = getBytesFromFile(file);
//	                byte[] data = binfo.getBundleCode();
//	                
//	                LOG.debug("file data size:"+data.length);
//	                
//	                Element[] elms = new Element[5];
//	                
//	                elms[0] = new Element(SERVICE_TYPE, 
//	                        JAR_ACK, 
//	                        Message.JXTA_NAME_SPACE);
//	                elms[1] = new Element(SERVICE_ID, 
//	                        id, Message.JXTA_NAME_SPACE);
//	                elms[2] = new Element(SERVICE_CODE, 
//	                        data, Message.JXTA_NAME_SPACE, Element.TEXTUTF8_MIME_TYPE);
//	                elms[3] = new Element(SERVICE_FROM_PEER, 
//	                        ServiceManagerActivator.peername, 
//	                        Message.JXTA_NAME_SPACE);
//	                elms[4] = new Element(SERVICE_TO_PEER, 
//	                        frompeer, 
//	                        Message.JXTA_NAME_SPACE);
//			        try
//			        {                    
//			            ServiceManagerActivator.groupService.send(ServiceManagerActivator.groupPipe, new Message(elms));
//			        } catch (IOException e)
//			        {
//			            LOG.debug("error in sending message");
//			        }
//			        
//	            } catch (IOException ioe)
//	            {
//	                LOG.error("couldn't append File to Event", ioe);
//	            }
	        }
	        	// JAR-Ack
	        else if (type.equals(JAR_ACK))
	        {
		        byte[] data = msg.getElement(SERVICE_CODE).getData();
		        String id = new String(msg.getElement(SERVICE_ID).getData());
		
//		        BundleInformation binfo = ServiceManagerActivator.bundleLoader.getBundleInfo(id);
//		        
//		        saveJarInCache(data, id, binfo);
		        
		        // TODO call bundleloader for the received bundle
		        
//		        ServiceListener svcListener = (ServiceListener)mapId2SvcListener.remove(id);
//		        ServiceReference sref = (ServiceReference)mapId2SvcReference.remove(id);
		        
//		        svcListener.receivedService(sref);
		        
	//	        ByteArrayInputStream bin = new ByteArrayInputStream(data);
	//	        bc.installBundle(filename, bin);
		
	        }
        }
    }

    private void sendTestServiceAdvertisement(String svctype, String topeer)
    {
        
        String uuid = "jadabs-im:installee:0.7.1:opd";
        String downloadurl = "http://wlab.ethz.ch/test/installee.jad";
        String port = "5432";
        
        System.out.println("id: "+uuid);
        
        Element[] elm = new Element[6];
        
        elm[0] = new Element(SERVICE_TYPE, svctype, Message.JXTA_NAME_SPACE);
        elm[1] = new Element(SERVICE_TO_PEER, topeer, Message.JXTA_NAME_SPACE);
        elm[2] = new Element(SERVICE_FROM_PEER, ServiceManagerActivator.peername, Message.JXTA_NAME_SPACE);		        
        elm[3] = new Element(UUID, uuid, Message.JXTA_NAME_SPACE);
        elm[4] = new Element(DOWNLOAD_URL, downloadurl, Message.JXTA_NAME_SPACE);
        elm[5] = new Element("port", port, Message.JXTA_NAME_SPACE);
        //        elm[6] = new Element(SERVICE_ADV, adv, Message.JXTA_NAME_SPACE);
        
        try
        {                    
            ServiceManagerActivator.groupService.send(ServiceManagerActivator.groupPipe, new Message(elm));
        } catch (IOException e)
        {
            LOG.debug("error in sending message");
        }
        
    }
    
    private void sendServiceAdvertisement(String svctype, String topeer, 
            String uuid, String rptype, String name, String value)
    {
        
	    InputStream instr = ServiceManagerActivator.bundleLoader.fetchInformation(uuid, this);
	    
	    String adv = inputStream2String(instr);
        
        System.out.println("id: "+uuid);
        
        Element[] elm = new Element[7];
        
        elm[0] = new Element(SERVICE_TYPE, svctype, Message.JXTA_NAME_SPACE);
        elm[1] = new Element(SERVICE_RP_TYPE, rptype, Message.JXTA_NAME_SPACE);
        elm[2] = new Element(name, value, Message.JXTA_NAME_SPACE);
        elm[3] = new Element(SERVICE_TO_PEER, topeer, Message.JXTA_NAME_SPACE);
        elm[4] = new Element(SERVICE_FROM_PEER, ServiceManagerActivator.peername, Message.JXTA_NAME_SPACE);		        
        elm[5] = new Element(SERVICE_ID, uuid, Message.JXTA_NAME_SPACE);
        
        elm[6] = new Element(SERVICE_ADV, adv, Message.JXTA_NAME_SPACE);
        
        try
        {                    
            ServiceManagerActivator.groupService.send(ServiceManagerActivator.groupPipe, new Message(elm));
        } catch (IOException e)
        {
            LOG.debug("error in sending message");
        }
        
    }
    
    private String inputStream2String(InputStream instream)
    {
        int k; 
        int aBuffSize = 1123123; 
        String StringFromWS=""; 
        
        byte buff[] = new byte[aBuffSize]; 
        OutputStream xOutputStream = new ByteArrayOutputStream(aBuffSize); 
        try
        {
            while ( (k=instream.read(buff) ) != -1) 
                xOutputStream.write(buff,0,k);
            
        } catch (IOException e)
        {
            LOG.error("could not create string from instream");
            return "";
        }

        return xOutputStream.toString();
    }
    
    private void saveJarInCache(byte[] data, String uuid)
    {
        String[] args = uuid.split(":");
        String group = args[0];
        String name = args[1];
        String version = args[2];
        String type = args[3];
        
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
//        binfo.setBundleCacheLocation(absfilepath);
        
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
    
    private void saveServiceAdvInCache(String uuid, String adv)
    {
        String[] args = uuid.split(":");
        String group = args[0];
        String name = args[1];
        String version = args[2];
        String type = args[3];
        
        
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
    
    private void sendRequest(String peername, String svcType, String name, String value)
    {        
//        addListener(filter, serviceListener);
        
        Element[] elm = new Element[4];
        
        elm[0] = new Element(SERVICE_TYPE, svcType, Message.JXTA_NAME_SPACE);
        elm[1] = new Element(name, value, Message.JXTA_NAME_SPACE);
        elm[2] = new Element(SERVICE_TO_PEER, peername, Message.JXTA_NAME_SPACE);
        elm[3] = new Element(SERVICE_FROM_PEER, ServiceManagerActivator.peername, Message.JXTA_NAME_SPACE);
        
        try
        {
            LOG.debug("send servicemanager message");
            
            ServiceManagerActivator.groupService.send(
                    ServiceManagerActivator.groupPipe, 
                    new Message(elm));
            
        } catch (IOException e)
        {
            LOG.debug("error in sending message");
        }
    }
    
    //---------------------------------------------------
    // implemente PluginFilterMatcher abstract methods
    //---------------------------------------------------
    
    /* (non-Javadoc)
     * @see ch.ethz.jadabs.bundleLoader.api.PluginFilterMatcher#debug(java.lang.String)
     */
    protected void debug(String str)
    {
        // TODO remove this mehod if not used anymore in pluginfiltermatcher
        
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.bundleLoader.api.PluginFilterMatcher#error(java.lang.String)
     */
    protected void error(String str)
    {
        // TODO remove this mehod if not used anymore in pluginfiltermatcher
        
    }
    
    //---------------------------------------------------
    // implement InformationSource interface
    //---------------------------------------------------

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.bundleLoader.api.InformationSource#retrieveInformation(java.lang.String)
     */
    public InputStream retrieveInformation(String uuid)
    {
        
        sendRequest(ANYPEER, INFO_REQ, UUID, uuid);
        
        return null;
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.bundleLoader.api.InformationSource#retrieveInformation(java.lang.String, java.lang.String)
     */
    public InputStream retrieveInformation(String uuid, String source)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.bundleLoader.api.InformationSource#getMatchingPlugins(java.lang.String)
     */
    public Iterator getMatchingPlugins(String filter)
    {
        
        if (filter == null)
            filter = FILTER_DEFAULT;
        
        sendRequest(ANYPEER, FILTER_REQ, SERVICE_FILTER, filter);
        
        
        return null;
    }

    
}
