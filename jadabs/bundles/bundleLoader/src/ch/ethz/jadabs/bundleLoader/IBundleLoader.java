package ch.ethz.jadabs.bundleLoader;

/**
 * @author rjan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface IBundleLoader {

	/**
	 * 
	 * @param name
	 * @param group
	 * @param version	
	 * @throws Exception
	 */
	public void load(String name, String group, String version)
			throws Exception;
}