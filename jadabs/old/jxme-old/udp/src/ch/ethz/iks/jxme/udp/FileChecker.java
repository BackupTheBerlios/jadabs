/*
 * Created on Aug 4, 2003
 *
*/
package ch.ethz.iks.jxme.udp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.iks.jxme.IMessage;
import ch.ethz.iks.jxme.impl.ElementParseException;
import ch.ethz.iks.jxme.impl.Message;
import ch.ethz.iks.jxme.impl.MessageParseException;

/**
 * @author smaslic
 * 
 * Diese Klasse prueft, ob alle Pakete eines bestimmten Files angekommen sind.
 * Dazu sind die Pakete in Vectoren abgelegt und zwar gemaess iherer Sequenznummer.
 * Sind alle Pakete angekommen, wird das File wieder zusammengesetzt und der 
 * Thread terminiert.
 *
 */
public class FileChecker {
	
	private static Logger LOG = Logger.getLogger(FileChecker.class);
	
	private FileVector fileVector = null;
	private UDPPeerNetwork udppn = null;
	private String uuid;
	
	private long timeout;
	
	public FileChecker(String uuid, UDPPeerNetwork upn, int sizeOfVector){
		fileVector = new FileVector(sizeOfVector);
		LOG.debug("filevector size: " + sizeOfVector);		
		this.uuid = uuid;
		this.udppn = upn;
		
		// timeout function of no ofpackets,
		timeout = 3*1000*sizeOfVector;
	}
	
	public void putMsg(	IMessage msg, int seqNr){
		LOG.debug("add DatagramPacket: " + seqNr);				
		fileVector.setElementAt(msg, seqNr);
		
		if (fileVector.comple())
			processMessage();
	
	}
	
	public boolean completed(){
		return fileVector.comple();
	}
	
	public boolean timedout(long delay){
		timeout -= delay;
		
		if (timeout < 0)
			return true;
		else
			return false;
	}
	
	public String getID(){
		return uuid;
	}
	
	private void processMessage(){
		
		int i=0;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		ByteArrayInputStream in;
		byte[] test;
		int temp = 0;
		int count=0;
		LOG.debug("Checker-Thread started");
		
		try {
			for (i=0; i<fileVector.size(); i++){
				IMessage msg  = (IMessage)fileVector.elementAt(i);
							
				byte[] data = msg.getElement(FileSplitter.FS_DATA).getData();
				buffer.write(data);
							
			}
					
			in = new ByteArrayInputStream(buffer.toByteArray());
		
			IMessage msg = Message.read(in);
			in.close();
			String remotePeerName = Message.getElementString(msg, Message.SENDER);
			LOG.debug("Message concatenated!!!");
						
			udppn.processMessage(msg);

		} catch (MessageParseException pe){
			LOG.error("MessageParseException", pe);
		} catch (ElementParseException ee){
			LOG.error("ElementParseException", ee);
		} catch (IOException ioe) {
			LOG.error("IOException", ioe);
		}
	}
	
	class FileVector extends Vector{
		
		int recpkgs = 0;
		int capacity;
		
		public FileVector(int capacity){
			super();
			setSize(capacity);
			this.capacity = capacity;
		}
		
		public void setElementAt(Object obj, int index){
			super.setElementAt(obj, index);
			
			if (obj != null)
				recpkgs++;
		}
		
		public boolean comple(){
			return (recpkgs == capacity);
		}
		
	}
	
}
