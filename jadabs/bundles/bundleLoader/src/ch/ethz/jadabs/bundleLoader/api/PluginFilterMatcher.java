package ch.ethz.jadabs.bundleLoader.api;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Stack;

import org.kxml2.io.KXmlParser;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public abstract class PluginFilterMatcher {   
   
   protected boolean matches(InputStream plugin, String filter) {
      MatchItem requires = extensionPointFromFilter(filter);
      LinkedList provides = environmentFromFilter(filter);
      Stack stack = new Stack();
      
      try {
         KXmlParser parser = new KXmlParser();
         parser.setInput(plugin, null);
         boolean matches = false;
         
         for (int type = parser.next(); (type != KXmlParser.END_DOCUMENT); type = parser
         .next()) {
            if (type == KXmlParser.START_TAG) {
               stack.push(parser.getName());

               if (stack.peek().equals("Extension")) {
                  String id = parser.getAttributeValue(null, "id");
                  debug("FOUND EXTENSION " + id);
                  if (requires.equals("Extension/" + "id:" + id)) {
                     matches = true;
                  }
               } else if (stack.peek().equals("Extension-Point")) {
                  String id = parser.getAttributeValue(null, "id");
                  debug("FOUND EXTENSION-POINT " + id);
                  if (!id.startsWith("Extension")) {                     
                     MatchItem elem = new MatchItem(id);
                     if (!provides.contains(elem)) {
                        debug("Extension-Point " + id + " unsatisfied");
                        return false;
                     }
                  }                  
               }               
            } else if (type == KXmlParser.END_TAG) {
               try {
                  stack.pop();
               } catch (Exception e) {
                  error("ERROR while parsing, Platform-File not well-formed");
               }
            }
         }
         return matches;
      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }
      
      
   }
   
   
   private MatchItem extensionPointFromFilter(String filter) {      
      String[] parts = filter.split("¦");
      return new MatchItem(parts[0]);
   }

   
   private LinkedList environmentFromFilter(String filter) {
      LinkedList result = new LinkedList();
      
      String[] parts = filter.split("¦");
      if (!parts[1].trim().equals("")) {
         parts = parts[1].split(";");
         for (int index=0; index < parts.length; index++) {
            result.add(new MatchItem(parts[index]));
         }
      }
      return result;
   }
   
   protected abstract void debug(String str);
   
   protected abstract void error(String str);
}
