/*
 * Created on Feb 3, 2005
 *
 */
package ch.ethz.jadabs.servicemanager;

//import ch.ethz.jadabs.bundleloader.BundleInformation;
//import ch.ethz.jadabs.pluginloader.OSGiPlugin;


/**
 * @author andfrei
 * 
 */
public interface ServiceReference
{

    static int SERVICE_TYPE_OBR = 1;
    static int SERVICE_TYPE_OPD = 2;
    
    String getName();
    
    String getVersion();
    
    String getGroup();
    
    String getPeer();
    
    String getID();
    
}
