/*
 * Created on 15-Feb-2005
 */
package ch.ethz.jadabs.pluginLoader.api;

import java.util.Iterator;

import ch.ethz.jadabs.bundleLoader.api.HttpRequestHandler;
import ch.ethz.jadabs.bundleLoader.api.InformationSource;
import ch.ethz.jadabs.bundleLoader.api.LoaderListener;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public interface PluginLoader {

   public void loadPlugin(String uuid) throws Exception;
   
   public String getExtensionGraph(String uuid);
   
   public Iterator getInstalledPlugins();
   
   // registration functions  
   public void registerInformationSource(InformationSource infoSource);
   public void unregisterInformationSource(InformationSource infoSource);
   
   // callback registration functions
   public void registerLoaderListener(LoaderListener listener);
   public void unregisterLoaderListener(LoaderListener listener);

}
