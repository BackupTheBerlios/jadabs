package ch.ethz.iks.evolution.mgr;

import ch.ethz.iks.proxy.WrappingException;


/**
 * Raised upon a critical failure or mandatory condition that is not satisfied
 * during a component evolution.
 * 
 * Master thesis on Component Evolution in Distributed Ad-hoc Containers
 * @author tmicha@student.ethz.ch
 */
public class OnlineUpgradeFailedException extends WrappingException {

	public OnlineUpgradeFailedException() {
		super();
	}
	
	public OnlineUpgradeFailedException(String s) {
		super(s);
	}
	
	/**
	 * @param t
	 */
	public OnlineUpgradeFailedException(Throwable t) {
		super(t);
	}



}
