package ch.ethz.jadabs.jxme.chat.j2se;

/**
 * This class represents entries to the chat log.
 * 
 * @author Ren&eacute; M&uuml;ller
 */

public class ChatEntry
{       
    /**
     * indicate the nick name of the sender 
     * (empty string if local system message)
     */
    private String sender;

    /** the message content */
    private String message;

    /**
     * Create new ChatEntry with a user and the corresonding
     * @param sender user that sent the message 
     * @param msg the message itself
     */
    public ChatEntry(String sender, String msg)
    {
        this.sender = sender;
        this.message = msg;
    }
    
    /**
     * Create new System-ChatEntry (without user)
     * This entry is used for local system messages 
     * @param msg message text of this entry 
     */ 
    public ChatEntry(String msg)
    {
        this.sender = null;
        this.message = msg;
    }

    /**
     * Convert this ChatEntry into a String
     * @return String concatenation of the ChatEntry 
     */
    public String toString()
    {
        if (sender == null) {
            return message;
        } else {
            return sender+": "+message;
        }
    }
}