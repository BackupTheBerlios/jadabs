/*
 * Created on Jan 31, 2005
 *
 */
package ch.ethz.jadabs.servicemanager;


/**
 * @author andfrei
 * 
 */
public interface RemoteServiceHandler
{
    Object getRemoteService(String peer);
}
