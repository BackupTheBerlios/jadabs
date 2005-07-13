package ch.ethz.jadabs.im.sip;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.Timeout;
import javax.sip.TimeoutEvent;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.ToHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.im.ioapi.IMUtilities;
import ch.ethz.jadabs.im.ioapi.IOProperty;
import ch.ethz.jadabs.im.ioapi.MessageCons;
import ch.ethz.jadabs.im.api.IMContact;
import ch.ethz.jadabs.im.api.IMListener;
import ch.ethz.jadabs.im.api.IMService;
import ch.ethz.jadabs.im.api.IMSettings;
import ch.ethz.jadabs.im.common.CommonIMSettings;
import ch.ethz.jadabs.im.common.UserList;
import ch.ethz.jadabs.im.common.pc.FileIOProperty;
import ch.ethz.jadabs.sip.handler.IMReceiveProcessor;
import ch.ethz.jadabs.sip.handler.IMUserAgent;
import ch.ethz.jadabs.sip.handler.SIPAckClient;
import ch.ethz.jadabs.sip.handler.SIPByeClient;
import ch.ethz.jadabs.sip.handler.SIPInviteClient;
import ch.ethz.jadabs.sip.handler.SIPMessageClient;
import ch.ethz.jadabs.sip.handler.SIPNotifyClient;
import ch.ethz.jadabs.sip.handler.SIPPublishClient;
import ch.ethz.jadabs.sip.handler.SIPRegistrationClient;
import ch.ethz.jadabs.sip.handler.SIPSubscribeClient;
import ch.ethz.jadabs.sip.handler.authentication.AuthenticationProcess;

/*
 * Created on Nov 22, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

/**
 * @author franz
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SIPUserAgentClient extends IMUserAgent implements IMService, IMSettings, IMReceiveProcessor {
	
	static Logger logger = Logger.getLogger(SIPUserAgentClient.class.getName());
	
	private IMListener sipIMListener;
	
	private SIPRegistrationClient sipRC;
	private SIPMessageClient sipMC;
	private SIPInviteClient sipIC;
	private SIPSubscribeClient sipSC;
	private SIPPublishClient sipPC;
	private SIPNotifyClient sipNC;
	private SIPByeClient sipBC;
	private SIPAckClient sipAC;

	private CommonIMSettings settings;
	private UserList userList;

	private AuthenticationProcess authenticationProcess;

	private PresenceManager presenceManager;
	private InviteManager inviteManager;
	
	// the only method allowed to change status is setLocalStatus, REMEMBER that !!
	private int status = MessageCons.IM_STATUS_OFFLINE;
	private String imtype = MessageCons.IM_SIP;

	private boolean registered = false;
	
	private int authenticating;
	
	public SIPUserAgentClient(IOProperty prop) {
	    super(prop);
		settings = new CommonIMSettings(prop, "IM settings");
		FileIOProperty iop = new FileIOProperty(settings.getBuddyListPath());
		userList = new UserList(iop);
		authenticationProcess = new AuthenticationProcess(this, this);
		presenceManager = new PresenceManager();
		inviteManager = new InviteManager();
		
        sipRC = new SIPRegistrationClient(this, this);
        sipMC = new SIPMessageClient(this, this);
        sipIC = new SIPInviteClient(this, this);
        sipSC = new SIPSubscribeClient(this, this);
        sipPC = new SIPPublishClient(this);
        sipNC = new SIPNotifyClient(this, this);
        sipBC = new SIPByeClient(this, this);
        sipAC = new SIPAckClient(this);
	}
	
	// ********************** GETTER METHODS *******************
    
   
    

	
	// ********************** END GETTER METHODS *******************
    
    private void stop() {
    	if (started) {
        	started = false;
      	 	registered = false;
      	 	if (sipIMListener != null) {
      	 		sipIMListener.disconnectOK();
      	 	}
      	 	// TODO nick the stack
      	 	try {
      	 		sipStack.deleteListeningPoint(listeningPointTCP);
      	 		sipStack.deleteListeningPoint(listeningPointUDP);
//    	 		sipStack.deleteSipProvider(tcpProvider);
//    	 		sipStack.deleteSipProvider(udpProvider);
      	 	}
      	 	catch (Exception e){
      	 		e.printStackTrace();
      	 	}
        }
    }
	
	
    // ********************** SETTER METHODS *******************

    public void newSettings(String username, String password, String registrar, String ipPort) {
        settings.setUserName(username);
        settings.setPassword(password);
        settings.setRegistrar(registrar);
        settings.setIpPort(ipPort);
        stop();
    }  
    // ********************** SETTER METHODS *******************
	
	/* (non-Javadoc)
	 * @see javax.sip.SipListener#processRequest(javax.sip.RequestEvent)
	 */
	public void processRequest(RequestEvent requestEvent) {
		authenticating = 0;
		Request request = requestEvent.getRequest();
		logger.debug("***** Request: "+request.getMethod() + " received by UA *****");
		logger.debug(request);
		logger.debug("***** Request dispatched *****");
		
		if(request.getMethod().equals("INVITE")) {
			// logger.debug("INVITE Request received");
			sipIC.processRequest(requestEvent);
		}
		else if(request.getMethod().equals("MESSAGE")) {
			// logger.debug("MESSAGE Request received");
			sipMC.processRequest(requestEvent);
		}
		else if(request.getMethod().equals("SUBSCRIBE")) {
			sipSC.processRequest(requestEvent);
		}
		else if(request.getMethod().equals("NOTIFY")) {
			sipNC.processRequest(requestEvent);
		}
		else if(request.getMethod().equals("BYE")){
			sipBC.processRequest(requestEvent);
		}
		else if (request.getMethod().equals("ACK")){
//			sipAC.processRequest(requestEvent);
		}
		else {
			logger.info("Not supported yet");
		}
	}

	/* (non-Javadoc)
	 * @see javax.sip.SipListener#processResponse(javax.sip.ResponseEvent)
	 */
	public void processResponse(ResponseEvent responseEvent) {
		Response response = responseEvent.getResponse();
		ClientTransaction clientTransaction = responseEvent.getClientTransaction();
		CSeqHeader cseqHeader=(CSeqHeader)response.getHeader(CSeqHeader.NAME);
		
		logger.debug("***** Response: "+response.getStatusCode() + " received by UA *****");
		logger.debug(response);
		logger.debug("***** Response dispatched *****");
		
		if (
				response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED || response.getStatusCode() == Response.UNAUTHORIZED) {
			if (authenticating == 10 || clientTransaction == null) {
				authenticating = 0;
				sipIMListener.operationFailed(IMListener.CONNECTION_FAILED, "Bad username/password");
			}
			else {
				try {
					logger
					.debug("IMUserAgent, processResponse(), Credentials to provide!");
					// WE start the authentication process!!!
					// Let's get the Request related to this response:
					Request clonedRequest = (Request)clientTransaction.getRequest().clone();
					if (clonedRequest == null) {
						authenticating = 0;
						logger.debug("IMUserAgent, processResponse(), the request "
								+ " that caused the 407 has not been retrieved!!! Return cancelled!");
					} else {
						authenticating++;
						// Let's increase the Cseq:
						cseqHeader = (CSeqHeader) clonedRequest
						.getHeader(CSeqHeader.NAME);
						cseqHeader
						.setSequenceNumber(cseqHeader.getSequenceNumber() + 1);
						
						// Let's add a Proxy-Authorization header:
						// We send the informations stored:
						Header header = authenticationProcess.getHeader(response);
						
						if (header == null) {
							logger.debug("IMUserAgent, processResponse(), Proxy-Authorization "
									+ " header is null, the request is not resent");
						} else {
							clonedRequest.setHeader(header);
							
							ClientTransaction newClientTransaction = getSipProvider().getNewClientTransaction(clonedRequest);
							
							newClientTransaction.sendRequest();
							logger.debug("IMUserAgent, processResponse(), REGISTER "
									+ "with credentials sent:\n" + clonedRequest);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			authenticating = 0;
			if (cseqHeader.getMethod().equals("REGISTER")) {
				//	logger.info("REGISTER Response received");
				sipRC.processResponse(responseEvent);
			}
			else if (cseqHeader.getMethod().equals("INVITE")) {
				//	logger.info("INVITE Response received");
				sipIC.processResponse(responseEvent);
			}
			else if (cseqHeader.getMethod().equals("ACK")) {
				
			}
			else if (cseqHeader.getMethod().equals("MESSAGE")) {
				sipMC.processResponse(responseEvent);
			}
			else if (cseqHeader.getMethod().equals("SUBSCRIBE")) {
				sipSC.processResponse(responseEvent);
		}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.sip.SipListener#processTimeout(javax.sip.TimeoutEvent)
	 */
	public void processTimeout(TimeoutEvent timeoutEvent) {
	    // TODO
	    Timeout timeout = timeoutEvent.getTimeout();
	    if (timeout.getValue() == Timeout._RETRANSMIT) { 
//	        sipIMListener.operationFailed(timeoutEvent.getTimeout().toString());
	    }
	    if (timeoutEvent.isServerTransaction()) {
	    	
	    	timeoutEvent.getServerTransaction().getRequest();
	    }
	}

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.api.IMService#getStatus()
	 */
	public int getStatus() {
	    return status;
	}

    public void setListener(IMListener listener)
    {
        this.sipIMListener = listener;
    }
    
	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.api.IMService#register(ch.ethz.jadabs.im.api.IMListener, int)
	 */
	public void connect()
	{
//	    this.sipIMListener = imlistener;
	    
		// Done
	    if (!registered) {
	        if (!started) {
	            try {
	                start();
	            }
	            catch (Exception e) {
	                logger.debug(e);
	                sipIMListener.operationFailed(IMListener.CONNECTION_FAILED, "Could not start SIP Stack\n\ncheck your settings");
	                return;
	            }
	        }
			try {
                sipRC.register(getLocalURI(), false);
            } catch (Exception e) {
                logger.debug(e);
                sipIMListener.operationFailed(IMListener.CONNECTION_FAILED, "Register failed\n\ncheck your settings");
            }
            
		}
		else {
			logger.info("Already registered, try to unregister before re-registering !");
		}
	}

    
	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.api.IMService#unregister()
	 */
	public void disconnect() {	    
		// send BYE to all invites
		Inviter[] inv = inviteManager.getAllConfirmedInvites();
		for (int i =0;i<inv.length;i++) {
			sipBC.sendBye(getLocalURI(), "sip:"+inv[i].getUsername(), inv[i].getDialog());
			inviteManager.removeInvite(inv[i].getUsername());
		}
		inviteManager.purge();
		
		//	  send notify to all subscribers ... // STATUS / GETLOCALSTATUS
		setStatus(MessageCons.IM_STATUS_OFFLINE);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		IMContact[] buddies = getBuddies();
		for (int i=0;i<buddies.length;i++) {
			buddies[i].setStatus(MessageCons.IM_STATUS_UNKNOWN);
		}
		
		try {
			sipRC.register(getLocalURI(), true);
		} catch (Exception e) {
			e.printStackTrace();
//			sipIMListener.operationFailed("Could not unregister, check settings !");
		}
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		stop();
	}
	
	public IMListener getListener()
    {
        return sipIMListener;
    }

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.api.IMService#sendMessage(java.lang.String, java.lang.String)
	 */
	public void sendMessage(String tousername, String message) {
		// Done
	    if (registered) {
	        try {
	        	if (inviteManager.hasInviteReceived(tousername)){
	        		Inviter inv = inviteManager.getInvite(tousername);
//	        		sipMC.sendMessage(getLocalURI(), "sip:"+tousername, message, inv.getDialog());
	        		sipMC.sendMessage(getLocalURI(), "sip:"+tousername, message);
	        		
	        		logger.debug("I already know him, no need to send INVITE !");
	        	}
	        	else {
	        		logger.debug("Processing INVITE requesting !");
	        		inviteManager.addPendingMessage(tousername, message);
	        		sipIC.invite("sip:"+tousername);
	        	}
//	            sipMC.sendMessage(getLocalURI(), "sip:"+tosipaddress, message);
	        }
	        catch (Exception e) {
	            sipIMListener.operationFailed(IMListener.CANNOT_DELIVER_MESSAGE, "Could not deliver message");
	            e.printStackTrace();
	        }
	    }
	}

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.api.IMService#getNeighbours()
	 */
	public IMContact[] getNeighbours() {
		return new IMContact[0];
	}

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMService#getBuddies()
     */
    public IMContact[] getBuddies() {
        return userList.getUsers();
    }
	
	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.api.IMService#setStatus(int)
	 */
	public void setStatus(int status) 
	{
		if (getStatus() == status) {
			logger.info("Trying to change from "+status+" to "+status+", weird ...");
		}
		else {
			this.status = status;
			sipPC.sendPublish(IMUtilities.getStringStatus(status));
			// send notify to all subscribers
			sendNotifytoAll();
		}
	}
	
    public String getIMType()
    {
        return imtype;
    }

    public void setIMType(String imtype) {
        this.imtype = imtype;
    }

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.api.IMService#addSipBuddy(java.lang.String)
	 */
	public void addSipBuddy(String buddy) {
	    logger.debug("addSipBuddy ....");
	    
	    userList.addUser(new IMContact(buddy, MessageCons.IM_STATUS_UNKNOWN));
	    
	    if (registered) {
	        sipSC.sendSubscribe(getLocalURI(), "sip:"+buddy, 3600);
	    }
	}

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.api.IMService#removeSipBuddy(java.lang.String)
	 */
	public void removeSipBuddy(String buddy) {
	    logger.debug("removeSipBuddy ....");
	    
	    userList.removeUser(buddy);
	    
	    if(registered) {
	        sipSC.sendSubscribe(getLocalURI(), "sip:"+buddy, 0);
	    }
	}
	

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.sip.handler.IMUserAgent#processSubscribe(javax.sip.message.Request)
	 */
	public void processSubscribeRequest(Response response, ServerTransaction transaction) {
        Dialog dialog = transaction.getDialog();
        FromHeader fromHeader = (FromHeader)response.getHeader(FromHeader.NAME);
        String fromAddress = IMUtilities.getUsernameFromURI(fromHeader.getAddress().getURI().toString());
        
        ExpiresHeader expiresHeader = (ExpiresHeader)response.getHeader(ExpiresHeader.NAME);
	        
	    if (expiresHeader != null && expiresHeader.getExpires()==0) {
	        // let's remove this guy from our buddy list
	        logger.debug("removing subscriber: "+ fromAddress);
	        getPresenceManager().removeSubscriber(fromAddress);
        }
	    else {
	        // let's add it to our subscriber list
	        logger.debug("Adding subscriber: "+ fromAddress);
	        getPresenceManager().addSubscriber(fromAddress, response, dialog);
	 
	    	if (getStatus() != MessageCons.IM_STATUS_OFFLINE) {
	    		String strStatus = IMUtilities.getStringStatus(getStatus());
	    		sipNC.sendNotify(dialog, strStatus);
	    	}	
	    }
	}
	
	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.sip.IMUserAgent#process(java.lang.String, java.lang.String)
	 */
	public void processMessageRequest(String fromSipUri, String toSipUri, String text) {
		sipIMListener.incomingMessage(IMUtilities.getUsernameFromURI(fromSipUri), text);
	}
	
	public void processRegister(boolean registered) {
	    if (registered) {
	        setStatus(MessageCons.IM_STATUS_ONLINE);
	        this.registered = registered;
			sipIMListener.connectOK();
			sendSubscribeToAll();
		}
		else if (registered) {
		    setStatus(MessageCons.IM_STATUS_OFFLINE);
		    this.registered = registered;
//			sipIMListener.disconnectOK();
		}		
	}

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.sip.handler.IMUserAgent#getPresenceManager()
     */
    public PresenceManager getPresenceManager() {
        return presenceManager;
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.sip.handler.IMUserAgent#processSubscribeResponse(javax.sip.message.Response)
     */
    public void processSubscribeResponse(Response response, ClientTransaction transaction, boolean expired) {
        ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
        String toAddress = IMUtilities.getUsernameFromURI(toHeader.getAddress().getURI().toString());
        
        if (expired) {
            getPresenceManager().removePresentity(toAddress);
        }
        else {
            if (transaction != null && transaction.getDialog() != null) {
                Dialog dialog = transaction.getDialog();
	            getPresenceManager().addPresentity(toAddress, response, dialog);
            }
	        else
	        {
	            logger.debug("ERROR, IMSubscribeProcessing, processOK(), the"
	                    + " dialog for the SUBSCRIBE we sent is null!!!" + " No presentity added....");
	        }
        }
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.sip.handler.IMUserAgent#processNotifyRequest(java.lang.String, java.lang.String)
     */
    public void processNotifyRequest(String fromUri, String status, boolean expired) {
        if (expired) {
            sipSC.sendSubscribe(getLocalURI(), fromUri, 30);
        }
        else {
            logger.debug("Processing notify request, looking for: "+fromUri+" in UserList...");
            
            String username = IMUtilities.getUsernameFromURI(fromUri);
            IMContact user = userList.getUser(username);
            logger.debug("Found:" + user.getUsername()+", status: "+status);
            
            user.setStatus(IMUtilities.getIntStatus(status));
            sipIMListener.buddyStatusChanged();
//            sipIMListener.neighbourListChanged();
            
            getPresenceManager().updatePresentity(username, status);
        }
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMSettings#getBuddyListPath()
     */
    public String getBuddyListPath() {
        return settings.getBuddyListPath();
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMSettings#setBuddyListPath(java.lang.String)
     */
    public void setBuddyListPath(String path) {
        settings.setBuddyListPath(path);
    }
    
    private void sendSubscribeToAll () {
		IMContact[] contacts = userList.getUsers();
		for (int i=0;i<contacts.length; i++) { 
			sipSC.sendSubscribe(getLocalURI(), "sip:"+contacts[i].getUsername(), 3600);
		}
    }
    
    private void sendNotifytoAll () {
		Vector subscribers = presenceManager.getAllSubscribers();
		Iterator i = subscribers.iterator();
		while (i.hasNext()) {
		    Subscriber sub = (Subscriber)i.next();
			sipNC.sendNotify(sub.getDialog(), IMUtilities.getStringStatus(status));
		}
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.im.api.IMService#getReceivers()
     */
    public String[] getReceivers() {
        IMContact[] contacts1 = getNeighbours();
        IMContact[] contacts2 = getBuddies();
        
        HashSet hashSet = new HashSet();
        for (int i=0; i< contacts1.length; i++) {
            hashSet.add(contacts1[i].getUsername());
        }
        for (int i=0; i< contacts2.length; i++) {
            hashSet.add(contacts2[i].getUsername());
        }
        return (String[])(hashSet.toArray(new String[0]));
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.sip.handler.IMReceiveProcessor#processMessage(boolean)
     */
    public void processMessageResponse(boolean b) {
        if (!b) {
            sipIMListener.operationFailed(IMListener.CANNOT_DELIVER_MESSAGE, "Could not deliver message");
        }
    }

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.sip.handler.IMReceiveProcessor#processInvite(javax.sip.message.Response)
	 */
	public void processInviteRequest(String sipURI, ServerTransaction transaction) {
		String username = IMUtilities.getUsernameFromURI(sipURI);
		logger.debug("Processing INVITE request from: "+ username);
		inviteManager.addInvite(username, transaction.getDialog());
		logger.debug("Added invite to invitemanager");
	}
	
	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.sip.handler.IMReceiveProcessor#processInvite(javax.sip.message.Response)
	 */
	public void processInviteResponse(String sipURI, ClientTransaction transaction, boolean isOK) {
		String username = IMUtilities.getUsernameFromURI(sipURI);
		if (isOK) {
			logger.debug("Processing INVITE response from "+username);
			inviteManager.updateInvite(username, transaction.getDialog());
			sipAC.sendAck(sipURI, transaction.getDialog());
			logger.debug("Added invite to invitemanager");
			
			Inviter inv = inviteManager.getInvite(username);
			if (inv != null) {
				while (inv.hasPendingMessage()) {
					try {
//						sipMC.sendMessage(getLocalURI(), "sip:"+inv.getUsername(), inv.getNextMessage(), inv.getDialog());
						sipMC.sendMessage(getLocalURI(), "sip:"+inv.getUsername(), inv.getNextMessage());
					}
					catch (Exception e) {
						sipIMListener.operationFailed(IMListener.CANNOT_DELIVER_MESSAGE, "Could not deliver message");
						e.printStackTrace();
					}
				}
			}
			else {
				sipIMListener.operationFailed(IMListener.CANNOT_DELIVER_MESSAGE, "Could not deliver message");
			}
		}
		else {
			inviteManager.removeInvite(username);
		}
	}

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.sip.handler.IMReceiveProcessor#processBye(java.lang.String)
	 */
	public void processBye(String sipURI) {
		String username = IMUtilities.getUsernameFromURI(sipURI);
		logger.debug("Removing inviter : "+ username);
		inviteManager.removeInvite(username);
	}


	
}