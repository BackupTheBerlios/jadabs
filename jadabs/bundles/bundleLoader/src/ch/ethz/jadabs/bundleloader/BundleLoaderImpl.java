package ch.ethz.jadabs.bundleloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.kxml2.io.KXmlParser;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.xmlpull.v1.XmlPullParserException;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */

public class BundleLoaderImpl implements BundleLoader, BundleListener
{

    private static Logger LOG = Logger.getLogger(BundleLoaderImpl.class);

    private static HashSet queuedBundles = new HashSet();

    private static HashSet installedBundles = new HashSet();

    private static LinkedList installationQueue = new LinkedList();

    private static Hashtable binfos = new Hashtable(); // [(String(group-name-version),
                                                       // BundleInformation)]

    protected final static int Eager = 1;

    protected final static int Lazy = 2;

    protected static int fetchPolicy = Eager;

    private BundleStarter starter;

    private static HashSet bls = new HashSet();

    /**
     * 
     * @param sysBundles
     */
    public BundleLoaderImpl(Collection sysBundles)
    {
        starter = new BundleStarter();
        starter.start();
        installedBundles.addAll(sysBundles);
    }

    /**
     *  
     */
    protected void startup()
    {

        try
        {
            FileReader reader = new FileReader("startup.xml");
            KXmlParser parser = new KXmlParser();
            parser.setInput(reader);
            // parse startup.xml
            Vector tasks = parseStartup(parser);

            // resolve all tasks and install scheduled bundles
            for (Enumeration en = tasks.elements(); en.hasMoreElements();)
            {
                BundleInformation bundle = (BundleInformation) en.nextElement();
                scheduleDependencies(bundle);
                LOG.debug("<<Schedule for " + bundle + ": " + installationQueue + ">>");
                install();
            }

        } catch (Exception e)
        {
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
    public synchronized void load(String name, String group, String version) throws Exception
    {
        BundleInformation bundle = new BundleInformation(name, group, version);
        scheduleDependencies(bundle);
        LOG.debug("Schedule for " + bundle + ": " + installationQueue);
        install();
    }

    public void addListener(BundleLoaderListener bl)
    {
        bls.add(bl);
    }

    public void removeListener(BundleLoaderListener bl)
    {
        bls.remove(bl);
    }

    private void notifyBundleLoaderListener(BundleInformation binfo, int event)
    {
        for (Iterator it = bls.iterator(); it.hasNext();)
        {
            BundleLoaderListener bl = (BundleLoaderListener) it.next();

            bl.bundleChanged(binfo, event);
        }
    }

    /**
     * 
     * @throws BundleException
     * @throws FileNotFoundException
     */
    private synchronized void install() throws BundleException, FileNotFoundException
    {

        // in case we have lazy fetching, bundles must be downloaded now
        if (BundleLoaderImpl.fetchPolicy == BundleLoaderImpl.Lazy)
        {
            if (!loadBundles(installationQueue))
                throw new BundleException("BundleLoader failed getting dependencies");
        }

        // install and start all bundles
        for (Iterator bundles = installationQueue.iterator(); bundles.hasNext();)
        {
            BundleInformation binf = ((BundleInformation) bundles.next());

            String location = binf.filename;
            File file = new File(location);
            FileInputStream fin = new FileInputStream(file);
            Bundle bundle = BundleLoaderActivator.bc.installBundle(file.getName(), fin);
            System.out.println("installed " + location);

            binfos.put(binf.getID(), binf);

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
    protected static boolean loadBundle(String name, String group, String version)
    {

        // TODO: download obr

        if (BundleLoaderImpl.fetchPolicy == BundleLoaderImpl.Eager) { return fetchBundle(name, group, version); }
        return true;
    }

    protected static boolean loadBundles(LinkedList bundles) throws BundleException
    {
        for (Iterator it = bundles.iterator(); it.hasNext();)
        {
            BundleInformation bundle = (BundleInformation) it.next();
            if (!fetchBundle(bundle.getName(), bundle.getGroup(), bundle.getVersion())) { throw new BundleException(
                    "Could not locate " + bundle); }
        }
        return true;
    }

    private static boolean fetchBundle(String name, String group, String version)
    {
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
    private static void scheduleDependencies(BundleInformation initial) throws XmlPullParserException, IOException
    {

        queuedBundles.clear();
        installationQueue.add(initial);

        if (BundleLoaderActivator.LOG.isDebugEnabled())
            BundleLoaderActivator.LOG.debug("<<Installed Bundles: " + installedBundles + ">>");
        LOG.debug("<<Reqested Bundle: " + initial + ">>");

        boolean found;
        int index;

        do
        {
            found = false;

            // another iteration until all dependencies are processed
            for (index = 0; ((BundleInformation) installationQueue.get(index)).bundleDependencies.isEmpty()
                    && index < installationQueue.size() - 1; index++)
                ;

            if (BundleLoaderActivator.LOG.isDebugEnabled())
                BundleLoaderActivator.LOG.debug(installationQueue);

            // found one that has not been processed yet
            if (!((BundleInformation) installationQueue.get(index)).bundleDependencies.isEmpty())
            {
                found = true;
                Vector dependencies = ((BundleInformation) installationQueue.get(index)).bundleDependencies;
                if (BundleLoaderActivator.LOG.isDebugEnabled())
                    BundleLoaderActivator.LOG.debug(((BundleInformation) installationQueue.get(index)).toString()
                            + " HAS DEPENDENCIES: " + dependencies);

                // TODO: Check, if a bundle is already in the queue but in a
                // different version

                for (Enumeration deps = dependencies.elements(); deps.hasMoreElements();)
                {
                    BundleInformation depsBundle = (BundleInformation) deps.nextElement();

                    if (!installedBundles.contains(depsBundle.toString()))
                    {
                        // bundle is not a system bundle
                        if (!queuedBundles.contains(depsBundle.toString()))
                        {
                            // bundle is not yet in queue
                            // so add it prior to the bundle that had this
                            // dependency
                            if (index > 0)
                            {
                                installationQueue.add(index - 1, depsBundle);
                            } else
                            {
                                installationQueue.addFirst(depsBundle);
                            }
                            queuedBundles.add(depsBundle.toString());
                        } else
                        {
                            // bundle is already in queue
                            // check, if it is scheduled later than the bundle
                            // that had this dependency
                            int inQueue = installationQueue.indexOf(depsBundle);
                            // it is in queue
                            if (index < inQueue)
                            {
                                // remove existing entry from queue
                                installationQueue.remove(depsBundle);

                                // and add prior to the bundle that had this
                                // dependency
                                if (index > 0)
                                {
                                    installationQueue.add(index - 1, depsBundle);
                                } else
                                {
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
    private Vector parseStartup(KXmlParser parser) throws Exception
    {
        Vector startup = new Vector();

        parser.next();
        parser.require(KXmlParser.START_TAG, "", "bundleLoader");
        parser.next();

        while (parser.next() == KXmlParser.START_TAG)
        {
            parser.require(KXmlParser.START_TAG, "", "bundle");
            parser.next();

            String bundlename = null;
            String bundleversion = null;
            String bundleid = null;
            String bundlegroup = null;

            while (parser.next() == KXmlParser.START_TAG)
            {
                String tagname = parser.getName();
                parser.next();

                if (tagname.equals("bundle-name"))
                {
                    bundlename = parser.getText();
                    if (BundleLoaderActivator.LOG.isDebugEnabled())
                        BundleLoaderActivator.LOG.debug("bundle-name: " + bundlename);
                } else if (tagname.equals("bundle-version"))
                {
                    bundleversion = parser.getText();
                    if (BundleLoaderActivator.LOG.isDebugEnabled())
                        BundleLoaderActivator.LOG.debug("bundle-version: " + bundleversion);
                } else if (tagname.equals("bundle-group"))
                {
                    bundlegroup = parser.getText();
                    if (BundleLoaderActivator.LOG.isDebugEnabled())
                        BundleLoaderActivator.LOG.debug("bundle-group: " + bundlegroup);
                }

                if (bundlename != null && bundleversion != null && bundlegroup != null)
                {
                    BundleInformation dependency = new BundleInformation(bundlename, bundlegroup, bundleversion);
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
    public void bundleChanged(BundleEvent bevent)
    {

        if (bevent.getType() == Bundle.UNINSTALLED)
        {
            String loc = bevent.getBundle().getLocation();
            LOG.debug("bundle uninstalled: " + loc);

            // how do we map unknown bundle obrs to BundleInfo???

        }
        // Jan, ist der Vergleich mit 1, 16 richtig hier?
        else if (bevent.getType() == 1)
        {
            LOG.debug("bevent");
            String loc = bevent.getBundle().getLocation();
            int pos = loc.lastIndexOf(File.separatorChar);

            if (pos > -1)
            {
                loc = loc.substring(pos + 1);
            }
            pos = loc.indexOf(".jar");
            if (pos > -1)
            {
                loc = loc.substring(0, pos);
            }
            installedBundles.add(loc);
        } else if (bevent.getType() == 16)
        {
            String loc = bevent.getBundle().getLocation();
            int pos = loc.lastIndexOf(File.separatorChar);

            if (pos > -1)
            {
                loc = loc.substring(pos + 1);
            }
            pos = loc.indexOf(".jar");
            if (pos > -1)
            {
                loc = loc.substring(0, pos);
            }
            installedBundles.remove(loc);
        }
    }

//    protected Hashtable getInstalledBundles()
//    {
//        return binfos;
//    }

    public Enumeration getBundleAdvertisements()
    {
        return binfos.elements();
    }
    
    public BundleInformation getBundleInfo(String id)
    {
        return (BundleInformation)binfos.get(id);
    }
    
    public class BundleStarter extends Thread
    {

        private LinkedList bundles = new LinkedList();

        public boolean running = true;

        public void enqueue(Bundle bundle)
        {
            synchronized (bundles)
            {
                bundles.add(bundle);
                bundles.notifyAll();
                if (BundleLoaderActivator.LOG.isDebugEnabled())
                    BundleLoaderActivator.LOG.debug(bundles);
            }
        }

        public void run()
        {
            while (running)
            {
                if (!bundles.isEmpty())
                {
                    Bundle bundle;
                    synchronized (bundles)
                    {
                        bundle = (Bundle) bundles.removeFirst();
                    }
                    try
                    {
                        bundle.start();
                        if (BundleLoaderActivator.LOG.isDebugEnabled())
                            BundleLoaderActivator.LOG.debug(bundle + " started");
                    } catch (BundleException be)
                    {
                        LOG.debug("Error starting bundle " + bundle.getLocation());
                        be.printStackTrace();
                        if (be.getNestedException() == null)
                        {
                            LOG.debug("Exception: ");
                            be.printStackTrace();
                        } else
                        {
                            LOG.debug("Nested exception: ");
                            be.getNestedException().printStackTrace();
                        }
                    }
                }
                // sleep
                try
                {
                    synchronized (bundles)
                    {
                        bundles.wait();
                    }
                } catch (InterruptedException err)
                {
                    err.printStackTrace();
                }
            }
        }
    }
}