/*
 * Created on 15-Feb-2005
 */
package ch.ethz.jadabs.pluginLoader;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import org.apache.log4j.Logger;
import ch.ethz.jadabs.bundleLoader.Repository;
import ch.ethz.jadabs.bundleLoader.api.InformationSource;
import ch.ethz.jadabs.bundleLoader.api.LoaderListener;
import ch.ethz.jadabs.pluginLoader.api.PluginLoader;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class PluginLoaderImpl implements PluginLoader {
   private PluginLoaderImpl me;
   private static Vector infoSources = new Vector();
   private Vector loaderListeners = new Vector();
   private static Hashtable descriptorCache = new Hashtable();
   private static HashSet loadedPlugins = new HashSet();
   private static Logger LOG = Logger.getLogger(PluginLoaderImpl.class);
   
   
   private PluginLoaderImpl() {
      infoSources.add(new Repository());
      
   }
   
   
   public PluginLoaderImpl getInstance() {
      if (me == null)
         me = new PluginLoaderImpl();
      return me;
   }
   
   
   /**
    * @see ch.ethz.jadabs.pluginLoader.api.PluginLoader#loadPlugin(java.lang.String)
    */
   public void loadPlugin(String uuid) throws Exception {
      // TODO Auto-generated method stub      
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
    * @see ch.ethz.jadabs.pluginLoader.api.PluginLoader#getMatchingPlugins(java.lang.String)
    */
   public Iterator getMatchingPlugins(String filter, Object requestor) {
      HashSet result = new HashSet();
      for (Enumeration sources = infoSources.elements(); sources.hasMoreElements();) {
         InformationSource source = (InformationSource) sources.nextElement();
         if (source instanceof InformationSource) {
            if (((InformationSource)source) == requestor) return result.iterator();
         }
         Iterator it = source.getMatchingPlugins(filter);
         while (it.hasNext()) {
            result.add(it.next());
         }
      }
      return result.iterator();
   }


   private PluginDescriptor getPluginDescriptor(String uuid) {
      PluginDescriptor result = null;
      try {
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
      } catch (Exception e) {
         e.printStackTrace();
         LOG.error("Could not locate bundle descriptor " + uuid);
      }
      if (result != null) {
         /*
         result.processed = false;
      	 result.level = 0;
      	 */
      }
      return result;
   }

   
   protected static InputStream fetchInformation(String uuid) {
      InputStream result = null;
      for (Enumeration sources = infoSources.elements(); sources
            .hasMoreElements();) {
         if ((result = ((InformationSource) sources.nextElement())
               .retrieveInformation(uuid)) != null)
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

}
