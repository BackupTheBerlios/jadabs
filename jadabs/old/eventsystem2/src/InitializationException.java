/*
 * Created on Jul 2, 2003
 *
 * $Id: InitializationException.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.jadabs.eventsystem;

/**
 * InitializationException is throwen when an Event can't be initialized
 * correctly.
 * 
 * @author andfrei
 */
public class InitializationException extends Exception
{

    public InitializationException(String message, Throwable cause)
    {
        super(message);
    }

}