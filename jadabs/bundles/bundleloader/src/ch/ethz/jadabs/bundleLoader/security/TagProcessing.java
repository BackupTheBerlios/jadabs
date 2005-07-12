/*
 * Created on Jul 8, 2005
 */
package ch.ethz.jadabs.bundleLoader.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Vector;

/**
 * @author otmar
 */
public class TagProcessing {
    
    // possible tags
    static final byte SEQUENCE_CONSTRUCTED = 48;
    static final byte INTEGER = 2;
    static final byte BITSTRING = 3;
    static final byte OBJECTIDENTIFIER = 6;
    
    protected static ASN1Object readObject(byte[] data) throws ParseException{
        ByteArrayInputStream inData = new ByteArrayInputStream(data);
        return readObject(inData);
    }
    
    private static ASN1Object readObject(ByteArrayInputStream inData) throws ParseException{
        try{
            int tag = inData.read();
            if (tag == -1) return null; //EOF reached
            int length = readLength(inData);
            switch (tag){
            case SEQUENCE_CONSTRUCTED:
                Vector resVector = new Vector();
                byte[] objData = new byte[length];
                inData.read(objData);
                ByteArrayInputStream objStream = new ByteArrayInputStream(objData);
                Object seqResult = readObject(objStream);
                while (seqResult != null){
                    resVector.addElement(seqResult);
                    seqResult = readObject(objStream);
                }
                return new ASN1Object(tag, resVector);
            case INTEGER:
            case OBJECTIDENTIFIER:
                byte[] intResult = new byte[length];
                inData.read(intResult);
                return new ASN1Object(tag, intResult);
            case BITSTRING:
                byte[] bStrResult = new byte[length-1];
                //TODO ignoring pad bits
                inData.read();
                inData.read(bStrResult);
                return new ASN1Object(tag, bStrResult);
            }
        } catch (IOException e) {
            throw new ParseException(e);
        }
        return null;
    }
    
    private static int readLength(ByteArrayInputStream inData) throws ParseException {
	    int length = inData.read();
	    if (length < 0)
	        throw new ParseException("EOF found when length expected");
	
	    if (length == 0x80)
	        return -1;      // indefinite-length encoding
	
	    if (length > 127) {
	        int size = length & 0x7f;
	        if (size > 4)
	            throw new ParseException("DER length more than 4 bytes");
	        
	        length = 0;
	        for (int i = 0; i < size; i++) {
	            int next = inData.read();
	            if (next < 0)
	                throw new ParseException("EOF found reading length");
	
	            length = (length << 8) + next;
	        }
	        
	        if (length < 0)
	            throw new ParseException("corrupted steam - negative length found");
	    }
	    return length;
	}

}
