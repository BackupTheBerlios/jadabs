/*
 * $Id: ChatCommunication.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package ch.ethz.jadabs.jxme.chat;

import java.io.IOException;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.EndpointService;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.MalformedURIException;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;


/**
 * This class manages the communication layer of the chat 
 * application over Jadabs-Jxme. 
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class ChatCommunication implements Listener 
{
    /** Log4j Logger to be used */
    private Logger LOG = Logger.getLogger("ChatCommunication");    
    
    /** Listener that gets notified if something happens in the chat */
    private ChatListener chatListener;
    
    /** name of the local chat user in the chat */
    private String nickname;
    
    /** Endpoint service that will be used for communication */
    private EndpointService endptsvc;
    
    /** Endpoint to send broadcast messages to */
    private EndpointAddress endpoint;
    
    /**
     * Constructor for the chat communication layer 
     * @param listener listener that will be called if something happens in the chat
     * @param endptsvc EndpointService that will be used for communication 
     */
    public ChatCommunication(ChatListener listener, EndpointService endptsvc) 
    {
        this.chatListener = listener;
        this.endptsvc = endptsvc;
        try {
            this.endpoint = new EndpointAddress("btspp", "anybody", -1, "jxmechat");
        } catch (MalformedURIException e) {
            LOG.error("invalid endpoint: "+e.getMessage());
        }
    }
    
    /**
     * 
     * @param nickname
     */
    public void enterChat(String nickname)
    {
        this.nickname = nickname;
        if (LOG.isDebugEnabled()) {
            LOG.debug("enterChat(\""+nickname+"\")");
        }
        
        // send JOIN message
        Element[] elm = new Element[2];
        elm[0] = new Element("type", "join", Message.JXTA_NAME_SPACE);
        elm[1] = new Element("user", nickname, Message.JXTA_NAME_SPACE);
        try { 
            endptsvc.propagate(elm, endpoint);
        } catch (IOException e) {
            LOG.error("cannot send JOIN message: "+e.getMessage());
        }
    }
    
    /**
     * Called when the local user decides to leave the chat.
     * 
     */
    public void leaveChat() 
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("leaveChat()");
        }
        
        // send LEAVE message
        Element[] elm = new Element[2];
        elm[0] = new Element("type", "leave", Message.JXTA_NAME_SPACE);
        elm[1] = new Element("user", nickname, Message.JXTA_NAME_SPACE);
        try {
            endptsvc.propagate(elm, endpoint); 
        } catch (IOException e) {
            LOG.error("cannot send LEAVE message: "+e.getMessage());
        }
    }
    
    /**
     * Send message into chat
     * @param message message to send
     */
    public void sendMessage(String message) 
    {
        if (LOG.isDebugEnabled()) {
            LOG.debug("sendMessage(\""+message+"\")");
        }
        
        // send CHAT message
        Element[] elm = new Element[3];
        elm[0] = new Element("type", "chat", Message.JXTA_NAME_SPACE);
        elm[1] = new Element("user", nickname, Message.JXTA_NAME_SPACE);   
        elm[2] = new Element("text", message, Message.JXTA_NAME_SPACE);
        try {
            endptsvc.propagate(elm, endpoint);
        } catch(IOException e) {
            LOG.error("cannot send chat message: "+e.getMessage());
        }
    }
    
    
    //
    // Implementation of ch.ethz.ch.jadabs.jxme.Listener
    //
    
    /** 
     * Called by the JXME system if a new message arrives at this endpoint.
     * @param message message that was deceived
     * @param listenerId Id String of this handler 
     * @see ch.ethz.jadabs.jxme.Listener#handleMessage(ch.ethz.jadabs.jxme.Message, java.lang.String)
     */
    public void handleMessage(Message message, String listenerId)
    {
        LOG.debug("handleMessage(...)");
        if (message.getElementCount() < 2) {
            LOG.error("invalid message, less than two elements");
            return;
        }
        Element type = message.getElement(0);
        Element user = message.getElement(1);
        if (!type.getName().equals("type")) {
            LOG.error("invalid name in element 'type'");
            return;
        }               
        if (!user.getName().equals("user")) {
            LOG.error("invalid name in element 'user'");
            return;
        }
        String typeStr = new String(type.getData());
        String userStr = new String(user.getData());
        if (typeStr.equals("join")) {
            // user joined the chat
            chatListener.handleAction(ChatListener.EVENT_JOIN, userStr);
        } else if (typeStr.equals("leave")) {
            // user left the chat
            chatListener.handleAction(ChatListener.EVENT_LEAVE, userStr);
        } else if (typeStr.equals("chat")) {
            // chat message received 
            if (message.getElementCount() < 3) {
                LOG.error("invalid chat message, less than three elements");
                return;
            }
            Element chatMsg = message.getElement(2);
            if (!chatMsg.getName().equals("text")) {
               LOG.error("invalid name in element 'text'");
               return;
            }
            String textStr = new String(chatMsg.getData());
            chatListener.handleAction(ChatListener.EVENT_RECEIVED, userStr+": "+textStr);
            
        } else {
            LOG.error("invalid value in 'type' element, message type not recognized.");
        }       
    }
    
    /**
     * 
     * @see ch.ethz.jadabs.jxme.Listener#handleSearchResponse(ch.ethz.jadabs.jxme.NamedResource)
     */
    public void handleSearchResponse(NamedResource namedResource)
    {
        LOG.debug("called handleSearchResponse: not implemented");        
    }

    /**
     * Get nickname of the local chat user
     * @return string with nickname of the local chat user
     */
    public String getNickname() 
    {
        return nickname;
    }
    
    /** 
     * Set nickname of the local chat user 
     * @param name string with nickname of the local chat user 
     */
    public void setNickname(String name) 
    {
        nickname = name;
    }
}