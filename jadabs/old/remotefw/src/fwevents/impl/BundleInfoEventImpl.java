package ch.ethz.jadabs.eventsystem.fwevents.impl;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;

import ch.ethz.jadabs.eventsystem.InitializationException;
import ch.ethz.jadabs.eventsystem.impl.AEvent;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.remotefw.BundleInfo;

//import com.thoughtworks.xstream.XStream;

/**
 * @author rjan
 * 
 */
public class BundleInfoEventImpl extends AEvent
{

	private static Logger LOG = Logger.getLogger(BundleInfoEventImpl.class.getName());

	//---------------------------------------------------
    // De-,Serializable
    //---------------------------------------------------
	private static final String BINFO = "binfo";
	
	//---------------------------------------------------
    // Instant Fields
    //---------------------------------------------------
	
	protected BundleInfo binfo;
	
	public BundleInfoEventImpl()
	{
	    
	}
	
	public BundleInfoEventImpl(Bundle bundle)
	{
	    binfo = new BundleInfo(bundle);
	}

    /**
     * Return the content of the event
     */
    public BundleInfo getBundleInfo()
    {
        return binfo;
    }
	
	public IMessage toMessage(Class clas)
	{
		
		IMessage msg = null;
		if (clas != null) {
			msg = super.toMessage(clas);
		} else {
			msg = super.toMessage(BundleInfoEventImpl.class);
		}
		
		// xstream version
//		XStream xstream = new XStream();
//		String bxml = xstream.toXML(binfo);
//		
//		msg.setElement(new Element(BINFO,bxml));
		
		// byte version
		msg.setElement(new Element(BINFO, binfo));
		
		return (msg);
	}
	
	public void init(IMessage msg) throws InitializationException
	{
	    super.init(msg);
        
	    String bxml;
        try
        {
            //bxml = Message.getElementString(msg, BINFO);

            // xstream version
//            XStream xstream = new XStream();
//    	    binfo = (BundleInfo)xstream.fromXML(bxml);
    	    
    	    // byte version
    	    binfo = (BundleInfo)Message.getElementObject(msg, BINFO);

			System.out.println(binfo.toString());
			
        } catch (IOException e)
        {
        }
	}

}
