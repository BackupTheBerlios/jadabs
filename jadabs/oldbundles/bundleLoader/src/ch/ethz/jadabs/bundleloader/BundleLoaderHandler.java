/*
 * Created on 07-Feb-2005
 */
package ch.ethz.jadabs.bundleloader;

import java.io.File;

import ch.ethz.jadabs.http.HttpSocket;


/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class BundleLoaderHandler implements ch.ethz.jadabs.http.RequestHandler {
   private final static String repolocation = BundleLoaderActivator.bc.getProperty("org.knopflerfish.gosg.jars");
 
   
   /**
    * @see ch.ethz.jadabs.http.RequestHandler#delegate(ch.ethz.jadabs.http.HttpSocket)
    */
   public boolean delegate(HttpSocket request) {      
      System.out.println("REQUEST: " + request.file);
      if (request.file.startsWith("/listrepo")) {
         File repofile = new File(repolocation.substring(5) + File.separatorChar + ".." + File.separatorChar + "htdocs" + File.separatorChar + "repository.xml");
         request.sendFile(repofile, "text/xml", null);
         return true;
      } 
      if (request.file.startsWith("/getobr/")) {
         String id = request.file.substring(8);
         System.out.println(id);
         try {
            String group = id.substring(0, id.indexOf(":"));
            id = id.substring(id.indexOf(":") + 1);
            String name = id.substring(0, id.indexOf(":"));
            id = id.substring(id.indexOf(":") + 1);
            String version = id.substring(0, id.indexOf(":"));
            String rest = id.substring(id.indexOf(":") + 1);

            File repofile = new File(repolocation.substring(5) + group
                  + File.separatorChar + "obrs" + File.separatorChar + name
                  + "-" + version + ".obr");
            
            request.sendFile(repofile, "text/xml", null);
         } catch (Exception e) {
            e.printStackTrace();
            request.send404();            
         } 
         return true;
      }
      if (request.file.startsWith("/getjar/")) {
         String id = request.file.substring(8);
         System.out.println(id);
         try {
            String group = id.substring(0, id.indexOf(":"));
            id = id.substring(id.indexOf(":") + 1);
            String name = id.substring(0, id.indexOf(":"));
            id = id.substring(id.indexOf(":") + 1);
            String version = id.substring(0, id.indexOf(":"));
            String rest = id.substring(id.indexOf(":") + 1);

            File repofile = new File(repolocation.substring(5) + group
                  + File.separatorChar + "jars" + File.separatorChar + name
                  + "-" + version + ".jar");

            request.sendFile(repofile, "application/x-java-jar-file", "Content-disposition: attachment; filename =" + name + "-" + version + ".jar"  + "\r\n");
         } catch (Exception e) {
            e.printStackTrace();
            request.send404();            
         }
         return true;
      }
      if (request.file.startsWith("/test")) {
         request.sendString("<test>Hallo Welt</test>", "text/xml");
         return true;
      } 
      if (request.file.startsWith("/error")) {
         request.send404();
         return true;
      }
      return false;
   }

}
