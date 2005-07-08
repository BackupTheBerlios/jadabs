/*
 * Created on Jul 6, 2005
 */
package ch.ethz.jadabs.bundleSecurity;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import ch.ethz.jadabs.bundleLoader.api.BundleSecurity;

/**
 * @author otmar
 */
public class BundleSecurityImpl implements BundleSecurity {
    
    private static Logger LOG = Logger.getLogger(BundleSecurityImpl.class.getName());
    
    BASE64Decoder decoder = new BASE64Decoder();
    BASE64Encoder encoder = new BASE64Encoder();
    
    private static BundleSecurityImpl singleton;
    
    private BundleSecurityImpl(){
    }
    
    protected static BundleSecurityImpl getInstance(){
        if (singleton == null) singleton = new BundleSecurityImpl();
        return singleton;
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.security.api.Security#checkBundle(java.io.InputStream, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public boolean checkBundle(InputStream stream, String digest,
            String digestGenAlgo, String signature, String keyGenAlgo,
            String publicKey) throws Exception {
        
        //debug info
        String provider = java.security.Security.getProviders("MessageDigest." + digestGenAlgo)[0].getName();
        LOG.debug("Provider used for digest computation: " + provider);
        provider = java.security.Security.getProviders("Signature." + keyGenAlgo)[0].getName();
        LOG.debug("Provider used for signature checking: " + provider);
        
        // TODO: better way to do this?
        Vector byteArrays = new Vector();
        byte[] tmpArr = new byte[4096];
        int i;
        int nBytes;
        for (i = 0; (nBytes = stream.read(tmpArr)) == 4096; i += 4096){
            byteArrays.add(tmpArr.clone());
        }
        byte[] jarBytes = new byte[i + nBytes];
        Iterator iter = byteArrays.iterator();
        i = 0;
        while (iter.hasNext()) {
            System.arraycopy(iter.next(), 0, jarBytes, i, 4096);
            i += 4096;
        }
        System.arraycopy(tmpArr, 0, jarBytes, i, nBytes);
                
        byte[] digestBytes = computeDigest(jarBytes, digestGenAlgo);
        if (digest.equals(encoder.encode(digestBytes))){
            return verifySignature(getPublicKey(publicKey, keyGenAlgo), digestBytes, decoder.decodeBuffer(signature));
        }
        return false;
    }
    
    private PublicKey getPublicKey(String pubKeyStr, String keyGenAlgo) throws Exception{
        KeyFactory keyFactory = KeyFactory.getInstance(keyGenAlgo);
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(decoder.decodeBuffer(pubKeyStr));
        return keyFactory.generatePublic(publicKeySpec);
    }
    
    private byte[] computeDigest(byte[] buffer, String digestGenAlgo) throws Exception{
        MessageDigest msgDigest = MessageDigest.getInstance(digestGenAlgo);
        msgDigest.update(buffer);
        return msgDigest.digest();
    }
    
    // Verifies the signature for the given buffer of bytes using the public key.
    private boolean verifySignature(PublicKey key, byte[] buffer,
            byte[] signature) throws Exception {
        Signature sig = Signature.getInstance(key.getAlgorithm());
        sig.initVerify(key);
        sig.update(buffer, 0, buffer.length);
        return sig.verify(signature);
    }

}
