/*
 * Created on 30.12.2004
 */
package ch.ethz.jadabs.serviceManager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class ServiceManager implements IServiceManager {
   private KXmlParser parser;
   private OSGiPlugin currentPlugin;
   private Vector registeredPlugins = new Vector();
   private Hashtable extensions = new Hashtable();
   private Platform platform;   
   
   public ServiceManager() {
      // get all *Plugin.xml files ...
      File dir = new File("./plugins");
      File[] files = dir.listFiles(new PluginFilter());

      System.out.println("File list: ");
      // and iterate over all files
      for (int i = 0; i < files.length; i++) {
         System.out.println(files[i].getAbsolutePath());

         FileReader reader;
         try {
            parser = new KXmlParser();

            reader = new FileReader(files[i]);
            parser.setInput(reader);
            parsePlugin();
            registerPlugin(currentPlugin);
         } catch (Exception e) {
            e.printStackTrace();
         } finally {
            parser = null;
            reader = null;
         }

         System.out.println(extensions);
      }
   }
   
   public void registerPlugin(File file) {      
      FileReader reader;
      try {
         reader = new FileReader(file);
         parser.setInput(reader);
         parsePlugin();
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         parser = null;
         reader = null;
      }
   }

   private void parsePlugin() throws XmlPullParserException, IOException {

      Stack stack = new Stack();

      for (int type = parser.next(); (type != KXmlParser.END_DOCUMENT); type = parser
            .next()) {
         if (type == KXmlParser.START_TAG) {
            stack.push(parser.getName());
            processAttributes(stack);
         } else if (type == KXmlParser.END_TAG) {
            try {
               stack.pop();
            } catch (Exception e) {
               System.err
                     .println("ERROR while parsing, Plugin-File not well-formed");
            }
         }
      }
   }

   private void processAttributes(Stack stack) {

      if (stack.peek().equals("OSGiPlugin")) {
         currentPlugin = new OSGiPlugin(parser.getAttributeValue(null, "id"), parser.getAttributeValue(null, "name"), parser.getAttributeValue(null, "version"), parser.getAttributeValue(null, "description"), parser.getAttributeValue(null, "provider"));
      } else if (stack.peek().equals("Extension")) {
         currentPlugin.addExtension(new Extension(parser.getAttributeValue(null, "id"), parser.getAttributeValue(null, "service")));
      } else if (stack.peek().equals("Extension-Point")) {
         currentPlugin.addExtensionPoint(new ExtensionPoint(parser.getAttributeValue(null, "id"), parser.getAttributeValue(null, "service"), parser.getAttributeValue(null, "description")));
      } else if (stack.peek().equals("PluginActivatorBundle")) {
         System.out.println(currentPlugin);
      } else if (stack.peek().equals("Configuration")) {
         // TODO: implement configuration
         
      }
   }
   
   private void registerPlugin(OSGiPlugin plugin) {
      registeredPlugins.add(plugin);
      for (Enumeration en = plugin.getExtensions(); en.hasMoreElements(); ) {
         Extension key = (Extension)en.nextElement();
         if (!extensions.containsKey(key.toString())) {            
            Vector value = new Vector();
            value.add(plugin);
            extensions.put(key.toString(), value);   
         } else {
            System.out.println("SECOND ENTRY :...........................");
            Vector values = (Vector)extensions.get(key.toString());
            values.add(plugin);
            extensions.remove(key.toString());
            extensions.put(key.toString(), values);
         }         
      }
   }

   public void getService() {
      
   }
   
}