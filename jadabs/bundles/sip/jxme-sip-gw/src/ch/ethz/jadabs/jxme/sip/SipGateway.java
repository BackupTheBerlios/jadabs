/*
 * Created on Nov 15, 2004
 *
 */
package ch.ethz.jadabs.jxme.sip;


/**
 * @author andfrei
 * 
 */
public interface SipGateway
{

    void signIn(String localSipURL);
    
    void signOut(String localSipURL);
    
    void sendPublish(String localURI, String status);
    
    void sendSubscribe(String localURL, String buddyURI, boolean EXPIRED);
}
