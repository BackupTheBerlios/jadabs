/*
 * Created on 16.05.2004
 *
 */
package ch.ethz.jadabs.jxme.jacldiscovery.impl;

import ch.ethz.iks.eventsystem.IFilter;

/**
 * encapsulates a JACL lease.
 * 
 * @author rjan
 */
public class Lease {
	
	public long expires;
	public Filter filter;

	public Lease(long expires, Filter filter)
	{
		this.expires = expires;
		this.filter = filter;
	}
}