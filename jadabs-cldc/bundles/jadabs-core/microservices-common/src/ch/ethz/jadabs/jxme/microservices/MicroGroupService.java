/************************************************************************
 *
 * $Id: MicroGroupService.java,v 1.1 2005/01/16 14:06:37 printcap Exp $
 * 
 **********************************************************************/

package ch.ethz.jadabs.jxme.microservices;
import java.io.IOException;

/**
 * MicroGroupService - provides Group Management for J2ME/MIDP. MicroGroupService 
 * is a striped down variant of GroupService from jxta-osgi.
 * 
 * @author Ren&eacute; M&uller 
 * @see ch.ethz.jadabs.jxme.services.GroupService
 */
public interface MicroGroupService extends MicroListener
{
    /** Group resource. */
    public static final String GROUP = "02";

    /** Peer resource. */
    public static final String PEER = "03";

    /** Pipe resource.  */
    public static final String PIPE = "04";

    /** Undefined resource. */
    public static final String OTHER = "99";    
    
    /**
     * Publishing a resource in the network using the resolver service into the 
     * local cache.
     * 
     * @param resourceType. 
     * 				One of {@link MicroGroupService.PEER},
     *            {@link MicroGroupService.GROUP}or {@link MicroGroupService.PIPE}or
     *            {@link MicroGroupService.OTHER}
     * @param resourceName 
     * 				the name of the entity being created 
     * @param stringID
     *            JXTA ID string for the resource
     */
    public void publish(String resourceType, String resourceName, String stringID);

    /**
     * Publishing a resource in the network using the resolver service by sending
     * an advertisement message.
     * 
     * @param resourceType. 
     * 				One of {@link MicroGroupService.PEER},
     *            {@link MicroGroupService.GROUP}or {@link MicroGroupService.PIPE}or
     *            {@link MicroGroupService.OTHER}
     * @param resourceName 
     * 				the name of the entity being created 
     * @param stringID
     *            JXTA ID string for the resource
     */
    public void remotePublish(String resourceType, String resourceName, String stringID);
    
    /**
     * Search for Peers, Groups, Pipes or Content resources defined by
     * Applications.
     * <p>
     * 
     * First, it searches in the local cache. If a match is found, NamedResource
     * is returned as the matching value. If a match is not found in the local
     * cache, query is propagated to peer's neighbor based on ResolverService
     * and a null value is returned.
     * 
     * @param type
     *            one of {@link NamedResource.PEER},
     *            {@link NamedResource.GROUP},{@link NamedResource.PIPE} or
     *            {@link NamedResource.OTHER}
     * 
     * @param attribute
     *            the name of the attribute to search for. This is one of the
     *            fields defined by a NamedResource and advertisements are
     *            indexed one. For example <code>NAME</code> or
     *            <code>ID</code> are usually used to search resources by name
     *            or id.
     * 
     * @param value
     *            an expression specifying the items being searched for and also
     *            limiting the scope of items to be returned. This is usually a
     *            simple regular expression such as, for example,
     *            <code>TicTacToe*</code> to search for all entities with
     *            names that begin with TicTacToe.
     * 
     * @param threshold
     *            the maximum number of responses allowed from any one peer.
     * 
     * @return JXTA-ID string if a match was found, null other wise.
     * 
     * @throws IOException
     *             if a communication error occurs with the the JXTA network
     */
    public String[] localSearch(String type, String attribute, 
            String value, int threshold) throws IOException;
    
    public void remoteSearch(String type, String attribute, 
            String value, int threshold, MicroDiscoveryListener listener) 
    	throws IOException;
 
    
    public void cancelSearch(MicroDiscoveryListener listener);
    
    
    /**
     * Create and publish a {@link NamedResource#GROUP}
     * {@link NamedResource#PIPE}or a resource defined by Applications.
     * Typically, a resource defined by an application should be created by the
     * application itself.
     * 
     * @param resourceType
     *            one of {@link NamedResource#GROUP},
     *            {@link NamedResource#PIPE}or {@link NamedResource#OTHER}
     * 
     * @param resourceName
     *            the name of the resource being created, need not be unique
     * 
     * 
     * @param precookedID
     *            pre-defined id string  of the resource being created. Can be null.
     * 
     * @param arg
     *            an optional arg depending upon the type of resource being
     *            created. For example, for {@link NamedResource#PIPE}, this
     *            would be the type of {@link NamedResource#PIPE}that is to be
     *            created. For example, <code>JxtaUniCast</code> and
     *            <code>JxtaPropagate</code> are commonly-used values. This
     *            parameter can be <code>null</code>.
     * 
     * @return JXTA-ID string
     *  
     */
    public String create(String resourceType, String resourceName, String precookedID, String arg);

    /**
     * Join a peer group and publishes peer's advertisement in the peer group.
     * 
     * A peer can join a group by issuing this request. Currently there is no
     * leave command, but could decide to leave the group if there are no more
     * active clients using that group.
     * 
     * @param groupID
     *            JXTA-ID string of group to join. The group to be joined can be got by either: Creating
     *            it using the {@linl #create}or Searching a group
     *            advertisement using the {@link #search}
     * 
     * @param password
     *            the password required to join the group, if one is required.
     *            Otherwise, it is ignored. (Note: currently it is always
     *            ignored.
     * 
     * @return returns a new GroupService handler for the group joined.
     */
    public MicroGroupService join(String groupID, String password);

    /**
     * Send data to the specified Pipe.
     * 
     * @param pipeID,
     *            JXTA-ID string of {@link Pipe} to which data is to be sent.
     * 
     * @param data
     *            a {@link Message}containing an array of {@link Element}s
     *            which contain application data that is to be sent.
     * 
     * @throws IOException
     *             if there is a problem sending the message
     */
    public void send(String pipeID, MicroMessage data) throws IOException;

    /**
     * Register a listener for the pipe and start listening on the pipe.
     * 
     * @param pipeID
     *            JXTA-ID {@link Pipe}on which to listen for incoming messages
     * 
     * @param listener
     *            listener for incoming messages.
     * 
     * @throws IOException
     *             if a communication error occurs
     */
    public void listen(String pipeID, MicroListener listener) throws IOException;

    /**
     * resolves an output pipe.
     * 
     * Waits for timeout period to resolve a pipe and returns back true if a
     * pipe is resolved, false other wise.
     * 
     * @param pipeID
     *            JXTA-ID of {@link Pipe} on which to listen for incoming messages
     * 
     * @param timeout
     *            in ms
     * 
     * @return true if a pipe is resolved, false otherwise.
     * 
     * @throws IOException
     *             if a communication error occurs
     */
    public boolean resolve(String pipeID, int timeout) throws IOException;

    /**
     * Close a resource such as input Pipe. It removes any
     * listeners added for resource
     * 
     * @param stringID 
     * 				JXTA-ID string of resource to be closed
     * @throws IOException
     *             if a communication error occurs.
     */
    public void close(String stringID) throws IOException;

    /*
     * Following methods are not included into the MicroGroupService:
     * public void addDiscoveryListener(DiscoveryListener listener);
     * public void removeDiscoveryListener(DiscoveryListener listener);
     */
}