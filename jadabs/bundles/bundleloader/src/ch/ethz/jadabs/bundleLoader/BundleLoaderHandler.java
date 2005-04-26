/*
 * Copyright (c) 2003-2005, Jadabs project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials
 *   provided with the distribution.
 *
 * - Neither the name of the Jadabs project nor the names of its
 *   contributors may be used to endorse or promote products derived
 *   from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on 07-Feb-2005
 */
package ch.ethz.jadabs.bundleLoader;

import java.io.File;

import ch.ethz.jadabs.bundleLoader.api.HttpRequestHandler;
import ch.ethz.jadabs.bundleLoader.api.Utilities;
import ch.ethz.jadabs.http.HttpSocket;


/**
 * HttpRequestHandler for BundleLoader
 * Can be registered to the HttpDaemon to process requests concerning bundles. 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class BundleLoaderHandler implements HttpRequestHandler {
   private final static String repolocation = BundleLoaderActivator.bc.getProperty("org.knopflerfish.gosg.jars");
   
   /**
    * This method is called by the HttpDaemon if a request comes in.
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
         String uuid = request.file.substring(8);

         try {
            String[] args = Utilities.split(uuid, ":");
            String group = args[0];
            String name = args[1];
            String version = args[2];
            String type = args[3];

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
         String uuid = request.file.substring(8);

         try {
            String[] args = Utilities.split(uuid, ":");
            String group = args[0];
            String name = args[1];
            String version = args[2];
            String type = args[3];

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
      return false;
   }
}
