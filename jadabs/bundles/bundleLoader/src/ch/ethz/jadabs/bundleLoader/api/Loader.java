/*
 * Created on 16-Feb-2005
 */
package ch.ethz.jadabs.bundleLoader.api;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public interface Loader {

   // registration functions
   public void registerInformationSource(InformationSource infoSource);
   public void unregisterInformationSource(InformationSource infoSource);
   
   // callback registration functions
   public void registerLoaderListener(LoaderListener listener);
   public void unregisterLoaderListener(LoaderListener listener);

}
