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
 * Created on 17-Feb-2005
 */
package ch.ethz.jadabs.pluginLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.kxml2.io.KXmlParser;



/**
 * Static class to parse platform informations 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class PlatformInformation {
   private static Logger LOG = Logger.getLogger(PlatformInformation.class);

   /**
    * hidden constructor, this is a static class
    */
   private PlatformInformation() {
   }

   /**
    * parses platform information from a file and returns a filterPart
    * @param location <code>String</code> of the platform file location
    * @return <code>String</code> that represents a filterPart, can be used
    *         as second part of PluginFilters.  
    */
   public static String parsePAD(String location) {
      try {
    	  
         File file = new File(location);
         FileInputStream fis = new FileInputStream(file);
         KXmlParser parser = new KXmlParser();
         
         parser.setInput(new InputStreamReader(fis));
         StringBuffer buffer = new StringBuffer();
         for (int type = parser.next(); 
         		(type != KXmlParser.END_DOCUMENT); 
         		 type = parser.next()) 
         {	 
            if (!(type == KXmlParser.START_TAG)) continue;
            buffer.append("; ");
            if (type == KXmlParser.START_TAG) {
               buffer.append(parser.getName() + "/");
               for (int index = 0; index < parser.getAttributeCount(); index++) {
                  if (parser.getAttributeName(index).equals("description")) continue;
                  if (index != 0)
                     buffer.append(", ");
                  buffer.append(parser.getAttributeName(index) + ":"
                        + parser.getAttributeValue(index));
               }
            }
         }
         return buffer.substring(2);
      } catch (Exception e) {
         LOG.error(e.getMessage());
         return new String("");
      }
   }
}