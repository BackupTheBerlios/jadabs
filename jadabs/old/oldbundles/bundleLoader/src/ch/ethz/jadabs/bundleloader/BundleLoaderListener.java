/*
 * Created on Feb 3, 2005
 *
 */
package ch.ethz.jadabs.bundleloader;


/**
 * @author andfrei
 * 
 */
public interface BundleLoaderListener
{

    /**
     * Called once a Bundle changes, type corresponds to Bundle.START/INSTALL/UNINSTALL
     * 
     * @param binfo
     * @param type
     */    
    void bundleChanged(BundleInformation binfo, int type);
}
