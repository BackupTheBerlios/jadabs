/************************************************************************
 *
 * $Id: MicroElement.java,v 1.1 2005/01/16 14:06:37 printcap Exp $
 *
 **********************************************************************/

package ch.ethz.jadabs.jxme.microservices;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;


/**
 * This class represents an Element of a JXTA {@linkMessage}. A JXTA Message is
 * composed of several Elements. MicroElement is a striped down variant of 
 * ch.ethz.jadabs.services.MicroElement for J2ME/MIDP.
 * 
 * This is an immutable class.
 */

public final class MicroElement
{

    public static final String AOS_MIME_TYPE = "application/octet-stream";

    public static final String TEXTUTF8_MIME_TYPE = "text/plain; charset=UTF-8";

    // flags
    private static final int HAS_TYPE = 0x01;

    private static final String JXTA_ELEMENT_HEADER = "jxel";

    private String name = null;

    private byte[] data = null;

    private String nameSpace = null;

    private String mimeType = null;

    /**
     * Construct an Element from its parts.
     * 
     * @param name
     *            the name of the Element
     * 
     * @param data
     *            the data that this Element carries. This data is transported
     *            across the network as-is.
     * 
     * @param nameSpace
     *            the name space used by the Element. JXTA messages use the
     *            <code>"jxta"</code> namespace. JXTA for J2ME messages use a
     *            private namespace. If namespace is <code>null</code>, the
     *            default namespace of <code>""</code> is used.
     * 
     * @param mimeType
     *            the mimeType of the data. If <code>null</code>, the default
     *            MIME type of <code>"application/octet-stream"</code> is
     *            assumed.
     */
    public MicroElement(String name, byte[] data, String nameSpace, String mimeType)
    {
        if (name == null) { throw new IllegalArgumentException("Element name cannot be null"); }

        this.name = name;

        if (data == null) { throw new IllegalArgumentException("Element data cannot be null"); }
        this.data = data;

        if (nameSpace == null)
        {
            this.nameSpace = MicroMessage.DEFAULT_NAME_SPACE;
        } else
        {
            this.nameSpace = nameSpace;
        }

        if (mimeType == null)
        {
            this.mimeType = AOS_MIME_TYPE;
        } else
        {
            this.mimeType = mimeType;
        }
    }

    /**
     * Construct an Element from its parts.
     * 
     * @param name
     *            the name of the Element
     * 
     * @param data
     *            the data that this Element carries. This data is transported
     *            across the network with the getBytes() call.
     * 
     * @param nameSpace
     *            the name space used by the Element. JXTA messages use the
     *            <code>"jxta"</code> namespace. JXTA for J2ME messages use a
     *            private namespace. If namespace is <code>null</code>, the
     *            default namespace of <code>""</code> is used.
     * 
     * @param mimeType
     *            the mimeType of the data. If <code>null</code>, the plain
     *            text MIME type of <code>"text/plain"</code> is assumed.
     */
    public MicroElement(String name, String data, String nameSpace)
    {
        this(name, (data == null) ? null : data.getBytes(), nameSpace, TEXTUTF8_MIME_TYPE);
    }

    void write(DataOutputStream dos, Hashtable static_ns2id, Hashtable ns2id) throws IOException
    {

        // write element signature
        for (int i = 0; i < JXTA_ELEMENT_HEADER.length(); i++)
        {
            dos.writeByte(JXTA_ELEMENT_HEADER.charAt(i));
        }

        // write element name space id

        // initialize nsId to the index of Message.DEFAULT_NAME_SPACE
        int nsId = 0;
        Integer nsIdInt = (Integer) static_ns2id.get(nameSpace);
        if (nsIdInt != null)
        {
            nsId = nsIdInt.intValue();
        } else
        {
            nsIdInt = (Integer) ns2id.get(nameSpace);
            if (nsIdInt != null)
            {
                nsId = nsIdInt.intValue() + static_ns2id.size();
            }
        }
        dos.writeByte(nsId);

        // write element flags
        byte flags = 0;
        if (!AOS_MIME_TYPE.equals(mimeType))
        {
            flags |= HAS_TYPE;
        }
        dos.writeByte(flags);

        // write element name
        MicroMessage.writeString(dos, name);

        // write element mime type
        if ((flags & HAS_TYPE) != 0)
        {
            MicroMessage.writeString(dos, mimeType);
        }

        // write element data
        dos.writeInt(data.length);
        
        // dos.write(byte[]) (FilterOutputStream) is not available in J2ME
        dos.write(data,0,data.length);
    }

    static MicroElement read(DataInputStream dis, Hashtable static_id2ns, Hashtable id2ns) throws IOException
    {

        // read element signature
        for (int i = 0; i < JXTA_ELEMENT_HEADER.length(); i++)
        {
            if (dis.readByte() != JXTA_ELEMENT_HEADER.charAt(i)) { throw new IOException(
                    "Message element header not found"); }
        }

        // read element name space id
        int nsId = dis.readByte();
        Integer nsIdInt = new Integer(nsId);
        String ns = (String) static_id2ns.get(nsIdInt);
        if (ns == null)
        {
            nsId -= static_id2ns.size();
            nsIdInt = new Integer(nsId);
            ns = (String) id2ns.get(nsIdInt);
            if (ns == null) { throw new IOException("Namespace not found for id " + nsId); }
        }

        // read element flags
        byte flags = dis.readByte();

        // read element name
        String name = MicroMessage.readString(dis);

        // read element mime type
        String mimeType = AOS_MIME_TYPE;
        if ((flags & HAS_TYPE) != 0)
        {
            mimeType = MicroMessage.readString(dis);
        }

        // read element data
        int len = dis.readInt();
        byte[] data = new byte[len];
        dis.readFully(data);

        return new MicroElement(name, data, ns, mimeType);
    }

    /**
     * Return the name of the Element.
     * 
     * @return the Element name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Return the namespace used by the Element name.
     * 
     * @return the Element namespace
     */
    public String getNameSpace()
    {
        return nameSpace;
    }

    /**
     * Return the MIME type of the data in the Element.
     * 
     * @return the Element MIME type
     */
    public String getMimeType()
    {
        return mimeType;
    }

    /**
     * Return the data in the Element.
     * 
     * @return the Element data
     */
    public byte[] getData()
    {
        return data;
    }

    /**
     * Return a String representation of the Element.
     * 
     * @return a string representation of the Element
     */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("\"");
        sb.append(nameSpace);
        sb.append(":");
        sb.append(name);
        sb.append("\" \"");
        sb.append(mimeType);
        sb.append("\" dlen=");
        sb.append(Integer.toString(data.length));
        
        if (mimeType.equals(TEXTUTF8_MIME_TYPE) || mimeType.equals(AOS_MIME_TYPE))
            sb.append(" \"data=" + new String(data) + "\"");

        return sb.toString();
    }
  
}

