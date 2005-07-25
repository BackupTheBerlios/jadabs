/*
 * Created on Jul 6, 2005
 */
package ch.ethz.jadabs.bundleSecurity;

import java.security.MessageDigest;
import java.security.Signature;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.bundleLoader.api.Descriptor;
import ch.ethz.jadabs.bundleLoader.api.BundleSecurity;

/**
 * @author otmar
 */
public class BundleSecurityImpl implements BundleSecurity {
    
    private static Logger LOG = Logger.getLogger(BundleSecurityImpl.class.getName());
    
    private static int BUFFERSIZE = 1024;
    
    private static BundleSecurityImpl instance;
    
    private BundleSecurityImpl(){
    }
    
    protected static BundleSecurityImpl getInstance(){
        if (instance == null) instance = new BundleSecurityImpl();
        return instance;
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.security.api.Security#checkBundle(java.io.InputStream, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean checkBundle(Descriptor descr, byte[] bundleData) throws Exception {
        LOG.debug("Checking bundle with BundleSecurity Bundle");
        
        byte[] sigBytes = Base64.decodeBase64(descr.getProperty("signature").getBytes());
        byte[] certBytes = Base64.decodeBase64(descr.getProperty("certificate").getBytes());
        X509Certificate certificate = CertificateManager.Instance().getTrustedCertificate(certBytes);
        
        LOG.debug("public key: " + certificate.getPublicKey());
        if (LOG.isDebugEnabled()){
            String fingerprint = new String(Base64.encodeBase64(certificate.getSignature()));
            LOG.debug("certificate fingerprint: " + fingerprint);
            String signature = new String(Base64.encodeBase64(sigBytes));
            LOG.debug("signature: " + signature);
        }
        
        String digestGenAlgo = descr.getProperty("digestGenerationAlgorithm");
        String digest = descr.getProperty("digest");
        LOG.debug("length of data: " + bundleData.length);
        if (digestGenAlgo != null && digest != null){
            byte[] digestBytes = computeDigest(bundleData, digestGenAlgo);
            if (!digest.equals(new String(Base64.encodeBase64(digestBytes)))){
                LOG.debug("Digest is not the same...");
                return false;
            }
        }
        return verifySignature(certificate, bundleData, sigBytes);
    }
    
    private byte[] computeDigest(byte[] message, String digestGenAlgo) throws Exception{
        MessageDigest msgDigest = MessageDigest.getInstance(digestGenAlgo);
        return msgDigest.digest(message);
    }
    
    // Verifies the signature for the given buffer of bytes using the public key.
    private boolean verifySignature(X509Certificate certificate, byte[] message,
            byte[] signature) throws Exception {
        Signature sig = Signature.getInstance(certificate.getSigAlgName());
        sig.initVerify(certificate);
        sig.update(message);
        return sig.verify(signature);
    }

}