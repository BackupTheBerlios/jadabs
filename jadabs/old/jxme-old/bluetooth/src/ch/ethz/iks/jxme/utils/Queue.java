/*
 * Created on Oct, 2003
 *
 * $Id: Queue.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.iks.jxme.utils;

import ch.ethz.iks.concurrent.LinkedQueue;
//import ch.ethz.iks.logger.ILogger;
//import ch.ethz.iks.logger.Logger;


/**  An implementation of this abstract class 
 * must implement the method <code>processEvent()</code> and a method to add
 * events to the queue. This method has been deleted to make it easier to work with
 * typed events. <b>Note:</b> if you can only add events of a given type to the queue
 * you don't have to check it's type at processing time. */
public abstract class Queue implements Runnable{
	
	/** This vector stores the incoming events.  */
	protected LinkedQueue _queue = null;
	
	private boolean stopThread = false;
	
//	private static ILogger LOG = Logger.getLogger(Queue.class);
	
	
	/** Default constructor creating a queue with 
	 * <code>java.util.Vector</code>s default size. */
	public Queue(){
		_queue = new LinkedQueue();
		startThread();
	}
	
	
	/** Call this method to start the thread processing the events in the queue. */
	public void startThread(){
		new Thread(this).start();
	}
	
	
	/** Calling this method the thread managing the queue will be stopped immediately.
	 * If the call of this method is during processing an event, this event will be processed
	 * and afterwards the thread will be stopped also if there are still other events in the
	 * queue. */
	public void stopQueue(){
		stopThread = true;
		synchronized(_queue){
			_queue.notifyAll();
		}
	}
	
	
	/** This method will be called by the queue to process an event. This method will be called
	 * exactly once per event. Calling individual listeners must be done by the concrete implementation
	 * of this method.  */
	public abstract void processEvent(Object event);
	
	
	/** Thread routine managing the queue. If there are any events in the queue this thread will call the 
	 * <code>processEvent</code> method. <b>Keep in mind</b> the method 
	 * <code>processEvent</code> will be called exactly once.
	 **/
	public void run() {
			
		while(!stopThread){
			/* waiting for incoming events if the event queue is empty */
			if(_queue.isEmpty()){
				try {
//					if( LOG.isDebugEnabled() ){
//						LOG.debug("waiting for events");
//					}
					synchronized(_queue){
						_queue.wait();
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				/* process the events in the event queue */
//				if(LOG.isDebugEnabled()){
//					LOG.debug("processing event");
//				}
				try {
					processEvent(_queue.take());
				} catch (InterruptedException e) {
//					LOG.error("dequeueing an element was interrupted", e);
				}
			}
		}
	}
}
