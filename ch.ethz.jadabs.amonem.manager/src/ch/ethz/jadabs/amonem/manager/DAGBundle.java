/*
 * Created on 23.12.2004
 *
 */
package ch.ethz.jadabs.amonem.manager;

/**
 * @author bam
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DAGBundle {
	private long BundleID;
	private String Name;
	private int State;
	private String Property;
	private String UpdateLocation;		//im xml?
	private String UUID;
	
	/**
	 * @return Returns the uUID.
	 */
	public String getUUID() {
		return UUID;
	}
	/**
	 * @param uuid The uUID to set.
	 */
	public void setUUID(String uuid) {
		UUID = uuid;
	}
	/**
	 * @return Returns the bundleID.
	 */
	public long getBundleID() {
		return BundleID;
	}
	/**
	 * @param bundleID The bundleID to set.
	 */
	public void setBundleID(long bundleID) {
		BundleID = bundleID;
	}
	/**
	 * @return Returns the location.
	 */
	public String getUpdateLocation() {
		return UpdateLocation;
	}
	/**
	 * @param location The location to set.
	 */
	public void setUpdateLocation(String location) {
		UpdateLocation = location;
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return Name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		Name = name;
	}
	/**
	 * @return Returns the property.
	 */
	public String getProperty() {
		return Property;
	}
	/**
	 * @param property The property to set.
	 */
	public void setProperty(String property) {
		Property = property;
	}
	/**
	 * @return Returns the property.
	 */
	public int getState() {
		return State;
	}
	/**
	 * @param property The property to set.
	 */
	public void setState(int state) {
		this.State = state;
	}
}
