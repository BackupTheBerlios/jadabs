package ch.ethz.jadabs.bundleloader;

/**
 * @author rjan
 *
 */
public interface BundleLoader {

	/**
	 * 
	 * @param name
	 * @param group
	 * @param version	
	 * @throws Exception
	 */
	public void load(String name, String group, String version)
			throws Exception;
	
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