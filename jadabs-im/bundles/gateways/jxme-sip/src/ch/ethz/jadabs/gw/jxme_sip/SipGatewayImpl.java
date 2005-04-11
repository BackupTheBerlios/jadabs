/*
 * Created on Nov 15, 2004
 *
 */
package ch.ethz.jadabs.gw.jxme_sip;


import java.io.IOException;

import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.TimeoutEvent;
import javax.sip.header.CSeqHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.api.IMUtilities;
import ch.ethz.jadabs.api.IOProperty;
import ch.ethz.jadabs.api.MessageCons;
import ch.ethz.jadabs.api.Settings;
import ch.ethz.jadabs.gw.api.Gateway;
import ch.ethz.jadabs.jxme.DiscoveryListener;
import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.Pipe;
import ch.ethz.jadabs.jxme.services.GroupService;
import ch.ethz.jadabs.sip.handler.IMReceiveProcessor;
import ch.ethz.jadabs.sip.handler.IMUserAgent;
import ch.ethz.jadabs.sip.handler.SIPByeClient;
import ch.ethz.jadabs.sip.handler.SIPInviteClient;
import ch.ethz.jadabs.sip.handler.SIPMessageClient;
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
public class SipGatewayImpl extends IMUserAgent implements Gateway,
Listener, DiscoveryListener, IMReceiveProcessor, Settings {
	
	static Logger logger = Logger.getLogger(SipGatewayImpl.class);

	private SIPRegistrationClient sipRC;
	private SIPMessageClient sipMC;
	private SIPInviteClient sipIC;
	private SIPByeClient sipBC;
    private SIPPublishClient sipPC;
    private SIPSubscribeClient sipSC;
    
    private static int SearchPipeTimeout = 5000;
    private static final String IM_PIPE_NAME = "impipe-open";
    
    private Pipe impipe;
    private GroupService groupsvc;

	private boolean started = false;
	private boolean registered = false;

    private String epid;

    private AuthenticationProcess authenticationProcess;

    private RegisteredUserList registeredUserList;
    
	public SipGatewayImpl(GroupService groupsvc, Pipe pipe, IOProperty iop) {
	    super(iop);
		this.groupsvc = groupsvc;
		this.impipe = pipe;
		registeredUserList = new RegisteredUserList();
//		settings = new CommonSettings(iop, "IM Settings");
//		epid = Utils.generateTag();
		
		sipRC = new SIPRegistrationClient(this, this);
		sipMC = new SIPMessageClient(this, this);
		sipIC = new SIPInviteClient(this, this);
		sipPC = new SIPPublishClient(this);
		sipSC = new SIPSubscribeClient(this, this);
		sipBC = new SIPByeClient(this, this);
		authenticationProcess = new AuthenticationProcess(this, this);
	}
	
	public void start() {
	    try {
	    	super.start();
	    	
			// create Jxme stack
	        // get or create an IMPipe
	    	logger.debug("SIP side started, starting JXME");
	    	
//	        setupIMPipe();
	        groupsvc.listen(impipe, this);
	        started = true;
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    private void setupIMPipe()
    {
        // try first to find impipe
        
        try
        {
//            groupsvc.remoteSearch(NamedResource.PEER, "Name", 
//                    "", 1, this);
            groupsvc.remoteSearch(NamedResource.PIPE, "Name", 
                    IM_PIPE_NAME, 1, this);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        
        // wait for finding pipe
        try
        {
            Thread.sleep(SearchPipeTimeout);
        } catch (InterruptedException e1)
        {
            e1.printStackTrace();
        }
        
        // if no IMPipe found create one
        if (impipe == null)
        {
            logger.debug("no pipe found, create one: "+ IM_PIPE_NAME);
            // propagation pipe
            impipe = (Pipe)groupsvc.create(NamedResource.PIPE, 
                    IM_PIPE_NAME, null, Pipe.PROPAGATE);
            
            groupsvc.remotePublish(impipe);
        }
        
    }
	
    private void stop() {
    	if (started) {
    	    started = false;
    	    registered = false;
//    	    if (sipIMListener != null) {
//    	        sipIMListener.disconnectOK();
//    	    }
    	}
    }
	
    
    public void processRegister(boolean registered) {
//    	// this only changes status and sends an event statusChanged , that's it !!!
//    	if (registered) {
//    	    sipIMListener.connectOK();
//    	}
//    	else {
//    	    sipIMListener.disconnectOK();
//    	}
    }
    
    private void sendGatewayMessage() {
        Element[] elms = new Element[3];
        
        logger.debug("Sending GATEWAY messgae to indicate presence ...");
        elms[0] = new Element(MessageCons.IM_TYPE, MessageCons.GATEWAY, Message.JXTA_NAME_SPACE);
        elms[1] = new Element(MessageCons.FROM_HEADER, this.getLocalURI(), Message.JXTA_NAME_SPACE);
        elms[2] = new Element(MessageCons.IM_STATUS, Integer.toString(MessageCons.IM_STATUS_ONLINE), Message.JXTA_NAME_SPACE);
        
        try
        {            
            groupsvc.send(impipe,new Message(elms));
        } catch (IOException e)
        {            
        	e.printStackTrace();
//            LOG.error("could not subscribe, no other IM running");
//            throw new IMException("could not subscribe, no other IM running");           
        }
    }
    
    
    public void signIn()
    {
        try {
            sipRC.register(this.getLocalURI(), false);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        sendGatewayMessage();
    }

	public void signOut()
    {
        try {
            sipRC.register(this.getLocalURI(), true);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        Element[] elms = new Element[3];
        
        elms[0] = new Element(MessageCons.IM_TYPE, MessageCons.GATEWAY, Message.JXTA_NAME_SPACE);
        elms[1] = new Element(MessageCons.FROM_HEADER, this.getLocalURI(), Message.JXTA_NAME_SPACE);
        elms[2] = new Element(MessageCons.IM_STATUS, Integer.toString(MessageCons.IM_STATUS_OFFLINE), Message.JXTA_NAME_SPACE);
        
        try
        {            
            groupsvc.send(impipe,new Message(elms));
        } catch (IOException e)
        {            
        	e.printStackTrace();
//            LOG.error("could not subscribe, no other IM running");
//            throw new IMException("could not subscribe, no other IM running");           
        }
    }
    
    
    //---------------------------------------------------
    // Implements SipListener
    //---------------------------------------------------
    
    public void processRequest(RequestEvent requestEvent) {
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
		else if(request.getMethod().equals("BYE")) {
			// logger.debug("MESSAGE Request received");
			sipMC.processRequest(requestEvent);
		}
		else {
			logger.info("Not supported yet");
		}
    }

    public void processResponse(ResponseEvent responseEvent)
    {
        Response response = responseEvent.getResponse();
		ClientTransaction clientTransaction = responseEvent.getClientTransaction();
		CSeqHeader cseqHeader=(CSeqHeader)response.getHeader(CSeqHeader.NAME);
		
		logger.debug("***** Response: "+response.getStatusCode() + " received by UA *****");
		logger.debug(response);
		logger.debug("***** Response dispatched *****");
		
		if (
				response.getStatusCode() == Response.PROXY_AUTHENTICATION_REQUIRED || response.getStatusCode() == Response.UNAUTHORIZED) {
			if (clientTransaction == null) {
			    logger.info("Bad username ?");
			}
			else {
				try {
					logger
					.debug("IMUserAgent, processResponse(), Credentials to provide!");
					// WE start the authentication process!!!
					// Let's get the Request related to this response:
					Request clonedRequest = (Request)clientTransaction.getRequest().clone();
					if (clonedRequest == null) {
						logger.debug("IMUserAgent, processResponse(), the request "
								+ " that caused the 407 has not been retrieved!!! Return cancelled!");
					} else {
						// Let's increase the Cseq:
						cseqHeader = (CSeqHeader) clonedRequest.getHeader(CSeqHeader.NAME);
						cseqHeader.setSequenceNumber(cseqHeader.getSequenceNumber() + 1);
						// Let's add a Proxy-Authorization header:
						// We send the informations stored:
						FromHeader fromHeader = (FromHeader)clonedRequest.getHeader(FromHeader.NAME);
						String fromURI = fromHeader.getAddress().getURI().toString();
						Header header;
			            if (fromURI.equals(getLocalURI())) {
			                header = authenticationProcess.getHeader(response);
			            }
						else {
						    String fromAddress = IMUtilities.getUsernameFromURI(fromURI);
						    logger.info("Fetching credentials dor user: "+fromAddress);
						    RegisteredUser user = registeredUserList.getUser(fromAddress);
						    if (user != null) {
						        header = user.getAuthProcess().getHeader(response);
						    }
						    else {
						        logger.error("ERROR, user not found");
						        header = null;
						    }
						}
						
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
			if (cseqHeader.getMethod().equals("REGISTER")) {
				//	logger.info("REGISTER Response received");
				sipRC.processResponse(responseEvent);
			}
			else if (cseqHeader.getMethod().equals("ACK")) {
				
			}
			else if (cseqHeader.getMethod().equals("MESSAGE")) {
				sipMC.processResponse(responseEvent);
			}
		}
    }

    public void processTimeout(TimeoutEvent timeOutEvent)
    {
    }

    //---------------------------------------------------
    // Implement Jxme Listener
    //---------------------------------------------------
    
    /*
     */
    public void handleMessage(Message message, String arg)
    {
        logger.debug("got message:"+message.toXMLString());
        
        String fromaddress = new String(message.getElement(MessageCons.FROM_HEADER).getData());
        
        String type = new String(message.getElement(MessageCons.IM_TYPE).getData());
        
        // IM_TYPE_MSG
        if (type.equals(MessageCons.MESSAGE))
//                (addressto.equals(sipaddress) || address.equals("any")))
        {
            
            String toaddress = new String(message.getElement(MessageCons.TO_HEADER).getData());
            String msg = new String(message.getElement(MessageCons.MESSAGE_VALUE).getData());
            
            if (!registeredUserList.hasUser(toaddress)) {
                try {
                    sipMC.sendMessage("sip:"+fromaddress, "sip:"+toaddress, msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
//            if (toaddress.equals(sipaddress) || 
//            if (toaddress.equals(MessageCons.SIP_ADDRESS_ANY))
//            {
//                String msg = new String(message.getElement(MessageCons.MESSAGE_VALUE).getData());
//                imlistener.process(fromaddress, msg);
//            }
        }
        // IM_TYPE_REG
        else if (type.equals(MessageCons.REGISTER))
        {
            int status = Integer.parseInt(new String(message.getElement(MessageCons.IM_STATUS).getData()));
            Element element = message.getElement(MessageCons.PASSWORD);
            String password = null;
            if (element != null) {
                password = new String(element.getData());
            }
            else {
                logger.error("PLEASE PROVIDE A PASSWORD !");
            }
            String username = fromaddress.substring(0, fromaddress.indexOf("@"));
            String realm = fromaddress.substring(fromaddress.indexOf("@")+1);
            
            logger.debug("Adding user to registereduser list: "+fromaddress);
            RegisteredUser user = new RegisteredUser(username, realm, password, new AuthenticationProcess(this, username, realm, password));
            registeredUserList.addUser(user);
            
            try {
                sipRC.register("sip:"+fromaddress, false);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
            sendGatewayMessage();
            
//            try{
//                processMessage(fromaddress,"sip:gw@localhost","test");
//            }
//            catch (IMException e) {
//            	e.printStackTrace();
//            }
//            NeighbourTuple ntuple = new NeighbourTuple(fromaddress, status);
//            neighbours.put(fromaddress,ntuple);
//            
//            imlistener.imRegistered(fromaddress, status);
        }
        // IM_TYPE_UNREG
        else if (type.equals(MessageCons.BYE))
        {
//            imlistener.imUnregistered(fromaddress);
//            
//            neighbours.remove(fromaddress);
        }
        // IM_TYPE_NACK
//        else if (type.equals(IM_TYPE_ALIVE))
//        {
//            int status = Integer.parseInt(new String(message.getElement(IM_STATUS).getData()));
//        
//            if (!neighbours.containsKey(fromaddress))
//            {
//                NeighbourTuple ntuple = new NeighbourTuple(fromaddress,status);
//                neighbours.put(fromaddress, ntuple);
//            }
//        }
        // not known...
        else if (type.equals(MessageCons.PUBLISH)) {
            if (registeredUserList.hasUser(fromaddress)) {
                String status = new String(message.getElement(MessageCons.IM_STATUS).getData());
                
                sipPC.sendPublish("sip:"+fromaddress, status);
            }
        }
        else
            logger.debug("message type not known: "+type);
    }

    /**
     * Implements DiscoveryListener to discover IMPipe and Listener
     */
    public void handleSearchResponse(NamedResource namedResource)
    {
        if (namedResource instanceof Pipe)
        {
            impipe = (Pipe)namedResource;
            logger.debug("found pipe: " + impipe.toString());
            
            try
            {
                groupsvc.resolve(impipe, 1000);
                
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /*
     */
    public void handleNamedResourceLoss(NamedResource arg0)
    {
        // TODO Auto-generated method stub
        
    }

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.im.sip.IMUserAgent#process(java.lang.String, java.lang.String)
	 */
	public void processMessageRequest(String fromSipAddress, String toSipAddress, String message) {
		if (impipe == null)
            logger.info("could not send message, no pipe");
        
        Element[] elms = new Element[4];
            
        String toaddress = IMUtilities.getUsernameFromURI(toSipAddress);
        String fromaddress = IMUtilities.getUsernameFromURI(fromSipAddress);
        
        elms[0] = new Element(MessageCons.IM_TYPE, MessageCons.MESSAGE, Message.JXTA_NAME_SPACE);
        elms[1] = new Element(MessageCons.TO_HEADER, toaddress, Message.JXTA_NAME_SPACE);
        elms[2] = new Element(MessageCons.FROM_HEADER, fromaddress, Message.JXTA_NAME_SPACE);
        elms[3] = new Element(MessageCons.MESSAGE_VALUE, message, Message.JXTA_NAME_SPACE);
        

        try
        {
        	logger.debug("Sending message: "+message+" to: "+toaddress);
            groupsvc.send(impipe,new Message(elms));
        } catch (IOException e)
        {
        	e.printStackTrace();
            logger.info("could not send message", e);
        }
	}

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.sip.handler.IMUserAgent#processNotify(java.lang.String, java.lang.String)
	 */
	public void processNotify(String fromSipUri, String toSipUri) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.sip.handler.IMUserAgent#processSubscribe(javax.sip.message.Request)
	 */
	public void processSubscribe(Request request) {
		// TODO Auto-generated method stub
		
	}

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.sip.handler.IMUserAgent#processSubscribeRequest(javax.sip.message.Request, javax.sip.message.Response, javax.sip.ServerTransaction)
     */
    public void processSubscribeRequest(Response response, ServerTransaction transaction) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.sip.handler.IMUserAgent#processSubscribeResponse(javax.sip.message.Response, javax.sip.ClientTransaction)
     */
    public void processSubscribeResponse(Response response, ClientTransaction transaction, boolean expired) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.sip.handler.IMUserAgent#processNotifyRequest(java.lang.String, java.lang.String)
     */
    public void processNotifyRequest(String fromUri, String status, boolean expired) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see ch.ethz.jadabs.sip.handler.IMReceiveProcessor#processMessage(boolean)
     */
    public void processMessageResponse(boolean b) {
        	logger.debug("Processing message response...");
    }

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.sip.handler.IMReceiveProcessor#processInviteRequest(java.lang.String, javax.sip.ServerTransaction)
	 */
	public void processInviteRequest(String sipURI, ServerTransaction transaction) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.sip.handler.IMReceiveProcessor#processBye(java.lang.String)
	 */
	public void processBye(String sipURI) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.jadabs.sip.handler.IMReceiveProcessor#processInviteResponse(java.lang.String, javax.sip.ClientTransaction, boolean)
	 */
	public void processInviteResponse(String sipURI, ClientTransaction transaction, boolean isOK) {
		// TODO Auto-generated method stub
		
	}

}
