/*
 * Created on Jul 8, 2005
 */
package ch.ethz.jadabs.bundleLoader.security;

/**
 * @author otmar
 */
public class ParseException extends Exception {

    public ParseException(){
        super();
    }
    
    public ParseException(String text){
        super(text);
    }
    
    public ParseException(Throwable cause){
        super(cause.getMessage());
    }
}
