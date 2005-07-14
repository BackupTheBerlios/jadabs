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

package ch.ethz.jadabs.bundleLoader.api;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Stack;

import org.kxml2.io.KXmlParser;

/**
 * Abstract base class of all classes that can match plugin filters 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public abstract class PluginFilterMatcher {   
   
   /**
    * Checks if the InputStream of a plugin opd file 
    * matches a filter. 
    * @param plugin 
    * @param filter
    * @return <code>boolean</code> value of matching
    */
   protected boolean matches(InputStream plugin, String filter) {
      MatchItem requires = extensionPointFromFilter(filter);
      LinkedList provides = environmentFromFilter(filter);
      Stack stack = new Stack();
      
      try {
         KXmlParser parser = new KXmlParser();
         parser.setInput(new InputStreamReader(plugin));
         boolean matches = false;
         
                
         for (int type = parser.next(); (type != KXmlParser.END_DOCUMENT); type = parser
         .next()) {
            if (type == KXmlParser.START_TAG) {
               stack.push(parser.getName());

               if (stack.peek().equals("Extension")) {
                  String id = parser.getAttributeValue(null, "id");
                  debug("FOUND EXTENSION " + id);
                  if (requires == null || requires.equals("Extension/" + "id:" + id)) {
                     matches = true;
                  }
               } else if (stack.peek().equals("Extension-Point")) {
                  String id = parser.getAttributeValue(null, "id");
                  debug("FOUND EXTENSION-POINT " + id);
                  // only match platform clauses here, extensionPoints are 
                  // not subjects of a matching
                  if (!id.startsWith("Extension")) {                     
                     MatchItem elem = new MatchItem(id);
                     if (!provides.contains(elem)) {
                        debug("Extension-Point " + id + " unsatisfied");
                        return false;
                     }
                  }                  
               }               
            } else if (type == KXmlParser.END_TAG) {
               try {
                  stack.pop();
               } catch (Exception e) {
                  error("ERROR while parsing, Platform-File not well-formed");
               }
            }
         }
         
         if (requires == null)
             return true;
         
         return matches;
      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }           
   }
   
   /**
    * Returns the extension clause of a filter
    * @param filter
    * @return extension clause
    */
   private MatchItem extensionPointFromFilter(String filter) {      
      String[] parts = Utilities.split(filter,"¦");
      if (parts[0].trim().equals(""))
          return null;
      return new MatchItem(parts[0]);
   }

   /**
    * Returns the platform clause of a filter
    * @param filter 
    * @return platform clause
    */
   private LinkedList environmentFromFilter(String filter) {
                     
      LinkedList result = new LinkedList();
      
      String[] parts = Utilities.split(filter,"¦");
      if (!parts[1].trim().equals("")) {
         parts = Utilities.split(parts[1],";");
         for (int index=0; index < parts.length; index++) {
            result.add(new MatchItem(parts[index]));
         }
      }
      return result;
   }
   
   protected abstract void debug(String str);
   
   protected abstract void error(String str);
}
