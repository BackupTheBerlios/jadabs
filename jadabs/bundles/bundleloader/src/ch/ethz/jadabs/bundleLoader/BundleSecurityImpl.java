/*
 * Created on Jul 7, 2005
 */
package ch.ethz.jadabs.bundleLoader;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.bundleLoader.api.BundleSecurity;

/**
 * @author otmar
 * 
 * Main idea: if java.security.Security available, load security bundle,
 * else try to compute signatures/digests with methods in this class
 * 
 */
public class BundleSecurityImpl implements BundleSecurity {

    private static Logger LOG = Logger.getLogger(BundleSecurityImpl.class);
    
    // singleton pattern
    
    private static BundleSecurity instance;
    
    private BundleSecurityImpl(){
    }
    
    public static BundleSecurity Instance(){
        if (instance == null) {
            try {
                BundleSecurityImpl.class.getClassLoader().loadClass("java.security.Security");
                LOG.debug("Class java.security.Security present.");
                // use the security bundle
                instance = loadSecurityBundle();
            } catch (Exception e){
                LOG.debug("", e);
            }
            if (instance == null) instance = new BundleSecurityImpl();
        }
        return instance;
    }
    
    private static BundleSecurity loadSecurityBundle() throws Exception{
        BundleContext bc = BundleLoaderActivator.bc;
        BundleLoaderImpl bl = BundleLoaderImpl.getInstance();
        // get the bundle jars from available information sources
        String securityBundleJarUuid = System.getProperty("ch.ethz.jadabs.bundlesecurity.jaruuid");
        if (securityBundleJarUuid != null){
            InputStream jar = bl.fetchInformation(securityBundleJarUuid, null);
            Bundle bundle = BundleLoaderActivator.bc.installBundle(
                    securityBundleJarUuid, jar);
            bundle.start();
            ServiceReference sref = bc.getServiceReference(BundleSecurity.class.getName());
            return (BundleSecurity) bc.getService(sref);
        }
        return null;        
    }
    
    /* (non-Javadoc)
     * @see ch.ethz.jadabs.bundleLoader.api.Security#checkBundle(java.io.InputStream, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean checkBundle(InputStream stream, String digest,
            String digestGenAlgo, String signature, String keyGenAlgo,
            String publicKey) throws Exception {
        // TODO here comes the local implementation
        return false;
    }

}
