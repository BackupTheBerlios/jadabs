package ch.ethz.jadabs.ws.proxy;

import org.apache.log4j.Logger;

import ch.ethz.jadabs.jxme.Element;
import ch.ethz.jadabs.jxme.Listener;
import ch.ethz.jadabs.jxme.Message;
import ch.ethz.jadabs.jxme.NamedResource;

import java.io.IOException;

/**
 * The SBBService handles the communication with the SBB Server. A message
 * received through the JXME EndpointService will be forwarded to the SBB Server
 * and the response object will be sent back to the client. The SBBService is
 * called by the client when a new request for the SBB timetable has to be
 * handled.
 * 
 * @author Franz Maier
 */

public class SBBService implements Listener
{

    private static Logger LOG = Logger.getLogger(SBBService.class.getName());
    
//    private SBBProxyActivator fSBBProxyActivator = new SBBProxyActivator();

//    private EndpointAddress fEndpoint;
//
//    private EndpointService fEndptsvc;

    public SBBService()
    {
    }

    /**
     * Listener-Method is called by the JXME EndpointService when a message has
     * to be sent to the SBB Server .
     * 
     * @param message
     *            The JXME message to be send
     * @param args
     *            additional arguments (required by the listener interface but
     *            are ignored in this case)
     */
    public void handleMessage(Message message, String args)
    {        
        Element elm = message.getElement("OPIPE_TAG");
        
        if (elm != null)
        {
	        String soapString = new String(message.getElement("SOAP_REQUEST_TAG").getData());
	        String response = SBBProxyActivator.sbbproxy.getSoapResponse(soapString, args);
	        
	        forwardSBBResponseToClient(response);
        
        }
    }

    /**
     * Method to send the response, received by the SBB Server back to the
     * client through the JXME EndpointService.
     * 
     * @param response
     *            The response Object which will be sent back to the client.
     */
    public void forwardSBBResponseToClient(String response)
    {
        Element[] elms = new Element[2];
        elms[0] = new Element("OPIPE_TAG", "in", Element.TEXTUTF8_MIME_TYPE);
        elms[1] = new Element("SOAP_RESPONSE_TAG", response, Element.TEXTUTF8_MIME_TYPE);

        try
        {
//            fEndpoint = new EndpointAddress("btspp", "anybody", -1, "sbbmidlet", null);
//            SBBProxyActivator.fEndptsvc.propagate(elms, fEndpoint);
        
            SBBProxyActivator.groupService.send(SBBProxyActivator.groupPipe, new Message(elms));
        } catch (IOException ioe)
        {
            LOG.debug(ioe.getMessage());
        }
    }

    public void handleSearchResponse(NamedResource res)
    {
    }
}