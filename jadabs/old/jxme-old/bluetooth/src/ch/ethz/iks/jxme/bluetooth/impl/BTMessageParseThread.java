/**
 * midas
 * ch.ethz.iks.jxme.bluetooth.impl
 * BTMessageParseThread.java
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 *
 * 04.07.2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * */
//package ch.ethz.iks.jxme.bluetooth.impl;
//
//import java.io.IOException;
//import java.io.InputStream;
//
//import ch.ethz.iks.jxme.msg.*;
//import ch.ethz.iks.jxme.msg.IMessage;
//import ch.ethz.iks.jxme.msg.impl.Message;
//
//import org.apache.log4j.Logger;
//
///**  */
//public class BTMessageParseThread extends Thread {
//
//	private static Logger LOG = Logger.getLogger(BTMessageParseThread.class);
//
//	IMessageProducer _producer = null;
//	IMessageConsumer _consumer = null;
//	
//	/** */
//	public BTMessageParseThread(IMessageProducer producer, IMessageConsumer consumer){
//		super();
//		_producer = producer;
//		_consumer = consumer;
//	}
//
//	public void run() {
//		while( true ){
//			while( !_producer.hasUnparsedMessages() ){
//				try {
//					sleep( 500 );
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}	
//			}
//			InputStream in = _producer.getNextUnparsedMessage();
//			try {
//				IMessage message = Message.read( in );
//				_consumer.consumeMessage(message);
//			} catch (IOException e) {
//				LOG.fatal("Can't parse the given message", e);
//			} catch (MessageParseException e) {
//				LOG.fatal("Can't parse the given message", e);
//			} catch (ElementParseException e) {
//				LOG.fatal("Can't parse the given message", e);
//			}
//		}
//	}
//
//}
