/*
 * Created on 24.01.2005
 */
package ch.ethz.jadabs.pluginLoader;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class ActivatorBundle {

   private String name;
   private String group;
   private String version;

   public ActivatorBundle(String name, String group, String version) {
      this.name = name;
      this.group = group;
      this.version = version;
   }
   
   public String getName() {
      return name;      
   }
   
   public String getGroup() {
      return group;
   }
   
   public String getVersion() {
      return version;
   }
   
   public String toString() {
      return name + "-" + version;
   }
   
}
