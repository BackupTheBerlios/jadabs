/*
 * Created on 01.01.2005
 */
package ch.ethz.jadabs.pluginloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

import ch.ethz.jadabs.bundleloader.ServiceAdvertisement;


/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class OSGiPlugin extends ServiceAdvertisement
{

    private static Logger LOG = Logger.getLogger(OSGiPlugin.class);
    
    private String provider;

    private ActivatorBundle activator;

    private Vector extensions = new Vector();

    private Vector extensionPoints = new Vector();

    private File opdfile;
    
    private KXmlParser parser;

    private OSGiPlugin currentPlugin;
    
    private OSGiPlugin()
    {
        
    }
    
    public OSGiPlugin(String name, String group, String version, String description, String provider)
    {
        this.name = name;
        this.group = group;
        this.version = version;
        this.description = description;
        this.provider = provider;
    }

    public static ServiceAdvertisement initAdvertisement(File file)
    {
        FileReader reader;
        try
        {
            reader = new FileReader(file);
            OSGiPlugin plugin =  initAdvertisement(reader);
            
            plugin.setAdvertisement(file);
            
            return plugin;
            
        } catch (FileNotFoundException e)
        {
            LOG.error("file not found");
        }
        
        return null;
    }

	public static ServiceAdvertisement initAdvertisement(String adv)
	{
        StringReader reader = new StringReader(adv);
        OSGiPlugin plugin =  initAdvertisement(reader);
        
        plugin.setAdvertisement(adv);
        
        return plugin;
	}
    
	private static OSGiPlugin initAdvertisement(Reader reader)
	{
	    OSGiPlugin newplugin = new OSGiPlugin();
	    
	    newplugin.parser = new KXmlParser();

        try
        {
            newplugin.parser.setInput(reader);
            
            newplugin.parsePlugin();
            
        } catch (XmlPullParserException e)
        {
            LOG.error("error in parsing string: ");
            return null;
        } catch (IOException e)
        {
            LOG.error("error in parsing string: ");
            return null;
        } finally
        {
            newplugin.parser = null;
            reader = null;
        } 
        
        
        return newplugin;
	}
	
    private void parsePlugin() throws XmlPullParserException, IOException
    {
        Stack stack = new Stack();

        for (int type = parser.next(); (type != KXmlParser.END_DOCUMENT); type = parser.next())
        {
            if (type == KXmlParser.START_TAG)
            {
                stack.push(parser.getName());
                processPluginAttributes(stack);
            } else if (type == KXmlParser.END_TAG)
            {
                try
                {
                    stack.pop();
                } catch (Exception e)
                {
                    System.err.println("ERROR while parsing, Plugin-File not well-formed");
                }
            }
        }
    }
	
    private void processPluginAttributes(Stack stack)
    {

        if (stack.peek().equals("OSGiServicePlugin"))
        {
            name = parser.getAttributeValue(null, "name");
            group = parser.getAttributeValue(null, "group");
            version = parser.getAttributeValue(null, "version");
            description = parser.getAttributeValue(null, "description");
            provider = parser.getAttributeValue(null, "provider");
        } else if (stack.peek().equals("Extension"))
        {
            addExtension(
                new Extension(
                        parser.getAttributeValue(null, "id"), 
                        parser.getAttributeValue(null, "service")));
        } else if (stack.peek().equals("Extension-Point"))
        {
            addExtensionPoint(
                new ExtensionPoint(
                        parser.getAttributeValue(null, "id"), 
                        parser.getAttributeValue(null, "service"), 
                        parser.getAttributeValue(null, "description")));
        } else if (stack.peek().equals("ServiceActivatorBundle"))
        {
            PluginLoaderActivator.LOG.debug(currentPlugin);
            setActivator(
    	        new ActivatorBundle(
    	                parser.getAttributeValue(null, "bundle-name"), 
    	                parser.getAttributeValue(null, "bundle-group"), 
    	                parser.getAttributeValue(null, "bundle-version")));
        } else if (stack.peek().equals("Configuration"))
        {
            // TODO: implement configuration

        }
    }
    
    protected void setActivator(ActivatorBundle activator)
    {
        this.activator = activator;
    }
    
    
    protected ActivatorBundle getActivator()
    {
        return activator;
    }

    protected void addExtension(Extension ext)
    {
        extensions.add(ext);
    }

    protected Enumeration getExtensions()
    {
        return extensions.elements();
    }

    protected void addExtensionPoint(ExtensionPoint extp)
    {
        extensionPoints.add(extp);
    }

    protected Enumeration getExtensionPoints()
    {
        return extensionPoints.elements();
    }
   
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append(id + ", ");
        buffer.append("Extensions: ");
        for (Enumeration en = extensions.elements(); en.hasMoreElements();)
        {
            buffer.append(en.nextElement().toString() + ", ");
        }
        buffer.append("ExtensionPoints: ");
        for (Enumeration en = extensionPoints.elements(); en.hasMoreElements();)
        {
            buffer.append(en.nextElement().toString() + ", ");
        }

        return buffer.toString();
    }

    //---------------------------------------------------
    // implements ServiceAdvertisement interface
    //---------------------------------------------------
    
    /*
     */
    public boolean matches(String filter)
    {
        // TODO Auto-generated method stub
        return true;
    }
    
    public String getID()
    {
        return group+ ":"+name + ":" + version+":"+"opd";
    }

}