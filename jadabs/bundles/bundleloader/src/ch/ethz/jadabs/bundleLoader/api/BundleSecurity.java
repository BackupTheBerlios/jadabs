/*
 * Created on Jul 6, 2005
 */
package ch.ethz.jadabs.bundleLoader.api;


/**
 * @author otmar
 */
public interface BundleSecurity {

    public boolean checkBundle(Descriptor descriptor, byte[] bundleData) throws Exception;
}
