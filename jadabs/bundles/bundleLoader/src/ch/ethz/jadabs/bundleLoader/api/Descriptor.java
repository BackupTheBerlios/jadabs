/*
 * Created on 14-Feb-2005
 */
package ch.ethz.jadabs.bundleLoader.api;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public abstract class Descriptor {
   private String uuid;
   private String location;
    
   protected Descriptor(String uuid) {
      this.uuid = uuid;
   }     

   public String toString() {
      return uuid;
   }
}
