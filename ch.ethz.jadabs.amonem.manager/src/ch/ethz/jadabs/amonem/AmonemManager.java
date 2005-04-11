/*
 * Created on Dec 8, 2004
 *
 */
package ch.ethz.jadabs.amonem;

import java.util.Vector;

import ch.ethz.jadabs.amonem.deploy.AmonemDeploySkeleton;
import ch.ethz.jadabs.amonem.manager.DAGGroup;
import ch.ethz.jadabs.amonem.manager.DAGMember;
import ch.ethz.jadabs.amonem.manager.DAGPeer;
import ch.ethz.jadabs.servicemanager.ServiceReference;


/**
 * @author andfrei
 * 
 */
public interface AmonemManager
{
    void start();
    
    void addGroupListener(GroupListener grouplistener);
    
    void addPeerListener(PeerListener peerlistener);
    
	void addBundleListener(BundleListener bundlelistener);
	
	void installBundle(String PeerName, String PathName);
	
	void stopBundle(String PeerName, String BundleName);
	
	void startBundle(String PeerName, String BundleName);
	
	void removeBundle(String PeerName, String BundleName);
	
	void getServiceAdvertisements(String peerName, String filter);
	
    DAGGroup getDiscoveryROOT();
    
    DAGGroup getDeployROOT();
    
    // void removeGroupListener....
    AmonemDeploySkeleton getSkeleton(String name, String download_dir, String deploy_path, String xargs_template);
    
    AmonemDeploySkeleton getDeploySkeleton(DAGPeer Peer, String name, String download_dir, String deploy_path, String xargs_template);
    
    void newPeer(AmonemDeploySkeleton Skeleton);
    
    Vector getRepository(String URLString);
    
	void setFolders(String download_dir, String deploy_path, String xargs_template, String temp_folder);
    
    void importDAG(String locationName, boolean Start);
	
    boolean exportDAG(String locationName, String ConfigName);
    
    void exportPeer(String locationName, String PeerName);
    
    void bundleChanged(String PeerName);
            
    void kill(String PeerName);
    
    void deletePeer(String PeerName);
    
    void childAddedInDeploy(DAGMember dm);
    
    void childAddedInDiscovery(DAGMember dm);
    
    String prepareDirPath(String path);
    
    String prepareFilePath(String path);
    
}
