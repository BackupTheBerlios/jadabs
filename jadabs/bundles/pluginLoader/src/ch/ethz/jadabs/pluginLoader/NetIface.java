/*
 * Created on 22.01.2005
 */
package ch.ethz.jadabs.pluginLoader;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class NetIface {
   private String type;
   private String connection;
   private String configuration;
   private String name;
   private String essid;
   private String mode;
   private String iface;
   private String ip;
   private String description;
   
   public NetIface(String type, String connection, String configuration, String name, String essid, String mode, String iface, String ip, String description) {
      this.type = type;
      this.connection = connection;
      this.configuration = configuration;
      this.name = name;
      this.essid = essid;
      this.mode = mode;
      this.iface = iface;
      this.ip = ip;
      this.description = description;
   }
   
   public String toString() {
      return "NetIface/" + type;
   }
}
