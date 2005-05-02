package ch.ethz.jadabs.ws.proxy;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import ch.ethz.jadabs.jxme.EndpointService;

/**
 * Activates the SBB Proxy. This bundle is used by the SBB service to get the
 * response from the SBB server.
 * 
 * @author Franz Maier
 */

public class SBBProxyActivator implements BundleActivator
{
    private static Logger LOG = Logger.getLogger(SBBProxyActivator.class.getName());

//    protected static final String SBB_WEBSERVICE_URL = "http://wlab.ethz.ch:8080/axis/services/SBBWebService";
    protected static final String SBB_WEBSERVICE_URL = "http://localhost:8081/axis/services/SBBWebService";

    private SBBService sbbservice;
    
    private String response;

    public BundleContext bundleContext = null;
    
    protected static EndpointService fEndptsvc;
    
    protected static SBBProxyActivator sbbproxy;


    public void start(BundleContext bc) throws Exception
    {

        this.bundleContext = bc;
        sbbproxy = this;

        // get Endpoint service
        ServiceReference fServiceReference = bc.getServiceReference("ch.ethz.jadabs.jxme.EndpointService");
        fEndptsvc = (EndpointService) bc.getService(fServiceReference);

        sbbservice = new SBBService();
        fEndptsvc.addListener("sbbservice", sbbservice);
        
    }

    public void stop(BundleContext bc)
    {
        bc = null;
    }

    public String getSoapResponse(String message, String args)
    {
        //LOG.debug(message);
        try
        {
            response = forwardSoapObjectToServer(message, new URL(SBB_WEBSERVICE_URL));
        } catch (MalformedURLException mue)
        {
            LOG.debug(mue.getMessage());
        }
        return response;
    }

    /**
     * proxy-method is called when a new SoapMessage arrives through the JXME
     * EndpointService. The Message will be forwarded to the SBB Server.
     * 
     * @param string
     *            The JXME message received from the client
     * @param url
     *            The Url where the SBBWebService runs
     */
    public String forwardSoapObjectToServer(String string, URL url)
    {
        String response = "";
        try
        {
            byte[] soapMessage = string.getBytes();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("User-Agent", "ETH Jadabs SBBProxyActivator for kSOAP/Axis");
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

        } catch (Exception e)
        {
            System.out.println(e);
        }
        return response;
    }
}