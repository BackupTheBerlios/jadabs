/*
 * Created on Jun 2, 2003
 *
 * $Id: EventImpl.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 */
package ch.ethz.jadabs.eventsystem.impl;

import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

import ch.ethz.jadabs.eventsystem.Event;
import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.EndpointAddress;
import ch.ethz.jadabs.jxme.ID;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;
import ch.ethz.jadabs.jxme.Service;

/**
 * AEvent is an abstract class which basically can be used to
 * ch.ethz.iks.jadabs.evolution a basic type.
 * 
 * 
 * @author andfrei
 *  
 */
public class EventImpl implements Event
{

    protected static Logger LOG = Logger.getLogger(EventImpl.class.getName());

    /**
     * TYPE constant to specify in (name,value) pair the type of the concrete
     * Event type.
     */
    protected static final String ID = "ID";
    
    static final String DATA_TAG = "edata";
    
    static final String PEER_ID_TAG = "epeerid";
    static final String PEERGROUP_ID_TAG = "epeergroupid";
    static final String PIPE_ID_TAG = "epipe";
    
    // PREFIX, groupID, resourceID and resourceType
    protected ID id;
    
    Object data = null;

    private Vector namedres = new Vector();
    
    XStream xstream = new XStream();
    
    private Vector addrs = new Vector();
    
    public EventImpl()
    {
        
    }

    /**
     * Create an EventImpl with the data content.
     * 
     */
    public EventImpl(Object data)
    {
        this(data, null, null);
    }
    
//    public EventImpl(NamedResource namedres)
//    {
//        this(null, namedres, null);
//    }
    
    public EventImpl(Object data, NamedResource namedres)
    {
        this(null, namedres, null);
    }

    public EventImpl(Object data, NamedResource namedres, 
            EndpointAddress addr)
    {
        this.data = data;
        this.namedres.add(namedres);
        this.addrs.add(addr);
        
        id = EventsystemActivator.eventsvc.nextEventID();
    }

//    public void addNamedResource(NamedResource namedres)
//    {
//        this.namedres.add(namedres);
//    }
//    
//    public void addEndpointAddress(EndpointAddress addr)
//    {
//        this.addrs.add(addr);
//    }
    
    Enumeration getNamedResources()
    {
        return namedres.elements();
    }
    
    public ID getID()
    {
        return id;
    }

    public Object getData()
    {
        return data;
    }
    
    Element[] marshal()
    {        
//        Vector elements = new Vector();
        Element[] elm = new Element[1];
        int index = 0;
        
        if (data != null)
        {
	        String datastr = xstream.toXML(data);
	        
	        elm[0] =new Element(DATA_TAG, datastr, 
	                Message.JXTA_NAME_SPACE);
        }
        
        // for the moment don't care about the named resources        
//        // add namedresources
//        for(Enumeration en = namedres.elements(); en.hasMoreElements(); )
//        {
//           NamedResource namedr = (NamedResource)en.nextElement();
//           
//           if(namedr instanceof Peer)
//           {
//               Peer peer = (Peer)namedr;
//               elements.add(new Element(PEER_ID_TAG, peer.getID().toString(), 
//	                Message.JXTA_NAME_SPACE));
//           }else if (namedr instanceof PeerGroup)
//           {
//               PeerGroup group = (PeerGroup)namedr;
//               elements.add(new Element(PEER_ID_TAG, group.getID().toString(), 
//	                Message.JXTA_NAME_SPACE));
//           } else if (namedr instanceof Pipe)
//           {
//               Pipe pipe = (Pipe)namedr;
//               elements.add(new Element(PEER_ID_TAG, pipe.getID().toString(), 
//	                Message.JXTA_NAME_SPACE));
//           }
//        }
        
//        return (Element[])elements.toArray();
        
        return elm;
    }
    
    void unmarshal(Element[] elements)
    {
        String datastr = Service.popString(DATA_TAG, elements);
        if (datastr != null)
        {
            data = xstream.fromXML(datastr);
        }
        
        
        // for the moment I don't care about the other namedresources
        
    }
    
    
    
//    public String toXMLString()
//    {
//
//        StringBuffer sb = new StringBuffer();
//
//        sb.append("<AEvent\n");
//        sb.append("  rel=" + reliability + "\n");
//        sb.append("  uuid=" + uuid + "\n");
//        sb.append("  tag=" + tag + "\n");
//        sb.append("  mpn=" + masterPeerName + "\n");
//        sb.append("  spn=" + slavePeerName + "\n");
//        if (obj != null)
//            sb.append("  obj=" + obj.toString() + "\n");
//        if (attributes.size() != 0)
//        {
//            sb.append("  <attributes \n");
//            for (Enumeration en = attributes.keys(); en.hasMoreElements();)
//            {
//                Object key = en.nextElement();
//                Object value = attributes.get(key);
//                sb.append("    name=" + key.toString() + " value=" + value.toString() + "\n");
//            }
//            sb.append("  />\n");
//        }
//
//        sb.append("/>");
//
//        return sb.toString();
//    }

}