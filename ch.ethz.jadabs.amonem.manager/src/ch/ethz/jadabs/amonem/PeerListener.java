/*
 * Created on Dec 8, 2004
 *
 */
package ch.ethz.jadabs.amonem;

import ch.ethz.jadabs.amonem.manager.DAGPipe;
import ch.ethz.jadabs.servicemanager.ServiceReference;


/**
 * @author andfrei
 * 
 */
public interface PeerListener extends DAGListener
{
    
    void pipeAdded(DAGPipe dagpipe);        
    
    void pipeRemoved(DAGPipe dagpipe);
    
    void serviceReferenceAdded(ServiceReference sref);
    
//    void bundleChanged(DAGBundleInfo dagbinfo);
}
