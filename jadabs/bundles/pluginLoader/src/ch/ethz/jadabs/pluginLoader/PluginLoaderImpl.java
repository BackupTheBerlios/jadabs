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
 * Created on 15-Feb-2005
 */
package ch.ethz.jadabs.pluginLoader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;

import ch.ethz.jadabs.bundleLoader.HttpClient;
import ch.ethz.jadabs.bundleLoader.Repository;
import ch.ethz.jadabs.bundleLoader.api.InformationSource;
import ch.ethz.jadabs.bundleLoader.api.LoaderListener;
import ch.ethz.jadabs.bundleLoader.api.PluginFilterMatcher;
import ch.ethz.jadabs.bundleLoader.api.Utilities;
import ch.ethz.jadabs.pluginLoader.api.PluginLoader;

/**
 * Plugin Loader loads plugins by resolving all extensionPoints of the plugin 
 * and finding other plugins, that are suitable for the platform and provide 
 * the extensions to finally load the plugin.  
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class PluginLoaderImpl extends PluginFilterMatcher implements
      PluginLoader {
   private static PluginLoaderImpl me;
   private static Vector infoSources = new Vector();
   private Vector loaderListeners = new Vector();
   private static Hashtable descriptorCache = new Hashtable();
   private static HashSet loadedPlugins = new HashSet();
   private static Logger LOG = Logger.getLogger(PluginLoaderImpl.class);
   protected static String platform;
   protected static Scheduler scheduler = new Scheduler();

   /**
    * Singleton, hidden constructor    
    */
   private PluginLoaderImpl() {
      infoSources.add(new Repository());
      /*
      try {
         HttpClient client = new HttpClient();
         if (client != null)
            registerInformationSource(client);
      } catch (Exception e) {
      	e.printStackTrace();
      }
      */
      
      // Register a PluginLoaderHandler at httpDaemon
      PluginLoaderActivator.bloader.registerRequestHandler(new PluginLoaderHandler());      
      
   }

   
   /**
    * Singleton
    * @return PluginLoaderImpl
    */
   public static PluginLoaderImpl getInstance() {
      if (me == null)
         me = new PluginLoaderImpl();
      return me;
   }

   
   /**
    * Initialisation method, reads the starter file and starts the startup plugins. 
    * Also starts the parsing of the platform information file.
    * @param starter <code>String<code> representing the location of the 
    * startup file
    */
   void init(String starter) {
      String uuid = new String();
      try {
         BufferedReader reader = new BufferedReader(new FileReader(starter));
         String line;
         while ((line = reader.readLine()) != null) {
            if (line.startsWith("-usepad")) {
               if (LOG.isDebugEnabled())
                  LOG.debug("Opening " + line.substring(7).trim());

               platform = PlatformInformation.parsePAD("." + File.separatorChar
                     + line.substring(7).trim());
            } else if (line.startsWith("-startopd")) {
               uuid = line.substring(9).trim();
               LOG.info("Loading Plugin " + uuid + "...");

               loadPlugin(uuid);
            }
         }
      } catch (Exception e) {
         LOG.error(e.getMessage());
         e.printStackTrace();
         LOG.error("Loading of Plugin " + uuid + " failed.");
      }
      
   }

   
   public boolean loadPluginIfMatches(String uuid, InputStream in) throws Exception
   {
       if (matches(in, " ¦ " + platform + " ¦ " + "PRO"))
       {
           loadPlugin(uuid);
           return true;
       }
       else
           return false;
   }
   
   /**
    * @see ch.ethz.jadabs.pluginLoader.api.PluginLoader#loadPlugin(java.lang.String)
    */
   public void loadPlugin(String uuid) throws Exception {
      scheduler.clear();
      scheduler.addPlugin(uuid);
      PluginDescriptor pDescr = this.getPluginDescriptor(uuid);            
      
      while (!scheduler.stillToProcess.isEmpty()) {
         if (LOG.isDebugEnabled())
            LOG.debug("Scheduler now processes " + scheduler.stillToProcess.get(0));
         PluginDescriptor providing = getPluginDescriptor((String)scheduler.stillToProcess.remove(0));
      }
      
      System.out.println(scheduler);
      
      // and finally load the activator bundles and all their dependencies 
      // via bundleLoader
      for (Iterator schedules = scheduler.getIterator(); schedules.hasNext(); ) {
         ArrayList schedule = (ArrayList)schedules.next();
         try {
            for (int index=0; index < schedule.size(); index++) {
               PluginDescriptor current = getPluginDescriptor((String)schedule.get(index));
               System.out.println("LOADING " + current.activator);
               PluginLoaderActivator.bloader.loadBundle(current.activator);
               loadedPlugins.add(current.toString());
            }            
         } catch (Exception e) {
            // loading did not work, but maybe there is another schedule 
            // and another chance
            continue;
         }
         break;
      }
   }

   
   /**
    * @see ch.ethz.jadabs.pluginLoader.api.PluginLoader#unloadPlugin(java.lang.String)
    */
   public void unloadPlugin(String uuid) throws Exception {
      PluginDescriptor descr = getPluginDescriptor(uuid);
      Bundle[] bundles = PluginLoaderActivator.bc.getBundles();
      String[] parts = Utilities.split(uuid,":");
      String filename = parts[2] + "-" + parts[3] + ".jar";
      
      for (int i=0; i<bundles.length; i++) {
         if (bundles[i].getLocation().endsWith(uuid) || bundles[i].getLocation().endsWith(filename)) {
            bundles[i].stop();
            bundles[i].uninstall();
         }
      }
      loadedPlugins.remove(uuid);
   }

   
   /**
    * @see ch.ethz.jadabs.pluginLoader.api.PluginLoader#getExtensionGraph(java.lang.String)
    */
   public String getExtensionGraph(String uuid) {
      StringBuffer buffer = new StringBuffer();
      scheduler.clear();
      scheduler.addPlugin(uuid);
      
      try {
         PluginDescriptor pDescr = getPluginDescriptor(uuid);            
         
         // TODO: Implement graph
         
      } catch (Exception e) {
         // LOG.error(e.getMessage());
         e.printStackTrace();
      }
      
      return buffer.toString();
   }

   
   /**
    * @see ch.ethz.jadabs.pluginLoader.api.PluginLoader#getInstalledPlugins()
    */
   public Iterator getInstalledPlugins() {
      return loadedPlugins.iterator();
   }

   
   /**
    * @throws Exception
    * @see ch.ethz.jadabs.pluginLoader.api.PluginLoader#getMatchingPlugins(java.lang.String)
    */
   public Iterator getMatchingPlugins(String filter, Object requestor) throws Exception {
      ArrayList result = new ArrayList();
      for (Enumeration sources = infoSources.elements(); sources
            .hasMoreElements();) {
         InformationSource source = (InformationSource) sources.nextElement();
         if (source instanceof InformationSource) {
            if (((InformationSource) source) == requestor)
               return result.iterator();
         }
         Iterator it = source.getMatchingPlugins(filter);
         while (it.hasNext()) {
            result.add(it.next());
         }
      }
      return result.iterator();
   }

   
   /**
    * Static version to be called by PluginDescriptions
    * @param filter
    * @return
    * @throws Exception
    */
   protected static Iterator getMatchingPlugins(String filter) throws Exception {
      ArrayList result = new ArrayList();
      for (Enumeration sources = infoSources.elements(); sources
            .hasMoreElements();) {
         InformationSource source = (InformationSource) sources.nextElement();
         Iterator it = source.getMatchingPlugins(filter);
         while (it.hasNext()) {
            result.add(it.next());
         }
      }
      return result.iterator();      
   }

   
   /**
    * Get a <code>PluginDescription</code>, either the cached version by weak
    * reference, or creates a new instance, if there is no cached version or the 
    * cached version has been collected in the meantime.   
    * @param uuid Uuid of the plugin.
    * @return PluginDescriptor
    * @throws Exception
    */
   private PluginDescriptor getPluginDescriptor(String uuid) throws Exception {
      PluginDescriptor result = null;
         WeakReference ref = (WeakReference) descriptorCache.get(uuid);
         if (ref == null) {
            result = new PluginDescriptor(uuid);
            descriptorCache.remove(uuid);
            ref = new WeakReference(result);
            descriptorCache.put(uuid, ref);
         } else {
            result = (PluginDescriptor) ref.get();
            if (result == null) {
               result = new PluginDescriptor(uuid);
               descriptorCache.remove(uuid);
               ref = new WeakReference(result);
               descriptorCache.put(uuid, ref);
            }
         }
      return result;
   }

   
   /**
    * Same as in BundleLoader, but only used to fetch opds
    * @param uuid Uuid of the opd
    * @param requestor self reference
    * @return InputStream to the opd or null if not found
    */
   public InputStream fetchInformation(String uuid, Object requestor) {
      InputStream result = null;
            
      for (Enumeration sources = infoSources.elements(); sources.hasMoreElements();) {
         InformationSource source = (InformationSource) sources.nextElement();
                  
         if (source instanceof InformationSource) {
            if (source == requestor) return result;
         }
         if ((result = source.retrieveInformation(uuid)) != null)
            break;
      }
      if (LOG.isDebugEnabled())
         LOG.debug("fetched: " + uuid + " - " + result);
      return result;
   }


   /**
    * @see ch.ethz.jadabs.pluginLoader.api.PluginLoader#registerInformationSource(ch.ethz.jadabs.bundleLoader.api.InformationSource)
    */
   public void registerInformationSource(InformationSource infoSource) {
      if (!infoSources.contains(infoSource))
         infoSources.add(infoSource);
   }

   
   /**
    * @see ch.ethz.jadabs.pluginLoader.api.PluginLoader#unregisterInformationSource(ch.ethz.jadabs.bundleLoader.api.InformationSource)
    */
   public void unregisterInformationSource(InformationSource infoSource) {
      infoSources.remove(infoSource);
   }

   
   /**
    * @see ch.ethz.jadabs.pluginLoader.api.PluginLoader#registerLoaderListener(ch.ethz.jadabs.bundleLoader.api.LoaderListener)
    */
   public void registerLoaderListener(LoaderListener listener) {
      if (!loaderListeners.contains(listener))
         loaderListeners.add(listener);
   }

   
   /**
    * @see ch.ethz.jadabs.pluginLoader.api.PluginLoader#unregisterLoaderListener(ch.ethz.jadabs.bundleLoader.api.LoaderListener)
    */
   public void unregisterLoaderListener(LoaderListener listener) {
      loaderListeners.remove(listener);
   }

   
   /**
    * @see ch.ethz.jadabs.bundleLoader.api.PluginFilterMatcher#debug(java.lang.String)
    */
   protected void debug(String str) {
      // TODO Auto-generated method stub

   }

   
   /**
    * @see ch.ethz.jadabs.bundleLoader.api.PluginFilterMatcher#error(java.lang.String)
    */
   protected void error(String str) {
      // TODO Auto-generated method stub

   }
   
}