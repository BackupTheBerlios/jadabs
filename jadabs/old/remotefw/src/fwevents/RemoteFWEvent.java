/*
 * Created on May 13, 2004
 *
 */
package ch.ethz.iks.eventsystem.fwevents;

import java.util.Vector;

import ch.ethz.iks.eventsystem.IEvent;

/**
 * @author andfrei
 * 
 */
public interface RemoteFWEvent extends Event
{
    public Vector getBundles();
}
