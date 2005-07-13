/*
 * Created on Dec 6, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ch.ethz.jadabs.sip.handler;

import gov.nist.javax.sip.Utils;

import java.util.Properties;

import javax.sip.ListeningPoint;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.im.ioapi.IOProperty;
import ch.ethz.jadabs.im.ioapi.Settings;
import ch.ethz.jadabs.im.ioapi.CommonSettings;


/**
 * @author franz
 *
 * abstract class for the SIP stack. The class extending this interface must implements the SipListener methods.
 * Basically it's the mais class to be extending for a class in our framework which uses the SIP stack.
 */
public abstract class IMUserAgent implements SipListener, Settings{
    private Logger logger = Logger.getLogger(IMUserAgent.class.getName());
    private SipFactory sipFactory;
    private MessageFactory messageFactory;
	private HeaderFactory headerFactory;
	private AddressFactory addressFactory;
	protected SipStack sipStack;
	protected ListeningPoint listeningPointTCP;
	protected ListeningPoint listeningPointUDP;
	protected SipProvider tcpProvider;
	protected SipProvider udpProvider;
    protected boolean started = false;
    protected CommonSettings settings;
    private String epid;
    
    public IMUserAgent(IOProperty prop) {
        epid = Utils.generateTag();
        settings = new CommonSettings(prop, "IMUseragent settings");
    }
    
	public SipProvider getSipProvider() {
	    return tcpProvider;
	}
	
	public MessageFactory getMessageFactory() {
        return messageFactory;
    }

    public HeaderFactory getHeaderFactory() {
    	return headerFactory;
    }
    
    public AddressFactory getAddressFactory() {
    	return addressFactory;
    }
	
    public String getUserName() {
    	return settings.getUserName();
    }
    
    public String getRealm() {
        return settings.getRealm();
    }
    
    public String getPassword() {
    	return settings.getPassword();
    }
    
	public String getRegistrar() {
		return settings.getRegistrar();
	}
	
	public String getIpPort() {
	    return settings.getIpAddress()+":"+settings.getPort();
	}

    public String getLocalProtocol() {
    	// TODO Protocol
    	return "TCP";
    }
    
    public int getPort() {
	    return settings.getPort();
	}
	
	public String getIpAddress() {
	    return settings.getIpAddress();
	}
    
    public String getLocalURI() {
    	return "sip:"+getUserName()+"@"+getRealm();
    }

	public String getEpid() {
		return epid;
	}
	
	// ********************** END GETTER METHODS *******************
	
    public boolean usesProxy() {
    	return true;
    }
    
    public void start() throws Exception {
		sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");

        Properties props = new Properties();

        //		props.setProperty("javax.sip.IP_ADDRESS","127.0.0.1");
        props.setProperty("javax.sip.IP_ADDRESS", getIpAddress());
        //		props.setProperty("javax.sip.IP_ADDRESS","172.30.57.44");
        props.setProperty("javax.sip.STACK_NAME", "NISTv1.1");
        //		props.setProperty("javax.sip.OUTBOUND_PROXY",
        // "172.30.57.44:5060/TCP");
        props.setProperty("gov.nist.javax.sip.DEBUG_LOG", "debug_log");
        props.setProperty("gov.nist.javax.sip.SERVER_LOG", "server_log");
        props.setProperty("javax.sip.RETRANSMISSON_FILTER", "ON");
        

        messageFactory = sipFactory.createMessageFactory();
        headerFactory = sipFactory.createHeaderFactory();
        addressFactory = sipFactory.createAddressFactory();

        sipStack = sipFactory.createSipStack(props);

        listeningPointTCP = sipStack.createListeningPoint(getPort(),
                "TCP");
        tcpProvider = sipStack.createSipProvider(listeningPointTCP);
        tcpProvider.addSipListener(this);

        listeningPointUDP = sipStack.createListeningPoint(getPort(), "UDP");
        udpProvider = sipStack.createSipProvider(listeningPointUDP);
        udpProvider.addSipListener(this);
        
        logger.debug("IP Address: " + getIpAddress() + " local port: "
                + getPort() + " protocol: " + getLocalProtocol());
        logger.debug("SIP Address: " + getLocalURI());

        started = true;
    }
    
    
}
