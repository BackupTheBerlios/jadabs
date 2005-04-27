package ch.ethz.jadabs.sbbservice;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.EndpointService;
import ch.ethz.jadabs.sbbproxy.SBBService;

/**
 * This class has to be started by the OSGI framework to make use of the
 * SBBService. It implements BundleActivator and holds a ServiceReference to the
 * JXME EndpointService and the SBBService. To use the SBB timetable inquiry,
 * this Bundle has to be started.
 * 
 * @author Franz Maier
 */
public class SBBServiceActivator implements BundleActivator
{
    private static Logger LOG = Logger.getLogger(SBBServiceActivator.class.getName());

    public static BundleContext bc = null;

    private EndpointService fEndptsvc;

    private SBBService sbbservice;

    
    public SBBServiceActivator()
    {
    }

    public void start(BundleContext bc) throws Exception
    {

        SBBServiceActivator.bc = bc;
        sbbservice = new SBBService();

        bc.registerService("ch.ethz.jadabs.sbbservice.SBBService", sbbservice, null);

        ServiceReference sref = bc.getServiceReference("ch.ethz.jadabs.jxme.EndpointService");
        fEndptsvc = (EndpointService) bc.getService(sref);

        sref = bc.getServiceReference("ch.ethz.jadabs.sbbservice.SBBService");
        sbbservice = (SBBService) bc.getService(sref);

        fEndptsvc.addListener("sbbservice", sbbservice);
        LOG.debug(bc.getServiceReference("ch.ethz.jadabs.sbbservice.SBBService"));
    }

    public void stop(BundleContext bc) throws Exception
    {
        SBBServiceActivator.bc = null;
    }
}