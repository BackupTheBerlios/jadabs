/*
 * Created on Feb 8, 2005
 *
 */
package ch.ethz.jadabs.servicemanager;


/**
 * @author andfrei
 * 
 */
public interface ServiceListener
{

    void receivedService(ServiceReference sref);
}
