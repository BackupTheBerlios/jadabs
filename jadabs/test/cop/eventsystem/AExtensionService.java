/*
 * Created on Jul 8, 2003
 *
 * $Id: AExtensionService.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.iks.cop.eventsystem;

import org.apache.log4j.Logger;

import ch.ethz.iks.cop.ComponentResource;
import ch.ethz.iks.cop.IComponentRepository;
import ch.ethz.iks.cop.IComponentResource;
import ch.ethz.iks.eventsystem.IAdvertisement;
import ch.ethz.iks.eventsystem.IEventService;

/**
 * @author andfrei
 */
public abstract class AExtensionService implements IComponentRepository {

	private static Logger LOG = Logger.getLogger(AExtensionService.class);

	private int tidcount = 0;         // transaction id for request events
	
	protected IEventService eventSvc;
	
	/* (non-Javadoc)
	 * @see ch.ethz.iks.extension.ExtensionService#insert(ch.ethz.iks.extension.ExtensionResource)
	 */
	public void insert(IComponentResource extRes) {
		LOG.info("not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.extension.ExtensionService#insert(ch.ethz.iks.extension.ExtensionResource, ch.ethz.iks.eventsystem.Advertisement)
	 */
	public void insert(ComponentResource extRes, IAdvertisement adv) {
		LOG.info("not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.extension.ExtensionService#withdraw(ch.ethz.iks.extension.ExtensionResource)
	 */
	public void withdraw(IComponentResource extRes) {
		LOG.info("not yet implemented");
	}

	/* (non-Javadoc)
	 * @see ch.ethz.iks.extension.ExtensionService#withdraw(ch.ethz.iks.extension.ExtensionResource, ch.ethz.iks.eventsystem.Advertisement)
	 */
	public void withdraw(ComponentResource extRes, IAdvertisement adv) {
		LOG.info("not yet implemented");
	}

	/**
	 * Returns a unique transaction id, unique during the lifetime of this object.
	 * 
	 * @return int tid
	 */
	protected int getTID(){
		return tidcount++;
	}

}
