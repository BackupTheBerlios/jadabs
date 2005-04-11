/*
 * Subscriber.java
 *
 * Created on September 26, 2002, 6:00 PM
 */

package ch.ethz.jadabs.im.sip;

import javax.sip.Dialog;
import javax.sip.message.Response;

/**
 * 
 * @author deruelle
 * @version 1.0
 */
public class Subscriber
{

    private String subscriberName;

    private Response okSent;

    private Dialog dialog;

    /** Creates new Subscriber */
    public Subscriber(String subscriberName, Response okSent)
    {
        this.subscriberName = subscriberName;
        this.okSent = okSent;
    }

    public Dialog getDialog()
    {
        return dialog;
    }

    public void setDialog(Dialog dialog)
    {
        this.dialog = dialog;
    }

    public Response getOkSent()
    {
        return okSent;
    }

    public String getSubscriberName()
    {
        return subscriberName;
    }

}