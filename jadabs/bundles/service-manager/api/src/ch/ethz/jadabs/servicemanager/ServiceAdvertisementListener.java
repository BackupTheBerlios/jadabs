/*
 * Created on Jan 31, 2005
 *
 */
package ch.ethz.jadabs.servicemanager;


/**
 * @author andfrei
 * 
 */
public interface ServiceAdvertisementListener
{
    
    void foundService(ServiceReference sref);
    
    void removedService(ServiceReference sref);
}
