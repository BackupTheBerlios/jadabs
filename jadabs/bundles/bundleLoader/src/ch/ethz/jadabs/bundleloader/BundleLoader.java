package ch.ethz.jadabs.bundleloader;

import java.util.Enumeration;

/**
 * @author rjan
 *
 */
public interface BundleLoader {

    Enumeration getBundleAdvertisements();
    
	/**
	 * 
	 * @param name
	 * @param group
	 * @param version	
	 * @throws Exception
	 */
	public void load(String name, String group, String version)
			throws Exception;
	
	public BundleInformation getBundleInfo(String id);
	
	/**
	 * 
	 * @param bl
	 */
	public void addListener(BundleLoaderListener bl);
	
	/**
	 * 
	 * @param bl
	 */
	public void removeListener(BundleLoaderListener bl);
}