/*
 * Created on Jul 8, 2005
 */
package ch.ethz.jadabs.bundleLoader.security;

import java.util.Vector;
import java.math.BigInteger;

/**
 * @author otmar
 */
public class DSASubjectPublicKey {
    
    static final byte[] algoIdent = {42, -122, 72, -50, 56, 4, 1};
    
    // the public key
    BigInteger y;
    
    // parameters for the DSA algorithm
    BigInteger p;
    BigInteger q;
    BigInteger g;
    
    public DSASubjectPublicKey(byte[] x509publicKeySubj) throws ParseException{
       parseSubjectPublicKey(x509publicKeySubj);
    }
    
    private void parseSubjectPublicKey(byte[] data) throws ParseException{
        ASN1Object result = TagProcessing.readObject(data);
        // root tag: algorithm information | key data
        if (result.getTag() != TagProcessing.SEQUENCE_CONSTRUCTED)
            throw new ParseException("found unexpected tag");
        ASN1Object algInfo = (ASN1Object)((Vector)result.getData()).elementAt(0);
        ASN1Object keyData = (ASN1Object)((Vector)result.getData()).elementAt(1);
        
        // algorithm information: identifier | parameters
        if (algInfo.getTag() != TagProcessing.SEQUENCE_CONSTRUCTED)
            throw new ParseException("found unexpected tag");
        ASN1Object algIdent = (ASN1Object)((Vector)algInfo.getData()).elementAt(0);
        ASN1Object algParams = (ASN1Object)((Vector)algInfo.getData()).elementAt(1);
        // check identifier
        byte[] toCheck = (byte [])algIdent.getData();
        for (int i = 0; i < algoIdent.length; i++) {
            if (algoIdent[i] != toCheck[i])
                throw new ParseException("algorithm identifier doesn't match");
        }
        
        // Algorithm parameters: p | q | g
        if (algParams.getTag() != TagProcessing.SEQUENCE_CONSTRUCTED)
            throw new ParseException("found unexpected tag");
        ASN1Object paramP = (ASN1Object)((Vector)algParams.getData()).elementAt(0);
        ASN1Object paramQ = (ASN1Object)((Vector)algParams.getData()).elementAt(1);
        ASN1Object paramG = (ASN1Object)((Vector)algParams.getData()).elementAt(2);
        if ((paramP.getTag() != TagProcessing.INTEGER)
                || (paramQ.getTag() != TagProcessing.INTEGER)
                || (paramG.getTag() != TagProcessing.INTEGER))
            throw new ParseException("found unexpected tag"); 
        p = new BigInteger((byte[]) paramP.getData());
        q = new BigInteger((byte[]) paramQ.getData());
        g = new BigInteger((byte[]) paramG.getData());
        
        // key data
        if (keyData.getTag() != TagProcessing.BITSTRING)
            throw new ParseException("found unexpected tag");
        ASN1Object keyObj = TagProcessing.readObject((byte[]) keyData.getData());
        y = new BigInteger((byte[]) keyObj.getData());
    }
    
    public BigInteger getP(){
        return p;
    }
    
    public BigInteger getQ(){
        return q;
    }
    
    public BigInteger getG(){
        return g;
    }
    
    public BigInteger getY(){
        return y;
    }
    
}
