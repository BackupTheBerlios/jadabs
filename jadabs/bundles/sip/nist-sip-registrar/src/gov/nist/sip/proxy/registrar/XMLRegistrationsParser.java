/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
 * Creator: O. Deruelle (deruelle@nist.gov)                                     *
 * Questions/Comments: nist-sip-dev@antd.nist.gov                               *
 *******************************************************************************/
package gov.nist.sip.proxy.registrar;

import gov.nist.sip.proxy.Proxy;
import gov.nist.sip.proxy.ProxyActivator;
import gov.nist.sip.proxy.ProxyDebug;

import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.URI;
import javax.sip.header.ContactHeader;
import javax.sip.header.HeaderFactory;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * parser for a XML file
 */
public class XMLRegistrationsParser extends DefaultHandler
{

    private Registrations registrations;

    private Registration registration;

    private ContactHeader contactHeader;

    private Vector registrationList;

    private XMLReader saxParser;

    private Proxy proxy;

    /**
     * start the parsing
     * 
     * @param file
     *            to parse
     * @return Vector containing the test cases
     */
    public XMLRegistrationsParser(String fileLocation, Proxy proxy)
    {
        try
        {
            this.proxy = proxy;
            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            XMLReader saxParser = saxParserFactory.newSAXParser().getXMLReader();
            saxParser.setContentHandler(this);
            saxParser.setFeature("http://xml.org/sax/features/validation", true);
            // parse the xml specification for the event tags.

            // load from bundle
            String file = "bundle://" + ProxyActivator.bc.getBundle().getBundleId() + ":0" + fileLocation;
            URL fileurl = new URL(file);

            saxParser.parse(fileurl.toString());

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

    public Registrations getRegistrations()
    {
        return registrations;
    }

    //===========================================================
    // SAX DocumentHandler methods
    //===========================================================

    public void startDocument() throws SAXException
    {
        try
        {
            ProxyDebug.println("Parsing XML registrations");
        } catch (Exception e)
        {
            throw new SAXException("XMLRegistrationsParser error", e);
        }
    }

    public void endDocument() throws SAXException
    {
        try
        {
            ProxyDebug.println("XML Registrations parsed successfully!!!");
        } catch (Exception e)
        {
            throw new SAXException("XMLRegistrationsParser error", e);
        }
    }

    public void startElement(String namespaceURI, String lName, // local name
            String qName, // qualified name
            Attributes attrs) throws SAXException
    {
        String element = qName;
        AddressFactory addressFactory = proxy.getAddressFactory();
        HeaderFactory headerFactory = proxy.getHeaderFactory();

        if (element.compareToIgnoreCase("registrations") == 0)
        {
            registrations = new Registrations();
            registrationList = new Vector();

        }

        if (element.compareToIgnoreCase("registration") == 0)
        {

            registration = new Registration();

            String displayName = attrs.getValue("displayName");
            if (displayName != null && !displayName.trim().equals(""))
            {
                registration.setDisplayName(displayName);
            } else
            {
                ProxyDebug.println("WARNING, XMLRegistrationsParser, startElement()"
                        + " the displayName attribute is not set for the registration tag!!!");
            }

            String uri = attrs.getValue("uri");
            if (uri != null && !uri.trim().equals(""))
            {
                registration.setKey(uri);
            } else
            {
                throw new SAXException("ERROR, XMLRegistrationsParser, startElement(), "
                        + "the uri attribute has not been specified for the " + "registration tag.");
            }
        }

        if (element.compareToIgnoreCase("CONTACT") == 0)
        {

            String uriString = attrs.getValue("uri");
            if (uriString != null && !uriString.trim().equals(""))
            {
                try
                {

                    URI uri = (URI) addressFactory.createURI(uriString);
                    if (uri == null)
                        throw new SAXException("ERROR: " + "the parsed uri is null!");

                    Address address = null;

                    String displayName = attrs.getValue("displayName");
                    if (displayName != null && !displayName.trim().equals(""))
                    {
                        address = addressFactory.createAddress(displayName, uri);
                    } else
                    {
                        ProxyDebug.println("WARNING, XMLRegistrationsParser, startElement()"
                                + " the displayName attribute is not set for the contact tag!!!");
                        address = addressFactory.createAddress(null, uri);
                    }

                    if (address == null)
                        throw new SAXException("ERROR: " + "the generated address is null!");
                    contactHeader = headerFactory.createContactHeader(address);
                    if (contactHeader == null)
                        throw new SAXException("ERROR: " + "the generated contactHeader is null!");

                    String expires = attrs.getValue("expires");
                    int exp = -1;
                    if (expires != null && !expires.trim().equals(""))
                    {
                        try
                        {
                            exp = Integer.valueOf(expires.trim()).intValue();
                            if (exp > Registrar.EXPIRES_TIME_MAX || exp < Registrar.EXPIRES_TIME_MIN)
                            {
                                ProxyDebug
                                        .println("WARNING, XMLRegistrationsParser, startElement(),"
                                                + " the contact expires value is not in the good range. Default expires value used:"
                                                + Registrar.EXPIRES_TIME_MAX);
                                exp = Registrar.EXPIRES_TIME_MAX;
                            }
                            contactHeader.setExpires(exp);
                        } catch (Exception e)
                        {
                            ProxyDebug.println("WARNING, XMLRegistrationsParser, startElement(),"
                                    + " error parsing the expires value, "
                                    + "the contact expires value is not well-formated. Default expires value used:"
                                    + Registrar.EXPIRES_TIME_MAX);
                            exp = Registrar.EXPIRES_TIME_MAX;
                            contactHeader.setExpires(exp);
                            e.printStackTrace();
                        }
                    } else
                    {
                        ProxyDebug.println("WARNING, XMLRegistrationsParser, startElement(),"
                                + "the contact expires value is not specified. Default expires value used:"
                                + Registrar.EXPIRES_TIME_MAX);
                        exp = Registrar.EXPIRES_TIME_MAX;
                        contactHeader.setExpires(exp);
                    }

                } catch (Exception e)
                {
                    throw new SAXException("ERROR, XMLRegistrationsParser, startElement(), "
                            + "the contact uri attribute has not been parsed correctly: " + e.getMessage());
                }
            } else
            {
                throw new SAXException("ERROR, XMLRegistrationsParser, startElement(), "
                        + "the uri attribute has not been specified for the contact tag!!!");
            }

        }
    }

    public void endElement(String namespaceURI, String sName, // simple name
            String qName // qualified name
    ) throws SAXException
    {
        String element = qName;

        if (element.compareToIgnoreCase("registrations") == 0)
        {
            registrations.registrationList = registrationList;

        }
        if (element.compareToIgnoreCase("registration") == 0)
        {
            registrationList.addElement(registration);

        }

        if (element.compareToIgnoreCase("contact") == 0)
        {

            if (contactHeader != null)
            {

                if (registration != null)
                {

                    registration.addContactHeader(contactHeader);

                } else
                    throw new SAXException("ERROR, XMLRegistrationsParser, startElement(), "
                            + "Registration object is null!!! Cannot add the registration");
            } else
                throw new SAXException("ERROR, XMLRegistrationsParser, startElement(), "
                        + "the contact Header is null!!! Cannot add the registration");
        }

    }

    public void characters(char buf[], int offset, int len) throws SAXException
    {
        String str = new String(buf, offset, len);
    }

}