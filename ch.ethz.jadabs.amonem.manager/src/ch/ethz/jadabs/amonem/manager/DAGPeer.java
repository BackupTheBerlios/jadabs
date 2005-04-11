package ch.ethz.jadabs.amonem.manager;

import java.util.Enumeration;
import java.util.Vector;

import ch.ethz.jadabs.remotefw.BundleInfoListener;
import ch.ethz.jadabs.servicemanager.ServiceReference;

/*
 * Created on 18.11.2004
 *
 */

/**
 * Implementation of a DAGPeer. Contains all Peerinformation Necessary
 * for both, Deploy-DAG and Discovery-DAG.  
 * 
 *  @author barbara
 *
 */
public class DAGPeer extends DAGMember{

	private Vector Connections= new Vector();
	private Process myProcess;
	private Vector Jars= new Vector();
	private String JavaPath;
	private Vector Bundles= new Vector();
	private String Platform;
	private String DEPLOY_PATH;
	private BundleInfoListener listener;
	private boolean Deployed;
	
	private Vector services = new Vector();
	/**
	 * @return Returns deployed.
	 */
	public boolean isDeployed() {
		return Deployed;
	}
	/**
	 * @param sets deployed 
	 */
	public void setDeployed(boolean deployed) {
		Deployed = deployed;
	}
	/**
	 * @return Returns the dEPLOY_PATH.
	 */
	public String getDEPLOY_PATH() {
		return DEPLOY_PATH;
	}
	/**
	 * @param deploy_path the DEPLOY_PATH to set.
	 */
	public void setDEPLOY_PATH(String deploy_path) {
		DEPLOY_PATH = deploy_path;
	}
	/**
	 * @return Returns the platform.
	 */
	public String getPlatform() {
		return Platform;
	}
	/**
	 * @param sets the platform
	 */
	public void setPlatform(String platform) {
		Platform = platform;
	}
	/**
	 * @param name
	 */
	
	public void addServiceReference(ServiceReference sref)
	{
	    services.add(sref);
	}
	
	public DAGPeer(String name) {
		super(name);
		this.setType(2);
	}
	
	/**
	 * Adds a DAGPipe to the Peer. 
	 * 
	 * @param Peer the pipe leads to
	 * @param type of the connection
	 */
	public void addConnection(DAGPeer Peer, int type){
		DAGPipe Con = new DAGPipe(this, Peer, type);
		Connections.add(Con);
		//System.out.println(Con.getPeer());
		//System.out.println(Peer.getName()+" added");
	}
	
	/**
	 * 
	 * @param Peer, that the pipe connects to
	 * @param type of the pipe
	 */
	//what does the pipe look like
	public void deleteConnection(DAGPeer Peer, int type){
		Enumeration search= Connections.elements();
		while (search.hasMoreElements()){
			DAGPipe Con = (DAGPipe) search.nextElement();
			//System.out.println(Con.getPeer());
			if (Con.getPeers().equals(Peer.getName())){
				if (Con.getType() == type){
					Connections.remove(Con);
					//System.out.println(Peer.getName()+ " removed");
				}
			}
		}
	}
	
	/**
	 *  removes the peer form the DAG (form all parents)
	 */
	public void removeMyself(){
		Enumeration iter= Parents.elements();
		while (iter.hasMoreElements()){
			DAGGroup group= (DAGGroup) iter.nextElement();
			group.removeChild(this);
			Parents.remove(group);
		}
	}
	
	/**
	 * the processobject of the started peer is stored here
	 * @param procedure
	 */
	public void setProcess(Process proc){
		myProcess= proc;
	}
	/**
	 * @return process of the peer
	 */
	public Process getProcess(){
		return myProcess;
	}
	/**
	 * @param javapath
	 */
	public void setJavaPath(String path){
		JavaPath= path;
	}
	/**
	 * @return javapath
	 */
	public String getJavaPath(){
		return JavaPath;
	}
	/**
	 * @param DAGBundle
	 */
	public void setBundle(DAGBundle myBundle){
		Bundles.add(myBundle);
	}
	/**
	 * @return allBudles
	 */
	public Vector getBundles(){
		return Bundles;
	}
	/**
	 * searches bundles by ID.
	 * @param BundleID
	 * @return DAGBundle
	 */
	public DAGBundle getBundle(long ID){
		DAGBundle cur;
		Enumeration en= Bundles.elements();
		while (en.hasMoreElements()){
			cur= (DAGBundle) en.nextElement();
			if (cur.getBundleID()== ID){
				return cur;
			}
		}
		return null;
	}
	/**
	 * searches bundles by name
	 * @param BundleName
	 * @return DAGBundle
	 */
	public DAGBundle getBundle(String Name){
		DAGBundle cur;
		Enumeration en= Bundles.elements();
		while (en.hasMoreElements()){
			cur= (DAGBundle) en.nextElement();
			if (cur.getName().equals(Name)){
				return cur;
			}
		}
		return null;
	}
	/**
	 * @param BundleName
	 */
	public void removeBundle(String BundleName){
		DAGBundle cur;
		Enumeration en= Bundles.elements();
		while (en.hasMoreElements()){
			cur= (DAGBundle) en.nextElement();
			if (cur.getName().equals(BundleName)){
				Bundles.remove(cur);
			}
		}
	}
	/**
	 * recursive search for a DAGMember (by name)
	 * @param membername
	 * @return DAGMember
	 */
	public DAGMember getElement(String name){
		if (this.getName().equals(name)){
			return this;
		}
		return null;
	}
	/**
	 * @return listener.
	 */
	public BundleInfoListener getListener() {
		return listener;
	}
	/**
	 * @param listener
	 */
	public void setListener(BundleInfoListener listener) {
		this.listener = listener;
	}
    /**
     * @param uuid
     * @return
     */
    public ServiceReference getServiceReference(String uuid)
    {
        for(Enumeration en = services.elements(); en.hasMoreElements();)
        {
            ServiceReference sref = (ServiceReference) en.nextElement();
            
            if (sref.getID().equals(uuid))
                return sref;
        }
        
        return null;
    }
}
