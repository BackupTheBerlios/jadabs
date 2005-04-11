package ch.ethz.jadabs.sbbproxy;


import org.apache.log4j.Logger;

import java.net.URL;
import java.net.MalformedURLException;


/**
 * Date: 21.01.2005 21:58:18
 * To change this template use Options | File Templates.
 *
 * @author Franz Maier
 */

public class ProxyTest {

    private static Logger LOG;

    /**
     * Test method for a SoapMessage
     *
     * @param request The SoapMessage which will be sent to the SBB Server.
     */
    public void testForwardSBBQueryToServer(String request) {
        StringBuffer soapStrBuf = new StringBuffer();
        soapStrBuf.append("<SOAP-ENV:Envelope xmlns:n0=\"http://sbb.webservices.jadabs.ethz.ch\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">").append('\n');
        soapStrBuf.append("<SOAP-ENV:Body SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">").append('\n');
        soapStrBuf.append("<queryTimetable xmlns=\"SBBWebService\" id=\"o0\" SOAP-ENC:root=\"1\">").append('\n');
        soapStrBuf.append("<TimetableQuery xmlns=\"\" xsi:type=\"n0:TimetableQuery\">").append('\n');
        soapStrBuf.append("<from xsi:type=\"xsd:string\">Z%C3%BCrich</from>").append('\n');
        soapStrBuf.append("<to xsi:type=\"xsd:string\">Niederweningen</to>").append('\n');
        soapStrBuf.append("<date xsi:type=\"xsd:string\">10.11.2005</date>").append('\n');
        soapStrBuf.append("<time xsi:type=\"xsd:string\">10%3A00</time>").append('\n');
        soapStrBuf.append("<timeToggle xsi:type=\"xsd:int\">1</timeToggle>").append('\n');
        soapStrBuf.append("<details xsi:type=\"xsd:int\">0</details>").append('\n');
        soapStrBuf.append("</TimetableQuery>").append('\n');
        soapStrBuf.append("</queryTimetable>").append('\n');
        soapStrBuf.append("</SOAP-ENV:Body>").append('\n');
        soapStrBuf.append("</SOAP-ENV:Envelope>").append('\n');

        String soapMessage = soapStrBuf.toString();

        try {
            LOG.debug((new Proxy().forwardSoapObjectToServer(soapMessage, new URL(Proxy.SBB_WEBSERVICE_URL))));
        } catch (MalformedURLException mue) {
            LOG.debug(mue.getMessage());
        }
    }
}
