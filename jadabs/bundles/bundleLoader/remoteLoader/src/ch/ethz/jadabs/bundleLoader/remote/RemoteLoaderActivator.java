/*
 * Created on 30.12.2004
 */
package ch.ethz.jadabs.bundleLoader.remote;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.DiscoveryListener;
import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.EndpointService;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.MalformedURIException;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.Peer;
import ch.ethz.jadabs.jxme.services.GroupService;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class RemoteLoaderActivator implements BundleActivator, Listener, DiscoveryListener{
   
   protected static Logger LOG = Logger.getLogger(RemoteLoaderActivator.class.getName());
   protected static BundleContext bc;
	protected static EndpointService endptsvc;
	protected static GroupService groupsvc;

	protected static String repository;
	
	protected static final String ENDPOINT_SVC_NAME = "bundleLoader";
	protected static final String MSG_TYPE = "msg_type";
	protected static final String BUNDLE_NAME = "bundle_name";
	protected static final String BUNDLE_GROUP = "bundle_group";
	protected static final String BUNDLE_VERSION = "bundle_version";
	protected static final String DATA = "data";

	protected static final int REQUEST_BUNDLE_LIST = 1;
    protected static final int REPLY_BUNDLE_LIST = 2;
    protected static final int REQUEST_BUNDLE_OBR = 3;
    protected static final int REPLY_BUNDLE_OBR = 4;
    protected static final int REQUEST_BUNDLE_JAR = 5;
    protected static final int REPLY_BUNDLE_JAR = 6;

   	/**
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bc) throws Exception {
		System.out.println("BundleLoader starting ...");			
		RemoteLoaderActivator.bc = bc;
				
		String location = RemoteLoaderActivator.bc.getBundle().getLocation();

		// get EndpointService
		ServiceReference srefesvc = bc.getServiceReference("ch.ethz.jadabs.jxme.EndpointService");
		if (srefesvc == null)
		{
		   	LOG.debug("Can't start RemoteLoader, endpointservice not running !");
		   	throw new BundleException("Can't start RemoteFramework, endpointservice not running !");
		}
       
		endptsvc = (EndpointService) bc.getService(srefesvc);
		endptsvc.addListener(ENDPOINT_SVC_NAME, this);

       // get GroupService
       ServiceReference srefgsvc = bc.getServiceReference("ch.ethz.jadabs.jxme.services.GroupService");
       if (srefgsvc == null)
       {
           LOG.debug("Can't start RemoteLoader, endpointservice not running !");
           throw new BundleException("Can't start RemoteFramework, groupservice not running !");
       }

       groupsvc = (GroupService) bc.getService(srefgsvc);
       
       // subscribe in groupsvc for discovery events
       groupsvc.addDiscoveryListener(this);


		// register all system bundles
		Bundle [] bundles = RemoteLoaderActivator.bc.getBundles();
		Vector sysBundles = new Vector();
		
		for (int i = 0; i < bundles.length; i++) {
			
			String loc = bundles[i].getLocation();			
			int pos = loc.lastIndexOf(File.separatorChar);
									
			if (pos > -1) {
				loc = loc.substring(pos+1);
				pos = loc.indexOf(".jar");							
				if (pos > -1) {
				 	loc = loc.substring(0,pos);	
				}
			} 
			sysBundles.add(loc);							
		}

		// OSGi API is provided by knopflerfish
		sysBundles.add(new String("osgi-framework-1.2"));
		// this is a hack
		sysBundles.add(new String("log4j-cdc-0.7.1-SNAPSHOT"));
		
		
	}

	/**
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bc) throws Exception {
		RemoteLoaderActivator.bc = null;	
	}
	
   protected static void sendMessage(Peer peer, Element[] elms)
   {
       // send it to the specified remoteframework
       EndpointAddress[] urilist = peer.getURIList();
       if (urilist.length == 0)
           LOG.debug("urilist is null");
       
       EndpointAddress endptaddr;
       try
       {
           
           endptaddr = new EndpointAddress(
                   urilist[0], RemoteLoaderActivator.ENDPOINT_SVC_NAME, null);

           RemoteLoaderActivator.endptsvc.send(elms, 
                   new EndpointAddress[]{endptaddr});
           
       } catch (MalformedURIException e1)
       {
           LOG.debug("malformed endpointaddress");
       } catch (IOException e)
       {
           LOG.debug("couldn't send message");
       }
   }

  /**
   * @see ch.ethz.jadabs.jxme.Listener#handleMessage(ch.ethz.jadabs.jxme.Message, java.lang.String)
   */
  public void handleMessage(Message message, String listenerId) {
     
     int type = Integer.parseInt(new String(message.getElement(MSG_TYPE).getData()));
     
     if (type == REQUEST_BUNDLE_LIST) {
        // TODO: Get bundleLoader via ServiceReference ...
        // Set installedBundles = bundleLoader.getInstalledBundles();
        Set installedBundles = null;
        Peer origin = new Peer();
        try {
           ByteArrayOutputStream baos = new ByteArrayOutputStream();
           ObjectOutputStream ous = new ObjectOutputStream(baos);
           ous.writeObject(installedBundles);
           
           Element[] elems = new Element[2];
           elems[0] = new Element(MSG_TYPE, Integer.toString(REPLY_BUNDLE_LIST), Message.JXTA_NAME_SPACE);
           elems[1] = new Element(DATA, baos.toString(), Message.JXTA_NAME_SPACE);
           sendMessage(origin, elems);
           
        } catch (Exception err) {
           err.printStackTrace();
        }
        
     } else if (type == REQUEST_BUNDLE_OBR) {
        String bundleName = new String(message.getElement(BUNDLE_NAME).getData());
        
        
     } else if (type == REQUEST_BUNDLE_JAR) {
        
     } else if (type == REPLY_BUNDLE_LIST) {
        byte[] bundleData = message.getElement(DATA).getData();
        try {
           ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bundleData));
           Set remoteBundles = (Set)ois.readObject();            
        } catch (Exception err) {
           err.printStackTrace();
        }         
        notifyAll();
     } else if (type == REPLY_BUNDLE_OBR) {
        String bundleName = new String(message.getElement(BUNDLE_NAME).getData());
        String bundleGroup = new String(message.getElement(BUNDLE_GROUP).getData());
        String bundleVersion = new String(message.getElement(BUNDLE_VERSION).getData());
        byte[] bundleData = message.getElement(DATA).getData();
        try {
           FileOutputStream fos = new FileOutputStream(repository + File.separatorChar + bundleGroup + File.separatorChar + "obr" + File.separatorChar + bundleName + "-" + bundleVersion + ".obr");
           fos.write(bundleData);
           fos.flush();
           fos.close();
        } catch (Exception err) {
           err.printStackTrace();       
        }
        notifyAll();
     } else if (type == REPLY_BUNDLE_JAR) {
        String bundleName = new String(message.getElement(BUNDLE_NAME).getData());
        String bundleGroup = new String(message.getElement(BUNDLE_GROUP).getData());
        String bundleVersion = new String(message.getElement(BUNDLE_VERSION).getData());
        byte[] bundleData = message.getElement(DATA).getData();
        try {
           FileOutputStream fos = new FileOutputStream(repository + File.separatorChar + bundleGroup + File.separatorChar + "jars" + File.separatorChar + bundleName + "-" + bundleVersion + ".jar");
           fos.write(bundleData);
           fos.flush();
           fos.close();
        } catch (Exception err) {
           err.printStackTrace();       
        }         
        notifyAll();
     }      
  }

  /**
   * @see ch.ethz.jadabs.jxme.Listener#handleSearchResponse(ch.ethz.jadabs.jxme.NamedResource)
   */
  public void handleSearchResponse(NamedResource namedResource) {
     // TODO Auto-generated method stub
     
  }

  /**
   * @see ch.ethz.jadabs.jxme.DiscoveryListener#handleNamedResourceLoss(ch.ethz.jadabs.jxme.NamedResource)
   */
  public void handleNamedResourceLoss(NamedResource namedResource) {
     // TODO Auto-generated method stub
     
  }
 
}


