package ch.ethz.jadabs.bundleLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParserException;
import org.apache.log4j.Logger;
import org.kxml2.io.KXmlParser;
import org.osgi.framework.*;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */

public class BundleLoader implements IBundleLoader, BundleListener {
   private static Logger LOG = Logger.getLogger(BundleLoader.class);

   private static HashSet queuedBundles = new HashSet();
   private static HashSet installedBundles = new HashSet();
   private static LinkedList installationQueue = new LinkedList();

   protected final static int Eager = 1;
   protected final static int Lazy = 2;

   protected static int fetchPolicy = Eager;

   private BundleStarter starter;

   /**
    * 
    * @param sysBundles
    */
   public BundleLoader(Collection sysBundles) {
      starter = new BundleStarter();
      starter.start();
      installedBundles.addAll(sysBundles);
   }

   /**
    *  
    */
   protected void startup() {

      try {
         FileReader reader = new FileReader("startup.xml");
         KXmlParser parser = new KXmlParser();
         parser.setInput(reader);
         // parse startup.xml
         Vector tasks = parseStartup(parser);

         // resolve all tasks and install scheduled bundles
         for (Enumeration en = tasks.elements(); en.hasMoreElements();) {
            BundleInformation bundle = (BundleInformation) en.nextElement();
            scheduleDependencies(bundle);
            System.out.println("<<Schedule for " + bundle + ": "
                  + installationQueue + ">>");
            install();
         }

      } catch (Exception e) {
         //         e.printStackTrace();
         LOG.error("could not load or parse the startup.xml", e);
      }
   }

   /**
    * 
    * @param name
    * @param group
    * @param version
    * @throws Exception
    */
   public synchronized void load(String name, String group, String version) throws Exception {
      BundleInformation bundle = new BundleInformation(name, group, version);
      scheduleDependencies(bundle);
      System.out.println();
      System.out.println("Schedule for " + bundle + ": " + installationQueue);
      install();
   }

   /**
    * 
    * @throws BundleException
    * @throws FileNotFoundException
    */
   private synchronized void install() throws BundleException, FileNotFoundException {

      // in case we have lazy fetching, bundles must be downloaded now
      if (BundleLoader.fetchPolicy == BundleLoader.Lazy) {
         if (!loadBundles(installationQueue))
            throw new BundleException(
                  "BundleLoader failed getting dependencies");
      }

      // install and start all bundles
      for (Iterator bundles = installationQueue.iterator(); bundles.hasNext();) {
         BundleInformation binf = ((BundleInformation) bundles.next());
         String location = binf.filename;
         File file = new File(location);
         FileInputStream fin = new FileInputStream(file);
         Bundle bundle = BundleLoaderActivator.bc.installBundle(file.getName(),
               fin);
         System.out.println("installed " + location);

         // enqueue bundle for threaded starting
         starter.enqueue(bundle);
      }

      // clear queue
      installationQueue.clear();
   }

   /**
    * 
    * @param bundleName
    * @param group
    * @param version
    */
   protected static boolean loadBundle(String name, String group, String version) {

      // TODO: download obr

      if (BundleLoader.fetchPolicy == BundleLoader.Eager) {
         return fetchBundle(name, group, version);
      }
      return true;
   }

   protected static boolean loadBundles(LinkedList bundles)
         throws BundleException {
      for (Iterator it = bundles.iterator(); it.hasNext();) {
         BundleInformation bundle = (BundleInformation) it.next();
         if (!fetchBundle(bundle.getName(), bundle.getGroup(), bundle
               .getVersion())) {
            throw new BundleException("Could not locate " + bundle);
         }
      }
      return true;
   }

   private static boolean fetchBundle(String name, String group, String version) {
      // TODO: download bundle
      // FIXME: Maybe return false here and crosscut this method from
      // remoteLoader ?

      return true;
   }

   /**
    * 
    * @param initial
    * @throws XmlPullParserException
    * @throws IOException
    */
   private static void scheduleDependencies(BundleInformation initial)
         throws XmlPullParserException, IOException {

      queuedBundles.clear();
      installationQueue.add(initial);

      System.out.println();
      if (BundleLoaderActivator.LOG.isDebugEnabled())
         BundleLoaderActivator.LOG.debug("<<Installed Bundles: "
               + installedBundles + ">>");
      System.out.println("<<Reqested Bundle: " + initial + ">>");

      boolean found;
      int index;

      do {
         found = false;

         // another iteration until all dependencies are processed
         for (index = 0; ((BundleInformation) installationQueue.get(index)).bundleDependencies
               .isEmpty()
               && index < installationQueue.size() - 1; index++)
            ;

         if (BundleLoaderActivator.LOG.isDebugEnabled())
            BundleLoaderActivator.LOG.debug(installationQueue);

         // found one that has not been processed yet
         if (!((BundleInformation) installationQueue.get(index)).bundleDependencies
               .isEmpty()) {
            found = true;
            Vector dependencies = ((BundleInformation) installationQueue
                  .get(index)).bundleDependencies;
            if (BundleLoaderActivator.LOG.isDebugEnabled())
               BundleLoaderActivator.LOG
                     .debug(((BundleInformation) installationQueue.get(index))
                           .toString()
                           + " HAS DEPENDENCIES: " + dependencies);

            // TODO: Check, if a bundle is already in the queue but in a
            // different version

            for (Enumeration deps = dependencies.elements(); deps
                  .hasMoreElements();) {
               BundleInformation depsBundle = (BundleInformation) deps
                     .nextElement();

               if (!installedBundles.contains(depsBundle.toString())) {
                  // bundle is not a system bundle
                  if (!queuedBundles.contains(depsBundle.toString())) {
                     // bundle is not yet in queue
                     // so add it prior to the bundle that had this dependency
                     if (index > 0) {
                        installationQueue.add(index - 1, depsBundle);
                     } else {
                        installationQueue.addFirst(depsBundle);
                     }
                     queuedBundles.add(depsBundle.toString());
                  } else {
                     // bundle is already in queue
                     // check, if it is scheduled later than the bundle
                     // that had this dependency
                     int inQueue = installationQueue.indexOf(depsBundle);
                     // it is in queue
                     if (index < inQueue) {
                        // remove existing entry from queue
                        installationQueue.remove(depsBundle);

                        // and add prior to the bundle that had this dependency
                        if (index > 0) {
                           installationQueue.add(index - 1, depsBundle);
                        } else {
                           installationQueue.addFirst(depsBundle);
                        }
                     }

                  }
               }
            }

            dependencies.removeAllElements();
            queuedBundles.add(initial);
         }

      } while (found);
   }

   /**
    * 
    * @param parser
    * @return
    * @throws Exception
    */
   private Vector parseStartup(KXmlParser parser) throws Exception {
      Vector startup = new Vector();

      parser.next();
      parser.require(KXmlParser.START_TAG, "", "bundleLoader");
      parser.next();

      while (parser.next() == KXmlParser.START_TAG) {
         parser.require(KXmlParser.START_TAG, "", "bundle");
         parser.next();

         String bundlename = null;
         String bundleversion = null;
         String bundleid = null;
         String bundlegroup = null;

         while (parser.next() == KXmlParser.START_TAG) {
            String tagname = parser.getName();
            parser.next();

            if (tagname.equals("bundle-name")) {
               bundlename = parser.getText();
               if (BundleLoaderActivator.LOG.isDebugEnabled())
                  BundleLoaderActivator.LOG.debug("bundle-name: " + bundlename);
            } else if (tagname.equals("bundle-version")) {
               bundleversion = parser.getText();
               if (BundleLoaderActivator.LOG.isDebugEnabled())
                  BundleLoaderActivator.LOG.debug("bundle-version: "
                        + bundleversion);
            } else if (tagname.equals("bundle-group")) {
               bundlegroup = parser.getText();
               if (BundleLoaderActivator.LOG.isDebugEnabled())
                  BundleLoaderActivator.LOG.debug("bundle-group: "
                        + bundlegroup);
            }

            if (bundlename != null && bundleversion != null
                  && bundlegroup != null) {
               BundleInformation dependency = new BundleInformation(bundlename,
                     bundlegroup, bundleversion);
               startup.add(dependency);
            }

            parser.next();
            parser.require(KXmlParser.END_TAG, "", tagname);
            parser.next();
         }

         parser.require(KXmlParser.END_TAG, "", "bundle");
         parser.next();
      }

      parser.require(KXmlParser.END_TAG, "", "bundleLoader");

      return startup;
   }

   /**
    * @see org.osgi.framework.BundleListener#bundleChanged(org.osgi.framework.BundleEvent)
    */
   public void bundleChanged(BundleEvent bevent) {
      if (bevent.getType() == 1) {
         String loc = bevent.getBundle().getLocation();
         int pos = loc.lastIndexOf(File.separatorChar);

         if (pos > -1) {
            loc = loc.substring(pos + 1);
         }
         pos = loc.indexOf(".jar");
         if (pos > -1) {
            loc = loc.substring(0, pos);
         }
         installedBundles.add(loc);
      } else if (bevent.getType() == 16) {
         String loc = bevent.getBundle().getLocation();
         int pos = loc.lastIndexOf(File.separatorChar);

         if (pos > -1) {
            loc = loc.substring(pos + 1);
         }
         pos = loc.indexOf(".jar");
         if (pos > -1) {
            loc = loc.substring(0, pos);
         }
         installedBundles.remove(loc);
      }
   }

   protected Set getInstalledBundles() {
      return installedBundles;
   }

   
   public class BundleStarter extends Thread {
      private LinkedList bundles = new LinkedList();
      public boolean running = true;

      public void enqueue(Bundle bundle) {
         synchronized (bundles) {
            bundles.add(bundle);
            bundles.notifyAll();
            if (BundleLoaderActivator.LOG.isDebugEnabled())
               BundleLoaderActivator.LOG.debug(bundles);
         }
      }

      public void run() {
         while(running) {
         if (!bundles.isEmpty()) {
            Bundle bundle;
            synchronized (bundles) {
               bundle = (Bundle) bundles.removeFirst();
            }
            try {
               bundle.start();
               if (BundleLoaderActivator.LOG.isDebugEnabled())
                  BundleLoaderActivator.LOG.debug(bundle + " started");
            } catch (BundleException be) {
               System.out.println("Error starting bundle "
                     + bundle.getLocation());
               if (be.getNestedException() == null) {
                  System.out.println("Exception: ");
                  be.printStackTrace();
               } else {
                  System.out.println("Nested exception: ");
                  be.getNestedException().printStackTrace();
               }
            }
         }
         // sleep 
         try {
            synchronized(bundles) {
               bundles.wait();
            }
         } catch (InterruptedException err) {
            err.printStackTrace();
         }
         }
      }
   }
}