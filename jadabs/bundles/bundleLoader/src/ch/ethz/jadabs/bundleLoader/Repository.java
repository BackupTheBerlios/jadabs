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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.bundleLoader.api.InformationSource;
import ch.ethz.jadabs.bundleLoader.api.PluginFilterMatcher;

/**
 * Manages the Repository and provides an <code>InformationSource</code>
 * adapter to the repository file system. Also implements
 * <code>PluginFilterMatcher</code> to find plugins in 
 * the repository that match a filter string. 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class Repository extends PluginFilterMatcher implements InformationSource {

   private String repopath = BundleLoaderActivator.bc.getProperty("org.knopflerfish.gosg.jars").substring(5);
   private static Logger LOG = Logger.getLogger(Repository.class);
   
   
   /**
    * Retrieves a file from the file system, e.g. a bundle jar or obr
    * @see ch.ethz.jadabs.bundleLoader.api.InformationSource#retrieveInformation(java.lang.String)
    */
   public InputStream retrieveInformation(String uuid) {
      try {
         String[] args = uuid.split(":");
         String group = args[0];
         String name = args[1];
         String version = args[2];
         String type = args[3];

         File repofile = new File(repopath + File.separator + group + File.separator + type + "s"
         + File.separator + name + "-" + version + "." + type);
         
         if (repofile.exists())
             return new FileInputStream(repofile);
         else
             return null;
         
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }

   
   /**
    * Directed retrieval is not implemented for repositories. Redirects 
    * the call to the undirected method. 
    * @see ch.ethz.jadabs.bundleLoader.api.InformationSource#retrieveInformation(java.lang.String, java.lang.String)
    */
   public InputStream retrieveInformation(String uuid, String source) {
      return retrieveInformation(uuid);
   }

   
   /**
    * Find plugins, that match a given filter string. 
    * @throws Exception
    * @see ch.ethz.jadabs.bundleLoader.api.InformationSource#getMatchingPlugins(java.lang.String)
    */
   public Iterator getMatchingPlugins(String filter) throws Exception {
      ArrayList result = new ArrayList();
      
      try {
         List files = getOPDListing(new File(repopath));
         
         Iterator filesIter = files.iterator();
         while( filesIter.hasNext() ){
            File file = (File)filesIter.next();
            if (LOG.isDebugEnabled())
               LOG.debug(file);
            if (matches(new FileInputStream(file), filter)) {
               result.add(location2uuid(file.toString()));
               if (LOG.isDebugEnabled())
                  LOG.debug("MATCHES: " + location2uuid(file.toString()));
            }            
         }
         
      } catch (FileNotFoundException err) {
         err.printStackTrace();
      }
      return result.iterator();
   }
   
   
   /**
    * Get all opds in the repository file system.
    * @param startingDir Directory to start.  
    * @return List of all opd files
    * @throws FileNotFoundException
    */
   public List getOPDListing(File startingDir) throws FileNotFoundException {
      List result = new ArrayList();

      File[] filesAndDirs = startingDir.listFiles();
      List filesDirs = Arrays.asList(filesAndDirs);
      Iterator filesIter = filesDirs.iterator();
      File file = null;
      while ( filesIter.hasNext() ) {
        file = (File)filesIter.next();
        if (!file.isFile()) {
          List deeperList = getOPDListing(file);
          result.addAll(deeperList);
        } else if (file.getName().endsWith(".opd")){
           result.add(file);
        }
      }
      return result;
    }

   
   /**
    * redirector method used by PluginFilterMatcher methods
    * @see ch.ethz.jadabs.bundleLoader.api.PluginFilterMatcher#debug(java.lang.String)
    */
   protected void debug(String str) {
      if (LOG.isDebugEnabled())
         LOG.debug(str);      
   }

   
   /**
    * redirector method used by PluginFilterMatcher methods 
    * @see ch.ethz.jadabs.bundleLoader.api.PluginFilterMatcher#error(java.lang.String)
    */
   protected void error(String str) {
      LOG.error(str);      
   }
   
   /**
    * Gets a opd uuid from a file location
    * @param loc file location
    * @return opd uuid
    * @throws Exception
    */
   private static String location2uuid(String loc) throws Exception {
      int pos = loc.lastIndexOf(File.separatorChar);

      if (pos > -1) {
         String filename = loc.substring(pos + 1);
         int pos2 = filename.indexOf(".opd");
         if (pos2 > -1) {
            filename = filename.substring(0, pos2);
         }
         pos2 = filename.indexOf("-");
         while (!Character.isDigit(filename.charAt(pos2+1))) {
            int next = filename.substring(pos2+1).indexOf("-");
            if (next == -1) throw new Exception("Illegal uuid");
            pos2 +=next+1;
         }
         String name = filename.substring(0, pos2);
         String version = filename.substring(pos2 + 1);

         String group = loc.substring(0, pos);
         pos2 = group.lastIndexOf(File.separatorChar);
         group = group.substring(0, pos2);
         pos2 = group.lastIndexOf(File.separatorChar);
         group = group.substring(pos2 + 1);

         return group + ":" + name + ":" + version + ":opd";
      }
      return null;
   }
  
}
