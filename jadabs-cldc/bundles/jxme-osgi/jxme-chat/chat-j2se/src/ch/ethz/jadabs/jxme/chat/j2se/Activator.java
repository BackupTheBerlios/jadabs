/* Created on Jul 14, 2004
 * $Id: Activator.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 * $Log: Activator.java,v $
 * Revision 1.1  2004/11/10 10:28:13  afrei
 * initial checkin to berlios.de
 *
 * Revision 1.2  2004/10/17 19:19:10  iksgst3
 * Mueller Rene: documentation of JXME-Chat
 *
 * Revision 1.1  2004/10/13 20:49:23  iksgst3
 * Mueller Rene: inital commit after splitting Jadabs-CLDC from main Jadabs tree
 *
 * Revision 1.1  2004/08/02 15:44:23  iksgst3
 * Rene Mueller: implementation of bt-cldc jxme-chat and jxme-chat-j2se
 *
 * Revision 1.1  2004/07/14 18:38:47  iksgst3
 * initial checkin
 *
 */

package ch.ethz.jadabs.jxme.chat.j2se;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.EndpointService;

/**
 * This is the OSGi Activator for the BlueChat Application.
 * 
 * @author muellerr
 */
public class Activator implements BundleActivator
{

    /** The log4j logging mechanism that will be used in the activator */
    protected static Logger LOG = Logger.getLogger(Activator.class.getName());

    /** the application singleton */
    private JxmeChat app;

    /** EndpointService used in the chat application */
    private EndpointService endptsvc;

    /**
     * life cycle method called to start the bundle
     * 
     * @param bc
     *            reference to the context in the bundle container
     */
    public void start(BundleContext bc) throws Exception
    {
        // get Endpoint service
        ServiceReference sref = bc.getServiceReference("ch.ethz.jadabs.jxme.EndpointService");
        endptsvc = (EndpointService)bc.getService(sref);
        app = new JxmeChat(endptsvc);
    }

    /**
     * life cycle method called to stop the bundle
     * 
     * @param bc
     *            reference to the context in the bundle container
     */
    public void stop(BundleContext bc) throws Exception
    {
        app.shutdown();
        endptsvc.removeListener("jxmechat");
    }

}