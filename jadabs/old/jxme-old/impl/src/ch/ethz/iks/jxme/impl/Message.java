/**
 * JXMEbt
 * ch.ethz.iks.jxme.bluetooth.impl
 * Message.java
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
 * $Id: Message.java,v 1.1 2004/11/08 07:30:34 afrei Exp $
 * */
package ch.ethz.iks.jxme.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.ethz.iks.jxme.IElement;
import ch.ethz.iks.jxme.IMessage;
import ch.ethz.iks.utils.StringDataInputStream;
import ch.ethz.iks.utils.StringDataOutputStream;

/**
 * @author daniel
 */
public class Message implements IMessage
{

    private static Logger logger = Logger.getLogger(Message.class.getName());

    /* * static fields from net.jxta.j2me.Message */
    public static final String DEFAULT_NAME_SPACE = "";

    public static final String JXTA_NAME_SPACE = "jxta";

    public static final String PROXY_NAME_SPACE = "proxy";

    public static final String DEFAULT_MIME_TYPE = "application/x-jxta-msg";

    public static final String REQUESTID_TAG = "requestId";

    public static final String TYPE_TAG = "type";

    public static final String NAME_TAG = "name";

    public static final String ID_TAG = "id";

    public static final String ARG_TAG = "arg";

    public static final String ATTRIBUTE_TAG = "attr";

    public static final String VALUE_TAG = "value";

    public static final String THRESHOLD_TAG = "threshold";

    public static final String REQUEST_TAG = "request";

    public static final String RESPONSE_TAG = "response";

    public static final String ERROR_TAG = "error";

    public static final String REQUEST_JOIN = "join";

    public static final String REQUEST_CREATE = "create";

    public static final String REQUEST_SEARCH = "search";

    //public static final String REQUEST_LISTEN = "listen";
    public static final String REQUEST_CLOSE = "close";

    public static final String REQUEST_SEND = "send";

    public static final String RESPONSE_SUCCESS = "success";

    public static final String RESPONSE_ERROR = "error";

    public static final String RESPONSE_INFO = "info";

    public static final String RESPONSE_RESULT = "result";

    public static final String RESPONSE_MESSAGE = "data";

    private static final String JXTA_MESSAGE_HEADER = "jxmg";

    private static final int MESSAGE_VERSION = 0;

    /** SENDER specifies the name for the (name,value) in an element. */
    public static final String SENDER = "sender";

    /** RECEIVER specifies the name for the (name,value) in an element. */
    public static final String RECEIVER = "receiver";

    private Vector _elements = null;

    private String _mimeType;

    private String _nameSpace;

    private String uuid = null;

    public Message()
    {
        uuid = "uuid:test:0";
        _elements = new Vector();
    }

    public Message(String uuid)
    {
        this();
        this.uuid = uuid;
    }

    public Message(IElement element)
    {
        this();
        setElement(element);
    }

    /**
     * @param elements
     *            Array of Element containing the data elements of this message
     * @param mimeType
     *            defines the mime type of this message. This parameter can be
     *            <code>null</code>.
     * @param nameSpace
     *            defines the name space of this message. This paramter can be
     *            <code>null</code>
     * @throws IllegalArgumentException
     *             if the parameter elements is <code>null</code> an
     *             IllegalArgumentException will be thrown.
     */
    public Message(IElement[] elements)
    {

        this();

        if (elements != null)
        {
            _elements = new Vector(elements.length);
            for (int i = 0; i < elements.length; i++)
            {
                _elements.add(elements[i]);
            }
        } else
        {
            logger.fatal("Element[] elements == null is not allowed");
            throw new IllegalArgumentException("Element[] elements == null is not allowed");
        }
    }

    public String getID()
    {
        return uuid;
    }

    /** Initializing Logger */
    //	private void initLogger(){
    //		if (cAppender == null){
    //			ConsoleAppender appender = new ConsoleAppender(new PatternLayout());
    //			logger.addAppender(appender);
    //		}
    //	}
    public IElement getElement(int index)
    {
        return (IElement) _elements.get(index);
    }

    public int getElementCount()
    {
        return _elements.size();
    }

    public IElement getElement(String name)
    {

        // we suppose there is only one element with the the given name
        for (Enumeration en = _elements.elements(); en.hasMoreElements();)
        {
            IElement element = (IElement) en.nextElement();

            if (element.getName().equals(name))
                return element;
        }

        // no element found
        return null;
    }

    /**
     * Return the Element value for the given name as a String.
     * 
     * @param name
     *            the name of the (name,value) pair
     * @return
     */
    public static String getElementString(IMessage msg, String name) throws IOException
    {

        IElement element = msg.getElement(name);
        String value = null;

        try
        {
            if (element != null)
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                out.write(element.getData());
                value = out.toString();
                out.close();
                return value;
            } else
                return null;

        } catch (IOException ioe)
        {

            logger.error("could not generate Element value from bytearray", ioe);

            throw ioe;
        }
    }

    public static int getElementInt(IMessage msg, String name)
    {

        IElement element = msg.getElement(name);
        return Integer.decode(new String(element.getData())).intValue(); 
        
    }

    public static long getElementLong(IMessage msg, String name)
    {
        IElement element = msg.getElement(name);
        return Long.decode(new String(element.getData())).longValue();        
    }
    
    public static Object getElementObject(IMessage msg, String name) throws IOException
    {

        IElement element = msg.getElement(name);
        Object value = null;

        try
        {

            if (element != null)
            {
                ByteArrayInputStream bytesin = new ByteArrayInputStream(element.getData());
                ObjectInputStream objin = new ObjectInputStream(bytesin);

                try
                {
                    value = objin.readObject();
                } catch (ClassNotFoundException cne)
                {
                    logger.error("couldn't find class", cne);
                }

                objin.close();
                bytesin.close();

                return value;
            } else
                return null;

        } catch (IOException ioe)
        {

            logger.error("could not generate Element value from bytearray", ioe);

            throw ioe;
        }

    }

    public int getSize()
    {
        return 0;
    }

    public void writeMessage(OutputStream out) throws IOException
    {
        StringDataOutputStream dataOut = new StringDataOutputStream(out);
        writeMessage(dataOut);
        //		dataOut.close();
    }

    public void writeMessage(StringDataOutputStream out) throws IOException
    {
        // collect all namespaces
        String[] nameSpaces = collectNameSpaces();

        // writing the message header
        out.writeString(JXTA_MESSAGE_HEADER);

        // write message version
        out.write(MESSAGE_VERSION);

        //write the number of namespaces (2 bytes)
        out.writeShort(nameSpaces.length - 2);

        //begin:loop loop over all name spaces
        for (int index = 2; index < nameSpaces.length; index++)
        {
            //write name spaces's length (2 bytes)
            out.writeShort(nameSpaces[index].length());

            // write name space's name
            out.writeString(nameSpaces[index]);
        } //end:loop

        //write the number of elements in this message (2 bytes)
        out.writeShort(_elements.size());

        //begin:loop loop over all elements of the message
        Enumeration enumeration = _elements.elements();
        IElement tmp = null;
        while (enumeration.hasMoreElements())
        {
            tmp = (IElement) enumeration.nextElement();
            tmp.write(out, nameSpaces);
        }
        //end:loop
    }

    /**
     * This method reads a message form a given DataInputStream
     * 
     * @param DataInputStream
     *            to read from
     * @throws IOException
     * @throws MessageParseException
     *             is thrown if the message cannot be read
     * @throws ElementParseException
     *             is thrown if an element contained by the message cannot be
     *             read
     */
    public static IMessage read(StringDataInputStream in) throws IOException, MessageParseException,
            ElementParseException
    {
        // check JXTA message header -> must be 'jxmg'
        for (int index = 0; index < JXTA_MESSAGE_HEADER.length(); index++)
        {
            if (JXTA_MESSAGE_HEADER.charAt(index) != in.read()) { throw new MessageParseException(
                    "malformed JXTA_MESSAGE_HEADER"); }
        }

        // check JXTA message version
        if (in.read() != 0) { throw new MessageParseException("message is not of version 0."); }

        // read userdefined name spaces
        int numberOfNameSpaces = in.readShort();
        String[] nameSpaces = new String[numberOfNameSpaces + 2];
        nameSpaces[0] = DEFAULT_NAME_SPACE;
        nameSpaces[1] = JXTA_NAME_SPACE;
        for (int index = 0; index < numberOfNameSpaces; index++)
        {
            int length = in.readShort();
            nameSpaces[index + 2] = in.readString(length);
        }

        // read the number of elements contained in this message
        int numberOfElements = in.readShort();
        IElement[] elements = new IElement[numberOfElements];
        for (int index = 0; index < numberOfElements; index++)
        {
            elements[index] = Element.readElement(in, nameSpaces);
        }
        return new Message(elements);
    }

    /**
     * This method reads a message from a given InputStream
     * 
     * @throws IOException
     * @throws MessageParseException
     *             is thrown if the message cannot be read
     * @throws ElementParseException
     *             is thrown if an element contained by the message cannot be
     *             read
     */
    public static IMessage read(InputStream in) throws IOException, MessageParseException, ElementParseException
    {
        StringDataInputStream dataIn = new StringDataInputStream(in);
        IMessage message = read(dataIn);
        dataIn.close();
        return message;
    }

    /**
     * Inserts an element at given position to this message
     * 
     * @param element
     *            element to be inserted
     * @param index
     *            position to insert element
     */
    public void setElement(IElement element, int index)
    {
        _elements.add(index, element);
    }

    /**
     * Appends an element to the collection of elements.
     * 
     * @param element
     *            element to attach to this message
     */
    public void setElement(IElement element)
    {
        _elements.add(element);
    }

    /**
     * Iterates over all elements and collects the namespaces. pos 0: empty name
     * space pos 1: "jxta" name space
     * 
     * @return String[] containing the namespaces
     */
    public String[] collectNameSpaces()
    {
        /* 2 default name spaces ["", "jxta"] plus a name space per element */
        String[] nameSpaceArray = new String[_elements.size() + 2];
        int numOfNameSpaces = 2;
        nameSpaceArray[0] = DEFAULT_NAME_SPACE;
        nameSpaceArray[1] = JXTA_NAME_SPACE;

        Enumeration enumeration = _elements.elements();
        while (enumeration.hasMoreElements())
        {
            IElement currentElement = (IElement) enumeration.nextElement();
            boolean foundNameSpace = false;
            for (int index = 0; index < nameSpaceArray.length; index++)
            {
                if (currentElement.getNameSpace().equals(nameSpaceArray[index]))
                {
                    foundNameSpace = true;
                    break;
                }
            }
            if (!foundNameSpace)
            {
                nameSpaceArray[numOfNameSpaces++] = currentElement.getNameSpace();
            }
        }

        String[] stripedNameSpaceArray = new String[numOfNameSpaces];
        for (int index = 0; index < numOfNameSpaces; index++)
        {
            stripedNameSpaceArray[index] = nameSpaceArray[index];
        }
        return stripedNameSpaceArray;
    }

    public String toXMLString()
    {

        StringBuffer sb = new StringBuffer();

        sb.append("<message>\n");

        for (Enumeration en = _elements.elements(); en.hasMoreElements();)
        {
            IElement el = (IElement) en.nextElement();

            sb.append(el.toXMLString() + "\n");
        }

        sb.append("</message>");

        return sb.toString();
    }

}