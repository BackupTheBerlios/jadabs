/*
 * Created on 15-Feb-2005
 */
package ch.ethz.jadabs.pluginLoader.api;

import java.util.Iterator;

import ch.ethz.jadabs.bundleLoader.api.Loader;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public interface PluginLoader extends Loader {

   public void loadPlugin(String uuid) throws Exception;
   
   public void unloadPlugin(String uuid) throws Exception;
   
   public String getExtensionGraph(String uuid);
   
   public Iterator getInstalledPlugins();
   
   public Iterator getMatchingPlugins(String filter, Object requestor) throws Exception;
   
}
