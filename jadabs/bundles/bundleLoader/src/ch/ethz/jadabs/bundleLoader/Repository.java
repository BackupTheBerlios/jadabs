/*
 * Created on 15-Feb-2005
 */
package ch.ethz.jadabs.bundleLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import ch.ethz.jadabs.bundleLoader.api.InformationSource;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class Repository implements InformationSource {

   private String repopath = BundleLoaderActivator.bc.getProperty("org.knopflerfish.gosg.jars");
   
   /**
    * @see ch.ethz.jadabs.bundleLoader.api.InformationSource#retrieveInformation(java.lang.String)
    */
   public InputStream retrieveInformation(String uuid) {
      try {
         String[] args = uuid.split(":");
         String group = args[0];
         String name = args[1];
         String version = args[2];
         String type = args[3];

         File repofile = new File(repopath.substring(5) + File.separator + group + File.separator + type + "s"
         + File.separator + name + "-" + version + "." + type);
         
         return new FileInputStream(repofile);
         
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   /**
    * @see ch.ethz.jadabs.bundleLoader.api.InformationSource#retrieveInformation(java.lang.String, java.lang.String)
    */
   public InputStream retrieveInformation(String uuid, String source) {
      return retrieveInformation(uuid);
   }
   
}
