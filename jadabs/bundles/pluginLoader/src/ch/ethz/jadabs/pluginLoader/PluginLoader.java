/*
 * Created on 30.12.2004
 */
package ch.ethz.jadabs.pluginLoader;

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

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import ch.ethz.jadabs.pluginLoader.fileFilters.*;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class PluginLoader extends Thread implements IPluginLoader {
   private KXmlParser parser;
   private OSGiPlugin currentPlugin;
   private Hashtable registeredPlugins = new Hashtable();
   private Hashtable extensions = new Hashtable();
   private LinkedList pluginSchedule = new LinkedList();
   private Platform platform;

   public PluginLoader() {   
   }

   public void run() {
      // load starter
      File dir = new File("." + File.separatorChar + "plugins");
      File[] files = dir.listFiles(new StarterFilter());

      for (int i = 0; i < files.length; i++) {
         try {
            BufferedReader reader = new BufferedReader(new FileReader(files[i]));
            String line;
            while ((line = reader.readLine()) != null) {
               if (line.startsWith("-usepad")) {
                  System.out.println("Opening " + "." + File.separatorChar
                        + "plugins" + File.separatorChar
                        + line.substring(7).trim());
                  File padfile = new File("." + File.separatorChar + "plugins"
                        + File.separatorChar + line.substring(7).trim());
                  parser = new KXmlParser();

                  FileReader padreader = new FileReader(padfile);
                  parser.setInput(padreader);

                  parsePlatform();

                  padreader = null;
                  parser = null;
               }
            }
         } catch (Exception err) {
            err.printStackTrace();
         }
      }

      // get all .opd files ...
      files = dir.listFiles(new PluginFilter());

      // and iterate over all files
      for (int i = 0; i < files.length; i++) {

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
         if (PluginLoaderActivator.LOG.isDebugEnabled())
            PluginLoaderActivator.LOG.debug(extensions);
      }

      // TEST STARTS HERE
      OSGiPlugin pl = (OSGiPlugin) registeredPlugins
            .get("SMTPGateway::ch.ethz.jadabs.mservices.smtpgw");
      try {
         resolvePlugin(pl);
      } catch (Exception e) {
         e.printStackTrace();
      }

      System.out.println(pluginSchedule);

      loadScheduledPlugins();
      // TEST ENDS HERE
   }
   
   private void loadScheduledPlugins() {
      OSGiPlugin plugin;
      for (Iterator it = pluginSchedule.iterator(); it.hasNext();) {
         plugin = (OSGiPlugin) it.next();
         System.out.println("LOADING BUNDLE " + plugin.getActivator());
         try {
            PluginLoaderActivator.bloader.load(plugin.getActivator().getName(),
                  plugin.getActivator().getGroup(), plugin.getActivator()
                        .getVersion());
         } catch (Exception err) {
            err.printStackTrace();
         }
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
         platform.setProperty(parser.getAttributeValue(null, "name"), parser
               .getAttributeValue(null, "value"));
         System.out.println("ADDED PROPERTY "
               + parser.getAttributeValue(null, "name"));
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
         System.out.println("ADDED EXTENSION " + iface.toString());
      } else if (stack.peek().equals("Configuration")) {
         // TODO: implement configuration

      }
   }

   private void parsePlugin() throws XmlPullParserException, IOException {
      Stack stack = new Stack();

      for (int type = parser.next(); (type != KXmlParser.END_DOCUMENT); type = parser
            .next()) {
         if (type == KXmlParser.START_TAG) {
            stack.push(parser.getName());
            processPluginAttributes(stack);
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

   private void processPluginAttributes(Stack stack) {

      if (stack.peek().equals("OSGiServicePlugin")) {
         currentPlugin = new OSGiPlugin(parser.getAttributeValue(null, "id"),
               parser.getAttributeValue(null, "name"), parser
                     .getAttributeValue(null, "version"), parser
                     .getAttributeValue(null, "description"), parser
                     .getAttributeValue(null, "provider"));
      } else if (stack.peek().equals("Extension")) {
         currentPlugin.addExtension(new Extension(parser.getAttributeValue(
               null, "id"), parser.getAttributeValue(null, "service")));
      } else if (stack.peek().equals("Extension-Point")) {
         currentPlugin.addExtensionPoint(new ExtensionPoint(parser
               .getAttributeValue(null, "id"), parser.getAttributeValue(null,
               "service"), parser.getAttributeValue(null, "description")));
      } else if (stack.peek().equals("ServiceActivatorBundle")) {
         if (PluginLoaderActivator.LOG.isDebugEnabled())
            PluginLoaderActivator.LOG.debug(currentPlugin);
         currentPlugin.setActivator(new ActivatorBundle(parser
               .getAttributeValue(null, "bundle-name"), parser
               .getAttributeValue(null, "bundle-group"), parser
               .getAttributeValue(null, "bundle-version")));
      } else if (stack.peek().equals("Configuration")) {
         // TODO: implement configuration

      }
   }

   private void registerPlugin(OSGiPlugin plugin) {
      if (PluginLoaderActivator.LOG.isDebugEnabled())
         PluginLoaderActivator.LOG.debug("registering " + plugin.getName());
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
         String ep = en.nextElement().toString();
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

}