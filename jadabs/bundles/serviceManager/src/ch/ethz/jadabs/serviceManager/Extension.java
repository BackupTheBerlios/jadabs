/*
 * Created on 01.01.2005
 */
package ch.ethz.jadabs.serviceManager;

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
    
   public boolean matches(ExtensionPoint ext) {
      return (id.equals(ext.id()) && service.equals(ext.service()));
   }
   
   public boolean matches(Extension extp) {
      return false;
   }
   
   protected String id() {
      return id;
   }
   
   protected String service() {
      return service;
   }

}
