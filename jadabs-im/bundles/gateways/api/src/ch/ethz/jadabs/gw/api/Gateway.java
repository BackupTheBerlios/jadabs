/*
 * Created on Nov 15, 2004
 *
 */
package ch.ethz.jadabs.gw.api;


/**
 * @author andfrei
 * 
 */
public interface Gateway
{

    void signIn();
    
    void signOut();
    
//    void sendPublish(String localURI, String status);
//    
//    void sendSubscribe(String localURL, String buddyURI, boolean EXPIRED);
}
