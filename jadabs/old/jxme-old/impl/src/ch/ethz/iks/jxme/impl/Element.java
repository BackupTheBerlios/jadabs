/**
 * JXMEbt
 * ch.ethz.iks.jxme.bluetooth.impl
 * Element.java
 * 
 * @author Daniel Kaeppeli, danielka@student.ethz.ch
 *
 * 17.06.2003
 *
 * Diploma Theses: JXTA Over Bluetooth
 * 
 * Department Of Computer Science
 * Swiss Federal Institute of Technology, Zurich
 * 
 * $Id: Element.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 * */
package ch.ethz.iks.jxme.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import ch.ethz.iks.jxme.IElement;
import ch.ethz.iks.utils.StringDataInputStream;
import ch.ethz.iks.utils.StringDataOutputStream;

/**
 * @author daniel
 *  
 */
public class Element implements IElement
{

    static public final String DEFAULT_MIME_TYPE = "application/octet-stream";

    static public final String DEFAULT_NAME_SPACE = Message.DEFAULT_NAME_SPACE;

    static public final String ELEMENT_HEADER = "jxel";

    // flags
    private static final int HAS_TYPE = 0x01;

    static Logger logger = Logger.getLogger(Element.class.getName());

    private byte[] _data = null;

    private String _mimeType = null;

    private String _name = null;

    private String _nameSpace = null;

    /**
     * @param data
     *            byte array containing the data of this element, must be not
     *            <code>null</code>. If
     *            <code>data</ code> is <code>null</code> an IllegalArgumentException is thrown.
     * @param name of this elment, must be not <code>null</code> 
     *     If <code>name</ code> is <code>null</code> an IllegalArgumentException is thrown.
     */
    public Element(byte[] data, String name)
    {

        if (data == null)
        {
            logger.error("byte[] data is null");
            throw new IllegalArgumentException("byte data must be not null");
        }
        if (name == null)
        {
            logger.error("String name is null");
            throw new IllegalArgumentException("String name must be not null");
        }
        _data = data;
        _name = name;
        _mimeType = DEFAULT_MIME_TYPE;
        _nameSpace = DEFAULT_NAME_SPACE;
    }

    /**
     * @param data
     *            byte array containing the data of this element, must be not
     *            <code>null</code>. If
     *            <code>data</ code> is <code>null</code> an IllegalArgumentException is thrown.
     * @param name of this elment, must be not <code>null</code> 
     *     If <code>name</ code> is <code>null</code> an IllegalArgumentException is thrown.
     * @param mimeType defines the mime type of this element, can be <code>null</code>
     * @param nameSpace defines the name space of this element, can be <code>null</code>
     */
    public Element(byte[] data, String name, String mimeType, 
            String nameSpace)
    {

        if (data == null)
        {
            logger.error("byte[] name is null");
            throw new IllegalArgumentException("byte[] data must be not null");
        }

        if (name == null)
        {
            logger.error("String name is null");
            throw new IllegalArgumentException("String name must be not null");
        }

        _data = data;
        _name = name;

        if (mimeType == null)
        {
            _mimeType = DEFAULT_MIME_TYPE;
        } else
        {
            _mimeType = mimeType;
        }

        if (nameSpace == null)
        {
            _nameSpace = DEFAULT_NAME_SPACE;
        } else
        {
            _nameSpace = nameSpace;
        }
    }

    /**
     * Create an Element out of a (name, value) pair, where both are strings.
     * 
     * @param name -
     *            String, shouldn't be null
     * @param value
     */
    public Element(String name, String value)
    {
        this(value.getBytes(), name);
    }

    /**
     * Create an Element out of a (name, value) pair.
     * 
     * @param name -
     *            String, shouldn't be null
     * @param valueint -
     *            has to be an integer
     */
    public Element(String name, int valueint)
    {
        this(Integer.toString(valueint).getBytes(), name);
    }

    /**
     * Create an Element out of a (name, value) pair.
     * 
     * @param name -
     *            String, shouldn't be null
     * @param valuelong -
     *            has to be a long
     */
    public Element(String name, long valuelong)
    {
        this(Long.toString(valuelong).getBytes(), name);
    }
    
    /**
     * Create an Element out of a (name, value) pair, where both are strings.
     * 
     * @param name -
     *            String, shouldn't be null
     * @param value
     */
    public Element(String name, Object value)
    {

        ByteArrayOutputStream bytesout = new ByteArrayOutputStream();
        ObjectOutputStream objout;
        try
        {
            objout = new ObjectOutputStream(bytesout);

            objout.writeObject(value);
            objout.flush();
            bytesout.flush();

            objout.close();

        } catch (IOException e)
        {
            logger.error("couldn't write hashtable to outputstream", e);
        }

        _data = bytesout.toByteArray();
        _name = name;

        _mimeType = DEFAULT_MIME_TYPE;
        _nameSpace = DEFAULT_NAME_SPACE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.jxme.bt.Element#getData()
     */
    public byte[] getData()
    {
        return _data;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.jxme.bt.Element#getData()
     */
    public int getIntData()
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
		try
        {
            out.write(_data);
            
    		int value = Integer.parseInt(out.toString());
    		out.close();
    		
    		return value;
        } catch (IOException e)
        {
            //af: ok, I should handle this somewhere else...
            System.out.println("error in parsing data: "+_data);
            
            return -1;
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.jxme.bt.Element#getMimeType()
     */
    public String getMimeType()
    {
        return _mimeType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.jxme.bt.Element#getName()
     */
    public String getName()
    {
        return _name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.jxme.bt.Element#getNameSpace()
     */
    public String getNameSpace()
    {
        return _nameSpace;
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        sb.append("\"");
        sb.append(_nameSpace);
        sb.append(":");
        sb.append(_name);
        sb.append("\" \"");
        sb.append(_mimeType);
        sb.append("\" dlen=");
        sb.append(Integer.toString(_data.length));

        return sb.toString();
    }

    public void write(OutputStream out, String[] nameSpaces) throws IOException
    {
        StringDataOutputStream dataOut = new StringDataOutputStream(out);
        write(dataOut, nameSpaces);
        dataOut.flush();
        out.flush();
        //		dataOut.close();
        //		out.close();
    }

    public void write(StringDataOutputStream out, String[] nameSpaces)
            throws IOException
    {
        // write element header
        out.writeString(ELEMENT_HEADER);

        // write number of name space of this element
        for (int index = 0; index < nameSpaces.length; index++)
        {
            if (nameSpaces[index].compareTo(_nameSpace) == 0)
            {
                out.write(index);
            }
        }

        // write number of mime type of this element
        // default mime type: 0
        byte flags = 0;
        if (_mimeType.compareTo(DEFAULT_MIME_TYPE) != 0)
        {
            flags |= HAS_TYPE;
        }
        out.write(flags);

        // write the length of name
        out.writeShort(_name.length());

        // write name
        out.writeString(_name);

        // write namespace, if necessary (flag HAS_TYPE is set)
        if ((flags & HAS_TYPE) != 0)
        {
            out.writeShort(_mimeType.length());
            out.writeString(_mimeType);
        }

        // write the length of the data
        out.writeInt(_data.length);

        // write data
        out.write(_data);
    }

    /**
     * this method creates a BTElement out of a InputStream
     * 
     * @param in
     *            InputStream containing a BTElement
     * @throws Throws
     *             a ElementParseException if the stream doesn't contain a valid
     *             element byte stream
     */
    public static Element readElement(InputStream in, String[] nameSpaces)
            throws IOException, ElementParseException
    {
        Element btElement;
        StringDataInputStream dataIn = new StringDataInputStream(in);
        btElement = readElement(dataIn, nameSpaces);
        dataIn.close();

        return btElement;
    }

    /**
     * this method creates a BTElement out of a DataInputStream
     * 
     * @param in
     *            InputStream containing a BTElement
     * @throws Throws
     *             a ElementParseException if the stream doesn't contain a valid
     *             element byte stream
     */
    public static Element readElement(StringDataInputStream in,
            String[] nameSpaces) throws IOException, ElementParseException
    {
        boolean hasType = false;

        // read element header
        for (int index = 0; index < ELEMENT_HEADER.length(); index++)
        {
            int currentByte = in.readByte();
            if (currentByte != ELEMENT_HEADER.charAt(index)) { throw new ElementParseException(
                    "invalid header"); }
        }

        // read the number of this name space and check if this number is in
        // nameSpaces
        int nameSpaceIndex = in.read();
        if (nameSpaces.length <= nameSpaceIndex)
        {
            logger.fatal("invalid nameSpace");
            throw new ElementParseException("invalid name space index");
        }

        // read flags and check it
        int flags = in.read();
        if ((flags & HAS_TYPE) != 0)
        {
            hasType = true;
        }

        // read length of name
        int nameLength = in.readShort();

        // read name
        String name = in.readString(nameLength);

        // if there is set the mimetype flag read the mimetype length and the
        // mimetype
        String mimeType = DEFAULT_MIME_TYPE;
        if (hasType)
        {
            int mimeLength = in.readShort();
            mimeType = in.readString(mimeLength);
        }

        // read length of data
        int payloadLength = in.readInt();

        // read data
        byte[] data = new byte[payloadLength];
        if (in.read(data) != payloadLength) { throw new ElementParseException(
                "invalid payload size"); }

        // create BTElement
        Element btElement = new Element(data, name, mimeType,
                nameSpaces[nameSpaceIndex]);

        return btElement;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.ethz.iks.jxme.bt.Element#getSize()
     */
    public int getSize()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public String toXMLString()
    {

        StringBuffer sb = new StringBuffer();

        sb.append("   <element");

        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(_data);

            sb.append(" name='" + _name + "' value='" + out.toString() + "'");

            out.close();

        } catch (IOException ioe)
        {
            logger.error(ioe);
        }

        sb.append(" />");

        return sb.toString();
    }

}

