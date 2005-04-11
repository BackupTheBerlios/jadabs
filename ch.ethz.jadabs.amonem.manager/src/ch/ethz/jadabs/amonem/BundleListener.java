package ch.ethz.jadabs.amonem;

import ch.ethz.jadabs.amonem.manager.DAGPeer;


public interface BundleListener extends DAGListener
{
    
    void bundleChanged(DAGPeer peer);   
    
       
}
