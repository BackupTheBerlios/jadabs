/*
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
import ch.ethz.jadabs.bundleLoader.Repository;
import ch.ethz.jadabs.bundleLoader.api.InformationSource;
import ch.ethz.jadabs.bundleLoader.api.LoaderListener;
import ch.ethz.jadabs.bundleLoader.api.PluginFilterMatcher;
import ch.ethz.jadabs.pluginLoader.api.PluginLoader;

/**
 * 
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

   private PluginLoaderImpl() {
      infoSources.add(new Repository());

      String starter = PluginLoaderActivator.bc
            .getProperty("ch.ethz.jadabs.pluginloader.starter");

      if (starter == null)
         starter = "init.starter";
      
      init(starter);
      
      // BUNDLE LOADER TEST SECTION
      /*
      try {
         PluginLoaderActivator.bloader.loadBundle("jadabs:jxme-udp:0.7.1-SNAPSHOT:obr");
         PluginLoaderActivator.bloader.loadBundle("jadabs:jxme-services-impl:0.7.1-SNAPSHOT:obr");
         PluginLoaderActivator.bloader.loadBundle("jadabs:remotefw-impl:0.7.1-SNAPSHOT:obr");
      } catch (Exception e) {
         e.printStackTrace();
      }
      System.out.println();
      System.out.println(PluginLoaderActivator.bloader.getDependencyGraph("jadabs:jxme-services-impl:0.7.1-SNAPSHOT:obr"));
      */
      // PLUGIN LOADER TEST SECTION
      
      // System.out.println();
      // System.out.println(new Repository().getMatchingPlugins("Extension/id:Transport ¦ Container/id:core-osgi-daop,version:0.1.0; NetIface/type:bt-jsr82 ¦ RP"));

   }

   public static PluginLoaderImpl getInstance() {
      if (me == null)
         me = new PluginLoaderImpl();
      return me;
   }

   private void init(String starter) {
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
         LOG.error("Loading of Plugin " + uuid + " failed.");
      }
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
               PluginDescriptor current = this.getPluginDescriptor((String)schedule.get(index));
               System.out.println("LOADING " + current.activator);
               PluginLoaderActivator.bloader.loadBundle(current.activator);
            }            
         } catch (Exception e) {
            continue;
         }
         break;
      }
   }

   /**
    * @see ch.ethz.jadabs.pluginLoader.api.PluginLoader#unloadPlugin(java.lang.String)
    */
   public void unloadPlugin(String uuid) throws Exception {
      // TODO Auto-generated method stub
   }

   /**
    * @see ch.ethz.jadabs.pluginLoader.api.PluginLoader#getExtensionGraph(java.lang.String)
    */
   public String getExtensionGraph(String uuid) {
      // TODO Auto-generated method stub
      return null;
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

   protected static InputStream fetchInformation(String uuid, Object requestor) {
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
         infoSources.add(infoSources);
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
   
   private String opd2obr(String opd) {
      return opd.substring(0,opd.length()-3) + "obr"; 
   }

}