/*
 * Created on 16.02.2004, ETH Zurich
 *  
 */
package ch.ethz.iks.jxme.bluetooth.test.scatternet;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.ethz.iks.jxme.Element;
import ch.ethz.iks.jxme.IElement;
import ch.ethz.iks.jxme.IMessage;
import ch.ethz.iks.jxme.IMessageListener;
import ch.ethz.iks.jxme.Message;
import ch.ethz.iks.jxme.bluetooth.ConnectEvent;
import ch.ethz.iks.jxme.bluetooth.DisconnectEvent;
import ch.ethz.iks.jxme.bluetooth.IConnectListener;
import ch.ethz.iks.jxme.bluetooth.IConnectionFilter;
import ch.ethz.iks.jxme.bluetooth.IDisconnectListener;
import ch.ethz.iks.jxme.bluetooth.impl.BTEndpoint;
import ch.ethz.iks.jxme.bluetooth.impl.BTPeerNetwork;
import djunit.framework.IConfigurable;
import djunit.framework.TestCase;

/**
 * @author Daniel Kaeppeli, jdan[at]kaeppe.li
 *  
 */
public class FirstChainLink
	extends TestCase
	implements IConnectListener, IDisconnectListener, IMessageListener, IConfigurable {

	private static Logger LOG = Logger.getLogger(FirstChainLink.class);

	private static BTPeerNetwork btPeer = null;
	protected static Properties systemProps = System.getProperties();

	private boolean notConnected = true;
	private Object dummy = new Object();
	
	public FirstChainLink() {
		return;
	}

	public FirstChainLink(String name) {
		super(name);

		if (btPeer == null) {
			btPeer = new BTPeerNetwork(BTEndpoint.IS_CLIENT, 0);
			btPeer.applyFilter(new LetFirstPassFilter());
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

	public void testConnecting() {
		// establishing a connection
		try {
			if(LOG.isDebugEnabled()){
				LOG.debug("testConnecting()");
			}
			while(notConnected){
				btPeer.connect(BTEndpoint.IS_CLIENT);
			}
		} catch (IOException unexpected) {
			LOG.error(
				"unexpected exception during establishment of the connection");
			fail("Unable to establish any connection \n" + unexpected);
		}
	}
	
	public void testSending(){
		IElement[] elements = new Element[2];
		elements[0] = new Element(new String("ping").getBytes(), "ping");
		elements[1] = new Element(new Long(System.currentTimeMillis()).toString().getBytes(), "time");
		try {
			synchronized(this){
				this.wait(1000);
			}
			if(LOG.isDebugEnabled()){
				LOG.debug("START SENDING OUT TEST MESSAGE");
			}
			btPeer.send(null, new Message(elements));
			if(LOG.isDebugEnabled()){
				LOG.debug("STOP SENDING OUT TEST MESSAGE");
			}
		} catch (IOException e) {
			LOG.error("Can't send test  message", e);
			fail("Can't send test message");
		} catch (InterruptedException e) {
			LOG.error("Interrupted during wait", e);
			fail("Interrupted during wait");
		}
		
		// waiting for response
		if(LOG.isDebugEnabled()){
			LOG.debug("WAITING FOR  D U M M Y L E A S E");
		}
		synchronized(dummy){
			try {
				dummy.wait();
			} catch (InterruptedException e1) {
				LOG.error("Can't wait for response, get interrupted", e1);
				fail("Interrupted during waiting for answer");
			}
		}
		if(LOG.isDebugEnabled()){
			LOG.debug("GOT  D U M M Y L E A S E");
		}
	}
	
	public void testClosing(){
		if(LOG.isDebugEnabled()){
			LOG.debug("testClosing()");
		}
		btPeer.stop();
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.iks.jxme.bluetooth.IConnectListener#processEvent(ch.ethz.iks.jxme.bluetooth.ConnectEvent)
	 */
	public void processEvent(ConnectEvent event) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("New connection to: " + event);
		}
		notConnected = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.ethz.iks.jxme.bluetooth.IDisconnectListener#processEvent(ch.ethz.iks.jxme.bluetooth.DisconnectEvent)
	 */
	public void processEvent(DisconnectEvent event) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Closed/lost connection to " + event);
		}
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jxme.IMessageListener#processMessage(ch.ethz.iks.jxme.IMessage)
	 */
	public void processMessage(IMessage message) {
		long stopTime = System.currentTimeMillis();
		long startTime = Long.parseLong(new String(message.getElement("time").getData()));
		if(LOG.isInfoEnabled()){
			LOG.info("\n\n\n * * * * Rount trip time: " + (stopTime - startTime) + " ms * * * * \n\n");
		}
		
		synchronized(dummy){
			dummy.notifyAll();
			LOG.debug("LEASING   D U M M Y L E A S E");
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
			return "./first.xml";
		}
	}

	private class LetFirstPassFilter implements IConnectionFilter{
		
		private String allowedBTAddress = systemProps.getProperty("allowedBTAddr");
		
		public synchronized boolean acceptsConnection(String uri) {
			// allow only connections if there don't exist any other ones
			boolean accept = btPeer.numberOfConnections() == 0;
			
			if(accept && (allowedBTAddress != null) && (allowedBTAddress.length() == 12)){
				// if accepted BTAddress is included in URI -> true, otherwise false
				accept = accept && (uri.indexOf(allowedBTAddress) != -1);
			}
			
			// doing some loging
			if(LOG.isDebugEnabled()){
				LOG.debug("Contection filter: allowed BTAddr = " + allowedBTAddress + "\n\t\t\turi = " + uri+ "\n\t\t\tresult = " + accept);
			}
			
			return accept;
		}
	}
	
}
