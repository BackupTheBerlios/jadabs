/*
 * Created on Nov 16, 2004
 *
 */
package ch.ethz.jadabs.im.api;


/**
 * @author andfrei
 * 
 */
public class IMException extends Exception
{

    public IMException(String message)
    {
        super(message);
    }
    
    public IMException(String message, Exception e)
    {
        super(message, e);
    }
}
