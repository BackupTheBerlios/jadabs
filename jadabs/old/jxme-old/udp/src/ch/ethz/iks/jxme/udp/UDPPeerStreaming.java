/*
 * Created on Jul 31, 2003
 *
 * $Id: UDPPeerStreaming.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.iks.jxme.udp;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import ch.ethz.iks.jxme.IMessage;

/**
 * 
 * @author smaslic
 */
public class UDPPeerStreaming {
	
	private static Logger LOG = Logger.getLogger(UDPPeerStreaming.class);
	
	public static final String RESEND = "resend";
	private UDPPeerNetwork udppn = null;
	private Hashtable hash = new Hashtable(40);
	private FileGlue fg;
//	private Thread glueThread;
	
	public UDPPeerStreaming(UDPPeerNetwork udppn){
		this.udppn = udppn;
		fg = new FileGlue(udppn);
		fg.start();
//		glueThread = new Thread(fg);
//		glueThread.start();
	}
	
	public void sendData(IMessage data){
		FileSplitter split;
//		LOG.debug("sent message: " + data.toXMLString());

		if (! hash.containsKey(data.getID())){
			split = new FileSplitter(data, udppn);
			hash.put(data.getID(), split);
			split.start();			
		}
		
		// don't like this
		for (Enumeration e = hash.keys() ; e.hasMoreElements() ;) {
			finishedSending((String)e.nextElement());
		}
 
	}
	
	public void resendPaket(String ID, Integer i){
		FileSplitter temp = (FileSplitter)hash.get(ID);
		
		if (temp != null){
			temp.resend(i);
		}
	}
	
	public void finishedSending(String uuid){
		if (((FileSplitter)hash.get(uuid)).finished()){
			hash.remove(uuid);
		}
	}
	
	public void receiveMessage(IMessage msg){
		fg.put(msg);
	}
	
	public void stop(){
		fg.stopThread();
		fg.interrupt();
	}
	
}
