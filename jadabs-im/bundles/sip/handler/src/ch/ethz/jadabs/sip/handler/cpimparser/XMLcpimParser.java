/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
 * Creator: O. Deruelle (deruelle@nist.gov)                                     *
 * Questions/Comments: nist-sip-dev@antd.nist.gov                               *
 *******************************************************************************/
package ch.ethz.jadabs.sip.handler.cpimparser;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * parser for a XML file
 */
public class XMLcpimParser extends DefaultHandler
{
    private Logger LOG = Logger.getLogger(XMLcpimParser.class.getName());
    
    private PresenceTag presenceTag;

    private PresentityTag presentityTag;

    private StatusTag statusTag;

    private TupleTag tupleTag;

    private ContactTag contactTag;

    private ValueTag valueTag;

    private NoteTag noteTag;

    private String element;

    private XMLReader saxParser;

    /**
     * start the parsing
     * 
     * @param file
     *            to parse
     * @return Vector containing the test cases
     */
    public XMLcpimParser(String fileLocation)
    {
        try
        {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            this.saxParser = saxParserFactory.newSAXParser().getXMLReader();
            saxParser.setContentHandler(this);
            saxParser.setFeature("http://xml.org/sax/features/validation", true);
            // parse the xml specification for the event tags.
            saxParser.parse(fileLocation);

        } catch (SAXParseException spe)
        {
            spe.printStackTrace();
        } catch (SAXException sxe)
        {
            sxe.printStackTrace();
        } catch (IOException ioe)
        {
            // I/O error
            ioe.printStackTrace();
        } catch (Exception pce)
        {
            // Parser with specified options can't be built
            pce.printStackTrace();
        }
    }

    /**
     * start the parsing
     * 
     * @param file
     *            to parse
     * @return Vector containing the test cases
     */
    public XMLcpimParser()
    {
        try
        {
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            this.saxParser = saxParserFactory.newSAXParser().getXMLReader();
            saxParser.setContentHandler(this);
            saxParser.setFeature("http://xml.org/sax/features/validation", true);
            // parse the xml specification for the event tags.

        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void parseCPIMString(String body)
    {
        try
        {
            StringReader stringReader = new StringReader(body);
            InputSource inputSource = new InputSource(stringReader);
            this.saxParser.parse(inputSource);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public PresenceTag getPresenceTag()
    {
        return presenceTag;
    }

    //===========================================================
    // SAX DocumentHandler methods
    //===========================================================

    public void startDocument() throws SAXException
    {
        try
        {
            LOG.debug("Parsing XML cpim string");
        } catch (Exception e)
        {
            throw new SAXException("XMLcpimParser error", e);
        }
    }

    public void endDocument() throws SAXException
    {
        try
        {
            LOG.debug("XML cpim string parsed successfully!!!");
        } catch (Exception e)
        {
            throw new SAXException("XMLcpimParser error", e);
        }
    }

    public void startElement(String namespaceURI, String lName, // local name
            String qName, // qualified name
            Attributes attrs) throws SAXException
    {
        element = qName;
        LOG.debug("StartElement:" + element);
        if (element.compareToIgnoreCase("presence") == 0)
        {
            //LOG.debug("presence!!!!");
            presenceTag = new PresenceTag();
            String entity = attrs.getValue("entity").trim();
            presenceTag.setEntity(entity);
            //LOG.debug("presence!!!!");
        }
        if (element.compareToIgnoreCase("presentity") == 0)
        {
            //LOG.debug("presentity!!!!");
            presentityTag = new PresentityTag();
            String id = attrs.getValue("id").trim();
            presentityTag.setId(id);
            //LOG.debug("presentity!!!!");
        }
        if (element.compareToIgnoreCase("tuple") == 0)
        {
            //LOG.debug("tuple!!!!");
            tupleTag = new TupleTag();
            String id = attrs.getValue("id").trim();
            tupleTag.setId(id);
            //LOG.debug("tuple!!!!");
        }
        if (element.compareToIgnoreCase("status") == 0)
        {
            //LOG.debug("status!!!!");
            statusTag = new StatusTag();
            //LOG.debug("status!!!!");
        }
        if (element.compareToIgnoreCase("basic") == 0)
        {
            //LOG.debug("basic!!!!");
            valueTag = new ValueTag();
            //LOG.debug("basic!!!!");
        }
        if (element.compareToIgnoreCase("contact") == 0)
        {
            //LOG.debug("contact!!!!");
            contactTag = new ContactTag();
            String priority = attrs.getValue("priority").trim();
            if (priority != null)
            {
                try
                {
                    contactTag.setPriority(Float.parseFloat(priority));
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            //LOG.debug("contact!!!!");
        }
        if (element.compareToIgnoreCase("note") == 0)
        {
            //LOG.debug("note!!!!");
            noteTag = new NoteTag();
            //LOG.debug("note!!!!");
        }
    }

    public void endElement(String namespaceURI, String sName, // simple name
            String qName // qualified name
    ) throws SAXException
    {
        String element = qName;
        if (element.compareToIgnoreCase("presence") == 0)
        {
        }
        if (element.compareToIgnoreCase("presentity") == 0)
        {
            //LOG.debug("/presentity!!!!");
            presenceTag.setPresentityTag(presentityTag);
            //LOG.debug("/presentity!!!!");
        }
        if (element.compareToIgnoreCase("tuple") == 0)
        {
            //LOG.debug("/tuple!!!!");
            presenceTag.addTupleTag(tupleTag);
            //LOG.debug("/tuple!!!!");
        }
        if (element.compareToIgnoreCase("status") == 0)
        {
            //LOG.debug("/status!!!!");
            tupleTag.setStatusTag(statusTag);
            //LOG.debug("/status!!!");
        }
        if (element.compareToIgnoreCase("basic") == 0)
        {
            //LOG.debug("/basic!!!!");
            statusTag.setValueTag(valueTag);
            //LOG.debug("/basic!!!!");
        }
        if (element.compareToIgnoreCase("contact") == 0)
        {
            //LOG.debug("/contact!!!!");
            tupleTag.setContactTag(contactTag);
            //LOG.debug("/contact!!!!");
        }
        if (element.compareToIgnoreCase("note") == 0)
        {
            //LOG.debug("/note!!!!");
            tupleTag.setNoteTag(noteTag);
            //LOG.debug("//note!!!!");
        }
    }

    public void characters(char buf[], int offset, int len) throws SAXException
    {
        String str = new String(buf, offset, len);
        if (str != null && !str.trim().equals(""))
        {
            if (element.compareToIgnoreCase("basic") == 0)
            {
                valueTag.setValue(str);

            }
            if (element.compareToIgnoreCase("contact") == 0)
            {
                contactTag.setContact(str);

            }
            if (element.compareToIgnoreCase("note") == 0)
            {
                noteTag.setNote(str);

            }
        }
    }

}