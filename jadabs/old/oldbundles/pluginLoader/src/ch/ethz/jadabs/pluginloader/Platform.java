/*
 * Created on 02.01.2005
 */
package ch.ethz.jadabs.pluginloader;

import java.util.Properties;
import java.util.Vector;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class Platform {
   private String id;
   private String name;
   private String version;
   private String provider;
   private Properties properties = new Properties();
   private Vector ifaces = new Vector();
   
   public Platform(String id, String name, String version, String provider) {
      this.id = id;
      this.name = name;
      this.version = version;
      this.provider = provider;
   }
   
   public void setProperty(String name, String value) {
      properties.setProperty(name, value);
   }

   public String getProperty(String name) {
      return properties.getProperty(name);
   }
   
   public void addNetIface(NetIface iface) {
      ifaces.add(iface);
   }
}
