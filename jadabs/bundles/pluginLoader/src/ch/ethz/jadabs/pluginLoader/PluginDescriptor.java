/*
 * Created on 14-Feb-2005
 */
package ch.ethz.jadabs.pluginLoader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.kxml2.io.KXmlParser;

import ch.ethz.jadabs.bundleLoader.api.Descriptor;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class PluginDescriptor extends Descriptor {
   private static Logger LOG = Logger.getLogger(PluginDescriptor.class);
   private KXmlParser parser;
   protected Vector extensions = new Vector();
   private Hashtable extensionPoints = new Hashtable();

   private PluginDescriptor() {
      super(null);
   }

   /**
    * @param uuid
    */
   protected PluginDescriptor(String uuid) throws Exception {
      super(uuid);

      parser = new KXmlParser();

      InputStream instream = PluginLoaderImpl.fetchInformation(uuid, this);
      parser.setInput(new InputStreamReader(instream));
      parseOPD();

      if (LOG.isDebugEnabled())
         LOG.debug("Created new BundleDescriptor " + uuid);
   }

   private void parseOPD() throws Exception {
      Stack stack = new Stack();
      for (int type = parser.next(); (type != KXmlParser.END_DOCUMENT); type = parser
            .next()) {
         if (type == KXmlParser.START_TAG) {
            stack.push(parser.getName());

            if (stack.peek().equals("Extension")) {
               /*
               String id = parser.getAttributeValue(null, "id");
               LOG.debug("FOUND EXTENSION " + id);
               String extension = new String();
               if (!id.startsWith("Extension")) {
                  extension = extension.concat(parser.getName() + "/");
                  for (int index = 0; index < parser.getAttributeCount(); index++) {
                     if (index != 0)
                        extension = extension.concat(", ");
                     extension = extension.concat(parser.getAttributeName(index) + ":"
                           + parser.getAttributeValue(index));
                  }
               }
               System.out.println("EXTENSION: " + extension);                                                                                                                                                                     
               */
            } else if (stack.peek().equals("Extension-Point")) {
               String id = parser.getAttributeValue(null, "id");
               LOG.debug("FOUND EXTENSION-POINT " + id);
               
               if (id.startsWith("Extension")) {
               ArrayList matchingPlugins = new ArrayList();
               System.out.println("");
               System.out.println("REQUESTING " + id + " ¦ " + PluginLoaderImpl.platform + " ¦ " + "R");
               System.out.println("");
               for (Iterator matches = PluginLoaderImpl.getMatchingPlugins(id + " ¦ " + PluginLoaderImpl.platform + " ¦ " + "R"); matches.hasNext(); ) {
                  matchingPlugins.add(matches.next());
               }
               
               if (matchingPlugins.isEmpty()) throw new Exception("Unsatisfied extension point " + id + " in Plugin " + this.toString());
               if (matchingPlugins.size() == 1) {
                  PluginLoaderImpl.scheduler.addPlugin((String)matchingPlugins.get(0));
               } else {
                  PluginLoaderImpl.scheduler.addAlternativePlugins(matchingPlugins);
               }
              
               PluginLoaderImpl.scheduler.stillToProcess.addAll(matchingPlugins);
               }
               
            }
         } else if (type == KXmlParser.END_TAG) {
            try {
               stack.pop();
            } catch (Exception e) {
               LOG.error("ERROR while parsing, Platform-File not well-formed");
            }
         }
      }
   }

}