/*
 * Created on 14-Feb-2005
 */
package ch.ethz.jadabs.bundleLoader;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Vector;
import java.io.File;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;

import ch.ethz.jadabs.bundleLoader.api.BundleLoader;
import ch.ethz.jadabs.bundleLoader.api.HttpRequestHandler;
import ch.ethz.jadabs.bundleLoader.api.InformationSource;
import ch.ethz.jadabs.bundleLoader.api.LoaderListener;

/**
 * BundleLoader Implementation, resolves dependencies of a bundle, builds up a
 * schedule for a bundle and loads the schedule.
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class BundleLoaderImpl implements BundleLoader, BundleListener {
   private static BundleLoaderImpl me;
   private static Logger LOG = Logger.getLogger(BundleLoaderImpl.class);
   private static Hashtable descriptorCache = new Hashtable();
   private static HashSet loadedBundles = new HashSet();
   private static Vector infoSources = new Vector();
   private Vector loaderListeners = new Vector();
   private static Boolean locked = Boolean.FALSE;

   /**
    * Singleton, hidden constructor
    */
   private BundleLoaderImpl() {
      infoSources.add(new Repository());

      Bundle[] bundles = BundleLoaderActivator.bc.getBundles();
      for (int i = 0; i < bundles.length; i++) {
         String loc = bundles[i].getLocation();

         if (loc.equals("System Bundle")) {
            // provided by knopflerfish
            loadedBundles.add(new String("osgi:osgi-framework:1.2:obr"));
         } else {
            descriptorCache.put(location2uuid(loc), new WeakReference(null));
            loadedBundles.add(location2uuid(loc));
         }
      }
      // this is a hack
      loadedBundles.add(new String("jadabs:log4j-cdc:0.7.1-SNAPSHOT:obr"));

      // TEST SECTION
      try {
         this.loadBundle("jadabs:jxme-udp:0.7.1-SNAPSHOT:obr");
         this.loadBundle("jadabs:jxme-services-impl:0.7.1-SNAPSHOT:obr");
         this.loadBundle("jadabs:remotefw-impl:0.7.1-SNAPSHOT:obr");
      } catch (Exception e) {
         e.printStackTrace();
      }
      System.out.println();
      System.out.println(getDependencyGraph("jadabs:jxme-services-impl:0.7.1-SNAPSHOT:obr"));

      System.out.println();
      System.out.println(new Repository().getMatchingPlugins("Extension/id:Transport ¦ Container/id:core-osgi-daop,version:0.1.0; NetIface/type:bt-jsr82 ¦ RP"));
   }

   /**
    * Singleton, get implementation
    * 
    * @return BundleLoader Implementation
    */
   protected static BundleLoaderImpl getInstance() {
      if (me == null)
         me = new BundleLoaderImpl();
      return me;
   }

   /**
    * Load a bundle together with all dependencies.
    * 
    * @param uuid
    *           of the desired bundle, as uuid of the matching obr, e.g.
    *           jadabs:remotefw-impl:0.7.1-SNAPSHOT:obr
    * @see ch.ethz.jadabs.bundleLoader.api.BundleLoader#loadBundle(java.lang.String)
    */
   public void loadBundle(String uuid) throws Exception {

      synchronized (locked) {

         // semaphore, only one schedule at once,
         // to keep the system state constant during
         // resolvation and scheduling
         try {
            if (locked.booleanValue())
               this.wait();
         } catch (Exception e) {
            LOG.error(e.getStackTrace());
         }
         locked = Boolean.TRUE;

         if (LOG.isDebugEnabled())
            LOG.debug("loading " + uuid + " ...");
         BundleDescriptor bdescr = getBundleDescriptor(uuid);
         LinkedList schedule = buildSchedule(bdescr);
         if (LOG.isDebugEnabled())
            LOG.debug("THE SCHEDULE: " + schedule);

         // iterate over schedule
         for (Iterator bundleDescr = schedule.iterator(); bundleDescr.hasNext();) {
            BundleDescriptor bundleD = (BundleDescriptor) bundleDescr.next();

            // get the bundle jars from available information sources
            InputStream jar = fetchInformation(bundleD.jar_uuid(), bundleD
                  .jar_source());

            // install bundle
            Bundle bundle = BundleLoaderActivator.bc.installBundle(bundleD
                  .toString(), jar);
            try {
               // start bundle, if possible
               bundle.start();
            } catch (BundleException be) {
               be.getNestedException().printStackTrace();
            }

            // this fixes a bug: seems like knopflerfish does not notify
            // when bundles are installed when bundles via stream and started
            loadedBundles.add(bundle.toString());

         }

         // open semaphore
         locked = Boolean.FALSE;
         locked.notifyAll();
      }
   }

   /**
    * @see ch.ethz.jadabs.bundleLoader.api.BundleLoader#getDependencyGraph(java.lang.String)
    */
   public String getDependencyGraph(String uuid) {
      // still a bit buggy for large graphs ...
      
      int index;
      int level;
      boolean found;
      LinkedList list = new LinkedList();
      list.add(getBundleDescriptor(uuid));

      do {
         found = false;

         // another iteration until all dependencies are processed
         for (index = 0; ((BundleDescriptor) list.get(index)).processed
               && index < list.size() - 1; index++)
            ;

         BundleDescriptor current = (BundleDescriptor) list.get(index);

         // found one that has not been processed yet
         if (!current.processed) {
            found = true;

            Vector dependencies = current.dependencies;

            // insert all dependencies
            for (Enumeration deps = dependencies.elements(); deps
                  .hasMoreElements();) {
               String depUuid = (String) deps.nextElement();
               BundleDescriptor depDescr = getBundleDescriptor(depUuid);

               // Dependencies get parent's level plus 1
               depDescr.level = current.level + 1;
               list.add(index + 1, depDescr);
            }
            current.processed = true;
         }
      } while (found);

      // and now make an XML document from the list

      Stack stack = new Stack();
      StringBuffer buffer = new StringBuffer();
      level = -1;
      buffer.append("<dependency-graph>\n");
      
      while (!list.isEmpty()) {
         BundleDescriptor current = (BundleDescriptor)list.removeFirst();
         buffer.append("<bundle uuid=\"" + current.toString() + "\">\n");
         for (int dif = 0; dif <= level-current.level; dif++) {
            buffer.append("</bundle>\n");
         }
         level = current.level;
      }

      for (int dif = 0; dif <= level; dif++) {
         buffer.append("</bundle>\n");
      }

      buffer.append("</dependency-graph>\n");
      
      return buffer.toString();
   }

   
   /**
    * @see ch.ethz.jadabs.bundleLoader.api.BundleLoader#getInstalledBundles()
    */
   public Iterator getInstalledBundles() {
      return loadedBundles.iterator();
   }

   
   private BundleDescriptor getBundleDescriptor(String uuid) {
      BundleDescriptor result = null;
      try {
         WeakReference ref = (WeakReference) descriptorCache.get(uuid);
         if (ref == null) {
            result = new BundleDescriptor(uuid);
            descriptorCache.remove(uuid);
            ref = new WeakReference(result);
            descriptorCache.put(uuid, ref);
         } else {
            result = (BundleDescriptor) ref.get();
            if (result == null) {
               result = new BundleDescriptor(uuid);
               descriptorCache.remove(uuid);
               ref = new WeakReference(result);
               descriptorCache.put(uuid, ref);
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
         LOG.error("Could not locate bundle descriptor " + uuid);
      }
      if (result != null)
         result.processed = false;
      result.level = 0;
      return result;
   }

   private LinkedList buildSchedule(BundleDescriptor initial) {
      LinkedList installationQueue = new LinkedList();
      HashSet queuedBundles = new HashSet();

      installationQueue.add(initial);

      boolean found;
      int index;

      do {
         found = false;

         // another iteration until all dependencies are processed
         for (index = 0; ((BundleDescriptor) installationQueue.get(index)).processed
               && index < installationQueue.size() - 1; index++)
            ;

         BundleDescriptor current = (BundleDescriptor) installationQueue
               .get(index);

         // found one that has not been processed yet
         if (!current.processed) {
            found = true;

            Vector dependencies = current.dependencies;

            if (BundleLoaderActivator.LOG.isDebugEnabled())
               BundleLoaderActivator.LOG.debug(current.toString()
                     + " HAS DEPENDENCIES: " + dependencies);

            for (Enumeration deps = dependencies.elements(); deps
                  .hasMoreElements();) {
               String depUuid = (String) deps.nextElement();

               BundleDescriptor depDescr = getBundleDescriptor(depUuid);
               if (depDescr == null) {
                  LOG.error("Could not find Bundle Descriptor for " + depUuid);
                  return null;
               }

               // TODO: Check, if a bundle is already in the queue but in a
               // different version

               if (!installationQueue.contains(depUuid)
                     && !loadedBundles.contains(depUuid)) {

                  // bundle is not yet in queue
                  // so add it prior to the bundle that had this
                  // dependency
                  if (index > 0) {
                     installationQueue.add(index - 1, depDescr);
                  } else {
                     installationQueue.addFirst(depDescr);
                  }
               } else {
                  // bundle is already in queue
                  // check, if it is scheduled later than the bundle
                  // that had this dependency
                  int inQueue = installationQueue.indexOf(depDescr);
                  // it is in queue
                  if (index < inQueue) {
                     // remove existing entry from queue
                     installationQueue.remove(depDescr);
                     // and add prior to the bundle that had this
                     // dependency
                     if (index > 0) {
                        installationQueue.add(index - 1, depDescr);
                     } else {
                        installationQueue.addFirst(depDescr);
                     }
                  }
               }
            }
            current.processed = true;
         }
      } while (found);
      return installationQueue;
   }

   /**
    * @see ch.ethz.jadabs.bundleLoader.api.BundleLoader#registerRequestHandler(ch.ethz.jadabs.bundleLoader.api.HttpRequestHandler)
    */
   public void registerRequestHandler(HttpRequestHandler handler) {
      // TODO Auto-generated method stub
   }

   /**
    * @see ch.ethz.jadabs.bundleLoader.api.BundleLoader#unregisterRequestHandler(ch.ethz.jadabs.bundleLoader.api.HttpRequestHandler)
    */
   public void unregisterRequestHandler(HttpRequestHandler handler) {
      // TODO Auto-generated method stub
   }

   /**
    * @see ch.ethz.jadabs.bundleLoader.api.BundleLoader#registerInformationSource(ch.ethz.jadabs.bundleLoader.api.InformationSource)
    */
   public void registerInformationSource(InformationSource infoSource) {
      if (!infoSources.contains(infoSource))
         infoSources.add(infoSources);
   }

   /**
    * @see ch.ethz.jadabs.bundleLoader.api.BundleLoader#unregisterInformationSource(ch.ethz.jadabs.bundleLoader.api.InformationSource)
    */
   public void unregisterInformationSource(InformationSource infoSource) {
      infoSources.remove(infoSource);
   }

   
   protected static InputStream fetchInformation(String uuid, String location, Object requestor) {
      InputStream result = null;
      for (Enumeration sources = infoSources.elements(); sources.hasMoreElements();) {
         InformationSource source = (InformationSource) sources.nextElement();
         if (source instanceof InformationSource) {
            if (((InformationSource)source) == requestor) return result;
         }
         if ((result = ((InformationSource) sources.nextElement())
               .retrieveInformation(uuid, location)) != null)
            break;
      }
      if (LOG.isDebugEnabled())
         LOG.debug("fetched: " + uuid + " - " + result);
      return result;
   }


   /**
    * @see ch.ethz.jadabs.bundleLoader.api.BundleLoader#getInformation(java.lang.String, java.lang.Object)
    */
   public InputStream fetchInformation(String uuid, Object requestor) {
      InputStream result = null;
      for (Enumeration sources = infoSources.elements(); sources.hasMoreElements();) {
         InformationSource source = (InformationSource) sources.nextElement();
         if (source instanceof InformationSource) {
            if (((InformationSource)source) == requestor) return result;
         }
         if ((result = ((InformationSource) sources.nextElement())
               .retrieveInformation(uuid)) != null)
            break;
      }
      if (LOG.isDebugEnabled())
         LOG.debug("fetched: " + uuid + " - " + result);
      return result;
   }

   
   /**
    * @see ch.ethz.jadabs.bundleLoader.api.BundleLoader#registerLoaderListener(ch.ethz.jadabs.bundleLoader.api.LoaderListener)
    */
   public void registerLoaderListener(LoaderListener listener) {
      if (!loaderListeners.contains(listener))
         loaderListeners.add(listener);
   }

   /**
    * @see ch.ethz.jadabs.bundleLoader.api.BundleLoader#unregisterLoaderListener(ch.ethz.jadabs.bundleLoader.api.LoaderListener)
    */
   public void unregisterLoaderListener(LoaderListener listener) {
      loaderListeners.remove(listener);
   }

   private void notifyListeners(String uuid, int type) {
      for (Enumeration listeners = loaderListeners.elements(); listeners
            .hasMoreElements();) {
         ((LoaderListener) listeners.nextElement()).itemChanged(uuid, type);
      }
   }

   /**
    * @see org.osgi.framework.BundleListener#bundleChanged(org.osgi.framework.BundleEvent)
    */
   public void bundleChanged(BundleEvent bevent) {
      String loc = bevent.getBundle().getLocation();
      String uuid = location2uuid(loc);

      if (LOG.isDebugEnabled())
         LOG.debug("###########  BUNDLE STATE CHANGED ... " + uuid + " to "
               + bevent.getType());

      if (bevent.getType() == BundleEvent.INSTALLED) {
         if (LOG.isDebugEnabled())
            LOG.debug("bundle installed: " + uuid);

         loadedBundles.add(uuid);

         this.notifyListeners(uuid, bevent.getType());
      } else if (bevent.getType() == BundleEvent.UNINSTALLED) {
         if (LOG.isDebugEnabled())
            LOG.debug("bundle uninstalled: " + uuid);

         loadedBundles.remove(uuid);

         this.notifyListeners(uuid, bevent.getType());
      }
   }

   private String location2uuid(String loc) {
      int pos = loc.lastIndexOf(File.separatorChar);

      if (pos > -1) {
         String filename = loc.substring(pos + 1);
         int pos2 = filename.indexOf(".jar");
         if (pos2 > -1) {
            filename = filename.substring(0, pos2);
         }
         pos2 = filename.indexOf("-");
         String name = filename.substring(0, pos2);
         String version = filename.substring(pos2 + 1);

         String group = loc.substring(0, pos);
         pos2 = group.lastIndexOf(File.separatorChar);
         group = group.substring(0, pos2);
         pos2 = group.lastIndexOf(File.separatorChar);
         group = group.substring(pos2 + 1);

         return group + ":" + name + ":" + version + ":obr";
      }
      return null;
   }

}