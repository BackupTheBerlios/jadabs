/*
 * Created on Nov 16, 2004
 *
 */
package ch.ethz.jadabs.jxme.im;


/**
 * @author andfrei
 * 
 */
public class IMException extends Throwable
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
