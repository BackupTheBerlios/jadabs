/*
 * Created on 30.12.2004
 */
package ch.ethz.jadabs.bundleLoader.remote;

/*
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Set;
 */
import java.io.IOException;

import org.apache.log4j.Logger;
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
import ch.ethz.jadabs.jxme.Pipe;
import ch.ethz.jadabs.jxme.services.GroupService;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class RemoteLoaderActivator implements BundleActivator {

   protected static Logger LOG = Logger.getLogger(RemoteLoaderActivator.class
         .getName());
   protected static BundleContext bc;
   protected static EndpointService endptsvc;
   protected static GroupService groupsvc;

   protected static String peername;

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

   private Listener peer1listeners;
   private DiscoveryListener peer2listeners;

   /**
    * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
    */
   public void start(BundleContext bc) throws Exception {
      System.out.println("RemoteBundleLoader starting ...");
      RemoteLoaderActivator.bc = bc;

      String location = RemoteLoaderActivator.bc.getBundle().getLocation();

      // get EndpointService
      ServiceReference srefesvc = bc
            .getServiceReference("ch.ethz.jadabs.jxme.EndpointService");
      if (srefesvc == null) {
         LOG.debug("Can't start RemoteLoader, endpointservice not running !");
         throw new BundleException(
               "Can't start RemoteFramework, endpointservice not running !");
      }

      endptsvc = (EndpointService) bc.getService(srefesvc);

      // get GroupService
      ServiceReference srefgsvc = bc
            .getServiceReference("ch.ethz.jadabs.jxme.services.GroupService");
      if (srefgsvc == null) {
         LOG.debug("Can't start RemoteLoader, endpointservice not running !");
         throw new BundleException(
               "Can't start RemoteFramework, groupservice not running !");
      }

      groupsvc = (GroupService) bc.getService(srefgsvc);

      peername = bc.getProperty("ch.ethz.jadabs.jxme.peeralias");

      //     testpipe
      if (peername.equals("dhcppc0")) {
         peer1listeners = new Peer1Listeners();
         testPipePeer1();
      } else if (peername.equals("virtualPeer")) {
         peer2listeners = new Peer2Listeners();
         groupsvc.remoteSearch(NamedResource.PEER, "Name", "", 1,
               peer2listeners);
         testPipePeer2();
      }
   }

   public void testPipePeer1() {
      // propagation pipe
      Pipe proppipe = (Pipe) groupsvc.create(NamedResource.PIPE,
            "RemoteBundleLoaderPipe", null, Pipe.PROPAGATE);

      groupsvc.remotePublish(proppipe);

      try {
         groupsvc.listen(proppipe, peer1listeners);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public void testPipePeer2() {

      try {
         groupsvc.remoteSearch(NamedResource.PIPE, "Name", "", 1,
               peer2listeners);
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   class Peer2Listeners implements Listener, DiscoveryListener {

      public void handleSearchResponse(NamedResource namedResource) {
         System.out.println("found namedresource: " + namedResource.getName());

         System.out.println("group: " + namedResource.getID().getGroupID());

         if (namedResource instanceof Pipe) {
            Pipe pipe = (Pipe) namedResource;

            try {
               groupsvc.resolve(pipe, 100000);

               Element[] elms = new Element[] { new Element("testa", "testval",
                     Message.JXTA_NAME_SPACE) };

               groupsvc.send(pipe, new Message(elms));

            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }

      /**
       * @see ch.ethz.jadabs.jxme.DiscoveryListener#handleNamedResourceLoss(ch.ethz.jadabs.jxme.NamedResource)
       */
      public void handleNamedResourceLoss(NamedResource namedResource) {
         // TODO Auto-generated method stub
      }

      /**
       * @see ch.ethz.jadabs.jxme.Listener#handleMessage(ch.ethz.jadabs.jxme.Message,
       *      java.lang.String)
       */
      public void handleMessage(Message message, String listenerId) {
         // TODO Auto-generated method stub
      }
   }

   class Peer1Listeners implements Listener {
      public void handleMessage(Message message, String listenerId) {
         System.out.println("PropagationListener: " + message.toXMLString());
      }

      public void handleSearchResponse(NamedResource namedResource) {
         System.out.println("called Peer1Listeners handleSearchResponse");

      }
   }

   /**
    * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
    */
   public void stop(BundleContext arg0) throws Exception {
      // TODO Auto-generated method stub
   }
}

