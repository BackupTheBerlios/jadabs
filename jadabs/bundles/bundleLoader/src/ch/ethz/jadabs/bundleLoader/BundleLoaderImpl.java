/*
 * Copyright (c) 2003-2005, Jadabs project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * - Neither the name of the Jadabs project nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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
import ch.ethz.jadabs.bundleLoader.api.Utilities;

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
    *           Uuid of the desired bundle, as uuid of the matching obr, e.g.
    *           <code>jadabs:remotefw-impl:0.7.1-SNAPSHOT:obr</code>
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

         }

         // open semaphore
         locked = Boolean.FALSE;
         locked.notifyAll();
      }
   }

   /**
    * Get the dependency graph of a given bundle
    * 
    * @see ch.ethz.jadabs.bundleLoader.api.BundleLoader#getDependencyGraph(java.lang.String)
    */
   public String getDependencyGraph(String uuid) {
      // FIXME: still a bit buggy for large graphs ...
      LinkedList list = new LinkedList();
      list.add(getBundleDescriptor(uuid));

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

         do {
            int index;

            for (index = 0; index < list.size() - 1
                  && ((BundleDescriptor) list.get(index)).processed; index++)
               ;

            BundleDescriptor current = (BundleDescriptor) list.get(index);

            // all are processed, exit loop
            if (current.processed)
               break;

            int offset = 1;
            int childLevel = current.getLevel() + 1;

            System.out.println(current + " has Dependencies: ");
            for (Enumeration deps = current.dependencies.elements(); deps
                  .hasMoreElements();) {
               String depUuid = (String) deps.nextElement();
               System.out.println("\t" + depUuid);
               BundleDescriptor dep = getBundleDescriptor(depUuid);
               System.out.println("\t old level: " + dep.getLevel()
                     + ", new level: " + childLevel);
               dep.setLevel(childLevel);
               if (dep.dependencies.isEmpty())
                  dep.processed = true;
               list.add(index + offset, dep);

               offset++;
            }
            System.out.println();

            current.processed = true;

         } while (true);

         for (Iterator iter = list.iterator(); iter.hasNext();) {
            BundleDescriptor current = (BundleDescriptor) iter.next();
            System.out.println(current + " hat Level " + current.getLevel());
         }

         // and now make a XML document from the list

         Stack stack = new Stack();
         StringBuffer buffer = new StringBuffer();
         buffer.append("<dependency-graph>\n");

         String sep = new String();
         int level = -1;

         while (!list.isEmpty()) {
            BundleDescriptor current = (BundleDescriptor) list.removeFirst();

            for (int dif = level; dif >= current.getLevel(); dif--) {
               buffer.append(Utilities.tabs(dif) + "</bundle>\n");
            }

            buffer.append(Utilities.tabs(current.getLevel())
                  + "<bundle uuid=\"" + current.toString() + "\">\n");

            level = current.getLevel();

         }

         for (int dif = 0; dif <= level; dif++) {
            buffer.append(Utilities.tabs(level - dif) + "</bundle>\n");
         }

         buffer.append("</dependency-graph>\n");

         // open semaphore
         locked = Boolean.FALSE;
         locked.notifyAll();
         return buffer.toString();
      }

   }

   /**
    * Get a list of all currently installed bundles
    * 
    * @see ch.ethz.jadabs.bundleLoader.api.BundleLoader#getInstalledBundles()
    */
   public Iterator getInstalledBundles() {
      return loadedBundles.iterator();
   }

   /**
    * Get a bundle descriptor. Either, the <code>BundleDescriptor</code> still
    * exists, that means the <code>WeakReference</code> has not been broken up
    * by the garbage collector due to memory shortness. Or if the Reference
    * points to <code>null</code>, a new <code>BundleDescriptor</code> will
    * be build and returned.
    * 
    * @param uuid
    *           Uuid of the obr file.
    * @return A <code>BundleDescriptor</code> for the given uuid.
    */
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
      if (result != null) {
         result.processed = false;
         result.setLevel(0);
      }
      return result;
   }

   /**
    * Builds up a schedule for a given bundle.
    * 
    * @param initial
    *           <code>BundleDescriptor</code> of the bundle that is to be
    *           loaded. The schedule consists of the uuid of the bundle itself
    *           and the uuid of the closure of all dependencies.
    * @return <code>LinkedList</code> representing a schedule for the bundle.
    */
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
    * Register a request handler at the <code>HttpDaemon</code>
    * 
    * @see ch.ethz.jadabs.bundleLoader.api.BundleLoader#registerRequestHandler(ch.ethz.jadabs.bundleLoader.api.HttpRequestHandler)
    */
   public void registerRequestHandler(HttpRequestHandler handler) {
      BundleLoaderActivator.httpDaemon.addRequestHandler(handler);
   }

   /**
    * Unregister a request handler at the <code>HttpDaemon</code>
    * 
    * @see ch.ethz.jadabs.bundleLoader.api.BundleLoader#unregisterRequestHandler(ch.ethz.jadabs.bundleLoader.api.HttpRequestHandler)
    */
   public void unregisterRequestHandler(HttpRequestHandler handler) {
      BundleLoaderActivator.httpDaemon.removeRequestHandler(handler);
   }

   /**
    * Register an <code>InformationSource</code> to be used when fetching
    * bundle jars or obrs.
    * 
    * @see ch.ethz.jadabs.bundleLoader.api.BundleLoader#registerInformationSource(ch.ethz.jadabs.bundleLoader.api.InformationSource)
    */
   public void registerInformationSource(InformationSource infoSource) {
      if (!infoSources.contains(infoSource))
         infoSources.add(infoSources);
   }

   /**
    * Unregister an <code>InformationSource</code>
    * 
    * @see ch.ethz.jadabs.bundleLoader.api.BundleLoader#unregisterInformationSource(ch.ethz.jadabs.bundleLoader.api.InformationSource)
    */
   public void unregisterInformationSource(InformationSource infoSource) {
      infoSources.remove(infoSource);
   }

   /**
    * Fetch Information like bundle jars or obrs using all registered
    * <code>InformationSources<code>
    * @param uuid Uuid of the requested information. 
    * @param location If the <code>InformationSource</code> supports directed search, 
    *        the location is used as primary search location, e.g. the <code>HttpClient</code> 
    *        supports a URL as location.  
    * @param requestor A reference to the requestor, this is important, if the requestor
    *        is itself an <code>InformationSource</code>. Used to avoid cycles. 
    * @return <code>InputStream</code> to the found information or <code>null</code>.
    */
   protected InputStream fetchInformation(String uuid, String location,
         Object requestor) {
      InputStream result = null;
      for (Enumeration sources = infoSources.elements(); sources
            .hasMoreElements();) {
         InformationSource source = (InformationSource) sources.nextElement();
         if (source instanceof InformationSource) {
            if (source == requestor)
               return result;
         }
         if ((result = source.retrieveInformation(uuid, location)) != null)
            break;
      }
      if (LOG.isDebugEnabled())
         LOG.debug("fetched: " + uuid + " - " + result);
      return result;
   }

   /**
    * Fetch Information like bundle jars or obrs using all registered
    * <code>InformationSources<code>
    * @see ch.ethz.jadabs.bundleLoader.api.BundleLoader#getInformation(java.lang.String, java.lang.Object)
    */
   public InputStream fetchInformation(String uuid, Object requestor) {
      InputStream result = null;
      for (Enumeration sources = infoSources.elements(); sources
            .hasMoreElements();) {
         InformationSource source = (InformationSource) sources.nextElement();
         if (source instanceof InformationSource) {
            if (((InformationSource) source) == requestor)
               return result;
         }
         if ((result = source.retrieveInformation(uuid)) != null)
            break;
      }
      if (LOG.isDebugEnabled())
         LOG.debug("fetched: " + uuid + " - " + result);
      return result;
   }

   /**
    * Register a <code>LoaderListener</code>
    * 
    * @see ch.ethz.jadabs.bundleLoader.api.BundleLoader#registerLoaderListener(ch.ethz.jadabs.bundleLoader.api.LoaderListener)
    */
   public void registerLoaderListener(LoaderListener listener) {
      if (!loaderListeners.contains(listener))
         loaderListeners.add(listener);
   }

   /**
    * Unregister a <code>LoaderListener</code>
    * 
    * @see ch.ethz.jadabs.bundleLoader.api.BundleLoader#unregisterLoaderListener(ch.ethz.jadabs.bundleLoader.api.LoaderListener)
    */
   public void unregisterLoaderListener(LoaderListener listener) {
      loaderListeners.remove(listener);
   }

   /**
    * Notifies all registered <code>LoaderListeners</code> that the state of a
    * bundle has changed.
    * 
    * @param uuid
    *           Uuid of the bundle that changed
    * @param type
    *           New bundle state.
    */
   private void notifyListeners(String uuid, int type) {
      for (Enumeration listeners = loaderListeners.elements(); listeners
            .hasMoreElements();) {
         ((LoaderListener) listeners.nextElement()).itemChanged(uuid, type);
      }
   }

   /**
    * Called by framework, if a bundle state has changed.
    * 
    * @see org.osgi.framework.BundleListener#bundleChanged(org.osgi.framework.BundleEvent)
    */
   public void bundleChanged(BundleEvent bevent) {
      String loc = bevent.getBundle().getLocation();
      String uuid = new String();

      if (!loc.endsWith(":obr")) {
         uuid = location2uuid(loc);
      } else {
         uuid = loc;
      }

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

   /**
    * Get a obr uuid from a bundle jar filename
    * 
    * @param loc
    *           bundle jar filename
    * @return obr uuid
    */
   private static String location2uuid(String loc) {
      int pos = loc.lastIndexOf(File.separatorChar);

      if (pos > -1) {
         String filename = loc.substring(pos + 1);
         int pos2 = filename.indexOf(".opd");
         if (pos2 > -1) {
            filename = filename.substring(0, pos2);
         }
         pos2 = filename.indexOf("-");
         while (!Character.isDigit(filename.charAt(pos2 + 1))) {
            int next = filename.substring(pos2 + 1).indexOf("-");
            if (next == -1)
               return null;
            pos2 += next + 1;
         }
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