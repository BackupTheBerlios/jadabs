/*
 * Created on Jul 6, 2005
 */
package ch.ethz.jadabs.bundleSecurity;

import java.io.IOException;
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
        
        byte[] message = readAllData(stream);
        
        if (digestGenAlgo != null && digest != null){
            byte[] digestBytes = computeDigest(message, digestGenAlgo);
            if (!digest.equals(encoder.encode(digestBytes))) return false;
        }
        return verifySignature(getPublicKey(publicKey, keyGenAlgo), message, decoder.decodeBuffer(signature));
    }
    
    private PublicKey getPublicKey(String pubKeyStr, String keyGenAlgo) throws Exception{
        KeyFactory keyFactory = KeyFactory.getInstance(keyGenAlgo);
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(decoder.decodeBuffer(pubKeyStr));
        return keyFactory.generatePublic(publicKeySpec);
        
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
        sig.update(message);
        return sig.verify(signature);
    }
    
    private byte[] readAllData(InputStream is) throws IOException{
        Vector byteArrays = new Vector();
        byte[] tmpArr = new byte[BUFFERSIZE];
        int i;
        int nBytes;
        for (i = 0; (nBytes = is.read(tmpArr)) == BUFFERSIZE; i += BUFFERSIZE){
            byteArrays.add(tmpArr.clone());
        }
        byte[] byteArr = new byte[i + nBytes];
        Iterator iter = byteArrays.iterator();
        i = 0;
        while (iter.hasNext()) {
            System.arraycopy(iter.next(), 0, byteArr, i, BUFFERSIZE);
            i += BUFFERSIZE;
        }
        System.arraycopy(tmpArr, 0, byteArr, i, nBytes);
        return byteArr;
    }

}
