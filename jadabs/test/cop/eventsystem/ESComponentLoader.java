/*
 * Created on Jul 8, 2003
 *
 * $Id: ESComponentLoader.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.iks.cop.eventsystem;

import java.io.File;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import ch.ethz.iks.bootstrap.DefaultBootStrap;
import ch.ethz.iks.eventsystem.IAdvertisement;
import ch.ethz.iks.eventsystem.IEvent;
import ch.ethz.iks.eventsystem.IEventListener;
import ch.ethz.iks.eventsystem.IFilter;
import ch.ethz.iks.eventsystem.impl.FilterImpl;

/**
 * @author andfrei
 *
 */
public class ESComponentLoader {

	private static Logger LOG = Logger.getLogger(ESComponentLoader.class);

	private static int INSERT 				= 0;	// used to specify the event for outgoing (Master)
	private static int WITHDRAW		= 0;	// or if the event was received as ingoing (Slave)


	// Name of this peer, hack use one time the PeerID
	private String peerName;

//	private Timer timer;
	private boolean stop = false;  // Thread is initialy not stoped
	
	private Hashtable extRescrs; // list of (extID, Extension)

	protected String fileSaveLoc = "./bin/pcoprep";

	/**
	 * Create an ExtensionManager with given PeerGroup.
	 * Takes default extension directory ./download/
	 * 
	 * @param peergroup
	 */
	public ESComponentLoader(){
	
		start();
		
		// init 
		extRescrs = new Hashtable();
	}

	/**
	 * Create an ExtensionManager with given PeerGroup and a download directory for extensions.
	 * 
	 * @param peergroup
	 * @param fileSaveLoc defautl will be ./download/
	 */
	public ESComponentLoader(String fileSaveLoc){
		
		this();
		
		// set fileSaveLoc for Extensions to a new download directory
		if ( fileSaveLoc != null)
			this.fileSaveLoc = fileSaveLoc + File.separatorChar;
		 
	}


  	/**
   	* request Thread to stop
   	*/ 
  	public void requestStop(){
		stop = true;
  	}

	public void start(){
			
		eventSvc = DefaultBootStrap.Instance().getEventService();
		String peername = (String)DefaultBootStrap.Instance().getProperties().get(DefaultBootStrap.PEERNAME);
		
		ExtensionResource event = new ExtensionResource();
		event.setSlavePeerName(peername);
		IFilter filter = new FilterImpl(event);
		
		eventSvc.subscribe(filter, new ExtensionEventListener());
		
	}

	public void initExtensionRes(ExtensionResource extRes){
		
//		ExtensionResource extensionRes = null;
		
		synchronized(extRescrs){

			if (!extRescrs.containsKey(extRes.getExtID())) {
			
//				extensionRes = new ExtensionResource(this, extEvent);
				extRes.initClassLoader();
				
				extRescrs.put(extRes.getExtID(), extRes);
			}

		}
		
	}
	
	public ExtensionResource getExtRes(String extID){
		
		return (ExtensionResource)extRescrs.get(extID);
	}

	/**
	 * Insert an extension to a list of given peers. Sends an ExtensionEvent to the remote peer.
	 * PRECONDITION: the extension file has to be already in the remote peer.
	 * 
	 * @param peerlist
	 * @param extRes
	 */
	public void insert(ExtensionResource extRes, IAdvertisement adv){
		
		LOG.info("insert now ExtensionResource");
		
		extRes.setTag(ExtensionResource.INSERT_REMOTE);
		
		eventSvc.publish(extRes, adv);
		
	}
	
	public void insert(ExtensionResource extRes){
		
		eventSvc.publish(extRes);
	}
	/**
	 * After inserting the extension to the remote peers, it has to be commited which
	 * than starts the extension.
	 * 
	 * @param extension
	 */
	public void commit(ExtensionResource extRes){
		
		ExtensionRunner extRunner = new ExtensionRunner(extRes);
		
		extRunner.start();
		
	}
	
	protected void withdrawExt(ExtensionResource extRes){
		
		if (extRes != null){
			extRes.stop();
							
			// remove extension from the list
			String extid = extRes.getExtID();
			extRescrs.remove(extid);
		}
	}
	
	public void withdraw(ExtensionResource extRes) {
		
		withdraw(extRes, null);
		
	}
	
	public void withdraw(ExtensionResource extRes, IAdvertisement adv){
		
		extRes.setTag(ExtensionResource.WITHDRAW);
		
		eventSvc.publish(extRes, adv);
	}

	/**
	 * ExtensionEventListener is the slave to Master queries
	 *
	 */
	class ExtensionEventListener implements IEventListener {

		private Logger LOG = Logger.getLogger(ExtensionEventListener.class);

		public void processEvent(IEvent event) {
		
			if (event instanceof ExtensionResource){
			
				ExtensionResource extRes = (ExtensionResource)event;
				
				String slavePeerName = extRes.getSlavePeerName();
				String masterPeerName = extRes.getMasterPeerName();
									

				// =========== EVENTs  ========================
				//
				// INSERT_LOCAL
				//
				if (extRes.getTag().equals(ExtensionResource.INSERT_LOCAL)){
					if (LOG.isInfoEnabled())
						LOG.info("got EXT_INSERT_LOCAL from: " + masterPeerName);
					
					LOG.debug("will instantiate Extension with Extension ID: " + extRes.getExtID());
					
					// loadClasses for extension 
					initExtensionRes(extRes);
					// and initiate and start extension
					commit(extRes);

				}
				//
				// INSERT_REMOTE
				//
				else if (extRes.getTag().equals(ExtensionResource.INSERT_REMOTE)){
					if (LOG.isInfoEnabled())
						LOG.info("got EXT_INSERT_REMOTE from: " + masterPeerName);
					
					LOG.debug("will instantiate Extension with Extension ID: " + extRes.getExtID());
					
					// loadClasses for extension 
//					loadClasses(extRes);
					// and initiate and start extension
//					commit(extRes);

				}
				//
				// WITHDRAW
				//
				else if (extRes.getTag().equals(ExtensionResource.WITHDRAW)){
					if (LOG.isInfoEnabled())
						LOG.info("got Extension WITHDRAW from: " + masterPeerName);
					
					String extid = extRes.getExtID();
					ExtensionResource localextRes = (ExtensionResource)extRescrs.get( extid );
					
					withdrawExt(localextRes);
					
				}
					
			}
		}

	} //end ExtensionEventListener

	
} // end class ExtensionManager

/**
 * Create a Thread to start the extension.
 * But Extension is responsible for himself to start a Thread if required.
 * 
 * @author andfrei
 *
 */
class ExtensionRunner extends Thread{

	private static Logger LOG = Logger.getLogger(ExtensionRunner.class);
	
	private ExtensionResource extensionRes;

	public ExtensionRunner(ExtensionResource extensionRes){
		
		this.extensionRes = extensionRes;
	}

	public void run() {

		LOG.info("commit Extension " + extensionRes.toString());		

		if (extensionRes != null){
			synchronized(extensionRes){
//				LOG.info("synchronized Extension");		
//				if (extensionRes.getExtStatus() != ExtensionResource.STARTED){
				
					try {
						LOG.info("init the Extension");		
						// init the extension
						extensionRes.initExt();
						LOG.info("start the Extension");		
						// start the extension
						extensionRes.startExt(null);
							
					} catch( InstantiationException ie){
						LOG.error(" could not instantiate class", ie);
					} catch( IllegalAccessException ie){
						LOG.error(" illegal access exception", ie);
					}
//				}
			}
		}
	}
}