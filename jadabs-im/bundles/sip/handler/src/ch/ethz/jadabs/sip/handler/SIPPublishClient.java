/*
 * IMPublishProcessing.java
 */

package ch.ethz.jadabs.sip.handler;

import gov.nist.javax.sip.Utils;

import java.util.Vector;

import javax.sip.ClientTransaction;
import javax.sip.SipProvider;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;

import org.apache.log4j.Logger;

/**
 * The Publish is NOT supported by the Jain SIP 1.1. This is here for
 * experimental purposes.
 * 
 * @author Henrik Leion
 * @version 0.1
 */

public class SIPPublishClient
{
	private static Logger logger = Logger.getLogger(SIPPublishClient.class);
	
    private IMUserAgent sipUAClient;

    private int callIdCounter;

    /** A unique id used to identify this entity in a pidf-document * */
    private String entity;

    public SIPPublishClient(IMUserAgent sipUAClient)
    {
        this.sipUAClient = sipUAClient;
        this.callIdCounter = 0;
        this.entity = "NistSipIM_" + sipUAClient.getEpid();
    }

    public void sendPublish(String status)
    {
        try
        {
            SipProvider sipProvider = sipUAClient.getSipProvider();
            MessageFactory messageFactory = sipUAClient.getMessageFactory();
            HeaderFactory headerFactory = sipUAClient.getHeaderFactory();
            AddressFactory addressFactory = sipUAClient.getAddressFactory();

            // Request-URI:
            Address requestAddress = addressFactory.createAddress("sip:"+sipUAClient.getRegistrar());

            //  Via header
            String branchId = Utils.generateBranchId();
            ViaHeader viaHeader = headerFactory.createViaHeader(sipUAClient.getIpAddress(), sipUAClient.getPort(), sipUAClient.getLocalProtocol(),
                    branchId);
            Vector viaList = new Vector();
            viaList.addElement(viaHeader);

            // To header:
            Address localAddress = addressFactory.createAddress(sipUAClient.getLocalURI());
            String localTag = Utils.generateTag();
            ToHeader toHeader = headerFactory.createToHeader(localAddress, localTag);

            // From header:
            localTag = Utils.generateTag();
            FromHeader fromHeader = headerFactory.createFromHeader(localAddress, localTag);

            // Call-ID:
            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            // CSeq:
            CSeqHeader cseqHeader = headerFactory.createCSeqHeader(1, "PUBLISH");

            // MaxForwards header:
            MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);

            //Create Request
            Request request = messageFactory.createRequest(requestAddress.getURI(), "PUBLISH", callIdHeader, cseqHeader, fromHeader,
                    toHeader, viaList, maxForwardsHeader);

            // Expires header: (none, let server chose)

            // Event header:
            Header header = headerFactory.createHeader("Event", "presence");
            request.setHeader(header);

            // Content and Content-Type header:
            String basic;
            if (status.equals("offline"))
                basic = "closed";
            else
                basic = "open";

            String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<presence xmlns=\"urn:ietf:params:xml:ns:pidf\"" + " entity=\"" + sipUAClient.getLocalURI() + "\">\n"
                    + " <tuple id=\"" + entity + "\">\n" + "  <status>\n" + "   <basic>" + basic + "</basic>\n"
                    + "  </status>\n" + "  <note>" + status + "</note>\n" + " </tuple>\n" + "</presence>";

            ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "pidf+xml");
            request.setContent(content, contentTypeHeader);

            // Content-Length header:
            ContentLengthHeader contentLengthHeader = headerFactory.createContentLengthHeader(content.length());
            request.setContentLength(contentLengthHeader);

    		logger.debug("SENDING: "+request);
	        
            // Send request
            ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(request);
            clientTransaction.sendRequest();

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }


public void sendPublish(String toSipURI, String status)
{
    try
    {
        SipProvider sipProvider = sipUAClient.getSipProvider();
        MessageFactory messageFactory = sipUAClient.getMessageFactory();
        HeaderFactory headerFactory = sipUAClient.getHeaderFactory();
        AddressFactory addressFactory = sipUAClient.getAddressFactory();

        // Request-URI:
        Address requestAddress = addressFactory.createAddress("sip:"+sipUAClient.getRegistrar());

        //  Via header
        String branchId = Utils.generateBranchId();
        ViaHeader viaHeader = headerFactory.createViaHeader(sipUAClient.getIpAddress(), sipUAClient.getPort(), sipUAClient.getLocalProtocol(),
                branchId);
        Vector viaList = new Vector();
        viaList.addElement(viaHeader);

        // To header:
        Address localAddress = addressFactory.createAddress(toSipURI);
        String localTag = Utils.generateTag();
        ToHeader toHeader = headerFactory.createToHeader(localAddress, localTag);

        // From header:
        localTag = Utils.generateTag();
        FromHeader fromHeader = headerFactory.createFromHeader(localAddress, localTag);

        // Call-ID:
        CallIdHeader callIdHeader = sipProvider.getNewCallId();

        // CSeq:
        CSeqHeader cseqHeader = headerFactory.createCSeqHeader(1, "PUBLISH");

        // MaxForwards header:
        MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);

        //Create Request
        Request request = messageFactory.createRequest(requestAddress.getURI(), "PUBLISH", callIdHeader, cseqHeader, fromHeader,
                toHeader, viaList, maxForwardsHeader);

        // Expires header: (none, let server chose)

        // Event header:
        Header header = headerFactory.createHeader("Event", "presence");
        request.setHeader(header);

        // Content and Content-Type header:
        String basic;
        if (status.equals("offline"))
            basic = "closed";
        else
            basic = "open";

        String content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<presence xmlns=\"urn:ietf:params:xml:ns:pidf\"" + " entity=\"" + sipUAClient.getLocalURI() + "\">\n"
                + " <tuple id=\"" + entity + "\">\n" + "  <status>\n" + "   <basic>" + basic + "</basic>\n"
                + "  </status>\n" + "  <note>" + status + "</note>\n" + " </tuple>\n" + "</presence>";

        ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "pidf+xml");
        request.setContent(content, contentTypeHeader);

        // Content-Length header:
        ContentLengthHeader contentLengthHeader = headerFactory.createContentLengthHeader(content.length());
        request.setContentLength(contentLengthHeader);

		logger.debug("SENDING: "+request);
        
        // Send request
        ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(request);
        clientTransaction.sendRequest();

    } catch (Exception ex)
    {
        ex.printStackTrace();
    }
}

}
