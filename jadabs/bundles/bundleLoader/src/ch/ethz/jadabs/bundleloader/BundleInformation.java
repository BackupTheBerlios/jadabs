package ch.ethz.jadabs.bundleloader;

/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

public class BundleInformation extends ServiceAdvertisement
{

    private static Logger LOG = Logger.getLogger(BundleInformation.class);

    private String bundleId;

    private String bundleUpdateLocation;

    private String bundleSourceURL;

    private String bundleDocURL;

    private String bundleChecksum;

    protected Vector bundleDependencies = new Vector();

    protected String filename;

    private KXmlParser parser;

    /** location of the cached jar file */
    private String cachedFilePath;
    
    private BundleInformation()
    {

    }

    /**
     * 
     * @param bundle
     * @param group
     */
    public BundleInformation(String bundle, String group, String version)
    {
        parser = new KXmlParser();
        FileReader reader;

        try
        {
            if (BundleLoaderImpl.fetchPolicy == BundleLoaderImpl.Eager)
            {
                BundleLoaderImpl.loadBundle(bundle, group, version);
            }

            filename = BundleLoaderActivator.repository + File.separator + group + File.separator + "jars"
                    + File.separator + bundle + "-" + version + ".jar";
            
            File obrfile = new File(BundleLoaderActivator.repository + File.separator + group + File.separator + "obrs"
                    + File.separator + bundle + "-" + version + ".obr");
            
            reader = new FileReader(obrfile);
            parser.setInput(reader);
            parseOBR();
            
            setAdvertisement(obrfile);

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            parser = null;
            reader = null;
        }

    }
    
    public static ServiceAdvertisement initAdvertisement(File file)
    {
        FileReader reader;
        try
        {
            reader = new FileReader(file);
            BundleInformation binfo = initAdvertisement(reader);
            
            binfo.setAdvertisement(file);
            
            return binfo;
            
        } catch (FileNotFoundException e)
        {
            LOG.error("initializaing adv");
        }
        
        return null;
    }

    public static ServiceAdvertisement initAdvertisement(String adv)
    {
        StringReader reader = new StringReader(adv);
        BundleInformation binfo = initAdvertisement(reader);
        
        binfo.setAdvertisement(adv);
        
        return binfo;
    }
    
    private static BundleInformation initAdvertisement(Reader reader)
    {
        BundleInformation newbinfo = new BundleInformation();

        newbinfo.parser = new KXmlParser();

        try
        {
            newbinfo.parser.setInput(reader);
            newbinfo.parseOBR();

        } catch (XmlPullParserException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            LOG.error("ioexception: ",e);
        } finally
        {
            newbinfo.parser = null;
            reader = null;
        }

        return newbinfo;
    }

    public BundleInformation(String uuid) throws Exception
    {
        int firstSep = uuid.indexOf(":");
        if (firstSep < 0) { throw new Exception("illegal bundle uuid"); }
        int secSep = uuid.substring(firstSep + 1).indexOf(":");
        if (secSep < 0) { throw new Exception("illegal bundle uuid"); }
        secSep += firstSep + 1;
        int thirdSep = uuid.substring(secSep + 1).indexOf(":");
        if (thirdSep < 0) { throw new Exception("illegal bundle uuid"); }
        thirdSep += secSep + 1;

        String bundle = uuid.substring(firstSep + 1, secSep);
        String group = uuid.substring(0, firstSep);
        String version = uuid.substring(secSep + 1, thirdSep);

        parser = new KXmlParser();
        FileReader reader;

        try
        {
            if (BundleLoaderImpl.fetchPolicy == BundleLoaderImpl.Eager)
            {
                BundleLoaderImpl.loadBundle(bundle, group, version);
            }

            filename = BundleLoaderActivator.repository + File.separator + group + File.separator + "jars"
                    + File.separator + bundle + "-" + version + ".jar";
            reader = new FileReader(BundleLoaderActivator.repository + File.separator + group + File.separator + "obrs"
                    + File.separator + bundle + "-" + version + ".obr");
            parser.setInput(reader);
            parseOBR();

        } catch (Exception e)
        {
            e.printStackTrace();
        } finally
        {
            parser = null;
            reader = null;
        }

    }

    public byte[] getBundleCode() throws IOException
    {

        //TODO return code also from memory, hack suppose its in a file
        return read2list(new File(filename));

    }
    
    public void setBundleCacheLocation(String cachedFilePath)
    {
        this.cachedFilePath = cachedFilePath;
    }
    
    /**
     * 
     * @throws XmlPullParserException
     * @throws IOException
     */
    public void parseOBR() throws XmlPullParserException, IOException
    {
        Stack stack = new Stack();
        Vector dependencies = new Vector();

        for (int type = parser.next(); (type != KXmlParser.END_DOCUMENT); type = parser.next())
        {
            if (type == KXmlParser.START_TAG)
            {
                // if (parser.getName().equals("dependencies")) {
                // 	buildSchedule();
                // }
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
                    // DEBUG one line
                    //System.out.println("Scope:" + stack + " <" + stack.peek()
                    //		+ ">" + parser.getText().trim() + "</"
                    //		+ stack.peek() + ">");
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
        } else if (stack.peek().equals("bundle-description"))
        {
            description = parser.getText().trim();
        } else if (stack.peek().equals("bundle-version"))
        {
            version = parser.getText().trim();
        } else if (stack.peek().equals("bundle-updatelocation"))
        {
            this.bundleUpdateLocation = parser.getText().trim();
        } else if (stack.peek().equals("bundle-sourceurl"))
        {
            this.bundleUpdateLocation = parser.getText().trim();
        } else if (stack.peek().equals("bundle-docurl"))
        {
            this.bundleDocURL = parser.getText().trim();
        } else if (stack.peek().equals("bundle-checksum"))
        {
            this.bundleChecksum = parser.getText().trim();
        } else if (stack.peek().equals("dependency-uuid"))
        {
            String uuid = parser.getText().trim();
            try
            {
                BundleInformation dependency = new BundleInformation(uuid);
                bundleDependencies.add(dependency);
            } catch (Exception e)
            {
                LOG.error("malformed bundle uuid: " + uuid);
                e.printStackTrace();
            }
        }
    }

    /**
     * 
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void buildSchedule() throws XmlPullParserException, IOException
    {

        parser.require(KXmlParser.START_TAG, "", "dependencies");
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
                    // DEBUG one line
                    // System.out.println("bundle-name: " + bundlename);
                } else if (tagname.equals("bundle-version"))
                {
                    bundleversion = parser.getText();
                    // DEBUG one line
                    // System.out.println("bundle-version: " + bundleversion);
                } else if (tagname.equals("bundle-group"))
                {
                    bundlegroup = parser.getText();
                    // DEBUG one line
                    // System.out.println("bundle-group: " + bundlegroup);
                } else if (tagname.equals("dependency-uuid"))
                {
                    // TODO: do it
                }
                if (bundlename != null && bundleversion != null && bundlegroup != null)
                {
                    BundleInformation dependency = new BundleInformation(bundlename, bundlegroup, bundleversion);
                    bundleDependencies.add(dependency);
                }
                parser.next();
                parser.require(KXmlParser.END_TAG, "", tagname);
                parser.next();
            }

            parser.require(KXmlParser.END_TAG, "", "bundle");
            parser.next();
        }

        parser.require(KXmlParser.END_TAG, "", "dependencies");

        return;
    }

    /**
     * Reads a file storing intermediate data into a list. Fast method.
     * 
     * @param file
     *            the file to be read
     * @return a file data
     */
    public static byte[] read2list(File file) throws IOException
    {
        InputStream in = null;
        byte[] buf = null; // output buffer
        int bufLen = 20000 * 1024;
        try
        {
            in = new BufferedInputStream(new FileInputStream(file));
            buf = new byte[bufLen];
            byte[] tmp = null;
            int len = 0;
            List data = new ArrayList(24); // keeps peaces of data
            while ((len = in.read(buf, 0, bufLen)) != -1)
            {
                tmp = new byte[len];
                System.arraycopy(buf, 0, tmp, 0, len); // still need to do copy
                data.add(tmp);
            }
            /*
             * This part os optional. This method could return a List data for
             * further processing, etc.
             */
            len = 0;
            if (data.size() == 1)
                return (byte[]) data.get(0);
            for (int i = 0; i < data.size(); i++)
                len += ((byte[]) data.get(i)).length;
            buf = new byte[len]; // final output buffer
            len = 0;
            for (int i = 0; i < data.size(); i++)
            { // fill with data
                tmp = (byte[]) data.get(i);
                System.arraycopy(tmp, 0, buf, len, tmp.length);
                len += tmp.length;
            }
        } finally
        {
            if (in != null)
                try
                {
                    in.close();
                } catch (Exception e)
                {
                }
        }
        return buf;
    }
    
    /**
     *  
     */
    public String toString()
    {
        return name + "-" + version;
    }

    /**
     * 
     * @param obj
     */
    public boolean equals(Object obj)
    {
        if (obj instanceof BundleInformation) { return ((BundleInformation) obj).name.equals(name)
                && ((BundleInformation) obj).version.equals(version); }
        return false;
    }

    //---------------------------------------------------
    // implements ServiceAdvertisement interface
    //---------------------------------------------------

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.jadabs.bundleloader.ServiceAdvertisement#matches(java.lang.String)
     */
    public boolean matches(String filter)
    {
        // TODO Auto-generated method stub
        return true;
    }

    public String getID()
    {
        return group + ":" + name + ":" + version + ":" + "obr";
    }

}