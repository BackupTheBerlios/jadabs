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
public class IntermediateChainLink
	extends TestCase
	implements IConnectListener, IDisconnectListener, IMessageListener, IConfigurable {

	public static Logger LOG = Logger.getLogger(IntermediateChainLink.class);
	public static BTPeerNetwork btPeer = null;
	private static Properties systemProps = System.getProperties();
	private int messageCounter = 0; 

	protected boolean notConnected = true;
	protected boolean waitingForConnection = false;
	
	public IntermediateChainLink() {
		return;
	}
	
	public IntermediateChainLink(String name){
		super(name);
		
		if(btPeer == null){
			btPeer = new BTPeerNetwork(BTPeerNetwork.IS_BOTH, -1);
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
	
	public void testConnecting(){
		try {
			// waiting untile there is an connection to an rendez-vous peer (this node becomes master)
			btPeer.connect(BTEndpoint.IS_SERVER);
			synchronized(this){
				this.wait();
			}
			//
			waitingForConnection = true;
			while(notConnected){
				if(LOG.isDebugEnabled()){
					LOG.debug("starting inquiry");
				}
				btPeer.connect(BTEndpoint.IS_CLIENT);
				synchronized(this){
					this.wait(10000);
				}
			}
			
		} catch (IOException e) {
			LOG.error("Can't establish a connection", e);
		} catch (InterruptedException e) {
			LOG.error("Process was interrupted during wait", e);
		}
	}

	public void testForwarding(){
		synchronized(this){
			while(messageCounter < 2){
				try {
					this.wait();
					if(LOG.isDebugEnabled()){
						LOG.debug("Message Counter = " + messageCounter);
					}
				} catch (InterruptedException e) {
					LOG.error("Interrupted during waiting", e);
					fail("Interrupted during waiting");
				}
			}
		}
		return;
	}
	
	public void testClosing(){
		btPeer.stop();
	}
	
	
	
	/* (non-Javadoc)
	 * @see ch.ethz.iks.jxme.bluetooth.IConnectListener#processEvent(ch.ethz.iks.jxme.bluetooth.ConnectEvent)
	 */
	public void processEvent(ConnectEvent event) {
		if(LOG.isDebugEnabled()){
			LOG.debug("Connected to " + event);
		}
		
		if(waitingForConnection == true){
			notConnected = false;
		} else {
			synchronized(this){
				this.notifyAll();
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jxme.bluetooth.IDisconnectListener#processEvent(ch.ethz.iks.jxme.bluetooth.DisconnectEvent)
	 */
	public void processEvent(DisconnectEvent event) {
		if(LOG.isDebugEnabled()){
			LOG.debug("Closed/lost connection to " + event);
		}
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.jxme.IMessageListener#processMessage(ch.ethz.iks.jxme.IMessage)
	 */
	public void processMessage(IMessage message) {
		messageCounter++;

		if(LOG.isDebugEnabled()){
			LOG.debug("PROCESSING MESSAGE (" + messageCounter + "):\n" + message.toXMLString());
		}
		
		synchronized(this){
			this.notifyAll();
		}
	}

	/** This method returns the path to the configuration file. This path can be configured
	 * by setting environment variable <code>configPath</code> while starting the JMV.
	 * If there is no such definition the default configuration file path is returned which is 
	 * <code>./inter.xml</code>.
	 *  @return path to the config file
	 * @see djunit.framework.IConfigurable#getConfigFilePath()
	 */
	public String getConfigFilePath() {
		
		String path = systemProps.getProperty("configPath");
		
		if(path != null){
			return path;
		} else {
			return "/home/daniel/scatternet/localhost/inter1.xml";
		}
	}
	
	private class LetFirstPassFilter implements IConnectionFilter{

		private String allowedBTAddress = systemProps.getProperty("allowedBTAddr");
		
		public synchronized boolean acceptsConnection(String uri) {
			boolean accepted = btPeer.numberOfConnections() < 2;
			
			if(accepted && (allowedBTAddress != null) && (allowedBTAddress.length()==12)){
				accepted = accepted && (uri.indexOf(allowedBTAddress) != -1);
			}
			
			if(LOG.isDebugEnabled()){
				LOG.debug("Filtering connection request:" +
									"\n\t\tallowed BTAddr = "+ allowedBTAddress +
									"\n\t\turi = "+ uri+ "\n\t\trestult = " + accepted + 
									"\n\t\tconnections = " + btPeer.numberOfConnections());
			}
			return accepted;
		}
		
	}
}
