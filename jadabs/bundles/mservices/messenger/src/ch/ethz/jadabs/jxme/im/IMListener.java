/*
 * Created on Nov 15, 2004
 *
 */
package ch.ethz.jadabs.jxme.im;


/**
 * @author andfrei
 * 
 */
public interface IMListener
{

    void imRegistered(String sipaddress, int status);
        
    void imUnregistered(String sipaddress);
    
    void process(String sipaddress, String msg);
}
