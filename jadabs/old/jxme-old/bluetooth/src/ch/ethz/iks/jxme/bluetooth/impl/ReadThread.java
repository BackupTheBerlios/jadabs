/**
 * midas
 * ch.ethz.iks.jxme.bluetooth.impl
 * ReadThread.java
 * 
 * @author Daniel Kaeppeli, danielka[at]student.ethz.ch
 *
 * 03.07.2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * */
package ch.ethz.iks.jxme.bluetooth.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import ch.ethz.iks.jxme.bluetooth.IConnectionHandle;
import ch.ethz.iks.jxme.utils.IObjectQueue;

/** This threads listens to an <code>java.io.InputStream</code> for incoming messages. 
 * If the <code>ReadThread</code> receives a message the <code>byte[]</code> 
 * containing the messages data is given to the <code>ch.ethz.iks.utils.IObjectQueue</code>.
 * This Thread doesn't itself, so the object instantiating this thread must start it!
 * */
public class ReadThread implements Runnable {

	private final static Logger LOG = Logger.getLogger(ReadThread.class.getName());
	protected InputStream _in = null;
	protected IConnectionHandle _handle = null;
	protected IObjectQueue _messageQueue = null;
	private boolean _stop = false;
	private static int _messageCounter = 0;
	
	public ReadThread(IObjectQueue messageQueue, IConnectionHandle handle) throws IOException{
		_handle = handle;
		_in = handle.openInputStream();
		_messageQueue = messageQueue;
		if( LOG.isDebugEnabled() ){
			LOG.debug("\nread thread started\n");
		}
	}
	
	/**  */
	public void stopThread(){
		_stop = true;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		boolean failed = false;
		BTHeader header = null;
		int startOfHeaderProlog = BTHeader.PROLOG.charAt(0);
		
		try {
			if(LOG.isDebugEnabled()){
				LOG.debug("starting read thread");
			}
			while( !_stop ){
				
				boolean isHeaderOK = false;
				while( !isHeaderOK ){
					int currentSign = _in.read();
			
					while( currentSign != startOfHeaderProlog && currentSign != -1){
						currentSign = _in.read();
					}
					
					if( currentSign == -1){
						throw new IOException();
					}
					
					if(LOG.isDebugEnabled()){
						LOG.debug("beginn receiving message" + (_messageCounter++));
					}
					for(int i = 1; i < BTHeader.PROLOG.length(); i++){
						if( _in.read() != BTHeader.PROLOG.charAt(i)){
							isHeaderOK = false;
							break;
						} else {
							isHeaderOK = true;
						}
					}
					
				}
				
				header = BTHeader.readHeaderWithoutProlog( _in );
				if( header != null){
					byte[] data = new byte[header.getPayloadLength()];
					if(LOG.isDebugEnabled()){
						LOG.debug("Header ok, start reading message");
					}
					
					int counter = 0;
					while( counter < header.getPayloadLength() ){
						counter = counter + _in.read( data, counter, data.length - counter );
					}
					
					BTFooter footer = BTFooter.readFooter( _in );
					if( footer != null){
						BTPackage pack = new BTPackage(header, data, footer,_handle.getRemoteBTAddress());
						_messageQueue.putEvent(pack);
						if(LOG.isDebugEnabled()){
							LOG.debug("read message:\n\t" + pack.messageToString());
						}
					}
				}				
			}
		} catch (IOException e) {
			//expected exception
			//e.printStackTrace();
		} finally{
			// catch exception and remove handle from connectionpool
			BTConnectionPool.staticRemoveConnection( _handle );
			if(LOG.isInfoEnabled()){
					LOG.info("lost connection to " + _handle.getRemoteBTAddress());
				}
		}
	}
}
