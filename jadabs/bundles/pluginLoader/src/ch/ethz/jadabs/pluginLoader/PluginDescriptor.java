/*
 * Created on 14-Feb-2005
 */
package ch.ethz.jadabs.pluginLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import ch.ethz.jadabs.bundleLoader.api.Descriptor;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class PluginDescriptor extends Descriptor {
   private static Logger LOG = Logger.getLogger(PluginDescriptor.class);
   private KXmlParser parser;

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

   private void parseOPD() throws XmlPullParserException, IOException {
      Stack stack = new Stack();
      for (int type = parser.next(); (type != KXmlParser.END_DOCUMENT); type = parser
            .next()) {
         if (type == KXmlParser.START_TAG) {
            stack.push(parser.getName());

            if (stack.peek().equals("Extension")) {
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

               
            } else if (stack.peek().equals("Extension-Point")) {
               String id = parser.getAttributeValue(null, "id");
               LOG.debug("FOUND EXTENSION-POINT " + id);
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