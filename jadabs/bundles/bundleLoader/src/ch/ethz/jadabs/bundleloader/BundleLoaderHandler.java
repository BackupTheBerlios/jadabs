/*
 * Created on 07-Feb-2005
 */
package ch.ethz.jadabs.bundleloader;

import ch.ethz.jadabs.http.HttpSocket;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class BundleLoaderHandler implements ch.ethz.jadabs.http.RequestHandler {

   /**
    * @see ch.ethz.jadabs.http.RequestHandler#delegate(ch.ethz.jadabs.http.HttpSocket)
    */
   public void delegate(HttpSocket request) {
      System.out.println("REQUEST: " + request.file);      
   }

}
