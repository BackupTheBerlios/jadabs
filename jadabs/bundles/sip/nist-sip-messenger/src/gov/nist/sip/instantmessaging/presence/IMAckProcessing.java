/*
 * AckProcessing.java
 *
 * Created on September 25, 2002, 11:28 PM
 */

package gov.nist.sip.instantmessaging.presence;

import javax.sip.ServerTransaction;
import javax.sip.message.Request;

/**
 * 
 * @author olivier
 * @version 1.0
 */
public class IMAckProcessing
{

    private IMUserAgent imUA;

    /** Creates new AckProcessing */
    public IMAckProcessing(IMUserAgent imUA)
    {
        this.imUA = imUA;
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