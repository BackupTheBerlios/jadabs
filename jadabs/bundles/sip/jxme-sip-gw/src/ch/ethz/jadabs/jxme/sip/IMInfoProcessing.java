/*
 * IMInfoProcessing.java
 *
 * Created on September 26, 2002, 12:14 AM
 */

package ch.ethz.jadabs.jxme.sip;

import gov.nist.sip.instantmessaging.ChatSession;
import gov.nist.sip.instantmessaging.ChatSessionManager;
import gov.nist.sip.instantmessaging.DebugIM;
import gov.nist.sip.instantmessaging.IMUtilities;
import gov.nist.sip.instantmessaging.InfoTimer;
import gov.nist.sip.instantmessaging.InstantMessagingGUI;
import gov.nist.sip.instantmessaging.ListenerInstantMessaging;

import javax.sip.ServerTransaction;
import javax.sip.SipProvider;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

/**
 * 
 * @author olivier
 * @version 1.0
 */
public class IMInfoProcessing
{

    private IMUserAgent imUA;

    /** Creates new IMInfoProcessing */
    public IMInfoProcessing(IMUserAgent imUA)
    {
        this.imUA = imUA;
    }

    public void processInfo(Request request, ServerTransaction serverTransaction)
    {
        try
        {

            DebugIM.println("Process INFO in progress...");
            MessageFactory messageFactory = imUA.getMessageFactory();
            SipProvider sipProvider = imUA.getSipProvider();
            InstantMessagingGUI instantMessagingGUI = imUA.getInstantMessagingGUI();
            ListenerInstantMessaging listenerInstantMessaging = instantMessagingGUI.getListenerInstantMessaging();
            ChatSessionManager chatSessionManager = listenerInstantMessaging.getChatSessionManager();
            ChatSession chatSession = null;
            String fromURL = IMUtilities.getKey(request, "From");
            if (chatSessionManager.hasAlreadyChatSession(fromURL))
            {
                chatSession = chatSessionManager.getChatSession(fromURL);
                // WE have to parse the XML info body and notify the
                // user by the chatSession and ChatFrame!!
                Object content = request.getContent();
                String text = null;
                if (content instanceof String)
                    text = (String) content;
                else if (content instanceof byte[])
                {
                    text = new String((byte[]) content);
                } else
                {
                }
                if (text != null)
                {
                    //String infoParsed=infoParser.parseXMLInfoBody(text);
                    chatSession.setInfo("Your contact is typing...");
                    InfoTimer infoTimer = new InfoTimer(chatSession);
                    java.util.Timer timer = new java.util.Timer();
                    timer.schedule(infoTimer, 2000);
                }
            } else
            {
                // Nothing to update!!!
            }

            // Send an OK
            Response response = messageFactory.createResponse(Response.OK, request);
            serverTransaction.sendResponse(response);
            DebugIM.println("OK replied to INFO");

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}