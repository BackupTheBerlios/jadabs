/*
 * Created on Jul 8, 2003
 *
 */
package ch.ethz.iks.cop.eventsystem;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.iks.eventsystem.IAdvertisement;
import ch.ethz.iks.eventsystem.IEvent;
import ch.ethz.iks.eventsystem.IEventService;


/**
 * @author andfrei
 */
public class PeerListAdvertisement implements IAdvertisement{

	private static Logger LOG = Logger.getLogger(PeerListAdvertisement.class);
	
	private Vector peerlist;

	public PeerListAdvertisement(Vector peerlist){
		this.peerlist = peerlist;
	}

	public void narrow(IEventService eventservice, IEvent event) {
		
		try {
			for (Enumeration en = peerlist.elements(); en.hasMoreElements(); ) {
				String peername = (String) en.nextElement();
				
				IEvent  newEvent = (IEvent)event.clone();
			
				newEvent.setSlavePeerName(peername);
				
				eventservice.publish(newEvent);
			}
		} catch(CloneNotSupportedException ce){
			LOG.error("could not clone event", ce);
		}
	}
	
}