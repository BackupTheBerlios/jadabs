/*
 * InfoProcessing.java
 *
 * Created on September 26, 2002, 12:14 AM
 */

package ch.ethz.jadabs.jxme.sip;


import javax.sip.ServerTransaction;
import javax.sip.message.Request;

import org.apache.log4j.Logger;

/**
 * 
 * @author olivier, andfrei
 * @version 1.0
 */
public class InfoProcessing
{
    private Logger LOG = Logger.getLogger(InfoProcessing.class);
    
    private SipGatewayImpl sipgw;

    
    public InfoProcessing(SipGatewayImpl sipgw)
    {
        this.sipgw = sipgw;
    }

    public void processInfo(Request request, ServerTransaction serverTransaction)
    {
        LOG.debug("Process INFO in progress...");
        
//        try
//        {

//            LOG.debug("Process INFO in progress...");
//            MessageFactory messageFactory = sipgw.getMessageFactory();
//            SipProvider sipProvider = sipgw.getSipProvider();
//            InstantMessagingGUI instantMessagingGUI = sipgw.getInstantMessagingGUI();
//            ListenerInstantMessaging listenerInstantMessaging = instantMessagingGUI.getListenerInstantMessaging();
//            ChatSessionManager chatSessionManager = listenerInstantMessaging.getChatSessionManager();
//            ChatSession chatSession = null;
//            String fromURL = IMUtilities.getKey(request, "From");
//            if (chatSessionManager.hasAlreadyChatSession(fromURL))
//            {
//                chatSession = chatSessionManager.getChatSession(fromURL);
//                // WE have to parse the XML info body and notify the
//                // user by the chatSession and ChatFrame!!
//                Object content = request.getContent();
//                String text = null;
//                if (content instanceof String)
//                    text = (String) content;
//                else if (content instanceof byte[])
//                {
//                    text = new String((byte[]) content);
//                } else
//                {
//                }
//                if (text != null)
//                {
//                    //String infoParsed=infoParser.parseXMLInfoBody(text);
//                    chatSession.setInfo("Your contact is typing...");
//                    InfoTimer infoTimer = new InfoTimer(chatSession);
//                    java.util.Timer timer = new java.util.Timer();
//                    timer.schedule(infoTimer, 2000);
//                }
//            } else
//            {
//                // Nothing to update!!!
//            }
//
//            // Send an OK
//            Response response = messageFactory.createResponse(Response.OK, request);
//            serverTransaction.sendResponse(response);
//            DebugIM.println("OK replied to INFO");
//
//        } catch (Exception ex)
//        {
//            ex.printStackTrace();
//        }
    }

}