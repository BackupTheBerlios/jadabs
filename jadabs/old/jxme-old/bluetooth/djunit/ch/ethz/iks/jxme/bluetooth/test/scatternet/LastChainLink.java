/*
 * Created on 16.02.2004, ETH Zurich
 *  
 */
package ch.ethz.iks.jxme.bluetooth.test.scatternet;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.ethz.iks.jxme.IMessage;
import ch.ethz.iks.jxme.IMessageListener;
import ch.ethz.iks.jxme.bluetooth.ConnectEvent;
import ch.ethz.iks.jxme.bluetooth.DisconnectEvent;
import ch.ethz.iks.jxme.bluetooth.IConnectListener;
import ch.ethz.iks.jxme.bluetooth.IDisconnectListener;
import ch.ethz.iks.jxme.bluetooth.impl.BTEndpoint;
import ch.ethz.iks.jxme.bluetooth.impl.BTPeerNetwork;
import djunit.framework.IConfigurable;
import djunit.framework.TestCase;

/**
 * @author Daniel Kaeppeli, jdan[at]kaeppe.li
 *  
 */
public class LastChainLink
	extends TestCase
	implements IConnectListener, IDisconnectListener, IMessageListener, IConfigurable {

	public static BTPeerNetwork btPeer = null;

	private static Logger LOG = Logger.getLogger(LastChainLink.class);
	private static Properties systemProps = System.getProperties();
	
	
	private int messageCounter = 0;
	
	public LastChainLink() {
		return;
	}

	public LastChainLink(String name) {
		super(name);
		
		if (btPeer == null) {
			btPeer = new BTPeerNetwork(BTEndpoint.IS_SERVER, 0);
		}
	}
	
	public void setUp(){
		btPeer.addConnectListener(this);
		btPeer.addDisconnectListener(this);
		btPeer.addMessageListener(this);
	}
	
	public void tearDown(){
		btPeer.removeConnectListener(this);
		btPeer.removeDisconnectListener(this);
		btPeer.removeMessageListener(this);
	}

	public void testConnecting(){
		if(LOG.isDebugEnabled()){
			LOG.debug("-*-*-*-*-*-*-*-*-*   testClonnecting()   -*-*-*-*-*-*-*-*-*");
		}
		try {
			btPeer.connect(BTEndpoint.IS_SERVER);
			assertTrue("-*-*-*-*-*-*-*-*-*   connected   -*-*-*-*-*-*-*-*-*", true);
		} catch (IOException e) {
			LOG.error("Can't establish connection", e);
			fail("Can't establish connection");
		}
	}
	
	public void testReceiving() throws InterruptedException{
		while(messageCounter < 1){
			synchronized(this){
				this.wait();
			}
		}
		return;
	}
	
	public void testClosing(){
		if(LOG.isDebugEnabled()){
			LOG.debug("-*-*-*-*-*-*-*-*-*   testClosing()   -*-*-*-*-*-*-*-*-*");
		}
		btPeer.stop();
		assertTrue("closed connections", true);
	}
	
	/* (non-Javadoc)
	 * @see ch.ethz.iks.jxme.bluetooth.IConnectListener#processEvent(ch.ethz.iks.jxme.bluetooth.ConnectEvent)
	 */
	public void processEvent(ConnectEvent event) {
		if(LOG.isDebugEnabled()){
			LOG.debug("Established connection to " + event);
		}		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jxme.bluetooth.IDisconnectListener#processEvent(ch.ethz.iks.jxme.bluetooth.DisconnectEvent)
	 */
	public void processEvent(DisconnectEvent event) {
		if(LOG.isDebugEnabled()){
			LOG.debug("Closed/lost connection to "  + event);
		}
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jxme.IMessageListener#processMessage(ch.ethz.iks.jxme.IMessage)
	 */
	public void processMessage(IMessage message) {
		if(LOG.isDebugEnabled()){
			LOG.debug("PROCESSING TEST MESSAGE:\n" + message.toXMLString());
		}
		
		assertNotNull(message);
		
		messageCounter++;
		synchronized(this){
			this.notifyAll();
		}
		
		try {
			if(LOG.isDebugEnabled()){
				LOG.debug("SENDING TEST ANSWER");
			}
			//send message back
			btPeer.send(null, message);
		} catch (IOException e) {
			LOG.error("Can't send answer", e);
			fail("Can't send answer");
		}    
	}

	/* (non-Javadoc)
	 * @see djunit.framework.IConfigurable#getConfigFilePath()
	 */
	public String getConfigFilePath() {
		
		String path = systemProps.getProperty("configPath");
		
		if(path != null){
			return path;
		} else {
			return "./last.xml";
		}
	}

}
