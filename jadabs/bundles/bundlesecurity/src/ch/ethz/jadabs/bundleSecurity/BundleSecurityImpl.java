/*
 * Created on Jul 6, 2005
 */
package ch.ethz.jadabs.bundleSecurity;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import ch.ethz.jadabs.bundleLoader.api.Descriptor;
import ch.ethz.jadabs.bundleLoader.api.BundleSecurity;

/**
 * @author otmar
 */
public class BundleSecurityImpl implements BundleSecurity {
    
    private static Logger LOG = Logger.getLogger(BundleSecurityImpl.class.getName());
    
    private static int BUFFERSIZE = 1024;
    
    BASE64Decoder decoder = new BASE64Decoder();
    BASE64Encoder encoder = new BASE64Encoder();
    
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
        String certificateID = descr.getProperty("certificate-ID");
        X509Certificate certificate = CertificateRepository.Instance().getTrustedCertificate(certificateID);
        
        String digestGenAlgo = descr.getProperty("digestGenerationAlgorithm");
        String digest = descr.getProperty("digest");
        LOG.debug("public key: " + certificate.getPublicKey());
        if (LOG.isDebugEnabled()){
            String fingerprint = new String(Base64.encodeBase64(certificate.getSignature()));
            LOG.debug("certificate fingerprint: " + fingerprint);
            String signature = new String(Base64.encodeBase64(sigBytes));
            LOG.debug("signature: " + signature);
        }
        
        if (digestGenAlgo != null && digest != null){
            byte[] digestBytes = computeDigest(bundleData, digestGenAlgo);
            if (!digest.equals(encoder.encode(digestBytes))) return false;
        }
        return verifySignature(certificate.getPublicKey(), bundleData, sigBytes);
    }
    
    private byte[] computeDigest(byte[] message, String digestGenAlgo) throws Exception{
        MessageDigest msgDigest = MessageDigest.getInstance(digestGenAlgo);
        return msgDigest.digest(message);
    }
    
    // Verifies the signature for the given buffer of bytes using the public key.
    private boolean verifySignature(PublicKey key, byte[] message,
            byte[] signature) throws Exception {
        Signature sig = Signature.getInstance(key.getAlgorithm());
        sig.initVerify(key);
        
        //TODO updating with the whole array doesn't work (BUG?):
        //sig.update(message);
        //workaround:
        ByteArrayInputStream bis = new ByteArrayInputStream(message);
        int i;
        byte[] buffer = new byte[1024];
        while ((i = bis.read(buffer)) != -1)
            sig.update(buffer);
        return sig.verify(signature);
    }

}