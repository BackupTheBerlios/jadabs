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

import org.apache.log4j.Logger;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import ch.ethz.jadabs.bundleLoader.api.BundleSecurity;

/**
 * @author otmar
 */
public class BundleSecurityImpl implements BundleSecurity {
    
    private static Logger LOG = Logger.getLogger(BundleSecurityImpl.class.getName());
    
    private static int BUFFERSIZE = 1024;
    
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
        LOG.debug("Checking bundle with BundleSecurity Bundle");
        //debug info
        String provider = java.security.Security.getProviders("MessageDigest." + digestGenAlgo)[0].getName();
        LOG.debug("Provider used for digest computation: " + provider);
        provider = java.security.Security.getProviders("Signature." + keyGenAlgo)[0].getName();
        LOG.debug("Provider used for signature checking: " + provider);
               
        
        // TODO: better way to do this?
//        Vector byteArrays = new Vector();
//        byte[] tmpArr = new byte[BUFFERSIZE];
//        int i;
//        int nBytes;
//        for (i = 0; (nBytes = stream.read(tmpArr)) == BUFFERSIZE; i += BUFFERSIZE){
//            byteArrays.add(tmpArr.clone());
//        }
//        byte[] jarBytes = new byte[i + nBytes];
//        Iterator iter = byteArrays.iterator();
//        i = 0;
//        while (iter.hasNext()) {
//            System.arraycopy(iter.next(), 0, jarBytes, i, BUFFERSIZE);
//            i += BUFFERSIZE;
//        }
//        System.arraycopy(tmpArr, 0, jarBytes, i, nBytes);
        
        byte[] digestBytes = computeDigest(stream, digestGenAlgo);
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
    
    private byte[] computeDigest(InputStream stream, String digestGenAlgo) throws Exception{
        int length;
        byte[] buffer = new byte[BUFFERSIZE];
        MessageDigest msgDigest = MessageDigest.getInstance(digestGenAlgo);
        while ((length = stream.read(buffer)) != -1) {
            msgDigest.update(buffer, 0, length);
        }
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
