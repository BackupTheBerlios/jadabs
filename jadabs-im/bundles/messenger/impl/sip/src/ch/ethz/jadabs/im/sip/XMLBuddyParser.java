package ch.ethz.jadabs.im.sip;

import java.util.Vector;

import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/** parser for a XML file
 */
public class XMLBuddyParser extends DefaultHandler {
	private static Logger logger = Logger.getLogger(XMLBuddyParser.class.getName());
	
	private User user;
	private Vector buddies;
	private XMLReader saxParser;

	private String fileLocation;
	/** start the parsing
	 * @param file to parse
	 * @return Vector containing the test cases
	 */
	public XMLBuddyParser(String fileLocation) {
		try {
			this.fileLocation = fileLocation;
			SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
			saxParser = saxParserFactory.newSAXParser().getXMLReader();
			saxParser.setContentHandler(this);
			saxParser.setFeature("http://xml.org/sax/features/validation", true);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void parse () {
		try {
			// parse the xml specification for the event tags.
			saxParser.parse(fileLocation);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Vector getBuddies() {
		return buddies;
	}

	//===========================================================
	// SAX DocumentHandler methods
	//===========================================================

	public void startDocument() throws SAXException {
		try {
			logger.info("Parsing XML buddies");
		} catch (Exception e) {
			buddies = null;
			throw new SAXException("XMLBuddyParser error", e);
		}
	}

	public void endDocument() throws SAXException {
		try {
			logger.info("XML buddies parsed successfully!!!");
		} catch (Exception e) {
			throw new SAXException("XMLBuddyParser error", e);
		}
	}

	public void startElement(String namespaceURI, String lName, // local name
			String qName, // qualified name
			Attributes attrs) throws SAXException {
		String element = qName;
		if (element.compareToIgnoreCase("buddies") == 0) {
			buddies = new Vector();
		}
		if (element.compareToIgnoreCase("buddy") == 0) {
			user = new User();
			String name = attrs.getValue("name");
			if (name != null && !name.trim().equals(""))
				user.setName(name.trim());
			else {
				logger.debug("Name not correct.");
				throw new SAXException("ERROR parsing the buddy");
			}
		}
		if (element.compareToIgnoreCase("sip") == 0) {
			String alias = attrs.getValue("alias");
			if (alias != null && !alias.trim().equals("") /*&& IMUtilities.checkURI(alias)*/) {
				user.addSIPAddress(alias);
			}
			else {
				logger.debug("Alias not correct.");
				throw new SAXException("ERROR parsing the buddy");
			}
		}
	}

	public void endElement(String namespaceURI, String sName, // simple name
			String qName // qualified name
	) throws SAXException {
		String element = qName;
		if (element.compareToIgnoreCase("buddies") == 0) {
		}
		if (element.compareToIgnoreCase("buddy") == 0) {
			buddies.addElement(user);
		}
	}

	public void characters(char buf[], int offset, int len) throws SAXException {
		
	}
}