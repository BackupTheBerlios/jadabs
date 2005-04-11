package ch.ethz.jadabs.amonem.manager;
import java.util.Enumeration;
import java.util.Vector;

/*
 * Created on 21.11.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/**
 * @author bam
 *
 */
public class DAGGroup extends DAGMember{

	private Vector children = new Vector();
	private int Width;
	private int Height;
	private int Color;

	public DAGGroup(String name) {
		super(name);
		this.setType(1);
		// TODO Auto-generated constructor stub
	}
	/**
	 * @return height
	 */
	public int getHeight() {
		return Height;
	}
	/**
	 * @param height
	 */
	public void setHeight(int height) {
		Height = height;
	}
	/**
	 * 
	 * @return width
	 */
	public int getWidth() {
		return Width;
	}
	/**
	 * @param width
	 */
	public void setWidth(int width) {
		Width = width;
	}
	/**
	 * @return Returns the children.
	 */
	public Vector getChildren() {
		return children;
	}
	/**
	 * @param children The children to set.
	 */
	public void addChild(DAGMember child) {
		//System.out.println("add " + child.getName() + " to " + this.getName());
		this.children.add(child);
		child.addParent(this);
		
		// loop grouplisteners, call childAdded(child)
		//AmonemManagerActivator.amonemManager.groupListeners();
	}
	/**
	 * removes the given DAGMember from the subdag.
	 * if this=DAGMember it removes this.
	 * @param member
	 */
	public void removeChild(DAGMember member) {
		if (children.contains(member)) {
			children.remove(member);
			if (member.getType()==1){
				member.removeMyself();
			}
		}
		
	}
	/**
	 * removes this from the DAG
	 */
	public void removeMyself(){
		Enumeration Iter= children.elements();
		DAGMember curMember;
		while (Iter.hasMoreElements()){
			curMember= (DAGMember) Iter.nextElement();
			curMember.removeParent((DAGGroup) this);
		}
	}
	/**
	 * @return Color
	 */
	public int getColor(){
		return Color;
	}
	/**
	 * @param color
	 */
	public void setColor(int color){
		Color=color;
	}
	/**
	 * searches a DAGMember by Name
	 * @param name
	 * @return DAGMember
	 */
	public DAGMember getElement(String name){
		DAGMember Searched=null;
		if (this.getName().equals(name)){
			return this;
		}
		Enumeration iter= children.elements();
		while (iter.hasMoreElements()){
			DAGMember curSearched=null;
			DAGMember cur= (DAGMember) iter.nextElement();
			if (cur.getType()==1){
				curSearched= ((DAGGroup) cur).getElement(name);
			} else {
				curSearched= ((DAGPeer) cur).getElement(name);
			}
			if (curSearched!=null){
				Searched= curSearched;
			}
		}
		return Searched;
	}
	
}
