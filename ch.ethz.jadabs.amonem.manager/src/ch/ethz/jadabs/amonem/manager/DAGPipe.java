package ch.ethz.jadabs.amonem.manager;


/*
 * Created on 01.12.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
/**
 * @author barbara
 *
 * TODO integrate pipes to the functionality of the plugin
 */
public class DAGPipe {
	
	private DAGPeer[] Endpoints;
	private int Type; 		// 1= WiFi; 2= ? ...
	/**
	 * 
	 * @param endpoint1
	 * @param endpoint2
	 * @param pipetyp
	 */
	public DAGPipe(DAGPeer Peer1, DAGPeer Peer2, int Typ){
		Endpoints= new DAGPeer[2];
		Endpoints[0]= Peer1;
		Endpoints[1]= Peer2;
		Type= Typ;
	}
	/**
	 * 
	 * @return endpoint1
	 */
	public DAGPeer getPeers() {
		return Endpoints[0];
	}
	/**
	 * 
	 * @param endpoint1
	 * @param endpoint2
	 */
	public void setPeers(DAGPeer peer1, DAGPeer peer2) {
		Endpoints[0] = peer1;
		Endpoints[1] = peer2;
	}
	/**
	 * @return pipetype
	 */
	public int getType() {
		return Type;
	}
	/**
	 * @param pipetype
	 */
	public void setType(int type) {
		Type = type;
	}
	


}
