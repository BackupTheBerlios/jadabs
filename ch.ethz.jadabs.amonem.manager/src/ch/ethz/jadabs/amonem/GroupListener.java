/*
 * Created on Dec 8, 2004
 *
 */
package ch.ethz.jadabs.amonem;

import ch.ethz.jadabs.amonem.manager.DAGMember;


/**
 * @author andfrei
 * 
 */
public interface GroupListener extends DAGListener
{
    
    void childAddedInDiscovery(DAGMember dagmember);   // receives the member added
    
    void childAddedInDeploy(DAGMember dagmember);   // receives the member added
    
    void childDeleted(DAGMember dagmember);
    
    void childRemoved(DAGMember dagmember); // receives the member removed 
}
