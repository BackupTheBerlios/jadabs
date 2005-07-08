/*
 * Created on Jul 6, 2005
 */
package ch.ethz.jadabs.bundleLoader.api;

import java.io.InputStream;

/**
 * @author otmar
 */
public interface BundleSecurity {

    public boolean checkBundle(InputStream stream, String digest,
            String digestGenAlgo, String signature, String keyGenAlgo,
            String publicKey) throws Exception;
}
