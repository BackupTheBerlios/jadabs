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
 * Created on 14-Feb-2005
 */
package ch.ethz.jadabs.pluginLoader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.kxml2.io.KXmlParser;

import ch.ethz.jadabs.bundleLoader.api.Descriptor;

/**
 * Encapsulates opd information 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class PluginDescriptor extends Descriptor {
   private static Logger LOG = Logger.getLogger(PluginDescriptor.class);
   private KXmlParser parser;
   protected Vector extensions = new Vector();
   private ArrayList extensionPoints = new ArrayList();
   protected String activator;
   
   /**
    * Hidden constructor. But as an abstract class is inherited, the abstract 
    * standard constructor must be virtually called, even if never used. 
    */
   private PluginDescriptor() {
      super(null);
   }

   /**
    * This constructor return a new PluginDescriptor. Note, that this method should 
    * not be called directly, use the getPluginDescriptor of the PluginLoader instead 
    * to use the weak references model. 
    * @param uuid Uuid of the opd
    */
   protected PluginDescriptor(String uuid) throws Exception {
      super(uuid);

      parser = new KXmlParser();

      InputStream instream = PluginLoaderImpl.getInstance().fetchInformation(uuid, this);
      parser.setInput(new InputStreamReader(instream));
      parseOPD();

      if (LOG.isDebugEnabled())
         LOG.debug("Created new BundleDescriptor " + uuid);
   }

   /**
    * parse the opd
    * @throws Exception
    */
   private void parseOPD() throws Exception {
      Stack stack = new Stack();
      for (int type = parser.next(); (type != KXmlParser.END_DOCUMENT); type = parser
            .next()) {
         if (type == KXmlParser.START_TAG) {
            stack.push(parser.getName());

            if (stack.peek().equals("Extension")) {
               // TODO: add to Extension vector
               
               /*
               String id = parser.getAttributeValue(null, "id");
               LOG.debug("FOUND EXTENSION " + id);
               String extension = new String();
               if (!id.startsWith("Extension")) {
                  extension = extension.concat(parser.getName() + "/");
                  for (int index = 0; index < parser.getAttributeCount(); index++) {
                     if (index != 0)
                        extension = extension.concat(", ");
                     extension = extension.concat(parser.getAttributeName(index) + ":"
                           + parser.getAttributeValue(index));
                  }
               }
               System.out.println("EXTENSION: " + extension);                                                                                                                                                                     
               */
            } else if (stack.peek().equals("Extension-Point")) {
               String id = parser.getAttributeValue(null, "id");
               extensionPoints.add(id);
               
               LOG.debug("FOUND EXTENSION-POINT " + id);
               
               if (id.startsWith("Extension")) {
               ArrayList matchingPlugins = new ArrayList();
               if (LOG.isDebugEnabled()) {
                  LOG.debug("\n");
                  LOG.debug("REQUESTING " + id + " ¦ " + PluginLoaderImpl.platform + " ¦ " + "R");
                  LOG.debug("\n");
               }
               for (Iterator matches = PluginLoaderImpl.getMatchingPlugins(id + " ¦ " + PluginLoaderImpl.platform + " ¦ " + "R"); matches.hasNext(); ) {
                  matchingPlugins.add(matches.next());
               }
               
               if (matchingPlugins.isEmpty()) throw new Exception("Unsatisfied extension point " + id + " in Plugin " + this.toString());
               if (matchingPlugins.size() == 1) {
                  PluginLoaderImpl.scheduler.addPlugin((String)matchingPlugins.get(0));
               } else {
                  PluginLoaderImpl.scheduler.addAlternativePlugins(matchingPlugins);
               }
              
               PluginLoaderImpl.scheduler.stillToProcess.addAll(0, matchingPlugins);
               }
               
            } else if (stack.peek().equals("ServiceActivatorBundle")) {
               this.activator = parser.getAttributeValue(null, "activator-uuid");
            }
         } else if (type == KXmlParser.END_TAG) {
            try {
               stack.pop();
            } catch (Exception e) {
               LOG.error("ERROR while parsing, Platform-File not well-formed");
            }
         }
      }
   }
   
   public Iterator getExtensionPoints()
   {
       return extensionPoints.iterator();
   }
}