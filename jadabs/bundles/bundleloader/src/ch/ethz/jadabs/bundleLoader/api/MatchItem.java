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
 * Created on 16-Feb-2005
 */
package ch.ethz.jadabs.bundleLoader.api;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Encapsulates items that are matched during filter processing 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class MatchItem {
   private String name;
   private Hashtable properties = new Hashtable();

   /**
    * Constructor
    * @param filterPart The <code>String</code> holding the filter part
    * that will be encapsulated. Format: Element/attribute:value or concatention
    * of these clauses separated by ";".  
    */
   public MatchItem(String filterPart) {
       
       String[] parts = Utilities.split(filterPart,"/");
      this.name = parts[0].trim();
      String[] props = Utilities.split(parts[1],",");
      for (int index = 0; index < props.length; index++) {
         parts = Utilities.split(props[index],":");
         properties.put(parts[0].trim(), parts[1].trim());
      }      
   }
   
   /**
    * adds a property to the property list
    * @param property 
    * @param value
    */
   public void addProperty(String property, String value) {
      properties.put(property, value);
   }
    
   /**
    * core function of the <code>MatchItem</code>
    * used to check the matching of two items. 
    * Can be called on a <code>MatchItem</code> 
    * or directly on a FilterPart String. 
    * Implicitly used by .contains(...) 
    * functions of collections etc. 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   public boolean equals(Object obj) {      
      // DEBUG one line   
      // System.out.println("testing " + obj + " against " + toString());
      if (obj instanceof String) {
         MatchItem test = new MatchItem((String)obj);         
         return equals(test);         
      } else if (obj instanceof MatchItem) {
         MatchItem elem = (MatchItem)obj;
         if (!name.equals(elem.name)) return false;
         for (Enumeration en = properties.keys(); en.hasMoreElements(); ) {
            String key = (String)en.nextElement();
            if (!properties.get(key).equals(elem.properties.get(key))) return false;
         }
         return true;
      } 
      return false;
   }
     
   /**
    * @see java.lang.Object#toString()
    */
   public String toString() {
      return name + " - " + properties;
   }
}
