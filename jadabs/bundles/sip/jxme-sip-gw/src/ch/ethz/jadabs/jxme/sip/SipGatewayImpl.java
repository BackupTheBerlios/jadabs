/*
 * Created on Nov 15, 2004
 *
 */
package ch.ethz.jadabs.jxme.sip;

import java.util.Properties;

import javax.sip.ClientTransaction;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.address.AddressFactory;
import javax.sip.header.CSeqHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;


/**
 * @author andfrei
 * 
 */
public class SipGatewayImpl implements SipGateway, SipListener
{
    
    private Logger LOG = Logger.getLogger(SipGatewayImpl.class);
    
    private SipStack sipStack;
    
    private SipProvider sipProvider;
    
    private HeaderFactory headerFactory;
    
    private AddressFactory addressFactory;
    
    private MessageFactory messageFactory;
    
    private AckProcessing ackProcessing;
    
    private RegisterProcessing registerProcessing;
    
    private ByeProcessing byeProcessing;
    
    private NotifyProcessing notifyProcessing;
    
    private SubscribeProcessing subscribeProcessing;
    
    private InfoProcessing infoProcessing;
    
    private MessageProcessing messageProcessing;
    
    private PublishProcessing publishProcessing;
    
    private PresenceManager presenceManager;
    
    private ProxyAuthorizationHeader proxyAuthHeader;
    
    private String proxyAddress = "127.0.0.1";
    
    private int proxyPort = 4000;
    
    private String gwAddress = "127.0.0.1";
    
    private int gwPort = 5070;
    
    private String gwProtocol = "udp";
    
    private String registrarAddress = "127.0.0.1";
    
    private int registrarPort = 4000;
    
    public SipGatewayImpl()
    {

        ackProcessing = new AckProcessing(this);
        registerProcessing = new RegisterProcessing(this);
        byeProcessing = new ByeProcessing(this);
        subscribeProcessing = new SubscribeProcessing(this);
        notifyProcessing = new NotifyProcessing(this);
        infoProcessing = new InfoProcessing(this);
        messageProcessing = new MessageProcessing(this);

        publishProcessing = new PublishProcessing(this);

        presenceManager = new PresenceManager(this);
        proxyAuthHeader = null;
    }
    
    public void start() throws Exception
    {
        sipStack = null;
        sipProvider = null;

        SipFactory sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");

        headerFactory = sipFactory.createHeaderFactory();
        addressFactory = sipFactory.createAddressFactory();
        messageFactory = sipFactory.createMessageFactory();

        // Create SipStack object
        Properties properties = new Properties();

       
        properties.setProperty("javax.sip.IP_ADDRESS", proxyAddress);
        properties.setProperty("gov.nist.javax.sip.ACCESS_LOG_VIA_RMI", "false");
        properties.setProperty("gov.nist.javax.sip.RMI_PORT", "0");
        properties.setProperty("gov.nist.javax.sip.LOG_LIFETIME", "3600");
        properties.setProperty("gov.nist.javax.sip.BAD_MESSAGE_LOG", "./debug/bad_im_message_log.txt");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "./debug/debug_im_log.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "./debug/server_im_log.txt");

        properties.setProperty("javax.sip.STACK_NAME", "nist-sip-im-client");


        properties.setProperty("javax.sip.OUTBOUND_PROXY", proxyAddress + ":" + proxyPort + "/" + "udp");

        properties.setProperty("javax.sip.ROUTER_PATH", "ch.ethz.jadabs.jxme.sip.RouterImpl");
        

        // tell the stack to create a dialog when message comes in.
        // this is a hack
        properties.setProperty("javax.sip.EXTENSION_METHODS", Request.MESSAGE);
        sipStack = sipFactory.createSipStack(properties);

        // We create the Listening points:

        ListeningPoint lp = sipStack.createListeningPoint(gwPort, gwProtocol);
        LOG.debug("DebugIM, one listening point created: port:" + lp.getPort() + ", " + " transport:"
                + lp.getTransport());
        sipProvider = sipStack.createSipProvider(lp);
        sipProvider.addSipListener(this);

        LOG.debug("DebugIM, Instant Messaging user agent ready to work");

    }

    //---------------------------------------------------
    // implements SipGateway
    //---------------------------------------------------
    
    public void signIn(String localSipURL)
    {
        registerProcessing.signIn(localSipURL);
    }
    
    public void signOut(String localSipURL)
    {
        registerProcessing.signOut(localSipURL);
    }
    
    public void sendPublish(String localURI, String status)
    {
        publishProcessing.sendPublish(localURI, status);
    }
    
    public void sendSubscribe(String localURL, String buddyURI, boolean EXPIRED)
    {
        subscribeProcessing.sendSubscribe(localURL, buddyURI, EXPIRED);
    }

    
    //---------------------------------------------------
    // getter methods
    //---------------------------------------------------
    
    public PresenceManager getPresenceManager()
    {
        return presenceManager;
    }

    public AckProcessing getAckProcessing()
    {
        return ackProcessing;
    }

    public RegisterProcessing getRegisterProcessing()
    {
        return registerProcessing;
    }

    public ByeProcessing getByeProcessing()
    {
        return byeProcessing;
    }

    public SubscribeProcessing getSubscribeProcessing()
    {
        return subscribeProcessing;
    }

    public NotifyProcessing getNotifyProcessing()
    {
        return notifyProcessing;
    }

    public InfoProcessing getInfoProcessing()
    {
        return infoProcessing;
    }

    public MessageProcessing getMessageProcessing()
    {
        return messageProcessing;
    }

    public PublishProcessing getPublishProcessing()
    {
        return publishProcessing;
    }

    public SipStack getSipStack()
    {
        return sipStack;
    }

    public SipProvider getSipProvider()
    {
        return sipProvider;
    }

    public MessageFactory getMessageFactory()
    {
        return messageFactory;
    }

    public HeaderFactory getHeaderFactory()
    {
        return headerFactory;
    }

    public AddressFactory getAddressFactory()
    {
        return addressFactory;
    }

    public ProxyAuthorizationHeader getProxyAuthorizationHeader()
    {
        return proxyAuthHeader;
    }

    public String getProxyAddress()
    {
       return proxyAddress;
    }

    public int getProxyPort()
    {
        return proxyPort;
    }

    public String getRegistrarAddress()
    {
        return registrarAddress;
    }

    public int getRegistrarPort()
    {
        return registrarPort;
    }

    public String getIMAddress()
    {
        return gwAddress;
    }

    public int getIMPort()
    {
        return gwPort;
    }

    public String getIMProtocol()
    {
       return gwProtocol;
    }
    
    //---------------------------------------------------
    // Implements SipListener
    //---------------------------------------------------
    
    public void processRequest(RequestEvent requestEvent)
    {
        try
        {

            Request request = requestEvent.getRequest();

            Request requestCloned = (Request) request.clone();
            ServerTransaction serverTransaction = requestEvent.getServerTransaction();
            sipProvider = (SipProvider) requestEvent.getSource();

            LOG.debug("\n\nRequest " + request.getMethod() + " received:\n");

            if (serverTransaction == null)
                serverTransaction = sipProvider.getNewServerTransaction(request);

            if (request.getMethod().equals(Request.ACK))
            {
                ackProcessing.processAck(requestCloned, serverTransaction);
            } else if (request.getMethod().equals(Request.BYE))
            {
                byeProcessing.processBye(requestCloned, serverTransaction);
            } else if (request.getMethod().equals("MESSAGE"))
            {
                messageProcessing.processMessage(requestCloned, serverTransaction);
            } else if (request.getMethod().equals("INFO"))
            {
                infoProcessing.processInfo(requestCloned, serverTransaction);
            } else if (request.getMethod().equals("SUBSCRIBE"))
            {
                subscribeProcessing.processSubscribe(requestCloned, serverTransaction);
            } else if (request.getMethod().equals("NOTIFY"))
            {
                notifyProcessing.processNotify(requestCloned, serverTransaction);
            }

            else
            {
                LOG.debug("processRequest: 405 Method Not Allowed replied");

                Response response = messageFactory.createResponse(Response.METHOD_NOT_ALLOWED, request);
                serverTransaction.sendResponse(response);
            }
        } catch (Exception ex)
        {
            LOG.error("Unable to process the request:");
            ex.printStackTrace();
        }
    }

    public void processResponse(ResponseEvent responseEvent)
    {
        Response response = responseEvent.getResponse();
        LOG.debug("@@@ IMua processing response: " + response.toString());
        ClientTransaction clientTransaction = responseEvent.getClientTransaction();

        try
        {
            LOG.debug("\n\nResponse " + response.getStatusCode() + " " + response.getReasonPhrase() + " :\n"
                    + response);

            Response responseCloned = (Response) response.clone();
            CSeqHeader cseqHeader = (CSeqHeader) responseCloned.getHeader(CSeqHeader.NAME);
            if (response.getStatusCode() == Response.OK || response.getStatusCode() == 202)
            {
                if (cseqHeader.getMethod().equals("REGISTER"))
                {
                    registerProcessing.processOK(responseCloned, clientTransaction);
                }
                if (cseqHeader.getMethod().equals("MESSAGE"))
                {
                    messageProcessing.processOK(responseCloned, clientTransaction);
                }
                if (cseqHeader.getMethod().equals("BYE"))
                {
                    byeProcessing.processOK(responseCloned, clientTransaction);
                }
                if (cseqHeader.getMethod().equals("SUBSCRIBE"))
                {
                    subscribeProcessing.processOK(responseCloned, clientTransaction);
                }
                //Henrik Leion added NOTIFY processing
                if (cseqHeader.getMethod().equals("NOTIFY"))
                {
                    notifyProcessing.processOk(responseCloned, clientTransaction);
                }
            } else if (response.getStatusCode() == Response.NOT_FOUND
                    || response.getStatusCode() == Response.TEMPORARILY_UNAVAILABLE)
            {
                if (cseqHeader.getMethod().equals("SUBSCRIBE"))
                {
                    LOG.debug("The presence server is not aware " + "of the buddy you want to add.");
                } else
                {
//                    ListenerInstantMessaging listenerInstantMessaging = imGUI.getListenerInstantMessaging();
//                    ChatSessionManager chatSessionManager = listenerInstantMessaging.getChatSessionManager();
//                    ChatSession chatSession = null;
//                    String toURL = IMUtilities.getKey(response, "To");
//                    if (chatSessionManager.hasAlreadyChatSession(toURL))
//                    {
//                        chatSession = chatSessionManager.getChatSession(toURL);
//                        chatSession.setExitedSession(true, "Contact not found");
//                    }
//                    new AlertInstantMessaging("Your instant message could not be delivered..."
//                            + " The contact is not available!!!");
                }
            } else if (response.getStatusCode() == Response.DECLINE || response.getStatusCode() == Response.FORBIDDEN)
            {

//                String fromURL = IMUtilities.getKey(response, "From");
                LOG.debug("The contact " + "fromURL" + " has rejected your subscription!!!");
            } else
            {
                if (response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED
                        || response.getStatusCode() == Response.UNAUTHORIZED)
                {
                    LOG.debug("IMUserAgent, processResponse(), Credentials to " + " provide!");
                    // WE start the authentication process!!!
                    // Let's get the Request related to this response:
//                    Request request = clientTransaction.getRequest();
//                    if (request == null)
//                    {
//                        LOG.debug("IMUserAgent, processResponse(), the request "
//                                + " that caused the 407 has not been retrieved!!! Return cancelled!");
//                    } else
//                    {
//                        Request clonedRequest = (Request) request.clone();
//                        // Let's increase the Cseq:
//                        cseqHeader = (CSeqHeader) clonedRequest.getHeader(CSeqHeader.NAME);
//                        cseqHeader.setSequenceNumber(cseqHeader.getSequenceNumber() + 1);
//
//                        // Let's add a Proxy-Authorization header:
//                        // We send the informations stored:
//                        AuthenticationProcess authenticationProcess = imGUI.getAuthenticationProcess();
//                        Header header = authenticationProcess.getHeader(response);
//
//                        if (header == null)
//                        {
//                            DebugIM.println("IMUserAgent, processResponse(), Proxy-Authorization "
//                                    + " header is null, the request is not resent");
//                        } else
//                        {
//                            clonedRequest.setHeader(header);
//
//                            ClientTransaction newClientTransaction = sipProvider.getNewClientTransaction(clonedRequest);
//
//                            newClientTransaction.sendRequest();
//                            DebugIM.println("IMUserAgent, processResponse(), REGISTER " + "with credentials sent:\n"
//                                    + clonedRequest);
//                            DebugIM.println();
//                        }
//                    }
                }
            }
        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public void processTimeout(TimeoutEvent timeOutEvent)
    {
    }
}
