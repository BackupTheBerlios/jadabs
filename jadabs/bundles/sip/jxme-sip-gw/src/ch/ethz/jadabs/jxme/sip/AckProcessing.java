/*
 * AckProcessing.java
 *
 * Created on September 25, 2002, 11:28 PM
 */

package ch.ethz.jadabs.jxme.sip;

import javax.sip.ServerTransaction;
import javax.sip.message.Request;

/**
 * 
 * @author olivier, andfrei
 * @version 1.0
 */
public class AckProcessing
{

    

    /** Creates new AckProcessing */
    public AckProcessing(SipGatewayImpl sipgw)
    {
    }

    public void processAck(Request request, ServerTransaction serverTransaction)
    {
        try
        {

        } catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

}