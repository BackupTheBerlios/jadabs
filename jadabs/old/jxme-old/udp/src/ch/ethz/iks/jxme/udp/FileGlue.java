/*
 * Created on Aug 4, 2003
 *
 * $Id: FileGlue.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.iks.jxme.udp;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.iks.jxme.IMessage;
import ch.ethz.iks.jxme.impl.Message;

/**
 * Diese Klasse ist ein Singleton und sammelt zusammengehoerige Datenpakete
 * in einem Vector. Dazu verwaltet sie eine Hashtable, welche als Key einen
 * String braucht, der aus der RemoteIP und der FileID zusammengesetzt ist.
 * Um die Vollstaendigkeit zu garantieren wird ein Checker-Thread auf diesen
 * Vectoren gestartet.
 * 
 * @author smaslic
 * 
 */
public class FileGlue extends Thread {

	private static Logger LOG = Logger.getLogger(FileGlue.class);
	
	private Vector packets = new Vector();
	private boolean threadRunning = true;
	private Hashtable fileCheckers = new Hashtable();
	private UDPPeerNetwork udppn = null;
	
	private CheckerThread checkerT;

	private long checkerdelay = 5000;

	public FileGlue (UDPPeerNetwork udppn){
		this.udppn = udppn;
		
		checkerT = new CheckerThread();
	}

	public void put(IMessage msg){
		LOG.debug("adding packet to queue");
		synchronized (packets){
			packets.add(msg);
			packets.notify();
		}
	}
	
	public void run(){
		String remotePeerIP;
		String key;
		IMessage msg;
		
		checkerT.start();
		
		LOG.debug("FlieGlue-Thread started");
		while (threadRunning){
			synchronized (packets){

				while (packets.size() <= 0){
					try{
						packets.wait();
					}  catch (InterruptedException e){
						LOG.warn("FileGlue waiting packet has been stopped");
					}
				}
				
				msg = (IMessage)packets.remove(0);
			}

			try {
				
				String fileID = Message.getElementString(msg, FileSplitter.FS_FILEID);
				remotePeerIP = Message.getElementString(msg, Message.SENDER);
				key = new String(remotePeerIP+","+fileID); //generate the key for the hash
				int seqNr = Message.getElementInt(msg, FileSplitter.FS_SEQN);
				int arrlen = Message.getElementInt(msg, FileSplitter.FS_DATLEN);
				int nofPackets = Message.getElementInt(msg, FileSplitter.FS_NOFP);
				
				LOG.debug("FileID = " + fileID +" Datapacket seqnr = " + seqNr + " arrlen = " + arrlen + " nofpack = "+nofPackets);

				if (fileCheckers.containsKey(key)){
					LOG.debug("adding arrived packet to vector");
					synchronized(fileCheckers){
						((FileChecker)fileCheckers.get(key)).putMsg(msg, seqNr);
						fileCheckers.notify();
					}
				} else {
					LOG.debug("starting checker");

					FileChecker checkFiles = new FileChecker( key, udppn, nofPackets);
					checkFiles.putMsg(msg, seqNr);
					synchronized(fileCheckers){
						fileCheckers.put(key, checkFiles);
						fileCheckers.notify();
					}
				}
				
			} catch (UnknownHostException e){
				e.printStackTrace();
			} catch (IOException ex){
				ex.printStackTrace();
			}
		}	
	}
	
	public void stopThread(){
		threadRunning = false;
		checkerT.threadRunning = false;
		
		LOG.debug("Thread stopped");
	}

	class CheckerThread extends Thread {
		
		boolean threadRunning = true;
		
		public void run(){
			
			while( threadRunning){
				
				Vector rmlist =  new Vector();
				
				synchronized(fileCheckers){
					
					for (Enumeration en = fileCheckers.elements(); en.hasMoreElements();) {
						FileChecker fileChecker = (FileChecker)en.nextElement();
						
						if (fileChecker.completed() || fileChecker.timedout(checkerdelay))
							rmlist.add(fileChecker.getID());
					}
					
					for (Enumeration en = rmlist.elements(); en.hasMoreElements();) {
						String id = (String)en.nextElement();
						fileCheckers.remove(id);
						LOG.debug("removed checker: "+ id);
					}
					
					fileCheckers.notify();
				}
				
				try {
					Thread.sleep(checkerdelay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
			
		}
		
	}

}