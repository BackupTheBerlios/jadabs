package ch.ethz.jadabs.eventsystem.fwevents.impl;

import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;

import ch.ethz.jadabs.eventsystem.InitializationException;
import ch.ethz.jadabs.eventsystem.fwevents.RemoteFWEvent;
import ch.ethz.jadabs.eventsystem.impl.AEvent;
import ch.ethz.jadabs.remotefw.BundleInfo;

//import com.thoughtworks.xstream.XStream;

/**
 * @author rjan
 * 
 */
public class RemoteFWEventImpl extends AEvent implements RemoteFWEvent
{

	private static Logger LOG = Logger.getLogger(RemoteFWEventImpl.class.getName());
		
	private Vector bundles = new Vector(); // [BundelInfo]
	
	// lazy initialization
	long[] bids = null;
	
	public RemoteFWEventImpl()
	{
	    
	}
	
	public RemoteFWEventImpl(Bundle[] bundles)
	{
	    
	    for (int i = 0; i < bundles.length; i++)
	    {
	        this.bundles.add(new BundleInfo(bundles[i]));
	    }
	    
	}

	public IMessage toMessage(Class clas)
	{
		
		IMessage msg = null;
		if (clas != null) {
			msg = super.toMessage(clas);
		} else {
			msg = super.toMessage(RemoteFWEventImpl.class);
		}
		
		// xstream version
		//XStream xstream = new XStream();
		//String bxml = xstream.toXML(bundles);
		//msg.setElement(new Element("bundles",bxml));
		//System.out.println("RFWEvent streamed: " + bxml);
		
		// byte version
		msg.setElement(new Element("bundles", bundles));
		
		
		
		
		return (msg);
	}
	
	public void init(IMessage msg) throws InitializationException
	{
	    super.init(msg);
        
	    
	    String bxml;
        try
        {
            bxml = Message.getElementString(msg, "bundles");

            // xstream version
            //XStream xstream = new XStream();
    	    //bundles = (Vector)xstream.fromXML(bxml);
    	    
    	    // byte version
    	    bundles = (Vector)Message.getElementObject(msg, "bundles");
			if (bundles == null)
				bundles = new Vector();
			
        } catch (IOException e)
        {
        }
	}

    /* (non-Javadoc)
     * @see ch.ethz.iks.remotefw.RemoteFramework#getBundles()
     */
    public Vector getBundles()
    {
        return bundles;
    }

}
