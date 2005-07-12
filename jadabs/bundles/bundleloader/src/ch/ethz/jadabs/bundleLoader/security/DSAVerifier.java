/*
 * Created on Jul 7, 2005
 */
package ch.ethz.jadabs.bundleLoader.security;

import java.util.Vector;
import java.math.BigInteger;

import org.apache.log4j.Logger;

/**
 * @author otmar
 */
public class DSAVerifier {
    private static Logger LOG = Logger.getLogger(DSAVerifier.class);
    
    DSASubjectPublicKey key;

    public DSAVerifier(DSASubjectPublicKey key) throws Exception {
        this.key = key;
    }

    /**
     * return true if the value r and s represent a DSA signature for
     * the passed in message for standard DSA the message should be a
     * SHA-1 hash of the real message to be verified.
     */
    public boolean verifySignature(
        byte[]      message,
        BigInteger  r,
        BigInteger  s) throws Exception
    {
        BigInteger      m = new BigInteger(1, message);
        BigInteger      zero = BigInteger.valueOf(0);

        if (zero.compareTo(r) >= 0 || key.getQ().compareTo(r) <= 0)
        {
            return false;
        }

        if (zero.compareTo(s) >= 0 || key.getQ().compareTo(s) <= 0)
        {
            return false;
        }

        BigInteger  w = s.modInverse(key.getQ());

        BigInteger  u1 = m.multiply(w).mod(key.getQ());
        BigInteger  u2 = r.multiply(w).mod(key.getQ());

        u1 = key.getG().modPow(u1, key.getP());
        u2 = key.getY().modPow(u2, key.getP());

        BigInteger  v = u1.multiply(u2).mod(key.getP()).mod(key.getQ());
        
        LOG.debug("Signature ok: " + v.equals(r));

        return v.equals(r);
    }
    
    public boolean virfySignature(byte[] message, byte[] signature) throws Exception {
        BigInteger[]    sig;
        sig = decode(signature);
        return verifySignature(message, sig[0], sig[1]);
    }
    
    private BigInteger[] decode(byte[]  encoding) throws ParseException {
    
//        ByteArrayInputStream    bIn = new ByteArrayInputStream(encoding);
//        ASN1InputStream         aIn = new ASN1InputStream(bIn);
//        ASN1Sequence            s = (ASN1Sequence)aIn.readObject();
        
        ASN1Object result = TagProcessing.readObject(encoding);
        
        // root tag: r | s
        if (result.getTag() != TagProcessing.SEQUENCE_CONSTRUCTED)
            throw new ParseException("found unexpected tag");
        ASN1Object rTag = (ASN1Object)((Vector)result.getData()).elementAt(0);
        ASN1Object sTag = (ASN1Object)((Vector)result.getData()).elementAt(1);

        BigInteger[]            sig = new BigInteger[2];

        sig[0] = new BigInteger((byte[])rTag.getData());
        sig[1] = new BigInteger((byte[])sTag.getData());

        return sig;
    }
    
}