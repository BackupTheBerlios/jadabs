package ch.ethz.jadabs.bundleLoader.api;
import java.util.Enumeration;
import java.util.Hashtable;

/*
 * Created on 16-Feb-2005
 */

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class MatchItem {
   private String name;
   private Hashtable properties = new Hashtable();
   
   public MatchItem(String filterPart) {
      String[] parts = filterPart.split("/");
      this.name = parts[0].trim();
      String[] props = parts[1].split(",");
      for (int index = 0; index < props.length; index++) {
         parts = props[index].split(":");
         properties.put(parts[0].trim(), parts[1].trim());
      }      
   }
   
   public void addProperty(String property, String value) {
      properties.put(property, value);
   }
    
   public boolean equals(Object obj) {      
      // DEBUG one line   
      System.out.println("testing " + obj + " against " + toString());
      if (obj instanceof String) {
         MatchItem test = new MatchItem((String)obj);         
         return equals(test);         
      } else if (obj instanceof MatchItem) {
         MatchItem elem = (MatchItem)obj;
         if (!name.equals(elem.name)) return false;
         for (Enumeration en = elem.properties.keys(); en.hasMoreElements(); ) {
            String key = (String)en.nextElement();
            if (!elem.properties.get(key).equals(properties.get(key))) return false;
         }
         return true;
      } 
      return false;
   }
     
   public String toString() {
      return name + " - " + properties;
   }
}
