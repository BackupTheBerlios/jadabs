/*
 * Created on Jan 31, 2005
 *
 */
package ch.ethz.jadabs.servicemanager.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.bundleLoader.api.InformationSource;
import ch.ethz.jadabs.bundleLoader.api.PluginFilterMatcher;
import ch.ethz.jadabs.bundleLoader.api.Utilities;
import ch.ethz.jadabs.jxme.DiscoveryListener;
import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.Peer;
import ch.ethz.jadabs.pluginLoader.PluginDescriptor;
import ch.ethz.jadabs.servicemanager.ServiceAdvertisementListener;
import ch.ethz.jadabs.servicemanager.ServiceManager;
import ch.ethz.jadabs.servicemanager.ServiceReference;


/**
 * @author andfrei
 * 
 */
public class ServiceManagerImpl extends PluginFilterMatcher 
	implements ServiceManager, InformationSource, Listener, DiscoveryListener
{

    private static Logger LOG = Logger.getLogger(ServiceManagerImpl.class);
    
    
    /** default Filter */
//    private static String FILTER_DEFAULT = "|RP";
    
    private static String SERVICE_OPD = "OPD";
    private static String SERVICE_OBR = "OBR";
    
    //  [(String filter,Set sync(HashSet(ServiceListener))]
    private Hashtable serviceAdvListeners = new Hashtable(); 
        
    /** Service Listener */
    // [(String id, ServiceListener)]
    private Hashtable mapId2SvcListener = new Hashtable();
    private Hashtable mapId2SvcReference = new Hashtable();
      
    /** Local repository cache */
    private String repoCacheDirDefault = "./repository/";
    private File repoCacheDir;
    
    private Hashtable uuid2svcref = new Hashtable();
    
    /** providing OPDs */
    private Vector providingPlugins = new Vector();
    
    /** providing OBRS */
    private Vector providingBundles = new Vector();
    
    private Hashtable awaitUUID2InfoReqs = new Hashtable();
        
//    private InputStream inforeq = null;
    
    private static final int BUFFER_SIZE = 4096;
    
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
//        synchronized(serviceAdvListeners)
//        {
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
//        }
    }
    
    
    // add filter, listener: could be duplicate
    private void addListener(String filter, ServiceAdvertisementListener serviceListener)
    {
//        synchronized(serviceAdvListeners)
//        {
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
//        }
    }
    
    private void notifyListeners(String filter, ServiceReference sref)
    {
        
//        synchronized(serviceAdvListeners)
//        {
	        Set s = (Set)serviceAdvListeners.get(filter);
	        
	        for (Iterator it = s.iterator(); it.hasNext(); )
	        {
	            ServiceAdvertisementListener slistener = (ServiceAdvertisementListener)it.next();
	            
	            slistener.foundService(sref);
	        }
//        }
    }
    
    /*
     * Filter is defined in following EBNF:
     * (ServiceAdvFilter ",")* "|" ("OPD" ",")? ("OBR" ",")? ["R"|"P"|"A"]
     * 
     * In case filter is null default is: "|OPD,OBR,A"
     */
    public boolean getServiceAdvertisements(String peername, String filter, ServiceAdvertisementListener listener)
    {   
        
        addListener(filter, listener);
        
        sendRequest(peername, FILTER_REQ, SERVICE_FILTER, filter, null, null);
        
        return true;
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
    public void addProvidingService(String uuid)
    {
        InputStream in = ServiceManagerActivator.pluginLoader.fetchInformation(uuid,this);
        
        String adv = inputStream2String(in);
        
        sendRequest(ANYPEER, SERVICE_ADV, ADV_DESCRIPTOR, adv, SERVICE_ID, uuid);
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
//        LOG.debug("handle message: service manager"+ msg.toXMLString());
        
        Element typel = msg.getElement(SERVICE_TYPE);
        
        if (typel != null)
        {
	        String type = new String(typel.getData());
	        
	        String topeer = new String(msg.getElement(SERVICE_TO_PEER).getData());
	        String frompeer = new String(msg.getElement(SERVICE_FROM_PEER).getData());
	        
	        LOG.debug("topeer: "+topeer +" frompeer: "+frompeer);
	        
            LOG.debug(msg.toXMLString());
            
	        if (!frompeer.equals(ServiceManagerActivator.peername) && (topeer.equals(ServiceManagerActivator.peername) || topeer.equals(ANYPEER)))
	        {
	        
	            if (type.equals(SERVICE_ADV))
	            {
		            String adv = new String(msg.getElement(ADV_DESCRIPTOR).getData());
		            String id = new String(msg.getElement(SERVICE_ID).getData());
		                     
		            ServiceReferenceImpl svcRefImpl = new ServiceReferenceImpl(id, adv, frompeer, PROVIDING_SERVICES);
		           	            
		            LOG.debug("got service: "+id);
		            
	//	            synchronized(uuid2svcref)
	//	            {
			            if (!uuid2svcref.contains(id))
			            {
			                uuid2svcref.put(id, svcRefImpl);
			                // per default save the opd in the repocache
				            saveServiceAdvInCache(id, adv);
			            }
	//	            }
		            
		            
	                final String fid = id;
	                final String fadv = adv;
	                
	                // try to load and activate if it machtes
	                // has to be done in a thread, or it blocks further
	                // requests
	                new Thread(){ 
	                    public void run(){
	                        try
	                        {
	                            ServiceManagerActivator.pluginLoader.
	                            	loadPluginIfMatches(fid, 
	                            	        new ByteArrayInputStream(fadv.getBytes()));
	                            
	                            
	                        } catch (Exception e)
	                        {
	                            LOG.error("could not install");
	                        }
	                    }
	                }.start();
	                
		            	            	            
		            
	            }
	            else if (type.equals(NEWFILTER_REQ))
	            {
	                String filter = new String(msg.getElement(SERVICE_FILTER).getData());
	                
	                String exps = filter.substring(0,filter.indexOf("|"));
	                String platformrest = filter.substring(filter.indexOf("|"));
	                
	                StringTokenizer st = new StringTokenizer(exps, ",");
	                String exp;
                                        
	                while(st.hasMoreTokens())
	                {
                        exp = st.nextToken();
                        
	                    String newfilter = exp + " | " + platformrest + " | " + "R";
	                    try {
	                        Iterator it = ServiceManagerActivator.pluginLoader.getMatchingPlugins(
			                    newfilter, this);
	                        
	                        sendAdvertisements(it, NEWFILTER_ACK, INSTALLED_SERVICES, frompeer, filter);
	                        
	                    } catch(Exception e)
	                    { }
	                }
	                
	            }
	            else if (type.equals(NEWFILTER_ACK))
	            {
	                             
	             	String adv = new String(msg.getElement(ADV_DESCRIPTOR).getData());
		            String rptype = new String(msg.getElement(SERVICE_RP_TYPE).getData());
		            
		            String id = new String(msg.getElement(SERVICE_ID).getData());
		                     
		            LOG.debug("NEWFILTER_ACK plugin: "+id);
		            
		            ServiceReferenceImpl svcRefImpl = new ServiceReferenceImpl(id, adv, frompeer, rptype);
		            
		            // per default save the opd in the repocache
//		            saveServiceAdvInCache(id, adv);
		            
		            try
                    {
                        // needs to be installed
                        ServiceManagerActivator.pluginLoader.loadPlugin(id);
                    } catch (Exception e)
                    {
                        LOG.error("could not install plugin: "+id);
                    }
		            
		            
	            }
		        	// Plugin Filter-Request
		        //TODO include a callback function to the pluginloader
		        // to match the ExtensionPoint filter.
		        else if (type.equals(FILTER_REQ))
		        {         
		            	// match agains the provided filter
		            String filter = new String(msg.getElement(SERVICE_FILTER).getData());
		            
		            // transform | into | due to problems with nokia
//		            if (filter.indexOf('|') > -1)
//		                filter = filter.replace('|','|');
		            
		            String smfilter = filter.substring(filter.lastIndexOf("|")+1);
		            
		            	            
		            Iterator it = null;
		            String rptype = "";
		            if ((smfilter.indexOf("OPD".toString()) > -1) &&
		            	smfilter.indexOf("INS") > -1)
		            {
		                it = ServiceManagerActivator.pluginLoader.getInstalledPlugins();
		                rptype = INSTALLED_SERVICES;
		            }
		            sendAdvertisements(it, FILTER_ACK, rptype, frompeer, filter);
		            
		            if ((smfilter.indexOf("OPD".toString()) > -1) &&
			            	smfilter.indexOf("PRO") > -1)
		            {
			            // check for matching providing plugins
			            try
			            {
				            it = ServiceManagerActivator.pluginLoader.getMatchingPlugins(
				                    filter, this);
				            rptype = PROVIDING_SERVICES;
			            } catch(Exception e)
			            {
			                LOG.error("could not get plugins",e);
			            }
		            }
		            sendAdvertisements(it,FILTER_ACK, rptype, frompeer, filter);
		            
		            if ((smfilter.indexOf("OBR".toString()) > -1) &&
			            	smfilter.indexOf("INS") > -1)
		            {
			            // check for installed OBRs
			            try
			            {
				            it = ServiceManagerActivator.bundleLoader.getInstalledBundles();
				            rptype = INSTALLED_SERVICES;
			            } catch(Exception e)
			            {
			                LOG.error("could not get plugins",e);
			            }
		            }
		            sendAdvertisements(it,FILTER_ACK, rptype, frompeer, filter);
		            
		            
		        
		        }
	        		// Plugin Filter-Ack
		        else if (type.equals(FILTER_ACK))
		        {            
		            String adv = new String(msg.getElement(ADV_DESCRIPTOR).getData());
		            String rptype = new String(msg.getElement(SERVICE_RP_TYPE).getData());
		            
		            String id = new String(msg.getElement(SERVICE_ID).getData());
		                     
		            ServiceReferenceImpl svcRefImpl = new ServiceReferenceImpl(id, adv, frompeer, rptype);
		            
		            // per default save the opd in the repocache
		            saveServiceAdvInCache(id, adv);
		        
		            String filter = new String(msg.getElement(SERVICE_FILTER).getData());
		            
		            //TODO notify listeners call listeners
	//	            notifyListeners(filter, svcRefImpl);
		            
	//	            synchronized(uuid2svcref)
	//	            {
		                uuid2svcref.put(id, svcRefImpl);
	//	            }
		            
		        }
		        	// OBR_REQ
		        else if (type.equals(OBR_REQ))
		        {
		            String uuid = new String(msg.getElement(SERVICE_ID).getData());
		            
		            // lookup obr advertisement
		            
		            InputStream in = ServiceManagerActivator.bundleLoader.fetchInformation(uuid,this);
		            
		            if (in != null)
		            {
			            String adv = inputStream2String(in);
			            
			            sendRequest(frompeer, OBR_ACK, 
			            	SERVICE_ID, uuid,  
		  		    	    ADV_DESCRIPTOR, adv);
		            }
		            
		        }
	        		// OBR_ACK
		        else if (type.equals(OBR_ACK))
		        {
		            
		            String uuid = new String(msg.getElement(SERVICE_ID).getData());
		            String adv = new String(msg.getElement(ADV_DESCRIPTOR).getData());
		            
		            saveServiceAdvInCache(uuid, adv);
		            
		            InputStream inStream = new ByteArrayInputStream(adv.getBytes());
		            awaitUUID2InfoReqs.put(uuid, inStream);
		            	            
		        }
		    		// JAR-Request
		        else if (type.equals(JAR_REQ))
		        {	            
	                String id = new String(msg.getElement(SERVICE_ID).getData());
	                
	                InputStream in = ServiceManagerActivator.bundleLoader.fetchInformation(id, this);
	                
	                // append file data
	                //byte[] data = getBytesFromFile(file);
	                try {
		                byte[] data = copyToByteArray(in);
		                
		                LOG.debug("file data size:"+data.length);
		                
		                Element[] elms = new Element[5];
		                
		                elms[0] = new Element(SERVICE_TYPE, 
		                        JAR_ACK, 
		                        Message.JXTA_NAME_SPACE);
		                elms[1] = new Element(SERVICE_ID, 
		                        id, Message.JXTA_NAME_SPACE);
		                elms[2] = new Element(SERVICE_CODE, 
		                        data, Message.JXTA_NAME_SPACE, Element.TEXTUTF8_MIME_TYPE);
		                elms[3] = new Element(SERVICE_FROM_PEER, 
		                        ServiceManagerActivator.peername, 
		                        Message.JXTA_NAME_SPACE);
		                elms[4] = new Element(SERVICE_TO_PEER, 
		                        frompeer, 
		                        Message.JXTA_NAME_SPACE);
			                           
			            ServiceManagerActivator.groupService.send(ServiceManagerActivator.groupPipe, new Message(elms));
			        } catch (IOException e)
			        {
			            LOG.debug("error in sending message");
			        }
				        
		        }
		        	// JAR-Ack
		        else if (type.equals(JAR_ACK))
		        {
			        byte[] data = msg.getElement(SERVICE_CODE).getData();
			        String id = new String(msg.getElement(SERVICE_ID).getData());
			
			        saveJarInCache(data, id);
			        
		            InputStream inStream = new ByteArrayInputStream(data);
		            awaitUUID2InfoReqs.put(id, inStream);
			        
		        }
	        }
        }
    }

	/**
	 * Copy the contents of the given InputStream to the given OutputStream.
	 * Closes both streams when done.
	 * @param in the stream to copy from
	 * @param out the stream to copy to
	 * @throws IOException in case of I/O errors
	 */
	public static void copy(InputStream in, OutputStream out) throws IOException {

		try {
			byte[] buffer = new byte[BUFFER_SIZE];
			int nrOfBytes = -1;
			while ((nrOfBytes = in.read(buffer)) != -1) {
				out.write(buffer, 0, nrOfBytes);
			}
			out.flush();
		}
		finally {
			try {
				in.close();
			}
			catch (IOException ex) {
				LOG.warn("Could not close InputStream", ex);
			}
			try {
				out.close();
			}
			catch (IOException ex) {
				LOG.warn("Could not close OutputStream", ex);
			}
		}
	}

	/**
	 * Copy the contents of the given byte array to the given OutputStream.
	 * Closes the stream when done.
	 * @param in the byte array to copy from
	 * @param out the OutputStream to copy to
	 * @throws IOException in case of I/O errors
	 */
	public static void copy(byte[] in, OutputStream out) throws IOException {

		try {
			out.write(in);
		}
		finally {
			try {
				out.close();
			}
			catch (IOException ex) {
				LOG.warn("Could not close OutputStream", ex);
			}
		}
	}

	/**
	 * Copy the contents of the given InputStream into a new byte array.
	 * Closes the stream when done.
	 * @param in the stream to copy from
	 * @return the new byte array that has been copied to
	 * @throws IOException in case of I/O errors
	 */
	public static byte[] copyToByteArray(InputStream in) throws IOException 
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
		copy(in, out);
		return out.toByteArray();
	}
    
    private void sendAdvertisements(Iterator it, String type, String rptype, String frompeer, String filter)
    {
        while(it != null && it.hasNext())
        {
            String id = (String)it.next();
          
//            LOG.debug("id to send: "+id);
            
		    InputStream instr = ServiceManagerActivator.bundleLoader.fetchInformation(id, this);
		    if (instr == null)
		        instr = retrieveCachedInformation(id);
		    	
	    	sendServiceAdvertisement(type, frompeer, 
	    	        id, rptype, 
	    	        SERVICE_FILTER, filter);
        }
    }
    
    private void sendServiceAdvertisement(String svctype, String topeer, 
            String uuid, String rptype, String name, String value)
    {
        
	    InputStream instr = ServiceManagerActivator.bundleLoader.fetchInformation(uuid, this);
	    
	    String adv = inputStream2String(instr);
                
        Element[] elm = new Element[7];
        
        elm[0] = new Element(SERVICE_TYPE, svctype, Message.JXTA_NAME_SPACE);
        elm[1] = new Element(SERVICE_RP_TYPE, rptype, Message.JXTA_NAME_SPACE);
        elm[2] = new Element(name, value, Message.JXTA_NAME_SPACE);
        elm[3] = new Element(SERVICE_TO_PEER, topeer, Message.JXTA_NAME_SPACE);
        elm[4] = new Element(SERVICE_FROM_PEER, ServiceManagerActivator.peername, Message.JXTA_NAME_SPACE);		        
        elm[5] = new Element(SERVICE_ID, uuid, Message.JXTA_NAME_SPACE);
        
        elm[6] = new Element(ADV_DESCRIPTOR, adv, Message.JXTA_NAME_SPACE);
        
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
        int aBuffSize = 1024; 
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
        String[] args = Utilities.split(uuid, ":");
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
        String[] args = Utilities.split(uuid,":");
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
        
        File svcdir = null;
        if (type.equals("opd"))
        {
	        sb.append(File.separatorChar + "opds");
	        svcdir = new File(sb.toString());
	        sb.append(File.separatorChar + name+"-"+version+".opd");
        }
        else if (type.equals("obr"))
        {
	        sb.append(File.separatorChar + "obrs");
	        svcdir = new File(sb.toString());
	        sb.append(File.separatorChar + name+"-"+version+".obr");
        }
        
        if (svcdir != null)
        {
	        svcdir.mkdir();
	        
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

    }
    
    
    private void sendRequest(String peername, String svcType, 
            String name, String value, String name2, String value2)
    {        
//        addListener(filter, serviceListener);
        
        Element[] elm;
        if (name2 == null)
            elm = new Element[4];
        else
            elm = new Element[5];
        
        elm[0] = new Element(SERVICE_TYPE, svcType, Message.JXTA_NAME_SPACE);
        elm[1] = new Element(name, value, Message.JXTA_NAME_SPACE);
        elm[2] = new Element(SERVICE_TO_PEER, peername, Message.JXTA_NAME_SPACE);
        elm[3] = new Element(SERVICE_FROM_PEER, ServiceManagerActivator.peername, Message.JXTA_NAME_SPACE);
        
        if (name2 != null)
        {
            elm[4] = new Element(name2, value2, Message.JXTA_NAME_SPACE); 
        }
        
        try
        {
            Message msg = new Message(elm);
            
//            LOG.debug("send servicemanager message: "+ msg.toXMLString());
            
            
            ServiceManagerActivator.groupService.send(
                    ServiceManagerActivator.groupPipe, 
                    msg);
            
        } catch (IOException e)
        {
            LOG.debug("error in sending message");
        }
    }
    
    /*
     */
    public void handleSearchResponse(NamedResource namedResource)
    {
        
        LOG.debug("found new namedresource: "+namedResource.getName());
        
        
        if (namedResource instanceof Peer)
        {
	        
            // request plugins from newcommer
	        Iterator it =
	            ServiceManagerActivator.pluginLoader.getInstalledPlugins();
            
	        for (;it.hasNext();)
	        {
	            String pluginid = (String)it.next();
	            
	            try {
		            PluginDescriptor pd = 
		                ServiceManagerActivator.pluginLoader.getPluginDescriptor(pluginid);
		            
		            Iterator epit = pd.getExtensionPoints();
		            StringBuffer sb = new StringBuffer();
		            
		            for (;epit.hasNext();)
		            {
		                sb.append(epit.next());
                        
                        if (epit.hasNext())
                            sb.append(",");
		            }
		            
	                String filter= sb.toString() + " | " + 
	                	ServiceManagerActivator.pluginLoader.getPlatform() + " | " + "R";
		                
	                sendRequest(namedResource.getName(), 
		                        NEWFILTER_REQ, SERVICE_FILTER, filter, null, null);
			            
	            } catch (Exception e)
	            {
	                
	            }
	        }
            
            // push providing plugins to new commers
//	        Iterator it = ServiceManagerActivator.pluginLoader.getProvidingPlugins();
//	        for (;it.hasNext();)
//	        {
//	            String pluginid = (String)it.next();
//	        	
//	            LOG.debug("plugin: "+pluginid);
//	            
//	            InputStream is = 
//	                ServiceManagerActivator.pluginLoader.fetchInformation(pluginid, this);
//	            
//	            String pd = inputStream2String(is);
//	            
//	            sendRequest(namedResource.getName(),
//	                    SERVICE_ADV, ADV_DESCRIPTOR, pd,
//	                    SERVICE_ID, pluginid);
//	            
//	        }
        
        }
    }
    

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.jxme.DiscoveryListener#handleNamedResourceLoss(ch.ethz.jadabs.jxme.NamedResource)
     */
    public void handleNamedResourceLoss(NamedResource namedResource)
    {
        LOG.debug("lost namedResource: "+namedResource.getName());
        
        // remove all svcrefs of this peer
        if (namedResource instanceof Peer)
        {
            Peer peer = (Peer)namedResource;
            
            String pname = peer.getName();
            
//            synchronized(uuid2svcref)
//            {
	            for (Iterator it = uuid2svcref.values().iterator(); 
	            	it.hasNext();)
	            {
	                ServiceReference sref = (ServiceReference)it.next();
	                
	                if (pname.equals(sref.getPeer()))
	                    uuid2svcref.remove(sref.getID());
	                              
	            }
//            }
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
     
        // dispatch between opd,obr,jar
                        
        InputStream in = retrieveCachedInformation(uuid);
        
        if (in != null)
            return in;
        
        // do remote request
        
        // lookup  the peer for the serviceid
        String opdid = null;
        if (uuid.indexOf(":opd") < -1 )
        {
            // try to get the sref from the uuid:opd
            opdid = uuid.substring(0, uuid.lastIndexOf(":")) +":opd";
        }
        else
            opdid = uuid;
        
        ServiceReference sref = (ServiceReference)uuid2svcref.get(opdid);
        String pname;
        if (sref != null)
            pname = sref.getPeer();
        else
            pname = ANYPEER;
        
        if (sref != null && uuid.indexOf(":opd") > -1)
        {
            return new ByteArrayInputStream(
                    (((ServiceReferenceImpl)sref).getAdvertisement()).getBytes());
                                   
        }
        else if (uuid.indexOf(":jar") > -1 )
        {        
            sendRequest(pname, JAR_REQ, SERVICE_ID, uuid, null, null);
            
            return awaitInformation(uuid);
        }
        else if (uuid.indexOf(":obr") > -1)
        {
            sendRequest(pname, OBR_REQ, SERVICE_ID, uuid, null, null);
            
            return awaitInformation(uuid);
        }
            
        return null;
        
    }

    private InputStream awaitInformation(String uuid)
    {
        try
        {
        	int timeout = 0;
        	int tsleeptime = 2000;
        	int maxsleeptime = 3*tsleeptime;
        	InputStream inforeq = null;
            while(inforeq == null && timeout <= maxsleeptime)
            {
                Thread.sleep(tsleeptime);
                timeout += tsleeptime;
                
                inforeq = (InputStream)awaitUUID2InfoReqs.remove(uuid);
            }
            
            return inforeq;

        } catch (InterruptedException e)
        {
            LOG.warn("information request interrupted"); 
        } 
        
        return null;
      
    }
    
    public InputStream retrieveCachedInformation(String uuid) {
        try {
           String[] args = Utilities.split(uuid,":");
           String group = args[0];
           String name = args[1];
           String version = args[2];
           String type = args[3];

           File repofile = new File(repoCacheDir.getAbsolutePath() + File.separator + group + File.separator + type + "s"
           + File.separator + name + "-" + version + "." + type);
           
           if (repofile.exists())
               return new FileInputStream(repofile);
           else
               return null;
           
        } catch (Exception e) {
           e.printStackTrace();
           return null;
        }
     }
    
    /* (non-Javadoc)
     * @see ch.ethz.jadabs.bundleLoader.api.InformationSource#retrieveInformation(java.lang.String, java.lang.String)
     */
    public InputStream retrieveInformation(String uuid, String source)
    {
        // do not yet handle the source
        return retrieveInformation(uuid);
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.bundleLoader.api.InformationSource#getMatchingPlugins(java.lang.String)
     */
    public Iterator getMatchingPlugins(String filter)
    {        
        sendRequest(ANYPEER, FILTER_REQ, SERVICE_FILTER, filter, null, null);

        try {
            Thread.sleep(2000);
        } catch(InterruptedException ie)
        {
            LOG.warn("thread interrupted");
        }
        
        ArrayList uuids = new ArrayList();
        
        for(Enumeration en = uuid2svcref.elements(); en.hasMoreElements();)
        {
            ServiceReferenceImpl sref = (ServiceReferenceImpl)en.nextElement();
            
            InputStream in = new ByteArrayInputStream(sref.getAdvertisement().getBytes());
            if (matches(in, filter))
                uuids.add(sref.getID());
        }
        
        return uuids.iterator();
    }
    
    
}
