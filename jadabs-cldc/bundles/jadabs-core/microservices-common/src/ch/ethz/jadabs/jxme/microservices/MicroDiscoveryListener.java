/************************************************************************
 *
 * $Id: MicroDiscoveryListener.java,v 1.1 2005/01/16 14:06:37 printcap Exp $
 *
 **********************************************************************/

package ch.ethz.jadabs.jxme.microservices;

/**
 * Defines a callback interface for applications for getting incoming
 * {@link ch.ethz.jadabs.jxme.Message}. This is a striped down
 * variant for J2ME/MDIP. 
 */

public interface MicroDiscoveryListener
{

    /**
     * handle Search Response from the resolver and above
     * 
     * @param resourceType 
     *            type of discovered {@link NamedResource}
     * @param resourceName
     * 				name of discovered {@link NamedResource}
     * @param stringID 
     * 				JXTA-ID of discovered {@link NamedResource}
     */
    public void handleSearchResponse(String resourceType, String resourceName, String stringID);
    
    public void handleNamedResourceLoss(String resourceType, String resourceName, String stringID);

}