/*
 * Created on Feb 17, 2005
 *
 */
package ch.ethz.jadabs.servicemanager;


/**
 * @author andfrei
 * 
 */
public interface ServiceReference
{

    String getID();
    
    String getName();
    
    String getGroup();
    
    String getVersion();
    
    String getPeer();
    
    String getDownloadURL();
    
    String getProperty(String name);
}
