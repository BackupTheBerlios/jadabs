package ch.ethz.jadabs.sbbservice;


import ch.ethz.jadabs.jxme.*;
import ch.ethz.jadabs.sbbproxy.SBBProxyActivator;
import org.osgi.framework.ServiceReference;
import org.apache.log4j.Logger;

import java.io.IOException;


/**
 * The SBBService handles the communication with the SBB Server.
 * A message received through the JXME EndpointService will be forwarded to
 * the SBB Server and the response object will be sent back to the client.
 * The SBBService is called by the client when a new request for the SBB
 * timetable has to be handled.
 *
 * @author Franz Maier
 */

public class SBBService implements Listener {

    private SBBProxyActivator fSBBProxyActivator = new SBBProxyActivator();
    private EndpointAddress fEndpoint;
    private EndpointService fEndptsvc;
    private static Logger LOG;

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
        String soapString = new String(message.getElement("SOAP_REQUEST_TAG").getData());
        String response = fSBBProxyActivator.getSoapResponse(soapString, args);
        LOG.debug(response);
        forwardSBBResponseToClient(response);
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
        } catch (IOException ioe) {
            LOG.debug(ioe.getMessage());
        }
    }

    public void handleSearchResponse(NamedResource res) {
    }
}