/*
 * Created on 01.01.2005
 */
package ch.ethz.jadabs.pluginloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;


/**
 * 
 * @author Jan S. Rellermeyer, jrellermeyer_at_student.ethz.ch
 */
public class OSGiPlugin
{

    private static Logger LOG = Logger.getLogger(OSGiPlugin.class);
    
    private String id;

    private String name;

    private String group;
    
    private String version;

    private String description;

    private String provider;

    private ActivatorBundle activator;

    private Vector extensions = new Vector();

    private Vector extensionPoints = new Vector();

    private File opdfile;
    
    private String opdadv;
    
    public OSGiPlugin(String id, String name, String group, String version, String description, String provider)
    {
        this.id = id;
        this.name = name;
        this.group = group;
        this.version = version;
        this.description = description;
        this.provider = provider;
    }


    public void initAdvertisement(String str)
    {
        
    }
    
    protected void setActivator(ActivatorBundle activator)
    {
        this.activator = activator;
    }
    
    protected void setAdvertisement(File file)
    {
        this.opdfile = file;
        
        BufferedReader br;
        try
        {
            br = new BufferedReader(
                    new InputStreamReader(
                            new FileInputStream(file)));
            
            String line = "";
            
            StringBuffer sb = new StringBuffer();
            
            while((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }
            
            opdadv = sb.toString();
            
        } catch (FileNotFoundException e)
        {
            LOG.error("could not find file");
        } catch (IOException e)
        {
            LOG.error("error reading file");
        }

    }
    
    public String getName()
    {
        return name;
    }
    
    public String getGroup()
    {
        return group;
    }
    
    public String getVersion()
    {
        return version;
    }
    
    public String getAdvertisement()
    {
        return opdadv;
    }

    public void setAdvertisement(String adv)
    {
        opdadv = adv;
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

    public String getID()
    {
        return group+ ":"+name + ":" + version+":";
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

}