package ch.ethz.jadabs.jxme.microservices;
/************************************************************************
 *
 * $Id: MicroListener.java,v 1.2 2005/01/23 15:47:37 printcap Exp $
 *
 **********************************************************************/


/**
 * Defines a callback interface for applications for getting incoming
 * {@link ch.ethz.jadabs.jxme.Message}. MicroListener is a striped down 
 * variant of ch.ethz.jadabs.services.Listener.
 * 
 * @author Ren&eacute; M&uul;ller
 * @see ch.ethz.jadabs.services.Listener
 */
public interface MicroListener
{

    /**
     * handles messages from lower layers
     * 
     * @param message
     *            incoming {@link Message}
     * @param listenerId
     *            in order to be able to demux messages between the layers above
     */
    public void handleMessage(MicroMessage message, String listenerId);

    /**
     * handle Search Response from the resolver and above
     * 
     * @param namedResourceName
     *            name of NamedResource found
     */
    // FIXME: Andreas why does Listener contain a handleSearchResponse 
    //public void handleSearchResponse(String namedResourceName);

}