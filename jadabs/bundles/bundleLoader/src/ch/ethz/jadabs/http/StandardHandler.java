/*
 * Created on 13-Feb-2005
 */
package ch.ethz.jadabs.http;

import java.io.File;

import ch.ethz.jadabs.bundleLoader.BundleLoaderActivator;
import ch.ethz.jadabs.bundleLoader.api.HttpRequestHandler;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class StandardHandler implements HttpRequestHandler {
   private final static String repolocation = BundleLoaderActivator.bc.getProperty("org.knopflerfish.gosg.jars");
   /**
    * @see ch.ethz.jadabs.http.HttpRequestHandler#delegate(ch.ethz.jadabs.http.HttpSocket)
    */
   public boolean delegate(HttpSocket request) {      
      if (request.file.startsWith("/favicon.ico")) {
         File favicon = new File(repolocation.substring(5) + File.separatorChar + "favicon.ico");
         request.sendFile(favicon, "image/ico", null);
      } else {
         request.sendString("<test>Greetings from standard handler</test>", "text/xml");
      }
      return true;
   }

}
