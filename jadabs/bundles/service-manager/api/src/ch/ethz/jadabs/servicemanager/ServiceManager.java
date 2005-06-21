/*
 * Created on Jan 31, 2005
 *
 */
package ch.ethz.jadabs.servicemanager;



/**
 * @author andfrei
 * 
 */
public interface ServiceManager
{
    
    public static String PROVIDING_SERVICES = "PRO";
    public static String INSTALLED_SERVICES = "INS";

    public static String SERVICE_TYPE = "type";
    
    /** Service Types */
//    public static String SERVICE_REQ = "svcreq";
//    public static String SERVICE_ACK = "svcack";
    
    public static String FILTER_REQ = "filreq";
    public static String FILTER_ACK = "filack";
    
    public static String NEWFILTER_REQ = "nfilreq";
    public static String NEWFILTER_ACK = "nfilack";
    
    public static String OBR_REQ = "obrreq";
    public static String OBR_ACK = "obrack";
    
    /** Jar Service Types */
    public static String JAR_REQ = "jarreq";
    public static String JAR_ACK = "jarack";
    
    /** Service Information */
    public static String SERVICE_ADV = "svcadv";
    
    public static String ADV_DESCRIPTOR = "advdesc";
    public static String SERVICE_ID = "svcid";
    
    public static String SERVICE_FILTER = "svcfil";
    
    public static String SERVICE_CODE = "svccode";
    
//    public static String SERVICE_PEER = "svcpeer";
    
//    public static String UUID = "uuid";
    
    /** Service Running or Providing type */
    public static String SERVICE_RP_TYPE = "rptype";
    
    /** Service holder */
    public static String SERVICE_TO_PEER = "svctopeer";
    public static String SERVICE_FROM_PEER = "svcfrompeer";
    public static String ANYPEER = "any";
    
    
    public static String DOWNLOAD_URL = "durl";
    
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
     * @param peername
     * @param filter
     * @param serviceListener
     * @return
     */
    boolean getServiceAdvertisements(String peername, String filter, ServiceAdvertisementListener listener);
    
    /**
     * Add providing service which can be found by other ServiceManagers.
     * Service needs not to be installed.
     * 
     * @param pipe
     * @param uuid
     */
    void addProvidingService(String uuid);
    
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
