/*
 * IMSubscribeProcessing.java
 *
 * Created on September 26, 2002, 12:13 AM
 */

package ch.ethz.jadabs.jxme.sip;

import gov.nist.javax.sip.Utils;

import java.util.Vector;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.ServerTransaction;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

/**
 * 
 * @author olivier
 * @version 1.0
 */
public class SubscribeProcessing
{
    private Logger LOG = Logger.getLogger(SubscribeProcessing.class);
    
    private SipGatewayImpl sipgw;

    private int callIdCounter;

    private int cseq;

    /** Creates new IMSubscribeProcessing */
    public SubscribeProcessing(SipGatewayImpl sipgw)
    {
        this.sipgw = sipgw;
        callIdCounter = 0;
        cseq = 0;
    }

    public void processOK(Response responseCloned, ClientTransaction clientTransaction)
    {
        LOG.debug("Processing OK for SUBSCRIBE in progress...");
        
//        try
//        {
//            LOG.debug("Processing OK for SUBSCRIBE in progress...");
//
//            ExpiresHeader expiresHeader = (ExpiresHeader) responseCloned.getHeader(ExpiresHeader.NAME);
//            if (expiresHeader != null && expiresHeader.getExpires() == 0)
//            {
//                LOG.debug("DEBUG, IMSubscribeProcessing, processOK(), we got" + " the OK for the unsubscribe...");
//            } else
//            {
//
//                // We have to create or update the presentity!
//                PresenceManager presenceManager = sipgw.getPresenceManager();
//                String presentityURL = IMUtilities.getKey(responseCloned, "To");
//
//                Dialog dialog = clientTransaction.getDialog();
//                if (dialog != null)
//                    presenceManager.addPresentity(presentityURL, responseCloned, dialog);
//                else
//                {
//                    DebugIM.println("ERROR, IMSubscribeProcessing, processOK(), the"
//                            + " dialog for the SUBSCRIBE we sent is null!!!" + " No presentity added....");
//
//                }
//
//                // WE have to create a new Buddy in the GUI!!!
//                InstantMessagingGUI imGUI = sipgw.getInstantMessagingGUI();
//                BuddyList buddyList = imGUI.getBuddyList();
//                if (!buddyList.hasBuddy(presentityURL))
//                {
//                    buddyList.addBuddy(presentityURL, "offline");
//                } else
//                {
//                    DebugIM.println("The buddy is already in the Buddy list...");
//                }
//            }
//            LOG.debug("Processing OK for SUBSCRIBE completed...");
//        } catch (Exception ex)
//        {
//            ex.printStackTrace();
//        }
    }

    public void processSubscribe(Request request, ServerTransaction serverTransaction)
    {
        LOG.debug("Processing SUBSCRIBE in progress ");
//        try
//        {
//
//            MessageFactory messageFactory = sipgw.getMessageFactory();
//            HeaderFactory headerFactory = sipgw.getHeaderFactory();
//            AddressFactory addressFactory = sipgw.getAddressFactory();
//            Dialog dialog = serverTransaction.getDialog();
//
//            //********** Terminating subscriptions **********
//            ExpiresHeader expiresHeader = (ExpiresHeader) request.getHeader(ExpiresHeader.NAME);
//            if (expiresHeader != null && expiresHeader.getExpires() == 0)
//            {
//                if (dialog != null)
//                {
//                    //Terminating an existing subscription
//                    Response response = messageFactory.createResponse(Response.OK, request);
//                    serverTransaction.sendResponse(response);
//                    NotifyProcessing imNotifyProcessing = sipgw.getNotifyProcessing();
//                    imNotifyProcessing.sendNotify(response, null, dialog);
//                    return;
//                } else
//                {
//                    //Terminating an non existing subscription
//                    Response response = messageFactory.createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST,
//                            request);
//                    serverTransaction.sendResponse(response);
//                    return;
//                }
//            }
//
//            //********** Non-terminating subscriptions ************
//
//            //send a 202 Accepted while waiting for authorization from user
//            Response response = messageFactory.createResponse(Response.ACCEPTED, request);
//            // Tag:
//            ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
//            if (toHeader.getTag() == null)
//                toHeader.setTag(new Integer((int) (Math.random() * 10000)).toString());
//            serverTransaction.sendResponse(response);
//            DebugIM.println(response.toString());
//
//            // We have to ask the user to authorize the guy to be in his buddy
//            // list
//            String presentityURL = IMUtilities.getKey(request, "From");
//            SipProvider sipProvider = sipgw.getSipProvider();
//            InstantMessagingGUI imGUI = sipgw.getInstantMessagingGUI();
//            boolean authorization = imGUI.getAuthorizationForBuddy(presentityURL);
//            if (authorization)
//            {
//                DebugIM.println("DEBUG: SubscribeProcessing, processSubscribe(), " + " Response 202 Accepted sent.");
//
//                // We have to create or update the subscriber!
//                PresenceManager presenceManager = sipgw.getPresenceManager();
//                String subscriberURL = IMUtilities.getKey(request, "From");
//
//                if (dialog != null)
//                    presenceManager.addSubscriber(subscriberURL, response, dialog);
//                else
//                {
//                    DebugIM.println("ERROR, IMSubscribeProcessing, processSubscribe(), the"
//                            + " dialog for the SUBSCRIBE we received is null!!! No subscriber added....");
//                    return;
//                }
//
//                // Let's see if this buddy is in our buddy list
//                // if not let's ask to add him!
//                BuddyList buddyList = imGUI.getBuddyList();
//                ListenerInstantMessaging listenerIM = imGUI.getListenerInstantMessaging();
//                if (!buddyList.hasBuddy(subscriberURL))
//                {
//                    // Let's ask:
//                    listenerIM.addContact(subscriberURL);
//                }
//
//                /** ********************** send NOTIFY ************************* */
//                // We send a NOTIFY for any of our status but offline
//                String localStatus = listenerIM.getLocalStatus();
//                if (!localStatus.equals("offline"))
//                {
//                    NotifyProcessing imNotifyProcessing = sipgw.getNotifyProcessing();
//                    Subscriber subscriber = presenceManager.getSubscriber(subscriberURL);
//                    //Response okSent=subscriber.getOkSent();
//
//                    subscriberURL = subscriber.getSubscriberName();
//
//                    String contactAddress = sipgw.getIMAddress() + ":" + sipgw.getIMPort();
//
//                    String subStatus = listenerIM.getLocalStatus();
//                    String status = null;
//                    if (subStatus.equals("offline"))
//                        status = "closed";
//                    else
//                        status = "open";
//                    String xmlBody = imNotifyProcessing.xmlPidfParser.createXMLBody(status, subStatus, subscriberURL,
//                            contactAddress);
//                    imNotifyProcessing.sendNotify(response, xmlBody, dialog);
//
//                }
//            } else
//            {
//                //User did not authorize subscription. Terminate it!
//                DebugIM.println("DEBUG, IMSubsribeProcessing, processSubscribe(), " + " Subscription declined!");
//                DebugIM.println("DEBUG, IMSubsribeProcessing, processSubscribe(), "
//                        + " Sending a Notify with Subscribe-state=terminated");
//
//                NotifyProcessing imNotifyProcessing = sipgw.getNotifyProcessing();
//                if (dialog != null)
//                {
//                    imNotifyProcessing.sendNotify(response, null, dialog);
//                    DebugIM.println("DEBUG, IMSubsribeProcessing, processSubscribe(), "
//                            + " Sending a Notify with Subscribe-state=terminated");
//                } else
//                {
//                    DebugIM.println("ERROR, IMSubscribeProcessing, processSubscribe(), the"
//                            + " dialog for the SUBSCRIBE we received is null!!! \n" + "   No terminating Notify sent");
//
//                }
//                imNotifyProcessing.sendNotify(response, null, dialog);
//
//            }
//        } catch (Exception ex)
//        {
//            ex.printStackTrace();
//        }
    }

//    public void sendSubscribeToAllPresentities(Vector buddies, boolean EXPIRED)
//    {
//        try
//        {
//            DebugIM.println("DebugIM, IMSubscribeProcessing, sendSubscribeToAllPresentities(),"
//                    + " we have to subscribe to our buddies: let's send a SUBSCRIBE for each ones.");
//            for (int i = 0; i < buddies.size(); i++)
//            {
//                BuddyTag buddyTag = (BuddyTag) buddies.elementAt(i);
//
//                String buddyURI = buddyTag.getURI();
//
//                InstantMessagingGUI imGUI = sipgw.getInstantMessagingGUI();
//                ListenerInstantMessaging listenerIM = imGUI.getListenerInstantMessaging();
//                String localURL = listenerIM.getLocalSipURL();
//                sendSubscribe(localURL, buddyURI, EXPIRED);
//            }
//        } catch (Exception ex)
//        {
//            ex.printStackTrace();
//        }
//    }

    public void sendSubscribe(String localURL, String buddyURI, boolean EXPIRED)
    {
        try
        {
            
            LOG.debug("Sending SUBSCRIBE in progress to the buddy: " + buddyURI);
            int proxyPort = sipgw.getProxyPort();
            String proxyAddress = sipgw.getProxyAddress();
            String imProtocol = sipgw.getIMProtocol();
            SipStack sipStack = sipgw.getSipStack();
            SipProvider sipProvider = sipgw.getSipProvider();
            MessageFactory messageFactory = sipgw.getMessageFactory();
            HeaderFactory headerFactory = sipgw.getHeaderFactory();
            AddressFactory addressFactory = sipgw.getAddressFactory();

            // Request-URI:
            // URI requestURI=addressFactory.createURI(buddyURI);
            SipURI requestURI = addressFactory.createSipURI(null, proxyAddress);
            requestURI.setPort(proxyPort);
            requestURI.setTransportParam(imProtocol);

            // Call-Id:
            CallIdHeader callIdHeader = null;

            // CSeq:
            CSeqHeader cseqHeader = null;

            // To header:
            ToHeader toHeader = null;

            // From Header:
            FromHeader fromHeader = null;

            //  Via header
            String branchId = Utils.generateBranchId();
            ViaHeader viaHeader = headerFactory.createViaHeader(sipgw.getIMAddress(), sipgw.getIMPort(), imProtocol,
                    branchId);
            Vector viaList = new Vector();
            viaList.addElement(viaHeader);

            PresenceManager presenceManager = sipgw.getPresenceManager();
            Presentity presentity = presenceManager.getPresentity(buddyURI);
            Dialog dialog = null;
            if (presentity != null)
                dialog = presentity.getDialog();

            if (dialog != null)
            {

                // We have to remove the subscriber and the Presentity related
                // with this Buddy...
                presenceManager.removePresentity(buddyURI);
                Subscriber subscriber = presenceManager.getSubscriber(buddyURI);
                if (subscriber == null)
                {
                    // It means that the guy does not have us in his buddy list
                    // nothing to do!!!
                } else
                {
                    presenceManager.removeSubscriber(buddyURI);
                }

                Address localAddress = dialog.getLocalParty();
                Address remoteAddress = dialog.getRemoteParty();

                fromHeader = headerFactory.createFromHeader(localAddress, dialog.getLocalTag());
                toHeader = headerFactory.createToHeader(remoteAddress, dialog.getRemoteTag());

                int cseq = dialog.getLocalSequenceNumber();
                cseqHeader = headerFactory.createCSeqHeader(cseq, "MESSAGE");

                callIdHeader = dialog.getCallId();
            } else
            {
                String localTag = Utils.generateTag();

                Address toAddress = addressFactory.createAddress(buddyURI);
                Address fromAddress = addressFactory.createAddress(localURL);

                fromHeader = headerFactory.createFromHeader(fromAddress, localTag);
                toHeader = headerFactory.createToHeader(toAddress, null);

                // CSeq:
                cseqHeader = headerFactory.createCSeqHeader(1, "SUBSCRIBE");

                callIdCounter++;
                // Call-ID:
                callIdHeader = (CallIdHeader) headerFactory.createCallIdHeader("nist-sip-im-subscribe-callId"
                        + callIdCounter);
            }

            // MaxForwards header:
            MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);

            Request request = messageFactory.createRequest(requestURI, "SUBSCRIBE", callIdHeader, cseqHeader,
                    fromHeader, toHeader, viaList, maxForwardsHeader);

            // Contact header:
            SipURI sipURI = addressFactory.createSipURI(null, sipgw.getIMAddress());
            sipURI.setPort(sipgw.getIMPort());
            sipURI.setTransportParam(sipgw.getIMProtocol());
            Address contactAddress = addressFactory.createAddress(sipURI);
            ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
            request.setHeader(contactHeader);

            ExpiresHeader expiresHeader = null;
            if (EXPIRED)
            {
                expiresHeader = headerFactory.createExpiresHeader(0);
            } else
            {
                expiresHeader = headerFactory.createExpiresHeader(presenceManager.getExpiresTime());
            }
            request.setHeader(expiresHeader);

            // WE have to add a new Header: "Event"
            Header eventHeader = headerFactory.createHeader("Event", "presence");
            request.setHeader(eventHeader);

            // Add Acceptw Header
            Header acceptHeader = headerFactory.createHeader("Accept", "application/pidf+xml");
            request.setHeader(acceptHeader);

            // ProxyAuthorization header if not null:
            ProxyAuthorizationHeader proxyAuthHeader = sipgw.getProxyAuthorizationHeader();
            if (proxyAuthHeader != null)
                request.setHeader(proxyAuthHeader);

            ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(request);

            if (dialog != null)
            {
                dialog.sendRequest(clientTransaction);
            } else
            {
                clientTransaction.sendRequest();
            }

            LOG.debug("IMSubscribeProcessing, sendSubscribe(), SUBSCRIBE sent:\n" + request);
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}