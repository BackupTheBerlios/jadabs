/*
 * IMRouter.java
 *
 * Created on July 28, 2002, 12:47 PM
 */

package ch.ethz.jadabs.jxme.sip;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.sip.ListeningPoint;
import javax.sip.SipStack;
import javax.sip.address.Address;
import javax.sip.address.Hop;
import javax.sip.address.Router;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.RouteHeader;
import javax.sip.message.Request;

import org.apache.log4j.Logger;

/**
 * 
 * @author olivier
 * @version 1.0
 */
public class RouterImpl implements Router
{
    private Logger LOG = Logger.getLogger(RouterImpl.class);
    
    protected HopImpl defaultRoute;

    protected SipStack stack;

    /**
     * Creates new IMRouter
     */
    public RouterImpl(SipStack sipStack, String defaultRoute)
    {
        if (defaultRoute != null)
            this.defaultRoute = new HopImpl(defaultRoute);
        this.stack = sipStack;
    }

    public HopImpl getNextHop(ListIterator routes) throws IllegalArgumentException
    {
        try
        {
            while (routes.hasNext())
            {
                RouteHeader routeHeader = (RouteHeader) routes.next();
                Address routeAddress = routeHeader.getAddress();
                SipURI sipURI = (SipURI) routeAddress.getURI();

                String host = sipURI.getHost();
                String transport = sipURI.getTransportParam();
                int port = sipURI.getPort();
                if (port == -1)
                {
                    port = 5060;
                }
                if (transport == null)
                    transport = "UDP";

                // Dont want to route to myself.
                if (stack.getIPAddress().equals(host) && checkPort(port))
                {
                    LOG.debug("DEBUG, IMRouter, getNextHop(), "
                            + "The RouteHeader address matches the proxy, we remove it!");
                    // Let'take the next one:
                    routes.remove();
                } else
                {
                    HopImpl hop = new HopImpl(host, port, transport);
                    return hop;
                }
            }
            return null;
        } catch (Exception e)
        {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public boolean checkPort(int port)
    {
        Iterator lps = stack.getListeningPoints();
        if (lps == null)
            return false;
        while (lps.hasNext())
        {
            ListeningPoint lp = (ListeningPoint) lps.next();
            if (lp.getPort() == port)
                return true;
        }
        return false;
    }

    /**
     * Return addresses for default proxy to forward the request to. Thelist is
     * organized in the following priority. If the request contains a Route
     * Header it is used to construct the first element of the list. If the
     * requestURI refers directly to a host, the host and port information are
     * extracted from it and made the next hop on the list. If the default route
     * has been specified, then it is used to construct the next element of the
     * list.
     * 
     * @param method
     *            is the method of the request.
     * @param requestURI
     *            is the request URI of the request.
     */
    public ListIterator getNextHops(Request request) throws IllegalArgumentException
    {
        LinkedList nextHops = new LinkedList();
        ListIterator routes = request.getHeaders(RouteHeader.NAME);

        if (routes != null)
        {
            Hop nextHop = getNextHop(routes);
            if (nextHop != null)
            {
                nextHops.add(nextHop);
            }
        }

        URI requestURI = request.getRequestURI();
        if (requestURI instanceof SipURI)
        {
            String mAddr = ((SipURI) requestURI).getMAddrParam();
            if (mAddr != null)
            {
                try
                {
                    String mAddrTransport = ((SipURI) requestURI).getTransportParam();
                    if (mAddrTransport == null)
                        mAddrTransport = "UDP";
                    int mAddrPort = ((SipURI) requestURI).getPort();
                    if (mAddrPort == -1)
                        mAddrPort = 5060;
                    HopImpl mAddrHop = new HopImpl(mAddr, mAddrPort, mAddrTransport);
                    if (mAddrHop != null)
                        nextHops.add(mAddrHop);
                    LOG.debug("DEBUG, IMRouter, getNextHops(), One hop added: Request URI maddr parameter!");
                } catch (Exception e)
                {
                    throw new IllegalArgumentException("ERROR, IMRouter, pb to add the maddr hop");
                }
            }
            /**
             * The RequestUri should be the Address of Record(AOR) (bug reported
             * by Gaurav Khandpur else { SipURI sipURI=(SipURI)requestURI;
             * String host = sipURI.getHost(); int port = sipURI.getPort(); if
             * (port == -1) { port = 5060; } String transport =
             * sipURI.getTransportParam(); if (transport==null) transport="UDP";
             * IMHop requestURIHop = new IMHop(host,port,transport);
             * nextHops.add(requestURIHop); }
             */
        } else
        {
            LOG.debug("DEBUG, IMRouter, getNextHops(), " + " the request URI is not a SipURI:"
                    + " unable to build a hop.");
        }

        if (defaultRoute != null)
        {
            nextHops.add(defaultRoute);
        }

        return nextHops.listIterator();
    }

    /**
     * Get the default hop.
     * 
     * @return defaultRoute is the default route.
     */
    public javax.sip.address.Hop getOutboundProxy()
    {
        return (javax.sip.address.Hop) this.defaultRoute;
    }

}

