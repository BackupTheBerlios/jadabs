/*
 * Created on 14-Feb-2005
 */
package ch.ethz.jadabs.bundleLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Stack;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;
import ch.ethz.jadabs.bundleLoader.api.Descriptor;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class BundleDescriptor extends Descriptor {
   private static Logger LOG = Logger.getLogger(BundleDescriptor.class);
   protected Vector dependencies = new Vector();
   private KXmlParser parser; 
   private String name;
   private String version;
   private String group;
   private String bundleLocation;
   private String bundleChecksum;
   protected boolean processed = false;
   protected int level = 0;
   
   private BundleDescriptor() {   
      super(null);
   }

   protected BundleDescriptor(String uuid) throws Exception {      
      super(uuid);

      parser = new KXmlParser();
      
      InputStream instream = BundleLoaderActivator.bundleLoader.fetchInformation(uuid, this);      
      parser.setInput(new InputStreamReader(instream));
      parseOBR();
      if (! uuid.equals(group + ":" + name + ":" + version + ":obr"))
         throw new Exception("OBR file corrupted. Could not create BundleDescriptor");
      
      if (LOG.isDebugEnabled()) 
         LOG.debug("Created new BundleDescriptor " + uuid);
   }
 
   private void parseOBR() throws XmlPullParserException, IOException
   {
       Stack stack = new Stack();
       Vector dependencies = new Vector();

       for (int type = parser.next(); (type != KXmlParser.END_DOCUMENT); type = parser.next())
       {
           if (type == KXmlParser.START_TAG)
           {
               stack.push(parser.getName());
           }
           if (type == KXmlParser.END_TAG)
           {
               try
               {
                   stack.pop();
               } catch (Exception e)
               {
                   System.err.println("ERROR while parsing, OBR-File not well-formed");
               }
           }
           if (type == KXmlParser.TEXT)
           {
               if (!parser.getText().trim().equals(""))
               {
                   processElement(stack);
               }
           }
       }
   }

   /**
    * 
    * @param stack
    * @throws XmlPullParserException
    * @throws IOException
    */
   private void processElement(Stack stack) throws XmlPullParserException, IOException
   {
       if (stack.peek().equals("bundle-name"))
       {
           name = parser.getText().trim();
       } else if (stack.peek().equals("bundle-group"))
       {
           group = parser.getText().trim();
       } else if (stack.peek().equals("bundle-version"))
       {
           version = parser.getText().trim();
       } else if (stack.peek().equals("bundle-updatelocation"))
       {
           bundleLocation = parser.getText().trim();
       } else if (stack.peek().equals("bundle-checksum"))
       {
           bundleChecksum = parser.getText().trim();
       } else if (stack.peek().equals("dependency-uuid"))
       {
           String uuid = parser.getText().trim() + "obr";
           try
           { 
              LOG.debug("Dependency:" + uuid);
              dependencies.add(uuid);
           } catch (Exception e)
           {
               LOG.error("malformed bundle uuid: " + uuid);
               e.printStackTrace();
           }
       }
   }
   
   protected String jar_uuid() {
      String uuid = toString();
      uuid = uuid.substring(0, uuid.lastIndexOf(":")) + ":jar";
      return uuid;
   }
   
   protected String jar_source() {
      return bundleLocation;
   }
   
   protected boolean checkBundle(String bundle) {
      // TODO: calculate checksum of the bundle content 
      // and compare with checksum from obr
      return true;
   }
   
   public boolean equals(Object obj) {
      if (obj instanceof String) {
         return ((String)obj).equalsIgnoreCase(this.toString());
      } else if (obj instanceof BundleDescriptor) {
         BundleDescriptor descr = (BundleDescriptor)obj;
         return (this.toString().equals(descr.toString()) && this.dependencies.equals(descr.dependencies) && this.bundleChecksum.equals(descr.bundleChecksum));
      } 
      return false;
   }
}
