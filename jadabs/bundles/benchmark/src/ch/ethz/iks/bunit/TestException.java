/*
 * Created on Apr 28, 2004
 *
 */
package ch.ethz.iks.bunit;


/**
 * @author andfrei
 * 
 */
public class TestException extends Exception
{
    
    public TestException(String message)
    {
        super(message);
    }
    
    public TestException(String message, Throwable exception)
    {
        super(message, exception);
    }
}
