/*
 * Created on Nov 15, 2004
 *
 */
package ch.ethz.jadabs.im.api;




/**
 * @author andfrei
 * 
 */
public interface IMService {  
//    String getSipAddress();
    
    int getStatus();
    
    void setStatus(int status) ;
    
    String getIMType();
    
    void setIMType(String imtype);
    
    void setListener(IMListener listener);
    
    void connect();
    
    void disconnect();
    
    IMListener getListener();
    
    void sendMessage(String tosipaddress, String message);
    
    IMContact[] getNeighbours();
    
    String[] getReceivers();
    
    IMContact[] getBuddies();
    
    void addSipBuddy(String buddy);
    
    void removeSipBuddy(String buddy);
}
