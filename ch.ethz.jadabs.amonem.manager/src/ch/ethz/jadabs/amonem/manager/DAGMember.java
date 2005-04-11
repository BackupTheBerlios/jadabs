package ch.ethz.jadabs.amonem.manager;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.remotefw.Framework;
/*
 * Created on 18.11.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author barbara
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DAGMember{
	
	protected NamedResource Resource;
	protected Framework FW;
	protected int DAGID;
	protected int X;
	protected int Y;
	protected int Type;              // 1=Goup; 2=Peer
	protected String Name;
	protected Vector Parents=new Vector();
	protected Hashtable Properties= new Hashtable();

	
	protected Vector listeners = new Vector();
	/**
	 * @param name
	 */
	public DAGMember(String name){
		Name= name;
	}
	/**
	 * Grouptype=1; Peertype=2
	 * @param type
	 */
	public void setType(int type){
		Type= type;
	}
	/**
	 * @return type
	 */
	public int getType(){
		return Type;
	}
	/**
	 * @return Name
	 */
	public String getName(){
		return Name;
	}
	/**
	 * @param DAGMember
	 * @return boolean
	 */
	private boolean isEqual(DAGMember DM){
		if (this.DAGID == DM.getDAGID()){
			return true;
		}
		else return false;
	}
	/**
	 * @return DAGID
	 */
	public int getDAGID() {
		return DAGID;
	}
	/**
	 * 
	 * @param dagid
	 */
	public void setDAGID(int dagid) {
		DAGID = dagid;
	}
	/**
	 * @param parent
	 */
	public void addParent(DAGMember parent){
		Parents.add(parent);
	}
	/**
	 * @return all direct Parents
	 */
	public Vector getParents(){
		return Parents;
	}
	/**
	 * @return x
	 */
	public int getX() {
		return X;
	}
	/**
	 * @param x
	 */
	public void setX(int x) {
		X = x;
	}
	/**
	 * @return y
	 */
	public int getY() {
		return Y;
	}
	/**
	 * @param y
	 */
	public void setY(int y) {
		Y = y;
	}
	/**
	 * @return resource
	 */
    public NamedResource getResource()
    {
        return Resource;
    }
    /**
     * @param resource
     */
    public void setResource(NamedResource resource)
    {
        Resource = resource;
    }
    public void removeMyself(){
    	
    }
    /**
     * @param group
     */
    public void removeParent(DAGGroup group){
    	Parents.contains(group);
    	Parents.remove(group);
    }
    /**
     * @return all Parents
     */
    public Vector getAllParents(){
    	Enumeration Par= Parents.elements();
    	Vector allParents= new Vector();
    	DAGMember cur;
    	if (Name.equals("rootGroup")){
    		allParents.add(this);
    	}
    	else {
    		while (Par.hasMoreElements()){
    			cur= (DAGMember) Par.nextElement();
    			Vector parent= cur.getAllParents();
    			Enumeration curEnum= parent.elements();
    			while (curEnum.hasMoreElements()){
    				DAGMember meb= (DAGMember) curEnum.nextElement();
    				if (!allParents.contains(meb)){
    					allParents.add(meb);
    				}
    			}
    		}
    	}
    	return allParents;
    }
    /**
     * @param framework
     */
	public void setFramework(Framework fw){
		FW= fw;
		setProperties();
	}
	/**
	 * @return framework
	 */
	public Framework getFramework(){
		return FW;
	}
	/**
	 * fills the properties in a hashtable
	 *
	 */
	private void setProperties(){
		String Peername= FW.getPeername();
		Properties.put("ch.ethz.jadabs.jxme.peeralias", Peername);
	}
	/**
	 * 
	 * @return properties hashtable
	 */
    public Hashtable getProperites(){
    	return Properties;
    }
    
}
