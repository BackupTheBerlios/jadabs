/**
 * midas
 * ch.ethz.iks.jxme.bluetooth.impl
 * ConnectQueue.java
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 *
 * 20.07.2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * 
 * */
package ch.ethz.iks.jxme.bluetooth.impl;


import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.iks.jxme.bluetooth.ConnectEvent;
import ch.ethz.iks.jxme.bluetooth.IConnectListener;
import ch.ethz.iks.jxme.utils.Queue;


public class ConnectQueue extends Queue {

	private Vector _listeners = null;
	
	private static Logger LOG = Logger.getLogger(ConnectQueue.class.getName());
	
	
	/** Default constructor creates a message queue with <code>java.util.Vetctor</code>'s
	 * default size.
	 * */
	public ConnectQueue(){
		super();
		_listeners = new Vector();
	}
	
	
	/** Creates a message queue with the given size.
	 * @param initialQueueSize intial size of the message queue. */
//	public ConnectQueue(int initialQueueSize){
//		super(initialQueueSize);
//		_listeners = new Vector();
//	}
	
	
	/** This method adds a given <code>ConnectEvent</code> to the queue.
	 * This method is non-blocking. The registered listeners will be called asynchronously.
	 * There garanties about the time the notification will take.
	 * @param event event to be notified about
	 *  */
	public void putEvent( ConnectEvent event ) throws InterruptedException{
		if( LOG.isDebugEnabled() ){
			LOG.debug("new event: " + event.getFriendlyName() + "[" + event.getBluetoothAddress() + "]");
		}
		super._queue.put( event );
		synchronized(super._queue){
			super._queue.notifyAll();
		}
	}


	/** Adds a listener to list of registered listeners. The listener will be notifyed about all new 
	 * connections and all connect events that are actually in the queue but the one that is processed
	 * while calling this method.
	 * @param listener listener to be added to the list of the registered listeners
	 *  */ 
	public void addListener(IConnectListener listener){
		_listeners.add( listener );
		
		if( LOG.isDebugEnabled() ){
			LOG.debug("add listener [#listeners=" + _listeners.size() + "]");
		}
	}
	
	
	/** Removes the given listener from list of registered listenders. The listener will not be called anymore. 
	 * If the queue is processing a event exactly in this moment this method is called the listener might 
	 * be called a last time to process this event.  
	 * @param listener listener to be removed
	 * */
	public void removeListener(IConnectListener listener){
		_listeners.remove( listener );
		
		if( LOG.isDebugEnabled() ){
			LOG.debug("remove listener [#listeners=" + _listeners.size() + "]");
		}
	}


	/** This method returns number of registered listeners
	 * @return number of registered listeners */
	public int getNumberOfListeners(){
		return _listeners.size();
	}


	/** Implementation of <code>Queue</code>'s abstract method <code>processEvent(Object event)</code>.
	 * This method is called by the queue to process an event. The implementation will cast the event to a 
	 * <code>ConnectEvent</code> (since there can only be added events of this type to the queue) and will 
	 * notify all the registered listeners.
	 * @param event event to be processed
	 * */
	public void processEvent(Object event) {
		Enumeration listeners = _listeners.elements();
		
		ConnectEvent currentEvent = (ConnectEvent)event;
		
		if( LOG.isDebugEnabled() ){
			LOG.debug("process event:\n\t\t\t " + currentEvent.getFriendlyName() + "[" + currentEvent.getBluetoothAddress() + "]\n\t\t\t#listeners = " + _listeners.size());
		}
		
		while( listeners.hasMoreElements() ){
			IConnectListener currentListener = (IConnectListener)listeners.nextElement();
			currentListener.processEvent( currentEvent );
		}
	}

}
