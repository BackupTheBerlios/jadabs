/*
 * Created on Feb 3, 2005
 *
 */
package ch.ethz.jadabs.servicemanager;

import ch.ethz.jadabs.bundleloader.BundleInformation;
import ch.ethz.jadabs.pluginloader.OSGiPlugin;


/**
 * @author andfrei
 * 
 */
public class ServiceReference
{

    public static int SERVICE_TYPE_OBR = 1;
    public static int SERVICE_TYPE_OPD = 2;
    
    String group;
    String name;
    String version;
    
    String advertisement;
    
    public ServiceReference(String group, String name, String version, int type)
    {
        this.group = group;
        this.name = name;
        this.version = version;
    }
    
    public void initAdvertisement(String adv)
    {
        this.advertisement = adv;
    }
    
    public BundleInformation getBundleInformation()
    {
        return null;
    }
    
    public OSGiPlugin getOSGiPlugin()
    {
        return null;
    }
    
    
    
}
