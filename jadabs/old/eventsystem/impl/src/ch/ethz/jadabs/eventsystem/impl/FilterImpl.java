/*
 * Created on Jul 3, 2003
 *
 * $Id: FilterImpl.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.jadabs.eventsystem.impl;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.eventsystem.Event;
import ch.ethz.jadabs.eventsystem.Filter;

/**
 * @author andfrei
 */
public class FilterImpl implements Filter
{

    private static Logger LOG = Logger.getLogger(FilterImpl.class.getName());

    private EventImpl filterevent;

    /**
     *  
     */
    public FilterImpl(EventImpl event)
    {
        super();

        this.filterevent = event;
    }

    public Event getEvent()
    {
        return filterevent;
    }

    public boolean matches(Event event)
    {
        return true;
    }
//        // first check if the same instance
//        if (filterevent.getClass().isInstance(event))
//        {
//
//            // second test for content
//            String tmplStr, eventStr;
//
//            //			// match PeerName of not only content has to match
//            //			if (matchtype == MATCH_ALL){
//
//            if (LOG.isDebugEnabled())
//                LOG.debug("check MATCH_ALL: MasterPeerName, SlavePeerName");
//
//            // masterPeerName
//            tmplStr = filterevent.getMasterPeerName();
//            eventStr = event.getMasterPeerName();
//            if (tmplStr != null && !tmplStr.equals(eventStr) && eventStr != null)
//            {
//                LOG.info("masterPeerName didn't match");
//                return false;
//            }
//
//            // slavePeerName
//            tmplStr = filterevent.getSlavePeerName();
//            eventStr = event.getSlavePeerName();
//            if (tmplStr != null && !tmplStr.equals(eventStr) && eventStr != null)
//            {
//                LOG.info("slavePeerName didn't match");
//                return false;
//            }
//            //			}
//
//            // tag
//            tmplStr = filterevent.getTag();
//            eventStr = event.getTag();
//            if (LOG.isDebugEnabled())
//                LOG.debug("check TAG; Template: " + tmplStr + " ,Event: " + eventStr);
//
//            if (tmplStr != null && !tmplStr.equals(eventStr) && eventStr != null)
//            {
//                LOG.info("Tag didn't match");
//                return false;
//            }
//
//            // attributes as String
//            Hashtable eventAttrs = event.getAttributes();
//            if (LOG.isDebugEnabled())
//            {
//                if (filterevent.getAttributes() != null)
//                    LOG.debug("check attributes size; Template: " + filterevent.getAttributes().size());
//                if (eventAttrs != null)
//                    LOG.debug("check attributes size; Event: " + eventAttrs.size());
//            }
//            for (Enumeration aten = filterevent.getAttributes().keys(); aten.hasMoreElements();)
//            {
//                String tmplKey = (String) aten.nextElement();
//
//                if (LOG.isDebugEnabled())
//                    LOG.debug("check attribute; Template Key: " + tmplKey);
//
//                if (eventAttrs.containsKey(tmplKey))
//                {
//
//                    eventStr = (String) eventAttrs.get(tmplKey);
//
//                    Object element = filterevent.getAttributes().get(tmplKey);
//                    if (element instanceof String)
//                        tmplStr = (String) element;
//                    else if (element instanceof ANY)
//                        continue;
//
//                    //					tmplStr =
//                    // (String)filterevent.getAttributes().get(tmplKey);
//
//                    if (LOG.isDebugEnabled())
//                    {
//                        LOG.debug("match attributes; Template: " + tmplKey + " , " + tmplStr);
//                        LOG.debug("match attributes; Event: " + tmplKey + " , " + eventStr);
//                    }
//
//                    if (tmplStr != null && !tmplStr.equals(eventStr) && eventStr != null) { return false; }
//                } else
//                    return false;
//
//            }
//
//            if (LOG.isDebugEnabled())
//                LOG.debug("template matched event");
//
//            return true;
//
//        } else
//            return false;
//
//    }

}