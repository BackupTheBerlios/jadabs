package ch.ethz.jadabs.jxme.chat.j2se;

/**
 * This is the callback interface for the ChatCommunication Service 
 * 
 * @author Ren&eacute; M&uul;ller
 */
public interface ChatListener
{

    public final static String EVENT_JOIN = "join";

    public final static String EVENT_LEAVE = "leave";

    public final static String EVENT_RECEIVED = "received";

    public final static String EVENT_SENT = "sent";

    /**
     * BlueChat chat action handler.
     * 
     * @param action
     *            must be <code>EVENT_JOIN</code>, <code>EVENT_LEAVE</code>,
     *            <code>EVENT_RECEIVED</code> or <code>EVENT_SENT</code>
     * @param param String parameter of the action: 
     *            if <code>EVENT_JOIN</code> or </code>EVENT_LEAVE</code>
     *            then param is equal to the nickname, if <code>EVENT_SENT</code>
     *            or <code>EVENT_RECEIVED</code> then param is equal to 
     *            "nickname: message"
     */
    public void handleAction(String action, String param);
}