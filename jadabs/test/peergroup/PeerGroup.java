/*
 * Created on Nov 10, 2003
 *
 */
package ch.ethz.iks.jxme.peergroup;

import java.io.IOException;

import org.apache.log4j.Logger;

import ch.ethz.iks.cop.IComponent;
import ch.ethz.iks.jxme.Element;
import ch.ethz.iks.jxme.IMessage;
import ch.ethz.iks.jxme.IMessageListener;
import ch.ethz.iks.jxme.IPeerNetwork;
import ch.ethz.iks.jxme.Message;
import ch.ethz.iks.jxme.NoPeerAvailableException;
import ch.ethz.iks.jxme.PeerNetwork;

/**
 * @author andfrei
 *
 */
public class PeerGroup extends Thread implements IComponent, IMessageListener {

	private boolean running = true;

	private static Logger LOG = Logger.getLogger(PeerGroup.class);

	IPeerNetwork pnet;

	/**
	 * @see ch.ethz.iks.cop.IComponent#initComponent()
	 */
	public void initComponent() {

		pnet = PeerNetwork.Instance();
		pnet.addMessageListener(this);

	}

	/**
	 * @see ch.ethz.iks.cop.IComponent#startComponent(java.lang.String[])
	 */
	public void startComponent(String[] args) {

		this.start();

	}
	
	public void run(){
		
		while(running){
			//		send datastr
			String teststr = "send/receive string";
			
			Element el = new Element(teststr.getBytes(), "name");
			Message msg = new Message(el);
	
			try {
				pnet.send(null, msg);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoPeerAvailableException e) {
				e.printStackTrace();
			}
			
			
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		
	}

	/**
	 * @see ch.ethz.iks.cop.IComponent#stopComponent()
	 */
	public void stopComponent() {

		running = false;
	
	}

	public void processMessage(IMessage message) {
		
		LOG.info(message.toXMLString());

	}

}
