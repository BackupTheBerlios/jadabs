/*
 * Created on May 4, 2004
 *
 */
package ch.ethz.iks.jxme.tcp;

import java.io.IOException;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.ethz.iks.jxme.IPeerNetwork;


/**
 * @author andfrei
 * 
 */
public class Activator implements BundleActivator
{

    IPeerNetwork tcpnet;
    
    public Activator()
    {
        
    }

    /*
     */
    public void start(BundleContext context) throws Exception
    {
    		String peername = (String)context.getProperty(IPeerNetwork.JXME_PEERNAME);
    		tcpnet = new TCPPeerNetwork(peername);

    		// create and open connection
    		tcpnet.create(null, null, null);
    		try {
    			tcpnet.connect();
    		} catch (IOException ioe) {
    			throw new Exception("could not establish connection on this port", ioe);
    		}

    }

    /*
     */
    public void stop(BundleContext context) throws Exception
    {
    		tcpnet.close(null, null, null);
    }
}
