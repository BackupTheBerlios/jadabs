/*
 * Created on Dec 1, 2004
 * 
 * Implements ch.ethz.jadabs.remotefw.RemoteFrameworkListener
 *
 */
package ch.ethz.jadabs.amonem.discovery;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.remotefw.Framework;
import ch.ethz.jadabs.remotefw.RemoteFrameworkListener;


/**
 * 
 * Implements ch.ethz.jadabs.remotefw.RemoteFrameworkListener
 * 
 * @author bam
 *
 */
public class AmonemRFWListener implements RemoteFrameworkListener
{

    private Logger LOG = Logger.getLogger(AmonemRFWListener.class.getName());
    
    // the discovery instance, this is where joining/leaving peers have to be 
    // "promoted to"
    private AmonemDiscovery disc;
    
    
    /**
     * 
     * @param disc The ch.ethz.jadabs.amonem.discovery.AmonemDiscovery object this listener has to report to.
     */
    public AmonemRFWListener(AmonemDiscovery disc) {
        // do i need to call super?
        super();
        this.disc = disc;
    }
    

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.remotefw.RemoteFrameworkListener#enterFrameworkEvent(ch.ethz.jadabs.remotefw.Framework)
     */
    
    /**
     *
     * @param fw The ch.ethz.jadabs.remotefw.Framework of the peer that entered the network
     */
    public void enterFrameworkEvent(Framework fw)
    {
        LOG.info("Neuen Peer gefunden: " + fw.getPeername());
        disc.addPeerToDAG(fw);
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.remotefw.RemoteFrameworkListener#leaveFrameworkEvent(ch.ethz.jadabs.remotefw.Framework)
     */

    /**
    *
    * @param fw The ch.ethz.jadabs.remotefw.Framework of the peer that left the network
    * @see ch.ethz.jadabs.remotefw.RemoteFrameworkListener#leaveFrameworkEvent(ch.ethz.jadabs.remotefw.Framework)
    */
    public void leaveFrameworkEvent(Framework fw)
    {
        LOG.info("Peer verloren: " + fw.getPeername());
        disc.removePeerFromDAG(fw);
    }

}
