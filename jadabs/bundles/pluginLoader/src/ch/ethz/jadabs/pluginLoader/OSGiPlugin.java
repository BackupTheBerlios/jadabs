/*
 * Created on 01.01.2005
 */
package ch.ethz.jadabs.pluginLoader;

import java.util.Enumeration;
import java.util.Vector;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class OSGiPlugin {
   private String id;
   private String name;
   private String version;
   private String description;
   private String provider;
   private Vector extensions = new Vector();
   private Vector extensionPoints = new Vector();
   
   public OSGiPlugin(String id, String name, String version, String description, String provider) {
      this.id = id;
      this.name = name;
      this.version = version;
      this.description = description;
      this.provider = provider;
   }
   
   public void addExtension(Extension ext) {
      extensions.add(ext);
   }

   public Enumeration getExtensions() {
      return extensions.elements();
   }
    
   public void addExtensionPoint(ExtensionPoint extp) {
      extensionPoints.add(extp);
   }
   
   public Enumeration getExtensionPoints() {
      return extensionPoints.elements();
   }
   
   public String getName() {
      return name + "::" + id;
   }
   
   public String toString() {
      StringBuffer buffer = new StringBuffer();
      buffer.append(id + ", ");
      buffer.append("Extensions: ");
      for (Enumeration en = extensions.elements(); en.hasMoreElements(); ) {
         buffer.append(en.nextElement().toString() + ", ");
      }
      buffer.append("ExtensionPoints: ");
      for (Enumeration en = extensionPoints.elements(); en.hasMoreElements(); ) {
         buffer.append(en.nextElement().toString() + ", ");
      }

      return buffer.toString();
   }
      
}
