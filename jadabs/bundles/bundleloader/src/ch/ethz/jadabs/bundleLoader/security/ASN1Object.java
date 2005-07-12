/*
 * Created on Jul 11, 2005
 */
package ch.ethz.jadabs.bundleLoader.security;

/**
 * @author otmar
 */
public class ASN1Object {

    int tag;
    Object data;
    
    public ASN1Object(int tag, Object data){
        this.tag = tag;
        this.data = data;
    }
    
    public int getTag(){
        return tag;
    }
    
    public Object getData(){
        return data;
    }
}
