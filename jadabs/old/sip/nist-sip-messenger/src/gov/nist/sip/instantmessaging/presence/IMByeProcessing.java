/*
 * IMByeProcessing.java
 *
 * Created on September 25, 2002, 11:29 PM
 */

package gov.nist.sip.instantmessaging.presence;

import gov.nist.javax.sip.Utils;
import gov.nist.sip.instantmessaging.ChatSession;
import gov.nist.sip.instantmessaging.ChatSessionManager;
import gov.nist.sip.instantmessaging.DebugIM;
import gov.nist.sip.instantmessaging.IMUtilities;
import gov.nist.sip.instantmessaging.InstantMessagingGUI;
import gov.nist.sip.instantmessaging.ListenerInstantMessaging;

import java.util.Vector;

import javax.sip.ClientTransaction;
import javax.sip.ServerTransaction;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

/**
 * 
 * @author olivier
 * @version 1.0
 */
public class IMByeProcessing
{

    private IMUserAgent imUA;

    private int cseq;

    /** Creates new IMByeProcessing */
    public IMByeProcessing(IMUserAgent imUA)
    {
        this.imUA = imUA;
        cseq = 0;
    }

    public void processBye(Request request, ServerTransaction serverTransaction)
    {
        try
        {
            DebugIM.println("DEBUG: IMByeProcessing, Processing BYE in progress...");

            MessageFactory messageFactory = imUA.getMessageFactory();
            SipProvider sipProvider = imUA.getSipProvider();
            InstantMessagingGUI instantMessagingGUI = imUA.getInstantMessagingGUI();
            ListenerInstantMessaging listenerInstantMessaging = instantMessagingGUI.getListenerInstantMessaging();
            ChatSessionManager chatSessionManager = listenerInstantMessaging.getChatSessionManager();
            ChatSession chatSession = null;
            String buddy = IMUtilities.getKey(request, "From");
            if (chatSessionManager.hasAlreadyChatSession(buddy))
            {
                chatSession = chatSessionManager.getChatSession(buddy);
                chatSessionManager.removeChatSession(buddy);
                //chatSession.setExitedSession(true,"Your contact has exited
                // the session");
            } else
            {
                DebugIM.println("DEBUG: IMByeProcessing, processBye(), no active chatSession");
            }

            // Send an OK
            Response response = messageFactory.createResponse(Response.OK, request);
            serverTransaction.sendResponse(response);
            DebugIM.println("DEBUG: IMByeProcessing, processBye(), OK replied to the BYE");

            DebugIM.println("DEBUG: IMByeProcessing, Processing BYE completed...");
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void processOK(Response responseCloned, ClientTransaction clientTransaction)
    {
        DebugIM.println("Processing OK for BYE in progress...");
        DebugIM.println("Processing OK for BYE completed...");
    }

    public void sendBye(String localSipURL, String remoteSipURL, ChatSession chatSession)
    {
        // Send a Bye only if there were exchanged messages!!!
        if (chatSession.isEstablishedSession())
        {
            try
            {
                DebugIM.println();
                DebugIM.println("Sending a BYE in progress to " + remoteSipURL);

                int proxyPort = imUA.getProxyPort();
                String proxyAddress = imUA.getProxyAddress();
                String imProtocol = imUA.getIMProtocol();

                SipStack sipStack = imUA.getSipStack();
                SipProvider sipProvider = imUA.getSipProvider();
                MessageFactory messageFactory = imUA.getMessageFactory();
                HeaderFactory headerFactory = imUA.getHeaderFactory();
                AddressFactory addressFactory = imUA.getAddressFactory();

                // Request-URI:
                SipURI requestURI = addressFactory.createSipURI(null, localSipURL);

                javax.sip.Dialog dialog = chatSession.getDialog();

                // Call-ID:
                CallIdHeader callIdHeader = dialog.getCallId();

                // CSeq:
                cseq++;
                CSeqHeader cseqHeader = headerFactory.createCSeqHeader(cseq, "BYE");

                // To header:
                String schemeData = "NOT SET";
                //=IMUserAgent.getBuddyParsedMinusSIP("");
                Address toAddress = addressFactory.createAddress(schemeData);
                ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

                // From Header:
                Address fromAddress = addressFactory.createAddress(localSipURL);
                FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, null);

                //  Via header
                String branchId = Utils.generateBranchId();
                ViaHeader viaHeader = headerFactory.createViaHeader(imUA.getIMAddress(), imUA.getIMPort(), imProtocol,
                        branchId);
                Vector viaList = new Vector();
                viaList.addElement(viaHeader);

                // MaxForwards header:
                MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(10);

                Request request = messageFactory.createRequest(requestURI, "BYE", callIdHeader, cseqHeader, fromHeader,
                        toHeader, viaList, maxForwardsHeader);

                // Contact header:
                Address contactAddress = addressFactory.createAddress(imUA.getIMAddress());
                SipURI sipURI = (SipURI) contactAddress.getURI();
                sipURI.setPort(imUA.getIMPort());
                sipURI.setTransportParam(imUA.getIMProtocol());
                ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
                request.setHeader(contactHeader);

                // ProxyAuthorization header if not null:
                ProxyAuthorizationHeader proxyAuthHeader = imUA.getProxyAuthorizationHeader();
                if (proxyAuthHeader != null)
                    request.setHeader(proxyAuthHeader);

                ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(request);

                clientTransaction.sendRequest();
                DebugIM.println("BYE sent:\n" + request);
                DebugIM.println();

            } catch (Exception ex)
            {
                ex.printStackTrace();
            }
        } else
        {
            DebugIM.println("BYE not sent because of no exchanged messages!!!");
        }
    }

}