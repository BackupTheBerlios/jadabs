/*
 * Created on 01.01.2005
 */
package ch.ethz.jadabs.pluginLoader;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class Extension {
   private String id;
   private String service;
   
   public Extension(String id, String service) {
      this.id = id;
      this.service = service;
   }
   
   public String toString() {
      return id;
   }
      
   protected String id() {
      return id;
   }
   
   protected String service() {
      return service;
   }

}
