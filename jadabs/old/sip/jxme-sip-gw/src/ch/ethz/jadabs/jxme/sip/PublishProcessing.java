/*
 * IMPublishProcessing.java
 */

package ch.ethz.jadabs.jxme.sip;

import gov.nist.javax.sip.Utils;

import java.util.Vector;

import javax.sip.ClientTransaction;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
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

public class PublishProcessing
{
    private Logger LOG = Logger.getLogger(PublishProcessing.class);
    
    private SipGatewayImpl sipgw;

    private int callIdCounter;

    /** A unique id used to identify this entity in a pidf-document * */
    private String entity;

    public PublishProcessing(SipGatewayImpl sipgw)
    {
        this.sipgw = sipgw;
        this.callIdCounter = 0;
        this.entity = "NistSipIM_" + Utils.generateTag();
    }

    public void sendPublish(String localURI, String status)
    {
        try
        {
            LOG.debug("Sending PUBLISH in progress");
            int proxyPort = sipgw.getProxyPort();
            String proxyAddress = sipgw.getProxyAddress();
            String imProtocol = sipgw.getIMProtocol();
            SipStack sipStack = sipgw.getSipStack();
            SipProvider sipProvider = sipgw.getSipProvider();
            MessageFactory messageFactory = sipgw.getMessageFactory();
            HeaderFactory headerFactory = sipgw.getHeaderFactory();
            AddressFactory addressFactory = sipgw.getAddressFactory();

            // Request-URI:
            if (localURI.startsWith("sip:"))
                localURI = localURI.substring(4, localURI.length());
            SipURI requestURI = addressFactory.createSipURI(null, localURI);
            requestURI.setPort(proxyPort);
            requestURI.setTransportParam(imProtocol);

            //  Via header
            String branchId = Utils.generateBranchId();
            ViaHeader viaHeader = headerFactory.createViaHeader(sipgw.getIMAddress(), sipgw.getIMPort(), imProtocol,
                    branchId);
            Vector viaList = new Vector();
            viaList.addElement(viaHeader);

            // To header:
            System.out.println("XXX localURI=" + localURI);
            Address localAddress = addressFactory.createAddress("sip:" + localURI);
            ToHeader toHeader = headerFactory.createToHeader(localAddress, null);

            // From header:
            String localTag = Utils.generateTag();
            FromHeader fromHeader = headerFactory.createFromHeader(localAddress, localTag);

            // Call-ID:
            CallIdHeader callIdHeader = headerFactory.createCallIdHeader(callIdCounter + localURI);

            // CSeq:
            CSeqHeader cseqHeader = headerFactory.createCSeqHeader(1, "PUBLISH");

            // MaxForwards header:
            MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);

            //Create Request
            Request request = messageFactory.createRequest(requestURI, "PUBLISH", callIdHeader, cseqHeader, fromHeader,
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
                    + "<presence xmlns=\"urn:ietf:params:xml:ns:pidf\"" + " entity=\"" + localURI + "\">\n"
                    + " <tuple id=\"" + entity + "\">\n" + "  <status>\n" + "   <basic>" + basic + "</basic>\n"
                    + "  </status>\n" + "  <note>" + status + "</note>\n" + " </tuple>\n" + "</presence>";

            ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "pidf+xml");
            request.setContent(content, contentTypeHeader);

            // Content-Length header:
            ContentLengthHeader contentLengthHeader = headerFactory.createContentLengthHeader(content.length());
            request.setContentLength(contentLengthHeader);

            // Send request
            ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(request);
            clientTransaction.sendRequest();

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}

