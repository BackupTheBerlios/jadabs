/*
 * Created on Nov 15, 2004
 *
 */
package ch.ethz.jadabs.jxme.im;

/**
 * @author andfrei
 * 
 */
public interface IMService
{

    public static final int IM_STATUS_ONLINE = 1;
    
    public static final int IM_STATUS_BUSY = 2;
  
    public static final int IM_STATUS_OFFLINE = 3;
    
    public static final String SIP_ADDRESS_ANY = "any";
    
    
    int getStatus();
    
    void register(IMListener imlistener, int status);
    
    void unregister();
    
    void sendMessage(String tosipaddress, String message)  throws IMException;
    
    NeighbourTuple[] getNeighbours();
    
}
