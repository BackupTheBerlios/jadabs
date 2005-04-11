package ch.ethz.jadabs.sbbproxy;


import org.apache.log4j.Logger;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.*;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.io.*;


/**
 * The class SBBService works as a proxy for communication with the SBB Server.
 * A message received through the JXME EndpointService will be forwarded to
 * the SBB Server and the response object will be sent back to the client.
 * The SBBService is called by the client when a new request for the SBB
 * timetable has to be handled.
 *
 * @author Franz Maier
 */

public class SBBService implements Listener {

    private EndpointAddress fEndpoint;
    private EndpointService fEndptsvc;
    private static Logger LOG;
    private static final String SBB_WEBSERVICE_URL = "http://wlab.ethz.ch:8080/axis/services/SBBWebService";

    public SBBService() {
        LOG = Logger.getLogger("SBBService");
    }


    /**
     * Listener-Method is called by the JXME EndpointService when a
     * message has to be sent to the SBB Server .
     *
     * @param message The JXME message to be send
     * @param args    additional arguments (required by the listener interface but are
     *                ignored in this case)
     */
    public void handleMessage(Message message, String args) {

        LOG.debug(new String(message.getElement("SOAP_REQUEST_TAG").getData()));
        try {
            String response = forwardSoapObjectToServer(new String(message.getElement("SOAP_REQUEST_TAG").getData()), new URL(SBB_WEBSERVICE_URL));
            LOG.debug(response);
            forwardSBBResponseToClient(response);
        } catch (MalformedURLException mue) {
            LOG.debug(mue.getMessage());
        }
    }

    /**
     * proxy-method is called when a new SoapMessage
     * arrives through the JXME EndpointService. The Message
     * will be forwarded to the SBB Server.
     *
     * @param string The JXME message received from the client
     * @param url    The Url where the SBBWebService runs
     */
    public String forwardSoapObjectToServer(String string, URL url) {
        String response = "";
        try {
            byte[] soapMessage = string.getBytes();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("User-Agent", "ETH Jadabs Proxy for kSOAP/Axis");
            connection.setRequestProperty("SOAPAction", "");
            connection.setRequestProperty("Content-Type", "text/xml");
            connection.setRequestProperty("Connection", "close");
            connection.setRequestProperty("Content-Length", "" + soapMessage.length);
            connection.setRequestMethod("POST");

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(soapMessage, 0, soapMessage.length);
            outputStream.close();
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            while (reader.ready())
                response += reader.readLine() + '\n';

            inputStream.close();
            connection.disconnect();

        } catch (Exception e) {
            System.out.println(e);
        }
        return response;
    }

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
            LOG.debug((forwardSoapObjectToServer(soapMessage, new URL(SBB_WEBSERVICE_URL))));
        } catch (MalformedURLException mue) {
            LOG.debug(mue.getMessage());
        }
    }

    /**
     * Method to send the response, received by the SBB Server
     * back to the client through the JXME EndpointService.
     *
     * @param response The response Object which will be sent back to the client.
     */
    public void forwardSBBResponseToClient(String response) {
        Element[] elms = new Element[1];
        elms[0] = new Element("SOAP_RESPONSE_TAG", response, Element.TEXTUTF8_MIME_TYPE);
        try {
            ServiceReference sref = SBBServiceActivator.bc.getServiceReference("ch.ethz.jadabs.jxme.EndpointService");
            fEndptsvc = (EndpointService) SBBServiceActivator.bc.getService(sref);
            fEndpoint = new EndpointAddress("btspp", "anybody", -1, "sbbmidlet", null);
            fEndptsvc.propagate(elms, fEndpoint);
        } catch (MalformedURIException mue) {
            LOG.debug(mue.getMessage());
        } catch (IOException ioe) {
            LOG.debug(ioe.getMessage());
        }
    }

    public void handleSearchResponse(NamedResource res) {
    }
}

