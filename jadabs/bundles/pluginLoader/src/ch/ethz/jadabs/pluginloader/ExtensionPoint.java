/*
 * Created on 01.01.2005
 */
package ch.ethz.jadabs.pluginloader;


/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class ExtensionPoint {
   private static final int PLATFORM = 0;
   private static final int EXTENSION = 1;
   
   private String id;
   private String service;
   private int type;   
   private String section;  
   private String description;
   
   public ExtensionPoint(String id, String service, String description) {
      if (id.startsWith("Extension/")) {
         this.id = id.substring(10);
         type = EXTENSION;
      } else if (id.startsWith("Platform/")) {
         id = id.substring(9);
         int pos = id.indexOf("/");
         if (pos >= 0) {
            section = id.substring(0,pos);
            id = id.substring(pos+1);
         }
         this.id = id;
      } else {
         this.id = id;
      }
      this.description = description;
      this.description = service;
   }
   
   public String toString() {
      if (type == EXTENSION) {
         return id;
      } else {
         return section + "/" + id;
      }
   }

   /*
   public boolean equals(Object obj) {
      if (obj instanceof ExtensionPoint) {
         ExtensionPoint other = (ExtensionPoint)obj;
         return (other.id.equals(id) && other.service.equals(service) && other.type == type);
      }
      return false;
   }
   */
   
   protected String id() {
      return id;      
   }
   
   protected String service() {
      return service;
   }
   
}
