/*
 * Created on Aug 4, 2003
 *
 */
package ch.ethz.iks.jxme.udp;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.iks.jxme.IElement;
import ch.ethz.iks.jxme.IMessage;
import ch.ethz.iks.jxme.impl.Element;
import ch.ethz.iks.jxme.impl.Message;

/**
 * @author smaslic
 * 
 * Diese Klasse teilt eine Message in Pakete der Groesse packetSize auf und generiert gleich
 * DatagramPackets. Diese Pakete werden dann einer Instanz der OutQueue uebergeben.
 * Danach wartet der Thread auf Resend-Requests oder hoechstens bis zum Ablauf
 * des Timeouts. Er kann auch explizit beendet werden durch den Aufruf der Methode
 * stopThread().
 * 
 */
public class FileSplitter extends Thread{

	private static Logger LOG = Logger.getLogger(FileSplitter.class);

	// constants for IMessage Elements
	public static String FS_STRTAG		=	"fs_strtag";
	public static String FS_FILEID			=	"fs_fileid";
	public static String FS_DATLEN		= 	"fs_datlen";
	public static String FS_NOFP			=  "fs_nofp";
 	public static String FS_DATA			=	"fs_data";
	public static String FS_SEQN			=	"fs_seqn";
	
	private IMessage data = null;
	private final int packetSize  = 7400;
	private Vector dataVector = new Vector(); 
	private UDPPeerNetwork udppn = null;
	private boolean threadStopped = false;
	private long firstActivity;
	private long TIMEOUT = 30000; //timeout set 30s
	private int i=0;
	private Integer requestedSeqNr = new Integer(-1);
	private String fileId;
	
	
	public FileSplitter (IMessage data, UDPPeerNetwork udppn){
		this.data = data;
		this.udppn = udppn;
		this.fileId = data.getID();
	}
	
	public void run(){
		LOG.debug("FlieSplitter-Thread started");

		try {
			firstActivity = System.currentTimeMillis();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			data.writeMessage(out);
			out.flush();
			byte[] bytedata = out.toByteArray();
			out.close();
			
			LOG.debug("start checkandpack");
			dataVector = checkAndPackData(bytedata);
			
			for (i=0; i<dataVector.size(); i++){
				udppn.sendCheckedMsg(null, (IMessage)dataVector.elementAt(i) );
				LOG.debug(".");
			}

		} catch (IOException e){
			e.printStackTrace();
		}
		
//		while ((! threadStopped) && ((System.currentTimeMillis() - firstActivity) < TIMEOUT)) {
//			// wait until a resend-request received
//			synchronized (requestedSeqNr){
//				try {
//					if (LOG.isDebugEnabled())
//						LOG.debug("Wait for resend-requests");
//					requestedSeqNr.wait(TIMEOUT);
//					if (requestedSeqNr.intValue() != -1){
//						LOG.debug("Put requested packet to outqueue");
////						outq.put(dataVector.elementAt(requestedSeqNr.intValue()));
//					}
//				} catch( InterruptedException ie){
//					ie.printStackTrace();
//				}
//			}
//		}
		threadStopped = true;
	}
	
	public void resend(Integer i){
		synchronized (requestedSeqNr){
			LOG.debug("set the requested seqnumber "+i);
			requestedSeqNr = i;
			requestedSeqNr.notify();
		}
	}
	
	public void stopThread(){
		threadStopped = true;
		synchronized (requestedSeqNr){
			requestedSeqNr = new Integer(-1);
			requestedSeqNr.notify();
		}
	}
	
	public boolean finished(){
		return threadStopped;
	}

	private Vector checkAndPackData(byte[] data){
		Vector outvec = new Vector();
		byte[] tempbuf;
		boolean packing = true;
		int mybyte = 0;
		int count = 1;
		int pos=0;
		ByteArrayOutputStream outstream;
		DataOutputStream out;
		int seqNum = 0;
		int nOfPackets = (int)Math.ceil((double)data.length / (double)packetSize);
		
		LOG.debug("number of packets to send = " + nOfPackets);
	
		try{
			LOG.debug("data length = "+ data.length);				
			while (count <= nOfPackets){
				outstream = new ByteArrayOutputStream();
				out = new DataOutputStream(outstream);
				LOG.debug("Datagram: ID = "+fileId.toString() + "  seqNr = "+seqNum+" count = " + count +" nofPackets = " + nOfPackets);

				if (count == nOfPackets) {
					outstream.write(data, pos, data.length - (nOfPackets-1) *packetSize);
				} else {
					outstream.write(data,pos, packetSize);
					pos += packetSize;
				}
				outstream.flush();
				
//				outvec.add(new DatagramPacket(outstream.toByteArray(), outstream.size(), outqueue.getPNet().getGroup(), outqueue.getPNet().getPort()));				
				IMessage msg = new Message();
				
				IElement strtag = new Element(FS_STRTAG, 1);
				msg.setElement(strtag);
				IElement seqn = new Element(FS_SEQN, seqNum);
				msg.setElement(seqn);
				IElement fileid = new Element(FS_FILEID, fileId);
				msg.setElement(fileid);
				IElement datlen = new Element(FS_DATLEN, data.length);
				msg.setElement(datlen);
				IElement nofp = new Element(FS_NOFP, nOfPackets);
				msg.setElement(nofp);
				
				IElement datael = new Element(outstream.toByteArray(), FS_DATA);
				msg.setElement(datael);
				
				outvec.add(msg);

				count++;
				seqNum++;
				outstream.close();
				outstream = null;
			}
			
		} catch(IOException e){
			e.printStackTrace();		
		}
		return outvec;
	}

}
