/**
 * JXME[dk]
 * ch.ethz.iks.utils
 *
 * @author Daniel Kaeppeli, danielka@student.ethz.ch
 *
 * Jul 9, 2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * */
package ch.ethz.iks.jxme.utils;

import java.util.Enumeration;
import java.util.Vector;

//import ch.ethz.iks.logger.ILogger;
//import ch.ethz.iks.logger.Logger;

public class ObjectQueue implements IObjectQueue {

//	private static ILogger LOG = Logger.getLogger(ObjectQueue.class);

	private Vector _messagesInQueue = null;
	private Vector _listeners = null;
	
	private boolean _stopQueue = false;
	
	public ObjectQueue(){
		_messagesInQueue = new Vector();
		_listeners = new Vector();
		ProcessThread processor = new ProcessThread();
		new Thread( processor ).start();
//		if( LOG.isDebugEnabled() ){
//			LOG.debug("ObjectQueue initialized");
//		}
	}
	
	public ObjectQueue( int initialQueueSize ){
		_messagesInQueue = new Vector( initialQueueSize );
		_listeners = new Vector();
		ProcessThread processor = new ProcessThread();
		new Thread( processor ).start();
//		if( LOG.isDebugEnabled() ){
//			LOG.debug("initialized");
//		}
	}

	public void putEvent( Object message ){
		_messagesInQueue.add( message );
//		if( LOG.isDebugEnabled() ){
//			LOG.debug("received an event [new number of events in queue:" + _messagesInQueue.size() + "]");
//		}
		synchronized(_messagesInQueue){
			_messagesInQueue.notifyAll();
		}
	}

	/** Adds a IListener to the list of listeners.  */
	public void addListener( IObjectQueueListener listener){
		_listeners.add( listener );
//		if( LOG.isDebugEnabled() ){
//			LOG.debug("listener added [new number of listeners:" + _listeners.size() + "]");
//		}
	}

	/** Removes a IListener from the list of listeners signed to this queue. */
	public void removeListener( IObjectQueueListener listener){
		_listeners.remove( listener );
	}

	/** After calling this method the thread managing this queue will be stopped. */
	public void stopQueue(){
		_stopQueue = true;
		synchronized( _messagesInQueue ){
			_messagesInQueue.notifyAll();
		}
	}

	/** thread processing incoming events */
	class ProcessThread implements Runnable{

		public ProcessThread(){
			super();
		}

		public void run() {
			Enumeration listeners = null;
			
			while( !_stopQueue ){
				/* waiting for incoming events if the event queue is empty */
				if(_messagesInQueue.size() == 0 ){
					try {
						synchronized(_messagesInQueue){
							_messagesInQueue.wait();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else {
					/* process the events in the event queue */
//					if(LOG.isDebugEnabled()){
//						LOG.debug("Process message [#msg: " + (_messagesInQueue.size() + 1) + ", #listeners: " + _listeners.size() + "]");
//					}
					processEvent(_messagesInQueue.remove(0));
				}
			}
		}
		
		protected void processEvent(Object currentMessage){
			Enumeration listeners = _listeners.elements();
			while( listeners.hasMoreElements() ){
				IObjectQueueListener currentListener = (IObjectQueueListener)listeners.nextElement();
				currentListener.processEvent( currentMessage );
//				if(LOG.isDebugEnabled()){
//					LOG.debug("call listener " + currentListener.getClass().toString());
//				}
			}
		}
		
	}
}
