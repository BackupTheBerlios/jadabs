/*
 * Created on 14-Feb-2005
 */
package ch.ethz.jadabs.bundleLoader.api;

import java.util.Iterator;


/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public interface BundleLoader extends Loader {
   
   public void loadBundle(String uuid) throws Exception;
   
   public String getDependencyGraph(String uuid);
   
   public Iterator getInstalledBundles();   

   // registration functions
   public void registerRequestHandler(HttpRequestHandler handler);
   public void unregisterRequestHandler(HttpRequestHandler handler);
   
}
