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

/**
 * @author otmar
 * 
 * Main idea: if java.security.Security available, load security bundle,
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
    }
    
    public static BundleSecurity init(BundleLoader bl){
        if (instance == null) {
            BundleSecurityImpl.bl = bl;
            try {
                BundleSecurityImpl.class.getClassLoader().loadClass("java.security.Security");
                LOG.debug("Class java.security.Security present.");
                // use the security bundle
                instance = loadSecurityBundle();
            } catch (Exception e){
                LOG.debug("", e);
            }
            if (instance == null){
                LOG.debug("using local BundleSecurityImpl");
                instance = new BundleSecurityImpl();
            }
        }
        return instance;
    }
    
    public static BundleSecurity Instance() throws Exception{
        if (instance == null) throw new Exception("init first BundleSecurity!");
        return instance;
    }
    
    private static BundleSecurity loadSecurityBundle() throws Exception{
        BundleContext bc = BundleLoaderActivator.bc;
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
        LOG.debug("BundleSecurity bundle not found...");
        return null;        
    }
    
    /* (non-Javadoc)
     * @see ch.ethz.jadabs.bundleLoader.api.Security#checkBundle(java.io.InputStream, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean checkBundle(InputStream stream, String digest,
            String digestGenAlgo, String signature, String keyGenAlgo,
            String publicKey) throws Exception {
        LOG.debug("Checking bundle with local BundleSecurityImpl");
        if (sha1Digest(stream).equals(digest)){
            LOG.debug("Digest is ok.");
            return verifySignature(publicKey, digest, signature);
        }
        return false;
    }
    
    private String sha1Digest(InputStream is) throws Exception {
        Digest digest = new SHA1Digest();
        byte[] result = new byte[digest.getDigestSize()];
        byte[] buffer = new byte[BUFFERSIZE];
        int length = 0;

        // read bytes into buffer and feed these bytes into the message digest object
        while ((length = is.read(buffer)) != -1) {
            digest.update(buffer, 0, length);
        }

        digest.doFinal(result, 0);

        return new String(Base64.encodeBase64(result));
    }
    
    private boolean verifySignature(String publicKey, String digest, String signature) throws Exception{
        byte[] subjPubKeyBytes = Base64.decodeBase64(publicKey.getBytes());
        byte[] digestBytes = Base64.decodeBase64(digest.getBytes());
        byte[] signatureBytes = Base64.decodeBase64(signature.getBytes());
        
        DSASubjectPublicKey pubKey = new DSASubjectPublicKey(subjPubKeyBytes);
        SHA1Digest digester = new SHA1Digest();
        byte[] buffer = new byte[digester.getDigestSize()];
        digester.update(digestBytes, 0, digestBytes.length);
        digester.doFinal(buffer, 0);
        DSAVerifier verifier = new DSAVerifier(pubKey);
        return verifier.virfySignature(buffer, signatureBytes);
    }

}
