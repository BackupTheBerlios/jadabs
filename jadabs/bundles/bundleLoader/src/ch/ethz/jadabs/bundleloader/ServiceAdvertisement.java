/*
 * Created on Feb 5, 2005
 *
 */
package ch.ethz.jadabs.bundleloader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;


/**
 * @author andfrei
 * 
 */
public abstract class ServiceAdvertisement
{
    private static Logger LOG = Logger.getLogger(ServiceAdvertisement.class);
    
    protected String id;

    protected String name;

    protected String group;
    
    protected String version;

    protected String description;
    
    protected String adv;
    
    protected File svcFile;
    
    public ServiceAdvertisement()
    {
        
    }
    
    public ServiceAdvertisement(String name, String group, String version, String description)
    {
        this.name = name;
        this.group = group;
        this.version = version;
        this.description = description;
    }
        
    public String getAdvertisement()
    {
        return adv;
    }

    protected void setAdvertisement(String adv)
    {
        this.adv = adv;
    }
    
    protected void setAdvertisement(File file)
    {
        this.svcFile = file;
        
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
            
            adv = sb.toString();
            
            br.close();
            
        } catch (FileNotFoundException e)
        {
            LOG.error("could not find file");
        } catch (IOException e)
        {
            LOG.error("error reading file");
        }

    }
    
	/**
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * 
	 * @return
	 */
	public String getVersion() {
		return version;
	}
    
	public static ServiceAdvertisement initAdvertisement(String adv)
	{
	    return null;
	}
	
	public static String getGroup(String id)
	{
        String group = id.substring(0,id.indexOf(":"));
        
        return group;
	}
	
	public static String getFileName(String id)
	{
        String group = id.substring(0,id.indexOf(":"));
        id = id.substring(id.indexOf(":")+1);
        String name = id.substring(0,id.indexOf(":"));
        id = id.substring(id.indexOf(":")+1);
        String version = id.substring(0,id.indexOf(":"));
        String rest = id.substring(id.indexOf(":")+1);

        return name + "-" + version + "." + rest;
	}
	
    /**
     * Implement this with an addition fourth element in the id,
     * for example: group:name:version:obr
     * 
     * @return
     */
    public abstract String getID();
    
    /**
     * Implement a matching function for a filter. Define your
     * own filtering here
     * 
     * @param filter
     * @return
     */
    public abstract boolean matches(String filter);
    
    
}
