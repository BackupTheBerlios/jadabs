/*
 * Created on 14-Feb-2005
 */
package ch.ethz.jadabs.bundleLoader.api;

import java.util.Iterator;


/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public interface BundleLoader {
   
   public void loadBundle(String uuid) throws Exception;
   
   public String getDependencyGraph(String uuid);
   
   public Iterator getInstalledBundles();
   

   // registration functions
   public void registerRequestHandler(HttpRequestHandler handler);
   public void unregisterRequestHandler(HttpRequestHandler handler);
   
   public void registerInformationSource(InformationSource infoSource);
   public void unregisterInformationSource(InformationSource infoSource);
   
   // callback registration functions
   public void registerLoaderListener(LoaderListener listener);
   public void unregisterLoaderListener(LoaderListener listener);
}
