/**
 * PeerNetwork.java
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 *
 * 18.06.2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * 
 * $Id: PeerNetwork.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 * */
package ch.ethz.iks.jxme.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.iks.concurrent.LinkedQueue;
import ch.ethz.iks.jxme.IMessage;
import ch.ethz.iks.jxme.IMessageListener;
import ch.ethz.iks.jxme.IPeerNetwork;


/** 
 * This class implements the interface <code>IPeerNetwork</code>.
 * PeerNetwork acts as a singleton which allows concrete implementations like
 * UDP, BT to gather a concrete instance of the upper layer.
 */
public abstract class PeerNetwork implements IPeerNetwork, IMessageListener {

	private static Logger LOG = Logger.getLogger(PeerNetwork.class.getName());
	{
		if (LOG.isDebugEnabled())
			LOG.debug("ch.ethz.iks.jxme.PeerNetwork initialized");
	}
	
    //---------------------------------------
    // constants
    //---------------------------------------
    public static String PEERNAME = "peername";
    
    //---------------------------------------
    // Fields
    //---------------------------------------
	private boolean _isRendezvousPeer = false;
	//private StreamConnection _connection = null;
	private InputStream _in = null;
	private OutputStream _out = null;

	/* Thread to handle incomming messages */
//	private ProcessThread processT;

	/* ListenerList for message consumer */
	private Vector msglisteners = new Vector();
	private LinkedQueue msgQ = new LinkedQueue();
	
	/** Default constructor creating a regular peer */
	public PeerNetwork(){

	}

	public PeerNetwork(boolean isRendezvousPeer)
    {
		_isRendezvousPeer = isRendezvousPeer;
	}
	
	/** At the moment there is only one connection supported!
	 *  @param name parameter not used! can be <code>null</code>
	 *  @param id parameter not used! can be <code>null</code>
	 *  @param type parameter not used! can be <code>null</code> 
	 * 
	 * TODO: Do we really need all this parameters, and the return int value?
	 */
	public int close(String name, String id, String type)
    {		
//		processT.running = false;
//		processT.interrupt();
		
		return 0;
	}


	/**
	 * @see ch.ethz.iks.jxme.bt.PeerNetwork#connect()
	 */
//	public void connect() throws IOException
//    {
//		processT = new ProcessThread();
//		processT.start();
//	}

	/** 
	 * <code>processMessage</code> is called on incomming messages.
	 * It goes through the list of registered listeners.
	 */
	public void processMessage(IMessage message)
    {
//		try {
//			msgQ.put(message);
//		} catch (InterruptedException e) {
//			LOG.error("could not add message to the list of processing messages");
//		}
        
        
        Enumeration listeners = msglisteners.elements();
        for (;listeners.hasMoreElements();){
            IMessageListener msglistener = (IMessageListener)listeners.nextElement();
            
            msglistener.processMessage(message);
            
        }
	}

	/**
	 * 
	 */
	public void addMessageListener(IMessageListener msglistener)
    {		
		msglisteners.add(msglistener);
	}

	/**
	 * 
	 */
	public void removeMessageListener(IMessageListener msglistener)
    {
		msglisteners.remove(msglistener);
	}
	
	/**
	 * Enumerate through the registered listeners.
	 * 
	 * @return
	 */
	public Enumeration getMessageListeners()
    {
		return msglisteners.elements();
	}

//	/** This thread is working on the message queue. If there are any messages in the
//	 * queue the oldest message will be removed from the queue and all registered
//	 * listerners will be called one after the other. */
//	class ProcessThread extends Thread
//    {
//		boolean running = true;
//
//		public ProcessThread()
//        {
//			setName("PeerNetwork:ProcessThread");
//		}
//
//		public void run()
//        {			
//			while(running)
//            {			
//				IMessage message;
//				try {
//					message = (IMessage)msgQ.take();
//
//                    System.out.println("check for listener");
//                    
//					Enumeration listeners = msglisteners.elements();
//					for (;listeners.hasMoreElements();){
//						IMessageListener msglistener = (IMessageListener)listeners.nextElement();
//						
//                        
//                        System.out.println("received message and forward message to listener");
//                        
//						msglistener.processMessage(message);
//						
//					}
//				} catch (InterruptedException e) {
//					LOG.error("linked message queue has been interrupted");
//				}
//				
//			}
//		}
//	
//	}

}
