/*
 * Created on Jan 31, 2005
 *
 */
package ch.ethz.jadabs.servicemanager;

import ch.ethz.jadabs.jxme.Pipe;


/**
 * @author andfrei
 * 
 */
public interface ServiceManager
{
    
    public static String PROVIDING_SERVICE = "provide";
    public static String RUNNING_SERVICE = "running";

    /**
     * Returns installed services as an array of strings.
     * The strings usually contain .obr, opd strings
     * 
     * Set which type of service you are interested in. Running gives
     * the services which are running on the other node, Providing gives
     * the services which are available through the repository.
     * 
     * @param pipe
     * @param filter
     * @param serviceListener
     * @param type kind of PROVIDING_SERVICE, RUNNING_SERVICE
     * @return
     */
    boolean getServices(ServiceListener serviceListener, String type);
    
    /**
     * Add providing service which can be found by other ServiceManagers.
     * Service needs not to be installed.
     * 
     * @param pipe
     * @param sref
     */
    void addProvidingService(ServiceReference sref);
    
    /** 
     * Remove providing service added with addProvidingService.
     * 
     * @param pipe
     * @param sref
     */
    void removeProvidingService(ServiceReference sref);
    
    /**
     * Returns bytearray of a specified service. The service is
     * specified as an uuid.
     * 
     * @param pipe
     * @param peer
     * @param uuid
     * @return
     */
    boolean getService(String fromPeer, ServiceReference sref);
    
    /**
     * Install and Start service on a remote Peer.
     * 
     * @param pipe
     * @param toPeer
     * @param sref ServiceReference should contain at least the uuid and path to the jar file to install
     * @return
     */
    boolean istartService(String toPeer, ServiceReference sref);
    
    /**
     * Returns a RemoteService with the given serviceName. Return null if
     * the service can not be instantiated or found on the remote peer.
     * 
     * @param pipe
     * @param fromPeer
     * @param serviceName
     * @return
     */
//    Object getRemoteService(Pipe pipe, String fromPeer, String serviceName);
    
    /**
     * Register a RemoteService which can be queried by getRemoteService.
     * Use the same serviceName as in getRemoteService.
     * 
     * @param serviceName
     * @param handler
     */
//    void registerRemoteService(String serviceName, RemoteServiceHandler handler);
    
}
