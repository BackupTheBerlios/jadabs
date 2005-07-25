/*
 * Created on Jul 7, 2005
 */
package ch.ethz.jadabs.bundleLoader.security;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.bundleLoader.BundleLoaderActivator;
import ch.ethz.jadabs.bundleLoader.api.BundleLoader;
import ch.ethz.jadabs.bundleLoader.api.BundleSecurity;
import ch.ethz.jadabs.bundleLoader.api.Descriptor;

/**
 * @author otmar
 * 
 * Main idea: if java.security.Security available, try to load security bundle,
 * else try to compute signatures/digests with methods in this class
 *  
 */
public class BundleSecurityImpl implements BundleSecurity {

    private static Logger LOG = Logger.getLogger(BundleSecurityImpl.class);
    
    private static final int BUFFERSIZE = 1024;
    
    // singleton pattern
    
    private static BundleSecurity instance;
    private static BundleLoader bl;
    
    private BundleSecurityImpl(){
        LOG.info("Internal BundleSecurityImpl initialized.");
    }
    
    public static void init(BundleLoader bl){
        if (instance == null) {
            BundleSecurityImpl.bl = bl;
            boolean noSecurity = System.getProperty("ch.ethz.jadabs.bundlesecurity.ignoresecurity", "").equals("true");
            if (noSecurity){
                LOG.info("WARNING: ignoring security checks");
                instance = new BundleSecurity(){
	                public boolean checkBundle(Descriptor desc, byte[] stream) throws Exception {
	                    return true;
	                }
	            };
            } else {
	            try {
	                BundleSecurityImpl.class.getClassLoader().loadClass("java.security.Security");
	                LOG.debug("Class java.security.Security present.");
	                // use the security bundle
	                instance = loadSecurityBundle();
	            } catch (Exception e){
	                LOG.debug("", e);
	            }
            }
            
            // default:
            if (instance == null){
                LOG.debug("using local BundleSecurityImpl");
                instance = new BundleSecurityImpl();
            }
        }
    }
    
    public static BundleSecurity Instance() throws Exception{
        if (instance == null) throw new Exception("init first BundleSecurity!");
        return instance;
    }
    
    private static BundleSecurity loadSecurityBundle() throws Exception{
        BundleContext bc = BundleLoaderActivator.bc;
        // get the bundle jars from available information sources
        String securityBundleJarUuid = BundleLoaderActivator.bc.getProperty("ch.ethz.jadabs.bundlesecurity.jaruuid");
        if (securityBundleJarUuid != null){
            InputStream jar = bl.fetchInformation(securityBundleJarUuid, null);
            Bundle bundle = BundleLoaderActivator.bc.installBundle(
                    securityBundleJarUuid, jar);
            bundle.start();
            ServiceReference sref = bc.getServiceReference(BundleSecurity.class.getName());
            return (BundleSecurity) bc.getService(sref);
        }
        LOG.debug("BundleSecurity bundle not found...");
        return null;        
    }
    
    /* (non-Javadoc)
     * @see ch.ethz.jadabs.bundleLoader.api.Security#checkBundle(java.io.InputStream, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean checkBundle(Descriptor desc, byte[] bundleData) throws Exception {
        LOG.debug("Checking bundle with local BundleSecurityImpl");
        byte[] digestBytes = sha1Digest(bundleData);
        String digestB64 = new String(Base64.encodeBase64(digestBytes));
        String bundleDigest = desc.getProperty("digest");
        if (bundleDigest != null && !digestB64.equals(bundleDigest)){
            LOG.debug("Digest is not the same.");
            return false;
        }
        String bundleSignature = desc.getProperty("signature");
        String bundlePublicKey = BundleLoaderActivator.bc.getProperty("ch.ethz.jadabs.bundlesecurity.publickey");
        if (bundlePublicKey == null){
            LOG.error("No public key available for checking signatures");
            return false;
        }
        return verifySignature(bundlePublicKey, digestBytes, bundleSignature);
    }
    
    private byte[] sha1Digest(byte[] data) throws Exception {
        Digest digest = new SHA1Digest();
        byte[] result = new byte[digest.getDigestSize()];
        digest.update(data, 0, data.length);
        digest.doFinal(result, 0);

        return result;
    }
        
    private boolean verifySignature(String publicKey, byte[] digestBytes, String signature) throws Exception{
        byte[] subjPubKeyBytes = Base64.decodeBase64(publicKey.getBytes());
        byte[] signatureBytes = Base64.decodeBase64(signature.getBytes());
        DSASubjectPublicKey pubKey = new DSASubjectPublicKey(subjPubKeyBytes);
        DSAVerifier verifier = new DSAVerifier(pubKey);
        return verifier.virfySignature(digestBytes, signatureBytes);
    }

}
