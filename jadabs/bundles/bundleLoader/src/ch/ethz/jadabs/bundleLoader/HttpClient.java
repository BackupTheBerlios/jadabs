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
 * Created on 15-Feb-2005
 */
package ch.ethz.jadabs.bundleLoader;

import java.io.*;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.bundleLoader.api.InformationSource;
import ch.ethz.jadabs.bundleLoader.api.PluginFilterMatcher;
import ch.ethz.jadabs.http.HttpSocket;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class HttpClient extends PluginFilterMatcher implements InformationSource {
   private Vector knownHosts;
   private static Logger LOG = Logger.getLogger(HttpClient.class);


   public HttpClient() {
      //TODO: read property or file and build up list of known hosts
      
      knownHosts = new Vector();
      knownHosts.add("jadabsrepo.ethz.ch");
   }
   
   /**
    * @see ch.ethz.jadabs.bundleLoader.api.InformationSource#retrieveInformation(java.lang.String)
    */
   public InputStream retrieveInformation(String uuid) {
      String[] args = uuid.split(":");
      String group = args[0];
      String name = args[1];
      String version = args[2];
      String type = args[3];

      for (Enumeration hosts = knownHosts.elements(); hosts.hasMoreElements(); ) {
         try {
            String host = (String)hosts.nextElement();
            HttpSocket clientSocket = new HttpSocket(host, 9278);

            clientSocket.get("/get" + type + "/" + uuid);
            clientSocket.request();

            return new ByteArrayInputStream(clientSocket.data.getBytes());
            
         } catch (Exception e) {
            e.printStackTrace();
         }
        
      }
            
      return null;
   }

   /**
    * @see ch.ethz.jadabs.bundleLoader.api.InformationSource#retrieveInformation(java.lang.String, java.lang.String)
    */
   public InputStream retrieveInformation(String uuid, String source) {
      String[] args = uuid.split(":");
      String group = args[0];
      String name = args[1];
      String version = args[2];
      String type = args[3];

     try {
        HttpSocket clientSocket = new HttpSocket(source, 9278);

        clientSocket.get("/get" + type + "/" + uuid);
        clientSocket.request();

        return new ByteArrayInputStream(clientSocket.data.getBytes());
           
     } catch (Exception e) {
        e.printStackTrace();
     }
     
     return null;
   }

   /**
    * @see ch.ethz.jadabs.bundleLoader.api.InformationSource#getMatchingPlugins(java.lang.String)
    */
   public Iterator getMatchingPlugins(String filter) {

      for (Enumeration hosts = knownHosts.elements(); hosts.hasMoreElements(); ) {
         try {
            String host = (String)hosts.nextElement();
            HttpSocket clientSocket = new HttpSocket(host, 9278);

            clientSocket.get("/match" + "/" + filter);
            clientSocket.request();

            return new PluginIterator(clientSocket.data);
            
         } catch (Exception e) {
            e.printStackTrace();
         }
        
      }
            
      return null;
   }

   /**
    * @see ch.ethz.jadabs.bundleLoader.api.PluginFilterMatcher#debug(java.lang.String)
    */
   protected void debug(String str) {
      if(LOG.isDebugEnabled())
         LOG.debug(str);
   }

   /**
    * @see ch.ethz.jadabs.bundleLoader.api.PluginFilterMatcher#error(java.lang.String)
    */
   protected void error(String str) {
      LOG.error(str);
   }  

   
   public class PluginIterator implements Iterator {
      private String[] plugins;
      private int index;
      
      public PluginIterator(String data) {
         plugins = data.split("#####");
         index = 0;
      }

      /**
       * @see java.util.Iterator#remove()
       */
      public void remove() {
         // It is optional, we don't need it so we leave it unimplemented         
      }

      /**
       * @see java.util.Iterator#hasNext()
       */
      public boolean hasNext() {
         return (index < plugins.length);
      }

      /**
       * @see java.util.Iterator#next()
       */
      public Object next() {
         return new ByteArrayInputStream(plugins[index].getBytes());
      }            
   }
   
}
