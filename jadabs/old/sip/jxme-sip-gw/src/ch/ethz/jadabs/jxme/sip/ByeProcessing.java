/*
 * ByeProcessing.java
 *
 * Created on September 25, 2002, 11:29 PM
 */

package ch.ethz.jadabs.jxme.sip;

import javax.sip.ClientTransaction;
import javax.sip.ServerTransaction;
import javax.sip.SipProvider;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

/**
 * 
 * @author olivier, andfrei
 * @version 1.0
 */
public class ByeProcessing
{
    private Logger LOG = Logger.getLogger(ByeProcessing.class);
    
    private SipGatewayImpl sipgw;

    private int cseq;

    /** Creates new IMByeProcessing */
    public ByeProcessing(SipGatewayImpl sipgw)
    {
        this.sipgw = sipgw;
        cseq = 0;
    }

    public void processBye(Request request, ServerTransaction serverTransaction)
    {
        try
        {
            LOG.debug("DEBUG: IMByeProcessing, Processing BYE in progress...");

            MessageFactory messageFactory = sipgw.getMessageFactory();
            SipProvider sipProvider = sipgw.getSipProvider();
//            InstantMessagingGUI instantMessagingGUI = sipgw.getInstantMessagingGUI();
//            ListenerInstantMessaging listenerInstantMessaging = instantMessagingGUI.getListenerInstantMessaging();
//            ChatSessionManager chatSessionManager = listenerInstantMessaging.getChatSessionManager();
//            ChatSession chatSession = null;
//            String buddy = IMUtilities.getKey(request, "From");
//            if (chatSessionManager.hasAlreadyChatSession(buddy))
//            {
//                chatSession = chatSessionManager.getChatSession(buddy);
//                chatSessionManager.removeChatSession(buddy);
//                //chatSession.setExitedSession(true,"Your contact has exited
//                // the session");
//            } else
//            {
//                LOG.debug("DEBUG: IMByeProcessing, processBye(), no active chatSession");
//            }

            // Send an OK
            Response response = messageFactory.createResponse(Response.OK, request);
            serverTransaction.sendResponse(response);
            LOG.debug("DEBUG: IMByeProcessing, processBye(), OK replied to the BYE");

            LOG.debug("DEBUG: IMByeProcessing, Processing BYE completed...");
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void processOK(Response responseCloned, ClientTransaction clientTransaction)
    {
        LOG.debug("Processing OK for BYE in progress...");
        LOG.debug("Processing OK for BYE completed...");
    }

    public void sendBye(String localSipURL, String remoteSipURL)
    {
        LOG.debug("Sending a BYE in progress to " + remoteSipURL);
        
        // Send a Bye only if there were exchanged messages!!!
//        if (chatSession.isEstablishedSession())
//        {
//            try
//            {
//                LOG.debug("Sending a BYE in progress to " + remoteSipURL);
//
//                int proxyPort = sipgw.getProxyPort();
//                String proxyAddress = sipgw.getProxyAddress();
//                String imProtocol = sipgw.getIMProtocol();
//
//                SipStack sipStack = sipgw.getSipStack();
//                SipProvider sipProvider = sipgw.getSipProvider();
//                MessageFactory messageFactory = sipgw.getMessageFactory();
//                HeaderFactory headerFactory = sipgw.getHeaderFactory();
//                AddressFactory addressFactory = sipgw.getAddressFactory();
//
//                // Request-URI:
//                SipURI requestURI = addressFactory.createSipURI(null, localSipURL);
//
//                javax.sip.Dialog dialog = chatSession.getDialog();
//
//                // Call-ID:
//                CallIdHeader callIdHeader = dialog.getCallId();
//
//                // CSeq:
//                cseq++;
//                CSeqHeader cseqHeader = headerFactory.createCSeqHeader(cseq, "BYE");
//
//                // To header:
//                String schemeData = "NOT SET";
//                //=IMUserAgent.getBuddyParsedMinusSIP("");
//                Address toAddress = addressFactory.createAddress(schemeData);
//                ToHeader toHeader = headerFactory.createToHeader(toAddress, null);
//
//                // From Header:
//                Address fromAddress = addressFactory.createAddress(localSipURL);
//                FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, null);
//
//                //  Via header
//                String branchId = Utils.generateBranchId();
//                ViaHeader viaHeader = headerFactory.createViaHeader(sipgw.getIMAddress(), sipgw.getIMPort(), imProtocol,
//                        branchId);
//                Vector viaList = new Vector();
//                viaList.addElement(viaHeader);
//
//                // MaxForwards header:
//                MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(10);
//
//                Request request = messageFactory.createRequest(requestURI, "BYE", callIdHeader, cseqHeader, fromHeader,
//                        toHeader, viaList, maxForwardsHeader);
//
//                // Contact header:
//                Address contactAddress = addressFactory.createAddress(sipgw.getIMAddress());
//                SipURI sipURI = (SipURI) contactAddress.getURI();
//                sipURI.setPort(sipgw.getIMPort());
//                sipURI.setTransportParam(sipgw.getIMProtocol());
//                ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
//                request.setHeader(contactHeader);
//
//                // ProxyAuthorization header if not null:
//                ProxyAuthorizationHeader proxyAuthHeader = sipgw.getProxyAuthorizationHeader();
//                if (proxyAuthHeader != null)
//                    request.setHeader(proxyAuthHeader);
//
//                ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(request);
//
//                clientTransaction.sendRequest();
//                LOG.debug("BYE sent:\n" + request);
//
//            } catch (Exception ex)
//            {
//                ex.printStackTrace();
//            }
//        } else
//        {
//            LOG.debug("BYE not sent because of no exchanged messages!!!");
//        }
    }

}