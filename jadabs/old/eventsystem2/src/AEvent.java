/*
 * Created on Jun 2, 2003
 *
 * $Id: AEvent.java,v 1.1 2004/11/08 07:30:35 afrei Exp $
 */
package ch.ethz.jadabs.eventsystem.impl;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.eventsystem.Event;
import ch.ethz.jadabs.eventsystem.InitializationException;
import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.Message;

/**
 * AEvent is an abstract class which basically can be used to
 * ch.ethz.iks.jadabs.evolution a basic type.
 * 
 * 
 * @author andfrei
 *  
 */
public abstract class AEvent implements Event
{

    protected static Logger LOG = Logger.getLogger(AEvent.class.getName());

    /**
     * TYPE constant to specify in (name,value) pair the type of the concrete
     * Event type.
     */
    public static final String TYPE = "type";

    protected static final String ID = "ID";

    protected static final String TAG = "TAG";

    protected static final String OBJ = "OBJ"; // object

    protected static final String MPN = "MPN"; // Masterpeername

    protected static final String SPN = "SPN"; // Slavepeername

    protected static final String ATS = "ATS"; // Attributes

    protected static final String REL = "REL"; // reliable event

    protected static final String REL_REQ = "REL_REQ"; // reliable request

    protected static final String REL_ACK = "REL_ACK"; // reliable ack

    protected static final String REL_NONE = "REL_NONE"; // reliable not set,

    // unreliable event

    protected String reliability = REL_NONE; // per default unreliable

    protected Message message;

    protected String uuid;

    protected String tag;

    protected Object obj;

    protected String masterPeerName;

    protected String slavePeerName;

    protected Hashtable attributes = new Hashtable();

    /**
     * Constructor for AMidasEvent, will be used for class.newInstance()
     * 
     * @see java.lang.Object#Object()
     */
    public AEvent()
    {
        uuid = IDFactory.Instance().newEventID();
    }

    /**
     * Constructor for AMidasEvent.
     * 
     * @param tag
     */
    public AEvent(String tag)
    {

        this();
        this.tag = tag;
    }

    /**
     * Constructor for AMidasEvent.
     * 
     * @param tag
     * @param obj
     */
    public AEvent(String tag, Object obj)
    {

        this(tag);
        this.obj = obj;
    }

    public String getID()
    {
        return uuid;
    }

    /**
     * @see midas.eventsystem.MidasEvent#getTag()
     */
    public String getTag()
    {
        return tag;
    }

    /**
     * @see midas.eventsystem.MidasEvent#setTag(String)
     */
    public void setTag(String tag)
    {
        this.tag = tag;
    }

    /**
     * 
     * @see midas.eventsystem.MidasEvent#getValue()
     */
    public Object getValue()
    {
        return obj;
    }

    /**
     * Add an attribute Name = Value Pair to the MidasEvent.
     * 
     * @param name
     * @param value
     */
    public void addAttribute(Object name, Object value)
    {
        if (attributes == null)
            attributes = new Hashtable();

        attributes.put(name, value);
    }

    /**
     * Return Value for the given Attribute Name.
     * 
     * @param name
     * @return
     */
    public Object getAttributeValue(Object name)
    {
        if (attributes != null)
            return attributes.get(name);
        else
            return null;
    }

    /**
     * Return the attributes.
     * 
     * @return
     */
    public Hashtable getAttributes()
    {
        return attributes;
    }

    /**
     * Remove Attribute with given name.
     * 
     * @param name
     */
    public void removeAttributeValue(Object name)
    {
        attributes.remove(name);
    }

    /**
     * Set MasterPeerName of the sending Peer.
     * 
     * @param peername
     */
    public void setMasterPeerName(String peername)
    {
        this.masterPeerName = peername;
    }

    /**
     * Set SlavePeerName of the sending Peer.
     * 
     * @param peername
     */
    public void setSlavePeerName(String peername)
    {
        this.slavePeerName = peername;
    }

    /**
     * Get MasterPeerName who has sent the Event.
     * 
     * @return String
     */
    public String getMasterPeerName()
    {
        return masterPeerName;
    }

    /**
     * Get SlavePeerName who has sent the Event.
     * 
     * @return String
     */
    public String getSlavePeerName()
    {
        return slavePeerName;
    }

    public void init(Element[] elm) throws InitializationException
    {
        reliability = Message.getElementString(msg, REL);
        uuid = Message.getElementString(msg, ID);
        tag = Message.getElementString(msg, TAG);
        obj = Message.getElementString(msg, OBJ);
        masterPeerName = Message.getElementString(msg, MPN);
        slavePeerName = Message.getElementString(msg, SPN);

        attributes = (Hashtable) Message.getElementObject(msg, ATS);
        if (attributes == null)
            attributes = new Hashtable();

    }

    /**
     * Create a Message Type from the content.
     * 
     * @return Message
     */
    public IMessage toMessage(Class clas)
    {

        IMessage msg = new Message();

        msg.setElement(new Element(AEvent.TYPE, clas.getName()));

        msg.setElement(new Element(REL, reliability));
        msg.setElement(new Element(ID, uuid));

        if (tag != null)
            msg.setElement(new Element(TAG, tag));

        if (obj != null)
            msg.setElement(new Element(OBJ, obj.toString()));

        if (masterPeerName != null)
            msg.setElement(new Element(MPN, masterPeerName));

        if (slavePeerName != null)
            msg.setElement(new Element(SPN, slavePeerName));

        if (attributes.size() != 0)
            msg.setElement(new Element(ATS, attributes));

        //		if (attributes != null && attributes.size() != 0) {
        //			StringHashtableMessageElement hashMsgEl =
        //				new StringHashtableMessageElement("attributes",attributes);
        //			msg = hashMsgEl.extMessage(msg);
        //		}

        return (msg);

    }

    public String toXMLString()
    {

        StringBuffer sb = new StringBuffer();

        sb.append("<AEvent\n");
        sb.append("  rel=" + reliability + "\n");
        sb.append("  uuid=" + uuid + "\n");
        sb.append("  tag=" + tag + "\n");
        sb.append("  mpn=" + masterPeerName + "\n");
        sb.append("  spn=" + slavePeerName + "\n");
        if (obj != null)
            sb.append("  obj=" + obj.toString() + "\n");
        if (attributes.size() != 0)
        {
            sb.append("  <attributes \n");
            for (Enumeration en = attributes.keys(); en.hasMoreElements();)
            {
                Object key = en.nextElement();
                Object value = attributes.get(key);
                sb.append("    name=" + key.toString() + " value=" + value.toString() + "\n");
            }
            sb.append("  />\n");
        }

        sb.append("/>");

        return sb.toString();
    }

    public Object clone() throws CloneNotSupportedException
    {
        AEvent clone = (AEvent) super.clone();

        return clone;
    }

}