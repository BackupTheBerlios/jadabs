/*
 * Created on 30.12.2004
 */
package ch.ethz.jadabs.serviceManager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

         } catch (Exception e) {
            e.printStackTrace();
         } finally {
            parser = null;
            reader = null;
         }

      }
   }

   private void parsePlugin() throws XmlPullParserException, IOException {

      Stack stack = new Stack();
      Vector extensions = new Vector();
      Vector extensionPoints = new Vector();

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
                     .println("ERROR while parsing, OBR-File not well-formed");
            }
         } else if (type == KXmlParser.TEXT) {
            if (!parser.getText().trim().equals("")) {
               // DEBUG one line
               System.out.println("Scope:" + stack + " <" + stack.peek() + ">"
                     + parser.getText().trim() + "</" + stack.peek() + ">");
               // processElement(stack);
            }
         } else {
            // element with attributes

         }
      }
   }

   private void processAttributes(Stack stack) {

      if (stack.peek().equals("OSGiPlugin")) {
         OSGiPlugin pl = new OSGiPlugin(parser.getAttributeValue(null, "id"), parser.getAttributeValue(null, "name"), parser.getAttributeValue(null, "version"), parser.getAttributeValue(null, "description"), parser.getAttributeValue(null, "provider"));
      } else if (stack.peek().equals("Extension")) {

      } else if (stack.peek().equals("ExtensionPoint")) {

      } else if (stack.peek().equals("PluginActivatorBundle")) {

      }

   }

}