/*
 * Created on 27-ene-2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.ethz.jadabs.gw.sip_smtp;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.sip.ClientTransaction;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.TimeoutEvent;
import javax.sip.header.CSeqHeader;
import javax.sip.header.Header;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.objectstyle.cayenne.access.DataContext;
import org.objectstyle.cayenne.conf.Configuration;
import org.objectstyle.cayenne.conf.FileConfiguration;

import ch.ethz.jadabs.im.ioapi.IOProperty;
import ch.ethz.jadabs.im.ioapi.Settings;
import ch.ethz.jadabs.gw.api.Gateway;
import ch.ethz.jadabs.im.db.Account;
import ch.ethz.jadabs.sip.handler.IMReceiveProcessor;
import ch.ethz.jadabs.sip.handler.IMUserAgent;
import ch.ethz.jadabs.sip.handler.SIPAckClient;
import ch.ethz.jadabs.sip.handler.SIPByeClient;
import ch.ethz.jadabs.sip.handler.SIPInviteClient;
import ch.ethz.jadabs.sip.handler.SIPMessageClient;
import ch.ethz.jadabs.sip.handler.SIPRegistrationClient;
import ch.ethz.jadabs.sip.handler.authentication.AuthenticationProcess;

/**
 * @author franz
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TemplateJL extends IMUserAgent implements Gateway, IMReceiveProcessor, Settings {
	
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(TemplateJL.class.getName());
	private SIPRegistrationClient sipRC;
	private SIPMessageClient sipMC;
	private SIPInviteClient sipIC;
	private SIPByeClient sipBC;
	private SIPAckClient sipAC;
	private AuthenticationProcess authenticationProcess;
//	private String epid;
	
	public final static Integer EMAIL = new Integer(1);
	public final static Integer SMS = new Integer(2);
	public final static Integer BOTH = new Integer(3);
	
	private DataContext dataContext;
	
	public TemplateJL (IOProperty iop, String cayenneConfigFile) {
		super(iop);
		sipRC = new SIPRegistrationClient(this, this);
		sipMC = new SIPMessageClient(this, this);
		sipIC = new SIPInviteClient(this, this);
		sipBC = new SIPByeClient(this, this);
		sipAC = new SIPAckClient(this);
		authenticationProcess = new AuthenticationProcess(this, this);
		//TODO Check if conf file exists !
		FileConfiguration conf = new FileConfiguration(new File(cayenneConfigFile));
		Configuration.initializeSharedConfiguration(conf);
		dataContext = DataContext.createDataContext();
	}
	
	/**
	 * 
	 * Actual gateway logic. Transforms a sip msg to an smtp msg.
	 * Gets the required address from the db.
	 * 
	 * @author FT, JLG
	 */
	public void processMessageRequest(String fromSipUri, String toSipUri, String text) {
		logger.info("MSG FROM : "+fromSipUri+" TO : "+toSipUri+" TEXT : "+text);
		//System.out.println("MSG FROM : "+fromSipUri+" TO : "+toSipUri+" TEXT : "+text);
		String usernameFrom = fromSipUri.replaceFirst("sip:","");
		usernameFrom = usernameFrom.replaceFirst("@.*\\z","");
		String usernameTo = toSipUri.replaceFirst("sip:","");
		usernameTo = usernameTo.replaceFirst("@.*\\z","");
		logger.info("FROM: " + usernameFrom);
		logger.info("TO: " + usernameTo);
		Map parameters = new HashMap();
		parameters.put("username", usernameFrom);
		List objects = dataContext.performQuery("account_by_username", parameters, true);
		if (objects.size() == 1) {
			Account from = (Account)objects.get(0);
			parameters = new HashMap();
			parameters.put("username", usernameTo);
			objects = dataContext.performQuery("account_by_username", parameters, true);
			if (objects.size() == 1) {
				Account to = (Account)objects.get(0);
				try {
					Properties props = new Properties();
					props.put("mail.smtp.host", "smtp.ethz.ch");
					Session session = Session.getDefaultInstance(props);
					Message msg = new MimeMessage(session);
					msg.setFrom(new InternetAddress(from.getEmail()));
					if (to.getPref().intValue() == 1) {
						msg.setRecipient(Message.RecipientType.TO,new InternetAddress(to.getEmail()));					
					}
					else if (to.getPref().intValue() == 2) {
						msg.setRecipient(Message.RecipientType.TO,new InternetAddress(to.getMobilePhone()+"@sms.switch.ch"));
					}
					else if (to.getPref().intValue() == 3) {
						InternetAddress [] tos
						= {
								new InternetAddress(to.getMobilePhone()+"@sms.switch.ch"),
								new InternetAddress(to.getEmail())
						};
						msg.setRecipients(Message.RecipientType.TO, tos);
					}
					else {
						throw new AddressException("Configuration error !");
					}
					msg.setSubject("Message from "+usernameFrom);
					msg.setText(text);
					msg.setSentDate(new Date());
					Transport.send(msg);
				} catch (AddressException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				logger.error("User \""+usernameTo+"\" does not exist. Cannot forward msg.");
			}
		}
		else {
			logger.error("User \""+usernameFrom+"\" does not exist. Cannot forward msg.");
		}
	}
	
	public void processRegister(boolean registered) {
		logger.info("Registered went well !");
	}
	
	public void processSubscribeRequest(Response response, ServerTransaction transaction) {
		//		nothing here !
	}
	
	public void processSubscribeResponse(Response response, ClientTransaction transaction, boolean expired) {
		//      nothing here !
	}
	
	public void processNotifyRequest(String fromUri, String status, boolean d) {
		//      nothing here !
	}
	
	public void processMessageResponse(boolean b) {
		//      nothing here !
	}
	
	public void processInviteRequest(String sipURI, ServerTransaction transaction) {
		//      nothing here !
	}
	
	public void processInviteResponse(String sipURI, ClientTransaction transaction, boolean isOK) {
		//      nothing here !
	}
	
	
	public void processBye(String sipURI) {
		//      nothing here !
	}
	
	/* (non-Javadoc)
	 * @see javax.sip.SipListener#processRequest(javax.sip.RequestEvent)
	 */
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
	
	public void processTimeout(TimeoutEvent arg0) {
		
	}
	
	public void signIn() {
		try {
			sipRC.register(this.getLocalURI(), false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void signOut() {
		try {
			sipRC.register(this.getLocalURI(), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
