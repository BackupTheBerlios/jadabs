/*
 * Created on 17-Feb-2005
 */
package ch.ethz.jadabs.pluginLoader;

import java.io.File;
import java.io.FileInputStream;
import org.apache.log4j.Logger;
import org.kxml2.io.KXmlParser;



/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class PlatformInformation {
   private static Logger LOG = Logger.getLogger(PlatformInformation.class);

   private PlatformInformation() {
   }

   public static String parsePAD(String location) {
      try {
         File file = new File(location);
         FileInputStream fis = new FileInputStream(file);
         KXmlParser parser = new KXmlParser();
         parser.setInput(fis, null);
         StringBuffer buffer = new StringBuffer();

         for (int type = parser.next(); (type != KXmlParser.END_DOCUMENT); type = parser
               .next()) {
            buffer.append("; ");
            if (type == KXmlParser.START_TAG) {
               buffer.append(parser.getName() + "/");
               for (int index = 0; index < parser.getAttributeCount(); index++) {
                  if (index != 0)
                     buffer.append(", ");
                  buffer.append(parser.getAttributeName(index) + ":"
                        + parser.getAttributeValue(index));
               }
            }
         }
         return buffer.substring(2);
      } catch (Exception e) {
         LOG.error(e.getMessage());
         return new String("");
      }
   }
}