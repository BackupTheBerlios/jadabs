/*
 * Created on 30.12.2004
 */
package ch.ethz.jadabs.pluginloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class PluginLoaderImpl extends Thread implements PluginLoader {

   private static Logger LOG = Logger.getLogger(PluginLoaderImpl.class);
   private KXmlParser parser;
   private Hashtable registeredPlugins = new Hashtable();    														  
   private Hashtable extensions = new Hashtable();
   private LinkedList pluginSchedule = new LinkedList();
   private Platform platform;

   /**
    * 
    * @see java.lang.Runnable#run()
    */
   public void run() {
      // load starter

      // get starter file
      String starter = PluginLoaderActivator.b_context
            .getProperty("ch.ethz.jadabs.pluginloader.starter");

      if (starter == null)
         starter = "init.starter";

      File file = new File("." + File.separatorChar + starter);

      parser = new KXmlParser();

      try {
         BufferedReader reader = new BufferedReader(new FileReader(file));
         String line;
         while ((line = reader.readLine()) != null) {
            if (line.startsWith("-usepad")) {
               if (LOG.isDebugEnabled())
                  LOG.debug("Opening " + "." + File.separatorChar
                     + line.substring(7).trim());
               File padfile = new File("." + File.separatorChar
                     + line.substring(7).trim());
               FileReader padreader = new FileReader(padfile);
               parser.setInput(padreader);

               parsePlatform();
               parser = null;
               padreader = null;
            } else if (line.startsWith("-startopd")) {
               String id = line.substring(9).trim();
               if (LOG.isDebugEnabled())
                  LOG.debug("Starting Plugin " + id);
               // TODO: Get OSGiBundle, resolve it and start it.

               String group = id.substring(0, id.indexOf(":"));
               id = id.substring(id.indexOf(":") + 1);
               String name = id.substring(0, id.indexOf(":"));
               id = id.substring(id.indexOf(":") + 1);
               String version = id.substring(0, id.indexOf(":"));
               String rest = id.substring(id.indexOf(":") + 1);

               String repopath = PluginLoaderActivator.b_context
                     .getProperty("org.knopflerfish.gosg.jars");

               String opdpath = repopath.substring(5) + group
                     + File.separatorChar + "opds" + File.separatorChar + name
                     + "-" + version + ".opd";
               if (LOG.isDebugEnabled())
                  LOG.debug("opdpath: " + opdpath);

               File opdfile = new File(opdpath);

               if (LOG.isDebugEnabled())
                  LOG.debug("opdfile: " + opdfile.getAbsolutePath());

               loadPlugin(opdfile);
            }
         }
      } catch (Exception err) {
         err.printStackTrace();
      }
   }

   private void loadScheduledPlugins() {
      OSGiPlugin plugin;
      for (Iterator it = pluginSchedule.iterator(); it.hasNext();) {
         plugin = (OSGiPlugin) it.next();
         if (LOG.isDebugEnabled())
            LOG.debug("LOADING BUNDLE " + plugin.getActivator());

         try {
            PluginLoaderActivator.bloader.load(plugin.getActivator().getName(),
                  plugin.getActivator().getGroup(), plugin.getActivator()
                        .getVersion());
         } catch (Exception err) {
            err.printStackTrace();
         }
      }
   }

   public void loadPlugin(File file) {
      if (LOG.isDebugEnabled())
      	LOG.debug("Loading Plugin " + file);
      
      OSGiPlugin plugin = (OSGiPlugin) OSGiPlugin.initAdvertisement(file);

      // close file to avoid problem with too many open files
      file = null;

      registerPlugin(plugin);
      try {
         resolvePlugin(plugin);
      } catch (Exception e) {
         LOG.error("could not resolvePlugin: ", e);
      }

      if (LOG.isDebugEnabled())
         LOG.debug("Schedule for " + pluginSchedule);

      loadScheduledPlugins();

   }

   public Enumeration getOSGiPlugins() {
      return registeredPlugins.elements();
   }

   private void parsePlatform() throws XmlPullParserException, IOException {
      Stack stack = new Stack();

      for (int type = parser.next(); (type != KXmlParser.END_DOCUMENT); type = parser
            .next()) {
         if (type == KXmlParser.START_TAG) {
            stack.push(parser.getName());
            processPlatformAttributes(stack);
         } else if (type == KXmlParser.END_TAG) {
            try {
               stack.pop();
            } catch (Exception e) {
               System.err
                     .println("ERROR while parsing, Platform-File not well-formed");
            }
         }
      }

   }

   private void processPlatformAttributes(Stack stack) {

      if (stack.peek().equals("Platform")) {
         platform = new Platform(parser.getAttributeValue(null, "id"), parser
               .getAttributeValue(null, "name"), parser.getAttributeValue(null,
               "version"), parser.getAttributeValue(null, "provider"));
      } else if (stack.peek().equals("Property")) {
         String name = parser.getAttributeValue(null, "name");
         String value = parser.getAttributeValue(null, "value");

         platform.setProperty(name, value);
         
         if (LOG.isDebugEnabled())
            LOG.debug("ADDED PROPERTY: " + name + "/" + value);
      } else if (stack.peek().equals("NetIface")) {
         NetIface iface = new NetIface(parser.getAttributeValue(null, "type"),
               parser.getAttributeValue(null, "connection"), parser
                     .getAttributeValue(null, "configuration"), parser
                     .getAttributeValue(null, "name"), parser
                     .getAttributeValue(null, "essid"), parser
                     .getAttributeValue(null, "mode"), parser
                     .getAttributeValue(null, "iface"), parser
                     .getAttributeValue(null, "ip"), parser.getAttributeValue(
                     null, "description"));

         platform.addNetIface(iface);

         extensions.put(iface.toString(), new Vector());

         if (LOG.isDebugEnabled())
            LOG.debug("ADDED EXTENSION: " + iface.toString());
      } else if (stack.peek().equals("Configuration")) {
         // TODO: implement configuration

      }
   }

   private void registerPlugin(OSGiPlugin plugin) {
      if (LOG.isDebugEnabled())
         LOG.debug("registering " + plugin.getName() + plugin);

      registeredPlugins.put(plugin.getName(), plugin);

      for (Enumeration en = plugin.getExtensions(); en.hasMoreElements();) {
         Extension key = (Extension) en.nextElement();
         if (!extensions.containsKey(key.toString())) {
            Vector value = new Vector();
            value.add(plugin);
            extensions.put(key.toString(), value);
         } else {
            Vector values = (Vector) extensions.get(key.toString());
            values.add(plugin);
            extensions.remove(key.toString());
            extensions.put(key.toString(), values);
         }
      }
   }

   private void resolvePlugin(OSGiPlugin plugin) throws Exception {
      for (Enumeration en = plugin.getExtensionPoints(); en.hasMoreElements();) {
         ExtensionPoint extp = (ExtensionPoint) en.nextElement();
         String ep = extp.toString();

         if (extp.getType().equals("platform"))
            continue;

         Vector matchingPlugins = (Vector) extensions.get(ep);

         if (matchingPlugins == null)
            throw new Exception("Plugin " + plugin.getName()
                  + " has unsatisfied ExtensionPoint " + ep);

         //TODO: Implement some kind of priority if more than one matching
         // plugin has been found
         
         for (Enumeration pl = matchingPlugins.elements(); pl.hasMoreElements();) {
            try {
               OSGiPlugin current = (OSGiPlugin) pl.nextElement();
               resolvePlugin(current);

            } catch (Exception e) {
               if (!pl.hasMoreElements())
                  throw e;
            }
         }
      }
      pluginSchedule.add(plugin);
   }

   //    private synchronized void parsePlugin() throws XmlPullParserException,
   // IOException
   //    {
   //        Stack stack = new Stack();
   //
   //        for (int type = parser.next(); (type != KXmlParser.END_DOCUMENT); type =
   // parser.next())
   //        {
   //            if (type == KXmlParser.START_TAG)
   //            {
   //                stack.push(parser.getName());
   //                processPluginAttributes(stack);
   //            } else if (type == KXmlParser.END_TAG)
   //            {
   //                try
   //                {
   //                    stack.pop();
   //                } catch (Exception e)
   //                {
   //                    System.err.println("ERROR while parsing, Plugin-File not well-formed");
   //                }
   //            }
   //        }
   //    }

   //    private void processPluginAttributes(Stack stack)
   //    {
   //
   //        if (stack.peek().equals("OSGiServicePlugin"))
   //        {
   //            currentPlugin = new OSGiPlugin(
   //                    parser.getAttributeValue(null, "id"),
   //                    parser.getAttributeValue(null, "name"),
   //                    parser.getAttributeValue(null, "group"),
   //                    parser.getAttributeValue(null, "version"),
   //                    parser.getAttributeValue(null, "description"),
   //                    parser.getAttributeValue(null, "provider"));
   //        } else if (stack.peek().equals("Extension"))
   //        {
   //            currentPlugin.addExtension(
   //                    new Extension(
   //                            parser.getAttributeValue(null, "id"),
   //                            parser.getAttributeValue(null, "service")));
   //        } else if (stack.peek().equals("Extension-Point"))
   //        {
   //            currentPlugin.addExtensionPoint(
   //                    new ExtensionPoint(
   //                            parser.getAttributeValue(null, "id"),
   //                            parser.getAttributeValue(null, "service"),
   //                            parser.getAttributeValue(null, "description")));
   //        } else if (stack.peek().equals("ServiceActivatorBundle"))
   //        {
   //            if (PluginLoaderActivator.LOG.isDebugEnabled())
   //                PluginLoaderActivator.LOG.debug(currentPlugin);
   //            	currentPlugin.setActivator(
   //            	        new ActivatorBundle(
   //            	                parser.getAttributeValue(null, "bundle-name"),
   //            	                parser.getAttributeValue(null, "bundle-group"),
   //            	                parser.getAttributeValue(null, "bundle-version")));
   //        } else if (stack.peek().equals("Configuration"))
   //        {
   //            // TODO: implement configuration
   //
   //        }
   //    }

   //    public OSGiPlugin parsePluginAdvertisement(String adv)
   //    {
   //        parser = new KXmlParser();
   //
   //        StringReader reader = new StringReader(adv);
   //        try
   //        {
   //            parser.setInput(reader);
   //            
   //
   //            parsePlugin();
   //            
   //        } catch (XmlPullParserException e)
   //        {
   //            LOG.error("error in parsing string: "+adv);
   //            return null;
   //        } catch (IOException e)
   //        {
   //            LOG.error("error in parsing string: "+adv);
   //            return null;
   //        }
   //        
   //        
   //        return currentPlugin;
   //    }

}