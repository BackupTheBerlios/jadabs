/************************************************************************
 *
 * $Id: MicroMessage.java,v 1.1 2005/01/16 14:06:37 printcap Exp $
 *
 **********************************************************************/

package ch.ethz.jadabs.jxme.microservices;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class represents a JXTA Message. A JXTA Message is composed of several
 * {@link Element}s. The Elements can be in any order, but certain elements are
 * reserved for use by the JXTA Network. These private Elements use a private
 * namespace.
 * <p>
 * 
 * It also defines convenience methods for accessing commonly-used properties
 * for handling responses to the asynchronous operations defined in
 * {@link PeerNetwork}.
 * <p>
 * 
 * This is an immutable class.
 */
public final class MicroMessage
{

    public static final String GROUP_ID_TAG = "GroupId";

    public static final String ENDPOINTDEST_TAG = "EndpointDestAddress";

    public static final String DEFAULT_NAME_SPACE = "";

    public static final String JXTA_NAME_SPACE = "jxta";

    //public static final String PROXY_NAME_SPACE = "jxta"; //TBD: remove this
    // defintion "proxy";
    public static final String DEFAULT_MIME_TYPE = "application/x-jxta-msg";

    public static final String REQUESTID_TAG = "requestId";

    public static final String TYPE_TAG = "type";

    public static final String NAME_TAG = "Name";

    public static final String ID_TAG = "id";

    public static final String DESC_TAG = "desc";

    public static final String ARG_TAG = "arg";

    public static final String ATTRIBUTE_TAG = "attr";

    public static final String VALUE_TAG = "value";

    public static final String THRESHOLD_TAG = "threshold";

    public static final String ENDPOINTSRC_TAG = "EndpointSourceAddress";

    public static final String SRCEA_TAG = "srcEA";
    
    public static final String HOPCOUNT_TAG = "hc";

    //public static final String HELLO ="hello";
    public static final String WELCOME = "welcome";

    public static final String NUM_URI_TAG = "NumberURI";

    public static final String MESSAGE_TYPE_TAG = "messageType";

    //public static final String REQUEST_TAG = "request";
    //public static final String RESPONSE_TAG = "response";
    public static final String ERROR_TAG = "error";

    public static final String REQUEST_JOIN = "join";

    public static final String REQUEST_CREATE = "create";

    public static final String REQUEST_SEARCH = "search";

    public static final String REQUEST_RESOLVE = "resolve";

    public static final String REQUEST_LISTEN = "listen";

    public static final String REQUEST_CLOSE = "close";

    public static final String REQUEST_SEND = "send";

    public static final String RESPONSE_SUCCESS = "success";

    public static final String RESPONSE_ERROR = "error";

    public static final String RESPONSE_INFO = "info";

    public static final String RESPONSE_RESULT = "result";

    public static final String RESPONSE_MESSAGE = "data";

    public static final String RESPONSE_TYPE = "type";

    private static final String JXTA_MESSAGE_HEADER = "jxmg";

    private static final int MESSAGE_VERSION = 0;

    private static Hashtable static_ns2id = new Hashtable(2);

    private static Hashtable static_id2ns = new Hashtable(2);

    private static final ByteCounterOutputStream byteCounter = new ByteCounterOutputStream();

    private static final DataOutputStream dataCounter = new DataOutputStream(byteCounter);

    /**
     * An empty Message to send when we have no outgoing message. This helps
     * maintain a persistent connection to an HTTP relay.
     */
    public static final MicroMessage EMPTY = new MicroMessage();

    /**
     * array of elements with the data in them or other information in them
     */
    private MicroElement[] elements = null;

    private int nextNameSpaceId = 0;

    static
    {
        int nextNameSpaceId = 0;
        Integer id = new Integer(nextNameSpaceId++);
        static_ns2id.put(DEFAULT_NAME_SPACE, id);
        static_id2ns.put(id, DEFAULT_NAME_SPACE);

        id = new Integer(nextNameSpaceId++);
        static_ns2id.put(JXTA_NAME_SPACE, id);
        static_id2ns.put(id, JXTA_NAME_SPACE);
    }

    /**
     * Construct an empty Message, without any Elements.
     */
    private MicroMessage()
    {
        elements = new MicroElement[0];
    }

    /**
     * Construct a Message from an array of Elements. The supplied Elements are
     * passed along as-is to the relay. Typically, these Elements would hold
     * application data. Internally, JXTA for J2ME may add its own Elements to
     * the Message for routing and other purposes.
     * 
     * @param elms
     *            an array of elements
     */
    public MicroMessage(MicroElement[] elms)
    {
		elements = new MicroElement[elms.length];
		for (int i=0; i < elms.length; i++) {
			elements[i] = elms[i];
		}
    }

    /**
     * Return the number of Elements contained in this Message. Usage:
     * 
     * <p>
     * <code>
     * for (int i=0; i < msg.getElementCount(); i++) { <br>
     * &nbsp;&nbsp;Element el = msg.getElement(i); <br>
     * &nbsp;&nbsp;... <br>
     * }
     * </code>
     * </p>
     * 
     * @return the number of Elements in this Message.
     */
    public int getElementCount()
    {
        return elements.length;
    }
    
	public MicroElement getElement(int index){
		return elements[index];
	}
	
    public MicroElement getElement(String tag)
    {
        for (int i = 0; i < elements.length; i++)
        {
            if (elements[i].getName().equals(tag))
                return elements[i];
        }
        
        return null;
    }
    
    public MicroElement[] getElements()
    {
        return elements;
    }
    
    /**
     * writes to a stream for serializing
     * 
     * @param dos
     * @throws IOException
     */
    public void write(DataOutputStream dos) throws IOException
    {

		// write message signature
		for (int i=0; i < JXTA_MESSAGE_HEADER.length(); i++) {
			dos.writeByte(JXTA_MESSAGE_HEADER.charAt(i));
		}

		// write message version
		dos.writeByte(MESSAGE_VERSION);

		// calculate namespace table indices

		/* there can be at most elements.length namespaces. We specify
		   the Hashtable size because mostly, element count will be
		   around 3-5 and using the default Hashtable size of 11 would
		   be quite wasteful */
		Hashtable ns2id = new Hashtable(elements.length);
		int nsId = nextNameSpaceId;
		for (int i = 0; i < elements.length; i++) {
			String ns = elements[i].getNameSpace();
			if (static_ns2id.get(ns) == null && 
				ns2id.get(ns) == null) {
				ns2id.put(ns, new Integer(nsId++));
			}
		}

		// write message name spaces
		dos.writeShort(ns2id.size());
		Enumeration nse = ns2id.keys();
		while(nse.hasMoreElements()) {
			String ns = (String) nse.nextElement();
			writeString(dos, ns);
		}

		// write message element count
		int elementCount = getElementCount();
		dos.writeShort(elementCount);
		for (int i=0; i < elementCount; i++) {
			// write message elements
			getElement(i).write(dos, static_ns2id, ns2id);
		}
    }

    /**
     * reading a serialized mesg
     * 
     * @param dis
     * @return {@link Message}
     * @throws IOException
     */
    public static MicroMessage read(DataInputStream dis) throws IOException
    {

        // read message signature
        for (int i = 0; i < JXTA_MESSAGE_HEADER.length(); i++)
        {
            if (dis.readByte() != JXTA_MESSAGE_HEADER.charAt(i)) { throw new IOException("Message header not found"); }
        }

        // read message version
        int version = dis.readByte();
        if (version != MESSAGE_VERSION) { throw new IOException("Message version mismatch: expected " + MESSAGE_VERSION
                + ", got " + version); }

        // read message name spaces
        int nsCount = dis.readShort();
        String[] nameSpaces = new String[nsCount];
        for (int i = 0; i < nsCount; i++)
        {
            String ns = readString(dis);
            nameSpaces[i] = ns;
        }

        // read message element count
        int elementCount = dis.readShort();
        MicroElement[] elms = new MicroElement[elementCount];

        // create name space indices
        Hashtable id2ns = new Hashtable(nsCount);
        for (int i = 0; i < nsCount; i++)
        {
            id2ns.put(new Integer(i), nameSpaces[i]);
        }

        for (int i = 0; i < elementCount; i++)
        {
            // read message elements
            elms[i] = MicroElement.read(dis, static_id2ns, id2ns);
        }

        MicroMessage msg = new MicroMessage(elms);
        return msg;
    }

    static String readString(DataInputStream dis) throws IOException
    {

        int len = dis.readShort();
        if (len < 0) { throw new IOException("Negative string length in message"); }
        byte[] bytes = new byte[len];
        dis.readFully(bytes);
        return new String(bytes);
    }

    static void writeString(DataOutputStream dos, String s) throws IOException
    {

        dos.writeShort(s.length());
        byte[] b=s.getBytes();
        dos.write(b, 0, b.length);
    }

    /**
     * Returns the size in bytes of this Message.
     */
    public int getSize()
    {
        synchronized (byteCounter)
        {
            byteCounter.reset();
            synchronized (dataCounter)
            {
                try
                {
                    write(dataCounter);
                } catch (IOException ex)
                {
                    throw new RuntimeException("ByteCounter should never " + " throw an IOException");
                }
            }
            return byteCounter.size();
        }
    }
    
    public String toXMLString()
    {

        StringBuffer sb = new StringBuffer();

        sb.append("<message>\n");

        for (int i = 0; i < elements.length; i++)
        {
            sb.append(elements[i].toString() + "\n");
        }

        sb.append("</message>");

        return sb.toString();
    }
}

