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
    
    public static String PROVIDING_SERVICES = "P";
    public static String RUNNING_SERVICES 	= "R";
    public static String ALL_SERVICES 		= "A";

    /**
     * Sends out a request to gather the services, OBRs and OPDs 
     * which are installed or providing by any peer in the same
     * group manager pipe.
     * This call is asynchronous, it returns true if the request
     * could be sent out.
     * 
     * Filter is defined as an EBNF, ServiceAdvFilter is defined in
     * PluginLoader:
     * (ServiceAdvFilter ",")* "|" ("OPD" ",")? ("OBR" ",")? ["R"|"P"|"A"]
     * 
     * In case the filter is null the default is: "|OPD,OBR,A"
     * 
     * @param filter
     * @param serviceListener
     * @return
     */
    boolean getServices(String filter, ServiceListener serviceListener);
    
    /**
     * Remove Service Listener, which has been added to the internal
     * list by getServices.
     * 
     * @param serviceListener
     */
    public void removeListener(ServiceListener serviceListener);
    
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
    
}
